package com.TenX.Automobile.repository;

import com.TenX.Automobile.entity.User;
import com.TenX.Automobile.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Enterprise-level User Repository with role-based query methods
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (used for authentication)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by employee ID
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if employee ID already exists
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * Find all users by role
     */
    List<User> findByRole(Role role);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find all users by role and active status
     */
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    /**
     * Find all users under a specific manager
     */
    List<User> findByManagerId(Long managerId);

    /**
     * Find all users in a specific department
     */
    List<User> findByDepartment(String department);

    /**
     * Find users by department and role
     */
    List<User> findByDepartmentAndRole(String department, Role role);

    /**
     * Find all managers and admins
     */
    @Query("SELECT u FROM User u WHERE u.role IN ('MANAGER', 'ADMIN') AND u.isActive = true")
    List<User> findAllManagersAndAdmins();

    /**
     * Find users with roles higher than or equal to the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role OR " +
           "(u.role = 'ADMIN') OR " +
           "(u.role = 'MANAGER' AND :role IN ('EMPLOYEE', 'USER')) OR " +
           "(u.role = 'EMPLOYEE' AND :role = 'USER')")
    List<User> findUsersWithRoleOrHigher(@Param("role") Role role);

    /**
     * Count users by role
     */
    long countByRole(Role role);

    /**
     * Count active users
     */
    long countByIsActiveTrue();

    /**
     * Find users by name (first name or last name contains the search term)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("name") String name);
}