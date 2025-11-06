package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND (s.status IS NULL OR s.status != 'COMPLETED')")
    Long countActiveServicesByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.status = 'COMPLETED'")
    Long countCompletedServicesByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.arrivingDate > :currentDate")
    Long countUpcomingServicesByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT s FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.createdAt >= :startDate ORDER BY s.createdAt")
    List<Service> findServicesByCustomerIdAndDateRange(@Param("customerId") UUID customerId, @Param("startDate") LocalDateTime startDate);

    // Get all services for a customer
    @Query("SELECT DISTINCT s FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId ORDER BY s.createdAt DESC")
    List<Service> findAllByCustomerId(@Param("customerId") UUID customerId);

    // Get active services (not completed)
    @Query("SELECT DISTINCT s FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.status != 'COMPLETED' ORDER BY s.createdAt DESC")
    List<Service> findActiveServicesByCustomerId(@Param("customerId") UUID customerId);

    // Get completed services
    @Query("SELECT DISTINCT s FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.status = 'COMPLETED' ORDER BY s.createdAt DESC")
    List<Service> findCompletedServicesByCustomerId(@Param("customerId") UUID customerId);

    // Get upcoming services
    @Query("SELECT DISTINCT s FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.arrivingDate > :currentDate ORDER BY s.arrivingDate ASC")
    List<Service> findUpcomingServicesByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);

    // Get service by ID and customer ID
    @Query("SELECT DISTINCT s FROM Service s LEFT JOIN FETCH s.vehicles v LEFT JOIN FETCH s.tasks LEFT JOIN FETCH v.customer WHERE s.jobId = :serviceId AND v.customer.id = :customerId")
    Optional<Service> findByIdAndCustomerId(@Param("serviceId") Long serviceId, @Param("customerId") UUID customerId);
}

