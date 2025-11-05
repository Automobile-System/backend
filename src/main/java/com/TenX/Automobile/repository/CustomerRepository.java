package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerId(String customerId);
    boolean existsByCustomerId(String customerId);

    @Query("Select c.customerId from Customer c")
    List<String> findAllCustomerIds();
}