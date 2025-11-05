package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.ManageAssignJob;
import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.repository.ManageAssignJobRepository;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.EmployeeRepository;
import com.TenX.Automobile.exception.ResourceNotFoundException;
import com.TenX.Automobile.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManageAssignJobService {

    private final ManageAssignJobRepository manageAssignJobRepository;
    private final JobRepository jobRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Create a new job assignment (Manager assigns job to Employee)
     */
    public ManageAssignJob createJobAssignment(Long jobId, UUID managerId, UUID employeeId) {
        log.info("Creating job assignment: jobId={}, managerId={}, employeeId={}", jobId, managerId, employeeId);

        // Validate that job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Validate that manager exists
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));

        // Validate that employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Check if assignment already exists for this job (one-to-one relationship)
        if (manageAssignJobRepository.existsByJob_JobId(jobId)) {
            throw new DuplicateResourceException("Job assignment already exists for job ID: " + jobId);
        }

        ManageAssignJob assignment = new ManageAssignJob();
        assignment.setJob(job);
        assignment.setManagerId(manager);
        assignment.setEmployeeId(employee);

        ManageAssignJob savedAssignment = manageAssignJobRepository.save(assignment);

        log.info("Job assignment created successfully with ID: {} for job: {}", 
                savedAssignment.getManageAssignJob_Id(), jobId);
        return savedAssignment;
    }

    /**
     * Get assignment by ID
     */
    @Transactional(readOnly = true)
    public ManageAssignJob getAssignmentById(UUID assignmentId) {
        log.info("Fetching job assignment with ID: {}", assignmentId);
        return manageAssignJobRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Job assignment not found with id: " + assignmentId));
    }

    /**
     * Get assignment by job ID
     */
    @Transactional(readOnly = true)
    public ManageAssignJob getAssignmentByJobId(Long jobId) {
        log.info("Fetching job assignment for job ID: {}", jobId);
        return manageAssignJobRepository.findByJob_JobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job assignment not found for job id: " + jobId));
    }

    /**
     * Get all assignments
     */
    @Transactional(readOnly = true)
    public List<ManageAssignJob> getAllAssignments() {
        log.info("Fetching all job assignments");
        return manageAssignJobRepository.findAll();
    }

    /**
     * Get all assignments by manager ID
     */
    @Transactional(readOnly = true)
    public List<ManageAssignJob> getAssignmentsByManagerId(UUID managerId) {
        log.info("Fetching job assignments for manager ID: {}", managerId);
        return manageAssignJobRepository.findByManagerId_Id(managerId);
    }

    /**
     * Get all assignments by employee ID
     */
    @Transactional(readOnly = true)
    public List<ManageAssignJob> getAssignmentsByEmployeeId(UUID employeeId) {
        log.info("Fetching job assignments for employee ID: {}", employeeId);
        return manageAssignJobRepository.findByEmployeeId_Id(employeeId);
    }

    /**
     * Get assignments by manager and employee
     */
    @Transactional(readOnly = true)
    public List<ManageAssignJob> getAssignmentsByManagerAndEmployee(UUID managerId, UUID employeeId) {
        log.info("Fetching job assignments for manager ID: {} and employee ID: {}", managerId, employeeId);
        return manageAssignJobRepository.findByManagerId_IdAndEmployeeId_Id(managerId, employeeId);
    }

    /**
     * Reassign job to a different employee
     */
    public ManageAssignJob reassignJob(Long jobId, UUID newEmployeeId) {
        log.info("Reassigning job {} to employee {}", jobId, newEmployeeId);

        ManageAssignJob assignment = getAssignmentByJobId(jobId);
        
        Employee newEmployee = employeeRepository.findById(newEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + newEmployeeId));

        assignment.setEmployeeId(newEmployee);
        ManageAssignJob updatedAssignment = manageAssignJobRepository.save(assignment);

        log.info("Job reassigned successfully");
        return updatedAssignment;
    }

    /**
     * Delete assignment
     */
    public void deleteAssignment(UUID assignmentId) {
        log.info("Deleting job assignment with ID: {}", assignmentId);
        ManageAssignJob assignment = getAssignmentById(assignmentId);
        manageAssignJobRepository.delete(assignment);
        log.info("Job assignment deleted successfully with ID: {}", assignmentId);
    }

    /**
     * Delete assignment by job ID
     */
    public void deleteAssignmentByJobId(Long jobId) {
        log.info("Deleting job assignment for job ID: {}", jobId);
        ManageAssignJob assignment = getAssignmentByJobId(jobId);
        manageAssignJobRepository.delete(assignment);
        log.info("Job assignment deleted successfully for job ID: {}", jobId);
    }
}
