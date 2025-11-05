package com.TenX.Automobile.controller;

import java.util.List;
import java.util.Optional;

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

import com.TenX.Automobile.entity.Service;
import com.TenX.Automobile.service.ServiceEntityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceEntityService serviceEntityService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceEntityService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getService(@PathVariable Long id) {
        Optional<Service> s = serviceEntityService.findById(id);
        if (s.isPresent()) return ResponseEntity.ok(s.get());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createService(@Valid @RequestBody Service service) {
        try {
            Service saved = serviceEntityService.create(service);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Failed to create service", e);
            return new ResponseEntity<>("Failed to create service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Service service) {
        Optional<Service> opt = serviceEntityService.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        Service updated = serviceEntityService.update(id, service);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        Optional<Service> opt = serviceEntityService.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        serviceEntityService.delete(id);
        return ResponseEntity.ok("Service deleted");
    }

}
