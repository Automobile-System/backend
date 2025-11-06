package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.ManualTimeLogRequest;
import com.TenX.Automobile.dto.response.TimeLogResponse;
import com.TenX.Automobile.dto.response.WeeklyTotalHoursResponse;
import com.TenX.Automobile.service.TimeLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Time Logs Controller - Employee time logging endpoints
 * Supports viewing time logs, manual time entry, and weekly totals
 * Note: This controller is at /api/timelogs (plural) to avoid conflicts with existing /api/timelog controller
 */
@RestController
@RequestMapping("/api/timelogs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TimeLogsController {

    private final TimeLogService timeLogService;

    /**
     * GET /api/timelogs/employee/{employeeId}?dateRange={range}
     * View Time Logs
     * Retrieves detailed history of an employee's work logs
     * Supports optional dateRange query parameter (e.g., "week", "month", or "2024-01-01,2024-01-31")
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TimeLogResponse>> getEmployeeTimeLogs(
            @PathVariable UUID employeeId,
            @RequestParam(required = false) String dateRange) {
        log.info("Fetching time logs for employee ID: {} with date range: {}", employeeId, dateRange);
        List<TimeLogResponse> timeLogs = timeLogService.getTimeLogsByEmployeeId(employeeId, dateRange);
        return ResponseEntity.ok(timeLogs);
    }

    /**
     * POST /api/timelogs
     * Add New Manual Log
     * Allows an employee to manually submit a time log entry
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<TimeLogResponse> createManualTimeLog(
            @Valid @RequestBody ManualTimeLogRequest request) {
        log.info("Creating manual time log for task ID: {}", request.getTaskId());
        try {
            TimeLogResponse timeLog = timeLogService.createManualTimeLog(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(timeLog);
        } catch (Exception e) {
            log.error("Error creating manual time log: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET /api/timelogs/weekly-total
     * Load Weekly Total Hours
     * Retrieves total hours logged for the current week for authenticated employee
     */
    @GetMapping("/weekly-total")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<WeeklyTotalHoursResponse> getWeeklyTotalHours(Authentication authentication) {
        UUID employeeId = getEmployeeIdFromAuth(authentication);
        log.info("Fetching weekly total hours for employee ID: {}", employeeId);
        WeeklyTotalHoursResponse response = timeLogService.getWeeklyTotalHours(employeeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to extract employee ID from authentication
     */
    private UUID getEmployeeIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Not authenticated. Provide a valid Authorization: Bearer <token> header.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof com.TenX.Automobile.entity.UserEntity) {
            return ((com.TenX.Automobile.entity.UserEntity) principal).getId();
        }

        throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass().getName());
    }
}

