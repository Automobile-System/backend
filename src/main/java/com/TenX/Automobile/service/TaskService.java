package com.TenX.Automobile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.entity.Project;
import com.TenX.Automobile.entity.Task;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.TaskRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task create(Task task) {
        if (task.getProject() != null && task.getProject().getJobId() != null) {
            Project p = projectRepository.findById(task.getProject().getJobId()).orElseThrow(() -> new RuntimeException("Project not found"));
            task.setProject(p);
        }
        return taskRepository.save(task);
    }

    public Task update(Long id, Task task) {
        Task existing = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        existing.setTaskTitle(task.getTaskTitle());
        existing.setTaskDescription(task.getTaskDescription());
        existing.setEstimatedHours(task.getEstimatedHours());
        existing.setStatus(task.getStatus());
        existing.setCompletedAt(task.getCompletedAt());
        if (task.getProject() != null && task.getProject().getJobId() != null) {
            Project p = projectRepository.findById(task.getProject().getJobId()).orElseThrow(() -> new RuntimeException("Project not found"));
            existing.setProject(p);
        }
        return taskRepository.save(existing);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
