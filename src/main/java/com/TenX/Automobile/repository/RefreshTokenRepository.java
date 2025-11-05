package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.UserEntity;
import com.TenX.Automobile.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Refresh Tokens
 * Supports token rotation and cleanup operations
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Find all valid tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findAllValidTokensByUser(@Param("user") UserEntity user, @Param("now") Instant now);
    
    /**
     * Find all tokens by user
     */
    List<RefreshToken> findAllByUser(UserEntity user);
    
    /**
     * Check if token exists and is valid
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    boolean existsByTokenAndValid(@Param("token") String token, @Param("now") Instant now);
    
    /**
     * Delete all tokens for a user
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") UserEntity user);
    
    /**
     * Revoke all tokens for a user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllUserTokens(@Param("user") UserEntity user);
    
    /**
     * Delete all expired tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP")
    void deleteAllExpiredTokens();
    
    /**
     * Delete all revoked tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :date")
    void deleteRevokedTokensOlderThan(@Param("date") Instant date);
}