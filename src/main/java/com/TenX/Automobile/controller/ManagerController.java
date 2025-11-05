package com.TenX.Automobile.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager Controller - ADMIN and MANAGER roles
 * Demonstrates RBAC for management operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ManagerController {

    /**
     * View staff performance (Manager and Admin)
     */
    @GetMapping("/staff/performance")
    public ResponseEntity<Map<String, Object>> getStaffPerformance() {
        log.info("Manager: Get staff performance");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager: Viewing staff performance metrics");
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Approve service requests (Manager and Admin)
     */
    @PostMapping("/services/{serviceId}/approve")
    public ResponseEntity<Map<String, Object>> approveService(@PathVariable String serviceId) {
        log.info("Manager: Approve service request: {}", serviceId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager: Service approved");
        response.put("serviceId", serviceId);
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * View reports (Manager and Admin)
     */
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        log.info("Manager: Get reports");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager: Viewing business reports");
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER"});
        
        return ResponseEntity.ok(response);
    }

    /**
     * Manage inventory (Manager and Admin)
     */
    @PutMapping("/inventory/{itemId}")
    public ResponseEntity<Map<String, Object>> updateInventory(
            @PathVariable String itemId,
            @RequestBody Map<String, Object> inventoryUpdate) {
        
        log.info("Manager: Update inventory item: {}", itemId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manager: Inventory updated");
        response.put("itemId", itemId);
        response.put("allowedRoles", new String[]{"ADMIN", "MANAGER"});
        
        return ResponseEntity.ok(response);
    }
}
