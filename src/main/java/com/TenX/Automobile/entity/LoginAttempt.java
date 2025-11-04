package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to track login attempts for security purposes
 * Helps prevent brute force attacks with account lockout mechanism
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_ip_address", columnList = "ip_address"),
        @Index(name = "idx_attempt_time", columnList = "attempt_time")
})
@EntityListeners(AuditingEntityListener.class)
public class LoginAttempt {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "attempt_time", nullable = false, updatable = false)
    private LocalDateTime attemptTime;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
