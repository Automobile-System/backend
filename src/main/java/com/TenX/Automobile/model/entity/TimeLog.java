package com.TenX.Automobile.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "time_log", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "job_id", "start_time"})
})
public class TimeLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long logId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private Job job;

  @Column(name = "hours_worked")
  private Double hoursWorked;

  @Column(name = "description")
  private String description;

  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public void calculateHoursWorked() {
    if (startTime != null && endTime != null) {
      long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
      this.hoursWorked = minutes / 60.0;
    }
  }

  public boolean isActive() {
    return startTime != null && endTime == null;
  }

  public boolean isCompleted() {
    return startTime != null && endTime != null;
  }
}
