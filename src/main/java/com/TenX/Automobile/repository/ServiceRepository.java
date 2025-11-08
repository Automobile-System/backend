package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.model.entity.Service;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // Find services by category
    List<Service> findByCategory(String category);

    // Find services by title (search)
    List<Service> findByTitleContainingIgnoreCase(String title);

    // Find services by category and title
    List<Service> findByCategoryAndTitleContainingIgnoreCase(String category, String title);

    // Find service by ID
    Optional<Service> findByServiceId(Long serviceId);
}

