package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

}
