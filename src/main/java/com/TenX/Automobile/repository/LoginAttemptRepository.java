package com.TenX.Automobile.repository;

import com.TenX.Automobile.model.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing login attempts
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    /**
     * Count failed login attempts for email since specified time
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.attemptTime > :since")
    long countFailedAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Count failed login attempts from IP address since specified time
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.attemptTime > :since")
    long countFailedAttemptsFromIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Find all login attempts for email
     */
    List<LoginAttempt> findByEmailOrderByAttemptTimeDesc(String email);

    /**
     * Find recent login attempts for email
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.attemptTime > :since ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findRecentAttemptsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Delete old login attempts
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.attemptTime < :before")
    void deleteAttemptsOlderThan(@Param("before") LocalDateTime before);

    /**
     * Delete login attempts for specific email
     */
    @Modifying
    void deleteByEmail(String email);
}
