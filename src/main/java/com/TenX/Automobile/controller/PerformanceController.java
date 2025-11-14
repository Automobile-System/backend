package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.response.ChartsDataResponse;
import com.TenX.Automobile.model.dto.response.DemandMeterResponse;
import com.TenX.Automobile.model.dto.response.MonthlyEarningsResponse;
import com.TenX.Automobile.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Performance Controller - Employee Performance & Earnings endpoints
 * Supports Performance & Earnings page with all charts and metrics
 */
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * GET /api/performance/earnings/{employeeId}
     * Load Monthly Earnings Breakdown
     * Retrieves detailed earnings breakdown for the current month
     */
    @GetMapping("/earnings/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<MonthlyEarningsResponse> getMonthlyEarnings(@PathVariable UUID employeeId) {
        log.info("Fetching monthly earnings for employee ID: {}", employeeId);
        MonthlyEarningsResponse earnings = performanceService.getMonthlyEarnings(employeeId);
        return ResponseEntity.ok(earnings);
    }

    /**
     * GET /api/performance/demand-meter/{employeeId}
     * Load Demand Meter Status
     * Retrieves the employee's popularity/demand rating
     */
    @GetMapping("/demand-meter/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<DemandMeterResponse> getDemandMeterStatus(@PathVariable UUID employeeId) {
        log.info("Fetching demand meter status for employee ID: {}", employeeId);
        DemandMeterResponse demandMeter = performanceService.getDemandMeterStatus(employeeId);
        return ResponseEntity.ok(demandMeter);
    }

    /**
     * GET /api/performance/charts/{employeeId}
     * Load All Chart Data
     * Provides datasets structured for charting (daily hours, monthly tasks, rating trends, service distribution)
     */
    @GetMapping("/charts/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ChartsDataResponse> getChartsData(@PathVariable UUID employeeId) {
        log.info("Fetching chart data for employee ID: {}", employeeId);
        ChartsDataResponse chartsData = performanceService.getChartsData(employeeId);
        return ResponseEntity.ok(chartsData);
    }
}

