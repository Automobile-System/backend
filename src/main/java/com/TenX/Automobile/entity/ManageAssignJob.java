package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "manage_assign_job")
public class ManageAssignJob {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="mangeAssignJob_Id" ,unique = true, nullable = false)
  @org.hibernate.annotations.UuidGenerator
  private UUID manageAssignJob_Id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private Job job;

    // The employee receiving the job
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

    // The manager assigning the job (also an Employee, but of type MANAGER)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id", nullable = false)
  private Employee manager;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;


}
