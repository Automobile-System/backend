package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.ServiceBookingRequest;
import com.TenX.Automobile.dto.response.AvailableSlotResponse;
import com.TenX.Automobile.dto.response.ServiceBookingResponse;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.ServiceRepository;
import com.TenX.Automobile.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final JobRepository jobRepository;
    private final ServiceRepository serviceRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    @Value("${app.booking.max-jobs-per-day:10}")
    private Integer maxJobsPerDay;

    /**
     * Get available time slots for a date range (default 30 days from today)
     * @param employeeId Optional employee ID filter
     * @param startDate Optional start date (default: today)
     * @param endDate Optional end date (default: 30 days from start)
     * @return List of available slots with capacity information
     */
    public List<AvailableSlotResponse> getAvailableSlots(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting available slots - employeeId: {}, startDate: {}, endDate: {}", employeeId, startDate, endDate);

        // Set default dates if not provided
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start.plusDays(30);

        // Convert to LocalDateTime for querying
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        // Get job counts grouped by date
        List<Object[]> jobCounts = jobRepository.countJobsByDateRange(startDateTime, endDateTime);

        // Create a list of available slots
        List<AvailableSlotResponse> availableSlots = new ArrayList<>();

        // Iterate through each day in the range
        LocalDate currentDate = start;
        while (!currentDate.isAfter(end)) {
            LocalDate finalCurrentDate = currentDate;
            
            // Find the booking count for this date
            Long bookedCount = jobCounts.stream()
                    .filter(obj -> {
                        LocalDate date = (LocalDate) obj[0];
                        return date.equals(finalCurrentDate);
                    })
                    .map(obj -> ((Number) obj[1]).longValue())
                    .findFirst()
                    .orElse(0L);

            int bookedInt = bookedCount.intValue();
            int available = maxJobsPerDay - bookedInt;

            AvailableSlotResponse slot = AvailableSlotResponse.builder()
                    .date(currentDate)
                    .totalCapacity(maxJobsPerDay)
                    .bookedCount(bookedInt)
                    .availableCount(available)
                    .isAvailable(available > 0)
                    .build();

            availableSlots.add(slot);
            currentDate = currentDate.plusDays(1);
        }

        log.info("Found {} available slots", availableSlots.size());
        return availableSlots;
    }

    /**
     * Check if a specific date has available slots
     * @param date The date to check
     * @return AvailableSlotResponse with capacity information
     */
    public AvailableSlotResponse checkAvailability(LocalDate date) {
        log.info("Checking availability for date: {}", date);

        LocalDateTime dateTime = date.atStartOfDay();
        Long bookedCount = jobRepository.countJobsByDate(dateTime);

        int bookedInt = bookedCount.intValue();
        int available = maxJobsPerDay - bookedInt;

        return AvailableSlotResponse.builder()
                .date(date)
                .totalCapacity(maxJobsPerDay)
                .bookedCount(bookedInt)
                .availableCount(available)
                .isAvailable(available > 0)
                .build();
    }

    /**
     * Book a service for a customer
     * @param request Service booking request with serviceId, arrivingDate, and vehicleIds
     * @return ServiceBookingResponse with booking details
     */
    @Transactional
    public ServiceBookingResponse bookService(ServiceBookingRequest request) {
        log.info("Booking service - serviceId: {}, arrivingDate: {}, vehicles: {}", 
                request.getServiceId(), request.getArrivingDate(), request.getVehicleIds());

        // Get authenticated customer email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Authenticated user: {}", email);

        // Find customer
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Check availability for the date
        LocalDate bookingDate = request.getArrivingDate().toLocalDate();
        AvailableSlotResponse availability = checkAvailability(bookingDate);
        
        if (!availability.getIsAvailable()) {
            throw new RuntimeException("No available slots for date: " + bookingDate + 
                    ". Available slots: " + availability.getAvailableCount());
        }

        // Find the service
        com.TenX.Automobile.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found with ID: " + request.getServiceId()));

        // Find and validate vehicles belong to customer
        List<Vehicle> vehicles = new ArrayList<>();
        for (UUID vehicleId : request.getVehicleIds()) {
            Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found or doesn't belong to customer: " + vehicleId));
            vehicles.add(vehicle);
        }

        // Update service with booking details
        service.setArrivingDate(request.getArrivingDate());
        service.setStatus("PENDING");
        
        // Add vehicles to the service
        for (Vehicle vehicle : vehicles) {
            service.addVehicle(vehicle);
        }

        // Save the service
        com.TenX.Automobile.entity.Service savedService = serviceRepository.save(service);

        log.info("Service booked successfully - jobId: {}, serviceId: {}", savedService.getJobId(), savedService.getJobId());

        // Build response
        return ServiceBookingResponse.builder()
                .jobId(savedService.getJobId())
                .serviceId(savedService.getJobId())
                .title(savedService.getTitle())
                .description(savedService.getDescription())
                .status(savedService.getStatus())
                .arrivingDate(savedService.getArrivingDate())
                .cost(savedService.getCost())
                .estimatedHours(savedService.getEstimatedHours())
                .category(savedService.getCategory())
                .vehicleRegistrations(vehicles.stream()
                        .map(Vehicle::getRegistration_No)
                        .collect(Collectors.toList()))
                .message("Service booked successfully!")
                .bookedAt(LocalDateTime.now())
                .build();
    }
}
