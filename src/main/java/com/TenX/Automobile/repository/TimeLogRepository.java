package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
     * No date range filter - returns all time logs
     */
    @Query("SELECT DISTINCT tl FROM TimeLog tl " +
           "JOIN tl.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "ORDER BY tl.startTime DESC")
    List<TimeLog> findTimeLogsByEmployeeId(@Param("employeeId") UUID employeeId);

    /**
     * Find all time logs for an employee via ManageAssignJob
     * Filters by date range using LocalDateTime to avoid PostgreSQL parameter type issues
     */
    @Query("SELECT DISTINCT tl FROM TimeLog tl " +
           "JOIN tl.job j " +
           "JOIN ManageAssignJob maj ON maj.job.jobId = j.jobId " +
           "WHERE maj.employee.id = :employeeId " +
           "AND tl.startTime >= :startDateTime " +
           "AND tl.startTime <= :endDateTime " +
           "ORDER BY tl.startTime DESC")
    List<TimeLog> findTimeLogsByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime
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
}
