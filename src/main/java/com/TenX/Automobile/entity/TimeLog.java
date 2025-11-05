package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "time_log")
public class TimeLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long logId;

  @Column(name = "hours_worked")
  private Double hoursWorked;

  @Column(name = "description")
  private String description;

  @Column(name = "start_time")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", unique = true)
  private Job job;

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
