package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.ManageAssignJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    
    /**
     * Count jobs assigned to an employee in current month
     */
    @Query("SELECT COUNT(maj) FROM ManageAssignJob maj " +
           "WHERE maj.employee.id = :employeeId " +
           "AND YEAR(maj.created_at) = YEAR(CURRENT_DATE) " +
           "AND MONTH(maj.created_at) = MONTH(CURRENT_DATE)")
    Long countJobsAssignedThisMonth(@Param("employeeId") UUID employeeId);
    
    /**
     * Count all jobs assigned to an employee
     */
    @Query("SELECT COUNT(maj) FROM ManageAssignJob maj " +
           "WHERE maj.employee.id = :employeeId")
    Long countTotalJobsAssigned(@Param("employeeId") UUID employeeId);
}
