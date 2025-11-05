package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.profile.request.CustomerProfileUpdateRequest;
import com.TenX.Automobile.dto.profile.response.CustomerProfileResponse;
import com.TenX.Automobile.dto.request.CustomerRegistrationRequest;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public Customer registerCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        log.info("Customer Registration Request: {}", customerRegistrationRequest.getEmail());

        if(customerRepository.findByEmail(customerRegistrationRequest.getEmail()).isPresent()){
            throw new RuntimeException("Customer already exists in " +  customerRegistrationRequest.getEmail());
        }

        Customer customer = Customer.builder()
                .customerId(generateCustomerId())
                .firstName(customerRegistrationRequest.getFirstName())
                .lastName(customerRegistrationRequest.getLastName())
                .nationalId(customerRegistrationRequest.getNationalId())
                .phoneNumber(customerRegistrationRequest.getPhoneNumber())
                .email(customerRegistrationRequest.getEmail())
                .password(passwordEncoder.encode(customerRegistrationRequest.getPassword()))

                .build();

        customer.addRole(Role.CUSTOMER);
        System.out.println(customer);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getCustomerId());
        return savedCustomer;

    }

    public String generateCustomerId(){
        log.info("Generating Customer ID...");

        List<String> existingIds = customerRepository.findAllCustomerIds();

        if(existingIds.isEmpty()){
            return  "CUST0001";
        }

        List<Integer> numbers = existingIds.stream()
                .filter(id->id.startsWith("CUST"))
                .map(id->Integer.parseInt(id.substring(4)))
                .sorted()
                .toList();

        int nextNumber = findNextNumber(numbers);

        String newId =String.format("CUST%04d", nextNumber);
        log.info("Generated Customer ID: {}", newId);
        return newId;
    }


    public int findNextNumber(List<Integer> numbers){
        for (int i=0;i<numbers.size();i++){
            if(numbers.get(i) != i+1){
                return i+1;
            }

        }
        return numbers.size() +1;
    }

    /**
     * Get customer profile by email
     */
    public CustomerProfileResponse getCustomerProfile(String email) {
        log.info("Fetching profile for customer: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .customerId(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .nationalId(customer.getNationalId())
                .phoneNumber(customer.getPhoneNumber())
                .profileImageUrl(customer.getProfileImageUrl())
                .roles(customer.getRoles())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .lastLoginAt(customer.getLastLoginAt())
                .build();
    }

    /**
     * Update customer profile
     */
    public CustomerProfileResponse updateCustomerProfile(String email, CustomerProfileUpdateRequest request) {
        log.info("Updating profile for customer: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        // Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            
            // Check if email is actually changing
            if (!newEmail.equals(customer.getEmail())) {
                // Validate email format (additional backend validation)
                if (!isValidEmail(newEmail)) {
                    throw new RuntimeException("Invalid email format: " + newEmail);
                }
                
                // Check if new email already exists
                if (customerRepository.findByEmail(newEmail).isPresent()) {
                    throw new RuntimeException("Email already exists: " + newEmail);
                }
                
                log.info("Updating email from {} to {}", customer.getEmail(), newEmail);
                customer.setEmail(newEmail);
            }
        }
        
        // Update only non-null fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            if (request.getFirstName().trim().length() < 2 || request.getFirstName().trim().length() > 50) {
                throw new RuntimeException("First name must be between 2 and 50 characters");
            }
            customer.setFirstName(request.getFirstName().trim());
        }
        
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            if (request.getLastName().trim().length() < 2 || request.getLastName().trim().length() > 50) {
                throw new RuntimeException("Last name must be between 2 and 50 characters");
            }
            customer.setLastName(request.getLastName().trim());
        }
        
        if (request.getNationalId() != null && !request.getNationalId().trim().isEmpty()) {
            String nationalId = request.getNationalId().trim();
            if (!isValidNationalId(nationalId)) {
                throw new RuntimeException("Invalid National ID format. Must be 9 digits followed by V/X or 12 digits");
            }
            customer.setNationalId(nationalId);
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phoneNumber = request.getPhoneNumber().trim();
            if (!isValidPhoneNumber(phoneNumber)) {
                throw new RuntimeException("Invalid phone number format. Must be 10-15 digits, optionally starting with +");
            }
            customer.setPhoneNumber(phoneNumber);
        }
        
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().trim().isEmpty()) {
            customer.setProfileImageUrl(request.getProfileImageUrl().trim());
        }
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Profile updated successfully for customer: {}", email);
        
        return CustomerProfileResponse.builder()
                .id(updatedCustomer.getId())
                .customerId(updatedCustomer.getCustomerId())
                .email(updatedCustomer.getEmail())
                .firstName(updatedCustomer.getFirstName())
                .lastName(updatedCustomer.getLastName())
                .nationalId(updatedCustomer.getNationalId())
                .phoneNumber(updatedCustomer.getPhoneNumber())
                .profileImageUrl(updatedCustomer.getProfileImageUrl())
                .roles(updatedCustomer.getRoles())
                .createdAt(updatedCustomer.getCreatedAt())
                .updatedAt(updatedCustomer.getUpdatedAt())
                .lastLoginAt(updatedCustomer.getLastLoginAt())
                .build();
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // RFC 5322 compliant email regex (simplified)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validate National ID format
     */
    private boolean isValidNationalId(String nationalId) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return false;
        }
        // Old NIC: 9 digits + V/v/X/x OR New NIC: 12 digits
        return nationalId.matches("^[0-9]{9}[vVxX]?$") || nationalId.matches("^[0-9]{12}$");
    }
    
    /**
     * Validate phone number format
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // Phone: optional + followed by 10-15 digits
        return phoneNumber.matches("^\\+?[0-9]{10,15}$");
    }
}
