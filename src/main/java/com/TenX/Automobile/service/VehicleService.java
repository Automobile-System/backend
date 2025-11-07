package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.VehicleRequest;
import com.TenX.Automobile.dto.response.VehicleResponse;
import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.exception.DuplicateResourceException;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.repository.VehicleRepository;
import com.TenX.Automobile.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


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
     * Get all vehicles for a customer
     */
    public List<VehicleResponse> getCustomerVehicles(String email) {
        log.info("Fetching vehicles for customer: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        List<Vehicle> vehicles = vehicleRepository.findAllByCustomerId(customer.getId());

        return vehicles.stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a new vehicle for a customer
     */
    public VehicleResponse addVehicle(String email, VehicleRequest request) {
        log.info("Adding vehicle for customer: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Check if registration number already exists
        if (vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }

        // Validate input
        validateVehicleRequest(request);

        Vehicle vehicle = Vehicle.builder()
                .registration_No(request.getRegistrationNo().trim().toUpperCase())
                .brandName(request.getBrandName().trim())
                .model(request.getModel().trim())
                .capacity(request.getCapacity())
                .customer(customer)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle added successfully with ID: {}", savedVehicle.getV_Id());

        return mapToVehicleResponse(savedVehicle);
    }

    /**
     * Update a vehicle
     */
    public VehicleResponse updateVehicle(String email, UUID vehicleId, VehicleRequest request) {
        log.info("Updating vehicle {} for customer: {}", vehicleId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to this customer"));

        // Check if registration number is being changed and if it already exists
        if (!vehicle.getRegistration_No().equals(request.getRegistrationNo()) &&
            vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }

        // Validate input
        validateVehicleRequest(request);

        // Update fields
        vehicle.setRegistration_No(request.getRegistrationNo().trim().toUpperCase());
        vehicle.setBrandName(request.getBrandName().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setCapacity(request.getCapacity());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully: {}", vehicleId);

        return mapToVehicleResponse(updatedVehicle);
    }

    /**
     * Delete a vehicle
     */
    public void deleteVehicle(String email, UUID vehicleId) {
        log.info("Deleting vehicle {} for customer: {}", vehicleId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to this customer"));

        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully: {}", vehicleId);
    }

    /**
     * Validate vehicle request
     */
    private void validateVehicleRequest(VehicleRequest request) {
        if (request.getRegistrationNo() == null || request.getRegistrationNo().trim().isEmpty()) {
            throw new RuntimeException("Registration number is required");
        }

        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            throw new RuntimeException("Brand name is required");
        }

        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new RuntimeException("Model is required");
        }

        if (request.getCapacity() < 1) {
            throw new RuntimeException("Capacity must be at least 1");
        }
    }

    /**
     * Map Vehicle entity to VehicleResponse DTO
     */
    private VehicleResponse mapToVehicleResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getV_Id())
                .registrationNo(vehicle.getRegistration_No())
                .brandName(vehicle.getBrandName())
                .model(vehicle.getModel())
                .capacity(vehicle.getCapacity())
                .createdBy(vehicle.getCreatedBy())
                .customerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getId() : null)
                .customerEmail(vehicle.getCustomer() != null ? vehicle.getCustomer().getEmail() : null)
                .createdAt(null) // Vehicle entity doesn't have createdAt field
                .build();
    }

    // Additional methods for VehicleController

    /**
     * Get all vehicles
     * @return List of all vehicles
     */
    public List<VehicleResponse> getAllVehicles() {
        log.info("Fetching all vehicles");
        return vehicleRepository.findAll().stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle by ID
     * @param vehicleId The vehicle ID
     * @return VehicleResponse
     */
    public VehicleResponse getVehicleById(UUID vehicleId) {
        log.info("Fetching vehicle by ID: {}", vehicleId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Get vehicle by registration number
     * @param registrationNo The registration number
     * @return VehicleResponse
     */
    public VehicleResponse getVehicleByRegistrationNo(String registrationNo) {
        log.info("Fetching vehicle by registration number: {}", registrationNo);
        Vehicle vehicle = vehicleRepository.findByRegistration_No(registrationNo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with registration number: " + registrationNo));
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Get all vehicles for a specific customer by customer ID
     * @param customerId The customer ID
     * @return List of vehicles belonging to the customer
     */
    public List<VehicleResponse> getVehiclesByCustomerId(UUID customerId) {
        log.info("Fetching vehicles for customer ID: {}", customerId);
        
        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        return vehicleRepository.findByCustomer(customer).stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new vehicle for a specific customer (UUID-based)
     * @param request The vehicle request
     * @param customerId The customer ID
     * @return Created vehicle response
     */
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, UUID customerId) {
        log.info("Creating vehicle for customer ID: {}", customerId);
        
        // Validate request
        validateVehicleRequest(request);
        
        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        // Check for duplicate registration number
        if (vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new DuplicateResourceException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }
        
        // Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .registration_No(request.getRegistrationNo())
                .brandName(request.getBrandName())
                .model(request.getModel())
                .capacity(request.getCapacity())
                .customer(customer)
                .build();
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getV_Id());
        
        return mapToVehicleResponse(savedVehicle);
    }

    /**
     * Update a vehicle by ID (UUID-based, without email authentication)
     * @param vehicleId The vehicle ID
     * @param request The update request
     * @return Updated vehicle response
     */
    @Transactional
    public VehicleResponse updateVehicle(UUID vehicleId, VehicleRequest request) {
        log.info("Updating vehicle with ID: {}", vehicleId);
        
        // Validate request
        validateVehicleRequest(request);
        
        // Find vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        
        // Check if registration number is being changed and if it's already taken
        if (!vehicle.getRegistration_No().equals(request.getRegistrationNo()) &&
                vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new DuplicateResourceException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }
        
        // Update vehicle fields
        vehicle.setRegistration_No(request.getRegistrationNo());
        vehicle.setBrandName(request.getBrandName());
        vehicle.setModel(request.getModel());
        vehicle.setCapacity(request.getCapacity());
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully with ID: {}", updatedVehicle.getV_Id());
        
        return mapToVehicleResponse(updatedVehicle);
    }

    /**
     * Delete a vehicle by ID (UUID-based, without email authentication)
     * @param vehicleId The vehicle ID
     */
    @Transactional
    public void deleteVehicle(UUID vehicleId) {
        log.info("Deleting vehicle with ID: {}", vehicleId);
        
        // Find vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully with ID: {}", vehicleId);
    }
}

