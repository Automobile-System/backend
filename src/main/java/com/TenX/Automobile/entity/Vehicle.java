package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;

import java.util.UUID;

@Data
@Entity

public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "v_Id")
  @org.hibernate.annotations.UuidGenerator
  private UUID v_Id;

  @Column(name="registration_No")
  private String registration_No;


  @Column(name="brand_name")
  private String brand_name;

  @Column(name="model")
  private String model;

  @Column(name="capacity")
  private int capacity;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private String createdBy;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

}
