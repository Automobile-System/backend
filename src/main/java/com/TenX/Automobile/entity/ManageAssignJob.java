package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "manage_assign_job")
@EntityListeners(AuditingEntityListener.class)
public class ManageAssignJob {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="manageAssignJob_Id", unique = true, nullable = false, updatable = false)
  private UUID manageAssignJob_Id;

  @CreatedDate
  @Column(name="created_at", nullable = false, updatable = false)
  private LocalDateTime created_at;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="job_id", unique = true)
  private Job job;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="manager_id", nullable = false)
  private Employee managerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="employee_id", nullable = false)
  private Employee employeeId;
}
