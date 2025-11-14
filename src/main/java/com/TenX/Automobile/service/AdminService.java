package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.request.*;
import com.TenX.Automobile.model.dto.response.*;
import com.TenX.Automobile.model.entity.*;
import com.TenX.Automobile.model.enums.Role;
import com.TenX.Automobile.model.enums.JobType;
import com.TenX.Automobile.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final BaseUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationRepository notificationRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private final VehicleRepository vehicleRepository;
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

        // Ongoing Jobs (Services + Projects)
        Long ongoingJobs = jobRepository.findAll().stream()
            .filter(j -> j.getStatus() == null || !"COMPLETED".equals(j.getStatus()))
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
                .value(ongoingJobs.intValue())
                .status("In Progress")
                .build())
            .activeEmployees(DashboardStatsResponse.ActiveEmployees.builder()
                .value(activeEmployees.intValue())
                .onLeave(onLeave.intValue())
                .frozen(frozen.intValue())
                .build())
            .build();
    }

    /**
     * Get comprehensive dashboard statistics for admin overview
     */
    public AdminDashboardStatsResponse getComprehensiveDashboardStats() {
        log.info("Fetching comprehensive dashboard statistics");
        
        // KPIs
        AdminDashboardStatsResponse.DashboardKPIs kpis = calculateDashboardKPIs();
        
        // Monthly Profit Trends (last 6 months)
        AdminDashboardStatsResponse.MonthlyProfitTrend profitTrend = calculateMonthlyProfitTrend();
        
        // Job & Project Completion Status
        AdminDashboardStatsResponse.JobProjectCompletion jobProjectCompletion = calculateJobProjectCompletion();
        
        // Service Category Distribution
        AdminDashboardStatsResponse.ServiceCategoryDistribution serviceCategoryDistribution = calculateServiceCategoryDistribution();
        
        // Top 5 Employees by Hours Worked
        List<AdminDashboardStatsResponse.TopEmployeeByHours> topEmployees = calculateTopEmployeesByHours();
        
        // Business Alerts
        List<AdminDashboardStatsResponse.BusinessAlert> alerts = calculateBusinessAlerts();
        
        return AdminDashboardStatsResponse.builder()
            .kpis(kpis)
            .profitTrend(profitTrend)
            .jobProjectCompletion(jobProjectCompletion)
            .serviceCategoryDistribution(serviceCategoryDistribution)
            .topEmployees(topEmployees)
            .alerts(alerts)
            .build();
    }

    private AdminDashboardStatsResponse.DashboardKPIs calculateDashboardKPIs() {
        // Total Customers (enabled users with CUSTOMER role)
        Integer totalCustomers = (int) customerRepository.findAll().stream()
            .filter(c -> c.isEnabled())
            .count();
        
        // Total Employees and Managers
        Integer totalEmployees = (int) employeeRepository.findAll().stream()
            .filter(e -> e.getRoles().contains(Role.STAFF) && e.isEnabled())
            .count();
        
        Integer totalManagers = (int) employeeRepository.findAll().stream()
            .filter(e -> e.getRoles().contains(Role.MANAGER) && e.isEnabled())
            .count();
        
        // Ongoing Jobs and Projects (not completed)
        Integer ongoingJobs = (int) jobRepository.findAll().stream()
            .filter(j -> j.getStatus() != null && 
                !j.getStatus().equalsIgnoreCase("COMPLETED") && 
                JobType.SERVICE.equals(j.getType()))
            .count();
        
        Integer ongoingProjects = (int) jobRepository.findAll().stream()
            .filter(j -> j.getStatus() != null && 
                !j.getStatus().equalsIgnoreCase("COMPLETED") && 
                JobType.PROJECT.equals(j.getType()))
            .count();
        
        // Monthly Revenue (current month)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        Double monthlyRevenue = paymentRepository.findAll().stream()
            .filter(p -> p.getCreatedAt() != null && 
                p.getCreatedAt().isAfter(startOfMonth) && 
                p.getCreatedAt().isBefore(now))
            .mapToDouble(Payment::getP_Amount)
            .sum();
        
        // Completed Services (all time)
        Integer completedServices = (int) jobRepository.findAll().stream()
            .filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase("COMPLETED"))
            .count();
        
        return AdminDashboardStatsResponse.DashboardKPIs.builder()
            .totalCustomers(totalCustomers)
            .totalEmployees(totalEmployees)
            .totalManagers(totalManagers)
            .ongoingJobs(ongoingJobs)
            .ongoingProjects(ongoingProjects)
            .monthlyRevenue(monthlyRevenue)
            .completedServices(completedServices)
            .build();
    }

    private AdminDashboardStatsResponse.MonthlyProfitTrend calculateMonthlyProfitTrend() {
        List<String> labels = new ArrayList<>();
        List<Double> revenue = new ArrayList<>();
        List<Double> cost = new ArrayList<>();
        List<Double> profit = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        // Group payments by month
        Map<String, Double> monthlyRevenue = paymentRepository.findAll().stream()
            .filter(p -> p.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                p -> p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.summingDouble(Payment::getP_Amount)
            ));
        
        // Group jobs by month
        Map<String, Double> monthlyCost = jobRepository.findAll().stream()
            .filter(j -> j.getCreatedAt() != null && j.getCost() != null)
            .collect(Collectors.groupingBy(
                j -> j.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.summingDouble(j -> j.getCost().doubleValue())
            ));
        
        // Get all unique months and sort them
        Set<String> allMonths = new java.util.TreeSet<>();
        allMonths.addAll(monthlyRevenue.keySet());
        allMonths.addAll(monthlyCost.keySet());
        
        // If we have data, take the last 6 months of actual data
        // If no data, show last 6 calendar months with zeros
        List<String> monthsToShow = new ArrayList<>(allMonths);
        if (monthsToShow.isEmpty()) {
            // No data - show last 6 months with zeros
            for (int i = 5; i >= 0; i--) {
                LocalDateTime monthStart = now.minusMonths(i);
                String monthKey = monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                monthsToShow.add(monthKey);
            }
        } else {
            // Has data - take last 6 months of data
            int startIndex = Math.max(0, monthsToShow.size() - 6);
            monthsToShow = monthsToShow.subList(startIndex, monthsToShow.size());
        }
        
        // Build the response
        for (String monthKey : monthsToShow) {
            // Format label as "MMM yyyy"
            try {
                java.time.YearMonth ym = java.time.YearMonth.parse(monthKey);
                labels.add(ym.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            } catch (Exception e) {
                labels.add(monthKey);
            }
            
            Double monthRevenue = monthlyRevenue.getOrDefault(monthKey, 0.0);
            Double monthCost = monthlyCost.getOrDefault(monthKey, 0.0);
            
            revenue.add(monthRevenue);
            cost.add(monthCost);
            profit.add(monthRevenue - monthCost);
        }
        
        return AdminDashboardStatsResponse.MonthlyProfitTrend.builder()
            .labels(labels)
            .revenue(revenue)
            .cost(cost)
            .profit(profit)
            .build();
    }

    private AdminDashboardStatsResponse.JobProjectCompletion calculateJobProjectCompletion() {
        List<Job> allJobs = jobRepository.findAll();
        
        // Count jobs by status
        int jobsCompleted = 0, jobsInProgress = 0, jobsOnHold = 0, jobsPending = 0;
        int projectsCompleted = 0, projectsInProgress = 0, projectsOnHold = 0, projectsPending = 0;
        
        for (Job job : allJobs) {
            String status = job.getStatus() != null ? job.getStatus().toUpperCase() : "PENDING";
            boolean isService = JobType.SERVICE.equals(job.getType());
            
            if (status.contains("COMPLETED")) {
                if (isService) jobsCompleted++; else projectsCompleted++;
            } else if (status.contains("IN_PROGRESS") || status.contains("ONGOING")) {
                if (isService) jobsInProgress++; else projectsInProgress++;
            } else if (status.contains("HOLD") || status.contains("PAUSED")) {
                if (isService) jobsOnHold++; else projectsOnHold++;
            } else {
                if (isService) jobsPending++; else projectsPending++;
            }
        }
        
        return AdminDashboardStatsResponse.JobProjectCompletion.builder()
            .jobs(AdminDashboardStatsResponse.StatusCounts.builder()
                .completed(jobsCompleted)
                .in_progress(jobsInProgress)
                .on_hold(jobsOnHold)
                .pending(jobsPending)
                .build())
            .projects(AdminDashboardStatsResponse.StatusCounts.builder()
                .completed(projectsCompleted)
                .in_progress(projectsInProgress)
                .on_hold(projectsOnHold)
                .pending(projectsPending)
                .build())
            .build();
    }

    private AdminDashboardStatsResponse.ServiceCategoryDistribution calculateServiceCategoryDistribution() {
        var allServices = serviceRepository.findAll();
        
        // Group by category and count
        Map<String, Long> categoryCounts = new HashMap<>();
        for (var service : allServices) {
            String category = service.getCategory() != null ? service.getCategory() : "Other";
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0L) + 1);
        }
        
        List<String> labels = new ArrayList<>(categoryCounts.keySet());
        List<Integer> data = categoryCounts.values().stream()
            .map(Long::intValue)
            .collect(Collectors.toList());
        
        return AdminDashboardStatsResponse.ServiceCategoryDistribution.builder()
            .labels(labels)
            .data(data)
            .build();
    }

    private List<AdminDashboardStatsResponse.TopEmployeeByHours> calculateTopEmployeesByHours() {
        List<TimeLog> allTimeLogs = timeLogRepository.findAll();
        
        // Group by employee and sum hours
        Map<Employee, Double> employeeHours = allTimeLogs.stream()
            .filter(tl -> tl.getHoursWorked() != null && tl.getEmployee() != null)
            .collect(Collectors.groupingBy(
                TimeLog::getEmployee,
                Collectors.summingDouble(TimeLog::getHoursWorked)
            ));
        
        // Sort by hours descending and take top 5
        return employeeHours.entrySet().stream()
            .sorted(Map.Entry.<Employee, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Employee emp = entry.getKey();
                return AdminDashboardStatsResponse.TopEmployeeByHours.builder()
                    .employeeId(emp.getId() != null ? emp.getId().getMostSignificantBits() : 0L)
                    .name(emp.getFirstName() + " " + emp.getLastName())
                    .specialty(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                    .totalHours(entry.getValue().intValue())
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<AdminDashboardStatsResponse.BusinessAlert> calculateBusinessAlerts() {
        List<AdminDashboardStatsResponse.BusinessAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Check for overdue jobs
        List<Job> overdueJobs = jobRepository.findAll().stream()
            .filter(j -> j.getArrivingDate() != null && 
                j.getArrivingDate().isBefore(now) && 
                !j.getStatus().equalsIgnoreCase("COMPLETED"))
            .collect(Collectors.toList());
        
        for (Job job : overdueJobs.stream().limit(3).collect(Collectors.toList())) {
            alerts.add(AdminDashboardStatsResponse.BusinessAlert.builder()
                .id((long) alerts.size() + 1)
                .type("overdue_job")
                .message("Job #" + job.getJobId() + " is overdue by " + 
                    java.time.Duration.between(job.getArrivingDate(), now).toDays() + " days")
                .severity("high")
                .createdAt(now.toString())
                .isRead(false)
                .relatedId(job.getJobId())
                .build());
        }
        
        // Check for delayed projects
        List<Project> delayedProjects = projectRepository.findAll().stream()
            .filter(p -> p.getStatus() != null && 
                (p.getStatus().contains("DELAYED") || p.getStatus().contains("HOLD")))
            .collect(Collectors.toList());
        
        for (Project project : delayedProjects.stream().limit(2).collect(Collectors.toList())) {
            alerts.add(AdminDashboardStatsResponse.BusinessAlert.builder()
                .id((long) alerts.size() + 1)
                .type("delayed_project")
                .message("Project '" + project.getTitle() + "' is experiencing delays")
                .severity("medium")
                .createdAt(now.toString())
                .isRead(false)
                .relatedId(project.getProjectId())
                .build());
        }
        
        // Check for payment errors (jobs completed but not paid)
        List<Job> completedJobs = jobRepository.findAll().stream()
            .filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase("COMPLETED"))
            .collect(Collectors.toList());
        
        for (Job job : completedJobs) {
            boolean hasPaid = paymentRepository.findAll().stream()
                .anyMatch(p -> p.getJob() != null && p.getJob().getJobId().equals(job.getJobId()));
            
            if (!hasPaid) {
                alerts.add(AdminDashboardStatsResponse.BusinessAlert.builder()
                    .id((long) alerts.size() + 1)
                    .type("payment_error")
                    .message("Job #" + job.getJobId() + " completed but payment not recorded")
                    .severity("high")
                    .createdAt(now.toString())
                    .isRead(false)
                    .relatedId(job.getJobId())
                    .build());
                
                if (alerts.stream().filter(a -> a.getType().equals("payment_error")).count() >= 2) {
                    break;
                }
            }
        }
        
        return alerts;
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

        // Check for jobs paused due to parts delay
        Long pausedJobs = jobRepository.findAll().stream()
            .filter(j -> "WAITING_FOR_PARTS".equals(j.getStatus()) || "PAUSED".equals(j.getStatus()))
            .count();
        if (pausedJobs > 0) {
            alerts.add(SystemAlertResponse.builder()
                .id("alert-" + UUID.randomUUID().toString().substring(0, 8))
                .type("warning")
                .message(pausedJobs + " jobs paused due to part delay")
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
                .filter(j -> JobType.SERVICE.equals(j.getType()))
                .collect(Collectors.toList());
        } else if ("custom".equals(serviceFilter)) {
            filteredJobs = jobs.stream()
                .filter(j -> JobType.PROJECT.equals(j.getType()))
                .collect(Collectors.toList());
        }

        // Group by service/project title
        Map<String, List<Job>> jobsByType = filteredJobs.stream()
            .collect(Collectors.groupingBy(job -> {
                if (JobType.SERVICE.equals(job.getType())) {
                    return serviceRepository.findById(job.getTypeId())
                        .map(com.TenX.Automobile.model.entity.Service::getTitle)
                        .orElse("Unknown Service");
                } else if (JobType.PROJECT.equals(job.getType())) {
                    return projectRepository.findById(job.getTypeId())
                        .map(Project::getTitle)
                        .orElse("Unknown Project");
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
                    String jobTitle = null;
                    if (JobType.SERVICE.equals(j.getType())) {
                        jobTitle = serviceRepository.findById(j.getTypeId())
                            .map(com.TenX.Automobile.model.entity.Service::getTitle)
                            .orElse("Unknown Service");
                    } else if (JobType.PROJECT.equals(j.getType())) {
                        jobTitle = projectRepository.findById(j.getTypeId())
                            .map(Project::getTitle)
                            .orElse("Unknown Project");
                    }
                    return serviceType.equals(jobTitle != null ? jobTitle : "Other");
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

        // Calculate monthly trend
        FinancialReportResponse.MonthlyTrend monthlyTrend = calculateMonthlyTrend(
            startDateTime, endDateTime, serviceFilter);

        // Calculate revenue distribution
        List<FinancialReportResponse.RevenueDistribution> revenueDistribution = breakdown.stream()
            .map(b -> FinancialReportResponse.RevenueDistribution.builder()
                .name(b.getServiceType())
                .value(b.getRevenue())
                .build())
            .collect(Collectors.toList());

        // Calculate cost analysis
        List<FinancialReportResponse.CostAnalysis> costAnalysis = breakdown.stream()
            .map(b -> FinancialReportResponse.CostAnalysis.builder()
                .category(b.getServiceType())
                .amount(b.getCost())
                .build())
            .collect(Collectors.toList());

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
            .monthlyTrend(monthlyTrend)
            .revenueDistribution(revenueDistribution)
            .costAnalysis(costAnalysis)
            .build();
    }

    private FinancialReportResponse.MonthlyTrend calculateMonthlyTrend(
            LocalDateTime startDateTime, LocalDateTime endDateTime, String serviceFilter) {
        
        // Group jobs by month
        List<Job> jobs = jobRepository.findJobsByDateRange(startDateTime, endDateTime);
        
        // Filter by service type if needed
        if ("predefined".equals(serviceFilter)) {
            jobs = jobs.stream()
                .filter(j -> JobType.SERVICE.equals(j.getType()))
                .collect(Collectors.toList());
        } else if ("custom".equals(serviceFilter)) {
            jobs = jobs.stream()
                .filter(j -> JobType.PROJECT.equals(j.getType()))
                .collect(Collectors.toList());
        }

        // Group by month
        Map<String, List<Job>> jobsByMonth = jobs.stream()
            .filter(j -> j.getCreatedAt() != null)
            .collect(Collectors.groupingBy(job -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                return job.getCreatedAt().format(formatter);
            }));

        // Get sorted months
        List<String> months = new ArrayList<>(jobsByMonth.keySet());
        Collections.sort(months);

        // Calculate revenue, cost, profit per month
        List<String> labels = new ArrayList<>();
        List<Double> revenueList = new ArrayList<>();
        List<Double> costList = new ArrayList<>();
        List<Double> profitList = new ArrayList<>();

        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        for (String month : months) {
            List<Job> monthJobs = jobsByMonth.get(month);
            
            Double revenue = monthJobs.stream()
                .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
                .sum();
            
            Double cost = revenue * 0.5; // Simplified cost calculation
            Double profit = revenue - cost;

            // Format label as "Jan 2025"
            LocalDate monthDate = LocalDate.parse(month + "-01");
            labels.add(monthDate.format(labelFormatter));
            revenueList.add(revenue);
            costList.add(cost);
            profitList.add(profit);
        }

        return FinancialReportResponse.MonthlyTrend.builder()
            .labels(labels)
            .revenue(revenueList)
            .cost(costList)
            .profit(profitList)
            .build();
    }

    // ==================== PAGE 3: WORKFORCE OVERVIEW ====================

    public WorkforceOverviewResponse getWorkforceOverview() {
        // Get all employees (both STAFF and MANAGER)
        List<Employee> allStaff = employeeRepository.findByRole(Role.STAFF);
        List<Employee> allManagers = employeeRepository.findByRole(Role.MANAGER);
        
        // Calculate total employees (STAFF + MANAGER)
        Integer totalEmployees = allStaff.size() + allManagers.size();
        Integer activeEmployees = (int) allStaff.stream().filter(Employee::isEnabled).count() + 
                                  (int) allManagers.stream().filter(Employee::isEnabled).count();
        Integer deactivatedEmployees = totalEmployees - activeEmployees;

        // Calculate average rating (mock - should come from customer ratings)
        Double avgRating = 4.5 + (Math.random() * 0.5);
        Double ratingChange = 0.2;

        // Calculate average workload
        Double avgWorkload = allStaff.stream()
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
        for (Employee emp : allStaff) {
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
                .onLeave(0) // Not used - keeping for backward compatibility
                .frozen(deactivatedEmployees)
                .avgRating(Math.round(avgRating * 10.0) / 10.0)
                .ratingChange(ratingChange)
                .avgWorkload(Math.round(avgWorkload * 10.0) / 10.0)
                .avgSalary(avgSalary)
                .build())
            .centerInfo(WorkforceOverviewResponse.CenterInfo.builder()
                .totalCenters(1)
                .activeManagers(allManagers.size())
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
                .status(manager.isEnabled() ? "Active" : "Deactivated")
                .build())
            .collect(Collectors.toList());
    }

    public List<EmployeeDetailResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        
        return employees.stream()
            .map(emp -> {
                Double rating = 4.5 + (Math.random() * 0.5);
                String status = emp.isEnabled() ? "Active" : "Deactivated";
                
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

    public Map<String, Object> updateManager(String managerId, UpdateManagerRequest request) {
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

    public Map<String, Object> updateEmployee(String employeeId, UpdateEmployeeRequest request) {
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

    public Map<String, Object> activateManager(String managerId) {
        Employee manager = employeeRepository.findByEmployeeId(managerId)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (!manager.getRoles().contains(Role.MANAGER)) {
            throw new RuntimeException("Employee is not a manager");
        }

        manager.setEnabled(true);
        employeeRepository.save(manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Manager " + managerId + " has been activated and can now access the system.");
        return response;
    }

    // ==================== PAGE 4: SERVICES ANALYTICS ====================

    public ServicesAnalyticsResponse getServicesAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        // Most Profitable Service
        ServicesAnalyticsResponse.MostProfitableService mostProfitable = getMostProfitableService();

        // Total Services - count completed SERVICE type jobs this month
        Long totalServicesMonth = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfMonth))
            .count();
        Long totalServicesLastMonth = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfLastMonth) &&
                j.getUpdatedAt().isBefore(startOfMonth))
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

        // Get completed SERVICE type jobs this month
        List<Job> serviceJobs = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfMonth) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()))
            .collect(Collectors.toList());

        Map<String, Double> profitByType = new HashMap<>();
        Map<String, Double> revenueByType = new HashMap<>();

        for (Job job : serviceJobs) {
            // Look up the actual service details using typeId
            serviceRepository.findById(job.getTypeId()).ifPresent(service -> {
                String type = service.getTitle() != null ? service.getTitle() : "Service";
                Double revenue = job.getCost() != null ? job.getCost().doubleValue() : 0.0;
                Double cost = revenue * 0.5; // Simplified
                Double profit = revenue - cost;

                profitByType.put(type, profitByType.getOrDefault(type, 0.0) + profit);
                revenueByType.put(type, revenueByType.getOrDefault(type, 0.0) + revenue);
            });
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

        Long totalServicesMonth = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfMonth))
            .count();
        Long totalServicesLastMonth = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfLastMonth) &&
                j.getUpdatedAt().isBefore(startOfMonth))
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

        // Get completed SERVICE type jobs this month
        List<Job> serviceJobs = jobRepository.findAll().stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                j.getUpdatedAt() != null && j.getUpdatedAt().isAfter(startOfMonth) &&
                j.getStatus() != null && "COMPLETED".equals(j.getStatus()))
            .collect(Collectors.toList());

        // Group jobs by service title
        Map<String, List<Job>> jobsByServiceType = new HashMap<>();
        for (Job job : serviceJobs) {
            serviceRepository.findById(job.getTypeId()).ifPresent(service -> {
                String title = service.getTitle() != null ? service.getTitle() : "Service";
                jobsByServiceType.computeIfAbsent(title, k -> new ArrayList<>()).add(job);
            });
        }

        List<ServicesAnalyticsResponse.ServicePerformance> performance = new ArrayList<>();
        int idCounter = 1;

        for (Map.Entry<String, List<Job>> entry : jobsByServiceType.entrySet()) {
            String serviceName = entry.getKey();
            List<Job> typeJobs = entry.getValue();

            Integer totalBookings = typeJobs.size();
            
            // Average duration - get from service templates
            Double avgDurationMinutes = 45.0;
            if (!typeJobs.isEmpty()) {
                Long firstServiceId = typeJobs.get(0).getTypeId();
                avgDurationMinutes = serviceRepository.findById(firstServiceId)
                    .map(s -> s.getEstimatedHours() != null ? s.getEstimatedHours() * 60 : 45.0)
                    .orElse(45.0);
            }
            String avgDuration = formatDuration(avgDurationMinutes);

            // Average profit per service
            Double avgProfit = typeJobs.stream()
                .mapToDouble(j -> {
                    Double revenue = j.getCost() != null ? j.getCost().doubleValue() : 0.0;
                    Double cost = revenue * 0.5;
                    return revenue - cost;
                })
                .average()
                .orElse(4500.0);

            // Customer rating (mock)
            Double customerRating = 4.5 + (Math.random() * 0.5);

            // Trend calculation - compare with last month
            Long prevMonthCount = jobRepository.findAll().stream()
                .filter(j -> {
                    if (!JobType.SERVICE.equals(j.getType())) return false;
                    if (j.getStatus() == null || !"COMPLETED".equals(j.getStatus())) return false;
                    if (j.getUpdatedAt() == null) return false;
                    if (!j.getUpdatedAt().isAfter(startOfLastMonth) || !j.getUpdatedAt().isBefore(startOfMonth)) return false;
                    
                    // Check if service title matches
                    return serviceRepository.findById(j.getTypeId())
                        .map(s -> serviceName.equals(s.getTitle()))
                        .orElse(false);
                })
                .count();
            
            Double trend = prevMonthCount > 0 ? ((totalBookings - prevMonthCount.doubleValue()) / prevMonthCount) * 100 : 15.0;

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

    /**
     * Get all services with details
     */
    public List<ServiceInfoResponse> getAllServices() {
        List<com.TenX.Automobile.model.entity.Service> services = serviceRepository.findAll();
        
        return services.stream()
            .map(service -> {
                // Count total bookings for this service
                Long totalBookings = jobRepository.findAll().stream()
                    .filter(job -> JobType.SERVICE.equals(job.getType()) 
                        && service.getServiceId().equals(job.getTypeId()))
                    .count();
                
                return ServiceInfoResponse.builder()
                    .serviceId(service.getServiceId())
                    .title(service.getTitle())
                    .description(service.getDescription())
                    .category(service.getCategory())
                    .cost(service.getCost())
                    .estimatedHours(service.getEstimatedHours())
                    .imageUrl(service.getImageUrl())
                    .totalBookings(totalBookings.intValue())
                    .createdAt(service.getCreatedAt())
                    .updatedAt(service.getUpdatedAt())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Create a new predefined service
     */
    public Map<String, Object> createService(CreateServiceRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        com.TenX.Automobile.model.entity.Service service = new com.TenX.Automobile.model.entity.Service();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setCost(request.getCost());
        service.setEstimatedHours(request.getEstimatedHours());
        service.setImageUrl(request.getImageUrl());
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        
        com.TenX.Automobile.model.entity.Service savedService = serviceRepository.save(service);
        
        response.put("success", true);
        response.put("message", "Service created successfully");
        response.put("serviceId", savedService.getServiceId());
        response.put("serviceName", savedService.getTitle());
        
        return response;
    }

    /**
     * Update a predefined service
     */
    public Map<String, Object> updateService(Long serviceId, UpdateServiceRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        com.TenX.Automobile.model.entity.Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));
        
        // Update fields
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setCategory(request.getCategory());
        service.setCost(request.getCost());
        service.setEstimatedHours(request.getEstimatedHours());
        service.setImageUrl(request.getImageUrl());
        service.setUpdatedAt(LocalDateTime.now());
        
        serviceRepository.save(service);
        
        response.put("success", true);
        response.put("message", "Service updated successfully");
        response.put("serviceId", serviceId);
        response.put("serviceName", request.getTitle());
        
        return response;
    }

    /**
     * Delete a predefined service
     */
    public Map<String, Object> deleteService(Long serviceId) {
        Map<String, Object> response = new HashMap<>();
        
        com.TenX.Automobile.model.entity.Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found with ID: " + serviceId));
        
        // Check if service is being used in any jobs
        boolean isUsed = jobRepository.findAll().stream()
            .anyMatch(job -> JobType.SERVICE.equals(job.getType()) 
                && serviceId.equals(job.getTypeId()));
        
        if (isUsed) {
            response.put("success", false);
            response.put("message", "Cannot delete service - it is currently being used in jobs");
            return response;
        }
        
        String serviceName = service.getTitle();
        serviceRepository.delete(service);
        
        response.put("success", true);
        response.put("message", "Service deleted successfully");
        response.put("serviceId", serviceId);
        response.put("serviceName", serviceName);
        
        return response;
    }

    /**
     * Get detailed service analytics with all charts
     */
    public ServiceAnalyticsDetailedResponse getDetailedServiceAnalytics() {
        return ServiceAnalyticsDetailedResponse.builder()
            .popularServices(calculatePopularServices())
            .averageCost(calculateAverageCost())
            .averageDuration(calculateAverageDuration())
            .categoryPerformance(calculateCategoryPerformance())
            .brandAnalytics(calculateBrandAnalytics())
            .jobTimeliness(calculateJobTimeliness())
            .taskDelays(calculateTaskDelays())
            .projectAnalytics(calculateProjectAnalytics())
            .serviceSummary(calculateServiceSummary())
            .build();
    }

    // Service Summary
    private ServiceAnalyticsDetailedResponse.ServiceSummary calculateServiceSummary() {
        List<Job> serviceJobs = jobRepository.findAll().stream()
            .filter(job -> JobType.SERVICE.equals(job.getType()))
            .collect(Collectors.toList());

        int totalServices = serviceJobs.size();
        int completed = (int) serviceJobs.stream()
            .filter(j -> "COMPLETED".equals(j.getStatus()))
            .count();
        int inProgress = (int) serviceJobs.stream()
            .filter(j -> "IN_PROGRESS".equals(j.getStatus()))
            .count();
        int waitingParts = (int) serviceJobs.stream()
            .filter(j -> "WAITING_PARTS".equals(j.getStatus()))
            .count();
        int scheduled = (int) serviceJobs.stream()
            .filter(j -> "SCHEDULED".equals(j.getStatus()))
            .count();
        int cancelled = (int) serviceJobs.stream()
            .filter(j -> "CANCELLED".equals(j.getStatus()))
            .count();

        Double avgCost = serviceJobs.stream()
            .filter(j -> "COMPLETED".equals(j.getStatus()) && j.getCost() != null)
            .mapToDouble(j -> j.getCost().doubleValue())
            .average()
            .orElse(0.0);

        return ServiceAnalyticsDetailedResponse.ServiceSummary.builder()
            .totalServices(totalServices)
            .completed(completed)
            .inProgress(inProgress)
            .waitingParts(waitingParts)
            .scheduled(scheduled)
            .cancelled(cancelled)
            .averageCost(Math.round(avgCost * 100.0) / 100.0)
            .build();
    }

    // 1. Popular Services Chart
    private ServiceAnalyticsDetailedResponse.PopularServices calculatePopularServices() {
        Map<String, Long> serviceCounts = jobRepository.findAll().stream()
            .filter(job -> JobType.SERVICE.equals(job.getType()))
            .collect(Collectors.groupingBy(
                job -> serviceRepository.findById(job.getTypeId())
                    .map(com.TenX.Automobile.model.entity.Service::getTitle)
                    .orElse("Unknown"),
                Collectors.counting()
            ));

        List<Map.Entry<String, Long>> sortedEntries = serviceCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());

        return ServiceAnalyticsDetailedResponse.PopularServices.builder()
            .labels(sortedEntries.stream().map(Map.Entry::getKey).collect(Collectors.toList()))
            .data(sortedEntries.stream().map(e -> e.getValue().intValue()).collect(Collectors.toList()))
            .build();
    }

    // 2. Average Job Cost
    private ServiceAnalyticsDetailedResponse.AverageCost calculateAverageCost() {
        Map<String, List<Job>> jobsByCategory = jobRepository.findAll().stream()
            .filter(job -> JobType.SERVICE.equals(job.getType()) 
                && "COMPLETED".equals(job.getStatus()))
            .collect(Collectors.groupingBy(job -> 
                serviceRepository.findById(job.getTypeId())
                    .map(com.TenX.Automobile.model.entity.Service::getCategory)
                    .orElse("Other")
            ));

        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        jobsByCategory.forEach((category, jobs) -> {
            labels.add(category);
            double avgCost = jobs.stream()
                .mapToDouble(j -> j.getCost() != null ? j.getCost().doubleValue() : 0.0)
                .average()
                .orElse(0.0);
            data.add(Math.round(avgCost * 100.0) / 100.0);
        });

        return ServiceAnalyticsDetailedResponse.AverageCost.builder()
            .labels(labels)
            .data(data)
            .build();
    }

    // 3. Average Service Duration
    private ServiceAnalyticsDetailedResponse.AverageDuration calculateAverageDuration() {
        // Services average
        Double servicesAvg = serviceRepository.findAll().stream()
            .filter(s -> s.getEstimatedHours() != null)
            .mapToDouble(com.TenX.Automobile.model.entity.Service::getEstimatedHours)
            .average()
            .orElse(1.5);

        // Projects average
        Double projectsAvg = projectRepository.findAll().stream()
            .mapToDouble(p -> p.getEstimatedHours() != null ? p.getEstimatedHours() : 0.0)
            .average()
            .orElse(5.0);

        return ServiceAnalyticsDetailedResponse.AverageDuration.builder()
            .labels(Arrays.asList("Services", "Projects"))
            .data(Arrays.asList(
                Math.round(servicesAvg * 10.0) / 10.0,
                Math.round(projectsAvg * 10.0) / 10.0
            ))
            .build();
    }

    // 4. Category Performance
    private ServiceAnalyticsDetailedResponse.CategoryPerformance calculateCategoryPerformance() {
        Map<String, List<Job>> jobsByCategory = jobRepository.findAll().stream()
            .filter(job -> JobType.SERVICE.equals(job.getType()))
            .collect(Collectors.groupingBy(job -> 
                serviceRepository.findById(job.getTypeId())
                    .map(com.TenX.Automobile.model.entity.Service::getCategory)
                    .orElse("Other")
            ));

        List<String> labels = new ArrayList<>();
        List<Integer> jobs = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        jobsByCategory.forEach((category, categoryJobs) -> {
            labels.add(category);
            jobs.add(categoryJobs.size());
            
            // Count delays (jobs with status containing HOLD or DELAYED)
            int delayCount = (int) categoryJobs.stream()
                .filter(j -> j.getStatus() != null && 
                    (j.getStatus().contains("HOLD") || j.getStatus().contains("DELAYED")))
                .count();
            delays.add(delayCount);
        });

        return ServiceAnalyticsDetailedResponse.CategoryPerformance.builder()
            .labels(labels)
            .jobs(jobs)
            .delays(delays)
            .build();
    }

    // 5. Brand Analytics
    private ServiceAnalyticsDetailedResponse.BrandAnalytics calculateBrandAnalytics() {
        Map<String, Long> brandCounts = vehicleRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                v -> v.getBrandName() != null ? v.getBrandName() : "Unknown",
                Collectors.counting()
            ));

        List<Map.Entry<String, Long>> sortedBrands = brandCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());

        return ServiceAnalyticsDetailedResponse.BrandAnalytics.builder()
            .labels(sortedBrands.stream().map(Map.Entry::getKey).collect(Collectors.toList()))
            .data(sortedBrands.stream().map(e -> e.getValue().intValue()).collect(Collectors.toList()))
            .build();
    }

    // 6. Job Timeliness
    private ServiceAnalyticsDetailedResponse.JobTimeliness calculateJobTimeliness() {
        List<Job> completedJobs = jobRepository.findAll().stream()
            .filter(j -> "COMPLETED".equals(j.getStatus()))
            .collect(Collectors.toList());

        int onTime = 0;
        int delayed = 0;

        for (Job job : completedJobs) {
            // Consider a job delayed if updated time is significantly after arriving time
            if (job.getUpdatedAt() != null && job.getArrivingDate() != null) {
                long daysBetween = java.time.Duration.between(job.getArrivingDate(), job.getUpdatedAt()).toDays();
                if (daysBetween > 7) { // More than 7 days is considered delayed
                    delayed++;
                } else {
                    onTime++;
                }
            }
        }

        return ServiceAnalyticsDetailedResponse.JobTimeliness.builder()
            .labels(Arrays.asList("On-Time", "Delayed"))
            .data(Arrays.asList(onTime, delayed))
            .build();
    }

    // 7. Task Delays
    private ServiceAnalyticsDetailedResponse.TaskDelays calculateTaskDelays() {
        List<Task> delayedTasks = taskRepository.findAll().stream()
            .filter(t -> t.getStatus() != null && 
                (t.getStatus().contains("HOLD") || t.getStatus().contains("WAITING")))
            .collect(Collectors.toList());

        return ServiceAnalyticsDetailedResponse.TaskDelays.builder()
            .totalDelayed(delayedTasks.size())
            .breakdown(new ArrayList<>()) // Can add employee breakdown if needed
            .build();
    }

    // 8. Project Analytics
    private ServiceAnalyticsDetailedResponse.ProjectAnalytics calculateProjectAnalytics() {
        List<Project> projects = projectRepository.findAll();
        
        List<String> labels = new ArrayList<>();
        List<Double> estimated = new ArrayList<>();
        List<Double> actual = new ArrayList<>();

        projects.stream().limit(10).forEach(project -> {
            labels.add(project.getTitle() != null ? project.getTitle() : "Project " + project.getProjectId());
            estimated.add(project.getEstimatedHours() != null ? project.getEstimatedHours() : 0.0);
            
            // Calculate actual hours from tasks
            Double actualHours = taskRepository.findAll().stream()
                .filter(t -> project.getProjectId().equals(t.getProject() != null ? t.getProject().getProjectId() : null))
                .mapToDouble(t -> t.getEstimatedHours() != null ? t.getEstimatedHours() : 0.0)
                .sum();
            actual.add(actualHours);
        });

        // Summary
        int totalProjects = projects.size();
        int pending = (int) projects.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        int approved = (int) projects.stream().filter(p -> "APPROVED".equals(p.getStatus())).count();
        int completed = (int) projects.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        int inProgress = (int) projects.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        int waitingParts = (int) projects.stream().filter(p -> "WAITING_PARTS".equals(p.getStatus())).count();
        int scheduled = (int) projects.stream().filter(p -> "SCHEDULED".equals(p.getStatus())).count();
        int cancelled = (int) projects.stream().filter(p -> "CANCELLED".equals(p.getStatus())).count();
        Double avgCost = projects.stream()
            .mapToDouble(p -> p.getCost() != null ? p.getCost() : 0.0)
            .average()
            .orElse(0.0);

        ServiceAnalyticsDetailedResponse.ProjectSummary summary = 
            ServiceAnalyticsDetailedResponse.ProjectSummary.builder()
                .totalProjects(totalProjects)
                .pending(pending)
                .approved(approved)
                .completed(completed)
                .inProgress(inProgress)
                .waitingParts(waitingParts)
                .scheduled(scheduled)
                .cancelled(cancelled)
                .averageCost(Math.round(avgCost * 100.0) / 100.0)
                .build();

        return ServiceAnalyticsDetailedResponse.ProjectAnalytics.builder()
            .labels(labels)
            .estimated(estimated)
            .actual(actual)
            .summary(summary)
            .build();
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
        List<com.TenX.Automobile.model.entity.Service> services = serviceRepository.findAll();
        
        return services.stream()
            .map(service -> SettingsServicesResponse.builder()
                .serviceId("srv_" + service.getServiceId())
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
        
        // Use the helper method to delete all related user data
        deleteUserRelatedData(customer, "customer", customerId);
        
        // Finally, delete the customer (vehicles and jobs will cascade due to CascadeType.ALL)
        customerRepository.delete(customer);
        log.info("Successfully deleted customer: {}", customerId);
    }

    /**
     * Helper method to delete all related data for any user entity
     * Prevents foreign key constraint violations when deleting users
     * 
     * @param user The user entity to delete related data for
     * @param userType Type of user (for logging purposes)
     * @param userId User identifier (for logging purposes)
     */
    private void deleteUserRelatedData(UserEntity user, String userType, String userId) {
        // 1. Delete refresh tokens
        refreshTokenRepository.deleteByUser(user);
        log.info("Deleted refresh tokens for {}: {}", userType, userId);
        
        // 2. Delete notifications
        notificationRepository.deleteByUser(user);
        log.info("Deleted notifications for {}: {}", userType, userId);
        
        // 3. Delete messages sent by this user
        messageRepository.deleteBySender(user);
        log.info("Deleted messages for {}: {}", userType, userId);
        
        // 4. Delete conversations where user is participant or employee
        conversationRepository.deleteByParticipant(user);
        conversationRepository.deleteByEmployee(user);
        log.info("Deleted conversations for {}: {}", userType, userId);
        
        // 5. If user is an employee, delete time logs and job assignments
        if (user instanceof Employee) {
            Employee employee = (Employee) user;
            timeLogRepository.deleteByEmployee(employee);
            log.info("Deleted time logs for employee: {}", userId);
            
            manageAssignJobRepository.deleteByEmployee(employee);
            manageAssignJobRepository.deleteByManager(employee);
            log.info("Deleted job assignments for employee: {}", userId);
        }
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

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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
            
            // Count completed SERVICE type jobs for this customer's vehicles
            Long servicesUsed = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                    "COMPLETED".equals(j.getStatus()))
                .count();

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

    /**
     * TEMPORARY: Add CUSTOMER role to an existing user
     * This is a fix for users who were created without roles
     */
    public void addCustomerRoleToUser(UUID userId) {
        log.warn("Adding CUSTOMER role to user: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if user already has CUSTOMER role
        if (user.getRoles().contains(Role.CUSTOMER)) {
            throw new RuntimeException("User already has CUSTOMER role");
        }
        
        // Add the CUSTOMER role
        user.addRole(Role.CUSTOMER);
        userRepository.save(user);
        
        log.info("Successfully added CUSTOMER role to user: {} ({})", user.getEmail(), userId);
    }
}
