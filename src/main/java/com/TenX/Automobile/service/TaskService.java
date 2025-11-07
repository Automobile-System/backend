package com.TenX.Automobile.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.dto.response.AssignedTaskResponse;
import com.TenX.Automobile.dto.response.CalendarEventResponse;
import com.TenX.Automobile.dto.response.DashboardSummaryResponse;
import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.entity.Project;
import com.TenX.Automobile.entity.Task;
import com.TenX.Automobile.entity.TimeLog;
import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.TaskRepository;
import com.TenX.Automobile.repository.TimeLogRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TimeLogRepository timeLogRepository;
    private final JobRepository jobRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task create(Task task) {
        if (task.getProject() != null && task.getProject().getProjectId() != null) {
            Project p = projectRepository.findById(task.getProject().getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
            task.setProject(p);
        }
        return taskRepository.save(task);
    }

    public Task update(Long id, Task task) {
        Task existing = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        existing.setTaskTitle(task.getTaskTitle());
        existing.setTaskDescription(task.getTaskDescription());
        existing.setEstimatedHours(task.getEstimatedHours());
        existing.setStatus(task.getStatus());
        existing.setCompletedAt(task.getCompletedAt());
        if (task.getProject() != null && task.getProject().getProjectId() != null) {
            Project p = projectRepository.findById(task.getProject().getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
            existing.setProject(p);
        }
        return taskRepository.save(existing);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Get all tasks assigned to an employee
     * Supports optional status filtering
     */
    @Transactional(readOnly = true)
    public List<AssignedTaskResponse> getAssignedTasksByEmployeeId(UUID employeeId, String status) {
        log.info("Fetching assigned tasks for employee ID: {} with status filter: {}", employeeId, status);
        List<Task> tasks;
        
        if (status != null && !status.isEmpty()) {
            tasks = taskRepository.findTasksByEmployeeIdAndStatus(employeeId, status);
        } else {
            tasks = taskRepository.findTasksByEmployeeId(employeeId);
        }
        
        return tasks.stream()
                .map(this::convertToAssignedTaskResponse)
                .collect(Collectors.toList());
    }

    /**
     * Start a task - Move from 'Not Started' to 'In Progress' and initiate timer
     */
    public Task startTask(Long taskId) {
        log.info("Starting task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Validate task can be started
        if ("IN_PROGRESS".equals(task.getStatus())) {
            throw new IllegalStateException("Task is already in progress");
        }
        if ("COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Cannot start a completed task");
        }

        // Update task status
        task.setStatus("IN_PROGRESS");
        Task savedTask = taskRepository.save(task);

        // Start timer in TimeLog
        if (task.getProject() != null) {
            // Find the Job associated with this project
            Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, task.getProject().getProjectId());
            
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                Long jobId = job.getJobId();
                Optional<TimeLog> existingLog = timeLogRepository.findByJob_JobId(jobId);
                
                if (existingLog.isEmpty()) {
                    // Create new TimeLog entry
                    TimeLog timeLog = new TimeLog();
                    timeLog.setJob(job);
                    timeLog.setStartTime(LocalDateTime.now());
                    timeLog.setDescription("Timer started for task: " + task.getTaskTitle());
                    timeLogRepository.save(timeLog);
                    log.info("TimeLog created for job ID: {}", jobId);
                } else {
                    // Resume existing timer if paused
                    TimeLog timeLog = existingLog.get();
                    if (timeLog.getEndTime() != null) {
                        // Resume paused timer
                        timeLog.setStartTime(LocalDateTime.now());
                        timeLog.setEndTime(null);
                        timeLogRepository.save(timeLog);
                        log.info("TimeLog resumed for job ID: {}", jobId);
                    }
                }
            }
        }

        log.info("Task {} started successfully", taskId);
        return savedTask;
    }

    /**
     * Pause a task - Move to 'Paused' or 'WAITING_PARTS' with reason and notes
     */
    public Task pauseTask(Long taskId, String reason, String notes) {
        log.info("Pausing task with ID: {}, reason: {}", taskId, reason);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Validate task can be paused
        if ("COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Cannot pause a completed task");
        }
        if ("PAUSED".equals(task.getStatus()) || "WAITING_PARTS".equals(task.getStatus())) {
            throw new IllegalStateException("Task is already paused");
        }

        // Set status based on reason (WAITING_PARTS if reason indicates waiting for parts, otherwise PAUSED)
        String pauseStatus = reason != null && reason.toLowerCase().contains("waiting for parts") 
                ? "WAITING_PARTS" 
                : "PAUSED";
        task.setStatus(pauseStatus);
        
        // Optionally update task description with pause notes
        if (notes != null && !notes.isEmpty()) {
            String currentDesc = task.getTaskDescription() != null ? task.getTaskDescription() : "";
            task.setTaskDescription(currentDesc + "\n[PAUSED] Reason: " + reason + ". Notes: " + notes);
        }

        Task savedTask = taskRepository.save(task);

        // Pause timer in TimeLog
        if (task.getProject() != null) {
            // Find the Job associated with this project
            Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, task.getProject().getProjectId());
            
            if (jobOpt.isPresent()) {
                Long jobId = jobOpt.get().getJobId();
                Optional<TimeLog> timeLogOpt = timeLogRepository.findByJob_JobId(jobId);
                
                if (timeLogOpt.isPresent()) {
                    TimeLog timeLog = timeLogOpt.get();
                    if (timeLog.getStartTime() != null && timeLog.getEndTime() == null) {
                        // Stop timer and calculate hours worked so far
                        timeLog.setEndTime(LocalDateTime.now());
                        timeLog.setDescription(
                            (timeLog.getDescription() != null ? timeLog.getDescription() + "\n" : "") +
                            "[PAUSED] Reason: " + reason + (notes != null ? ". Notes: " + notes : "")
                        );
                        timeLog.calculateHoursWorked();
                        timeLogRepository.save(timeLog);
                        log.info("TimeLog paused for job ID: {}", jobId);
                    }
                }
            }
        }

        log.info("Task {} paused successfully", taskId);
        return savedTask;
    }

    /**
     * Resume a task - Move from 'Paused' back to 'In Progress'
     */
    public Task resumeTask(Long taskId) {
        log.info("Resuming task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Validate task can be resumed
        if (!"PAUSED".equals(task.getStatus()) && !"WAITING_PARTS".equals(task.getStatus())) {
            throw new IllegalStateException("Task is not paused. Current status: " + task.getStatus());
        }

        // Update task status
        task.setStatus("IN_PROGRESS");
        Task savedTask = taskRepository.save(task);

        // Resume timer in TimeLog
        if (task.getProject() != null) {
            // Find the Job associated with this project
            Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, task.getProject().getProjectId());
            
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                Long jobId = job.getJobId();
                Optional<TimeLog> timeLogOpt = timeLogRepository.findByJob_JobId(jobId);
                
                if (timeLogOpt.isPresent()) {
                    TimeLog timeLog = timeLogOpt.get();
                    // Start new timer session (or resume existing)
                    timeLog.setStartTime(LocalDateTime.now());
                    timeLog.setEndTime(null);
                    timeLog.setDescription(
                        (timeLog.getDescription() != null ? timeLog.getDescription() + "\n" : "") +
                        "[RESUMED] Task resumed"
                    );
                    timeLogRepository.save(timeLog);
                    log.info("TimeLog resumed for job ID: {}", jobId);
                } else {
                    // Create new TimeLog if none exists
                    TimeLog timeLog = new TimeLog();
                    timeLog.setJob(job);
                    timeLog.setStartTime(LocalDateTime.now());
                    timeLog.setDescription("Timer started (resumed) for task: " + task.getTaskTitle());
                    timeLogRepository.save(timeLog);
                    log.info("TimeLog created for resumed task, job ID: {}", jobId);
                }
            }
        }

        log.info("Task {} resumed successfully", taskId);
        return savedTask;
    }

    /**
     * Complete a task - Mark as finished and stop timer
     */
    public Task completeTask(Long taskId) {
        log.info("Completing task with ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Validate task can be completed
        if ("COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Task is already completed");
        }

        // Update task status and completion time
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);

        // Stop timer in TimeLog
        if (task.getProject() != null) {
            // Find the Job associated with this project
            Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, task.getProject().getProjectId());
            
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                Long jobId = job.getJobId();
                Optional<TimeLog> timeLogOpt = timeLogRepository.findByJob_JobId(jobId);
                
                if (timeLogOpt.isPresent()) {
                    TimeLog timeLog = timeLogOpt.get();
                    if (timeLog.getEndTime() == null) {
                        // Stop timer and calculate final hours
                        timeLog.setEndTime(LocalDateTime.now());
                        timeLog.setDescription(
                            (timeLog.getDescription() != null ? timeLog.getDescription() + "\n" : "") +
                            "[COMPLETED] Task completed"
                        );
                        timeLog.calculateHoursWorked();
                        timeLogRepository.save(timeLog);
                        log.info("TimeLog completed for job ID: {}", jobId);
                    }
                } else {
                    // Create TimeLog entry if none exists (shouldn't happen, but handle it)
                    TimeLog timeLog = new TimeLog();
                    timeLog.setJob(job);
                    timeLog.setStartTime(LocalDateTime.now());
                    timeLog.setEndTime(LocalDateTime.now());
                    timeLog.setDescription("Timer created and completed for task: " + task.getTaskTitle());
                    timeLog.calculateHoursWorked();
                    timeLogRepository.save(timeLog);
                    log.info("TimeLog created and completed for job ID: {}", jobId);
                }
            }
        }

        log.info("Task {} completed successfully", taskId);
        return savedTask;
    }

    /**
     * Get dashboard summary for an employee
     */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID employeeId) {
        log.info("Fetching dashboard summary for employee ID: {}", employeeId);
        
        Long tasksToday = taskRepository.countTasksTodayByEmployeeId(employeeId);
        Long tasksCompleted = taskRepository.countCompletedTasksThisMonthByEmployeeId(employeeId);
        
        // Calculate total hours logged this month
        Double totalHours = calculateTotalHoursThisMonth(employeeId);
        
        return DashboardSummaryResponse.builder()
                .tasksToday(tasksToday != null ? tasksToday.intValue() : 0)
                .tasksCompletedThisMonth(tasksCompleted != null ? tasksCompleted.intValue() : 0)
                .totalHoursLoggedThisMonth(totalHours != null ? totalHours : 0.0)
                .averageRating(null) // TODO: Implement rating calculation if available
                .build();
    }

    /**
     * Get calendar events for an employee within date range
     */
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getCalendarEvents(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching calendar events for employee ID: {} from {} to {}", employeeId, startDate, endDate);
        List<Task> tasks = taskRepository.findTasksByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        
        return tasks.stream()
                .map(this::convertToCalendarEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Task entity to AssignedTaskResponse
     */
    private AssignedTaskResponse convertToAssignedTaskResponse(Task task) {
        Project project = task.getProject();
        if (project == null) {
            return AssignedTaskResponse.builder()
                    .id(task.getTId())
                    .title(task.getTaskTitle())
                    .status(task.getStatus())
                    .build();
        }

        // Get vehicle info from project's job
        UUID vehicleId = null;
        String vehicleRegNo = null;
        String customerName = null;
        LocalDateTime deadline = null;
        
        // Find the Job associated with this project to get vehicle and deadline
        Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, project.getProjectId());
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            Vehicle vehicle = job.getVehicle();
            if (vehicle != null) {
                vehicleId = vehicle.getV_Id();
                vehicleRegNo = vehicle.getRegistration_No();
                
                if (vehicle.getCustomer() != null) {
                    customerName = vehicle.getCustomer().getFirstName() + " " + 
                                  (vehicle.getCustomer().getLastName() != null ? 
                                   vehicle.getCustomer().getLastName() : "");
                }
            }
            deadline = job.getArrivingDate();
        }

        // Calculate time spent from TimeLog
        Double timeSpent = calculateTimeSpentForTask(task);

        return AssignedTaskResponse.builder()
                .id(task.getTId())
                .title(task.getTaskTitle())
                .status(task.getStatus())
                .deadline(deadline)
                .timeSpent(timeSpent)
                .vehicleId(vehicleId)
                .vehicleRegNo(vehicleRegNo)
                .customerName(customerName)
                .teamMembers(new ArrayList<>()) // TODO: Implement team members if available
                .build();
    }

    /**
     * Convert Task entity to CalendarEventResponse
     */
    private CalendarEventResponse convertToCalendarEventResponse(Task task) {
        Project project = task.getProject();
        if (project == null) {
            return CalendarEventResponse.builder()
                    .id(task.getTId())
                    .title(task.getTaskTitle())
                    .build();
        }

        String vehicleRegNo = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        
        // Find the Job associated with this project to get vehicle and times
        Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, project.getProjectId());
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            Vehicle vehicle = job.getVehicle();
            if (vehicle != null) {
                vehicleRegNo = vehicle.getRegistration_No();
            }
            
            // Use job's arrivingDate as startTime
            startTime = job.getArrivingDate();
            endTime = startTime != null && task.getEstimatedHours() != null ?
                    startTime.plusHours(task.getEstimatedHours().longValue()) : startTime;
        }

        return CalendarEventResponse.builder()
                .id(task.getTId())
                .title(task.getTaskTitle())
                .startTime(startTime)
                .endTime(endTime)
                .vehicleRegNo(vehicleRegNo)
                .build();
    }

    /**
     * Calculate time spent for a task from TimeLog
     */
    private Double calculateTimeSpentForTask(Task task) {
        if (task.getProject() == null) {
            return 0.0;
        }
        
        // Find the Job associated with this project
        Optional<Job> jobOpt = jobRepository.findByTypeAndTypeId(com.TenX.Automobile.enums.JobType.PROJECT, task.getProject().getProjectId());
        if (jobOpt.isPresent()) {
            Long jobId = jobOpt.get().getJobId();
            Optional<TimeLog> timeLogOpt = timeLogRepository.findByJob_JobId(jobId);
            if (timeLogOpt.isPresent()) {
                TimeLog timeLog = timeLogOpt.get();
                if (timeLog.getHoursWorked() != null) {
                    return timeLog.getHoursWorked();
                }
                // Calculate from start and end time if hoursWorked is null
                if (timeLog.getStartTime() != null && timeLog.getEndTime() != null) {
                    long minutes = java.time.Duration.between(timeLog.getStartTime(), timeLog.getEndTime()).toMinutes();
                    return minutes / 60.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Calculate total hours logged this month for an employee
     */
    private Double calculateTotalHoursThisMonth(UUID employeeId) {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        
        List<Task> tasks = taskRepository.findTasksByEmployeeIdAndDateRange(employeeId, startOfMonth, endOfMonth);
        
        return tasks.stream()
                .mapToDouble(this::calculateTimeSpentForTask)
                .sum();
    }
}
