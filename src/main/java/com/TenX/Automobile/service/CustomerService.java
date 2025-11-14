package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.profile.request.CustomerProfileUpdateRequest;
import com.TenX.Automobile.model.dto.profile.response.CustomerProfileResponse;
import com.TenX.Automobile.model.dto.request.CustomerProjectRequest;
import com.TenX.Automobile.model.dto.request.CustomerRegistrationRequest;
import com.TenX.Automobile.model.dto.response.CustomerDashboardResponse;
import com.TenX.Automobile.model.dto.response.EmployeeDetailsForCustomer;
import com.TenX.Automobile.model.dto.request.ProjectUpdateRequest;
import com.TenX.Automobile.model.dto.response.ProjectDetailResponse;
import com.TenX.Automobile.model.dto.response.ProjectListResponse;
import com.TenX.Automobile.model.dto.response.ServiceDetailResponse;
import com.TenX.Automobile.model.dto.response.ServiceFrequencyResponse;
import com.TenX.Automobile.model.dto.response.ServiceListResponse;
import com.TenX.Automobile.model.entity.Customer;
import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.entity.Job;
import com.TenX.Automobile.model.entity.ManageAssignJob;
import com.TenX.Automobile.model.entity.Project;
import com.TenX.Automobile.model.entity.Task;
import com.TenX.Automobile.model.entity.TimeLog;
import com.TenX.Automobile.model.entity.Vehicle;
import com.TenX.Automobile.model.enums.JobType;
import com.TenX.Automobile.model.enums.Role;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.repository.EmployeeRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.ManageAssignJobRepository;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.ServiceRepository;
import com.TenX.Automobile.repository.TaskRepository;
import com.TenX.Automobile.repository.TimeLogRepository;
import com.TenX.Automobile.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;
    private final JobRepository jobRepository;
    private final EmployeeRepository employeeRepository;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final VehicleRepository vehicleRepository;    

    public Customer registerCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        log.info("Customer Registration Request: {}", customerRegistrationRequest.getEmail());

        if(customerRepository.findByEmail(customerRegistrationRequest.getEmail()).isPresent()){
            throw new RuntimeException("Customer already exists in " +  customerRegistrationRequest.getEmail());
        }

        Customer customer = Customer.builder()
                .customerId(generateCustomerId())
                .firstName(customerRegistrationRequest.getFirstName())
                .lastName(customerRegistrationRequest.getLastName())
                .nationalId(customerRegistrationRequest.getNationalId())
                .phoneNumber(customerRegistrationRequest.getPhoneNumber())
                .email(customerRegistrationRequest.getEmail())
                .password(passwordEncoder.encode(customerRegistrationRequest.getPassword()))

                .build();

        customer.addRole(Role.CUSTOMER);
        System.out.println(customer);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getCustomerId());
        return savedCustomer;

    }

    public String generateCustomerId(){
        log.info("Generating Customer ID...");

        List<String> existingIds = customerRepository.findAllCustomerIds();

        if(existingIds.isEmpty()){
            return  "CUST0001";
        }

        List<Integer> numbers = existingIds.stream()
                .filter(id->id.startsWith("CUST"))
                .map(id->Integer.parseInt(id.substring(4)))
                .sorted()
                .toList();

        int nextNumber = findNextNumber(numbers);

        String newId =String.format("CUST%04d", nextNumber);
        log.info("Generated Customer ID: {}", newId);
        return newId;
    }


    public int findNextNumber(List<Integer> numbers){
        for (int i=0;i<numbers.size();i++){
            if(numbers.get(i) != i+1){
                return i+1;
            }

        }
        return numbers.size() +1;
    }

    /**
     * Get customer profile by email
     */
    public CustomerProfileResponse getCustomerProfile(String email) {
        log.info("Fetching profile for customer: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .customerId(customer.getCustomerId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .nationalId(customer.getNationalId())
                .phoneNumber(customer.getPhoneNumber())
                .profileImageUrl(customer.getProfileImageUrl())
                .roles(customer.getRoles())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .lastLoginAt(customer.getLastLoginAt())
                .build();
    }

    /**
     * Update customer profile
     */
    public CustomerProfileResponse updateCustomerProfile(String email, CustomerProfileUpdateRequest request) {
        log.info("Updating profile for customer: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        // Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            
            // Check if email is actually changing
            if (!newEmail.equals(customer.getEmail())) {
                // Validate email format (additional backend validation)
                if (!isValidEmail(newEmail)) {
                    throw new RuntimeException("Invalid email format: " + newEmail);
                }
                
                // Check if new email already exists
                if (customerRepository.findByEmail(newEmail).isPresent()) {
                    throw new RuntimeException("Email already exists: " + newEmail);
                }
                
                log.info("Updating email from {} to {}", customer.getEmail(), newEmail);
                customer.setEmail(newEmail);
            }
        }
        
        // Update only non-null fields
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            if (request.getFirstName().trim().length() < 2 || request.getFirstName().trim().length() > 50) {
                throw new RuntimeException("First name must be between 2 and 50 characters");
            }
            customer.setFirstName(request.getFirstName().trim());
        }
        
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            if (request.getLastName().trim().length() < 2 || request.getLastName().trim().length() > 50) {
                throw new RuntimeException("Last name must be between 2 and 50 characters");
            }
            customer.setLastName(request.getLastName().trim());
        }
        
        if (request.getNationalId() != null && !request.getNationalId().trim().isEmpty()) {
            String nationalId = request.getNationalId().trim();
            if (!isValidNationalId(nationalId)) {
                throw new RuntimeException("Invalid National ID format. Must be 9 digits followed by V/X or 12 digits");
            }
            customer.setNationalId(nationalId);
        }
        
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            String phoneNumber = request.getPhoneNumber().trim();
            if (!isValidPhoneNumber(phoneNumber)) {
                throw new RuntimeException("Invalid phone number format. Must be 10-15 digits, optionally starting with +");
            }
            customer.setPhoneNumber(phoneNumber);
        }
        
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().trim().isEmpty()) {
            customer.setProfileImageUrl(request.getProfileImageUrl().trim());
        }
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Profile updated successfully for customer: {}", email);
        
        return CustomerProfileResponse.builder()
                .id(updatedCustomer.getId())
                .customerId(updatedCustomer.getCustomerId())
                .email(updatedCustomer.getEmail())
                .firstName(updatedCustomer.getFirstName())
                .lastName(updatedCustomer.getLastName())
                .nationalId(updatedCustomer.getNationalId())
                .phoneNumber(updatedCustomer.getPhoneNumber())
                .profileImageUrl(updatedCustomer.getProfileImageUrl())
                .roles(updatedCustomer.getRoles())
                .createdAt(updatedCustomer.getCreatedAt())
                .updatedAt(updatedCustomer.getUpdatedAt())
                .lastLoginAt(updatedCustomer.getLastLoginAt())
                .build();
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // RFC 5322 compliant email regex (simplified)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Validate National ID format
     */
    private boolean isValidNationalId(String nationalId) {
        if (nationalId == null || nationalId.trim().isEmpty()) {
            return false;
        }
        // Old NIC: 9 digits + V/v/X/x OR New NIC: 12 digits
        return nationalId.matches("^[0-9]{9}[vVxX]?$") || nationalId.matches("^[0-9]{12}$");
    }
    
    /**
     * Validate phone number format
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // Phone: optional + followed by 10-15 digits
        return phoneNumber.matches("^\\+?[0-9]{10,15}$");
    }

    /**
     * Get dashboard overview for customer
     */
    public CustomerDashboardResponse getDashboardOverview(String email) {
        log.info("Fetching dashboard overview for customer: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusSeconds(1);
        
        // Count active services (SERVICE type jobs not completed)
        Long activeServices = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                        (j.getStatus() == null || !"COMPLETED".equals(j.getStatus())))
                .count();
        
        // Count completed services
        Long completedServices = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                        "COMPLETED".equals(j.getStatus()))
                .count();
        
        // Count active projects (PROJECT type jobs not completed)
        Long activeProjects = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.PROJECT.equals(j.getType()) &&
                        (j.getStatus() == null || !"COMPLETED".equals(j.getStatus())))
                .count();
        
        // Count completed projects
        Long completedProjects = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.PROJECT.equals(j.getType()) &&
                        "COMPLETED".equals(j.getStatus()))
                .count();
        
        // Count upcoming appointments (arriving date > end of today)
        Long upcomingServiceAppointments = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.SERVICE.equals(j.getType()) &&
                        j.getArrivingDate() != null && j.getArrivingDate().isAfter(endOfToday))
                .count();
        Long upcomingProjectAppointments = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> JobType.PROJECT.equals(j.getType()) &&
                        j.getArrivingDate() != null && j.getArrivingDate().isAfter(endOfToday))
                .count();
        Long upcomingAppointments = upcomingServiceAppointments + upcomingProjectAppointments;
        
        log.info("Dashboard overview fetched successfully for customer: {}", email);
        
        return CustomerDashboardResponse.builder()
                .activeServices(activeServices)
                .completedServices(completedServices)
                .activeProjects(activeProjects)
                .completedProjects(completedProjects)
                .upcomingAppointments(upcomingAppointments)
                .build();
    }

    /**
     * Get service frequency data for customer
     * @param email Customer email
     * @param period Period filter: "6months", "1year", or "all"
     * @return List of monthly service counts
     */
    public List<ServiceFrequencyResponse> getServiceFrequency(String email, String period) {
        log.info("Fetching service frequency for customer: {} with period: {}", email, period);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        
        // Determine the start date based on period
        LocalDateTime startDate;
        LocalDateTime now = LocalDateTime.now();
        
        switch (period != null ? period.toLowerCase() : "1year") {
            case "6months":
                startDate = now.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "all":
                startDate = LocalDateTime.of(2000, 1, 1, 0, 0); // Far past date
                break;
            case "1year":
            default:
                startDate = now.minusMonths(12).withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
        }
        
        // Fetch jobs (services and projects) for this customer since startDate
        List<Job> customerJobs = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(j -> j.getCreatedAt() != null && j.getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());
        
        // Group by month and count
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        
        // Initialize months based on period
        initializeMonths(monthlyCount, startDate, now, period);
        
        // Count jobs by month
        for (Job job : customerJobs) {
            if (job.getCreatedAt() != null) {
                String monthKey = getMonthKey(job.getCreatedAt());
                monthlyCount.put(monthKey, monthlyCount.getOrDefault(monthKey, 0L) + 1);
            }
        }
        
        // Convert to response list
        List<ServiceFrequencyResponse> response = monthlyCount.entrySet().stream()
                .map(entry -> ServiceFrequencyResponse.builder()
                        .month(entry.getKey())
                        .jobs(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        
        log.info("Service frequency fetched successfully for customer: {}", email);
        return response;
    }
    
    /**
     * Initialize months map with zero counts
     */
    private void initializeMonths(Map<String, Long> monthlyCount, LocalDateTime startDate, LocalDateTime endDate, String period) {
        LocalDateTime current = startDate.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime end = endDate.withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        while (!current.isAfter(end)) {
            String monthKey = getMonthKey(current);
            monthlyCount.put(monthKey, 0L);
            current = current.plusMonths(1);
        }
    }
    
    /**
     * Get month key in format "MMM" (e.g., "Jan", "Feb")
     */
    private String getMonthKey(LocalDateTime date) {
        return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    }

    /**
     * Get all services for a customer with optional status filter
     */
    public List<ServiceListResponse> getCustomerServices(String email, String status) {
        log.info("Getting services for customer: {}, status filter: {}", email, status);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Get all jobs for this customer that are SERVICE type
        List<Job> jobs = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(job -> JobType.SERVICE.equals(job.getType()))
                .collect(Collectors.toList());

        // Apply status filter
        if (status != null && !status.isEmpty()) {
            if ("active".equalsIgnoreCase(status)) {
                jobs = jobs.stream()
                        .filter(job -> "IN_PROGRESS".equals(job.getStatus()) || "PENDING".equals(job.getStatus()))
                        .collect(Collectors.toList());
            } else if ("completed".equalsIgnoreCase(status)) {
                jobs = jobs.stream()
                        .filter(job -> "COMPLETED".equals(job.getStatus()))
                        .collect(Collectors.toList());
            } else if ("upcoming".equalsIgnoreCase(status)) {
                LocalDateTime now = LocalDateTime.now();
                jobs = jobs.stream()
                        .filter(job -> job.getArrivingDate() != null && job.getArrivingDate().isAfter(now))
                        .collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException("Invalid status filter. Use: active, completed, or upcoming");
            }
        }

        log.info("Found {} service jobs for customer", jobs.size());

        return jobs.stream()
                .map(this::mapToServiceListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific service
     */
    public ServiceDetailResponse getServiceDetails(String email, Long serviceId) {
        log.info("Getting service details - email: {}, serviceId: {}", email, serviceId);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Find the service entity
        com.TenX.Automobile.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Find the job for this service and customer
        Job job = jobRepository.findByTypeAndTypeId(JobType.SERVICE, serviceId)
                .filter(j -> j.getVehicle() != null && customer.getId().equals(j.getVehicle().getCustomer().getId()))
                .orElseThrow(() -> new RuntimeException("Service job not found or doesn't belong to customer"));

        // Get time logs for assigned employees
        List<TimeLog> timeLogs = timeLogRepository.findByJobId(job.getJobId());

        log.info("Service found - title: {}, status: {}, assigned employees: {}", 
                service.getTitle(), job.getStatus(), timeLogs.size());

        return mapToServiceDetailResponse(service, job, timeLogs);
    }

    private ServiceListResponse mapToServiceListResponse(Job job) {
        // Lookup the service entity
        com.TenX.Automobile.model.entity.Service service = serviceRepository.findById(job.getTypeId())
                .orElse(null);

        if (service == null) {
            return null;
        }

        Vehicle vehicle = job.getVehicle();
        
        // Get assigned employees from ManageAssignJob
        List<ServiceListResponse.AssignedEmployeeSummary> assignedEmployees = new ArrayList<>();
        Optional<ManageAssignJob> assignmentOpt = manageAssignJobRepository.findByJob_JobId(job.getJobId());
        if (assignmentOpt.isPresent()) {
            Employee emp = assignmentOpt.get().getEmployee();
            assignedEmployees.add(ServiceListResponse.AssignedEmployeeSummary.builder()
                .employeeId(emp.getEmployeeId())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .specialty(emp.getSpecialty())
                .profileImageUrl(emp.getProfileImageUrl())
                .build());
        }

        return ServiceListResponse.builder()
                .serviceId(job.getJobId())
                .title(service.getTitle())
                .description(service.getDescription())
                .category(service.getCategory())
                .status(job.getStatus())
                .arrivingDate(job.getArrivingDate())
                .cost(job.getCost())
                .estimatedHours(service.getEstimatedHours())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration_No() : null)
                .vehicleBrand(vehicle != null ? vehicle.getBrandName() : null)
                .vehicleModel(vehicle != null ? vehicle.getModel() : null)
                .assignedEmployees(assignedEmployees)
                .bookedAt(job.getCreatedAt())
                .build();
    }

    private ServiceDetailResponse mapToServiceDetailResponse(com.TenX.Automobile.model.entity.Service service, Job job, List<TimeLog> timeLogs) {
        // Get vehicle information
        Vehicle vehicle = job.getVehicle();
        ServiceDetailResponse.VehicleInfo vehicleInfo = null;
        if (vehicle != null) {
            vehicleInfo = ServiceDetailResponse.VehicleInfo.builder()
                    .registrationNo(vehicle.getRegistration_No())
                    .brandName(vehicle.getBrandName())
                    .model(vehicle.getModel())
                    .capacity(vehicle.getCapacity())
                    .build();
        }

        // Services don't have tasks - only projects have tasks
        List<ServiceDetailResponse.TaskInfo> taskInfos = Collections.emptyList();

        // Map assigned employees from time logs
        List<ServiceDetailResponse.EmployeeInfo> employeeInfos = timeLogs.stream()
                .map(timeLog -> ServiceDetailResponse.EmployeeInfo.builder()
                        .employeeId(timeLog.getEmployee().getEmployeeId())
                        .firstName(timeLog.getEmployee().getFirstName())
                        .lastName(timeLog.getEmployee().getLastName())
                        .specialty(timeLog.getEmployee().getSpecialty())
                        .hoursWorked(timeLog.getHoursWorked())
                        .workDescription(timeLog.getDescription())
                        .startTime(timeLog.getStartTime())
                        .endTime(timeLog.getEndTime())
                        .build())
                .collect(Collectors.toList());

        return ServiceDetailResponse.builder()
                .serviceId(job.getJobId())
                .title(service.getTitle())
                .description(service.getDescription())
                .category(service.getCategory())
                .status(job.getStatus())
                .arrivingDate(job.getArrivingDate())
                .cost(job.getCost())
                .estimatedHours(service.getEstimatedHours())
                .imageUrl(service.getImageUrl())
                .vehicle(vehicleInfo)
                .tasks(taskInfos)
                .assignedEmployees(employeeInfos)
                .bookedAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    public List<EmployeeDetailsForCustomer> getAllActiveEmployees(){
        List<Employee> employees = employeeRepository.findEnabledStaffEmployees();
        return employees.stream()
                .map(e -> new EmployeeDetailsForCustomer(
                        e.getId(),
                        e.getFirstName() + " " + e.getLastName(),
                        e.getCreatedAt(),
                        e.getSpecialty(),   
                        e.getEmpRating(),
                        e.getProfileImageUrl()
                ))
                .toList();
    }

    // ==================== PROJECT METHODS ====================

    /**
     * Get all projects for a customer with optional status filter
     */
    public List<ProjectListResponse> getCustomerProjects(String email, String status) {
        log.info("Getting projects for customer: {}, status filter: {}", email, status);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Get all jobs for this customer that are PROJECT type
        List<Job> jobs = jobRepository.findByCustomerId(customer.getId()).stream()
                .filter(job -> JobType.PROJECT.equals(job.getType()))
                .collect(Collectors.toList());

        // Apply status filter
        if (status != null && !status.isEmpty()) {
            if ("active".equalsIgnoreCase(status)) {
                jobs = jobs.stream()
                        .filter(job -> "IN_PROGRESS".equals(job.getStatus()) || "PENDING".equals(job.getStatus()))
                        .collect(Collectors.toList());
            } else if ("completed".equalsIgnoreCase(status)) {
                jobs = jobs.stream()
                        .filter(job -> "COMPLETED".equals(job.getStatus()))
                        .collect(Collectors.toList());
            } else if ("upcoming".equalsIgnoreCase(status)) {
                LocalDateTime now = LocalDateTime.now();
                jobs = jobs.stream()
                        .filter(job -> job.getArrivingDate() != null && job.getArrivingDate().isAfter(now))
                        .collect(Collectors.toList());
            } else {
                throw new IllegalArgumentException("Invalid status filter. Use: active, completed, or upcoming");
            }
        }

        log.info("Found {} project jobs for customer", jobs.size());

        return jobs.stream()
                .map(this::mapToProjectListResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific project
     */
    public ProjectDetailResponse getProjectDetails(String email, Long jobId) {
        log.info("Getting project details - email: {}, jobId: {}", email, jobId);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Find the job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Verify it's a project job
        if (!JobType.PROJECT.equals(job.getType())) {
            throw new RuntimeException("Job is not a project");
        }

        // Verify it belongs to the customer
        if (!customer.getId().equals(job.getVehicle().getCustomer().getId())) {
            throw new RuntimeException("Project doesn't belong to customer");
        }

        // Find the project entity
        Project project = projectRepository.findById(job.getTypeId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Get tasks for this project
        List<Task> tasks = taskRepository.findByProjectProjectId(project.getProjectId());

        // Get time logs for assigned employees
        List<TimeLog> timeLogs = timeLogRepository.findByJobId(job.getJobId());

        log.info("Project found - title: {}, status: {}, tasks: {}, assigned employees: {}", 
                project.getTitle(), job.getStatus(), tasks.size(), timeLogs.size());

        return mapToProjectDetailResponse(project, job, tasks, timeLogs);
    }

    /**
     * Update project details
     */
    public ProjectDetailResponse updateProject(String email, Long jobId, ProjectUpdateRequest request) {
        log.info("Updating project - email: {}, jobId: {}", email, jobId);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Find the job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Verify it's a project job
        if (!JobType.PROJECT.equals(job.getType())) {
            throw new RuntimeException("Job is not a project");
        }

        // Verify it belongs to the customer
        if (!customer.getId().equals(job.getVehicle().getCustomer().getId())) {
            throw new RuntimeException("Project doesn't belong to customer");
        }

        // Find and update the project entity
        Project project = projectRepository.findById(job.getTypeId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Update project fields
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            project.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription().trim());
        }
        if (request.getEstimatedHours() != null && request.getEstimatedHours() > 0) {
            project.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getCost() != null && request.getCost() > 0) {
            project.setCost(request.getCost());
            job.setCost(java.math.BigDecimal.valueOf(request.getCost()));
        }
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            project.setStatus(request.getStatus().trim());
        }

        Project updatedProject = projectRepository.save(project);
        jobRepository.save(job);

        // Get tasks and time logs for response
        List<Task> tasks = taskRepository.findByProjectProjectId(updatedProject.getProjectId());
        List<TimeLog> timeLogs = timeLogRepository.findByJobId(job.getJobId());

        log.info("Project updated successfully - jobId: {}, projectId: {}", jobId, updatedProject.getProjectId());

        return mapToProjectDetailResponse(updatedProject, job, tasks, timeLogs);
    }

    /**
     * Delete a project/service job and all related data
     */
    @Transactional
    public void deleteJob(String email, Long jobId) {
        log.info("Deleting job - email: {}, jobId: {}", email, jobId);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Find the job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Verify it belongs to the customer
        if (!customer.getId().equals(job.getVehicle().getCustomer().getId())) {
            throw new RuntimeException("Job doesn't belong to customer");
        }

        // Delete in correct order to avoid foreign key constraints

        // 1. Delete time logs
        List<TimeLog> timeLogs = timeLogRepository.findByJobId(jobId);
        if (!timeLogs.isEmpty()) {
            timeLogRepository.deleteAll(timeLogs);
            log.info("Deleted {} time logs for job {}", timeLogs.size(), jobId);
        }

        // 2. Delete job assignments
        manageAssignJobRepository.findByJob_JobId(jobId).ifPresent(assignment -> {
            manageAssignJobRepository.delete(assignment);
            log.info("Deleted job assignment for job {}", jobId);
        });

        // 3. If it's a project, delete tasks (will cascade delete due to orphanRemoval)
        if (JobType.PROJECT.equals(job.getType())) {
            Project project = projectRepository.findById(job.getTypeId())
                    .orElse(null);
            if (project != null) {
                // Tasks will be deleted via cascade
                projectRepository.delete(project);
                log.info("Deleted project {} with all tasks", project.getProjectId());
            }
        }

        // 4. Finally delete the job
        jobRepository.delete(job);
        log.info("Successfully deleted job: {}", jobId);
    }

    /**
     * Create a new project by customer
     * Customer only provides: vehicle selection, title, and description
     * Other fields like cost, dates, tasks will be managed by manager later
     */
    @Transactional
    public ProjectDetailResponse createProjectByCustomer(String email, CustomerProjectRequest request) {
        log.info("Creating project for customer - email: {}, vehicleId: {}", email, request.getVehicleId());

        // Find customer
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Find and validate vehicle ownership
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!customer.getId().equals(vehicle.getCustomer().getId())) {
            throw new RuntimeException("Vehicle doesn't belong to customer");
        }

        // Create the project entity
        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status("PLANNING") // Initial status
                .estimatedHours(0.0) // To be set by manager
                .cost(0.0) // To be estimated by manager
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created with ID: {}", savedProject.getProjectId());

        // Create the job linking the project to the vehicle
        Job job = Job.builder()
                .type(JobType.PROJECT)
                .typeId(savedProject.getProjectId())
                .vehicle(vehicle)
                .status("PENDING") // Initial status
                .arrivingDate(LocalDateTime.now()) // Request date
                .build();

        job.setProjectType(savedProject);
        Job savedJob = jobRepository.save(job);
        log.info("Job created with ID: {}", savedJob.getJobId());

        // Return detailed response
        return mapToProjectDetailResponse(savedProject, savedJob, 
                Collections.emptyList(), // No tasks yet
                Collections.emptyList()); // No time logs yet
    }

    // ==================== HELPER METHODS ====================

    private ProjectListResponse mapToProjectListResponse(Job job) {
        // Lookup the project entity
        Project project = projectRepository.findById(job.getTypeId())
                .orElse(null);

        if (project == null) {
            return null;
        }

        Vehicle vehicle = job.getVehicle();

        // Get assigned employees from ManageAssignJob
        List<ProjectListResponse.AssignedEmployeeSummary> assignedEmployees = new ArrayList<>();
        Optional<ManageAssignJob> assignmentOpt = manageAssignJobRepository.findByJob_JobId(job.getJobId());
        if (assignmentOpt.isPresent()) {
            Employee emp = assignmentOpt.get().getEmployee();
            assignedEmployees.add(ProjectListResponse.AssignedEmployeeSummary.builder()
                .employeeId(emp.getEmployeeId())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .specialty(emp.getSpecialty())
                .profileImageUrl(emp.getProfileImageUrl())
                .build());
        }

        // Get task counts
        List<Task> tasks = taskRepository.findByProjectProjectId(project.getProjectId());
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();

        return ProjectListResponse.builder()
                .projectId(project.getProjectId())
                .jobId(job.getJobId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(job.getStatus())
                .projectStatus(project.getStatus())
                .arrivingDate(job.getArrivingDate())
                .completionDate(job.getCompletionDate())
                .cost(job.getCost())
                .estimatedHours(project.getEstimatedHours())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration_No() : null)
                .vehicleBrand(vehicle != null ? vehicle.getBrandName() : null)
                .vehicleModel(vehicle != null ? vehicle.getModel() : null)
                .assignedEmployees(assignedEmployees)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .bookedAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private ProjectDetailResponse mapToProjectDetailResponse(Project project, Job job, List<Task> tasks, List<TimeLog> timeLogs) {
        // Get vehicle information
        Vehicle vehicle = job.getVehicle();
        ProjectDetailResponse.VehicleInfo vehicleInfo = null;
        if (vehicle != null) {
            vehicleInfo = ProjectDetailResponse.VehicleInfo.builder()
                    .registrationNo(vehicle.getRegistration_No())
                    .brandName(vehicle.getBrandName())
                    .model(vehicle.getModel())
                    .capacity(vehicle.getCapacity())
                    .build();
        }

        // Map tasks
        List<ProjectDetailResponse.TaskInfo> taskInfos = tasks.stream()
                .map(task -> ProjectDetailResponse.TaskInfo.builder()
                        .taskId(task.getTId())
                        .taskTitle(task.getTaskTitle())
                        .taskDescription(task.getTaskDescription())
                        .status(task.getStatus())
                        .estimatedHours(task.getEstimatedHours())
                        .completedAt(task.getCompletedAt())
                        .createdAt(task.getCreatedAt())
                        .updatedAt(task.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        // Map assigned employees from time logs
        List<ProjectDetailResponse.EmployeeInfo> employeeInfos = timeLogs.stream()
                .map(timeLog -> {
                    Employee emp = timeLog.getEmployee();
                    return ProjectDetailResponse.EmployeeInfo.builder()
                            .employeeId(emp.getEmployeeId())
                            .firstName(emp.getFirstName())
                            .lastName(emp.getLastName())
                            .specialty(emp.getSpecialty())
                            .rating(emp.getEmpRating())
                            .profileImageUrl(emp.getProfileImageUrl())
                            .hoursWorked(timeLog.getHoursWorked())
                            .workDescription(timeLog.getDescription())
                            .startTime(timeLog.getStartTime())
                            .endTime(timeLog.getEndTime())
                            .build();
                })
                .collect(Collectors.toList());

        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .jobId(job.getJobId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(job.getStatus())
                .projectStatus(project.getStatus())
                .arrivingDate(job.getArrivingDate())
                .completionDate(job.getCompletionDate())
                .cost(job.getCost())
                .estimatedHours(project.getEstimatedHours())
                .vehicle(vehicleInfo)
                .tasks(taskInfos)
                .assignedEmployees(employeeInfos)
                .bookedAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}

