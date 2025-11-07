package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.Conversation;
import com.TenX.Automobile.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find all conversations for an employee
     */
    @Query("SELECT DISTINCT c FROM Conversation c " +
           "WHERE c.employee.id = :employeeId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findByEmployeeId(@Param("employeeId") UUID employeeId);
    
    /**
     * Find conversation between employee and participant
     */
    @Query("SELECT c FROM Conversation c " +
           "WHERE c.employee.id = :employeeId " +
           "AND c.participant.id = :participantId")
    Optional<Conversation> findByEmployeeIdAndParticipantId(
        @Param("employeeId") UUID employeeId,
        @Param("participantId") UUID participantId
    );
    
    /**
     * Find conversation by employee and vehicle
     */
    @Query("SELECT c FROM Conversation c " +
           "WHERE c.employee.id = :employeeId " +
           "AND c.vehicle.v_Id = :vehicleId")
    Optional<Conversation> findByEmployeeIdAndVehicleId(
        @Param("employeeId") UUID employeeId,
        @Param("vehicleId") UUID vehicleId
    );

    /**
     * Delete all conversations where user is a participant
     */
    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.participant = :user")
    void deleteByParticipant(@Param("user") UserEntity user);

    /**
     * Delete all conversations where user is an employee
     */
    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.employee = :user")
    void deleteByEmployee(@Param("user") UserEntity user);
}
