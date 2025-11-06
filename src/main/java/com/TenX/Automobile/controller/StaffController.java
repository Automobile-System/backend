package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.response.EmployeeResponse;
import com.TenX.Automobile.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staff Controller - ADMIN, MANAGER, and STAFF roles
 * Demonstrates RBAC for staff operations
 */
@Slf4j
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@CrossOrigin(origins = "*")
public class StaffController {

    private final EmployeeService employeeService;

    /**
     * Get all available staff with filters
     * @param specialty Optional filter by specialty
     * @param date Optional filter by joined date
     * @return List of employees matching the filters
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllStaff(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        
        log.info("Get all staff - specialty: {}, date: {}", specialty, date);
        
        List<EmployeeResponse> employees = employeeService.getEmployees(specialty, date);
        
        return ResponseEntity.ok(employees);
    }

    /**
     * Get all available staff with date range filters
     * @param specialty Optional filter by specialty
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of employees matching the filters
     */
    @GetMapping("/range")
    public ResponseEntity<List<EmployeeResponse>> getStaffByDateRange(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Get staff by date range - specialty: {}, startDate: {}, endDate: {}", specialty, startDate, endDate);
        
        List<EmployeeResponse> employees = employeeService.getEmployeesByDateRange(specialty, startDate, endDate);
        
        return ResponseEntity.ok(employees);
    }

    /**
     * View assigned tasks (Staff, Manager, Admin)
     */
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getAssignedTasks() {
        log.info("Staff: Get assigned tasks");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Staff: Viewing assigned tasks");
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER", "STAFF"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update service status (Staff, Manager, Admin)
     */
    @PutMapping("/services/{serviceId}/status")
    public ResponseEntity<Map<String, Object>> updateServiceStatus(
            @PathVariable String serviceId,
            @RequestBody Map<String, String> statusUpdate) {
        
        log.info("Staff: Update service status: {}", serviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Staff: Service status updated");
        response.put("serviceId", serviceId);
        response.put("newStatus", statusUpdate.get("status"));
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER", "STAFF"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * View customer information (Staff, Manager, Admin)
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerInfo(@PathVariable String customerId) {
        log.info("Staff: Get customer info: {}", customerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Staff: Viewing customer information");
        response.put("customerId", customerId);
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER", "STAFF"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create service record (Staff, Manager, Admin)
     */
    @PostMapping("/services")
    public ResponseEntity<Map<String, Object>> createService(@RequestBody Map<String, Object> serviceData) {
        log.info("Staff: Create new service record");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Staff: Service record created");
        response.put("serviceData", serviceData);
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER", "STAFF"});
        
        return ResponseEntity.ok(response);
    }
}
