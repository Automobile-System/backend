package com.TenX.Automobile.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TenX.Automobile.model.dto.request.JobRequest;
import com.TenX.Automobile.model.dto.response.JobResponse;
import com.TenX.Automobile.service.JobService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> jobs = jobService.findAll();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getJobById(@PathVariable Long id) {
        try {
            JobResponse job = jobService.findById(id);
            return ResponseEntity.ok(job);
        } catch (RuntimeException e) {
            log.error("Job not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Job not found with id: " + id);
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createJob(@Valid @RequestBody JobRequest request) {
        try {
            JobResponse created = jobService.create(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create job", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        try {
            JobResponse updated = jobService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Failed to update job with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            jobService.delete(id);
            return ResponseEntity.ok("Job deleted successfully");
        } catch (RuntimeException e) {
            log.error("Failed to delete job with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
