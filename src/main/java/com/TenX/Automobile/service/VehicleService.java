package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.VehicleRequest;
import com.TenX.Automobile.dto.response.VehicleResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final CustomerRepository customerRepository;

  /**
   * Create a new vehicle for a customer
   * @param vehicleRequest Vehicle DTO to create
   * @param customerId UUID of the customer (UserEntity ID since Customer extends UserEntity)
   * @return Created vehicle response DTO
   */
  public VehicleResponse createVehicle(VehicleRequest vehicleRequest, UUID customerId) {
    log.info("Creating vehicle with registration number: {} for customer: {}",
      vehicleRequest.getRegistrationNo(), customerId);

    // Fetch and validate customer (Customer extends UserEntity, so ID is from UserEntity)
    Customer customer = customerRepository.findById(customerId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Customer not found with id: " + customerId
      ));

    // Ensure customer account is active
    if (!customer.isEnabled()) {
      throw new IllegalStateException("Cannot add vehicle to disabled customer account");
    }

    // Check if registration number already exists
    if (vehicleRepository.existsByRegistration_No(vehicleRequest.getRegistrationNo())) {
      throw new DuplicateResourceException(
        "Vehicle with registration number " + vehicleRequest.getRegistrationNo() + " already exists"
      );
    }

    // Convert DTO to entity
    Vehicle vehicle = new Vehicle();
    vehicle.setRegistration_No(vehicleRequest.getRegistrationNo());
    vehicle.setBrand_name(vehicleRequest.getBrandName());
    vehicle.setModel(vehicleRequest.getModel());
    vehicle.setCapacity(vehicleRequest.getCapacity());
    vehicle.setCustomer(customer);

    Vehicle savedVehicle = vehicleRepository.save(vehicle);

    log.info("Vehicle created successfully with ID: {} for customer: {}",
      savedVehicle.getV_Id(), customerId);
    return convertToResponse(savedVehicle);
  }

  /**
   * Get vehicle by ID
   */
  @Transactional(readOnly = true)
  public VehicleResponse getVehicleById(UUID vehicleId) {
    log.info("Fetching vehicle with ID: {}", vehicleId);
    Vehicle vehicle = vehicleRepository.findById(vehicleId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with id: " + vehicleId
      ));
    return convertToResponse(vehicle);
  }

  /**
   * Get vehicle by registration number
   */
  @Transactional(readOnly = true)
  public VehicleResponse getVehicleByRegistrationNo(String registrationNo) {
    log.info("Fetching vehicle with registration number: {}", registrationNo);
    Vehicle vehicle = vehicleRepository.findByRegistration_No(registrationNo)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with registration number: " + registrationNo
      ));
    return convertToResponse(vehicle);
  }

  /**
   * Get all vehicles
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getAllVehicles() {
    log.info("Fetching all vehicles");
    return vehicleRepository.findAll().stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get all vehicles by customer ID
   * @param customerId Customer's UUID (from UserEntity ID)
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehiclesByCustomerId(UUID customerId) {
    log.info("Fetching vehicles for customer ID: {}", customerId);

    // Verify customer exists
    if (!customerRepository.existsById(customerId)) {
      throw new ResourceNotFoundException("Customer not found with id: " + customerId);
    }

    return vehicleRepository.findByCustomer_Id(customerId).stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get vehicles by customer email (since email is username in UserEntity)
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehiclesByCustomerEmail(String email) {
    log.info("Fetching vehicles for customer email: {}", email);

    Customer customer = customerRepository.findByEmail(email)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Customer not found with email: " + email
      ));

    return vehicleRepository.findByCustomer_Id(customer.getId()).stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get vehicles by brand
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehiclesByBrand(String brandName) {
    log.info("Fetching vehicles by brand: {}", brandName);
    return vehicleRepository.findByBrand_name(brandName).stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get vehicles by model
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehiclesByModel(String model) {
    log.info("Fetching vehicles by model: {}", model);
    return vehicleRepository.findByModel(model).stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get vehicles by minimum capacity
   */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehiclesByMinimumCapacity(int capacity) {
    log.info("Fetching vehicles with minimum capacity: {}", capacity);
    return vehicleRepository.findByCapacityGreaterThanEqual(capacity).stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  /**
   * Update vehicle details
   */
  public VehicleResponse updateVehicle(UUID vehicleId, VehicleRequest vehicleRequest) {
    log.info("Updating vehicle with ID: {}", vehicleId);

    Vehicle existingVehicle = vehicleRepository.findById(vehicleId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with id: " + vehicleId
      ));

    // Check if new registration number conflicts with existing one
    if (!existingVehicle.getRegistration_No().equals(vehicleRequest.getRegistrationNo())) {
      if (vehicleRepository.existsByRegistration_No(vehicleRequest.getRegistrationNo())) {
        throw new DuplicateResourceException(
          "Vehicle with registration number " + vehicleRequest.getRegistrationNo() + " already exists"
        );
      }
      existingVehicle.setRegistration_No(vehicleRequest.getRegistrationNo());
    }

    existingVehicle.setBrand_name(vehicleRequest.getBrandName());
    existingVehicle.setModel(vehicleRequest.getModel());
    existingVehicle.setCapacity(vehicleRequest.getCapacity());

    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
    log.info("Vehicle updated successfully with ID: {}", vehicleId);

    return convertToResponse(updatedVehicle);
  }

  /**
   * Transfer vehicle to another customer
   * @param vehicleId UUID of vehicle to transfer
   * @param newCustomerId UUID of new customer (UserEntity ID)
   */
  public VehicleResponse transferVehicleToCustomer(UUID vehicleId, UUID newCustomerId) {
    log.info("Transferring vehicle {} to customer {}", vehicleId, newCustomerId);

    Vehicle vehicle = vehicleRepository.findById(vehicleId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with id: " + vehicleId
      ));

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
    return convertToResponse(transferredVehicle);
  }

  /**
   * Delete vehicle
   */
  public void deleteVehicle(UUID vehicleId) {
    log.info("Deleting vehicle with ID: {}", vehicleId);

    Vehicle vehicle = vehicleRepository.findById(vehicleId)
      .orElseThrow(() -> new ResourceNotFoundException(
        "Vehicle not found with id: " + vehicleId
      ));
    vehicleRepository.delete(vehicle);

    log.info("Vehicle deleted successfully with ID: {}", vehicleId);
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
   * Convert Vehicle entity to VehicleResponse DTO
   */
  private VehicleResponse convertToResponse(Vehicle vehicle) {
    return VehicleResponse.builder()
      .vehicleId(vehicle.getV_Id())
      .registrationNo(vehicle.getRegistration_No())
      .brandName(vehicle.getBrand_name())
      .model(vehicle.getModel())
      .capacity(vehicle.getCapacity())
      .createdBy(vehicle.getCreatedBy())
      .customerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getId() : null)
      .customerEmail(vehicle.getCustomer() != null ? vehicle.getCustomer().getEmail() : null)
      .build();
  }
}