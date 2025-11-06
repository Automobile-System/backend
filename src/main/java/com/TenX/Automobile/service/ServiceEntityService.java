package com.TenX.Automobile.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.repository.ServiceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;

    public List<com.TenX.Automobile.entity.Service> findAll() {
        return serviceRepository.findAll();
    }

    public Optional<com.TenX.Automobile.entity.Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    public com.TenX.Automobile.entity.Service create(com.TenX.Automobile.entity.Service service) {
        return serviceRepository.save(service);
    }

    public com.TenX.Automobile.entity.Service update(Long id, com.TenX.Automobile.entity.Service service) {
        com.TenX.Automobile.entity.Service existing = serviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Service not found"));
        existing.setTitle(service.getTitle());
        existing.setDescription(service.getDescription());
        existing.setCategory(service.getCategory());
        existing.setImageUrl(service.getImageUrl());
        existing.setEstimatedHours(service.getEstimatedHours());
        existing.setStatus(service.getStatus());
        existing.setArrivingDate(service.getArrivingDate());
        existing.setCost(service.getCost());
        return serviceRepository.save(existing);
    }

    public void delete(Long id) {
        serviceRepository.deleteById(id);
    }
}
