package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "v_Id")
  @org.hibernate.annotations.UuidGenerator
  private UUID v_Id;

  @Column(name="registration_No")
  private String registration_No;


  @Column(name="brand_name")
  private String brandName;

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

  // One vehicle can have multiple jobs (jobs can be SERVICE or PROJECT type)
  @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Job> jobs = new ArrayList<>();

}
