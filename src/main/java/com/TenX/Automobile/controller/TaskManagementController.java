package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.PauseTaskRequest;
import com.TenX.Automobile.model.dto.response.AssignedTaskResponse;
import com.TenX.Automobile.model.dto.response.CalendarEventResponse;
import com.TenX.Automobile.model.dto.response.DashboardSummaryResponse;
import com.TenX.Automobile.model.entity.Task;
import com.TenX.Automobile.model.entity.UserEntity;
import com.TenX.Automobile.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Task Management Controller - Employee Dashboard endpoints
 * Supports Dashboard, Assigned Tasks, and Calendar pages
 * Note: This controller is separate from TaskController (/api/v1/tasks) to maintain existing flow
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskManagementController {

    private final TaskService taskService;

    /**
     * GET /api/tasks/employee/{employeeId}
     * Load All Assigned Tasks
     * Retrieves all tasks currently assigned to the employee (including completed)
     * Supports optional status filter query parameter
     * Tasks are sorted by deadline ASC
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<AssignedTaskResponse>> getAssignedTasks(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String status) {
        log.info("Fetching assigned tasks for employee ID: {} with status filter: {}", employeeId, status);
        List<AssignedTaskResponse> tasks = taskService.getAssignedTasksByEmployeeId(employeeId, status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /api/tasks/dashboard-summary
     * Load Dashboard Summary Cards
     * Retrieves key performance indicators (KPIs) for the main dashboard view
     * Uses authenticated employee from security context
     */
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        UUID employeeId = getEmployeeIdFromAuth(authentication);
        log.info("Fetching dashboard summary for employee ID: {}", employeeId);
        DashboardSummaryResponse summary = taskService.getDashboardSummary(employeeId);
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/tasks/calendar/{employeeId}?startDate={date}&endDate={date}
     * Load Calendar Events
     * Retrieves scheduled tasks/appointments for a specific time range
     */
    @GetMapping("/calendar/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CalendarEventResponse>> getCalendarEvents(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching calendar events for employee ID: {} from {} to {}", employeeId, startDate, endDate);
        List<CalendarEventResponse> events = taskService.getCalendarEvents(employeeId, startDate, endDate);
        return ResponseEntity.ok(events);
    }

    /**
     * POST /api/tasks/{taskId}/start
     * Start Task
     * Moves a task from 'Not Started' to 'In Progress' and initiates a timer
     */
    @PostMapping("/{taskId}/start")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> startTask(@PathVariable Long taskId) {
        log.info("Starting task with ID: {}", taskId);
        try {
            Task task = taskService.startTask(taskId);
            return ResponseEntity.ok(Map.of(
                "message", "Task started successfully",
                "taskId", taskId,
                "status", task.getStatus()
            ));
        } catch (IllegalStateException e) {
            log.warn("Cannot start task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error starting task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/{taskId}/pause
     * Pause Task
     * Moves a task to 'Paused' or 'Waiting for Parts'
     * Requires reason and optional notes in request body
     */
    @PostMapping("/{taskId}/pause")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> pauseTask(
            @PathVariable Long taskId,
            @Valid @RequestBody PauseTaskRequest request) {
        log.info("Pausing task with ID: {}, reason: {}", taskId, request.getReason());
        try {
            Task task = taskService.pauseTask(taskId, request.getReason(), request.getNotes());
            return ResponseEntity.ok(Map.of(
                "message", "Task paused successfully",
                "taskId", taskId,
                "status", task.getStatus()
            ));
        } catch (IllegalStateException e) {
            log.warn("Cannot pause task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error pausing task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/{taskId}/resume
     * Resume Task
     * Moves a task from 'Paused' back to 'In Progress'
     */
    @PostMapping("/{taskId}/resume")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> resumeTask(@PathVariable Long taskId) {
        log.info("Resuming task with ID: {}", taskId);
        try {
            Task task = taskService.resumeTask(taskId);
            return ResponseEntity.ok(Map.of(
                "message", "Task resumed successfully",
                "taskId", taskId,
                "status", task.getStatus()
            ));
        } catch (IllegalStateException e) {
            log.warn("Cannot resume task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error resuming task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tasks/{taskId}/complete
     * Complete Task
     * Marks a task as finished and stops its timer
     */
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> completeTask(@PathVariable Long taskId) {
        log.info("Completing task with ID: {}", taskId);
        try {
            Task task = taskService.completeTask(taskId);
            return ResponseEntity.ok(Map.of(
                "message", "Task completed successfully",
                "taskId", taskId,
                "status", task.getStatus(),
                "completedAt", task.getCompletedAt() != null ? task.getCompletedAt().toString() : ""
            ));
        } catch (IllegalStateException e) {
            log.warn("Cannot complete task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error completing task {}: {}", taskId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Helper method to extract employee ID from authentication
     */
    private UUID getEmployeeIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Not authenticated. Provide a valid Authorization: Bearer <token> header.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserEntity) {
            return ((UserEntity) principal).getId();
        }

        throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass().getName());
    }
}
