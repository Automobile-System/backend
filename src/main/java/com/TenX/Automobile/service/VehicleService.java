package com.TenX.Automobile.service;

import com.TenX.Automobile.model.dto.request.VehicleRequest;
import com.TenX.Automobile.model.dto.response.VehicleResponse;
import com.TenX.Automobile.model.dto.response.VehicleServiceHistoryResponse;
import com.TenX.Automobile.model.entity.Vehicle;
import com.TenX.Automobile.model.entity.Customer;
import com.TenX.Automobile.model.entity.Job;
import com.TenX.Automobile.model.entity.Project;
import com.TenX.Automobile.model.entity.Task;
import com.TenX.Automobile.model.entity.ManageAssignJob;
import com.TenX.Automobile.model.entity.TimeLog;
import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.enums.JobType;
import com.TenX.Automobile.exception.DuplicateResourceException;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.repository.VehicleRepository;
import com.TenX.Automobile.repository.CustomerRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.ServiceRepository;
import com.TenX.Automobile.repository.TaskRepository;
import com.TenX.Automobile.repository.ManageAssignJobRepository;
import com.TenX.Automobile.repository.TimeLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final JobRepository jobRepository;
    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;
    private final TaskRepository taskRepository;
    private final ManageAssignJobRepository manageAssignJobRepository;
    private final TimeLogRepository timeLogRepository;

    /**
     * Get all vehicles for a customer
     */
    public List<VehicleResponse> getCustomerVehicles(String email) {
        log.info("Fetching vehicles for customer: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        List<Vehicle> vehicles = vehicleRepository.findAllByCustomerId(customer.getId());

        return vehicles.stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a new vehicle for a customer
     */
    public VehicleResponse addVehicle(String email, VehicleRequest request) {
        log.info("Adding vehicle for customer: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        // Check if registration number already exists
        if (vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }

        // Validate input
        validateVehicleRequest(request);

        Vehicle vehicle = Vehicle.builder()
                .registration_No(request.getRegistrationNo().trim().toUpperCase())
                .brandName(request.getBrandName().trim())
                .model(request.getModel().trim())
                .capacity(request.getCapacity())
                .customer(customer)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle added successfully with ID: {}", savedVehicle.getV_Id());

        return mapToVehicleResponse(savedVehicle);
    }

    /**
     * Update a vehicle
     */
    public VehicleResponse updateVehicle(String email, UUID vehicleId, VehicleRequest request) {
        log.info("Updating vehicle {} for customer: {}", vehicleId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to this customer"));

        // Check if registration number is being changed and if it already exists
        if (!vehicle.getRegistration_No().equals(request.getRegistrationNo()) &&
            vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new RuntimeException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }

        // Validate input
        validateVehicleRequest(request);

        // Update fields
        vehicle.setRegistration_No(request.getRegistrationNo().trim().toUpperCase());
        vehicle.setBrandName(request.getBrandName().trim());
        vehicle.setModel(request.getModel().trim());
        vehicle.setCapacity(request.getCapacity());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully: {}", vehicleId);

        return mapToVehicleResponse(updatedVehicle);
    }

    /**
     * Delete a vehicle (with cascading delete of all related data)
     */
    @Transactional
    public void deleteVehicle(String email, UUID vehicleId) {
        log.info("Deleting vehicle {} for customer: {}", vehicleId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to this customer"));

        // Get all jobs for this vehicle
        List<Job> jobs = jobRepository.findByVehicleId(vehicleId);
        
        if (!jobs.isEmpty()) {
            log.info("Found {} jobs associated with vehicle {}. Performing cascading delete.", jobs.size(), vehicleId);
            
            // Delete each job and its related data
            for (Job job : jobs) {
                deleteJobAndRelatedData(job);
            }
        }

        // Finally delete the vehicle
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully: {}", vehicleId);
    }

    /**
     * Delete a job and all its related data in correct order
     */
    private void deleteJobAndRelatedData(Job job) {
        Long jobId = job.getJobId();
        log.info("Deleting job {} and related data", jobId);

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

        // 3. If it's a project, delete tasks and project
        if (JobType.PROJECT.equals(job.getType())) {
            projectRepository.findById(job.getTypeId()).ifPresent(project -> {
                // Tasks will be deleted via cascade due to orphanRemoval = true
                List<Task> tasks = taskRepository.findByProjectProjectId(project.getProjectId());
                if (!tasks.isEmpty()) {
                    taskRepository.deleteAll(tasks);
                    log.info("Deleted {} tasks for project {}", tasks.size(), project.getProjectId());
                }
                projectRepository.delete(project);
                log.info("Deleted project {}", project.getProjectId());
            });
        }

        // 4. Finally delete the job
        jobRepository.delete(job);
        log.info("Deleted job {}", jobId);
    }

    /**
     * Validate vehicle request
     */
    private void validateVehicleRequest(VehicleRequest request) {
        if (request.getRegistrationNo() == null || request.getRegistrationNo().trim().isEmpty()) {
            throw new RuntimeException("Registration number is required");
        }

        if (request.getBrandName() == null || request.getBrandName().trim().isEmpty()) {
            throw new RuntimeException("Brand name is required");
        }

        if (request.getModel() == null || request.getModel().trim().isEmpty()) {
            throw new RuntimeException("Model is required");
        }

        if (request.getCapacity() < 1) {
            throw new RuntimeException("Capacity must be at least 1");
        }
    }

    /**
     * Map Vehicle entity to VehicleResponse DTO
     */
    private VehicleResponse mapToVehicleResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getV_Id())
                .registrationNo(vehicle.getRegistration_No())
                .brandName(vehicle.getBrandName())
                .model(vehicle.getModel())
                .capacity(vehicle.getCapacity())
                .createdBy(vehicle.getCreatedBy())
                .customerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getId() : null)
                .customerEmail(vehicle.getCustomer() != null ? vehicle.getCustomer().getEmail() : null)
                .createdAt(null) // Vehicle entity doesn't have createdAt field
                .build();
    }

    // Additional methods for VehicleController

    /**
     * Get all vehicles
     * @return List of all vehicles
     */
    public List<VehicleResponse> getAllVehicles() {
        log.info("Fetching all vehicles");
        return vehicleRepository.findAll().stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get vehicle by ID
     * @param vehicleId The vehicle ID
     * @return VehicleResponse
     */
    public VehicleResponse getVehicleById(UUID vehicleId) {
        log.info("Fetching vehicle by ID: {}", vehicleId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Get vehicle by registration number
     * @param registrationNo The registration number
     * @return VehicleResponse
     */
    public VehicleResponse getVehicleByRegistrationNo(String registrationNo) {
        log.info("Fetching vehicle by registration number: {}", registrationNo);
        Vehicle vehicle = vehicleRepository.findByRegistration_No(registrationNo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with registration number: " + registrationNo));
        return mapToVehicleResponse(vehicle);
    }

    /**
     * Get all vehicles for a specific customer by customer ID
     * @param customerId The customer ID
     * @return List of vehicles belonging to the customer
     */
    public List<VehicleResponse> getVehiclesByCustomerId(UUID customerId) {
        log.info("Fetching vehicles for customer ID: {}", customerId);
        
        // Verify customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        return vehicleRepository.findByCustomer(customer).stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new vehicle for a specific customer (UUID-based)
     * @param request The vehicle request
     * @param customerId The customer ID
     * @return Created vehicle response
     */
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request, UUID customerId) {
        log.info("Creating vehicle for customer ID: {}", customerId);
        
        // Validate request
        validateVehicleRequest(request);
        
        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        // Check for duplicate registration number
        if (vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new DuplicateResourceException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }
        
        // Create vehicle
        Vehicle vehicle = Vehicle.builder()
                .registration_No(request.getRegistrationNo())
                .brandName(request.getBrandName())
                .model(request.getModel())
                .capacity(request.getCapacity())
                .customer(customer)
                .build();
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getV_Id());
        
        return mapToVehicleResponse(savedVehicle);
    }

    /**
     * Update a vehicle by ID (UUID-based, without email authentication)
     * @param vehicleId The vehicle ID
     * @param request The update request
     * @return Updated vehicle response
     */
    @Transactional
    public VehicleResponse updateVehicle(UUID vehicleId, VehicleRequest request) {
        log.info("Updating vehicle with ID: {}", vehicleId);
        
        // Validate request
        validateVehicleRequest(request);
        
        // Find vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        
        // Check if registration number is being changed and if it's already taken
        if (!vehicle.getRegistration_No().equals(request.getRegistrationNo()) &&
                vehicleRepository.existsByRegistrationNo(request.getRegistrationNo())) {
            throw new DuplicateResourceException("Vehicle with registration number " + request.getRegistrationNo() + " already exists");
        }
        
        // Update vehicle fields
        vehicle.setRegistration_No(request.getRegistrationNo());
        vehicle.setBrandName(request.getBrandName());
        vehicle.setModel(request.getModel());
        vehicle.setCapacity(request.getCapacity());
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully with ID: {}", updatedVehicle.getV_Id());
        
        return mapToVehicleResponse(updatedVehicle);
    }

    /**
     * Delete a vehicle by ID (UUID-based, with cascading delete)
     * @param vehicleId The vehicle ID
     */
    @Transactional
    public void deleteVehicle(UUID vehicleId) {
        log.info("Deleting vehicle with ID: {}", vehicleId);
        
        // Find vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        
        // Get all jobs for this vehicle
        List<Job> jobs = jobRepository.findByVehicleId(vehicleId);
        
        if (!jobs.isEmpty()) {
            log.info("Found {} jobs associated with vehicle {}. Performing cascading delete.", jobs.size(), vehicleId);
            
            // Delete each job and its related data
            for (Job job : jobs) {
                deleteJobAndRelatedData(job);
            }
        }
        
        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted successfully with ID: {}", vehicleId);
    }

    /**
     * Get complete service history for a vehicle
     * Shows all jobs (services and projects) with full details
     */
    public VehicleServiceHistoryResponse getVehicleServiceHistory(String email, UUID vehicleId) {
        log.info("Fetching service history for vehicle {} - customer: {}", vehicleId, email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));

        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customer.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to this customer"));

        // Get all jobs for this vehicle
        List<Job> jobs = jobRepository.findByVehicleId(vehicleId);

        // Calculate statistics
        long completedJobs = jobs.stream()
                .filter(job -> "COMPLETED".equals(job.getStatus()))
                .count();
        long activeJobs = jobs.stream()
                .filter(job -> !"COMPLETED".equals(job.getStatus()))
                .count();

        // Map to job history details
        List<VehicleServiceHistoryResponse.JobHistoryDetail> jobHistory = jobs.stream()
                .map(job -> mapToJobHistoryDetail(job))
                .collect(Collectors.toList());

        return VehicleServiceHistoryResponse.builder()
                .vehicleRegistration(vehicle.getRegistration_No())
                .vehicleBrand(vehicle.getBrandName())
                .vehicleModel(vehicle.getModel())
                .vehicleCapacity(vehicle.getCapacity())
                .totalJobs(jobs.size())
                .completedJobs((int) completedJobs)
                .activeJobs((int) activeJobs)
                .jobHistory(jobHistory)
                .build();
    }

    /**
     * Map Job entity to JobHistoryDetail DTO
     */
    private VehicleServiceHistoryResponse.JobHistoryDetail mapToJobHistoryDetail(Job job) {
        VehicleServiceHistoryResponse.JobHistoryDetail.JobHistoryDetailBuilder builder = VehicleServiceHistoryResponse.JobHistoryDetail.builder()
                .jobId(job.getJobId())
                .jobType(job.getType().name())
                .jobStatus(job.getStatus())
                .arrivingDate(job.getArrivingDate())
                .completionDate(job.getCompletionDate())
                .cost(job.getCost())
                .typeId(job.getTypeId())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt());

        // Get assigned employees and their time logs
        List<VehicleServiceHistoryResponse.AssignedEmployeeDetail> assignedEmployees = new ArrayList<>();
        
        manageAssignJobRepository.findByJob_JobId(job.getJobId()).ifPresent(assignment -> {
            Employee employee = assignment.getEmployee();
            
            // Get total hours worked by this employee on this job
            List<TimeLog> timeLogs = timeLogRepository.findByJobIdAndEmployeeId(
                    job.getJobId(), employee.getId());
            double totalHours = timeLogs.stream()
                    .mapToDouble(TimeLog::getHoursWorked)
                    .sum();
            
            // Get work descriptions from time logs
            String workDescription = timeLogs.stream()
                    .map(TimeLog::getDescription)
                    .filter(desc -> desc != null && !desc.isEmpty())
                    .collect(Collectors.joining("; "));

            VehicleServiceHistoryResponse.AssignedEmployeeDetail employeeDetail = VehicleServiceHistoryResponse.AssignedEmployeeDetail.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getFirstName() + " " + employee.getLastName())
                    .specialty(employee.getSpecialty())
                    .rating(employee.getEmpRating())
                    .profileImageUrl(employee.getProfileImageUrl())
                    .hoursWorked(totalHours)
                    .workDescription(workDescription.isEmpty() ? null : workDescription)
                    .assignedAt(assignment.getCreatedAt())
                    .build();
            
            assignedEmployees.add(employeeDetail);
        });

        builder.assignedEmployees(assignedEmployees);

        // If it's a service job
        if (JobType.SERVICE.equals(job.getType())) {
            serviceRepository.findById(job.getTypeId()).ifPresent(service -> {
                builder.title(service.getTitle())
                       .description(service.getDescription())
                       .estimatedHours(service.getEstimatedHours())
                       .category(service.getCategory())
                       .imageUrl(service.getImageUrl());
            });
        }
        // If it's a project job
        else if (JobType.PROJECT.equals(job.getType())) {
            projectRepository.findById(job.getTypeId()).ifPresent(project -> {
                builder.title(project.getTitle())
                       .description(project.getDescription())
                       .estimatedHours(project.getEstimatedHours())
                       .projectStatus(project.getStatus());
                
                // Get task statistics
                List<Task> tasks = taskRepository.findByProjectProjectId(project.getProjectId());
                long completedTasks = tasks.stream()
                        .filter(task -> "COMPLETED".equals(task.getStatus()))
                        .count();
                
                builder.totalTasks(tasks.size())
                       .completedTasks((int) completedTasks);
            });
        }

        return builder.build();
    }
}

