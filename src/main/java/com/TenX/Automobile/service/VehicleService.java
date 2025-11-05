package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.repository.VehicleRepository;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CustomerRepository customerRepository;

  /**
   * Create a new vehicle for a customer
   * @param vehicle Vehicle entity to create
   * @param customerId UUID of the customer (UserEntity ID since Customer extends UserEntity)
   * @return Created vehicle
   */
  public Vehicle createVehicle(Vehicle vehicle, UUID customerId) {
    log.info("Creating vehicle with registration number: {} for customer: {}",
      vehicle.getRegistration_No(), customerId);

    // Validate vehicle data
    validateVehicleData(vehicle);

    // Check if registration number already exists
    if (vehicleRepository.existsByRegistration_No(vehicle.getRegistration_No())) {
      throw new DuplicateResourceException(
        "Vehicle with registration number " + vehicle.getRegistration_No() + " already exists"
      );
    }

    // Fetch and validate customer (Customer extends UserEntity, so ID is from UserEntity)
    Customer customer = customerRepository.findById(customerId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Customer not found with id: " + customerId
      ));

    // Ensure customer account is active
    if (!customer.isEnabled()) {
      throw new IllegalStateException("Cannot add vehicle to disabled customer account");
    }

    vehicle.setCustomer(customer);
    Vehicle savedVehicle = vehicleRepository.save(vehicle);

    log.info("Vehicle created successfully with ID: {} for customer: {}",
      savedVehicle.getV_Id(), customerId);
    return savedVehicle;
  }

  /**
   * Get vehicle by ID
   */
  @Transactional(readOnly = true)
  public Vehicle getVehicleById(UUID vehicleId) {
    log.info("Fetching vehicle with ID: {}", vehicleId);
    return vehicleRepository.findById(vehicleId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with id: " + vehicleId
      ));
  }

  /**
   * Get vehicle by registration number
   */
  @Transactional(readOnly = true)
  public Vehicle getVehicleByRegistrationNo(String registrationNo) {
    log.info("Fetching vehicle with registration number: {}", registrationNo);
    return vehicleRepository.findByRegistration_No(registrationNo)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with registration number: " + registrationNo
      ));
  }

  /**
   * Get all vehicles
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getAllVehicles() {
    log.info("Fetching all vehicles");
    return vehicleRepository.findAll();
  }

  /**
   * Get all vehicles by customer ID
   * @param customerId Customer's UUID (from UserEntity ID)
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByCustomerId(UUID customerId) {
    log.info("Fetching vehicles for customer ID: {}", customerId);

    // Verify customer exists
    if (!customerRepository.existsById(customerId)) {
      throw new ResourceNotFoundException("Customer not found with id: " + customerId);
    }

    return vehicleRepository.findByCustomer_Id(customerId);
  }

  /**
   * Get vehicles by customer email (since email is username in UserEntity)
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByCustomerEmail(String email) {
    log.info("Fetching vehicles for customer email: {}", email);

    Customer customer = customerRepository.findByEmail(email)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Customer not found with email: " + email
      ));

    return vehicleRepository.findByCustomer_Id(customer.getId());
  }

  /**
   * Get vehicles by brand
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByBrand(String brandName) {
    log.info("Fetching vehicles by brand: {}", brandName);
    return vehicleRepository.findByBrand_name(brandName);
  }

  /**
   * Get vehicles by model
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByModel(String model) {
    log.info("Fetching vehicles by model: {}", model);
    return vehicleRepository.findByModel(model);
  }

  /**
   * Get vehicles by minimum capacity
   */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByMinimumCapacity(int capacity) {
    log.info("Fetching vehicles with minimum capacity: {}", capacity);
    return vehicleRepository.findByCapacityGreaterThanEqual(capacity);
  }

  /**
   * Update vehicle details
   */
  public Vehicle updateVehicle(UUID vehicleId, Vehicle vehicleDetails) {
    log.info("Updating vehicle with ID: {}", vehicleId);

    Vehicle existingVehicle = getVehicleById(vehicleId);

    // Validate new vehicle data
    validateVehicleData(vehicleDetails);

    // Check if new registration number conflicts with existing one
    if (!existingVehicle.getRegistration_No().equals(vehicleDetails.getRegistration_No())) {
      if (vehicleRepository.existsByRegistration_No(vehicleDetails.getRegistration_No())) {
        throw new DuplicateResourceException(
          "Vehicle with registration number " + vehicleDetails.getRegistration_No() + " already exists"
        );
      }
      existingVehicle.setRegistration_No(vehicleDetails.getRegistration_No());
    }

    existingVehicle.setBrand_name(vehicleDetails.getBrand_name());
    existingVehicle.setModel(vehicleDetails.getModel());
    existingVehicle.setCapacity(vehicleDetails.getCapacity());

    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
    log.info("Vehicle updated successfully with ID: {}", vehicleId);

    return updatedVehicle;
  }

  /**
   * Transfer vehicle to another customer
   * @param vehicleId UUID of vehicle to transfer
   * @param newCustomerId UUID of new customer (UserEntity ID)
   */
  public Vehicle transferVehicleToCustomer(UUID vehicleId, UUID newCustomerId) {
    log.info("Transferring vehicle {} to customer {}", vehicleId, newCustomerId);

    Vehicle vehicle = getVehicleById(vehicleId);

    Customer newCustomer = customerRepository.findById(newCustomerId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Customer not found with id: " + newCustomerId
      ));

    // Ensure new customer account is active
    if (!newCustomer.isEnabled()) {
      throw new IllegalStateException("Cannot transfer vehicle to disabled customer account");
    }

    UUID oldCustomerId = vehicle.getCustomer().getId();
    vehicle.setCustomer(newCustomer);
    Vehicle transferredVehicle = vehicleRepository.save(vehicle);

    log.info("Vehicle {} transferred successfully from customer {} to customer {}",
      vehicleId, oldCustomerId, newCustomerId);
    return transferredVehicle;
  }

  /**
   * Delete vehicle
   */
  public void deleteVehicle(UUID vehicleId) {
    log.info("Deleting vehicle with ID: {}", vehicleId);

    Vehicle vehicle = getVehicleById(vehicleId);
    vehicleRepository.delete(vehicle);

    log.info("Vehicle deleted successfully with ID: {}", vehicleId);
  }

  /**
   * Soft delete - mark vehicle as inactive (if you want to implement soft delete)
   */
  public void deactivateVehicle(UUID vehicleId) {
    log.info("Deactivating vehicle with ID: {}", vehicleId);
    Vehicle vehicle = getVehicleById(vehicleId);
    // You can add an 'active' field to Vehicle entity and set it to false here
    vehicleRepository.save(vehicle);
    log.info("Vehicle deactivated successfully with ID: {}", vehicleId);
  }

  /**
   * Check if vehicle exists by registration number
   */
  @Transactional(readOnly = true)
  public boolean existsByRegistrationNo(String registrationNo) {
    return vehicleRepository.existsByRegistration_No(registrationNo);
  }

  /**
   * Count vehicles by customer
   */
  @Transactional(readOnly = true)
  public long countVehiclesByCustomer(UUID customerId) {
    if (!customerRepository.existsById(customerId)) {
      throw new ResourceNotFoundException("Customer not found with id: " + customerId);
    }
    return vehicleRepository.countByCustomer_Id(customerId);
  }

  /**
   * Check if customer can add more vehicles (business rule example)
   */
  @Transactional(readOnly = true)
  public boolean canCustomerAddVehicle(UUID customerId, int maxVehiclesAllowed) {
    long currentVehicleCount = countVehiclesByCustomer(customerId);
    return currentVehicleCount < maxVehiclesAllowed;
  }

  /**
   * Validate vehicle data
   */
  private void validateVehicleData(Vehicle vehicle) {
    if (vehicle.getRegistration_No() == null || vehicle.getRegistration_No().trim().isEmpty()) {
      throw new IllegalArgumentException("Registration number cannot be empty");
    }

    if (vehicle.getBrand_name() == null || vehicle.getBrand_name().trim().isEmpty()) {
      throw new IllegalArgumentException("Brand name cannot be empty");
    }

    if (vehicle.getModel() == null || vehicle.getModel().trim().isEmpty()) {
      throw new IllegalArgumentException("Model cannot be empty");
    }

    if (vehicle.getCapacity() <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
  }
}
