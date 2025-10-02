package com.TenX.Automobile.service;

import com.TenX.Automobile.dto.SignupRequest;
import com.TenX.Automobile.dto.UserResponse;
import com.TenX.Automobile.entity.User;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enterprise-level User Service with role-based business logic
 */
@Service
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Security UserDetailsService implementation
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Register a new user
     */
    public UserResponse registerUser(SignupRequest signupRequest) {
        log.info("Registering new user with email: {}", signupRequest.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + signupRequest.getEmail());
        }

        // Check if employee ID already exists (for employees, managers, admins)
        if (signupRequest.getEmployeeId() != null && 
            userRepository.existsByEmployeeId(signupRequest.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already exists: " + signupRequest.getEmployeeId());
        }

        // Validate role-specific requirements
        validateRoleRequirements(signupRequest);

        // Create new user
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole());
        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setDepartment(signupRequest.getDepartment());
        user.setEmployeeId(signupRequest.getEmployeeId());
        user.setManagerId(signupRequest.getManagerId());

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {}", savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user response by ID
     */
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToUserResponse);
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /**
     * Get all users by role
     */
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active users
     */
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users under a specific manager
     */
    public List<UserResponse> getUsersUnderManager(Long managerId) {
        return userRepository.findByManagerId(managerId).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users by department
     */
    public List<UserResponse> getUsersByDepartment(String department) {
        return userRepository.findByDepartment(department).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("Deactivated user with ID: {}", userId);
        });
    }

    /**
     * Activate user account
     */
    public void activateUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setIsActive(true);
            userRepository.save(user);
            log.info("Activated user with ID: {}", userId);
        });
    }

    /**
     * Check if user has required role or higher
     */
    public boolean hasRoleOrHigher(String email, Role requiredRole) {
        return userRepository.findByEmail(email)
                .map(user -> user.getRole().hasAuthorityOver(requiredRole))
                .orElse(false);
    }

    /**
     * Validate role-specific requirements
     */
    private void validateRoleRequirements(SignupRequest signupRequest) {
        Role role = signupRequest.getRole();
        
        switch (role) {
            case EMPLOYEE:
            case MANAGER:
                // Employees and Managers require employee ID and department
                if (signupRequest.getEmployeeId() == null || signupRequest.getEmployeeId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Employee ID is required for role: " + role);
                }
                if (signupRequest.getDepartment() == null || signupRequest.getDepartment().trim().isEmpty()) {
                    throw new IllegalArgumentException("Department is required for role: " + role);
                }
                break;
            case ADMIN:
                // Admin can optionally have employee ID and department, but not required
                // This allows for system admins who might not be regular employees
                break;
            case USER:
                // No specific requirements for regular users
                break;
        }

        // Validate manager exists if managerId is provided
        if (signupRequest.getManagerId() != null) {
            User manager = userRepository.findById(signupRequest.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + signupRequest.getManagerId()));
            
            if (!manager.isManager()) {
                throw new IllegalArgumentException("Specified manager does not have manager role");
            }
        }
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getDepartment(),
                user.getEmployeeId(),
                user.getManagerId(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin()
        );
    }
}