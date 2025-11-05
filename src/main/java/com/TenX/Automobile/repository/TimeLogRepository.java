package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    @Query("SELECT tl FROM TimeLog tl WHERE tl.job.jobId = :jobId")
    List<TimeLog> findByJobId(@Param("jobId") Long jobId);
}
