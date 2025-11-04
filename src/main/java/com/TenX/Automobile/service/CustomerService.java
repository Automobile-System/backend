package com.TenX.Automobile.service;

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
}
