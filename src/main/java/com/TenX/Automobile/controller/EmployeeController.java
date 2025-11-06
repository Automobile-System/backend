package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.EmployeeRegistrationRequest;
import com.TenX.Automobile.dto.request.UpdateEmployeeProfileRequest;
import com.TenX.Automobile.dto.response.EmployeeNotificationResponse;
import com.TenX.Automobile.dto.response.EmployeeProfileResponse;
import com.TenX.Automobile.dto.response.EmployeeRegistrationResponse;
import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.entity.Notification;
import com.TenX.Automobile.entity.UserEntity;
import com.TenX.Automobile.service.EmployeeService;
import com.TenX.Automobile.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final NotificationService notificationService;


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

    /**
     * GET /api/employees/me
     * Retrieves the profile and static data for the logged-in employee
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeProfileResponse> getMyProfile(Authentication authentication) {
        UUID employeeId = getUserIdFromAuth(authentication);
        log.info("Fetching employee profile for ID: {}", employeeId);
        EmployeeProfileResponse profile = employeeService.getEmployeeProfile(employeeId);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/employees/me
     * Allows the employee to update specific profile details (e.g., contact information)
     */
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<EmployeeProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateEmployeeProfileRequest request) {
        UUID employeeId = getUserIdFromAuth(authentication);
        log.info("Updating employee profile for ID: {}", employeeId);
        EmployeeProfileResponse updatedProfile = employeeService.updateEmployeeProfile(employeeId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * GET /api/employees/notifications
     * Retrieves the list of unread notifications for the employee
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<EmployeeNotificationResponse>> getMyNotifications(Authentication authentication) {
        UUID employeeId = getUserIdFromAuth(authentication);
        log.info("Fetching unread notifications for employee ID: {}", employeeId);

        List<Notification> notifications = notificationService.getUnreadNotifications(employeeId);
        List<EmployeeNotificationResponse> response = notifications.stream()
                .map(this::convertToEmployeeNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to extract user ID from authentication
     */
    private UUID getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Not authenticated. Provide a valid Authorization: Bearer <token> header.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserEntity) {
            return ((UserEntity) principal).getId();
        }

        throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass().getName());
    }

    /**
     * Convert Notification entity to EmployeeNotificationResponse DTO
     */
    private EmployeeNotificationResponse convertToEmployeeNotificationResponse(Notification notification) {
        return EmployeeNotificationResponse.builder()
                .id(notification.getNoti_id())
                .message(notification.getMessage())
                .type(notification.getType())
                .timestamp(notification.getCreatedAt())
                .build();
    }
}
