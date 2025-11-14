package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.request.EmployeeRegistrationRequest;
import com.TenX.Automobile.model.dto.request.UpdateEmployeeProfileRequest;
import com.TenX.Automobile.model.dto.response.DailyHoursResponse;
import com.TenX.Automobile.model.dto.response.EmployeeProfileResponse;
import com.TenX.Automobile.model.dto.response.EmployeeResponse;
import com.TenX.Automobile.model.dto.response.RatingTrendResponse;
import com.TenX.Automobile.model.dto.response.StaffDashboardStatsResponse;
import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.entity.ManageAssignJob;
import com.TenX.Automobile.model.entity.TimeLog;
import com.TenX.Automobile.model.enums.Role;
import com.TenX.Automobile.repository.EmployeeRepository;
import com.TenX.Automobile.repository.ManageAssignJobRepository;
import com.TenX.Automobile.repository.TimeLogRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final TimeLogRepository timeLogRepository;

    public Employee addEmployee(EmployeeRegistrationRequest employeeRegistrationRequest){

        log.info("Employee Registration Request : {}", employeeRegistrationRequest.getEmail());

        if(employeeRepository.findByEmail(employeeRegistrationRequest.getEmail()).isPresent()){
            throw new RuntimeException("Employee already exists in " +  employeeRegistrationRequest.getEmail());
        }
        final String speciality = employeeRegistrationRequest.getSpeciality();

        Employee employee = Employee.builder()
                .employeeId(generateEmployeeId())
                .email(employeeRegistrationRequest.getEmail())
                .password(passwordEncoder.encode(employeeRegistrationRequest.getPassword()))
                .firstName(employeeRegistrationRequest.getFirstName())
                .lastName(employeeRegistrationRequest.getLastName())
                .nationalId(employeeRegistrationRequest.getNationalId())
                .phoneNumber(employeeRegistrationRequest.getPhoneNumber())
                .specialty(speciality)
                .build();
                
        if(employeeRegistrationRequest.getEmployeeType() != null){
            switch (employeeRegistrationRequest.getEmployeeType()){
                case MANAGER -> employee.addRole(Role.MANAGER);
                case STAFF -> employee.addRole(Role.STAFF);
                default -> employee.addRole(Role.STAFF);
            }
        }

        System.out.print("Saving Employee : "+employee);
        Employee savedEmployee = employeeRepository.save(employee);

        log.info("Employee registered successfully with ID: {}", savedEmployee.getEmployeeId());

        return  savedEmployee;
    }

    /**
     * Get employee profile by ID
     */
    public EmployeeProfileResponse getEmployeeProfile(UUID employeeId) {
        log.info("Fetching employee profile for ID: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return convertToProfileResponse(employee);
    }

    /**
     * Update employee profile
     */
    public EmployeeProfileResponse updateEmployeeProfile(UUID employeeId, UpdateEmployeeProfileRequest request) {
        log.info("Updating employee profile for ID: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            // Check if email is already taken by another employee
            employeeRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(employeeId)) {
                    throw new RuntimeException("Email already exists: " + request.getEmail());
                }
            });
            employee.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            employee.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImageUrl() != null) {
            employee.setProfileImageUrl(request.getProfileImageUrl());
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee profile updated successfully for ID: {}", employeeId);

        return convertToProfileResponse(updatedEmployee);
    }

    /**
     * Convert Employee entity to EmployeeProfileResponse DTO
     */
    private EmployeeProfileResponse convertToProfileResponse(Employee employee) {
        String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") +
                         (employee.getLastName() != null ? " " + employee.getLastName() : "").trim();

        return EmployeeProfileResponse.builder()
                .id(employee.getId())
                .name(fullName.isEmpty() ? null : fullName)
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .role(employee.getRoles())
                .email(employee.getEmail())
                .phone(employee.getPhoneNumber())
                .joinDate(employee.getCreatedAt())
                .currentRating(null) // TODO: Calculate from reviews if available
                .totalReviews(0) // TODO: Calculate from reviews if available
                .specialty(employee.getSpecialty())
                .employeeId(employee.getEmployeeId())
                .profileImageUrl(employee.getProfileImageUrl())
                .build();
    }

    private String generateEmployeeId(){
        log.info("Generating Employee ID...");

        List<String> existingIds = employeeRepository.getAllEmployeeIds();
        if(existingIds.isEmpty()){
            return  "EMP0001";
        }

        List<Integer> numbers = existingIds.stream()
                .filter(id->id.startsWith("EMP"))
                .map(id->Integer.parseInt(id.substring(4)))
                .sorted()
                .toList();

        int nextId = findNextNumber(numbers);

        String newId =String.format("EMP%04d",nextId);
        log.info("Generated Employee ID: {}", newId);

        return newId;
    }


    public int findNextNumber(List<Integer> numbers){
        for(int i=0;i<numbers.size();i++){
            if(numbers.get(i) != i+1){
                return i+1;
            }
        }
        return numbers.size() +1;
    }

    /**
     * Get all employees with optional filters
     * @param specialty Optional filter by specialty
     * @param date Optional filter by joined date
     * @return List of employees matching the filters
     */
    public List<EmployeeResponse> getEmployees(String specialty, LocalDateTime date) {
        log.info("Fetching employees with specialty: {}, date: {}", specialty, date);
        List<Employee> employees = employeeRepository.findEmployeesByFilters(specialty, date);
        return employees.stream()
                .map(this::convertToEmployeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all employees with date range filters
     * @param specialty Optional filter by specialty
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return List of employees matching the filters
     */
    public List<EmployeeResponse> getEmployeesByDateRange(String specialty, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching employees with specialty: {}, startDate: {}, endDate: {}", specialty, startDate, endDate);
        List<Employee> employees = employeeRepository.findEmployeesByDateRange(specialty, startDate, endDate);
        return employees.stream()
                .map(this::convertToEmployeeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Employee entity to EmployeeResponse DTO
     */
    private EmployeeResponse convertToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .nationalId(employee.getNationalId())
                .phoneNumber(employee.getPhoneNumber())
                .profileImageUrl(employee.getProfileImageUrl())
                .specialty(employee.getSpecialty())
                .roles(employee.getRoles())
                .joinedDate(employee.getCreatedAt())
                .lastLoginAt(employee.getLastLoginAt())
                .enabled(employee.isEnabled())
                .build();
    }

    public StaffDashboardStatsResponse getEmployeeDashboardStats(UUID employeeId){
        log.info("Fetching dashboard stats for employee: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Get current month start and end
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().withDayOfMonth(
                LocalDateTime.now().toLocalDate().lengthOfMonth()
        ).withHour(23).withMinute(59).withSecond(59);

        // Get today's date range
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // Get all jobs assigned to this employee in current month
        List<ManageAssignJob> allJobs = manageAssignJobRepository.findByEmployee_Id(employeeId);
        
        // Filter jobs for today
        long tasksToday = allJobs.stream()
                .filter(job -> job.getCreatedAt() != null)
                .filter(job -> job.getCreatedAt().isAfter(todayStart) && job.getCreatedAt().isBefore(todayEnd))
                .count();

        // Filter completed tasks in current month
        long completedTasks = allJobs.stream()
                .filter(job -> job.getJob() != null && job.getJob().getStatus() != null)
                .filter(job -> "COMPLETED".equalsIgnoreCase(job.getJob().getStatus()))
                .filter(job -> job.getJob().getUpdatedAt() != null)
                .filter(job -> job.getJob().getUpdatedAt().isAfter(monthStart) && 
                              job.getJob().getUpdatedAt().isBefore(monthEnd))
                .count();

        // Calculate total hours from TimeLog for current month
        Double totalHours = timeLogRepository.calculateTotalHoursByDateRange(
                employeeId,
                monthStart.toLocalDate(),
                monthEnd.toLocalDate()
        );

        // Get rating (random for now, will be calculated from reviews later)
        Double rating = employee.getEmpRating() != null ? employee.getEmpRating() : 
                        Math.round((Math.random() * 5) * 10.0) / 10.0;

        String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") +
                         (employee.getLastName() != null ? " " + employee.getLastName() : "");

        return StaffDashboardStatsResponse.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(fullName.trim())
                .tasksToday((int) tasksToday)
                .completedTasks((int) completedTasks)
                .totalHours(totalHours != null ? Math.round(totalHours * 10.0) / 10.0 : 0.0)
                .rating(rating)
                .currentMonthStart(monthStart.toLocalDate().toString())
                .currentMonthEnd(monthEnd.toLocalDate().toString())
                .build();
    }

    public DailyHoursResponse getDailyHours(UUID employeeId, String week) {
        log.info("Fetching daily hours for employee: {}, week: {}", employeeId, week);

        // Get current week start (Monday) and end (Friday)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(java.time.DayOfWeek.FRIDAY);

        // Get time logs for the week
        List<TimeLog> timeLogs = timeLogRepository.findByEmployeeAndDateRange(
                employeeId,
                weekStart,
                weekEnd
        );

        // Initialize hours for each day (Mon-Fri)
        List<Double> dailyHours = new java.util.ArrayList<>(java.util.Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
        List<String> labels = java.util.Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri");

        // Aggregate hours by day of week
        for (TimeLog log : timeLogs) {
            if (log.getStartTime() != null && log.getHoursWorked() != null) {
                java.time.DayOfWeek dayOfWeek = log.getStartTime().getDayOfWeek();
                int dayIndex = dayOfWeek.getValue() - 1; // Monday = 0, Friday = 4
                
                if (dayIndex >= 0 && dayIndex < 5) {
                    dailyHours.set(dayIndex, dailyHours.get(dayIndex) + log.getHoursWorked());
                }
            }
        }

        // Round to 1 decimal place
        dailyHours = dailyHours.stream()
                .map(h -> Math.round(h * 10.0) / 10.0)
                .collect(java.util.stream.Collectors.toList());

        // Calculate totals
        double totalHours = dailyHours.stream().mapToDouble(Double::doubleValue).sum();
        double averageHours = totalHours / 5.0;

        return DailyHoursResponse.builder()
                .weekStart(weekStart.toString())
                .weekEnd(weekEnd.toString())
                .chartType("bar")
                .labels(labels)
                .data(dailyHours)
                .totalHours(Math.round(totalHours * 10.0) / 10.0)
                .averageHoursPerDay(Math.round(averageHours * 100.0) / 100.0)
                .build();
    }

    public RatingTrendResponse getRatingTrend(UUID employeeId) {
        log.info("Fetching rating trend for employee: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // For now, generate mock weekly ratings
        // In a real system, this would query a reviews/ratings table
        List<String> labels = java.util.Arrays.asList("Week 1", "Week 2", "Week 3", "Week 4");
        List<Double> weeklyRatings = new java.util.ArrayList<>();
        
        Double baseRating = employee.getEmpRating() != null ? employee.getEmpRating() : 4.5;
        
        // Generate realistic weekly ratings with slight variations
        for (int i = 0; i < 4; i++) {
            double variation = (Math.random() - 0.5) * 0.4; // ±0.2 variation
            double rating = Math.max(0, Math.min(5, baseRating + variation));
            weeklyRatings.add(Math.round(rating * 10.0) / 10.0);
        }

        // Calculate average
        double avgRating = weeklyRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        avgRating = Math.round(avgRating * 100.0) / 100.0;

        // Determine trend
        String trend = "stable";
        if (weeklyRatings.size() >= 2) {
            double firstHalf = (weeklyRatings.get(0) + weeklyRatings.get(1)) / 2.0;
            double secondHalf = (weeklyRatings.get(2) + weeklyRatings.get(3)) / 2.0;
            
            if (secondHalf > firstHalf + 0.1) {
                trend = "improving";
            } else if (secondHalf < firstHalf - 0.1) {
                trend = "declining";
            }
        }

        return RatingTrendResponse.builder()
                .period("Last 30 Days")
                .chartType("line")
                .labels(labels)
                .data(weeklyRatings)
                .averageRating(avgRating)
                .totalReviews(18) // Mock value, should come from reviews table
                .trend(trend)
                .build();
    }
}

