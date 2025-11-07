package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Notification;
import com.TenX.Automobile.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // Get all notifications for a user
  List<Notification> findByUser_Id(UUID userId);

  // Get unread notifications for a user
  List<Notification> findByUser_IdAndIsRead(UUID userId, Boolean isRead);

  // Get notifications for a specific job
  List<Notification> findByJob_JobId(Long jobId);

  // Get notifications for a user related to a specific job
  List<Notification> findByUser_IdAndJob_JobId(UUID userId, Long jobId);

  // Count unread notifications for a user
  Long countByUser_IdAndIsRead(UUID userId, Boolean isRead);

  // Delete all notifications for a user
  @Modifying
  @Query("DELETE FROM Notification n WHERE n.user = :user")
  void deleteByUser(@Param("user") UserEntity user);
}
