package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.enums.JobType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find jobs by type
    List<Job> findByType(JobType type);

    // Find job by type and typeId
    Optional<Job> findByTypeAndTypeId(JobType type, Long typeId);

    // Find all jobs for a vehicle
    @Query("SELECT j FROM Job j WHERE j.vehicle.v_Id = :vehicleId")
    List<Job> findByVehicleId(@Param("vehicleId") UUID vehicleId);

    // Find jobs by vehicle and type
    @Query("SELECT j FROM Job j WHERE j.vehicle.v_Id = :vehicleId AND j.type = :type")
    List<Job> findByVehicleIdAndType(@Param("vehicleId") UUID vehicleId, @Param("type") JobType type);

    // Find jobs by vehicle and status
    @Query("SELECT j FROM Job j WHERE j.vehicle.v_Id = :vehicleId AND j.status = :status")
    List<Job> findByVehicleIdAndStatus(@Param("vehicleId") UUID vehicleId, @Param("status") String status);

    // Count jobs by date
    @Query("SELECT COUNT(j) FROM Job j WHERE DATE(j.arrivingDate) = DATE(:date)")
    Long countJobsByDate(@Param("date") LocalDateTime date);

    // Find jobs by date range
    @Query("SELECT j FROM Job j WHERE j.arrivingDate BETWEEN :startDate AND :endDate")
    List<Job> findJobsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count jobs by date range (for charts)
    @Query("SELECT DATE(j.arrivingDate) as date, COUNT(j) as count FROM Job j " +
           "WHERE j.arrivingDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(j.arrivingDate)")
    List<Object[]> countJobsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find jobs by customer (through vehicle)
    @Query("SELECT j FROM Job j WHERE j.vehicle.customer.id = :customerId")
    List<Job> findByCustomerId(@Param("customerId") UUID customerId);

    // Find jobs by customer and type
    @Query("SELECT j FROM Job j WHERE j.vehicle.customer.id = :customerId AND j.type = :type")
    List<Job> findByCustomerIdAndType(@Param("customerId") UUID customerId, @Param("type") JobType type);

    // Find jobs by customer and status
    @Query("SELECT j FROM Job j WHERE j.vehicle.customer.id = :customerId AND j.status = :status")
    List<Job> findByCustomerIdAndStatus(@Param("customerId") UUID customerId, @Param("status") String status);

    // Count active jobs by customer
    @Query("SELECT COUNT(j) FROM Job j WHERE j.vehicle.customer.id = :customerId AND (j.status IS NULL OR j.status != 'COMPLETED')")
    Long countActiveJobsByCustomerId(@Param("customerId") UUID customerId);

    // Count completed jobs by customer
    @Query("SELECT COUNT(j) FROM Job j WHERE j.vehicle.customer.id = :customerId AND j.status = 'COMPLETED'")
    Long countCompletedJobsByCustomerId(@Param("customerId") UUID customerId);

    // Find upcoming jobs by customer
    @Query("SELECT j FROM Job j WHERE j.vehicle.customer.id = :customerId AND j.arrivingDate > :currentDate ORDER BY j.arrivingDate ASC")
    List<Job> findUpcomingJobsByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);

    // Count upcoming jobs by customer
    @Query("SELECT COUNT(j) FROM Job j WHERE j.vehicle.customer.id = :customerId AND j.arrivingDate > :currentDate")
    Long countUpcomingJobsByCustomerId(@Param("customerId") UUID customerId, @Param("currentDate") LocalDateTime currentDate);
}
