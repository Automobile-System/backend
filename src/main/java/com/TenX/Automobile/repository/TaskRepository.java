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
    
    // Find tasks by project ID
    List<Task> findByProjectProjectId(Long projectId);
    
    // Find tasks by project ID and status
    List<Task> findByProjectProjectIdAndStatus(Long projectId, String status);
    
    /**
     * Find all tasks assigned to an employee via ManageAssignJob
     * Tasks belong to Projects, Projects are linked to Jobs, Jobs are assigned to Employees
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN t.project p " +
           "JOIN Job j ON j.typeId = p.projectId AND j.type = com.TenX.Automobile.enums.JobType.PROJECT " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "ORDER BY t.createdAt ASC")
    List<Task> findTasksByEmployeeId(@Param("employeeId") UUID employeeId);
    
    /**
     * Find tasks assigned to an employee filtered by status
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN t.project p " +
           "JOIN Job j ON j.typeId = p.projectId AND j.type = com.TenX.Automobile.enums.JobType.PROJECT " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND t.status = :status " +
           "ORDER BY t.createdAt ASC")
    List<Task> findTasksByEmployeeIdAndStatus(
        @Param("employeeId") UUID employeeId,
        @Param("status") String status
    );
    
    /**
     * Find tasks assigned to an employee with date range for calendar
     */
    @Query("SELECT DISTINCT t FROM Task t " +
           "JOIN t.project p " +
           "JOIN Job j ON j.typeId = p.projectId AND j.type = com.TenX.Automobile.enums.JobType.PROJECT " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND t.createdAt >= :startDate " +
           "AND t.createdAt <= :endDate")
    List<Task> findTasksByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Count tasks assigned to an employee due today
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Task t " +
           "JOIN t.project p " +
           "JOIN Job j ON j.typeId = p.projectId AND j.type = com.TenX.Automobile.enums.JobType.PROJECT " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND CAST(t.createdAt AS date) = CURRENT_DATE")
    Long countTasksTodayByEmployeeId(@Param("employeeId") UUID employeeId);
    
    /**
     * Count completed tasks assigned to an employee in current month
     */
    @Query("SELECT COUNT(DISTINCT t) FROM Task t " +
           "JOIN t.project p " +
           "JOIN Job j ON j.typeId = p.projectId AND j.type = com.TenX.Automobile.enums.JobType.PROJECT " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND t.status = 'COMPLETED' " +
           "AND YEAR(t.completedAt) = YEAR(CURRENT_DATE) " +
           "AND MONTH(t.completedAt) = MONTH(CURRENT_DATE)")
    Long countCompletedTasksThisMonthByEmployeeId(@Param("employeeId") UUID employeeId);
}
