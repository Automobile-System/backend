package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Customer;
import com.TenX.Automobile.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    @Query("SELECT v FROM Vehicle v WHERE v.customer.id = :customerId")
    List<Vehicle> findAllByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT v FROM Vehicle v WHERE v.v_Id = :vehicleId AND v.customer.id = :customerId")
    Optional<Vehicle> findByIdAndCustomerId(@Param("vehicleId") UUID vehicleId, @Param("customerId") UUID customerId);

    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vehicle v WHERE v.registration_No = :registrationNo")
    boolean existsByRegistrationNo(@Param("registrationNo") String registrationNo);
    
    @Query("SELECT v FROM Vehicle v WHERE v.registration_No = :registrationNo")
    Optional<Vehicle> findByRegistration_No(@Param("registrationNo") String registrationNo);
    List<Vehicle> findByCustomer_Id(UUID customerId);
    List<Vehicle> findByCustomer(Customer customer);
    List<Vehicle> findByBrandName(String brandName);
    List<Vehicle> findByModel(String model);
    List<Vehicle> findByCapacityGreaterThanEqual(int capacity);
    long countByCustomer_Id(UUID customerId);
}
