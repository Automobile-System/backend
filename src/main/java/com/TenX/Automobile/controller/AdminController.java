package com.TenX.Automobile.controller;

import com.TenX.Automobile.dto.request.*;
import com.TenX.Automobile.dto.response.*;
import com.TenX.Automobile.service.AdminService;
import com.TenX.Automobile.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Controller - ADMIN role only
 * Provides comprehensive endpoints for admin dashboard operations including:
 * - Dashboard overview and metrics
 * - Financial reports
 * - Workforce management
 * - Services analytics
 * - AI insights
 * - System settings
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final NotificationService notificationService;

    // ==================== COMMON/SHARED ENDPOINTS ====================

    /**
     * Fetch notifications for admin
     */
    @GetMapping("/notifications")
    public ResponseEntity<?> fetchNotifications(Authentication authentication) {
        log.info("Fetching notifications for admin: {}", authentication.getName());
        String email = authentication.getName();
        com.TenX.Automobile.entity.UserEntity user = adminService.getUserByEmail(email);
        UUID userId = user.getId();
        List<com.TenX.Automobile.entity.Notification> notifications = notificationService.getUserNotifications(userId);
        
        List<Map<String, Object>> response = notifications.stream()
            .map(n -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", "notification-" + n.getNoti_id());
                map.put("title", "System Alert");
                map.put("message", n.getMessage());
                map.put("time", formatTimeAgo(n.getCreatedAt()));
                map.put("read", n.getIsRead());
                map.put("type", n.getType() != null ? n.getType().name().toLowerCase() : "system");
                return map;
            })
            .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Marking notification {} as read", id);
        String email = authentication.getName();
        com.TenX.Automobile.entity.UserEntity user = adminService.getUserByEmail(email);
        UUID userId = user.getId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Deleting notification {}", id);
        String email = authentication.getName();
        com.TenX.Automobile.entity.UserEntity user = adminService.getUserByEmail(email);
        UUID userId = user.getId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }

    /**
     * Fetch user profile
     */
    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> fetchUserProfile(Authentication authentication) {
        log.info("Fetching profile for admin: {}", authentication.getName());
        String email = authentication.getName();
        com.TenX.Automobile.entity.UserEntity user = adminService.getUserByEmail(email);
        
        Map<String, Object> response = Map.of(
            "id", user.getId().toString(),
            "name", (user.getFirstName() != null ? user.getFirstName() : "") + 
                    (user.getLastName() != null ? " " + user.getLastName() : ""),
            "email", user.getEmail(),
            "role", "admin",
            "avatar", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : ""
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update user profile
     */
    @PutMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @RequestBody Map<String, Object> profileData,
            Authentication authentication) {
        log.info("Updating profile for admin: {}", authentication.getName());
        // TODO: Implement profile update logic
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    /**
     * Fetch user settings
     */
    @GetMapping("/user/settings")
    public ResponseEntity<Map<String, Object>> fetchUserSettings() {
        log.info("Fetching user settings");
        return ResponseEntity.ok(Map.of(
            "notifications", true,
            "emailAlerts", true,
            "theme", "light"
        ));
    }

    /**
     * Update user settings
     */
    @PutMapping("/user/settings")
    public ResponseEntity<Map<String, Object>> updateUserSettings(@RequestBody Map<String, Object> settings) {
        log.info("Updating user settings");
        return ResponseEntity.ok(settings);
    }

    // ==================== PAGE 1: DASHBOARD ====================

    /**
     * Get dashboard statistics
     * GET /api/admin/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        log.info("Getting dashboard statistics");
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    /**
     * Get system alerts
     * GET /api/admin/dashboard/alerts
     */
    @GetMapping("/dashboard/alerts")
    public ResponseEntity<List<SystemAlertResponse>> getSystemAlerts() {
        log.info("Getting system alerts");
        return ResponseEntity.ok(adminService.getSystemAlerts());
    }

    /**
     * Get AI insights
     * GET /api/admin/dashboard/ai-insights
     */
    @GetMapping("/dashboard/ai-insights")
    public ResponseEntity<List<AIInsightResponse>> getAIInsights() {
        log.info("Getting AI insights");
        return ResponseEntity.ok(adminService.getAIInsights());
    }

    // ==================== PAGE 2: FINANCIAL REPORTS ====================

    /**
     * Get financial reports
     * GET /api/admin/financial-reports?serviceFilter=all&startDate=2025-01-01&endDate=2025-11-04
     */
    @GetMapping("/financial-reports")
    public ResponseEntity<FinancialReportResponse> getFinancialReports(
            @RequestParam(defaultValue = "all") String serviceFilter,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Getting financial reports: filter={}, startDate={}, endDate={}", 
            serviceFilter, startDate, endDate);
        return ResponseEntity.ok(adminService.getFinancialReports(
            serviceFilter, startDate.toString(), endDate.toString()));
    }

    /**
     * Export financial report as PDF
     * POST /api/admin/financial-reports/export-pdf
     */
    @PostMapping("/financial-reports/export-pdf")
    public ResponseEntity<Map<String, Object>> exportFinancialReportPDF(
            @RequestBody FinancialReportRequest request) {
        log.info("Exporting financial report as PDF");
        // TODO: Implement PDF generation
        return ResponseEntity.ok(Map.of("message", "PDF export not yet implemented"));
    }

    /**
     * Export financial report as Excel
     * POST /api/admin/financial-reports/export-excel
     */
    @PostMapping("/financial-reports/export-excel")
    public ResponseEntity<Map<String, Object>> exportFinancialReportExcel(
            @RequestBody FinancialReportRequest request) {
        log.info("Exporting financial report as Excel");
        // TODO: Implement Excel generation
        return ResponseEntity.ok(Map.of("message", "Excel export not yet implemented"));
    }

    // ==================== PAGE 3: WORKFORCE OVERVIEW ====================

    /**
     * Get workforce overview
     * GET /api/admin/workforce/overview
     */
    @GetMapping("/workforce/overview")
    public ResponseEntity<WorkforceOverviewResponse> getWorkforceOverview() {
        log.info("Getting workforce overview");
        return ResponseEntity.ok(adminService.getWorkforceOverview());
    }

    /**
     * Get top employees
     * GET /api/admin/workforce/top-employees
     */
    @GetMapping("/workforce/top-employees")
    public ResponseEntity<List<TopEmployeeResponse>> getTopEmployees() {
        log.info("Getting top employees");
        return ResponseEntity.ok(adminService.getTopEmployees());
    }

    /**
     * Get manager performance
     * GET /api/admin/workforce/manager-performance
     */
    @GetMapping("/workforce/manager-performance")
    public ResponseEntity<List<ManagerPerformanceResponse>> getManagerPerformance() {
        log.info("Getting manager performance");
        return ResponseEntity.ok(adminService.getManagerPerformance());
    }

    /**
     * Get all managers
     * GET /api/admin/workforce/managers
     */
    @GetMapping("/workforce/managers")
    public ResponseEntity<List<ManagerResponse>> getAllManagers() {
        log.info("Getting all managers");
        return ResponseEntity.ok(adminService.getAllManagers());
    }

    /**
     * Get all employees
     * GET /api/admin/workforce/employees
     */
    @GetMapping("/workforce/employees")
    public ResponseEntity<List<EmployeeDetailResponse>> getAllEmployees() {
        log.info("Getting all employees");
        return ResponseEntity.ok(adminService.getAllEmployees());
    }

    /**
     * Add manager
     * POST /api/admin/workforce/managers
     */
    @PostMapping("/workforce/managers")
    public ResponseEntity<Map<String, Object>> addManager(@Valid @RequestBody AddManagerRequest request) {
        log.info("Adding manager: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminService.addManager(request));
    }

    /**
     * Add employee
     * POST /api/admin/workforce/employees
     */
    @PostMapping("/workforce/employees")
    public ResponseEntity<Map<String, Object>> addEmployee(@Valid @RequestBody AddEmployeeRequest request) {
        log.info("Adding employee: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminService.addEmployee(request));
    }

    /**
     * Update manager
     * PUT /api/admin/workforce/managers/{id}
     */
    @PutMapping("/workforce/managers/{id}")
    public ResponseEntity<Map<String, Object>> updateManager(
            @PathVariable String id,
            @Valid @RequestBody UpdateManagerRequest request) {
        log.info("Updating manager: {}", id);
        return ResponseEntity.ok(adminService.updateManager(id, request));
    }

    /**
     * Update employee
     * PUT /api/admin/workforce/employees/{id}
     */
    @PutMapping("/workforce/employees/{id}")
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        log.info("Updating employee: {}", id);
        return ResponseEntity.ok(adminService.updateEmployee(id, request));
    }

    /**
     * Freeze manager
     * PUT /api/admin/workforce/managers/{id}/freeze
     */
    @PutMapping("/workforce/managers/{id}/freeze")
    public ResponseEntity<Map<String, Object>> freezeManager(@PathVariable String id) {
        log.info("Freezing manager: {}", id);
        return ResponseEntity.ok(adminService.freezeManager(id));
    }

    /**
     * Freeze employee
     * PUT /api/admin/workforce/employees/{id}/freeze
     */
    @PutMapping("/workforce/employees/{id}/freeze")
    public ResponseEntity<Map<String, Object>> freezeEmployee(@PathVariable String id) {
        log.info("Freezing employee: {}", id);
        return ResponseEntity.ok(adminService.freezeEmployee(id));
    }

    /**
     * Activate employee
     * PUT /api/admin/workforce/employees/{id}/activate
     */
    @PutMapping("/workforce/employees/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateEmployee(@PathVariable String id) {
        log.info("Activating employee: {}", id);
        return ResponseEntity.ok(adminService.activateEmployee(id));
    }

    /**
     * Activate manager
     * PUT /api/admin/workforce/managers/{id}/activate
     */
    @PutMapping("/workforce/managers/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateManager(@PathVariable String id) {
        log.info("Activating manager: {}", id);
        return ResponseEntity.ok(adminService.activateManager(id));
    }

    // ==================== PAGE 4: SERVICES ANALYTICS ====================

    /**
     * Get services analytics (combined)
     * GET /api/admin/services/analytics
     */
    @GetMapping("/services/analytics")
    public ResponseEntity<ServicesAnalyticsResponse> getServicesAnalytics() {
        log.info("Getting services analytics");
        return ResponseEntity.ok(adminService.getServicesAnalytics());
    }

    /**
     * Get most profitable service
     * GET /api/admin/services/analytics/most-profitable
     */
    @GetMapping("/services/analytics/most-profitable")
    public ResponseEntity<ServicesAnalyticsResponse.MostProfitableService> getMostProfitableService() {
        log.info("Getting most profitable service");
        return ResponseEntity.ok(adminService.getMostProfitableService());
    }

    /**
     * Get total services data
     * GET /api/admin/services/analytics/total-services
     */
    @GetMapping("/services/analytics/total-services")
    public ResponseEntity<TotalServicesData> getTotalServicesData() {
        log.info("Getting total services data");
        return ResponseEntity.ok(adminService.getTotalServicesData());
    }

    /**
     * Get parts replaced data
     * GET /api/admin/services/analytics/parts-replaced
     */
    @GetMapping("/services/analytics/parts-replaced")
    public ResponseEntity<PartsReplacedData> getPartsReplacedData() {
        log.info("Getting parts replaced data");
        return ResponseEntity.ok(adminService.getPartsReplacedData());
    }

    /**
     * Get customer retention data
     * GET /api/admin/services/analytics/customer-retention
     */
    @GetMapping("/services/analytics/customer-retention")
    public ResponseEntity<CustomerRetentionData> getCustomerRetentionData() {
        log.info("Getting customer retention data");
        return ResponseEntity.ok(adminService.getCustomerRetentionData());
    }

    /**
     * Get service performance
     * GET /api/admin/services/analytics/service-performance
     */
    @GetMapping("/services/analytics/service-performance")
    public ResponseEntity<List<ServicesAnalyticsResponse.ServicePerformance>> getServicePerformance() {
        log.info("Getting service performance");
        return ResponseEntity.ok(adminService.getServicePerformance());
    }

    // ==================== PAGE 5: AI INSIGHTS ====================

    /**
     * Get demand forecast
     * GET /api/admin/ai-insights/demand-forecast
     */
    @GetMapping("/ai-insights/demand-forecast")
    public ResponseEntity<DemandForecastResponse> getDemandForecast() {
        log.info("Getting demand forecast");
        return ResponseEntity.ok(adminService.getDemandForecast());
    }

    /**
     * Get profit projection
     * GET /api/admin/ai-insights/profit-projection
     */
    @GetMapping("/ai-insights/profit-projection")
    public ResponseEntity<ProfitProjectionResponse> getProfitProjection() {
        log.info("Getting profit projection");
        return ResponseEntity.ok(adminService.getProfitProjection());
    }

    /**
     * Get underperforming departments
     * GET /api/admin/ai-insights/underperforming-departments
     */
    @GetMapping("/ai-insights/underperforming-departments")
    public ResponseEntity<List<UnderperformingDepartmentResponse>> getUnderperformingDepartments() {
        log.info("Getting underperforming departments");
        return ResponseEntity.ok(adminService.getUnderperformingDepartments());
    }

    /**
     * Get skill shortage prediction
     * GET /api/admin/ai-insights/skill-shortage-prediction
     */
    @GetMapping("/ai-insights/skill-shortage-prediction")
    public ResponseEntity<List<SkillShortagePredictionResponse>> getSkillShortagePrediction() {
        log.info("Getting skill shortage prediction");
        return ResponseEntity.ok(adminService.getSkillShortagePrediction());
    }

    // ==================== PAGE 6: SETTINGS ====================

    /**
     * Get roles and permissions
     * GET /api/admin/settings/roles
     */
    @GetMapping("/settings/roles")
    public ResponseEntity<List<SettingsRolesResponse>> getRolesPermissions() {
        log.info("Getting roles and permissions");
        return ResponseEntity.ok(adminService.getRolesPermissions());
    }

    /**
     * Get services and pricing
     * GET /api/admin/settings/services
     */
    @GetMapping("/settings/services")
    public ResponseEntity<List<SettingsServicesResponse>> getServicesPricing() {
        log.info("Getting services and pricing");
        return ResponseEntity.ok(adminService.getServicesPricing());
    }

    /**
     * Get task limits
     * GET /api/admin/settings/task-limits
     */
    @GetMapping("/settings/task-limits")
    public ResponseEntity<TaskLimitsResponse> getTaskLimits() {
        log.info("Getting task limits");
        return ResponseEntity.ok(adminService.getTaskLimits());
    }

    /**
     * Update task limits
     * PUT /api/admin/settings/task-limits
     */
    @PutMapping("/settings/task-limits")
    public ResponseEntity<Map<String, Object>> updateTaskLimits(
            @Valid @RequestBody UpdateTaskLimitsRequest request) {
        log.info("Updating task limits");
        return ResponseEntity.ok(adminService.updateTaskLimits(request));
    }

    /**
     * Get compensation rules
     * GET /api/admin/settings/compensation
     */
    @GetMapping("/settings/compensation")
    public ResponseEntity<CompensationRulesResponse> getCompensationRules() {
        log.info("Getting compensation rules");
        return ResponseEntity.ok(adminService.getCompensationRules());
    }

    /**
     * Update compensation rules
     * PUT /api/admin/settings/compensation
     */
    @PutMapping("/settings/compensation")
    public ResponseEntity<Map<String, Object>> updateCompensationRules(
            @Valid @RequestBody UpdateCompensationRulesRequest request) {
        log.info("Updating compensation rules");
        return ResponseEntity.ok(adminService.updateCompensationRules(request));
    }

    // ==================== PAGE 7: CUSTOMER MANAGEMENT ====================

    /**
     * Get customer overview statistics
     * GET /api/admin/customers/overview
     */
    @GetMapping("/customers/overview")
    public ResponseEntity<CustomerOverviewResponse> getCustomerOverview() {
        log.info("Getting customer overview");
        return ResponseEntity.ok(adminService.getCustomerOverview());
    }

    /**
     * Get list of all customers
     * GET /api/admin/customers/list
     */
    @GetMapping("/customers/list")
    public ResponseEntity<List<CustomerListResponse>> getCustomerList() {
        log.info("Getting customer list");
        return ResponseEntity.ok(adminService.getCustomerList());
    }

    /**
     * Get customer by ID
     * GET /api/admin/customers/{id}
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerListResponse> getCustomerById(@PathVariable String id) {
        log.info("Getting customer by ID: {}", id);
        return ResponseEntity.ok(adminService.getCustomerById(id));
    }

    /**
     * Add new customer
     * POST /api/admin/customers
     */
    @PostMapping("/customers")
    public ResponseEntity<CustomerListResponse> addCustomer(@Valid @RequestBody AddCustomerRequest request) {
        log.info("Adding customer: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminService.addCustomer(request));
    }

    /**
     * Update customer status
     * PUT /api/admin/customers/{id}/status
     */
    @PutMapping("/customers/{id}/status")
    public ResponseEntity<CustomerListResponse> updateCustomerStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusRequest) {
        log.info("Updating customer {} status to {}", id, statusRequest.get("status"));
        return ResponseEntity.ok(adminService.updateCustomerStatus(id, statusRequest.get("status")));
    }

    /**
     * Delete customer
     * DELETE /api/admin/customers/{id}
     */
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable String id) {
        log.info("Deleting customer: {}", id);
        adminService.deleteCustomer(id);
        return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));
    }

    /**
     * Activate customer
     * PUT /api/admin/customers/{id}/activate
     */
    @PutMapping("/customers/{id}/activate")
    public ResponseEntity<CustomerListResponse> activateCustomer(@PathVariable String id) {
        log.info("Activating customer: {}", id);
        return ResponseEntity.ok(adminService.activateCustomer(id));
    }

    /**
     * Deactivate customer
     * PUT /api/admin/customers/{id}/deactivate
     */
    @PutMapping("/customers/{id}/deactivate")
    public ResponseEntity<CustomerListResponse> deactivateCustomer(@PathVariable String id) {
        log.info("Deactivating customer: {}", id);
        return ResponseEntity.ok(adminService.deactivateCustomer(id));
    }

    // ==================== HELPER METHODS ====================

    private String formatTimeAgo(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long hours = duration.toHours();
        if (hours < 1) {
            long minutes = duration.toMinutes();
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            long days = duration.toDays();
            return days + " days ago";
        }
    }

    // ==================== TEMPORARY FIX ENDPOINT ====================
    
    /**
     * TEMPORARY: Fix user roles for existing users
     * Use this to add missing CUSTOMER role to users
     */
    @PostMapping("/fix-user-role/{userId}")
    public ResponseEntity<?> fixUserRole(@PathVariable UUID userId) {
        log.warn("ADMIN: Fixing roles for user: {}", userId);
        try {
            adminService.addCustomerRoleToUser(userId);
            return ResponseEntity.ok(Map.of(
                "message", "Role CUSTOMER added successfully to user: " + userId,
                "success", true
            ));
        } catch (Exception e) {
            log.error("Failed to add role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }
}

