package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.AuthResponse;
import com.TenX.Automobile.dto.LoginRequest;
import com.TenX.Automobile.dto.SignupRequest;
import com.TenX.Automobile.dto.UserResponse;
import com.TenX.Automobile.entity.User;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.service.JwtService;
import com.TenX.Automobile.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enterprise-level Authentication Controller
 * Handles user registration, login, logout, and user management operations
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            log.info("Registration attempt for email: {}", signupRequest.getEmail());
            
            UserResponse userResponse = userService.registerUser(signupRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", userResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error for email: {}", signupRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {
        try {
            log.info("Login attempt for email: {}", loginRequest.getEmail());

            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), 
                            loginRequest.getPassword())
            );

            // Get user details
            User user = (User) authentication.getPrincipal();
            
            // Generate JWT token
            String token = jwtService.generateToken(user, loginRequest.getRememberMe());

            // Update last login time
            userService.updateLastLogin(user.getEmail());

            // Create HttpOnly cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(loginRequest.getRememberMe() ? 
                    jwtService.getExtendedExpirationInSeconds().intValue() : 
                    jwtService.getExpirationInSeconds().intValue());

            response.addCookie(cookie);

            // Create response
            UserResponse userResponse = userService.getUserById(user.getId()).orElse(null);
            Long expiresIn = loginRequest.getRememberMe() ? 
                    jwtService.getExtendedExpirationInSeconds() : 
                    jwtService.getExpirationInSeconds();
            
            AuthResponse authResponse = new AuthResponse(token, expiresIn, userResponse);

            log.info("Login successful for user: {} with role: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(authResponse);

        } catch (BadCredentialsException e) {
            log.warn("Login failed - invalid credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("Login error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed. Please try again."));
        }
    }

    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            // Clear the JWT cookie
            Cookie cookie = new Cookie("jwt", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(0); // Immediately expire

            response.addCookie(cookie);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            log.info("User logged out successfully");
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
            
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed"));
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            String email = jwtService.extractEmail(token);
            Optional<User> user = userService.findByEmail(email);
            
            if (user.isPresent()) {
                UserResponse userResponse = userService.getUserById(user.get().getId()).orElse(null);
                return ResponseEntity.ok(userResponse);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }
            
        } catch (Exception e) {
            log.error("Error getting current user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get user profile"));
        }
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> users = userService.getActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get users"));
        }
    }

    /**
     * Get users by role (Manager and Admin only)
     */
    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            Role userRole = Role.fromString(role);
            List<UserResponse> users = userService.getUsersByRole(userRole);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role: " + role));
        } catch (Exception e) {
            log.error("Error getting users by role: {}", role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get users by role"));
        }
    }

    /**
     * Get users under manager (Manager and Admin only)
     */
    @GetMapping("/users/managed/{managerId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUsersUnderManager(@PathVariable Long managerId) {
        try {
            List<UserResponse> users = userService.getUsersUnderManager(managerId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users under manager: {}", managerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get managed users"));
        }
    }

    /**
     * Get users by department (Manager and Admin only)
     */
    @GetMapping("/users/department/{department}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByDepartment(@PathVariable String department) {
        try {
            List<UserResponse> users = userService.getUsersByDepartment(department);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users by department: {}", department, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get users by department"));
        }
    }

    /**
     * Deactivate user account (Admin only)
     */
    @PutMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate user"));
        }
    }

    /**
     * Activate user account (Admin only)
     */
    @PutMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(Map.of("message", "User activated successfully"));
        } catch (Exception e) {
            log.error("Error activating user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to activate user"));
        }
    }

    /**
     * Check if user has required role or higher
     */
    @GetMapping("/users/{email}/has-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkUserRole(@PathVariable String email, @PathVariable String role) {
        try {
            Role requiredRole = Role.fromString(role);
            boolean hasRole = userService.hasRoleOrHigher(email, requiredRole);
            return ResponseEntity.ok(Map.of("hasRole", hasRole));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role: " + role));
        } catch (Exception e) {
            log.error("Error checking user role for email: {}, role: {}", email, role, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check user role"));
        }
    }

    /**
     * Extract JWT token from request (cookie or Authorization header)
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Try to get token from Authorization header first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Try to get token from cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}