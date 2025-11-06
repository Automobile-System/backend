package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByAdminId(String adminId);
    boolean existsByEmail(String email);
}
