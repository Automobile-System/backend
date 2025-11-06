package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.profile.request.CustomerProfileUpdateRequest;
import com.TenX.Automobile.dto.profile.response.CustomerProfileResponse;
import com.TenX.Automobile.dto.request.CustomerRegistrationRequest;
import com.TenX.Automobile.dto.response.CustomerDashboardResponse;
import com.TenX.Automobile.dto.response.ServiceDetailResponse;
import com.TenX.Automobile.dto.response.ServiceFrequencyResponse;
import com.TenX.Automobile.dto.response.ServiceListResponse;
import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.entity.Project;
import com.TenX.Automobile.entity.TimeLog;
import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.ServiceRepository;
import com.TenX.Automobile.repository.TimeLogRepository;
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
    private final TimeLogRepository timeLogRepository;

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
        
        // Count active services (not completed)
        Long activeServices = serviceRepository.countActiveServicesByCustomerId(customer.getId());
        
        // Count completed services
        Long completedServices = serviceRepository.countCompletedServicesByCustomerId(customer.getId());
        
        // Count active projects (not completed)
        Long activeProjects = projectRepository.countActiveProjectsByCustomerId(customer.getId());
        
        // Count completed projects
        Long completedProjects = projectRepository.countCompletedProjectsByCustomerId(customer.getId());
        
        // Count upcoming appointments (arriving date > end of today)
        Long upcomingServiceAppointments = serviceRepository.countUpcomingServicesByCustomerId(customer.getId(), endOfToday);
        Long upcomingProjectAppointments = projectRepository.countUpcomingProjectsByCustomerId(customer.getId(), endOfToday);
        Long upcomingAppointments = upcomingServiceAppointments + upcomingProjectAppointments;
        
        log.info("Dashboard overview fetched successfully for customer: {}", email);
        
        return CustomerDashboardResponse.builder()
                .activeServices(activeServices != null ? activeServices : 0L)
                .completedServices(completedServices != null ? completedServices : 0L)
                .activeProjects(activeProjects != null ? activeProjects : 0L)
                .completedProjects(completedProjects != null ? completedProjects : 0L)
                .upcomingAppointments(upcomingAppointments != null ? upcomingAppointments : 0L)
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
        
        // Fetch services and projects
        List<com.TenX.Automobile.entity.Service> services = serviceRepository.findServicesByCustomerIdAndDateRange(customer.getId(), startDate);
        List<Project> projects = projectRepository.findProjectsByCustomerIdAndDateRange(customer.getId(), startDate);
        
        // Group by month and count
        Map<String, Long> monthlyCount = new LinkedHashMap<>();
        
        // Initialize months based on period
        initializeMonths(monthlyCount, startDate, now, period);
        
        // Count services by month
        for (com.TenX.Automobile.entity.Service service : services) {
            if (service.getCreatedAt() != null) {
                String monthKey = getMonthKey(service.getCreatedAt());
                monthlyCount.put(monthKey, monthlyCount.getOrDefault(monthKey, 0L) + 1);
            }
        }
        
        // Count projects by month
        for (Project project : projects) {
            if (project.getCreatedAt() != null) {
                String monthKey = getMonthKey(project.getCreatedAt());
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

        List<com.TenX.Automobile.entity.Service> services;

        if (status == null || status.isEmpty()) {
            services = serviceRepository.findAllByCustomerId(customer.getId());
        } else if ("active".equalsIgnoreCase(status)) {
            services = serviceRepository.findActiveServicesByCustomerId(customer.getId());
        } else if ("completed".equalsIgnoreCase(status)) {
            services = serviceRepository.findCompletedServicesByCustomerId(customer.getId());
        } else if ("upcoming".equalsIgnoreCase(status)) {
            services = serviceRepository.findUpcomingServicesByCustomerId(customer.getId(), LocalDateTime.now());
        } else {
            throw new IllegalArgumentException("Invalid status filter. Use: active, completed, or upcoming");
        }

        log.info("Found {} services for customer", services.size());

        return services.stream()
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

        com.TenX.Automobile.entity.Service service = serviceRepository.findByIdAndCustomerId(serviceId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Service not found or doesn't belong to customer"));

        // Get time logs for assigned employees
        List<TimeLog> timeLogs = timeLogRepository.findByJobId(serviceId);

        log.info("Service found - title: {}, status: {}, assigned employees: {}", 
                service.getTitle(), service.getStatus(), timeLogs.size());

        return mapToServiceDetailResponse(service, timeLogs);
    }

    private ServiceListResponse mapToServiceListResponse(com.TenX.Automobile.entity.Service service) {
        // Get the first vehicle (since we now only allow one vehicle per booking)
        Vehicle vehicle = service.getVehicles().isEmpty() ? null : service.getVehicles().get(0);

        return ServiceListResponse.builder()
                .serviceId(service.getJobId())
                .title(service.getTitle())
                .description(service.getDescription())
                .category(service.getCategory())
                .status(service.getStatus())
                .arrivingDate(service.getArrivingDate())
                .cost(service.getCost())
                .estimatedHours(service.getEstimatedHours())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration_No() : null)
                .vehicleBrand(vehicle != null ? vehicle.getBrandName() : null)
                .vehicleModel(vehicle != null ? vehicle.getModel() : null)
                .bookedAt(service.getCreatedAt())
                .build();
    }

    private ServiceDetailResponse mapToServiceDetailResponse(com.TenX.Automobile.entity.Service service, List<TimeLog> timeLogs) {
        // Get vehicle information
        Vehicle vehicle = service.getVehicles().isEmpty() ? null : service.getVehicles().get(0);
        ServiceDetailResponse.VehicleInfo vehicleInfo = null;
        if (vehicle != null) {
            vehicleInfo = ServiceDetailResponse.VehicleInfo.builder()
                    .registrationNo(vehicle.getRegistration_No())
                    .brandName(vehicle.getBrandName())
                    .model(vehicle.getModel())
                    .capacity(vehicle.getCapacity())
                    .build();
        }

        // Map tasks
        List<ServiceDetailResponse.TaskInfo> taskInfos = service.getTasks().stream()
                .map(task -> ServiceDetailResponse.TaskInfo.builder()
                        .taskId(task.getTId())
                        .taskTitle(task.getTaskTitle())
                        .taskDescription(task.getTaskDescription())
                        .status(task.getStatus())
                        .estimatedHours(task.getEstimatedHours())
                        .completedAt(task.getCompletedAt())
                        .createdAt(task.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

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
                .serviceId(service.getJobId())
                .title(service.getTitle())
                .description(service.getDescription())
                .category(service.getCategory())
                .status(service.getStatus())
                .arrivingDate(service.getArrivingDate())
                .cost(service.getCost())
                .estimatedHours(service.getEstimatedHours())
                .imageUrl(service.getImageUrl())
                .vehicle(vehicleInfo)
                .tasks(taskInfos)
                .assignedEmployees(employeeInfos)
                .bookedAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}

