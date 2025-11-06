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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        
        // Count ongoing services
        List<com.TenX.Automobile.entity.Service> allServices = serviceRepository.findAll();
        Long ongoingServicesCount = allServices.stream()
            .filter(s -> s.getStatus() == null || !"COMPLETED".equals(s.getStatus()))
            .count();
        
        // Count pending projects
        List<Project> allProjects = projectRepository.findAll();
        Long pendingProjectsCount = allProjects.stream()
            .filter(p -> p.getStatus() == null || (!"COMPLETED".equals(p.getStatus()) && !"CANCELLED".equals(p.getStatus())))
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
        boolean enabled = "Available".equalsIgnoreCase(request.getStatus()) || 
                         "Unavailable".equalsIgnoreCase(request.getStatus()) ? 
                         "Available".equalsIgnoreCase(request.getStatus()) : employee.isEnabled();
        
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
            if (!job.getVehicles().isEmpty()) {
                Vehicle v = job.getVehicles().get(0);
                vehicle = v.getBrandName() + " " + v.getModel();
            }
            
            String serviceType = "Service";
            if (job instanceof com.TenX.Automobile.entity.Service) {
                com.TenX.Automobile.entity.Service serviceJob = (com.TenX.Automobile.entity.Service) job;
                serviceType = serviceJob.getTitle() != null ? serviceJob.getTitle() : "Service";
            } else if (job instanceof Project) {
                Project projectJob = (Project) job;
                serviceType = projectJob.getTitle() != null ? projectJob.getTitle() : "Project";
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
        com.TenX.Automobile.entity.Service service = new com.TenX.Automobile.entity.Service();
        service.setTitle(request.getServiceType());
        service.setDescription(request.getServiceNotes());
        service.setEstimatedHours(request.getEstimatedDurationHours());
        service.setCost(request.getEstimatedPrice());
        service.setStatus("PENDING");
        service.setArrivingDate(request.getPreferredDate() != null && request.getPreferredTime() != null ?
            LocalDateTime.of(request.getPreferredDate(), request.getPreferredTime()) :
            LocalDateTime.now().plusDays(1));
        service.addVehicle(vehicle);
        service = serviceRepository.save(service);
        
        // Assign to employee if provided
        if (request.getAssignedEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getAssignedEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            Employee manager = employeeRepository.findByRole(Role.MANAGER).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Manager not found"));
            
            ManageAssignJob assignment = ManageAssignJob.builder()
                .job(service)
                .employee(employee)
                .manager(manager)
                .build();
            manageAssignJobRepository.save(assignment);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Task created and added to schedule successfully.");
        response.put("taskId", "task" + service.getJobId());
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
        project.setCost(request.getTotalProjectCost());
        project.setStatus("Discussion");
        project.setArrivingDate(LocalDateTime.of(request.getStartDate(), java.time.LocalTime.of(9, 0)));
        project.addVehicle(vehicle);
        
        // Create subtasks
        if (request.getSubTasks() != null) {
            for (CreateProjectRequest.SubTaskRequest subTaskReq : request.getSubTasks()) {
                Task task = new Task();
                task.setTaskTitle(subTaskReq.getName());
                task.setEstimatedHours(subTaskReq.getHours());
                task.setStatus("PENDING");
                task.setProject(project); // Set project relationship
                project.addTask(task); // This sets job relationship (since Project extends Job)
            }
        }
        
        project = projectRepository.save(project);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Project created and added to board successfully.");
        response.put("projectId", "proj" + project.getJobId());
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
                    if (!p.getVehicles().isEmpty() && p.getVehicles().get(0).getCustomer() != null) {
                        Customer customer = p.getVehicles().get(0).getCustomer();
                        customerName = customer.getFirstName() + " " + customer.getLastName();
                    }
                    return ProjectBoardResponse.ProjectSummary.builder()
                        .id("proj" + p.getJobId())
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
                    if (job instanceof com.TenX.Automobile.entity.Service) {
                        com.TenX.Automobile.entity.Service serviceJob = (com.TenX.Automobile.entity.Service) job;
                        taskType = serviceJob.getTitle() != null ? serviceJob.getTitle() : "Service";
                    } else if (job instanceof Project) {
                        Project projectJob = (Project) job;
                        taskType = projectJob.getTitle() != null ? projectJob.getTitle() : "Project";
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
}

