package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.EmployeeRegistrationRequest;
import com.TenX.Automobile.dto.request.UpdateEmployeeProfileRequest;
import com.TenX.Automobile.dto.response.EmployeeProfileResponse;
import com.TenX.Automobile.dto.response.EmployeeResponse;
import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.EmployeeRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    /**
     * Get employee profile by ID
     */
    public EmployeeProfileResponse getEmployeeProfile(UUID employeeId) {
        log.info("Fetching employee profile for ID: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return convertToProfileResponse(employee);
    }

    /**
     * Update employee profile
     */
    public EmployeeProfileResponse updateEmployeeProfile(UUID employeeId, UpdateEmployeeProfileRequest request) {
        log.info("Updating employee profile for ID: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            // Check if email is already taken by another employee
            employeeRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(employeeId)) {
                    throw new RuntimeException("Email already exists: " + request.getEmail());
                }
            });
            employee.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            employee.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            employee.setProfileImageUrl(request.getProfileImageUrl());
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee profile updated successfully for ID: {}", employeeId);

        return convertToProfileResponse(updatedEmployee);
    }

    /**
     * Convert Employee entity to EmployeeProfileResponse DTO
     */
    private EmployeeProfileResponse convertToProfileResponse(Employee employee) {
        String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") +
                         (employee.getLastName() != null ? " " + employee.getLastName() : "").trim();

        return EmployeeProfileResponse.builder()
                .id(employee.getId())
                .name(fullName.isEmpty() ? null : fullName)
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .role(employee.getRoles())
                .email(employee.getEmail())
                .phone(employee.getPhoneNumber())
                .joinDate(employee.getCreatedAt())
                .currentRating(null) // TODO: Calculate from reviews if available
                .totalReviews(0) // TODO: Calculate from reviews if available
                .specialty(employee.getSpecialty())
                .employeeId(employee.getEmployeeId())
                .profileImageUrl(employee.getProfileImageUrl())
                .build();
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

    /**
     * Get all employees with optional filters
     * @param specialty Optional filter by specialty
     * @param date Optional filter by joined date
     * @return List of employees matching the filters
     */
    public List<EmployeeResponse> getEmployees(String specialty, LocalDateTime date) {
        log.info("Fetching employees with specialty: {}, date: {}", specialty, date);
        List<Employee> employees = employeeRepository.findEmployeesByFilters(specialty, date);
        return employees.stream()
                .map(this::convertToEmployeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all employees with date range filters
     * @param specialty Optional filter by specialty
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of employees matching the filters
     */
    public List<EmployeeResponse> getEmployeesByDateRange(String specialty, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching employees with specialty: {}, startDate: {}, endDate: {}", specialty, startDate, endDate);
        List<Employee> employees = employeeRepository.findEmployeesByDateRange(specialty, startDate, endDate);
        return employees.stream()
                .map(this::convertToEmployeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Employee entity to EmployeeResponse DTO
     */
    private EmployeeResponse convertToEmployeeResponse(Employee employee) {
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
