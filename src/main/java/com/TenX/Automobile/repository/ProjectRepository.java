package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Project;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT COUNT(p) FROM Project p JOIN p.vehicles v WHERE v.customer.id = :customerId AND (p.status IS NULL OR p.status != 'COMPLETED')")
    Long countActiveProjectsByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(p) FROM Project p JOIN p.vehicles v WHERE v.customer.id = :customerId AND p.status = 'COMPLETED'")
    Long countCompletedProjectsByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(p) FROM Project p JOIN p.vehicles v WHERE v.customer.id = :customerId AND p.arrivingDate > :currentDate")
    Long countUpcomingProjectsByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);

}
