package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.entity.TimeLog;
import com.TenX.Automobile.service.TimeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/timelog")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimeLogController {

    private final TimeLogService timeLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TimeLog>> getAllTimeLogs() {
        log.info("Fetching all time logs");
        return ResponseEntity.ok(timeLogService.getAllTimeLogs());
    }

    @GetMapping("/{logId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeLog> getTimeLogById(@PathVariable Long logId) {
        log.info("Fetching time log with ID: {}", logId);
        return ResponseEntity.ok(timeLogService.getTimeLogById(logId));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeLog> getTimeLogByJobId(@PathVariable Long jobId) {
        log.info("Fetching time log for job ID: {}", jobId);
        return ResponseEntity.ok(timeLogService.getTimeLogByJobId(jobId));
    }

    @GetMapping("/job/{jobId}/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TimeLog>> getTimeLogsByJobId(@PathVariable Long jobId) {
        log.info("Fetching all time logs for job ID: {}", jobId);
        return ResponseEntity.ok(timeLogService.getTimeLogsByJobId(jobId));
    }

    @PostMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TimeLog> createTimeLog(@PathVariable Long jobId, @RequestBody TimeLog timeLog) {
        log.info("Creating time log for job ID: {}", jobId);
        return ResponseEntity.ok(timeLogService.createTimeLog(timeLog, jobId));
    }

    @PostMapping("/job/{jobId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TimeLog> startTimeTracking(@PathVariable Long jobId) {
        log.info("Starting time tracking for job ID: {}", jobId);
        return ResponseEntity.ok(timeLogService.startTimeTracking(jobId));
    }

    @PostMapping("/job/{jobId}/stop")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TimeLog> stopTimeTracking(@PathVariable Long jobId) {
        log.info("Stopping time tracking for job ID: {}", jobId);
        return ResponseEntity.ok(timeLogService.stopTimeTracking(jobId));
    }

    @PutMapping("/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TimeLog> updateTimeLog(@PathVariable Long logId, @RequestBody TimeLog timeLogDetails) {
        log.info("Updating time log with ID: {}", logId);
        return ResponseEntity.ok(timeLogService.updateTimeLog(logId, timeLogDetails));
    }

    @DeleteMapping("/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTimeLog(@PathVariable Long logId) {
        log.info("Deleting time log with ID: {}", logId);
        timeLogService.deleteTimeLog(logId);
        return ResponseEntity.noContent().build();
    }
}
