package com.TenX.Automobile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.entity.Job;
import com.TenX.Automobile.repository.JobRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {

    private final JobRepository jobRepository;

    public List<Job> findAll() {
        return jobRepository.findAll();
    }

    public Optional<Job> findById(Long id) {
        return jobRepository.findById(id);
    }

    public Job create(Job job) {
        return jobRepository.save(job);
    }

    public Job update(Long id, Job job) {
        Job existing = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        existing.setStatus(job.getStatus());
        existing.setArrivingDate(job.getArrivingDate());
        existing.setCost(job.getCost());
        return jobRepository.save(existing);
    }

    public void delete(Long id) {
        jobRepository.deleteById(id);
    }
}
