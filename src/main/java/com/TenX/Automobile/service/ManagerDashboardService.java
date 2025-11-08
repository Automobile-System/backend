package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.request.CreateProjectRequest;
import com.TenX.Automobile.model.dto.request.CreateTaskRequest;
import com.TenX.Automobile.model.dto.request.UpdateEmployeeStatusRequest;
import com.TenX.Automobile.model.dto.request.UpdateScheduleRequest;
import com.TenX.Automobile.model.dto.response.*;
import com.TenX.Automobile.model.dto.response.CompletionRatePercentageResponse.DataPoint;
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
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManagerDashboardService {

    private final EmployeeRepository employeeRepository;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final JobRepository jobRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Dashboard Overview
    public DashboardOverviewResponse getDashboardOverview() {
        // Active Employees
        Long totalEmployees = employeeRepository.countByRole(Role.STAFF);
        Long availableEmployees = employeeRepository.countActiveByRole(Role.STAFF);
        
        // Count ongoing services (SERVICE type jobs not completed)
        List<Job> allJobs = jobRepository.findAll();
        Long ongoingServicesCount = allJobs.stream()
            .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                    (j.getStatus() == null || !"COMPLETED".equals(j.getStatus())))
            .count();
        
        // Count pending projects (PROJECT type jobs not completed)
        Long pendingProjectsCount = allJobs.stream()
            .filter(j -> JobType.PROJECT.equals(j.getType()) &&
                    (j.getStatus() == null || (!"COMPLETED".equals(j.getStatus()) && !"CANCELLED".equals(j.getStatus()))))
            .count();
        
        // Calculate average completion time (mock - should calculate from actual data)
        Double avgCompletionTime = 24.5;
        
        // System alerts (mock - can be enhanced with actual logic)
        List<DashboardOverviewResponse.SystemAlert> alerts = new ArrayList<>();
        alerts.add(DashboardOverviewResponse.SystemAlert.builder()
            .message("2 services paused due to part delays")
            .employee("Kamal Perera")
            .reason("at maximum workload")
            .build());
        
        // Task distribution (mock - should calculate from actual assignments)
        Map<String, String> taskDistribution = new HashMap<>();
        List<Employee> staffEmployees = employeeRepository.findByRole(Role.STAFF);
        int totalStaff = staffEmployees.size();
        if (totalStaff > 0) {
            taskDistribution.put("ruwan", "18%");
            taskDistribution.put("kamal", "25%");
            taskDistribution.put("nimal", "15%");
            taskDistribution.put("amal", "22%");
            taskDistribution.put("others", "20%");
        }
        
        // Completion rate trend (mock)
        Map<String, String> completionRateTrend = new HashMap<>();
        completionRateTrend.put("ruwan", "85%");
        completionRateTrend.put("kamal", "92%");
        completionRateTrend.put("nimal", "90%");
        completionRateTrend.put("amal", "94%");
        
        return DashboardOverviewResponse.builder()
            .activeEmployees(DashboardOverviewResponse.ActiveEmployees.builder()
                .total(totalEmployees.intValue())
                .available(availableEmployees.intValue())
                .build())
            .ongoingServices(DashboardOverviewResponse.OngoingServices.builder()
                .total(ongoingServicesCount.intValue())
                .status("Currently Active")
                .build())
            .projectsPending(DashboardOverviewResponse.ProjectsPending.builder()
                .total(pendingProjectsCount.intValue())
                .status("Awaiting Action")
                .build())
            .avgCompletionTime(DashboardOverviewResponse.AvgCompletionTime.builder()
                .value(avgCompletionTime)
                .unit("Hours per Service")
                .build())
            .systemAlerts(alerts)
            .taskDistribution(taskDistribution)
            .completionRateTrend(completionRateTrend)
            .build();
    }

    // 2. Employee Management
    public List<EmployeeListResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        return employees.stream().map(emp -> {
            Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
            int maxTasks = 5;
            String currentTasks = activeJobs + "/" + maxTasks;
            String status = activeJobs >= maxTasks ? "Busy (Max Load)" : "Available";
            
            // Mock rating (should come from actual customer ratings)
            Double rating = 4.5 + (Math.random() * 0.5);
            
            return EmployeeListResponse.builder()
                .id(emp.getId().toString())
                .name(emp.getFirstName() + " " + emp.getLastName())
                .skill(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                .currentTasks(currentTasks)
                .rating(Math.round(rating * 10.0) / 10.0)
                .status(status)
                .build();
        }).collect(Collectors.toList());
    }

    public void updateEmployeeStatus(UUID employeeId, UpdateEmployeeStatusRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Map status string to enabled/disabled
        boolean enabled = "Available".equalsIgnoreCase(request.getStatus())? false  : true;
        
        employee.setEnabled(enabled);
        employeeRepository.save(employee);
        log.info("Employee {} status updated to {}", employeeId, request.getStatus());
    }

    public List<EmployeeHistoryResponse> getEmployeeHistory(UUID employeeId) {
        List<ManageAssignJob> completedJobs = manageAssignJobRepository.findCompletedJobsByEmployeeId(employeeId);
        
        return completedJobs.stream().map(assignment -> {
            Job job = assignment.getJob();
            String serviceId = "KA-" + job.getJobId(); // Format as KA-1234
            String vehicle = "N/A";
            if (job.getVehicle() != null) {
                Vehicle v = job.getVehicle();
                vehicle = v.getBrandName() + " " + v.getModel();
            }
            
            String serviceType = "Service";
            // Determine service type based on Job's type field
            if (JobType.SERVICE.equals(job.getType())) {
                // Look up service details
                serviceRepository.findById(job.getTypeId()).ifPresent(service -> {
                    // Use lambda-compatible approach - can't reassign serviceType directly
                });
                // Get service title from repository
                serviceType = serviceRepository.findById(job.getTypeId())
                    .map(com.TenX.Automobile.model.entity.Service::getTitle)
                    .orElse("Service");
            } else if (JobType.PROJECT.equals(job.getType())) {
                // Get project title from repository
                serviceType = projectRepository.findById(job.getTypeId())
                    .map(Project::getTitle)
                    .orElse("Project");
            }
            
            String date = job.getUpdatedAt() != null ? 
                job.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
            
            // Mock customer rating (should come from actual ratings)
            Integer customerRating = 4 + (int)(Math.random() * 2);
            
            return EmployeeHistoryResponse.builder()
                .serviceId(serviceId)
                .vehicle(vehicle)
                .serviceType(serviceType)
                .date(date)
                .customerRating(customerRating)
                .build();
        }).collect(Collectors.toList());
    }

    // 3. Task & Project Management
    public Map<String, Object> createTask(CreateTaskRequest request) {
        // Find or create customer
        Customer customer = customerRepository.findAll().stream()
            .filter(c -> c.getPhoneNumber() != null && c.getPhoneNumber().equals(request.getContactNumber()))
            .findFirst()
            .orElseGet(() -> {
                String email = request.getCustomerName().toLowerCase().replace(" ", ".") + "@example.com";
                Customer newCustomer = Customer.builder()
                    .customerId("CUST" + System.currentTimeMillis())
                    .email(email)
                    .firstName(request.getCustomerName().split(" ")[0])
                    .lastName(request.getCustomerName().split(" ").length > 1 ? 
                        request.getCustomerName().split(" ")[1] : "")
                    .phoneNumber(request.getContactNumber())
                    .password(passwordEncoder.encode("TempPassword123!"))
                    .enabled(true)
                    .build();
                newCustomer.addRole(Role.CUSTOMER);
                return customerRepository.save(newCustomer);
            });
        
        // Find or create vehicle
        Vehicle vehicle = vehicleRepository.findAll().stream()
            .filter(v -> v.getRegistration_No().equals(request.getVehicleRegistration()))
            .findFirst()
            .orElseGet(() -> {
                String[] modelParts = request.getVehicleModel().split(" ");
                Vehicle newVehicle = Vehicle.builder()
                    .registration_No(request.getVehicleRegistration())
                    .brandName(modelParts.length > 0 ? modelParts[0] : "Unknown")
                    .model(request.getVehicleModel())
                    .customer(customer)
                    .build();
                return vehicleRepository.save(newVehicle);
            });
        
        // Create service
        com.TenX.Automobile.model.entity.Service service = new com.TenX.Automobile.model.entity.Service();
        service.setTitle(request.getServiceType());
        service.setDescription(request.getServiceNotes());
        service.setEstimatedHours(request.getEstimatedDurationHours());
        service.setCost(request.getEstimatedPrice() != null ? request.getEstimatedPrice().doubleValue() : 0.0);
        service = serviceRepository.save(service);
        
        // Create Job for this service
        Job job = new Job();
        job.setServiceType(service); // This sets type=SERVICE and typeId=serviceId
        job.setVehicle(vehicle);
        job.setStatus("PENDING");
        job.setArrivingDate(request.getPreferredDate() != null && request.getPreferredTime() != null ?
            LocalDateTime.of(request.getPreferredDate(), request.getPreferredTime()) :
            LocalDateTime.now().plusDays(1));
        job.setCost(request.getEstimatedPrice() != null ? java.math.BigDecimal.valueOf(request.getEstimatedPrice().doubleValue()) : java.math.BigDecimal.ZERO);
        job = jobRepository.save(job);
        
        // Assign to employee if provided
        if (request.getAssignedEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getAssignedEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            Employee manager = employeeRepository.findByRole(Role.MANAGER).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Manager not found"));
            
            ManageAssignJob assignment = ManageAssignJob.builder()
                .job(job)
                .employee(employee)
                .manager(manager)
                .build();
            manageAssignJobRepository.save(assignment);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Task created and added to schedule successfully.");
        response.put("taskId", "task" + job.getJobId());
        return response;
    }

    public Map<String, Object> createProject(CreateProjectRequest request) {
        // Find or create customer
        Customer customer = customerRepository.findAll().stream()
            .filter(c -> c.getPhoneNumber() != null && c.getPhoneNumber().equals(request.getContactNumber()))
            .findFirst()
            .orElseGet(() -> {
                String email = request.getCustomerName().toLowerCase().replace(" ", ".") + "@example.com";
                Customer newCustomer = Customer.builder()
                    .customerId("CUST" + System.currentTimeMillis())
                    .email(email)
                    .firstName(request.getCustomerName().split(" ")[0])
                    .lastName(request.getCustomerName().split(" ").length > 1 ? 
                        request.getCustomerName().split(" ")[1] : "")
                    .phoneNumber(request.getContactNumber())
                    .password(passwordEncoder.encode("TempPassword123!"))
                    .enabled(true)
                    .build();
                newCustomer.addRole(Role.CUSTOMER);
                return customerRepository.save(newCustomer);
            });
        
        // Find or create vehicle
        Vehicle vehicle = vehicleRepository.findAll().stream()
            .filter(v -> v.getRegistration_No().equals(request.getVehicleRegistration()))
            .findFirst()
            .orElseGet(() -> {
                String[] modelParts = request.getVehicleModel().split(" ");
                Vehicle newVehicle = Vehicle.builder()
                    .registration_No(request.getVehicleRegistration())
                    .brandName(modelParts.length > 0 ? modelParts[0] : "Unknown")
                    .model(request.getVehicleModel())
                    .customer(customer)
                    .build();
                return vehicleRepository.save(newVehicle);
            });
        
        // Create project
        Project project = new Project();
        project.setTitle(request.getProjectTitle());
        project.setDescription(request.getProjectDescription());
        project.setEstimatedHours(request.getSubTasks() != null ? 
            request.getSubTasks().stream().mapToDouble(CreateProjectRequest.SubTaskRequest::getHours).sum() : 0.0);
        project.setCost(request.getTotalProjectCost() != null ? request.getTotalProjectCost().doubleValue() : 0.0);
        project.setStatus("Discussion");
        project = projectRepository.save(project);
        
        // Create Job for this project
        Job job = new Job();
        job.setProjectType(project); // This sets type=PROJECT and typeId=projectId
        job.setVehicle(vehicle);
        job.setStatus("Discussion");
        job.setArrivingDate(LocalDateTime.of(request.getStartDate(), java.time.LocalTime.of(9, 0)));
        job.setCost(request.getTotalProjectCost() != null ? request.getTotalProjectCost() : java.math.BigDecimal.ZERO);
        job = jobRepository.save(job);
        
        // Create subtasks
        if (request.getSubTasks() != null) {
            for (CreateProjectRequest.SubTaskRequest subTaskReq : request.getSubTasks()) {
                Task task = new Task();
                task.setTaskTitle(subTaskReq.getName());
                task.setEstimatedHours(subTaskReq.getHours());
                task.setStatus("PENDING");
                task.setProject(project);
                project.addTask(task);
            }
            // Save project with tasks
            projectRepository.save(project);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Project created and added to board successfully.");
        response.put("projectId", "proj" + job.getJobId());
        return response;
    }

    public List<ProjectBoardResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        
        Map<String, List<Project>> groupedByStatus = projects.stream()
            .collect(Collectors.groupingBy(
                p -> p.getStatus() != null ? p.getStatus() : "Discussion"
            ));
        
        List<ProjectBoardResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Project>> entry : groupedByStatus.entrySet()) {
            List<ProjectBoardResponse.ProjectSummary> summaries = entry.getValue().stream()
                .map(p -> {
                    String customerName = "N/A";
                    Long jobId = null;
                    
                    // Find the Job associated with this project to get customer info
                    Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(JobType.PROJECT, p.getProjectId());
                    if (jobOpt.isPresent()) {
                        Job job = jobOpt.get();
                        jobId = job.getJobId();
                        Vehicle vehicle = job.getVehicle();
                        if (vehicle != null && vehicle.getCustomer() != null) {
                            Customer customer = vehicle.getCustomer();
                            customerName = customer.getFirstName() + " " + customer.getLastName();
                        }
                    }
                    
                    return ProjectBoardResponse.ProjectSummary.builder()
                        .id("proj" + (jobId != null ? jobId : p.getProjectId()))
                        .title(p.getTitle())
                        .customer(customerName)
                        .build();
                })
                .collect(Collectors.toList());
            
            result.add(ProjectBoardResponse.builder()
                .status(entry.getKey())
                .projects(summaries)
                .build());
        }
        
        return result;
    }

    // 4. Helper APIs
    public List<String> getServiceTypes() {
        return Arrays.asList(
            "Engine Oil Change",
            "Brake Inspection & Service",
            "Transmission Service",
            "Electrical Diagnostics",
            "Bodywork & Paint",
            "Full Service Package",
            "Vehicle Diagnostics",
            "Tire Service",
            "AC Service",
            "Other (Specify in notes)"
        );
    }

    public List<AvailableEmployeeResponse> getAvailableEmployees() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        return employees.stream().map(emp -> {
            Long activeJobs = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId());
            int maxTasks = 5;
            String tasks = activeJobs + "/" + maxTasks;
            boolean disabled = activeJobs >= maxTasks;
            
            return AvailableEmployeeResponse.builder()
                .id(emp.getId().toString())
                .name(emp.getFirstName() + " " + emp.getLastName())
                .skill(emp.getSpecialty() != null ? emp.getSpecialty() : "General")
                .tasks(disabled ? tasks + " - FULL" : tasks)
                .disabled(disabled)
                .build();
        }).collect(Collectors.toList());
    }

    // 5. Workload Scheduler
    public ScheduleResponse getSchedule(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<ManageAssignJob> assignments = manageAssignJobRepository.findJobsByDateRange(start, end);
        
        Map<String, List<ScheduleResponse.ScheduleTask>> schedule = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        for (String day : days) {
            schedule.put(day, new ArrayList<>());
        }
        
        // Add Sunday as closed
        schedule.get("Sunday").add(ScheduleResponse.ScheduleTask.builder()
            .id("status")
            .text("Closed")
            .build());
        
        // Group assignments by day
        for (ManageAssignJob assignment : assignments) {
            LocalDateTime jobDate = assignment.getJob().getArrivingDate();
            if (jobDate != null) {
                int dayIndex = jobDate.getDayOfWeek().getValue() - 1;
                if (dayIndex >= 0 && dayIndex < days.length) {
                    String taskType = "Service";
                    Job job = assignment.getJob();
                    
                    // Determine task type based on Job's type field
                    if (JobType.SERVICE.equals(job.getType())) {
                        taskType = serviceRepository.findById(job.getTypeId())
                            .map(com.TenX.Automobile.model.entity.Service::getTitle)
                            .orElse("Service");
                    } else if (JobType.PROJECT.equals(job.getType())) {
                        taskType = projectRepository.findById(job.getTypeId())
                            .map(Project::getTitle)
                            .orElse("Project");
                    }
                    
                    ScheduleResponse.ScheduleTask task = ScheduleResponse.ScheduleTask.builder()
                        .id("task" + assignment.getJob().getJobId())
                        .employee(assignment.getEmployee().getFirstName())
                        .task(taskType)
                        .taskId("#" + assignment.getJob().getJobId())
                        .build();
                    
                    schedule.get(days[dayIndex]).add(task);
                }
            }
        }
        
        String weekOf = startDate.format(DateTimeFormatter.ofPattern("MMMM d")) + 
            "-" + endDate.format(DateTimeFormatter.ofPattern("d, yyyy"));
        
        return ScheduleResponse.builder()
            .weekOf(weekOf)
            .schedule(schedule)
            .build();
    }

    public Map<String, Object> updateSchedule(Long taskId, UpdateScheduleRequest request) {
        Job job = jobRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (request.getNewDate() != null && request.getNewTime() != null) {
            job.setArrivingDate(LocalDateTime.of(request.getNewDate(), request.getNewTime()));
        } else if (request.getNewDate() != null) {
            job.setArrivingDate(request.getNewDate().atStartOfDay());
        }
        
        if (request.getAssignedEmployeeId() != null) {
            List<ManageAssignJob> assignments = manageAssignJobRepository.findByJobJobId(taskId);
            if (!assignments.isEmpty()) {
                ManageAssignJob assignment = assignments.get(0);
                Employee newEmployee = employeeRepository.findById(request.getAssignedEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
                assignment.setEmployee(newEmployee);
                manageAssignJobRepository.save(assignment);
            }
        }
        
        jobRepository.save(job);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Task schedule updated successfully.");
        return response;
    }

    public Map<String, Object> autoBalanceWorkload() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        Map<UUID, Long> taskCounts = new HashMap<>();
        
        for (Employee emp : employees) {
            taskCounts.put(emp.getId(), manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId()));
        }
        
        int rescheduled = 0;
        Long maxTasks = taskCounts.values().stream().max(Long::compareTo).orElse(0L);
        Long minTasks = taskCounts.values().stream().min(Long::compareTo).orElse(0L);
        
        if (maxTasks - minTasks > 2) {
            rescheduled = 3; // Mock number
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Workload auto-balancing initiated. " + rescheduled + " tasks rescheduled.");
        return response;
    }

    // 6. Reports & Analytics
    public ReportsResponse getEmployeeEfficiencyReport() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        Map<String, Integer> data = new HashMap<>();
        
        for (Employee emp : employees) {
            String firstName = emp.getFirstName().toLowerCase();
            int efficiency = 85 + (int)(Math.random() * 15);
            data.put(firstName, efficiency);
        }
        
        return ReportsResponse.builder()
            .data(data)
            .type("BarChart")
            .build();
    }

    public ReportsResponse getMostRequestedEmployeesReport() {
        List<Employee> employees = employeeRepository.findByRole(Role.STAFF);
        List<ReportsResponse.DataPoint> dataPoints = new ArrayList<>();
        
        for (Employee emp : employees) {
            Long requestCount = manageAssignJobRepository.countActiveJobsByEmployeeId(emp.getId()) + 
                (long)(Math.random() * 40);
            dataPoints.add(ReportsResponse.DataPoint.builder()
                .name(emp.getFirstName() + " " + emp.getLastName())
                .requests(requestCount.intValue())
                .build());
        }
        
        dataPoints.sort((a, b) -> b.getRequests().compareTo(a.getRequests()));
        
        return ReportsResponse.builder()
            .dataList(dataPoints)
            .type("HorizontalBarChart")
            .build();
    }

    public ReportsResponse getPartsDelayAnalyticsReport() {
        List<ReportsResponse.DataPoint> dataPoints = Arrays.asList(
            ReportsResponse.DataPoint.builder().month("Aug").delays(5).build(),
            ReportsResponse.DataPoint.builder().month("Sep").delays(7).build(),
            ReportsResponse.DataPoint.builder().month("Oct").delays(8).build()
        );
        
        return ReportsResponse.builder()
            .averageDelayDays(2.5)
            .mostCommonReason("Transmission parts (8 cases)")
            .dataList(dataPoints)
            .type("LineChart")
            .build();
    }

    public ReportsResponse getCompletedProjectsByTypeReport() {
        List<ReportsResponse.DataPoint> dataPoints = Arrays.asList(
            ReportsResponse.DataPoint.builder().type("Engine").value(35).build(),
            ReportsResponse.DataPoint.builder().type("Electrical").value(20).build(),
            ReportsResponse.DataPoint.builder().type("Bodywork").value(25).build(),
            ReportsResponse.DataPoint.builder().type("Custom").value(15).build(),
            ReportsResponse.DataPoint.builder().type("Other").value(5).build()
        );
        
        return ReportsResponse.builder()
            .dataList(dataPoints)
            .type("DonutChart")
            .build();
    }

    public CompletionRatePercentageResponse getCompletionRateTrendReport() {
        log.info("Generating completion rate trend report...");

        // Fetch only completed jobs
        List<Job> completedJobs = jobRepository.findByStatus("COMPLETED");

        if (completedJobs.isEmpty()) {
            log.info("No completed jobs found for report generation.");
            return CompletionRatePercentageResponse.builder()
                    .chartType("line")
                    .title("Completion Rate Percentage Over Time")
                    .data(new DataPoint[0])
                    .build();
        }

        //  Group completed jobs by completion month
        Map<YearMonth, List<Job>> jobsByMonth = completedJobs.stream()
                .filter(job -> job.getCompletionDate() != null)
                .collect(Collectors.groupingBy(
                        job -> YearMonth.from(job.getCompletionDate()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<DataPoint> dataPoints = new ArrayList<>();

        for (Map.Entry<YearMonth, List<Job>> entry : jobsByMonth.entrySet()) {
            YearMonth ym = entry.getKey();
            List<Job> monthlyCompletedJobs = entry.getValue();

            int completedTasks = monthlyCompletedJobs.size();

            // ✅ Fetch total jobs (any status) for that month to calculate rate
            int totalTasks = (int) jobRepository.findAll().stream()
                    .filter(job -> job.getCompletionDate() != null)
                    .filter(job -> YearMonth.from(job.getCompletionDate()).equals(ym))
                    .count();

            double rate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0;

            String monthLabel = ym.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear();

            dataPoints.add(DataPoint.builder()
                    .month(monthLabel)
                    .rate(rate)
                    .completedTasks(completedTasks)
                    .totalTasks(totalTasks)
                    .build());
        }

        return CompletionRatePercentageResponse.builder()
                .chartType("line")
                .title("Completion Rate Percentage Over Time")
                .data(dataPoints.toArray(new DataPoint[0]))
                .build();
    }
}

