package com.TenX.Automobile.repository;


import com.TenX.Automobile.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByRegistration_No(String registrationNo);
    boolean existsByRegistration_No(String registrationNo);
    List<Vehicle> findByCustomer_Id(UUID customerId);
    List<Vehicle> findByBrand_name(String brandName);
    List<Vehicle> findByModel(String model);
    List<Vehicle> findByCapacityGreaterThanEqual(int capacity);
    long countByCustomer_Id(UUID customerId);
}
