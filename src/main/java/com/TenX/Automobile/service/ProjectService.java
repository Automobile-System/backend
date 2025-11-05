package com.TenX.Automobile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.entity.Project;
import com.TenX.Automobile.entity.Task;
import com.TenX.Automobile.repository.ProjectRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Project create(Project project) {
        // ensure tasks point back to project
        if (project.getTasks() != null) {
            for (Task t : project.getTasks()) {
                t.setProject(project);
            }
        }
        return projectRepository.save(project);
    }

    public Project update(Long id, Project project) {
        Project existing = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        existing.setTitle(project.getTitle());
        existing.setDescription(project.getDescription());
        existing.setEstimatedHours(project.getEstimatedHours());
        existing.setStatus(project.getStatus());
        existing.setArrivingDate(project.getArrivingDate());
        existing.setCost(project.getCost());

        // replace tasks if provided
        if (project.getTasks() != null) {
            // delete tasks that are no longer present
            existing.getTasks().clear();
            for (Task t : project.getTasks()) {
                t.setProject(existing);
                existing.getTasks().add(t);
            }
        }

        return projectRepository.save(existing);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
