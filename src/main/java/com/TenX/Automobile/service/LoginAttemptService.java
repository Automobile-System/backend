package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.LoginAttempt;
import com.TenX.Automobile.repository.LoginAttemptRepository;
import com.TenX.Automobile.repository.BaseUserRepository;
import com.TenX.Automobile.security.constants.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing login attempts and account lockout
 * Implements security measures to prevent brute force attacks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final BaseUserRepository userRepository;

    /**
     * Record a successful login attempt
     */
    @Transactional
    public void recordSuccessfulLogin(String email, HttpServletRequest request) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .ipAddress(getClientIpAddress(request))
                .success(true)
                .userAgent(getUserAgent(request))
                .build();

        loginAttemptRepository.save(attempt);
        log.debug("Recorded successful login for email: {}", email);

        // Reset failed attempts count for this user
        resetFailedAttempts(email);
    }

    /**
     * Record a failed login attempt
     */
    @Transactional
    public void recordFailedLogin(String email, String reason, HttpServletRequest request) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .ipAddress(getClientIpAddress(request))
                .success(false)
                .failureReason(reason)
                .userAgent(getUserAgent(request))
                .build();

        loginAttemptRepository.save(attempt);
        log.warn("Recorded failed login for email: {} - Reason: {}", email, reason);

        // Check if account should be locked
        checkAndLockAccount(email);
    }

    /**
     * Check if account is locked due to too many failed attempts
     */
    @Transactional(readOnly = true)
    public boolean isAccountLocked(String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (!user.isAccountNonLocked()) {
                        // Check if lock duration has passed
                        LocalDateTime lockTime = user.getLockedUntil();
                        if (lockTime != null && lockTime.isBefore(LocalDateTime.now())) {
                            // Lock duration has passed, account should be unlocked
                            return false;
                        }
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get failed login attempts count for email in last 30 minutes
     */
    @Transactional(readOnly = true)
    public long getFailedLoginAttempts(String email) {
        LocalDateTime since = LocalDateTime.now()
                .minusMinutes(SecurityConstants.ACCOUNT_LOCK_DURATION_MINUTES);
        return loginAttemptRepository.countFailedAttemptsSince(email, since);
    }

    /**
     * Check if account should be locked and lock it if necessary
     */
    @Transactional
    public void checkAndLockAccount(String email) {
        long failedAttempts = getFailedLoginAttempts(email);
        
        if (failedAttempts >= SecurityConstants.MAX_LOGIN_ATTEMPTS) {
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setAccountNonLocked(false);
                user.setLockedUntil(LocalDateTime.now()
                        .plusMinutes(SecurityConstants.ACCOUNT_LOCK_DURATION_MINUTES));
                user.setFailedLoginAttempts((int) failedAttempts);
                userRepository.save(user);
                
                log.warn("Account locked for email: {} after {} failed attempts", 
                        email, failedAttempts);
            });
        }
    }

    /**
     * Unlock account manually (admin action)
     */
    @Transactional
    public void unlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setAccountNonLocked(true);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            log.info("Account manually unlocked for email: {}", email);
        });
    }

    /**
     * Reset failed attempts count for user
     */
    @Transactional
    public void resetFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setAccountNonLocked(true);
                user.setLockedUntil(null);
                userRepository.save(user);
                log.debug("Reset failed attempts for email: {}", email);
            }
        });
    }

    /**
     * Check account lock status and unlock if duration has passed
     */
    @Transactional
    public void checkAndUnlockAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (!user.isAccountNonLocked()) {
                LocalDateTime lockedUntil = user.getLockedUntil();
                if (lockedUntil != null && lockedUntil.isBefore(LocalDateTime.now())) {
                    user.setAccountNonLocked(true);
                    user.setLockedUntil(null);
                    user.setFailedLoginAttempts(0);
                    userRepository.save(user);
                    log.info("Account automatically unlocked for email: {}", email);
                }
            }
        });
    }

    /**
     * Scheduled task to clean up old login attempts
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldLoginAttempts() {
        log.info("Starting cleanup of old login attempts");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        loginAttemptRepository.deleteAttemptsOlderThan(thirtyDaysAgo);
        log.info("Completed cleanup of old login attempts");
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extract user agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 512) {
            return userAgent.substring(0, 512);
        }
        return userAgent;
    }
}
