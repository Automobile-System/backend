package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.response.ChartsDataResponse;
import com.TenX.Automobile.dto.response.DemandMeterResponse;
import com.TenX.Automobile.dto.response.MonthlyEarningsResponse;
import com.TenX.Automobile.entity.*;
import com.TenX.Automobile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PerformanceService {

    private final EmployeeRepository employeeRepository;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;
    
    // Configuration constants - these could be moved to a config file or database
    private static final double BASE_SALARY = 50000.0; // Default base salary
    private static final double HOURLY_RATE = BASE_SALARY / (22 * 8); // Approx hourly rate
    private static final double OVERTIME_RATE_MULTIPLIER = 1.5; // 1.5x for overtime
    private static final double STANDARD_HOURS_PER_MONTH = 176.0; // 22 days * 8 hours
    private static final double PERFORMANCE_BONUS_RATE = 0.1; // 10% of base salary per completed task above threshold
    private static final double DEMAND_BONUS_RATE = 0.05; // 5% per job assigned above threshold

    /**
     * Get monthly earnings breakdown for an employee
     */
    @Transactional(readOnly = true)
    public MonthlyEarningsResponse getMonthlyEarnings(UUID employeeId) {
        log.info("Calculating monthly earnings for employee ID: {}", employeeId);
        
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        
        // Calculate base salary
        Double baseSalary = BASE_SALARY;
        
        // Calculate overtime hours
        Double totalHours = calculateTotalHoursForMonth(employeeId, startOfMonth, endOfMonth);
        Double overtimeHours = Math.max(0.0, totalHours - STANDARD_HOURS_PER_MONTH);
        
        // Calculate overtime pay
        Double overtimePay = overtimeHours * HOURLY_RATE * OVERTIME_RATE_MULTIPLIER;
        
        // Calculate performance bonus (based on completed tasks)
        Double performanceBonus = calculatePerformanceBonus(employeeId, startOfMonth, endOfMonth);
        
        // Calculate demand bonus (based on number of jobs assigned)
        Double demandBonus = calculateDemandBonus(employeeId, startOfMonth, endOfMonth);
        
        // Calculate total earnings
        Double totalEarnings = baseSalary + performanceBonus + demandBonus + overtimePay;
        
        return MonthlyEarningsResponse.builder()
                .baseSalary(baseSalary)
                .performanceBonus(performanceBonus)
                .demandBonus(demandBonus)
                .overtimePay(overtimePay)
                .totalEarnings(totalEarnings)
                .overtimeHours(overtimeHours)
                .build();
    }

    /**
     * Get demand meter status for an employee
     */
    @Transactional(readOnly = true)
    public DemandMeterResponse getDemandMeterStatus(UUID employeeId) {
        log.info("Calculating demand meter status for employee ID: {}", employeeId);
        
        // Count total jobs assigned to this employee
        manageAssignJobRepository.countTotalJobsAssigned(employeeId);
        
        // Count jobs assigned this month
        Long jobsThisMonth = manageAssignJobRepository.countJobsAssignedThisMonth(employeeId);
        
        // Calculate demand percentage based on jobs assigned
        // Assuming average employee gets 5 jobs/month, max is 20
        double averageJobsPerMonth = 5.0;
        double maxJobsPerMonth = 20.0;
        
        double demandPercentage = Math.min(100.0, (jobsThisMonth / averageJobsPerMonth) * 50.0);
        if (jobsThisMonth > averageJobsPerMonth) {
            demandPercentage = 50.0 + Math.min(50.0, ((jobsThisMonth - averageJobsPerMonth) / (maxJobsPerMonth - averageJobsPerMonth)) * 50.0);
        }
        
        // Determine demand status
        String demandStatus;
        if (demandPercentage >= 75) {
            demandStatus = "HIGH";
        } else if (demandPercentage >= 50) {
            demandStatus = "MEDIUM";
        } else {
            demandStatus = "LOW";
        }
        
        // Calculate bonus multiplier based on demand
        double bonusMultiplier = 1.0 + (demandPercentage / 100.0) * 0.5; // 1.0 to 1.5 multiplier
        
        return DemandMeterResponse.builder()
                .demandPercentage(Math.round(demandPercentage * 100.0) / 100.0)
                .demandStatus(demandStatus)
                .bonusMultiplier(Math.round(bonusMultiplier * 100.0) / 100.0)
                .build();
    }

    /**
     * Get all chart data for an employee
     */
    @Transactional(readOnly = true)
    public ChartsDataResponse getChartsData(UUID employeeId) {
        log.info("Fetching chart data for employee ID: {}", employeeId);
        
        // Daily hours data (last 30 days)
        List<ChartsDataResponse.DailyHoursData> dailyHoursData = getDailyHoursData(employeeId, 30);
        
        // Monthly tasks data (last 12 months)
        List<ChartsDataResponse.MonthlyTasksData> monthlyTasksData = getMonthlyTasksData(employeeId, 12);
        
        // Rating trend data (placeholder - would need rating system)
        List<ChartsDataResponse.RatingTrendData> ratingTrendData = getRatingTrendData(employeeId);
        
        // Service distribution data
        List<ChartsDataResponse.ServiceDistributionData> serviceDistributionData = getServiceDistributionData(employeeId);
        
        return ChartsDataResponse.builder()
                .dailyHoursData(dailyHoursData)
                .monthlyTasksData(monthlyTasksData)
                .ratingTrendData(ratingTrendData)
                .serviceDistributionData(serviceDistributionData)
                .build();
    }

    /**
     * Calculate total hours worked in a month
     */
    private Double calculateTotalHoursForMonth(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        List<TimeLog> timeLogs = timeLogRepository.findTimeLogsByEmployeeIdAndDateRange(
            employeeId, startDate, endDate);
        
        return timeLogs.stream()
                .filter(tl -> tl.getHoursWorked() != null)
                .mapToDouble(TimeLog::getHoursWorked)
                .sum();
    }

    /**
     * Calculate performance bonus based on completed tasks
     */
    private Double calculatePerformanceBonus(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        Long completedTasks = taskRepository.countCompletedTasksThisMonthByEmployeeId(employeeId);
        
        // Bonus: 10% of base salary for every 5 completed tasks above 10
        int threshold = 10;
        int bonusTasks = Math.max(0, completedTasks.intValue() - threshold);
        int bonusMultiplier = bonusTasks / 5; // Every 5 tasks
        
        return BASE_SALARY * PERFORMANCE_BONUS_RATE * bonusMultiplier;
    }

    /**
     * Calculate demand bonus based on jobs assigned
     */
    private Double calculateDemandBonus(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        Long jobsAssigned = manageAssignJobRepository.countJobsAssignedThisMonth(employeeId);
        
        // Bonus: 5% of base salary for every 3 jobs above 5
        int threshold = 5;
        int bonusJobs = Math.max(0, jobsAssigned.intValue() - threshold);
        int bonusMultiplier = bonusJobs / 3; // Every 3 jobs
        
        return BASE_SALARY * DEMAND_BONUS_RATE * bonusMultiplier;
    }

    /**
     * Get daily hours data for the last N days
     */
    private List<ChartsDataResponse.DailyHoursData> getDailyHoursData(UUID employeeId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<TimeLog> timeLogs = timeLogRepository.findTimeLogsByEmployeeIdAndDateRange(
            employeeId, startDate, endDate);
        
        // Group by date
        Map<LocalDate, Double> hoursByDate = timeLogs.stream()
                .filter(tl -> tl.getStartTime() != null && tl.getHoursWorked() != null)
                .collect(Collectors.groupingBy(
                    tl -> tl.getStartTime().toLocalDate(),
                    Collectors.summingDouble(TimeLog::getHoursWorked)
                ));
        
        // Fill in missing dates with 0
        List<ChartsDataResponse.DailyHoursData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Double hours = hoursByDate.getOrDefault(date, 0.0);
            result.add(ChartsDataResponse.DailyHoursData.builder()
                    .date(date.format(formatter))
                    .hours(Math.round(hours * 100.0) / 100.0)
                    .build());
        }
        
        return result;
    }

    /**
     * Get monthly tasks data for the last N months
     */
    private List<ChartsDataResponse.MonthlyTasksData> getMonthlyTasksData(UUID employeeId, int months) {
        List<ChartsDataResponse.MonthlyTasksData> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = months - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();
            
            // Get tasks for this month
            List<Task> tasks = taskRepository.findTasksByEmployeeIdAndDateRange(
                employeeId, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
            
            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .count();
            
            result.add(ChartsDataResponse.MonthlyTasksData.builder()
                    .month(yearMonth.format(formatter))
                    .completedTasks(completedTasks)
                    .totalTasks(totalTasks)
                    .build());
        }
        
        return result;
    }

    /**
     * Get rating trend data (placeholder - would need rating system)
     */
    private List<ChartsDataResponse.RatingTrendData> getRatingTrendData(UUID employeeId) {
        // TODO: Implement when rating system is available
        // For now, return empty list or placeholder data
        return new ArrayList<>();
    }

    /**
     * Get service distribution data
     */
    private List<ChartsDataResponse.ServiceDistributionData> getServiceDistributionData(UUID employeeId) {
        List<ManageAssignJob> assignments = manageAssignJobRepository.findByEmployee_Id(employeeId);
        
        // Group by job type/service type
        Map<String, Long> distribution = new java.util.HashMap<>();
        
        for (ManageAssignJob assignment : assignments) {
            Job job = assignment.getJob();
            String serviceType = "General Service";
            
            if (com.TenX.Automobile.enums.JobType.PROJECT.equals(job.getType())) {
                // Look up project title
                serviceType = projectRepository.findById(job.getTypeId())
                    .map(Project::getTitle)
                    .orElse("General Project");
            } else if (com.TenX.Automobile.enums.JobType.SERVICE.equals(job.getType())) {
                // Look up service title
                serviceType = serviceRepository.findById(job.getTypeId())
                    .map(com.TenX.Automobile.entity.Service::getTitle)
                    .orElse("General Service");
            }
            
            distribution.put(serviceType, distribution.getOrDefault(serviceType, 0L) + 1);
        }
        
        return distribution.entrySet().stream()
                .map(entry -> ChartsDataResponse.ServiceDistributionData.builder()
                        .serviceType(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
    }
}
