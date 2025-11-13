package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.CreateSubTaskRequest;
import com.TenX.Automobile.model.dto.request.UpdateEmployeeStatusRequest;
import com.TenX.Automobile.model.dto.request.UpdateScheduleRequest;
import com.TenX.Automobile.model.dto.response.*;
import com.TenX.Automobile.service.ManagerDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manager Controller - MANAGER role only
 * Provides endpoints for manager dashboard operations including:
 * - Dashboard overview and metrics
 * - Employee management
 * - Task and project management
 * - Schedule management
 * - Reports and analytics
 */
@Slf4j
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') AND isAuthenticated()")
public class ManagerController {

    private final ManagerDashboardService managerDashboardService;

    // 1. Dashboard Overview API
    @GetMapping("/dashboard/overview")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        log.info("Getting dashboard overview");
        return ResponseEntity.ok(managerDashboardService.getDashboardOverview());
    }

    // 2. Employee Management API
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeListResponse>> getAllEmployees() {
        log.info("Getting all employees");
        return ResponseEntity.ok(managerDashboardService.getAllEmployees());
    }

    @PutMapping("/employees/{id}/status")
    public ResponseEntity<Map<String, Object>> updateEmployeeStatus(
            @PathVariable("id") UUID employeeId,
            @Valid @RequestBody UpdateEmployeeStatusRequest request) {
        log.info("Updating employee {} status to {}", employeeId, request.getStatus());
        managerDashboardService.updateEmployeeStatus(employeeId, request);
        Map<String, Object> response = Map.of("message", "Employee status updated successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/{id}/history")
    public ResponseEntity<List<EmployeeHistoryResponse>> getEmployeeHistory(
            @PathVariable("id") UUID employeeId) {
        log.info("Getting history for employee {}", employeeId);
        return ResponseEntity.ok(managerDashboardService.getEmployeeHistory(employeeId));
    }

    // 3. Task & Project Management API
    @PostMapping("/subtasks")
    public ResponseEntity<Map<String, Object>> createSubTask(
            @Valid @RequestBody CreateSubTaskRequest request) {
        log.info("Creating new subtask '{}' for project {}", request.getTitle(), request.getProjectId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(managerDashboardService.createSubTask(request));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectBoardResponse>> getAllProjects() {
        log.info("Getting all projects");
        return ResponseEntity.ok(managerDashboardService.getAllProjects());
    }

    @GetMapping("/employees/available")
    public ResponseEntity<List<AvailableEmployeeResponse>> getAvailableEmployees() {
        log.info("Getting available employees");
        return ResponseEntity.ok(managerDashboardService.getAvailableEmployees());
    }

    // 5. Workload Scheduler API
    @GetMapping("/schedule")
    public ResponseEntity<ScheduleResponse> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting schedule from {} to {}", startDate, endDate);
        return ResponseEntity.ok(managerDashboardService.getSchedule(startDate, endDate));
    }

    @PutMapping("/schedule/task/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable("id") Long taskId,
            @Valid @RequestBody UpdateScheduleRequest request) {
        log.info("Updating schedule for task {}", taskId);
        return ResponseEntity.ok(managerDashboardService.updateSchedule(taskId, request));
    }

    @PostMapping("/schedule/auto-balance")
    public ResponseEntity<Map<String, Object>> autoBalanceWorkload() {
        log.info("Triggering workload auto-balancing");
        return ResponseEntity.ok(managerDashboardService.autoBalanceWorkload());
    }

    // 6. Reports & Analytics API
    @GetMapping("/reports/employee-efficiency")
    public ResponseEntity<ReportsResponse> getEmployeeEfficiencyReport() {
        log.info("Getting employee efficiency report");
        return ResponseEntity.ok(managerDashboardService.getEmployeeEfficiencyReport());
    }

    @GetMapping("/reports/most-requested-employees")
    public ResponseEntity<ReportsResponse> getMostRequestedEmployeesReport() {
        log.info("Getting most requested employees report");
        return ResponseEntity.ok(managerDashboardService.getMostRequestedEmployeesReport());
    }

    @GetMapping("/reports/parts-delay-analytics")
    public ResponseEntity<ReportsResponse> getPartsDelayAnalyticsReport() {
        log.info("Getting parts delay analytics report");
        return ResponseEntity.ok(managerDashboardService.getPartsDelayAnalyticsReport());
    }

    @GetMapping("/reports/completed-projects-by-type")
    public ResponseEntity<ReportsResponse> getCompletedProjectsByTypeReport() {
        log.info("Getting completed projects by type report");
        return ResponseEntity.ok(managerDashboardService.getCompletedProjectsByTypeReport());
    }

    @GetMapping("reports/completion-rate-trend")
    public ResponseEntity<CompletionRatePercentageResponse> getCompletionRateTrendReport() {
        log.info("Getting completion rate trend report");
        return ResponseEntity.ok(managerDashboardService.getCompletionRateTrendReport());
    }
}
