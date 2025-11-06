package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "projects")
@PrimaryKeyJoinColumn(name = "project_id", referencedColumnName = "job_id")
public class Project extends Job {

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

}
 
