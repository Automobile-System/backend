package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.model.entity.Project;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Find project by ID
    Optional<Project> findByProjectId(Long projectId);

    // Find projects by title (search)
    List<Project> findByTitleContainingIgnoreCase(String title);

    // Find projects by status
    List<Project> findByStatus(String status);

    // Find projects created within date range
    @Query("SELECT p FROM Project p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Project> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
