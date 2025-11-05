package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity

public class ManageAssignJob {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="mangeAssignJob_Id" ,unique = true, nullable = false)
  @org.hibernate.annotations.UuidGenerator
  private UUID manageAssignJob_Id;

  @CreatedDate
  @Column(name="created_at",nullable = false,updatable = false)
  private LocalDateTime created_at;

  @OneToOne(fetch =FetchType.LAZY)
  @JoinColumn(name="job_id",unique = true)
  private Job job;

  @OneToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="employee_id")
  private Employee employeeId;

}
