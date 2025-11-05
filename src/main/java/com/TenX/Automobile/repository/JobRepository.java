package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Job;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT COUNT(j) FROM Job j WHERE DATE(j.arrivingDate) = DATE(:date)")
    Long countJobsByDate(@Param("date") LocalDateTime date);

    @Query("SELECT j FROM Job j WHERE j.arrivingDate BETWEEN :startDate AND :endDate")
    List<Job> findJobsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(j.arrivingDate) as date, COUNT(j) as count FROM Job j " +
           "WHERE j.arrivingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(j.arrivingDate)")
    List<Object[]> countJobsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
