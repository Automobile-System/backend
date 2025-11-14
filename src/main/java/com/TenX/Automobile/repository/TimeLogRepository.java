package com.TenX.Automobile.repository;

import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    @Query("SELECT tl FROM TimeLog tl WHERE tl.job.jobId = :jobId")
    List<TimeLog> findByJobId(@Param("jobId") Long jobId);
    Optional<TimeLog> findByJob_JobId(Long jobId);

    /**
     * Find all time logs for an employee via ManageAssignJob
     * Filters by date range if provided
     */
    @Query("SELECT DISTINCT tl FROM TimeLog tl " +
           "JOIN tl.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND (:startDate IS NULL OR CAST(tl.startTime AS date) >= :startDate) " +
           "AND (:endDate IS NULL OR CAST(tl.startTime AS date) <= :endDate) " +
           "ORDER BY tl.startTime DESC")
    List<TimeLog> findTimeLogsByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total hours for an employee in current week
     */
    @Query("SELECT COALESCE(SUM(tl.hoursWorked), 0.0) FROM TimeLog tl " +
           "JOIN tl.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND YEARWEEK(tl.startTime, 1) = YEARWEEK(CURRENT_DATE, 1)")
    Double calculateWeeklyTotalHours(@Param("employeeId") UUID employeeId);

    /**
     * Calculate total hours for an employee in a date range
     */
    @Query("SELECT COALESCE(SUM(tl.hoursWorked), 0.0) FROM TimeLog tl " +
           "WHERE tl.employee.id = :employeeId " +
           "AND CAST(tl.startTime AS date) >= :startDate " +
           "AND CAST(tl.startTime AS date) <= :endDate")
    Double calculateTotalHoursByDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find time logs by employee and date range
     */
    @Query("SELECT tl FROM TimeLog tl " +
           "WHERE tl.employee.id = :employeeId " +
           "AND CAST(tl.startTime AS date) >= :startDate " +
           "AND CAST(tl.startTime AS date) <= :endDate " +
           "ORDER BY tl.startTime ASC")
    List<TimeLog> findByEmployeeAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Delete all time logs for an employee
     */
    @Modifying
    @Query("DELETE FROM TimeLog tl WHERE tl.employee = :employee")
    void deleteByEmployee(@Param("employee") Employee employee);

    /**
     * Find all time logs for a specific job and employee
     */
    @Query("SELECT tl FROM TimeLog tl WHERE tl.job.jobId = :jobId AND tl.employee.id = :employeeId")
    List<TimeLog> findByJobIdAndEmployeeId(@Param("jobId") Long jobId, @Param("employeeId") UUID employeeId);
}
