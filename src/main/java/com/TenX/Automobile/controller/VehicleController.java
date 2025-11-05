package com.TenX.Automobile.controller;

import com.TenX.Automobile.entity.Vehicle;
import com.TenX.Automobile.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VehicleController {

  private final VehicleService vehicleService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<List<Vehicle>> getAllVehicles() {
    log.info("Fetching all vehicles");
    return ResponseEntity.ok(vehicleService.getAllVehicles());
  }

  @GetMapping("/{vehicleId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Vehicle> getVehicleById(@PathVariable UUID vehicleId) {
    log.info("Fetching vehicle with ID: {}", vehicleId);
    return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
  }

  @GetMapping("/registration/{registrationNo}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Vehicle> getVehicleByRegistrationNo(@PathVariable String registrationNo) {
    log.info("Fetching vehicle with registration number: {}", registrationNo);
    return ResponseEntity.ok(vehicleService.getVehicleByRegistrationNo(registrationNo));
  }

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<Vehicle>> getVehiclesByCustomerId(@PathVariable UUID customerId) {
    log.info("Fetching vehicles for customer ID: {}", customerId);
    return ResponseEntity.ok(vehicleService.getVehiclesByCustomerId(customerId));
  }

  @PostMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<Vehicle> createVehicle(@PathVariable UUID customerId, @RequestBody Vehicle vehicle) {
    log.info("Creating vehicle for customer ID: {}", customerId);
    return ResponseEntity.ok(vehicleService.createVehicle(vehicle, customerId));
  }

  @PutMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<Vehicle> updateVehicle(@PathVariable UUID vehicleId, @RequestBody Vehicle vehicleDetails) {
    log.info("Updating vehicle with ID: {}", vehicleId);
    return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, vehicleDetails));
  }

  @DeleteMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<Void> deleteVehicle(@PathVariable UUID vehicleId) {
    log.info("Deleting vehicle with ID: {}", vehicleId);
    vehicleService.deleteVehicle(vehicleId);
    return ResponseEntity.noContent().build();
  }
}
