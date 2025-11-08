package com.TenX.Automobile.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.model.dto.request.JobRequest;
import com.TenX.Automobile.model.dto.response.JobResponse;
import com.TenX.Automobile.model.entity.Job;
import com.TenX.Automobile.model.entity.Vehicle;
import com.TenX.Automobile.model.enums.JobType;
import com.TenX.Automobile.repository.JobRepository;
import com.TenX.Automobile.repository.ProjectRepository;
import com.TenX.Automobile.repository.ServiceRepository;
import com.TenX.Automobile.repository.VehicleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final VehicleRepository vehicleRepository;

    public List<JobResponse> findAll() {
        return jobRepository.findAll().stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());
    }

    public JobResponse findById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return convertToJobResponse(job);
    }

    public JobResponse create(JobRequest request) {
        // Validate vehicle exists
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + request.getVehicleId()));

        // Validate service or project exists
        if (JobType.SERVICE.equals(request.getType())) {
            serviceRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Service not found with id: " + request.getTypeId()));
        } else if (JobType.PROJECT.equals(request.getType())) {
            projectRepository.findById(request.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + request.getTypeId()));
        }

        // Create job
        Job job = new Job();
        job.setType(request.getType());
        job.setTypeId(request.getTypeId());
        job.setVehicle(vehicle);
        job.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        job.setArrivingDate(request.getArrivingDate());
        job.setCost(request.getCost());

        Job savedJob = jobRepository.save(job);
        log.info("Created job with id: {}", savedJob.getJobId());
        
        return convertToJobResponse(savedJob);
    }

    public JobResponse update(Long id, JobRequest request) {
        Job existing = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getArrivingDate() != null) {
            existing.setArrivingDate(request.getArrivingDate());
        }
        if (request.getCost() != null) {
            existing.setCost(request.getCost());
        }

        Job updatedJob = jobRepository.save(existing);
        log.info("Updated job with id: {}", id);
        
        return convertToJobResponse(updatedJob);
    }

    public void delete(Long id) {
        if (!jobRepository.existsById(id)) {
            throw new RuntimeException("Job not found with id: " + id);
        }
        jobRepository.deleteById(id);
        log.info("Deleted job with id: {}", id);
    }

    private JobResponse convertToJobResponse(Job job) {
        JobResponse response = JobResponse.builder()
                .jobId(job.getJobId())
                .type(job.getType())
                .typeId(job.getTypeId())
                .vehicleId(job.getVehicle() != null ? job.getVehicle().getV_Id() : null)
                .vehicleRegistration(job.getVehicle() != null ? job.getVehicle().getRegistration_No() : null)
                .status(job.getStatus())
                .arrivingDate(job.getArrivingDate())
                .cost(job.getCost())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();

        // Add service or project details
        if (JobType.SERVICE.equals(job.getType())) {
            serviceRepository.findById(job.getTypeId()).ifPresent(service -> {
                response.setServiceDetails(JobResponse.ServiceDetails.builder()
                        .serviceId(service.getServiceId())
                        .title(service.getTitle())
                        .description(service.getDescription())
                        .category(service.getCategory())
                        .imageUrl(service.getImageUrl())
                        .estimatedHours(service.getEstimatedHours())
                        .serviceCost(service.getCost())
                        .build());
            });
        } else if (JobType.PROJECT.equals(job.getType())) {
            projectRepository.findById(job.getTypeId()).ifPresent(project -> {
                response.setProjectDetails(JobResponse.ProjectDetails.builder()
                        .projectId(project.getProjectId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .estimatedHours(project.getEstimatedHours())
                        .projectCost(project.getCost())
                        .projectStatus(project.getStatus())
                        .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                        .build());
            });
        }

        return response;
    }
}
