package com.TenX.Automobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.TenX.Automobile.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

}
