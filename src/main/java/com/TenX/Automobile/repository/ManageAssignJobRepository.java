package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.ManageAssignJob;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
