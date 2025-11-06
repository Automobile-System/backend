package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.VehicleRequest;
import com.TenX.Automobile.dto.response.VehicleResponse;
import com.TenX.Automobile.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
    log.info("Fetching all vehicles");
    return ResponseEntity.ok(vehicleService.getAllVehicles());
  }

  @GetMapping("/{vehicleId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable UUID vehicleId) {
    log.info("Fetching vehicle with ID: {}", vehicleId);
    return ResponseEntity.ok(vehicleService.getVehicleById(vehicleId));
  }

  @GetMapping("/registration/{registrationNo}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<VehicleResponse> getVehicleByRegistrationNo(@PathVariable String registrationNo) {
    log.info("Fetching vehicle with registration number: {}", registrationNo);
    return ResponseEntity.ok(vehicleService.getVehicleByRegistrationNo(registrationNo));
  }

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<VehicleResponse>> getVehiclesByCustomerId(@PathVariable UUID customerId) {
    log.info("Fetching vehicles for customer ID: {}", customerId);
    return ResponseEntity.ok(vehicleService.getVehiclesByCustomerId(customerId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<VehicleResponse> createVehicle(
      @RequestParam UUID customerId, 
      @Valid @RequestBody VehicleRequest vehicleRequest) {
    log.info("Creating vehicle for customer ID: {}", customerId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(vehicleService.createVehicle(vehicleRequest, customerId));
  }

  @PutMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable UUID vehicleId, 
      @Valid @RequestBody VehicleRequest vehicleRequest) {
    log.info("Updating vehicle with ID: {}", vehicleId);
    return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, vehicleRequest));
  }

  @DeleteMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
  public ResponseEntity<Void> deleteVehicle(@PathVariable UUID vehicleId) {
    log.info("Deleting vehicle with ID: {}", vehicleId);
    vehicleService.deleteVehicle(vehicleId);
    return ResponseEntity.noContent().build();
  }
}