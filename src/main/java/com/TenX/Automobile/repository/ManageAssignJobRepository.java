package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Employee;
import com.TenX.Automobile.entity.ManageAssignJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManageAssignJobRepository extends JpaRepository<ManageAssignJob, UUID> {
    Optional<ManageAssignJob> findByJob_JobId(Long jobId);
    List<ManageAssignJob> findByManager_Id(UUID managerId);
    List<ManageAssignJob> findByEmployee_Id(UUID employeeId);
    List<ManageAssignJob> findByManager_IdAndEmployee_Id(UUID managerId, UUID employeeId);
    boolean existsByJob_JobId(Long jobId);

    @Query("SELECT m FROM ManageAssignJob m WHERE m.employee.id = :employeeId")
    List<ManageAssignJob> findByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT m FROM ManageAssignJob m WHERE m.job.jobId = :jobId")
    List<ManageAssignJob> findByJobJobId(@Param("jobId") Long jobId);

    @Query("SELECT COUNT(m) FROM ManageAssignJob m WHERE m.employee.id = :employeeId AND " +
           "(m.job.status IS NULL OR m.job.status NOT IN ('COMPLETED', 'CANCELLED'))")
    Long countActiveJobsByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT m FROM ManageAssignJob m WHERE m.employee.id = :employeeId AND m.job.status = 'COMPLETED' " +
           "ORDER BY m.job.updatedAt DESC")
    List<ManageAssignJob> findCompletedJobsByEmployeeId(@Param("employeeId") UUID employeeId);
    /**
     * Count jobs assigned to an employee in current month
     */
    @Query("SELECT COUNT(maj) FROM ManageAssignJob maj " +
           "WHERE maj.employee.id = :employeeId " +
           "AND YEAR(maj.createdAt) = YEAR(CURRENT_DATE) " +
           "AND MONTH(maj.createdAt) = MONTH(CURRENT_DATE)")
    Long countJobsAssignedThisMonth(@Param("employeeId") UUID employeeId);

    @Query("SELECT m FROM ManageAssignJob m WHERE m.job.arrivingDate >= :startDate AND m.job.arrivingDate <= :endDate")
    List<ManageAssignJob> findJobsByDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    /**
     * Count all jobs assigned to an employee
     */
    @Query("SELECT COUNT(maj) FROM ManageAssignJob maj " +
           "WHERE maj.employee.id = :employeeId")
    Long countTotalJobsAssigned(@Param("employeeId") UUID employeeId);

    /**
     * Delete all job assignments for an employee
     */
    @Modifying
    @Query("DELETE FROM ManageAssignJob maj WHERE maj.employee = :employee")
    void deleteByEmployee(@Param("employee") Employee employee);

    /**
     * Delete all job assignments by a manager
     */
    @Modifying
    @Query("DELETE FROM ManageAssignJob maj WHERE maj.manager = :manager")
    void deleteByManager(@Param("manager") Employee manager);
}

