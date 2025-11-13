package com.TenX.Automobile.controller;

import java.util.List;
 import java.util.Map;

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

import com.TenX.Automobile.model.dto.request.ServiceRequest;
import com.TenX.Automobile.model.dto.response.ServiceResponse;
import com.TenX.Automobile.service.ServiceEntityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceEntityService serviceEntityService;

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> services = serviceEntityService.findAll();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getService(@PathVariable Long id) {
        try {
            ServiceResponse service = serviceEntityService.findById(id);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            log.error("Service not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Service not found with id: " + id);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createService(@Valid @RequestBody ServiceRequest request) {
        try {
            ServiceResponse created = serviceEntityService.create(request);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create service", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        try {
            ServiceResponse updated = serviceEntityService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Failed to update service with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            serviceEntityService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Service deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Failed to delete service with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
