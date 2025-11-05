package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.Data;

@Data
@Entity
@Table(name="Notifications")
public class Notifications {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "noti_Id", updatable = false, nullable = false)
  private Long noti_Id;

  @Column (name="isRead")
  private boolean isRead;

  @Column (name="message")
  private String message;

  @Column (name="type")
  private String type;

}
