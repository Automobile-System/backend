# Admin Dashboard Backend API Implementation

## Overview
This document describes the backend API implementation for the Admin Dashboard comprehensive statistics endpoint.

## Endpoint Details

### GET /api/admin/dashboard/stats
**Description:** Returns comprehensive dashboard statistics including KPIs, profit trends, job/project completion, service distribution, top employees, and business alerts.

**Authorization:** Requires `ADMIN` role

**Response Type:** `AdminDashboardStatsResponse`

## Response Structure

```json
{
  "kpis": {
    "totalCustomers": 312,
    "totalEmployees": 25,
    "totalManagers": 3,
    "ongoingJobs": 45,
    "ongoingProjects": 8,
    "monthlyRevenue": 1850000.00,
    "completedServices": 1247
  },
  "profitTrend": {
    "labels": ["Jun 2024", "Jul 2024", "Aug 2024", "Sep 2024", "Oct 2024", "Nov 2024"],
    "revenue": [1690000, 1520000, 1780000, 1650000, 1890000, 1850000],
    "cost": [760000, 680000, 800000, 740000, 850000, 820000],
    "profit": [930000, 840000, 980000, 910000, 1040000, 1030000]
  },
  "jobProjectCompletion": {
    "jobs": {
      "completed": 1247,
      "in_progress": 35,
      "on_hold": 8,
      "pending": 12
    },
    "projects": {
      "completed": 45,
      "in_progress": 6,
      "on_hold": 2,
      "pending": 3
    }
  },
  "serviceCategoryDistribution": {
    "labels": ["Periodic Service", "Electrical Repairs", "Engine Repairs", "Body Work"],
    "data": [425, 312, 278, 156]
  },
  "topEmployees": [
    {
      "employeeId": 12345,
      "name": "Ruwan Perera",
      "specialty": "Engine Specialist",
      "totalHours": 168
    }
  ],
  "alerts": [
    {
      "id": 1,
      "type": "overdue_job",
      "message": "Job #2451 is overdue by 3 days",
      "severity": "high",
      "createdAt": "2024-11-14T10:30:00",
      "isRead": false,
      "relatedId": 2451
    }
  ]
}
```

## Implementation Details

### Files Created/Modified

1. **AdminDashboardStatsResponse.java** (NEW)
   - Location: `backend/src/main/java/com/TenX/Automobile/dto/response/AdminDashboardStatsResponse.java`
   - Purpose: DTO for comprehensive dashboard statistics
   - Nested classes:
     - `DashboardKPIs`
     - `MonthlyProfitTrend`
     - `JobProjectCompletion`
     - `StatusCounts`
     - `ServiceCategoryDistribution`
     - `TopEmployeeByHours`
     - `BusinessAlert`

2. **AdminService.java** (MODIFIED)
   - Location: `backend/src/main/java/com/TenX/Automobile/service/AdminService.java`
   - New method: `getComprehensiveDashboardStats()`
   - Helper methods:
     - `calculateDashboardKPIs()`
     - `calculateMonthlyProfitTrend()`
     - `calculateJobProjectCompletion()`
     - `calculateServiceCategoryDistribution()`
     - `calculateTopEmployeesByHours()`
     - `calculateBusinessAlerts()`

3. **AdminController.java** (MODIFIED)
   - Location: `backend/src/main/java/com/TenX/Automobile/controller/AdminController.java`
   - Updated endpoint: `GET /api/admin/dashboard/stats`
   - New endpoint: `GET /api/admin/dashboard/overview` (deprecated, for backward compatibility)

### Database Queries

#### KPIs
- **Total Customers**: Count of enabled customers from `customers` table joined with `users`
- **Total Employees**: Count of users with `STAFF` role who are enabled
- **Total Managers**: Count of users with `MANAGER` role who are enabled
- **Ongoing Jobs**: Count of jobs from `jobs` table where `status != 'COMPLETED'` and `type = 'SERVICE'`
- **Ongoing Projects**: Count of jobs where `status != 'COMPLETED'` and `type = 'PROJECT'`
- **Monthly Revenue**: Sum of `p_Amount` from `payment` table for current month
- **Completed Services**: Count of jobs where `status = 'COMPLETED'`

#### Profit Trends (Last 6 Months)
- **Revenue**: Monthly sum of payments grouped by month
- **Cost**: Monthly sum of job costs grouped by month
- **Profit**: Revenue - Cost per month

#### Job & Project Completion
- Groups jobs by status and type (SERVICE/PROJECT)
- Status categories: COMPLETED, IN_PROGRESS, ON_HOLD, PENDING

#### Service Category Distribution
- Groups services by category
- Counts number of services in each category

#### Top Employees
- Aggregates total hours worked from `time_log` table
- Groups by employee
- Orders by total hours descending
- Returns top 5

#### Business Alerts
- **Overdue Jobs**: Jobs where `arriving_date < NOW()` and `status != 'COMPLETED'`
- **Delayed Projects**: Projects with status containing 'DELAYED' or 'HOLD'
- **Payment Errors**: Completed jobs without payment records

## Testing

### Backend Testing
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

### API Testing
```bash
# Test with authentication
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Content-Type: application/json" \
  --cookie "auth_cookie=your_session_cookie"
```

### Frontend Integration
The frontend `adminService.ts` already has the correct implementation:
```typescript
const response = await fetch(`${API_BASE_URL}/api/admin/dashboard/stats`, {
  method: 'GET',
  headers: { 'Content-Type': 'application/json' },
  credentials: 'include'
})
```

## Notes

- All calculations are done in real-time from the database
- No caching implemented (can be added for performance)
- Null safety warnings exist but don't affect functionality
- CORS must be configured to allow frontend origin
- Authentication cookies must be included in requests
