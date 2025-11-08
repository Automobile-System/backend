package com.TenX.Automobile.repository;

import com.TenX.Automobile.model.entity.Message;
import com.TenX.Automobile.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages for a conversation, ordered by timestamp ASC
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.conversationId = :conversationId " +
           "ORDER BY m.timestamp ASC")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);
    
    /**
     * Find the last message in a conversation
     */
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.conversationId = :conversationId " +
           "ORDER BY m.timestamp DESC")
    List<Message> findLastMessagesByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Delete all messages sent by a user
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.sender = :user")
    void deleteBySender(@Param("user") UserEntity user);
}
