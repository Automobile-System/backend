package com.TenX.Automobile.controller;

import com.TenX.Automobile.entity.Service;
import com.TenX.Automobile.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<Service>> getAll() {
        return ResponseEntity.ok(serviceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Service> create(@Valid @RequestBody Service service) {
        Service created = serviceService.create(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Service> update(@PathVariable UUID id, @Valid @RequestBody Service service) {
        return ResponseEntity.ok(serviceService.update(id, service));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
