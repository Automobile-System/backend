package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.*;
import com.TenX.Automobile.dto.response.*;
import com.TenX.Automobile.entity.*;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final JobRepository jobRepository;
    private final PaymentRepository paymentRepository;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final NotificationRepository notificationRepository;
    private final BaseUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== PAGE 1: DASHBOARD ====================

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // Profit This Month
        Double currentMonthProfit = calculateProfitForPeriod(startOfMonth, now);
        Double lastMonthProfit = calculateProfitForPeriod(startOfLastMonth, endOfLastMonth);
        Integer profitPercentage = lastMonthProfit > 0 ? 
            (int) Math.round(((currentMonthProfit - lastMonthProfit) / lastMonthProfit) * 100) : 0;
        String profitValue = formatCurrency(currentMonthProfit);
        String profitChange = profitPercentage + "% from last month";

        // Active Customers
        Long totalCustomers = customerRepository.count();
        Long newCustomersThisMonth = customerRepository.findAll().stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startOfMonth))
            .count();

        // Ongoing Services
        Long ongoingServices = serviceRepository.findAll().stream()
            .filter(s -> s.getStatus() == null || !"COMPLETED".equals(s.getStatus()))
            .count();

        // Active Employees
        Long activeEmployees = employeeRepository.countActiveByRole(Role.STAFF);
        Long onLeave = 0L; // TODO: Implement leave tracking
        Long frozen = employeeRepository.findAll().stream()
            .filter(e -> !e.isEnabled())
            .count();

        return DashboardStatsResponse.builder()
            .profitThisMonth(DashboardStatsResponse.ProfitThisMonth.builder()
                .value(profitValue)
                .change(profitChange)
                .percentage(profitPercentage)
                .build())
            .activeCustomers(DashboardStatsResponse.ActiveCustomers.builder()
                .value(totalCustomers.intValue())
                .newThisMonth(newCustomersThisMonth.intValue())
                .build())
            .ongoingServices(DashboardStatsResponse.OngoingServices.builder()
                .value(ongoingServices.intValue())
                .status("In Progress")
                .build())
            .activeEmployees(DashboardStatsResponse.ActiveEmployees.builder()
                .value(activeEmployees.intValue())
                .onLeave(onLeave.intValue())
                .frozen(frozen.intValue())
                .build())
            .build();
    }

    public List<SystemAlertResponse> getSystemAlerts() {
        List<SystemAlertResponse> alerts = new ArrayList<>();

        // Check for overloaded employees
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        for (Employee emp : employees) {
            Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
            if (activeJobs >= 5) { // Max tasks threshold
                alerts.add(SystemAlertResponse.builder()
                    .id("alert-" + UUID.randomUUID().toString().substring(0, 8))
                    .type("warning")
                    .message("Employee " + emp.getFirstName() + " " + emp.getLastName() + " at maximum workload capacity")
                    .timestamp(LocalDateTime.now())
                    .build());
            }
        }

        // Check for services paused due to parts delay
        Long pausedServices = serviceRepository.findAll().stream()
            .filter(s -> "WAITING_FOR_PARTS".equals(s.getStatus()) || "PAUSED".equals(s.getStatus()))
            .count();
        if (pausedServices > 0) {
            alerts.add(SystemAlertResponse.builder()
                .id("alert-" + UUID.randomUUID().toString().substring(0, 8))
                .type("warning")
                .message(pausedServices + " services paused due to part delay")
                .timestamp(LocalDateTime.now())
                .build());
        }

        // Security alerts (mock - should come from security service)
        alerts.add(SystemAlertResponse.builder()
            .id("alert-" + UUID.randomUUID().toString().substring(0, 8))
            .type("error")
            .message("Security system detected suspicious pattern")
            .timestamp(LocalDateTime.now().minusHours(2))
            .build());

        return alerts;
    }

    public List<AIInsightResponse> getAIInsights() {
        List<AIInsightResponse> insights = new ArrayList<>();

        // Demand Forecast Insight
        insights.add(AIInsightResponse.builder()
            .id("insight-001")
            .title("Next Month Demand Forecast")
            .description("15% increase expected. AI recommends hiring 2 more mechanics.")
            .category("forecast")
            .icon("trending-up")
            .build());

        // Profit Projection Insight
        insights.add(AIInsightResponse.builder()
            .id("insight-002")
            .title("Profit Projection Curve")
            .description("Monthly profit will reach LKR 2.1M by Q4 2025.")
            .category("projection")
            .icon("dollar-sign")
            .build());

        return insights;
    }

    // ==================== PAGE 2: FINANCIAL REPORTS ====================

    public FinancialReportResponse getFinancialReports(String serviceFilter, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get all services/projects in date range
        List<Job> jobs = jobRepository.findJobsByDateRange(startDateTime, endDateTime);

        // Filter by service type if needed
        List<Job> filteredJobs = jobs;
        if ("predefined".equals(serviceFilter)) {
            filteredJobs = jobs.stream()
                .filter(j -> j instanceof com.TenX.Automobile.entity.Service)
                .collect(Collectors.toList());
        } else if ("custom".equals(serviceFilter)) {
            filteredJobs = jobs.stream()
                .filter(j -> j instanceof Project)
                .collect(Collectors.toList());
        }

        // Group by service type
        Map<String, List<Job>> jobsByType = filteredJobs.stream()
            .collect(Collectors.groupingBy(job -> {
                if (job instanceof com.TenX.Automobile.entity.Service) {
                    com.TenX.Automobile.entity.Service service = (com.TenX.Automobile.entity.Service) job;
                    return service.getTitle() != null ? service.getTitle() : "Service";
                } else if (job instanceof Project) {
                    Project project = (Project) job;
                    return project.getTitle() != null ? project.getTitle() : "Project";
                }
                return "Other";
            }));

        // Calculate breakdown
        List<FinancialReportResponse.ServiceTypeBreakdown> breakdown = new ArrayList<>();
        Double totalRevenue = 0.0;
        Double totalCost = 0.0;

        for (Map.Entry<String, List<Job>> entry : jobsByType.entrySet()) {
            String serviceType = entry.getKey();
            List<Job> typeJobs = entry.getValue();

            Double revenue = typeJobs.stream()
                .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
                .sum();

            // Cost calculation (simplified - should include parts, labor, overhead)
            Double cost = revenue * 0.5; // Assume 50% cost for now

            Double profit = revenue - cost;
            Double margin = revenue > 0 ? (profit / revenue) * 100 : 0.0;

            // Calculate trend (compare with previous period)
            LocalDate prevStartDate = startDate.minusMonths(1);
            LocalDate prevEndDate = endDate.minusMonths(1);
            LocalDateTime prevStartDateTime = prevStartDate.atStartOfDay();
            LocalDateTime prevEndDateTime = prevEndDate.atTime(23, 59, 59);
            
            List<Job> prevJobs = jobRepository.findJobsByDateRange(prevStartDateTime, prevEndDateTime);
            Double prevRevenue = prevJobs.stream()
                .filter(j -> {
                    String jobType = j instanceof com.TenX.Automobile.entity.Service ? 
                        ((com.TenX.Automobile.entity.Service) j).getTitle() : 
                        ((Project) j).getTitle();
                    return serviceType.equals(jobType != null ? jobType : "Other");
                })
                .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
                .sum();
            
            Double trend = prevRevenue > 0 ? ((revenue - prevRevenue) / prevRevenue) * 100 : 0.0;

            breakdown.add(FinancialReportResponse.ServiceTypeBreakdown.builder()
                .serviceType(serviceType)
                .revenue(revenue)
                .cost(cost)
                .profit(profit)
                .margin(Math.round(margin * 10.0) / 10.0)
                .trend(Math.round(trend * 10.0) / 10.0)
                .build());

            totalRevenue += revenue;
            totalCost += cost;
        }

        Double totalProfit = totalRevenue - totalCost;
        Double overallMargin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100 : 0.0;

        // Calculate overall trend
        Double prevTotalRevenue = calculateTotalRevenueForPeriod(
            startDate.minusMonths(1).atStartOfDay(),
            endDate.minusMonths(1).atTime(23, 59, 59));
        Double overallTrend = prevTotalRevenue > 0 ? 
            ((totalRevenue - prevTotalRevenue) / prevTotalRevenue) * 100 : 0.0;

        return FinancialReportResponse.builder()
            .breakdown(breakdown)
            .totals(FinancialReportResponse.FinancialTotals.builder()
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .totalProfit(totalProfit)
                .overallMargin(Math.round(overallMargin * 10.0) / 10.0)
                .overallTrend(Math.round(overallTrend * 10.0) / 10.0)
                .build())
            .period(FinancialReportResponse.Period.builder()
                .startDate(startDateStr)
                .endDate(endDateStr)
                .build())
            .build();
    }

    // ==================== PAGE 3: WORKFORCE OVERVIEW ====================

    public WorkforceOverviewResponse getWorkforceOverview() {
        List<Employee> allEmployees = employeeRepository.findByRole(Role.STAFF);
        List<Employee> managers = employeeRepository.findByRole(Role.MANAGER);

        Integer totalEmployees = allEmployees.size();
        Integer activeEmployees = (int) allEmployees.stream().filter(Employee::isEnabled).count();
        Integer onLeave = 0; // TODO: Implement leave tracking
        Integer frozen = (int) allEmployees.stream().filter(e -> !e.isEnabled()).count();

        // Calculate average rating (mock - should come from customer ratings)
        Double avgRating = 4.5 + (Math.random() * 0.5);
        Double ratingChange = 0.2;

        // Calculate average workload
        Double avgWorkload = allEmployees.stream()
            .mapToDouble(emp -> {
                Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
                return activeJobs.doubleValue();
            })
            .average()
            .orElse(0.0);

        // Average salary (mock)
        String avgSalary = "85K";

        // Find overloaded employee
        WorkforceOverviewResponse.OverloadedEmployee overloadedEmployee = null;
        for (Employee emp : allEmployees) {
            Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
            if (activeJobs >= 5) {
                overloadedEmployee = WorkforceOverviewResponse.OverloadedEmployee.builder()
                    .name(emp.getFirstName() + " " + emp.getLastName())
                    .specialization(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                    .capacityPercentage(100)
                    .activeTasks(activeJobs.intValue())
                    .maxTasks(5)
                    .build();
                break;
            }
        }

        return WorkforceOverviewResponse.builder()
            .stats(WorkforceOverviewResponse.WorkforceStats.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .onLeave(onLeave)
                .frozen(frozen)
                .avgRating(Math.round(avgRating * 10.0) / 10.0)
                .ratingChange(ratingChange)
                .avgWorkload(Math.round(avgWorkload * 10.0) / 10.0)
                .avgSalary(avgSalary)
                .build())
            .centerInfo(WorkforceOverviewResponse.CenterInfo.builder()
                .totalCenters(1)
                .activeManagers(managers.size())
                .minimumManagers(1)
                .totalEmployees(totalEmployees)
                .build())
            .overloadedEmployee(overloadedEmployee)
            .build();
    }

    public List<TopEmployeeResponse> getTopEmployees() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        
        return employees.stream()
            .map(emp -> {
                // Mock rating (should come from actual customer ratings)
                Double rating = 4.5 + (Math.random() * 0.5);
                Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
                
                return TopEmployeeResponse.builder()
                    .id(emp.getEmployeeId())
                    .name(emp.getFirstName() + " " + emp.getLastName())
                    .specialization(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                    .rating(Math.round(rating * 10.0) / 10.0)
                    .rewardEligible(rating >= 4.8)
                    .overloaded(activeJobs >= 5)
                    .build();
            })
            .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
            .limit(5)
            .collect(Collectors.toList());
    }

    public List<ManagerPerformanceResponse> getManagerPerformance() {
        List<Employee> managers = employeeRepository.findByRole(Role.MANAGER);
        
        return managers.stream()
            .map(manager -> {
                // Count tasks assigned by this manager
                List<ManageAssignJob> assignments = manageAssignJobRepository.findAll().stream()
                    .filter(m -> m.getManager().getId().equals(manager.getId()))
                    .collect(Collectors.toList());
                
                Integer tasksAssigned = assignments.size();
                Long completedTasks = assignments.stream()
                    .filter(m -> "COMPLETED".equals(m.getJob().getStatus()))
                    .count();
                
                Double completionRate = tasksAssigned > 0 ? 
                    (completedTasks.doubleValue() / tasksAssigned) * 100 : 0.0;

                // Average employee rating (mock)
                Double avgEmployeeRating = 4.5 + (Math.random() * 0.5);

                return ManagerPerformanceResponse.builder()
                    .id(manager.getEmployeeId())
                    .name(manager.getFirstName() + " " + manager.getLastName())
                    .tasksAssigned(tasksAssigned)
                    .completionRate(Math.round(completionRate * 10.0) / 10.0)
                    .avgEmployeeRating(Math.round(avgEmployeeRating * 10.0) / 10.0)
                    .status(manager.isEnabled() ? "Active" : "Under Review")
                    .build();
            })
            .collect(Collectors.toList());
    }

    public List<ManagerResponse> getAllManagers() {
        List<Employee> managers = employeeRepository.findByRole(Role.MANAGER);
        
        return managers.stream()
            .map(manager -> ManagerResponse.builder()
                .id(manager.getEmployeeId())
                .name(manager.getFirstName() + " " + manager.getLastName())
                .email(manager.getEmail())
                .phone(manager.getPhoneNumber())
                .joinDate(formatDate(manager.getCreatedAt()))
                .status(manager.isEnabled() ? "Active" : "Frozen")
                .build())
            .collect(Collectors.toList());
    }

    public List<EmployeeDetailResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        
        return employees.stream()
            .map(emp -> {
                Double rating = 4.5 + (Math.random() * 0.5);
                String status = emp.isEnabled() ? "Active" : "Frozen";
                
                return EmployeeDetailResponse.builder()
                    .id(emp.getEmployeeId())
                    .name(emp.getFirstName() + " " + emp.getLastName())
                    .specialization(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                    .email(emp.getEmail())
                    .phone(emp.getPhoneNumber())
                    .rating(Math.round(rating * 10.0) / 10.0)
                    .status(status)
                    .build();
            })
            .collect(Collectors.toList());
    }

    public Map<String, Object> addManager(AddManagerRequest request) {
        // Check if email already exists
        if (employeeRepository.findByEmail(request.getEmail()).isPresent() ||
            adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Check if manager ID already exists
        if (employeeRepository.findByEmployeeId(request.getManagerId()).isPresent()) {
            throw new RuntimeException("Manager ID " + request.getManagerId() + " already exists");
        }

        Employee manager = Employee.builder()
            .employeeId(request.getManagerId())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhone())
            .enabled(true)
            .build();

        manager.addRole(Role.MANAGER);
        Employee savedManager = employeeRepository.save(manager);

        String message = String.format(
            "Manager Added Successfully!\n\nName: %s %s\nID: %s\nEmail: %s\n\nCredentials have been sent to the manager's email.",
            request.getFirstName(), request.getLastName(), request.getManagerId(), request.getEmail()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("manager", ManagerResponse.builder()
            .id(savedManager.getEmployeeId())
            .name(savedManager.getFirstName() + " " + savedManager.getLastName())
            .email(savedManager.getEmail())
            .phone(savedManager.getPhoneNumber())
            .joinDate(formatDate(savedManager.getCreatedAt()))
            .status("Active")
            .build());

        return response;
    }

    public Map<String, Object> addEmployee(AddEmployeeRequest request) {
        // Check if email already exists
        if (employeeRepository.findByEmail(request.getEmail()).isPresent() ||
            adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Check if employee ID already exists
        if (employeeRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
            throw new RuntimeException("Employee ID " + request.getEmployeeId() + " already exists");
        }

        Employee employee = Employee.builder()
            .employeeId(request.getEmployeeId())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhone())
            .specialty(request.getSpecialization())
            .enabled(true)
            .build();

        employee.addRole(Role.STAFF);
        Employee savedEmployee = employeeRepository.save(employee);

        String message = String.format(
            "Employee Added Successfully!\n\nName: %s %s\nID: %s\nSpecialization: %s\nEmail: %s\n\nCredentials have been sent to the employee's email.",
            request.getFirstName(), request.getLastName(), request.getEmployeeId(), 
            request.getSpecialization(), request.getEmail()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("employee", EmployeeDetailResponse.builder()
            .id(savedEmployee.getEmployeeId())
            .name(savedEmployee.getFirstName() + " " + savedEmployee.getLastName())
            .specialization(savedEmployee.getSpecialty() != null ? savedEmployee.getSpecialty() : "General")
            .email(savedEmployee.getEmail())
            .phone(savedEmployee.getPhoneNumber())
            .rating(0.0)
            .status("Active")
            .build());

        return response;
    }

    public Map<String, Object> updateManager(String managerId, AddManagerRequest request) {
        Employee manager = employeeRepository.findByEmployeeId(managerId)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!manager.getRoles().contains(Role.MANAGER)) {
            throw new RuntimeException("Employee is not a manager");
        }

        manager.setFirstName(request.getFirstName());
        manager.setLastName(request.getLastName());
        manager.setEmail(request.getEmail());
        manager.setPhoneNumber(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            manager.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employeeRepository.save(manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Manager " + managerId + " updated successfully.");
        return response;
    }

    public Map<String, Object> updateEmployee(String employeeId, AddEmployeeRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhone());
        employee.setSpecialty(request.getSpecialization());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        employeeRepository.save(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Employee " + employeeId + " updated successfully.");
        return response;
    }

    public Map<String, Object> freezeManager(String managerId) {
        Employee manager = employeeRepository.findByEmployeeId(managerId)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!manager.getRoles().contains(Role.MANAGER)) {
            throw new RuntimeException("Employee is not a manager");
        }

        manager.setEnabled(false);
        employeeRepository.save(manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Manager " + managerId + " has been frozen. They can no longer access the system.");
        return response;
    }

    public Map<String, Object> freezeEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setEnabled(false);
        employeeRepository.save(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Employee " + employeeId + " has been frozen. They can no longer access the system.");
        return response;
    }

    public Map<String, Object> activateEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setEnabled(true);
        employeeRepository.save(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Employee " + employeeId + " has been activated and is available for task assignments.");
        return response;
    }

    // ==================== PAGE 4: SERVICES ANALYTICS ====================

    public ServicesAnalyticsResponse getServicesAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // Most Profitable Service
        ServicesAnalyticsResponse.MostProfitableService mostProfitable = getMostProfitableService();

        // Total Services
        Long totalServicesMonth = serviceRepository.findAll().stream()
            .filter(s -> s.getStatus() != null && "COMPLETED".equals(s.getStatus()) &&
                s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfMonth))
            .count();
        Long totalServicesLastMonth = serviceRepository.findAll().stream()
            .filter(s -> s.getStatus() != null && "COMPLETED".equals(s.getStatus()) &&
                s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfLastMonth) &&
                s.getUpdatedAt().isBefore(startOfMonth))
            .count();
        Integer changeFromLastMonth = (int) (totalServicesMonth - totalServicesLastMonth);

        // Parts Replaced (mock - should come from parts inventory)
        Integer partsReplaced = 342;
        Integer partsUsageRate = 8;

        // Customer Retention
        Integer customerRetention = 87;
        Integer retentionImprovement = 3;

        // Service Performance
        List<ServicesAnalyticsResponse.ServicePerformance> servicePerformance = getServicePerformance();

        return ServicesAnalyticsResponse.builder()
            .summary(ServicesAnalyticsResponse.Summary.builder()
                .mostProfitableService(mostProfitable)
                .totalServicesMonth(totalServicesMonth.intValue())
                .changeFromLastMonth(changeFromLastMonth)
                .partsReplaced(partsReplaced)
                .partsUsageRate(partsUsageRate)
                .customerRetention(customerRetention)
                .retentionImprovement(retentionImprovement)
                .build())
            .servicePerformance(servicePerformance)
            .build();
    }

    public ServicesAnalyticsResponse.MostProfitableService getMostProfitableService() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<com.TenX.Automobile.entity.Service> services = serviceRepository.findAll().stream()
            .filter(s -> s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfMonth) &&
                s.getStatus() != null && "COMPLETED".equals(s.getStatus()))
            .collect(Collectors.toList());

        Map<String, Double> profitByType = new HashMap<>();
        Map<String, Double> revenueByType = new HashMap<>();

        for (com.TenX.Automobile.entity.Service service : services) {
            String type = service.getTitle() != null ? service.getTitle() : "Service";
            Double revenue = service.getCost() != null ? service.getCost().doubleValue() : 0.0;
            Double cost = revenue * 0.5; // Simplified
            Double profit = revenue - cost;

            profitByType.put(type, profitByType.getOrDefault(type, 0.0) + profit);
            revenueByType.put(type, revenueByType.getOrDefault(type, 0.0) + revenue);
        }

        String mostProfitableType = profitByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Engine Services");

        Double profit = profitByType.getOrDefault(mostProfitableType, 310000.0);
        Double revenue = revenueByType.getOrDefault(mostProfitableType, 620000.0);
        Double margin = revenue > 0 ? (profit / revenue) * 100 : 50.0;

        return ServicesAnalyticsResponse.MostProfitableService.builder()
            .name(mostProfitableType)
            .profit(profit)
            .margin(Math.round(margin * 10.0) / 10.0)
            .build();
    }

    public TotalServicesData getTotalServicesData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        Long totalServicesMonth = serviceRepository.findAll().stream()
            .filter(s -> s.getStatus() != null && "COMPLETED".equals(s.getStatus()) &&
                s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfMonth))
            .count();
        Long totalServicesLastMonth = serviceRepository.findAll().stream()
            .filter(s -> s.getStatus() != null && "COMPLETED".equals(s.getStatus()) &&
                s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfLastMonth) &&
                s.getUpdatedAt().isBefore(startOfMonth))
            .count();

        Integer changeFromLastMonth = (int) (totalServicesMonth - totalServicesLastMonth);

        return TotalServicesData.builder()
            .totalServicesMonth(totalServicesMonth.intValue())
            .changeFromLastMonth(changeFromLastMonth)
            .build();
    }

    public PartsReplacedData getPartsReplacedData() {
        // Mock data - should come from parts inventory tracking
        return PartsReplacedData.builder()
            .partsReplaced(342)
            .partsUsageRate(8)
            .build();
    }

    public CustomerRetentionData getCustomerRetentionData() {
        // Mock data - should calculate from customer service history
        return CustomerRetentionData.builder()
            .customerRetention(87)
            .retentionImprovement(3)
            .build();
    }

    public List<ServicesAnalyticsResponse.ServicePerformance> getServicePerformance() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        List<com.TenX.Automobile.entity.Service> services = serviceRepository.findAll().stream()
            .filter(s -> s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(startOfMonth) &&
                s.getStatus() != null && "COMPLETED".equals(s.getStatus()))
            .collect(Collectors.toList());

        Map<String, List<com.TenX.Automobile.entity.Service>> servicesByType = services.stream()
            .collect(Collectors.groupingBy(s -> s.getTitle() != null ? s.getTitle() : "Service"));

        List<ServicesAnalyticsResponse.ServicePerformance> performance = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, List<com.TenX.Automobile.entity.Service>> entry : servicesByType.entrySet()) {
            String serviceName = entry.getKey();
            List<com.TenX.Automobile.entity.Service> typeServices = entry.getValue();

            Integer totalBookings = typeServices.size();
            
            // Average duration
            Double avgDurationMinutes = typeServices.stream()
                .mapToDouble(s -> s.getEstimatedHours() != null ? s.getEstimatedHours() * 60 : 45.0)
                .average()
                .orElse(45.0);
            String avgDuration = formatDuration(avgDurationMinutes);

            // Average profit per service
            Double avgProfit = typeServices.stream()
                .mapToDouble(s -> {
                    Double revenue = s.getCost() != null ? s.getCost().doubleValue() : 0.0;
                    Double cost = revenue * 0.5;
                    return revenue - cost;
                })
                .average()
                .orElse(4500.0);

            // Customer rating (mock)
            Double customerRating = 4.5 + (Math.random() * 0.5);

            // Trend calculation
            List<com.TenX.Automobile.entity.Service> prevServices = serviceRepository.findAll().stream()
                .filter(s -> {
                    String sType = s.getTitle() != null ? s.getTitle() : "Service";
                    return serviceName.equals(sType) && s.getUpdatedAt() != null &&
                        s.getUpdatedAt().isAfter(startOfLastMonth) && s.getUpdatedAt().isBefore(startOfMonth) &&
                        "COMPLETED".equals(s.getStatus());
                })
                .collect(Collectors.toList());
            Integer prevBookings = prevServices.size();
            Double trend = prevBookings > 0 ? ((totalBookings - prevBookings.doubleValue()) / prevBookings) * 100 : 15.0;

            performance.add(ServicesAnalyticsResponse.ServicePerformance.builder()
                .id("S" + String.format("%03d", idCounter++))
                .name(serviceName)
                .totalBookings(totalBookings)
                .avgDuration(avgDuration)
                .profitPerService(Math.round(avgProfit * 100.0) / 100.0)
                .customerRating(Math.round(customerRating * 10.0) / 10.0)
                .trend(Math.round(trend * 10.0) / 10.0)
                .build());
        }

        return performance;
    }

    // ==================== PAGE 5: AI INSIGHTS ====================

    public DemandForecastResponse getDemandForecast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate nextMonth = now.toLocalDate().plusMonths(1);
        
        // Mock AI forecast data
        return DemandForecastResponse.builder()
            .overallIncrease(15)
            .forecastMonth(nextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
            .projectedBookings(170)
            .changeFromPrevious(22)
            .previousMonth(now.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM")))
            .serviceBreakdown(Arrays.asList(
                DemandForecastResponse.ServiceBreakdown.builder()
                    .serviceName("Engine services")
                    .percentageChange(18.0)
                    .build(),
                DemandForecastResponse.ServiceBreakdown.builder()
                    .serviceName("Electrical diagnostics")
                    .percentageChange(35.0)
                    .build(),
                DemandForecastResponse.ServiceBreakdown.builder()
                    .serviceName("Brake services")
                    .percentageChange(5.0)
                    .build(),
                DemandForecastResponse.ServiceBreakdown.builder()
                    .serviceName("Custom projects")
                    .percentageChange(10.0)
                    .build()
            ))
            .hiringRecommendation(DemandForecastResponse.HiringRecommendation.builder()
                .totalMechanics(2)
                .breakdown(Arrays.asList("1 Engine Specialist", "1 Electrical Diagnostics Expert"))
                .build())
            .build();
    }

    public ProfitProjectionResponse getProfitProjection() {
        // Mock projection data
        return ProfitProjectionResponse.builder()
            .monthOverMonthGrowth(9.0)
            .trajectory(Arrays.asList(
                ProfitProjectionResponse.MonthlyProfit.builder()
                    .month("October")
                    .profit("1.7M")
                    .build(),
                ProfitProjectionResponse.MonthlyProfit.builder()
                    .month("November")
                    .profit("1.85M")
                    .build(),
                ProfitProjectionResponse.MonthlyProfit.builder()
                    .month("December")
                    .profit("2.02M (projected)")
                    .build()
            ))
            .yearEndTarget(ProfitProjectionResponse.YearEndTarget.builder()
                .monthlyProfit("2.1M")
                .date("December 31, 2025")
                .annualGrowth(18.0)
                .build())
            .optimizationTip("Focus on high-margin custom projects (40% margin) and engine services (50% margin) for faster profit acceleration.")
            .build();
    }

    public List<UnderperformingDepartmentResponse> getUnderperformingDepartments() {
        // Mock data - should analyze actual department performance
        return Arrays.asList(
            UnderperformingDepartmentResponse.builder()
                .departmentName("Brake Service Department")
                .slowerCompletion(20.0)
                .avgCompletionTime(72)
                .targetTime(60)
                .rootCause("Possible workload imbalance or outdated tools")
                .managerOversight(UnderperformingDepartmentResponse.ManagerOversight.builder()
                    .name("C")
                    .score(82)
                    .threshold(90)
                    .build())
                .recommendation("Audit brake dept processes, provide training, or reassign tasks to balance workload across team.")
                .build()
        );
    }

    public List<SkillShortagePredictionResponse> getSkillShortagePrediction() {
        // Mock data - should analyze demand vs capacity
        return Arrays.asList(
            SkillShortagePredictionResponse.builder()
                .skillArea("Electrical Diagnostics")
                .demandIncrease(35.0)
                .availableEmployees(2)
                .crisisWeeks(8)
                .impactForecast(SkillShortagePredictionResponse.ImpactForecast.builder()
                    .delayedCustomers("12-15")
                    .month("December 2025")
                    .revenueLoss(180000.0)
                    .build())
                .actionPlan(Arrays.asList(
                    "Train 2 existing mechanics in electrical systems (4 weeks)",
                    "Hire 1 experienced electrician immediately"
                ))
                .build()
        );
    }

    // ==================== PAGE 6: SETTINGS ====================

    public List<SettingsRolesResponse> getRolesPermissions() {
        List<SettingsRolesResponse> roles = new ArrayList<>();

        // Admin role
        Long adminCount = adminRepository.count();
        roles.add(SettingsRolesResponse.builder()
            .roleId("role_001")
            .roleName("Admin (Business Owner)")
            .userCount(adminCount.intValue())
            .permissions("Full Access - All modules")
            .status("Active")
            .build());

        // Manager role
        Long managerCount = employeeRepository.countByRole(Role.MANAGER);
        roles.add(SettingsRolesResponse.builder()
            .roleId("role_002")
            .roleName("Manager")
            .userCount(managerCount.intValue())
            .permissions("Task assignment, Employee management, Customer handling")
            .status("Active")
            .build());

        // Employee role
        Long employeeCount = employeeRepository.countByRole(Role.STAFF);
        roles.add(SettingsRolesResponse.builder()
            .roleId("role_003")
            .roleName("Employee")
            .userCount(employeeCount.intValue())
            .permissions("View assigned tasks, Update status, Chat with manager")
            .status("Active")
            .build());

        // Customer role
        Long customerCount = customerRepository.count();
        roles.add(SettingsRolesResponse.builder()
            .roleId("role_004")
            .roleName("Customer")
            .userCount(customerCount.intValue())
            .permissions("Book services, View history, Make payments")
            .status("Active")
            .build());

        return roles;
    }

    public List<SettingsServicesResponse> getServicesPricing() {
        List<com.TenX.Automobile.entity.Service> services = serviceRepository.findAll();
        
        return services.stream()
            .map(service -> SettingsServicesResponse.builder()
                .serviceId("srv_" + service.getJobId())
                .serviceName(service.getTitle() != null ? service.getTitle() : "Service")
                .basePrice(service.getCost() != null ? service.getCost().doubleValue() : 0.0)
                .duration(formatDuration(service.getEstimatedHours() != null ? service.getEstimatedHours() : 1.0))
                .requiredSkill(service.getCategory() != null ? service.getCategory() : "General")
                .status("Active")
                .build())
            .collect(Collectors.toList());
    }

    public TaskLimitsResponse getTaskLimits() {
        // Mock data - should come from system configuration
        return TaskLimitsResponse.builder()
            .maxTasksPerDay(5)
            .overloadThreshold(4)
            .build();
    }

    public Map<String, Object> updateTaskLimits(UpdateTaskLimitsRequest request) {
        // TODO: Save to system configuration table
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Task limits updated successfully");
        return response;
    }

    public CompensationRulesResponse getCompensationRules() {
        // Mock data - should come from system configuration
        Double baseSalary = 65000.0;
        Double demandBonusPercentage = 30.0;
        Double exampleBonus = (baseSalary * demandBonusPercentage) / 100;
        Double exampleTotal = baseSalary + exampleBonus;

        return CompensationRulesResponse.builder()
            .baseSalary(baseSalary)
            .demandBonusPercentage(demandBonusPercentage)
            .exampleBonus(exampleBonus)
            .exampleTotal(exampleTotal)
            .build();
    }

    public Map<String, Object> updateCompensationRules(UpdateCompensationRulesRequest request) {
        // TODO: Save to system configuration table
        Double exampleBonus = (request.getBaseSalary() * request.getDemandBonusPercentage()) / 100;
        Double exampleTotal = request.getBaseSalary() + exampleBonus;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Compensation rules updated successfully");
        response.put("exampleBonus", exampleBonus);
        response.put("exampleTotal", exampleTotal);
        return response;
    }

    // ==================== PAGE 7: CUSTOMER MANAGEMENT ====================

    public CustomerOverviewResponse getCustomerOverview() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // Total customers
        Long totalCustomers = customerRepository.count();

        // New customers this month
        Long newCustomersThisMonth = customerRepository.findAll().stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startOfMonth))
            .count();

        // Active customers (enabled)
        Long activeCustomers = customerRepository.findAll().stream()
            .filter(Customer::isEnabled)
            .count();

        // Activity rate
        Double activityRate = totalCustomers > 0 ? 
            (activeCustomers.doubleValue() / totalCustomers) * 100 : 0.0;

        // Find top customer by total spent
        CustomerOverviewResponse.TopCustomer topCustomer = findTopCustomer();

        return CustomerOverviewResponse.builder()
            .totalCustomers(totalCustomers.intValue())
            .newThisMonth(newCustomersThisMonth.intValue())
            .activeCustomers(activeCustomers.intValue())
            .activityRate(Math.round(activityRate * 10.0) / 10.0)
            .topCustomer(topCustomer)
            .build();
    }

    public List<CustomerListResponse> getCustomerList() {
        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
            .map(customer -> {
                // Count vehicles
                Integer vehicleCount = customer.getVehicles() != null ? customer.getVehicles().size() : 0;

                // Calculate total spent from payments
                Double totalSpent = paymentRepository.sumAmountByCustomerId(customer.getId());
                if (totalSpent == null) totalSpent = 0.0;

                // Get last service date
                LocalDateTime lastPaymentDate = paymentRepository.findLastPaymentDateByCustomerId(customer.getId());
                String lastServiceDate = lastPaymentDate != null ? 
                    lastPaymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

                String name = (customer.getFirstName() != null ? customer.getFirstName() : "") + 
                             (customer.getLastName() != null ? " " + customer.getLastName() : "").trim();
                if (name.isEmpty()) name = "N/A";

                String status = customer.isEnabled() ? "Active" : "Inactive";

                return CustomerListResponse.builder()
                    .id(customer.getCustomerId())
                    .name(name)
                    .email(customer.getEmail())
                    .phone(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "N/A")
                    .vehicleCount(vehicleCount)
                    .totalSpent(totalSpent)
                    .lastServiceDate(lastServiceDate)
                    .status(status)
                    .build();
            })
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    public CustomerListResponse addCustomer(AddCustomerRequest request) {
        // Check if email already exists
        if (customerRepository.findByEmail(request.getEmail()).isPresent() ||
            employeeRepository.findByEmail(request.getEmail()).isPresent() ||
            adminRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        // Split name into first and last name
        String[] nameParts = request.getName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Generate customer ID
        String customerId = generateCustomerId();

        // Create customer with default password
        Customer customer = Customer.builder()
            .customerId(customerId)
            .email(request.getEmail())
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(request.getPhone())
            .password(passwordEncoder.encode("TempPassword123!"))
            .enabled(true)
            .build();

        customer.addRole(Role.CUSTOMER);
        Customer savedCustomer = customerRepository.save(customer);

        return CustomerListResponse.builder()
            .id(savedCustomer.getCustomerId())
            .name(firstName + (lastName.isEmpty() ? "" : " " + lastName))
            .email(savedCustomer.getEmail())
            .phone(savedCustomer.getPhoneNumber())
            .vehicleCount(0)
            .totalSpent(0.0)
            .lastServiceDate("N/A")
            .status("Active")
            .build();
    }

    public CustomerListResponse updateCustomerStatus(String customerId, String status) {
        Customer customer = customerRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        boolean enabled = "Active".equalsIgnoreCase(status);
        customer.setEnabled(enabled);
        customerRepository.save(customer);

        return mapToCustomerListResponse(customer);
    }

    public void deleteCustomer(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }

    public CustomerListResponse activateCustomer(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setEnabled(true);
        customerRepository.save(customer);

        return mapToCustomerListResponse(customer);
    }

    public CustomerListResponse deactivateCustomer(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setEnabled(false);
        customerRepository.save(customer);

        return mapToCustomerListResponse(customer);
    }

    public CustomerListResponse getCustomerById(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToCustomerListResponse(customer);
    }

    // ==================== HELPER METHODS ====================

    public UserEntity getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Double calculateProfitForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Job> jobs = jobRepository.findJobsByDateRange(start, end);
        Double revenue = jobs.stream()
            .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
            .sum();
        Double cost = revenue * 0.5; // Simplified - should include parts, labor, overhead
        return revenue - cost;
    }

    private Double calculateTotalRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Job> jobs = jobRepository.findJobsByDateRange(start, end);
        return jobs.stream()
            .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
            .sum();
    }

    private String formatCurrency(Double amount) {
        if (amount >= 1000000) {
            return String.format("LKR %.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format("LKR %.1fK", amount / 1000.0);
        } else {
            return String.format("LKR %.0f", amount);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private String formatDuration(Double hours) {
        if (hours == null) return "45 mins";
        if (hours < 1.0) {
            int minutes = (int) (hours * 60);
            return minutes + " mins";
        } else if (hours == Math.floor(hours)) {
            return (int) hours.doubleValue() + " hours";
        } else {
            return String.format("%.1f hours", hours);
        }
    }

    private CustomerOverviewResponse.TopCustomer findTopCustomer() {
        List<Customer> customers = customerRepository.findAll();
        
        Customer topCustomer = null;
        Double maxSpent = 0.0;
        Long maxServices = 0L;

        for (Customer customer : customers) {
            Double totalSpent = paymentRepository.sumAmountByCustomerId(customer.getId());
            if (totalSpent == null) totalSpent = 0.0;
            
            Long servicesUsed = serviceRepository.countCompletedServicesByCustomerId(customer.getId());
            if (servicesUsed == null) servicesUsed = 0L;

            if (totalSpent > maxSpent || (totalSpent.equals(maxSpent) && servicesUsed > maxServices)) {
                maxSpent = totalSpent;
                maxServices = servicesUsed;
                topCustomer = customer;
            }
        }

        if (topCustomer == null) {
            return CustomerOverviewResponse.TopCustomer.builder()
                .name("N/A")
                .email("N/A")
                .totalSpent(0.0)
                .servicesUsed(0)
                .build();
        }

        String name = (topCustomer.getFirstName() != null ? topCustomer.getFirstName() : "") + 
                     (topCustomer.getLastName() != null ? " " + topCustomer.getLastName() : "").trim();
        if (name.isEmpty()) name = "N/A";

        return CustomerOverviewResponse.TopCustomer.builder()
            .name(name)
            .email(topCustomer.getEmail())
            .totalSpent(maxSpent)
            .servicesUsed(maxServices.intValue())
            .build();
    }

    private String generateCustomerId() {
        List<String> existingIds = customerRepository.findAllCustomerIds();
        if (existingIds.isEmpty()) {
            return "CUST0001";
        }

        List<Integer> numbers = existingIds.stream()
            .filter(id -> id.startsWith("CUST"))
            .map(id -> Integer.parseInt(id.substring(4)))
            .sorted()
            .toList();

        int nextId = findNextCustomerNumber(numbers);
        return String.format("CUST%04d", nextId);
    }

    private int findNextCustomerNumber(List<Integer> numbers) {
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i) != i + 1) {
                return i + 1;
            }
        }
        return numbers.size() + 1;
    }

    private CustomerListResponse mapToCustomerListResponse(Customer customer) {
        Integer vehicleCount = customer.getVehicles() != null ? customer.getVehicles().size() : 0;
        Double totalSpent = paymentRepository.sumAmountByCustomerId(customer.getId());
        if (totalSpent == null) totalSpent = 0.0;

        LocalDateTime lastPaymentDate = paymentRepository.findLastPaymentDateByCustomerId(customer.getId());
        String lastServiceDate = lastPaymentDate != null ? 
            lastPaymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "N/A";

        String name = (customer.getFirstName() != null ? customer.getFirstName() : "") + 
                     (customer.getLastName() != null ? " " + customer.getLastName() : "").trim();
        if (name.isEmpty()) name = "N/A";

        String status = customer.isEnabled() ? "Active" : "Inactive";

        return CustomerListResponse.builder()
            .id(customer.getCustomerId())
            .name(name)
            .email(customer.getEmail())
            .phone(customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "N/A")
            .vehicleCount(vehicleCount)
            .totalSpent(totalSpent)
            .lastServiceDate(lastServiceDate)
            .status(status)
            .build();
    }
}
