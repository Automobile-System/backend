package com.TenX.Automobile.repository;

import com.TenX.Automobile.model.entity.Employee;
import com.TenX.Automobile.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);

    @Query("Select e.employeeId from Employee e")
    List<String> getAllEmployeeIds();
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(:specialty IS NULL OR LOWER(e.specialty) = LOWER(:specialty)) AND " +
           "(:date IS NULL OR DATE(e.createdAt) = DATE(:date)) " +
           "ORDER BY e.createdAt DESC")
    List<Employee> findEmployeesByFilters(
        @Param("specialty") String specialty, 
        @Param("date") LocalDateTime date
    );

    @Query("SELECT e FROM Employee e WHERE " +
           "(:specialty IS NULL OR LOWER(e.specialty) = LOWER(:specialty)) AND " +
           "(:startDate IS NULL OR e.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR e.createdAt <= :endDate) " +
           "ORDER BY e.createdAt DESC")
    List<Employee> findEmployeesByDateRange(
        @Param("specialty") String specialty,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT e FROM Employee e WHERE :role MEMBER OF e.roles")
    List<Employee> findByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE :role MEMBER OF e.roles")
    Long countByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE :role MEMBER OF e.roles AND e.enabled = true")
    Long countActiveByRole(@Param("role") Role role);
}
