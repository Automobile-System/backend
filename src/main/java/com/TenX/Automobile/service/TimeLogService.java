package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.request.ManualTimeLogRequest;
import com.TenX.Automobile.dto.response.TimeLogResponse;
import com.TenX.Automobile.dto.response.WeeklyTotalHoursResponse;
import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.entity.Project;
import com.TenX.Automobile.entity.Task;
import com.TenX.Automobile.entity.TimeLog;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.TaskRepository;
import com.TenX.Automobile.repository.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;

    /**
     * Create a new time log for a job
     */
    public TimeLog createTimeLog(TimeLog timeLog, Long jobId) {
        log.info("Creating time log for job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Check if a time log already exists for this job
        timeLogRepository.findByJob_JobId(jobId).ifPresent(existing -> {
            throw new IllegalStateException("Time log already exists for job ID: " + jobId);
        });

        timeLog.setJob(job);
        timeLog.calculateHoursWorked();
        TimeLog savedTimeLog = timeLogRepository.save(timeLog);

        log.info("Time log created successfully with ID: {} for job: {}", savedTimeLog.getLogId(), jobId);
        return savedTimeLog;
    }

    /**
     * Get time log by ID
     */
    @Transactional(readOnly = true)
    public TimeLog getTimeLogById(Long logId) {
        log.info("Fetching time log with ID: {}", logId);
        return timeLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Time log not found with id: " + logId));
    }

    /**
     * Get time log by job ID
     */
    @Transactional(readOnly = true)
    public TimeLog getTimeLogByJobId(Long jobId) {
        log.info("Fetching time log for job ID: {}", jobId);
        return timeLogRepository.findByJob_JobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Time log not found for job id: " + jobId));
    }

    /**
     * Get all time logs
     */
    @Transactional(readOnly = true)
    public List<TimeLog> getAllTimeLogs() {
        log.info("Fetching all time logs");
        return timeLogRepository.findAll();
    }

    /**
     * Get all time logs for a job
     */
    @Transactional(readOnly = true)
    public List<TimeLog> getTimeLogsByJobId(Long jobId) {
        log.info("Fetching time log for job ID: {}", jobId);
        return timeLogRepository.findByJob_JobId(jobId)
                .map(List::of)
                .orElse(List.of());
    }

    /**
     * Start tracking time for a job
     */
    public TimeLog startTimeTracking(Long jobId) {
        log.info("Starting time tracking for job ID: {}", jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Check if there's an active time log
        TimeLog existingLog = timeLogRepository.findByJob_JobId(jobId).orElse(null);
        
        if (existingLog != null && existingLog.isActive()) {
            throw new IllegalStateException("Time tracking is already active for this job");
        }

        TimeLog timeLog = new TimeLog();
        timeLog.setJob(job);
        timeLog.setStartTime(LocalDateTime.now());
        timeLog.setDescription("Time tracking started");

        TimeLog savedTimeLog = timeLogRepository.save(timeLog);
        log.info("Time tracking started for job ID: {}", jobId);
        return savedTimeLog;
    }

    /**
     * Stop tracking time for a job
     */
    public TimeLog stopTimeTracking(Long jobId) {
        log.info("Stopping time tracking for job ID: {}", jobId);

        TimeLog timeLog = timeLogRepository.findByJob_JobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Active time log not found for job id: " + jobId));

        if (!timeLog.isActive()) {
            throw new IllegalStateException("Time tracking is not active for this job");
        }

        timeLog.setEndTime(LocalDateTime.now());
        timeLog.calculateHoursWorked();
        TimeLog updatedTimeLog = timeLogRepository.save(timeLog);

        log.info("Time tracking stopped for job ID: {}. Total hours: {}", jobId, updatedTimeLog.getHoursWorked());
        return updatedTimeLog;
    }

    /**
     * Update time log
     */
    public TimeLog updateTimeLog(Long logId, TimeLog timeLogDetails) {
        log.info("Updating time log with ID: {}", logId);

        TimeLog existingTimeLog = getTimeLogById(logId);

        if (timeLogDetails.getStartTime() != null) {
            existingTimeLog.setStartTime(timeLogDetails.getStartTime());
        }
        if (timeLogDetails.getEndTime() != null) {
            existingTimeLog.setEndTime(timeLogDetails.getEndTime());
        }
        if (timeLogDetails.getDescription() != null) {
            existingTimeLog.setDescription(timeLogDetails.getDescription());
        }

        existingTimeLog.calculateHoursWorked();
        TimeLog updatedTimeLog = timeLogRepository.save(existingTimeLog);

        log.info("Time log updated successfully with ID: {}", logId);
        return updatedTimeLog;
    }

    /**
     * Delete time log
     */
    public void deleteTimeLog(Long logId) {
        log.info("Deleting time log with ID: {}", logId);
        TimeLog timeLog = getTimeLogById(logId);
        timeLogRepository.delete(timeLog);
        log.info("Time log deleted successfully with ID: {}", logId);
    }

    /**
     * Get time logs for an employee with optional date range filter
     */
    @Transactional(readOnly = true)
    public List<TimeLogResponse> getTimeLogsByEmployeeId(UUID employeeId, String dateRange) {
        log.info("Fetching time logs for employee ID: {} with date range: {}", employeeId, dateRange);
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        // Parse date range if provided (e.g., "2024-01-01,2024-01-31" or "week", "month")
        if (dateRange != null && !dateRange.isEmpty()) {
            if (dateRange.equalsIgnoreCase("week")) {
                LocalDate now = LocalDate.now();
                startDate = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                endDate = now;
            } else if (dateRange.equalsIgnoreCase("month")) {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1);
                endDate = now;
            } else if (dateRange.contains(",")) {
                String[] dates = dateRange.split(",");
                if (dates.length == 2) {
                    startDate = LocalDate.parse(dates[0].trim());
                    endDate = LocalDate.parse(dates[1].trim());
                }
            }
        }
        
        List<TimeLog> timeLogs = timeLogRepository.findTimeLogsByEmployeeIdAndDateRange(
            employeeId, startDate, endDate);
        
        return timeLogs.stream()
                .map(this::convertToTimeLogResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create manual time log entry from task ID
     */
    public TimeLogResponse createManualTimeLog(ManualTimeLogRequest request) {
        log.info("Creating manual time log for task ID: {}", request.getTaskId());
        
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
        
        if (task.getProject() == null) {
            throw new IllegalStateException("Task does not have an associated project/job");
        }
        
        Project project = task.getProject();
        Long jobId = project.getJobId();
        
        // Check if time log already exists for this job
        Optional<TimeLog> existingLog = timeLogRepository.findByJob_JobId(jobId);
        
        TimeLog timeLog;
        if (existingLog.isPresent()) {
            // Update existing log
            timeLog = existingLog.get();
            timeLog.setStartTime(request.getStartTime());
            timeLog.setEndTime(request.getEndTime());
            timeLog.setDescription(request.getRemarks() != null ? request.getRemarks() : "Manual time entry");
            timeLog.calculateHoursWorked();
            log.info("Updating existing time log for job ID: {}", jobId);
        } else {
            // Create new log
            timeLog = new TimeLog();
            timeLog.setJob(project);
            timeLog.setStartTime(request.getStartTime());
            timeLog.setEndTime(request.getEndTime());
            timeLog.setDescription(request.getRemarks() != null ? request.getRemarks() : "Manual time entry");
            timeLog.calculateHoursWorked();
            log.info("Creating new time log for job ID: {}", jobId);
        }
        
        TimeLog savedTimeLog = timeLogRepository.save(timeLog);
        log.info("Manual time log created/updated successfully with ID: {}", savedTimeLog.getLogId());
        
        return convertToTimeLogResponse(savedTimeLog);
    }

    /**
     * Get weekly total hours for an employee
     */
    @Transactional(readOnly = true)
    public WeeklyTotalHoursResponse getWeeklyTotalHours(UUID employeeId) {
        log.info("Fetching weekly total hours for employee ID: {}", employeeId);
        
        // Try using database function first
        Double totalHours = timeLogRepository.calculateWeeklyTotalHours(employeeId);
        
        // If database function doesn't work, calculate manually
        if (totalHours == null) {
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            List<TimeLog> timeLogs = timeLogRepository.findTimeLogsByEmployeeIdAndDateRange(
                employeeId, startOfWeek, endOfWeek);
            
            totalHours = timeLogs.stream()
                    .filter(tl -> tl.getHoursWorked() != null)
                    .mapToDouble(TimeLog::getHoursWorked)
                    .sum();
        }
        
        return WeeklyTotalHoursResponse.builder()
                .totalHoursThisWeek(totalHours != null ? totalHours : 0.0)
                .build();
    }

    /**
     * Convert TimeLog entity to TimeLogResponse DTO
     */
    private TimeLogResponse convertToTimeLogResponse(TimeLog timeLog) {
        String taskTitle = null;
        String vehicleRegNo = null;
        
        if (timeLog.getJob() != null) {
            // Get task title from project if it's a Project
            if (timeLog.getJob() instanceof Project) {
                Project project = (Project) timeLog.getJob();
                // Get first task title if available
                if (project.getTasks() != null && !project.getTasks().isEmpty()) {
                    taskTitle = project.getTasks().get(0).getTaskTitle();
                } else {
                    taskTitle = project.getTitle();
                }
                
                // Get vehicle registration number
                if (project.getVehicle() != null) {
                    vehicleRegNo = project.getVehicle().getRegistration_No();
                }
            }
        }
        
        LocalDate date = timeLog.getStartTime() != null ? 
            timeLog.getStartTime().toLocalDate() : null;
        
        return TimeLogResponse.builder()
                .id(timeLog.getLogId())
                .date(date)
                .taskTitle(taskTitle)
                .vehicleRegNo(vehicleRegNo)
                .startTime(timeLog.getStartTime())
                .endTime(timeLog.getEndTime())
                .durationHours(timeLog.getHoursWorked())
                .remarks(timeLog.getDescription())
                .build();
    }
}