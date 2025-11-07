package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find all tasks assigned to an employee via ManageAssignJob
     * Returns ALL tasks including completed, sorted by deadline ASC
     * DISTINCT removed to avoid PostgreSQL ORDER BY issues - duplicates prevented by data model
     */
    @Query("SELECT t FROM Task t " +
           "JOIN t.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "ORDER BY j.arrivingDate ASC")
    List<Task> findTasksByEmployeeId(@Param("employeeId") UUID employeeId);
    
    /**
     * Find tasks assigned to an employee filtered by status
     * Returns tasks sorted by deadline ASC
     * DISTINCT removed to avoid PostgreSQL ORDER BY issues - duplicates prevented by data model
     */
    @Query("SELECT t FROM Task t " +
           "JOIN t.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND t.status = :status " +
           "ORDER BY j.arrivingDate ASC")
    List<Task> findTasksByEmployeeIdAndStatus(
        @Param("employeeId") UUID employeeId,
        @Param("status") String status
    );
    
    /**
     * Find tasks assigned to an employee with date range for calendar
     * DISTINCT removed to avoid PostgreSQL ORDER BY issues - duplicates prevented by data model
     */
    @Query("SELECT t FROM Task t " +
           "JOIN t.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND j.arrivingDate >= :startDate " +
           "AND j.arrivingDate <= :endDate " +
           "ORDER BY j.arrivingDate ASC")
    List<Task> findTasksByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count tasks assigned to an employee due today
     * Uses Task's direct job relationship for consistent query structure
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Task t " +
           "JOIN t.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND CAST(j.arrivingDate AS date) = CURRENT_DATE")
    Long countTasksTodayByEmployeeId(@Param("employeeId") UUID employeeId);
    
    /**
     * Count completed tasks assigned to an employee in current month
     * Uses Task's direct job relationship for consistent query structure
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Task t " +
           "JOIN t.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND t.status = 'COMPLETED' " +
           "AND YEAR(t.completedAt) = YEAR(CURRENT_DATE) " +
           "AND MONTH(t.completedAt) = MONTH(CURRENT_DATE)")
    Long countCompletedTasksThisMonthByEmployeeId(@Param("employeeId") UUID employeeId);
}
