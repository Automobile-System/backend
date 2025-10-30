# Automobile Enterprise System - Spring Security RBAC Implementation

## Overview
This is an enterprise-level Spring Boot application implementing comprehensive Role-Based Access Control (RBAC) with JWT authentication, BCrypt password encoding, and remember-me functionality.

## 🚀 Features

### Security Features
- ✅ **JWT Authentication** - Stateless token-based authentication
- ✅ **BCrypt Password Encoding** - Industry-standard password hashing (strength: 12)
- ✅ **Remember Me** - Extended session support with token rotation
- ✅ **Account Lockout** - Automatic account locking after 5 failed login attempts
- ✅ **Token Refresh** - Secure token rotation mechanism
- ✅ **Login Attempt Tracking** - IP-based login attempt monitoring
- ✅ **CORS Configuration** - Configured for cross-origin requests
- ✅ **Role-Based Access Control** - 4-tier role hierarchy

### Role Hierarchy
1. **ADMIN** - Full system access
2. **MANAGER** - Management operations
3. **STAFF** - Staff-level operations
4. **CUSTOMER** - Customer self-service

## 📁 Project Structure

```
src/main/java/com/TenX/Automobile/
├── config/
│   ├── AppConfig.java                    # General application configuration
│   └── SecurityConfig.java               # Security & RBAC configuration
├── controller/
│   ├── AuthController.java               # Authentication endpoints
│   ├── AdminController.java              # Admin-only endpoints
│   ├── ManagerController.java            # Manager & Admin endpoints
│   ├── StaffController.java              # Staff, Manager & Admin endpoints
│   ├── CustomerController.java           # All authenticated users
│   └── EmployeeController.java           # Employee operations
├── dto/
│   └── auth/
│       ├── LoginRequest.java             # Login request DTO
│       ├── LoginResponse.java            # Login response with tokens
│       ├── RefreshTokenRequest.java      # Token refresh request
│       ├── RefreshTokenResponse.java     # Token refresh response
│       ├── LogoutRequest.java            # Logout request
│       ├── LogoutResponse.java           # Logout response
│       └── UserInfoResponse.java         # Current user info
├── entity/
│   ├── UserEntity.java                   # Base user entity
│   ├── Customer.java                     # Customer entity
│   ├── Employee.java                     # Employee entity
│   ├── Admin.java                        # Admin entity
│   ├── RefreshToken.java                # Refresh token entity
│   └── LoginAttempt.java                 # Login attempt tracking
├── enums/
│   ├── Role.java                         # User roles enum
│   └── EmployeeType.java                 # Employee types
├── exception/
│   ├── GlobalExceptionHandler.java       # Centralized exception handling
│   ├── AccountLockedException.java       # Account lockout exception
│   ├── InvalidTokenException.java        # Invalid JWT token
│   ├── InvalidCredentialsException.java  # Invalid login credentials
│   └── RefreshTokenException.java        # Refresh token errors
├── repository/
│   ├── BaseUserRepository.java           # User repository
│   ├── RefreshTokenRepository.java       # Refresh token repository
│   └── LoginAttemptRepository.java       # Login attempt repository
├── security/
│   ├── config/
│   │   └── JwtProperties.java            # JWT configuration properties
│   ├── constants/
│   │   └── SecurityConstants.java        # Security constants
│   ├── jwt/
│   │   └── JwtTokenProvider.java         # JWT token generation/validation
│   └── JwtAuthenticationFilter.java      # JWT authentication filter
└── service/
    ├── AuthService.java                  # Authentication service
    ├── RefreshTokenService.java          # Refresh token management
    ├── LoginAttemptService.java          # Login attempt tracking
    ├── MyUserDetailsService.java         # UserDetails service
    └── CustomerService.java              # Customer operations
```

## 🔐 Authentication Flow

### 1. Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "rememberMe": true
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "d4f7a8b2-3c1e-4f6d-9a2b-7e5f8c9d0a1b",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "userId": "uuid-here",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["CUSTOMER"],
  "lastLoginAt": "2025-10-20T10:30:00",
  "rememberMe": true,
  "message": "Login successful"
}
```

### 2. Use Access Token
```http
GET /api/v1/customer/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 3. Refresh Token
```http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "d4f7a8b2-3c1e-4f6d-9a2b-7e5f8c9d0a1b"
}
```

### 4. Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "refreshToken": "d4f7a8b2-3c1e-4f6d-9a2b-7e5f8c9d0a1b",
  "revokeAllTokens": false
}
```

## 🛡️ RBAC Configuration

### Role-Based Endpoints

#### Public Endpoints (No Authentication)
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/refresh-token` - Refresh access token
- `GET /api/v1/auth/health` - Health check

#### Customer Endpoints (All Authenticated Users)
- `GET /api/v1/customer/profile` - View own profile
- `GET /api/v1/customer/services` - View service history
- `POST /api/v1/customer/appointments` - Book appointment
- `PUT /api/v1/customer/profile` - Update profile
- `POST /api/v1/customer/feedback` - Submit feedback

