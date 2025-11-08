package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.entity.ManageAssignJob;
import com.TenX.Automobile.service.ManageAssignJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/manage-assign-job")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ManageAssignJobController {

    private final ManageAssignJobService manageAssignJobService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ManageAssignJob>> getAllAssignments() {
        log.info("Fetching all job assignments");
        return ResponseEntity.ok(manageAssignJobService.getAllAssignments());
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ManageAssignJob> getAssignmentById(@PathVariable UUID assignmentId) {
        log.info("Fetching job assignment with ID: {}", assignmentId);
        return ResponseEntity.ok(manageAssignJobService.getAssignmentById(assignmentId));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ManageAssignJob> getAssignmentByJobId(@PathVariable Long jobId) {
        log.info("Fetching job assignment for job ID: {}", jobId);
        return ResponseEntity.ok(manageAssignJobService.getAssignmentByJobId(jobId));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ManageAssignJob>> getAssignmentsByManagerId(@PathVariable UUID managerId) {
        log.info("Fetching job assignments for manager ID: {}", managerId);
        return ResponseEntity.ok(manageAssignJobService.getAssignmentsByManagerId(managerId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ManageAssignJob>> getAssignmentsByEmployeeId(@PathVariable UUID employeeId) {
        log.info("Fetching job assignments for employee ID: {}", employeeId);
        return ResponseEntity.ok(manageAssignJobService.getAssignmentsByEmployeeId(employeeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ManageAssignJob> createJobAssignment(@RequestBody Map<String, Object> request) {
        Long jobId = Long.valueOf(request.get("jobId").toString());
        UUID managerId = UUID.fromString(request.get("managerId").toString());
        UUID employeeId = UUID.fromString(request.get("employeeId").toString());

        log.info("Creating job assignment: jobId={}, managerId={}, employeeId={}", jobId, managerId, employeeId);
        return ResponseEntity.ok(manageAssignJobService.createJobAssignment(jobId, managerId, employeeId));
    }

    @PutMapping("/job/{jobId}/reassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ManageAssignJob> reassignJob(
            @PathVariable Long jobId,
            @RequestBody Map<String, String> request) {
        UUID newEmployeeId = UUID.fromString(request.get("employeeId"));
        UUID managerId = UUID.fromString(request.get("managerId"));
        log.info("Reassigning job {} to employee {} by manager {}", jobId, newEmployeeId, managerId);
        return ResponseEntity.ok(manageAssignJobService.reassignJob(jobId, newEmployeeId, managerId));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID assignmentId) {
        log.info("Deleting job assignment with ID: {}", assignmentId);
        manageAssignJobService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteAssignmentByJobId(@PathVariable Long jobId) {
        log.info("Deleting job assignment for job ID: {}", jobId);
        manageAssignJobService.deleteAssignmentByJobId(jobId);
        return ResponseEntity.noContent().build();
    }
}
