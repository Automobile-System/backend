package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.EmployeeRegistrationRequest;
import com.TenX.Automobile.dto.response.EmployeeRegistrationResponse;
import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.service.EmployeeService;
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

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/signup")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') AND isAuthenticated()")
    public ResponseEntity<?> addEmployee(@Valid @RequestBody EmployeeRegistrationRequest employeeRegistrationRequest) {
        try{
            log.info("Employee Registration Request:{}", employeeRegistrationRequest.getEmail());


            Employee employee = employeeService.addEmployee(employeeRegistrationRequest);
            EmployeeRegistrationResponse employeeRegistrationResponse = EmployeeRegistrationResponse.builder()
                    .id(employee.getId())
                    .employeeId(employee.getEmployeeId())
                    .email(employee.getEmail())
                    .firstName(employee.getFirstName())
                    .lastName(employee.getLastName())
                    .nic(employee.getNationalId())
                    .phoneNumber(employee.getPhoneNumber())
                    .roles(employee.getRoles())
                    .specialty(employee.getSpecialty())
                    .createdAt(employee.getCreatedAt())
                    .build();

            log.info("Employee Registration Successful:{}", employeeRegistrationResponse.getEmail());
            return new ResponseEntity<>(employeeRegistrationResponse, HttpStatus.CREATED);

        }
        catch(RuntimeException e){
            log.warn("Employee Registration Failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            log.error("Unexpected error during customer registration: {}", employeeRegistrationRequest.getEmail(), e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/staff/profile")
//    @PreAuthorize("isAuthenticated()")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        log.info("Employee: Get profile for user: {}", authentication.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Employee: Viewing own profile");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }

}
