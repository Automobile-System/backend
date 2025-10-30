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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/auth")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/signup")
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

}