#### Staff Endpoints (STAFF, MANAGER, ADMIN)
- `GET /api/v1/staff/tasks` - View assigned tasks
- `PUT /api/v1/staff/services/{id}/status` - Update service status
- `GET /api/v1/staff/customers/{id}` - View customer info
- `POST /api/v1/staff/services` - Create service record

#### Manager Endpoints (MANAGER, ADMIN)
- `GET /api/v1/manager/staff/performance` - View staff performance
- `POST /api/v1/manager/services/{id}/approve` - Approve services
- `GET /api/v1/manager/reports` - View reports
- `PUT /api/v1/manager/inventory/{id}` - Manage inventory

#### Admin Endpoints (ADMIN Only)
- `GET /api/v1/admin/users` - Get all users
- `PUT /api/v1/admin/users/{id}/roles` - Update user roles
- `GET /api/v1/admin/system/config` - System configuration
- `GET /api/v1/admin/audit-logs` - View audit logs
- `DELETE /api/v1/admin/users/{id}` - Delete user

## ⚙️ Configuration

### application.properties

```properties
# JWT Configuration
app.security.jwt.secret-key=AutomobileEnterpriseSecretKeyForJWTTokenGenerationAndValidation2024SecureKey
app.security.jwt.access-token-validity=900000
app.security.jwt.refresh-token-validity=604800000
app.security.jwt.remember-me-token-validity=2592000000
app.security.jwt.issuer=automobile-enterprise-system
app.security.jwt.audience=automobile-web-app

# Security Configuration
app.security.max-login-attempts=5
app.security.account-lock-duration=1800000
app.security.password-min-length=8
app.security.password-max-length=100

# CORS Configuration
app.security.cors.allowed-origins=http://localhost:3000,http://localhost:4200
app.security.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
app.security.cors.allowed-headers=*
app.security.cors.allow-credentials=true
app.security.cors.max-age=3600
```

## 🔧 Key Security Features Explained

### 1. Account Lockout Mechanism
- Accounts are locked after 5 failed login attempts
- Lock duration: 30 minutes
- Automatic unlock after duration expires
- IP-based tracking prevents distributed attacks

### 2. Token Rotation
- Refresh tokens are rotated on each use
- Old refresh tokens are immediately revoked
- Prevents token reuse attacks

### 3. Remember Me
- Extended token validity (30 days)
- Secure token storage
- Automatic cleanup of expired tokens

### 4. Scheduled Tasks
- **Token Cleanup**: Daily at 2 AM - removes expired/revoked tokens
- **Login Attempt Cleanup**: Daily at 3 AM - removes old login attempts

## 🚀 Getting Started

### Prerequisites
- Java 21
- PostgreSQL database
- Maven

### Running the Application

1. **Clone the repository**
```bash
git clone <repository-url>
cd backend
```

2. **Configure database**
Update `application.properties` with your database credentials.

3. **Build the project**
```bash
./mvnw clean install
```

4. **Run the application**
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## 📝 Testing with Postman/cURL

### Login Example
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "rememberMe": true
  }'
```

### Access Protected Resource
```bash
curl -X GET http://localhost:8080/api/v1/customer/profile \
  -H "Authorization: Bearer <your-access-token>"
```

## 🔒 Security Best Practices Implemented

1. ✅ Stateless JWT authentication
2. ✅ BCrypt password hashing (strength 12)
3. ✅ Token expiration and rotation
4. ✅ Account lockout protection
5. ✅ CORS configuration
6. ✅ Method-level security with @PreAuthorize
7. ✅ Centralized exception handling
8. ✅ Secure password validation
9. ✅ IP-based login tracking
10. ✅ Automated token cleanup

## 📊 Database Schema

### Users Table
- id (UUID, PK)
- email (unique)
- password (BCrypt hashed)
- first_name, last_name
- roles (user_roles table)
- enabled, account_non_expired, account_non_locked
- failed_login_attempts
- locked_until
- last_login_at, last_login_ip
- created_at, updated_at

### Refresh Tokens Table
- id (UUID, PK)
- user_id (FK)
- token (unique)
- expiry_date
- revoked
- remember_me
- ip_address, user_agent

### Login Attempts Table
- id (UUID, PK)
- email
- ip_address
- attempt_time
- success
- failure_reason

## 🎯 Future Enhancements

- [ ] Email verification
- [ ] Two-factor authentication (2FA)
- [ ] Password reset functionality
- [ ] OAuth2/Social login
- [ ] Rate limiting per endpoint
- [ ] API documentation with Swagger/OpenAPI
- [ ] Redis caching for tokens
- [ ] Audit logging improvements

## 📄 License

This project is part of the Automobile Enterprise System.

## 👥 Authors

TenX Development Team

---

**Note**: This is an enterprise-level implementation. Ensure you update the JWT secret key and database credentials before deploying to production.
