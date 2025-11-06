package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByJob_JobId(Long jobId);
    
    @Query("SELECT SUM(p.p_Amount) FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Double sumAmountByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.p_Amount) FROM Payment p WHERE MONTH(p.createdAt) = MONTH(:date) AND YEAR(p.createdAt) = YEAR(:date)")
    Double sumAmountByMonth(@Param("date") LocalDateTime date);
}

