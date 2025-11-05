package com.TenX.Automobile.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    public void addTask(Task t) {
        this.tasks.add(t);
        t.setProject(this);
    }

    public void removeTask(Task t) {
        this.tasks.remove(t);
        t.setProject(null);
    }

}
 
