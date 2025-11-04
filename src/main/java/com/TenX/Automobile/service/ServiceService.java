package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.Service;
import com.TenX.Automobile.repository.ServiceRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
@Transactional
public class ServiceService {

    private final ServiceRepository repository;

    public ServiceService(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<Service> getAll() {
        return repository.findAll();
    }

    public Service getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    public Service create(Service service) {
        // Ensure id is null so the UUID generator will run
        service.setServiceId(null);
        return repository.save(service);
    }

    public Service update(UUID id, Service incoming) {
        Service existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        existing.setCategory(incoming.getCategory());
        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setCost(incoming.getCost());
        existing.setImageUrl(incoming.getImageUrl());

        return repository.save(existing);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found");
        }
        repository.deleteById(id);
    }
}
