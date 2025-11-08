package com.TenX.Automobile.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.TenX.Automobile.model.enums.JobType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@Table(name = "jobs")
@Inheritance(strategy = InheritanceType.JOINED)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id", updatable = false, nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private JobType type;

    @Column(name = "type_id", nullable = false)
    private Long typeId;

    @Column(name = "status")
    private String status;

    @Column(name = "arriving_date")
    private LocalDateTime arrivingDate;

    @Column(name = "cost")
    private BigDecimal cost;

    // Each job belongs to one vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper method to set Service type and ID
    public void setServiceType(Service service) {
        this.type = JobType.SERVICE;
        this.typeId = service.getServiceId();
    }

    // Helper method to set Project type and ID
    public void setProjectType(Project project) {
        this.type = JobType.PROJECT;
        this.typeId = project.getProjectId();
    }

    // Check if this job is a service job
    public boolean isServiceJob() {
        return JobType.SERVICE.equals(this.type);
    }

    // Check if this job is a project job
    public boolean isProjectJob() {
        return JobType.PROJECT.equals(this.type);
    }

}
 
