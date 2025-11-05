package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.VehicleRequest;
import com.TenX.Automobile.dto.response.VehicleResponse;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.repository.VehicleRepository;
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
                .brand_name(request.getBrandName().trim())
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
        vehicle.setBrand_name(request.getBrandName().trim());
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
                .brandName(vehicle.getBrand_name())
                .model(vehicle.getModel())
                .capacity(vehicle.getCapacity())
                .createdBy(vehicle.getCreatedBy())
                .customerId(vehicle.getCustomer().getId())
                .build();
    }
}
