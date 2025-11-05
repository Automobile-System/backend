package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

}
