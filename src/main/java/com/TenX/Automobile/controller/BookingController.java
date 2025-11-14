package com.TenX.Automobile.controller;

import com.TenX.Automobile.model.dto.request.ServiceBookingRequest;
import com.TenX.Automobile.model.dto.response.AvailableSlotResponse;
import com.TenX.Automobile.model.dto.response.ServiceBookingResponse;
import com.TenX.Automobile.service.BookingService;
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
import java.util.UUID;

/**
 * Booking Controller - Handles booking availability and slot management
 */
@Slf4j
@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Get available time slots with capacity information
     * @param employeeId Optional employee ID filter
     * @param startDate Optional start date (default: today) - formats: yyyy-MM-dd, yyyy-M-d
     * @param endDate Optional end date (default: 30 days from start) - formats: yyyy-MM-dd, yyyy-M-d
     * @return List of available slots with booking counts
     */
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlots(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-M-d") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-M-d") LocalDate endDate) {
        
        log.info("Get available slots - employeeId: {}, startDate: {}, endDate: {}", employeeId, startDate, endDate);
        
        List<AvailableSlotResponse> slots = bookingService.getAvailableSlots(employeeId, startDate, endDate);
        
        return ResponseEntity.ok(slots);
    }

    /**
     * Check availability for a specific date
     * @param date The date to check (formats: yyyy-MM-dd, yyyy-M-d)
     * @return Availability information for the specified date
     */
    @GetMapping("/check-availability")
    public ResponseEntity<AvailableSlotResponse> checkAvailability(
            @RequestParam @DateTimeFormat(pattern = "yyyy-M-d") LocalDate date) {
        
        log.info("Check availability for date: {}", date);
        
        AvailableSlotResponse slot = bookingService.checkAvailability(date);
        
        return ResponseEntity.ok(slot);
    }

    /**
     * Book a service for authenticated customer
     * @param request Service booking request containing serviceId, arrivingDate, and vehicleId
     * @return ServiceBookingResponse with booking confirmation details
     */
    @PostMapping("/service")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ServiceBookingResponse> bookService(@Valid @RequestBody ServiceBookingRequest request) {
        
        log.info("Book service request - serviceId: {}, arrivingDate: {}", request.getServiceId(), request.getArrivingDate());
        
        ServiceBookingResponse response = bookingService.bookService(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
