package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.profile.request.CustomerProfileUpdateRequest;
import com.TenX.Automobile.model.dto.profile.response.CustomerProfileResponse;
import com.TenX.Automobile.model.dto.request.CustomerRegistrationRequest;
import com.TenX.Automobile.model.dto.request.VehicleRequest;
import com.TenX.Automobile.model.dto.response.CustomerDashboardResponse;
import com.TenX.Automobile.model.dto.response.CustomerRegistrationResponse;
import com.TenX.Automobile.model.dto.response.EmployeeDetailsForCustomer;
import com.TenX.Automobile.model.dto.response.ServiceDetailResponse;
import com.TenX.Automobile.model.dto.response.ServiceFrequencyResponse;
import com.TenX.Automobile.model.dto.response.ServiceListResponse;
import com.TenX.Automobile.model.dto.response.VehicleResponse;
import com.TenX.Automobile.model.entity.Customer;
import com.TenX.Automobile.service.CustomerService;
import com.TenX.Automobile.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Customer Controller - Registration and customer-specific operations
 * Demonstrates RBAC for customer operations
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;

    /**
     * Public endpoint for customer registration
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest customerRegistrationRequest) {

        try{
            log.info("Customer Registration Request: {}", customerRegistrationRequest.getEmail());

            Customer customer = customerService.registerCustomer(customerRegistrationRequest);

            CustomerRegistrationResponse customerRegistrationResponse = CustomerRegistrationResponse.builder()
                    .id(customer.getId())
                    .customerId(customer.getCustomerId())
                    .email(customer.getEmail())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .nic(customer.getNationalId())
                    .phoneNumber(customer.getPhoneNumber())
                    .roles(customer.getRoles())
                    .createdAt(customer.getCreatedAt())
                    .build();
            log.info("Customer Registration Successful: {}", customerRegistrationRequest.getEmail());
            return new ResponseEntity<>(customerRegistrationResponse,HttpStatus.CREATED);
        }
        catch (RuntimeException e){
            log.warn("Customer Registration Failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            log.error("Unexpected error during customer registration: {}", customerRegistrationRequest.getEmail(), e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * View own profile (All authenticated users)
     */
    @GetMapping("/customer/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            log.info("Customer: Get profile for user: {}, authorities: {}", 
                    authentication.getName(), authentication.getAuthorities());
            
            CustomerProfileResponse profile = customerService.getCustomerProfile(authentication.getName());
            
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            log.warn("Failed to fetch customer profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching customer profile for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Update own profile (All authenticated users)
     */
    @PutMapping("/customer/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody CustomerProfileUpdateRequest profileUpdateRequest,
            Authentication authentication) {
        
        try {
            log.info("Customer: Update profile for user: {}", authentication.getName());
            
            CustomerProfileResponse updatedProfile = customerService.updateCustomerProfile(
                    authentication.getName(), 
                    profileUpdateRequest
            );
            
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            log.warn("Failed to update customer profile: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating customer profile for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }


    /**
     * Get all vehicles for the authenticated customer
     */
    @GetMapping("/customer/vehicles")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getVehicles(Authentication authentication) {
        try {
            log.info("Customer: Get vehicles for user: {}", authentication.getName());
            
            List<VehicleResponse> vehicles = vehicleService.getCustomerVehicles(authentication.getName());
            
            return ResponseEntity.ok(vehicles);
        } catch (RuntimeException e) {
            log.warn("Failed to fetch customer vehicles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching customer vehicles for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Add a new vehicle for the authenticated customer
     */
    @PostMapping("/customer/vehicle")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> addVehicle(
            @Valid @RequestBody VehicleRequest vehicleRequest,
            Authentication authentication) {
        try {
            log.info("Customer: Add vehicle for user: {}", authentication.getName());
            
            VehicleResponse vehicle = vehicleService.addVehicle(authentication.getName(), vehicleRequest);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
        } catch (RuntimeException e) {
            log.warn("Failed to add vehicle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error adding vehicle for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Add a new vehicle for the authenticated customer (alternative endpoint)
     */
    @PostMapping("/customer/vehicles")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> addVehicleAlt(
            @Valid @RequestBody VehicleRequest vehicleRequest,
            Authentication authentication) {
        return addVehicle(vehicleRequest, authentication);
    }

    /**
     * Update a vehicle for the authenticated customer
     */
    @PutMapping("/customer/vehicles/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateVehicle(
            @PathVariable("id") UUID vehicleId,
            @Valid @RequestBody VehicleRequest vehicleRequest,
            Authentication authentication) {
        try {
            log.info("Customer: Update vehicle {} for user: {}", vehicleId, authentication.getName());
            
            VehicleResponse vehicle = vehicleService.updateVehicle(
                    authentication.getName(), 
                    vehicleId, 
                    vehicleRequest
            );
            
            return ResponseEntity.ok(vehicle);
        } catch (RuntimeException e) {
            log.warn("Failed to update vehicle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating vehicle {} for user: {}", vehicleId, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Delete a vehicle for the authenticated customer
     */
    @DeleteMapping("/customer/vehicles/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteVehicle(
            @PathVariable("id") UUID vehicleId,
            Authentication authentication) {
        try {
            log.info("Customer: Delete vehicle {} for user: {}", vehicleId, authentication.getName());
            
            vehicleService.deleteVehicle(authentication.getName(), vehicleId);
            
            return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
        } catch (RuntimeException e) {
            log.warn("Failed to delete vehicle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting vehicle {} for user: {}", vehicleId, authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }


    /**
     * Get dashboard overview for the authenticated customer
     */
    @GetMapping("/customer/dashboard/overview")
    @PreAuthorize("hasRole('CUSTOMER') AND isAuthenticated()")
    public ResponseEntity<?> getDashboardOverview(Authentication authentication) {
        try {
            log.info("Customer: Get dashboard overview for user: {}", authentication.getName());
            
            CustomerDashboardResponse overview = customerService.getDashboardOverview(authentication.getName());
            
            return ResponseEntity.ok(overview);
        } catch (RuntimeException e) {
            log.warn("Failed to fetch dashboard overview: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching dashboard overview for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get service frequency chart data for the authenticated customer
     */
    @GetMapping("/customer/dashboard/service-frequency")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getServiceFrequency(
            @RequestParam(value = "period", defaultValue = "1year") String period,
            Authentication authentication) {
        try {
            log.info("Customer: Get service frequency for user: {} with period: {}", authentication.getName(), period);

            List<ServiceFrequencyResponse> serviceFrequency = customerService.getServiceFrequency(authentication.getName(), period);

            return ResponseEntity.ok(serviceFrequency);
        } catch (RuntimeException e) {
            log.warn("Failed to fetch service frequency: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching service frequency for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get all services for authenticated customer
     * @param status Optional status filter (active, completed, upcoming)
     */
    @Operation(summary = "Get all services for the authenticated customer with optional status filter",
               description = "statuses: active (IN_PROGRESS, WAITING_PARTS), completed (COMPLETED), upcoming (SCHEDULED)")
    @GetMapping("/customer/services")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getCustomerServices(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        
        try {
            log.info("Get customer services - status filter: {}", status);
            
            List<ServiceListResponse> services = customerService.getCustomerServices(authentication.getName(), status);
            
            return ResponseEntity.ok(services);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status filter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.warn("Failed to fetch services: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching services for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get detailed information about a specific service
     * @param serviceId Service ID
     */
    @GetMapping("/customer/services/{serviceId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getServiceDetails(
            Authentication authentication,
            @PathVariable Long serviceId) {
        
        try {
            log.info("Get service details - serviceId: {}", serviceId);
            
            ServiceDetailResponse service = customerService.getServiceDetails(authentication.getName(), serviceId);
            
            return ResponseEntity.ok(service);
            
        } catch (RuntimeException e) {
            log.warn("Failed to fetch service details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching service details for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }


    @GetMapping("/customer/all-employees")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getAllEmployees(Authentication authentication){
        try{
            log.info("Customer fetching all active employees:{}",authentication.getName());
            List<EmployeeDetailsForCustomer> employees=customerService.getAllActiveEmployees();
            return ResponseEntity.ok(employees);
        }catch (RuntimeException e) {
            log.warn("Failed to fetch all active employees: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching service details for user: {}", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

}
