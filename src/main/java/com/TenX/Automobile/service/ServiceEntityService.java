package com.TenX.Automobile.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.TenX.Automobile.dto.request.ServiceRequest;
import com.TenX.Automobile.dto.response.ServiceResponse;
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

    public List<ServiceResponse> findAll() {
        return serviceRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponse findById(Long id) {
        com.TenX.Automobile.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        return convertToResponse(service);
    }

    public ServiceResponse create(ServiceRequest request) {
        com.TenX.Automobile.entity.Service service = com.TenX.Automobile.entity.Service.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .estimatedHours(request.getEstimatedHours())
                .cost(request.getCost())
                .build();
        
        com.TenX.Automobile.entity.Service saved = serviceRepository.save(service);
        log.info("Service created successfully with ID: {}", saved.getServiceId());
        return convertToResponse(saved);
    }

    public ServiceResponse update(Long id, ServiceRequest request) {
        com.TenX.Automobile.entity.Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setCategory(request.getCategory());
        existing.setImageUrl(request.getImageUrl());
        existing.setEstimatedHours(request.getEstimatedHours());
        existing.setCost(request.getCost());
        
        com.TenX.Automobile.entity.Service updated = serviceRepository.save(existing);
        log.info("Service updated successfully with ID: {}", updated.getServiceId());
        return convertToResponse(updated);
    }

    public void delete(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Service not found with id: " + id);
        }
        serviceRepository.deleteById(id);
        log.info("Service deleted successfully with ID: {}", id);
    }

    private ServiceResponse convertToResponse(com.TenX.Automobile.entity.Service service) {
        return ServiceResponse.builder()
                .serviceId(service.getServiceId())
                .title(service.getTitle())
                .description(service.getDescription())
                .category(service.getCategory())
                .imageUrl(service.getImageUrl())
                .estimatedHours(service.getEstimatedHours())
                .cost(service.getCost())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}
