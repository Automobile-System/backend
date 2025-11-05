package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.response.AvailableSlotResponse;
import com.TenX.Automobile.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingService {

    private final JobRepository jobRepository;

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
}
