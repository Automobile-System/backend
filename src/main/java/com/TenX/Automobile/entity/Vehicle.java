package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "vehicle")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "v_id", updatable = false, nullable = false)
  private UUID v_Id;

  @Column(name = "registration_no", nullable = false, unique = true)
  private String registration_No;

  @Column(name = "brand_name")
  private String brand_name;

  @Column(name = "model")
  private String model;

  @Column(name = "capacity")
  private int capacity;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  private String createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;


  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Job> jobs = new ArrayList<>();
}
