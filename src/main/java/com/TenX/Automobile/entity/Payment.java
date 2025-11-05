package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_Id" ,updatable= false, nullable = false)
  private long p_Id;

  @Column(name="payment_amount")
  private double p_Amount;

  @Column(name="payment_type")
  private String p_Type;

  @CreatedDate
  @Column(name="created_at",nullable=false,updatable = false)
  private LocalDateTime p_Created_at;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", unique = true)
  private Job job;

}

