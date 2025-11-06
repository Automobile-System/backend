package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.ManageAssignJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ManageAssignJobRepository extends JpaRepository<ManageAssignJob, UUID> {
    
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
    
    @Query("SELECT m FROM ManageAssignJob m WHERE m.job.arrivingDate >= :startDate AND m.job.arrivingDate <= :endDate")
    List<ManageAssignJob> findJobsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
}

