package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.CustomerRegistrationRequest;
import com.TenX.Automobile.dto.response.CustomerRegistrationResponse;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        log.info("Customer: Get profile for user: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer: Viewing own profile");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }

    /**
     * View own service history (All authenticated users)
     */
    @GetMapping("/customer/services")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getServiceHistory(Authentication authentication) {
        log.info("Customer: Get service history for user: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer: Viewing service history");
        response.put("user", authentication.getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Book appointment (All authenticated users)
     */
    @PostMapping("/v1/customer/appointments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestBody Map<String, Object> appointmentData,
            Authentication authentication) {
        
        log.info("Customer: Book appointment for user: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer: Appointment booked successfully");
        response.put("user", authentication.getName());
        response.put("appointmentData", appointmentData);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update own profile (All authenticated users)
     */
    @PutMapping("/customer/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {
        
        log.info("Customer: Update profile for user: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer: Profile updated successfully");
        response.put("user", authentication.getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Submit feedback (All authenticated users)
     */
    @PostMapping("/v1/customer/feedback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @RequestBody Map<String, String> feedback,
            Authentication authentication) {
        
        log.info("Customer: Submit feedback for user: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Customer: Feedback submitted successfully");
        response.put("user", authentication.getName());
        
        return ResponseEntity.ok(response);
    }
}
