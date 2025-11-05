package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.EmployeeRegistrationRequest;
import com.TenX.Automobile.dto.response.EmployeeResponse;
import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public Employee addEmployee(EmployeeRegistrationRequest employeeRegistrationRequest){

        log.info("Employee Registration Request : {}", employeeRegistrationRequest.getEmail());

        if(employeeRepository.findByEmail(employeeRegistrationRequest.getEmail()).isPresent()){
            throw new RuntimeException("Employee already exists in " +  employeeRegistrationRequest.getEmail());
        }
        final String speciality = employeeRegistrationRequest.getSpeciality();

        Employee employee = Employee.builder()
                .employeeId(generateEmployeeId())
                .email(employeeRegistrationRequest.getEmail())
                .password(passwordEncoder.encode(employeeRegistrationRequest.getPassword()))
                .firstName(employeeRegistrationRequest.getFirstName())
                .lastName(employeeRegistrationRequest.getLastName())
                .nationalId(employeeRegistrationRequest.getNationalId())
                .phoneNumber(employeeRegistrationRequest.getPhoneNumber())
                .specialty(speciality)
                .build();

        employee.addRole(Role.STAFF);

        System.out.print("Saving Employee : "+employee);
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee registered successfully with ID: {}", savedEmployee.getEmployeeId());

        return  savedEmployee;
    }

    private String generateEmployeeId(){
        log.info("Generating Employee ID...");

        List<String> existingIds = employeeRepository.getAllEmployeeIds();
        if(existingIds.isEmpty()){
            return  "EMP0001";
        }

        List<Integer> numbers = existingIds.stream()
                .filter(id->id.startsWith("EMP"))
                .map(id->Integer.parseInt(id.substring(4)))
                .sorted()
                .toList();

        int nextId = findNextNumber(numbers);

        String newId =String.format("EMP%04d",nextId);
        log.info("Generated Employee ID: {}", newId);

        return newId;
    }


    public int findNextNumber(List<Integer> numbers){
        for(int i=0;i<numbers.size();i++){
            if(numbers.get(i) != i+1){
                return i+1;
            }
        }
        return numbers.size() +1;
    }

    public List<EmployeeResponse> getEmployees(String specialty, LocalDateTime date) {
        log.info("Fetching employees with filters - specialty: {}, date: {}", specialty, date);

        List<Employee> employees = employeeRepository.findEmployeesByFilters(specialty, date);

        log.info("Found {} employees matching the filters", employees.size());

        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .nationalId(employee.getNationalId())
                .phoneNumber(employee.getPhoneNumber())
                .profileImageUrl(employee.getProfileImageUrl())
                .specialty(employee.getSpecialty())
                .roles(employee.getRoles())
                .joinedDate(employee.getCreatedAt())
                .lastLoginAt(employee.getLastLoginAt())
                .enabled(employee.isEnabled())
                .build();
    }
}
