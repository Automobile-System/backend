package com.TenX.Automobile.service;

import com.TenX.Automobile.entity.*;
import com.TenX.Automobile.enums.NotificationType;
import com.TenX.Automobile.enums.Role;
import com.TenX.Automobile.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final BaseUserRepository userRepository;
    private final JobRepository jobRepository;
    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Create notification for a specific user
     * @param userId - The ID of the user to notify
     * @param message - Notification message
     * @param type - SYSTEM, EMAIL, or BOTH
     * @param jobId - Optional: Related job ID (null for general notifications)
     */
    @Transactional
    public Notification createNotification(UUID userId, String message, NotificationType type, Long jobId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = null;
        if (jobId != null) {
            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));
        }

        Notification notification = Notification.builder()
                .message(message)
                .type(type)
                .isRead(false)
                .user(user)
                .job(job)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Send email if type is EMAIL or BOTH
        if (type == NotificationType.EMAIL || type == NotificationType.BOTH) {
            sendEmail(user.getEmail(), message);
        }

        return savedNotification;
    }

    /**
     * Create notification without job reference
     */
    @Transactional
    public Notification createNotification(UUID userId, String message, NotificationType type) {
        return createNotification(userId, message, type, null);
    }

    /**
     * Notify multiple users (e.g., all employees on a project)
     */
    @Transactional
    public void notifyMultipleUsers(List<UUID> userIds, String message, NotificationType type, Long jobId) {
        for (UUID userId : userIds) {
            createNotification(userId, message, type, jobId);
        }
    }

    /**
     * Notify users by role - you need to implement findByRole in BaseUserRepository
     * Example: notifyByRole(Role.MANAGER, "New approval needed", NotificationType.EMAIL, jobId);
     */
    @Transactional
    public void notifyByRole(Role role, String message, NotificationType type, Long jobId) {
        // TODO: Implement findByRole in BaseUserRepository first
        // List<UserEntity> users = userRepository.findByRole(role);
        // for (UserEntity user : users) {
        //     createNotification(user.getId(), message, type, jobId);
        // }
    }

    /**
     * Notify customer about job update
     * Example: "Your vehicle service has been completed"
     */
    @Transactional
    public void notifyCustomerJobUpdate(UUID customerId, Long jobId, String message) {
        createNotification(customerId, message, NotificationType.BOTH, jobId);
    }

    /**
     * Notify employee about new assignment
     * Example: "You have been assigned to a new job"
     */
    @Transactional
    public void notifyEmployeeAssignment(UUID employeeId, Long jobId, String message) {
        createNotification(employeeId, message, NotificationType.SYSTEM, jobId);
    }

    /**
     * Notify manager about approval needed
     * Example: "Job #123 requires your approval"
     */
    @Transactional
    public void notifyManagerApproval(UUID managerId, Long jobId, String message) {
        createNotification(managerId, message, NotificationType.EMAIL, jobId);
    }

    /**
     * Notify all admins about system event
     * Example: "System maintenance scheduled"
     */
    @Transactional
    public void notifyAllAdmins(String message) {
        notifyByRole(Role.ADMIN, message, NotificationType.BOTH, null);
    }

    // ==================== TASK NOTIFICATIONS ====================
    
    /**
     * Notify employee about task assignment
     * Example: Employee gets notified when assigned to a task
     */
    @Transactional
    public void notifyTaskAssignment(UUID employeeId, Long taskId, String assignedByName) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        Project project = task.getProject();
        Long jobId = project != null ? project.getJobId() : null;
        
        String message = String.format(
            "📋 New Task Assigned: '%s'\n" +
            "Title: %s\n" +
            "Assigned by: %s\n" +
            "Project: %s\n" +
            "Estimated Hours: %.1f",
            task.getTaskDescription() != null ? task.getTaskDescription() : "No description",
            task.getTaskTitle() != null ? task.getTaskTitle() : "Untitled Task",
            assignedByName,
            project != null ? project.getTitle() : "Unknown Project",
            task.getEstimatedHours() != null ? task.getEstimatedHours() : 0.0
        );
        
        createNotification(employeeId, message, NotificationType.SYSTEM, jobId);
        log.info("Task assignment notification sent to employee: {}", employeeId);
    }

    /**
     * Notify manager about task completion
     * Example: Manager gets notified when employee marks task as complete
     */
    @Transactional
    public void notifyTaskCompletion(Long taskId, UUID completedByEmployeeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        UserEntity employee = employeeRepository.findById(completedByEmployeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        Project project = task.getProject();
        Long jobId = project != null ? project.getJobId() : null;
        
        String message = String.format(
            "✅ Task Completed: '%s'\n" +
            "Title: %s\n" +
            "Completed by: %s %s\n" +
            "Project: %s",
            task.getTaskDescription() != null ? task.getTaskDescription() : "No description",
            task.getTaskTitle() != null ? task.getTaskTitle() : "Untitled Task",
            employee.getFirstName() != null ? employee.getFirstName() : "",
            employee.getLastName() != null ? employee.getLastName() : "",
            project != null ? project.getTitle() : "Unknown Project"
        );
        
        // Notify all managers
        notifyAllManagers(message, jobId);
        log.info("Task completion notification sent for task: {}", taskId);
    }

    /**
     * Notify employee about task deadline approaching
     */
    @Transactional
    public void notifyTaskDeadlineApproaching(UUID employeeId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        Project project = task.getProject();
        Long jobId = project != null ? project.getJobId() : null;
        
        String message = String.format(
            "⏰ Task Deadline Approaching: '%s'\n" +
            "Title: %s\n" +
            "Project: %s\n" +
            "Please complete this task soon.",
            task.getTaskDescription() != null ? task.getTaskDescription() : "No description",
            task.getTaskTitle() != null ? task.getTaskTitle() : "Untitled Task",
            project != null ? project.getTitle() : "Unknown Project"
        );
        
        createNotification(employeeId, message, NotificationType.EMAIL, jobId);
    }

    // ==================== JOB NOTIFICATIONS ====================
    
    /**
     * Notify customer about job status change (for Projects)
     * Example: "Your vehicle project is now IN_PROGRESS"
     */
    @Transactional
    public void notifyProjectStatusChange(Long projectId, UUID customerId, String newStatus) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        String message = String.format(
            "🔔 Project Status Update\n" +
            "Project: %s\n" +
            "New Status: %s\n" +
            "Estimated Hours: %.1f",
            project.getTitle() != null ? project.getTitle() : "Untitled Project",
            newStatus,
            project.getEstimatedHours() != null ? project.getEstimatedHours() : 0.0
        );
        
        createNotification(customerId, message, NotificationType.BOTH, project.getJobId());
        log.info("Project status change notification sent for project: {}", projectId);
    }

    /**
     * Notify customer about service status change
     */
    @Transactional
    public void notifyServiceStatusChange(Long serviceId, UUID customerId, String newStatus) {
        com.TenX.Automobile.entity.Service serviceEntity = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        String message = String.format(
            "🔔 Service Status Update\n" +
            "Service: %s\n" +
            "Category: %s\n" +
            "New Status: %s",
            serviceEntity.getTitle() != null ? serviceEntity.getTitle() : "Untitled Service",
            serviceEntity.getCategory() != null ? serviceEntity.getCategory() : "N/A",
            newStatus
        );
        
        createNotification(customerId, message, NotificationType.BOTH, serviceEntity.getJobId());
        log.info("Service status change notification sent for service: {}", serviceId);
    }

    /**
     * Notify employee about project assignment
     */
    @Transactional
    public void notifyProjectAssignment(UUID employeeId, Long projectId, String assignedByName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        String message = String.format(
            "🔧 New Project Assigned\n" +
            "Project: %s\n" +
            "Description: %s\n" +
            "Assigned by: %s\n" +
            "Estimated Hours: %.1f",
            project.getTitle() != null ? project.getTitle() : "Untitled Project",
            project.getDescription() != null ? project.getDescription() : "No description",
            assignedByName,
            project.getEstimatedHours() != null ? project.getEstimatedHours() : 0.0
        );
        
        createNotification(employeeId, message, NotificationType.SYSTEM, project.getJobId());
    }

    /**
     * Notify manager about project approval request
     */
    @Transactional
    public void notifyProjectApprovalRequest(Long projectId, UUID requestedByEmployeeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        UserEntity employee = employeeRepository.findById(requestedByEmployeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        String message = String.format(
            "📝 Project Approval Required\n" +
            "Project: %s\n" +
            "Requested by: %s %s\n" +
            "Status: Awaiting Approval",
            project.getTitle() != null ? project.getTitle() : "Untitled Project",
            employee.getFirstName() != null ? employee.getFirstName() : "",
            employee.getLastName() != null ? employee.getLastName() : ""
        );
        
        notifyAllManagers(message, project.getJobId());
    }

    /**
     * Notify customer that project/service is ready
     */
    @Transactional
    public void notifyCustomerJobReady(UUID customerId, Long jobId, String jobTitle) {
        String message = String.format(
            "✨ Your Job is Ready!\n" +
            "Job: %s\n" +
            "Please visit our service center for pickup or further details.",
            jobTitle
        );
        
        createNotification(customerId, message, NotificationType.BOTH, jobId);
    }

    // ==================== ROLE-BASED NOTIFICATIONS ====================
    
    /**
     * Notify all managers about an event
     * NOTE: Requires findByRole method in BaseUserRepository
     * Add this method to BaseUserRepository: List<UserEntity> findByRole(Role role);
     */
    @Transactional
    public void notifyAllManagers(String message, Long jobId) {
        // TODO: Uncomment when findByRole is implemented in BaseUserRepository
        // List<UserEntity> managers = userRepository.findByRole(Role.MANAGER);
        // for (UserEntity manager : managers) {
        //     createNotification(manager.getId(), message, NotificationType.EMAIL, jobId);
        // }
        // log.info("Notification sent to {} managers", managers.size());
        log.warn("notifyAllManagers called but findByRole not implemented in BaseUserRepository");
    }

    /**
     * Notify all admins about system event
     * NOTE: Requires findByRole method in BaseUserRepository
     */
    @Transactional
    public void notifyAllAdmins(String message, Long jobId) {
        // TODO: Uncomment when findByRole is implemented in BaseUserRepository
        // List<UserEntity> admins = userRepository.findByRole(Role.ADMIN);
        // for (UserEntity admin : admins) {
        //     createNotification(admin.getId(), message, NotificationType.BOTH, jobId);
        // }
        // log.info("Notification sent to {} admins", admins.size());
        log.warn("notifyAllAdmins called but findByRole not implemented in BaseUserRepository");
    }

    /**
     * Notify specific employees (requires specific employee IDs)
     * @param employeeIds List of employee UUIDs to notify
     */
    @Transactional
    public void notifySpecificEmployees(List<UUID> employeeIds, String message, Long jobId) {
        for (UUID employeeId : employeeIds) {
            createNotification(employeeId, message, NotificationType.SYSTEM, jobId);
        }
        log.info("Notification sent to {} employees", employeeIds.size());
    }

    /**
     * Notify admin about new user registration
     * @param adminId The UUID of the admin to notify
     */
    @Transactional
    public void notifyAdminNewUserRegistration(UUID adminId, String userName, String userEmail, Role role) {
        String message = String.format(
            "👤 New User Registered\n" +
            "Name: %s\n" +
            "Email: %s\n" +
            "Role: %s",
            userName,
            userEmail,
            role.name()
        );
        
        createNotification(adminId, message, NotificationType.BOTH, null);
    }

    /**
     * Notify admin about system event
     * @param adminId The UUID of the admin to notify
     */
    @Transactional
    public void notifyAdminSystemEvent(UUID adminId, String eventType, String eventDetails) {
        String message = String.format(
            "⚙️ System Event: %s\n%s",
            eventType,
            eventDetails
        );
        
        createNotification(adminId, message, NotificationType.BOTH, null);
    }

    // ==================== BROADCAST NOTIFICATIONS ====================
    
    /**
     * Broadcast notification to specific role
     * NOTE: Requires findByRole method in BaseUserRepository
     */
    @Transactional
    public void broadcastToRole(Role role, String message, NotificationType type) {
        // TODO: Uncomment when findByRole is implemented in BaseUserRepository
        // List<UserEntity> users = userRepository.findByRole(role);
        // for (UserEntity user : users) {
        //     createNotification(user.getId(), message, type, null);
        // }
        // log.info("Broadcast sent to {} users with role {}", users.size(), role);
        log.warn("broadcastToRole called but findByRole not implemented in BaseUserRepository");
    }

    /**
     * Broadcast notification to all users in the system
     */
    @Transactional
    public void broadcastToAllUsers(String message, NotificationType type) {
        List<UserEntity> allUsers = userRepository.findAll();
        for (UserEntity user : allUsers) {
            createNotification(user.getId(), message, type, null);
        }
        log.info("Broadcast sent to {} total users", allUsers.size());
    }

    /**
     * Broadcast to specific list of users
     * @param userIds List of user UUIDs to notify
     */
    @Transactional
    public void broadcastToUsers(List<UUID> userIds, String message, NotificationType type) {
        for (UUID userId : userIds) {
            createNotification(userId, message, type, null);
        }
        log.info("Broadcast sent to {} users", userIds.size());
    }

    /**
     * Get all notifications for a user (authorized user only)
     */
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUser_Id(userId);
    }

    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUser_IdAndIsRead(userId, false);
    }

    /**
     * Get notifications for a specific job
     */
    public List<Notification> getJobNotifications(UUID userId, Long jobId) {
        return notificationRepository.findByUser_IdAndJob_JobId(userId, jobId);
    }

    /**
     * Mark notification as read (with authorization check)
     */
    @Transactional
    public void markAsRead(Long notificationId, UUID requestingUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Authorization: Only the notification owner can mark it as read
        if (!notification.getUser().getId().equals(requestingUserId)) {
            throw new RuntimeException("Unauthorized: You cannot modify this notification");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdAndIsRead(userId, false);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Get unread notification count
     */
    public Long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndIsRead(userId, false);
    }

    /**
     * Delete notification (with authorization check)
     */
    @Transactional
    public void deleteNotification(Long notificationId, UUID requestingUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Authorization: Only the notification owner can delete it
        if (!notification.getUser().getId().equals(requestingUserId)) {
            throw new RuntimeException("Unauthorized: You cannot delete this notification");
        }
        
        notificationRepository.delete(notification);
    }

    /**
     * Send email notification (placeholder - implement when email is configured)
     */
    private void sendEmail(String to, String message) {
        // TODO: Add spring-boot-starter-mail dependency and configure email settings
        // Then uncomment the code below:
        
        // try {
        //     SimpleMailMessage mailMessage = new SimpleMailMessage();
        //     mailMessage.setTo(to);
        //     mailMessage.setSubject("Notification from TenX Automobile");
        //     mailMessage.setText(message);
        //     mailSender.send(mailMessage);
        // } catch (Exception e) {
        //     System.err.println("Failed to send email: " + e.getMessage());
        // }
        
        System.out.println("Email would be sent to: " + to + " with message: " + message);
    }
}
