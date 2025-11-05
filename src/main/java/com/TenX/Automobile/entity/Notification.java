package com.TenX.Automobile.entity;

import com.TenX.Automobile.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Notification", indexes = {
  @Index(name = "idx_user_id", columnList = "user_id"),
  @Index(name = "idx_job_id", columnList = "job_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "noti_id")
  private Long noti_id;

  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private NotificationType type; // SYSTEM, EMAIL, BOTH

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  // Relationship with UserEntity - will serialize with discriminator
  @ManyToOne(fetch = FetchType.EAGER)  // Changed to EAGER to load user details
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnoreProperties({"password", "refreshTokens", "loginAttempts", "hibernateLazyInitializer"})
  private UserEntity user;
  
  // Optional: Related to a specific job (nullable for system-wide notifications)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = true)
  private Job job;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
