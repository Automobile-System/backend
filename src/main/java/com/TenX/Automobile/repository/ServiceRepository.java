package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND (s.status IS NULL OR s.status != 'COMPLETED')")
    Long countActiveServicesByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.status = 'COMPLETED'")
    Long countCompletedServicesByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT COUNT(s) FROM Service s JOIN s.vehicles v WHERE v.customer.id = :customerId AND s.arrivingDate > :currentDate")
    Long countUpcomingServicesByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);

}
