package com.TenX.Automobile.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Staff Controller - ADMIN, MANAGER, and STAFF roles
 * Demonstrates RBAC for staff operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
public class StaffController {

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
