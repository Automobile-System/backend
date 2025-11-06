package com.TenX.Automobile.controller;

import com.TenX.Automobile.entity.Notification;
import com.TenX.Automobile.entity.UserEntity;
import com.TenX.Automobile.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications
     * Get all notifications for the authenticated user
     * Use this to display in notification dropdown/list
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/unread
     * Get only unread notifications
     * Use this to display in notification bell icon dropdown
     */
    @GetMapping("/unread")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/unread/count
     * Get unread notification count
     * Use this for the notification bell badge (red number)
     * Example response: { "count": 5 }
     */
    @GetMapping("/unread/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notifications/job/{jobId}
     * Get notifications for a specific job
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Notification>> getJobNotifications(
            @PathVariable Long jobId,
            Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        List<Notification> notifications = notificationService.getJobNotifications(userId, jobId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * PATCH /api/notifications/{notificationId}/read
     * Mark a notification as read
     * Call this when user clicks on a notification
     */
    @PatchMapping("/{noti_id}/read")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long noti_id,
            Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        notificationService.markAsRead(noti_id, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/notifications/read-all
     * Mark all notifications as read
     * Call this when user clicks "Mark all as read" button
     */
    @PatchMapping("/read-all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        notificationService.markAllAsRead(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/notifications/{notificationId}
     * Delete a notification
     */

    @PreAuthorize("permitAll()")
    @DeleteMapping("/{noti_id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long noti_id,
            Authentication authentication) {
        UUID userId = getUserIdFromAuth(authentication);
        notificationService.deleteNotification(noti_id, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/notifications/test
     * Create a test notification for testing purposes
     * REMOVE THIS IN PRODUCTION!
     */
    /**
     * POST /api/notifications/test
     * Create a test notification for testing purposes
     * NO AUTHENTICATION REQUIRED - For testing only!
     * REMOVE THIS IN PRODUCTION!
     * 
     * Request Body (optional):
     * {
     *   "userId": "uuid-string",
     *   "message": "custom message"
     * }
     */
    @PostMapping("/test")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> createTestNotification(
            @RequestBody(required = false) Map<String, Object> request,
            Authentication authentication) {
        
        UUID userId;
        String message;
        
        // If authenticated, use the authenticated user
        if (authentication != null && authentication.isAuthenticated()) {
            userId = getUserIdFromAuth(authentication);
            message = request != null && request.containsKey("message") 
                ? (String) request.get("message")
                : "Test Notification\nThis is a test notification created from Postman.\nCreated at: " +
                  java.time.LocalDateTime.now().toString();
        } 
        // If not authenticated, require userId in request body
        else {
            if (request == null || !request.containsKey("userId")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "userId is required when not authenticated");
                error.put("example", "{ \"userId\": \"550e8400-e29b-41d4-a716-446655440000\" }");
                return ResponseEntity.badRequest().body(error);
            }
            
            try {
                userId = UUID.fromString((String) request.get("userId"));
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid userId format. Must be a valid UUID");
                return ResponseEntity.badRequest().body(error);
            }
            
            message = request.containsKey("message") 
                ? (String) request.get("message")
                : "Test Notification (No Auth)\nThis is a test notification created without authentication.\nCreated at: " +
                  java.time.LocalDateTime.now().toString();
        }
        
        Notification notification = notificationService.createNotification(
            userId,
            message,
            com.TenX.Automobile.enums.NotificationType.SYSTEM,
            null
        );
        
        return ResponseEntity.ok(notification);
    }

    /**
     * Helper method to extract user ID from authentication
     */
  // ...existing code...
  /**
   * Helper method to extract user ID from authentication
   */
  private UUID getUserIdFromAuth(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
      throw new IllegalStateException("Not authenticated. Provide a valid Authorization: Bearer <token> header.");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserEntity) {
      return ((UserEntity) principal).getId();
    }

    // If your security uses a different principal type, handle it here
    throw new IllegalStateException("Unexpected authentication principal type: " + principal.getClass().getName());
  }
// ...existing code...
}
