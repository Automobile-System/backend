package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.TimeLog;
import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.repository.TimeLogRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final JobRepository jobRepository;

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
}
