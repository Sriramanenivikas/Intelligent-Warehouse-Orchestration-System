# Auth Service - JWT Authentication Implementation

## Overview
Complete JWT authentication system implemented for the IWOS Auth Service with full authentication, authorization, and token management capabilities.

## Implementation Summary

### 1. DTOs (Data Transfer Objects)
Created complete DTOs in `/src/main/java/com/iwos/dto/`:

- **LoginRequest.java** - Login credentials (username, password)
- **RegisterRequest.java** - Registration data (username, password, email, firstName, lastName)
- **LoginResponse.java** - Authentication response with access token, refresh token, expiration, and user info
- **RefreshTokenRequest.java** - Refresh token request
- **UserInfo.java** - User information DTO
- **MessageResponse.java** - Generic API response messages

All DTOs include:
- Jakarta validation annotations (@NotBlank, @Email, @Size)
- Lombok annotations for boilerplate code reduction
- Builder pattern for easy object creation

### 2. JWT Token Provider
Created `/src/main/java/com/iwos/util/JwtTokenProvider.java`:

**Key Methods:**
- `generateAccessToken(username, roles)` - Creates access token with 30-minute expiration
- `generateRefreshToken(username)` - Creates refresh token with 7-day expiration
- `validateToken(token)` - Validates JWT token signature and expiration
- `extractUsername(token)` - Extracts username from token
- `extractRoles(token)` - Extracts user roles from token
- `extractTokenType(token)` - Distinguishes between access and refresh tokens
- `isTokenExpired(token)` - Checks token expiration
- `getExpirationDate(token)` - Gets token expiration date

**Features:**
- Uses HS256 algorithm with HMAC-SHA256
- Configurable secret key and expiration times
- Comprehensive error handling
- Token type differentiation (access vs refresh)
- UUID-based token IDs

### 3. Security Configuration
Created Spring Security configuration in `/src/main/java/com/iwos/config/`:

**SecurityConfig.java:**
- Stateless session management (JWT-based)
- CORS configuration with configurable allowed origins
- BCrypt password encoder
- Public endpoints: /auth/login, /auth/register, /auth/refresh, /auth/health
- Protected endpoints: /auth/me, /auth/logout (require authentication)
- Swagger/OpenAPI endpoints accessible without authentication

**JwtAuthenticationFilter.java:**
- OncePerRequestFilter implementation
- Extracts JWT from Authorization header (Bearer token)
- Validates token and sets Spring Security context
- Converts roles to Spring Security authorities

**JpaAuditingConfig.java:**
- Enables automatic auditing for entities
- Auto-populates @CreatedDate and @LastModifiedDate fields

### 4. Entity Updates

**User.java:**
- Added ManyToMany relationship with Role entity
- Added audit fields (createdAt, updatedAt, lastLogin)
- Added database indexes for performance
- Proper constraints and validation

**Role.java:**
- Added audit fields (createdAt, updatedAt)
- Proper constraints and validation

**RefreshToken.java:**
- Changed from userId to proper ManyToOne relationship with User
- Added audit fields (createdAt)
- Added database indexes for token and user_id
- Proper revocation support

### 5. Repository Enhancements

**UserRepository.java:**
- `findByUsername(String username)` - Find user by username
- `findByEmail(String email)` - Find user by email
- `existsByUsername(String username)` - Check username existence
- `existsByEmail(String email)` - Check email existence
- `findActiveUserByUsername(String username)` - Find active users only

**RoleRepository.java:**
- `findByName(String name)` - Find role by name
- `existsByName(String name)` - Check role existence

**RefreshTokenRepository.java:**
- `findByToken(String token)` - Find refresh token
- `findValidToken(String token, LocalDateTime now)` - Find valid (non-revoked, non-expired) token
- `deleteByUser(User user)` - Delete all user tokens
- `revokeAllUserTokens(User user)` - Revoke all user tokens
- `deleteExpiredTokens(LocalDateTime now)` - Cleanup expired tokens

### 6. Service Layer

**UserService.java:**
- `registerUser(RegisterRequest)` - Complete user registration with password hashing
- `authenticateUser(username, password)` - User authentication with BCrypt verification
- `updateLastLogin(username)` - Updates last login timestamp
- `getUserByUsername(username)` - Retrieve user by username
- `toUserInfo(User)` - Convert User entity to UserInfo DTO
- Automatic role assignment (creates USER role if not exists)

**RefreshTokenService.java:**
- `createRefreshToken(User)` - Generate and store refresh token
- `verifyRefreshToken(String token)` - Validate refresh token
- `revokeToken(String token)` - Revoke specific token
- `revokeAllUserTokens(User)` - Revoke all user tokens
- `deleteExpiredTokens()` - Cleanup job for expired tokens

### 7. Authentication Controller
Created `/src/main/java/com/iwos/controller/AuthController.java`:

**Endpoints:**

1. **POST /auth/register** - User registration
   - Validates input data
   - Creates user with encrypted password
   - Assigns default USER role
   - Returns access token, refresh token, and user info

2. **POST /auth/login** - User authentication
   - Validates credentials
   - Generates access and refresh tokens
   - Updates last login timestamp
   - Returns tokens and user info

3. **POST /auth/refresh** - Refresh access token
   - Validates refresh token
   - Generates new access token
   - Returns new access token with user info

4. **POST /auth/logout** - User logout
   - Revokes refresh token
   - Returns success message

5. **GET /auth/me** - Get current user
   - Requires authentication
   - Returns current user information

6. **GET /auth/health** - Health check
   - Public endpoint
   - Returns service status

**Features:**
- Full validation using Jakarta validation
- Comprehensive error handling
- OpenAPI/Swagger documentation
- Proper HTTP status codes

### 8. Exception Handling
Created custom exceptions in `/src/main/java/com/iwos/exception/`:

- **AuthenticationException** - Authentication failures
- **TokenException** - Token validation/processing errors
- **UserAlreadyExistsException** - Duplicate user registration
- **GlobalExceptionHandler** - Centralized exception handling with proper HTTP responses

### 9. Configuration (application.yml)

**JWT Configuration:**
```yaml
jwt:
  secret: ${JWT_SECRET:iwos-super-secret-key-change-in-production-min-256-bits-required-for-hs256-algorithm}
  access-token-expiration: 1800000  # 30 minutes
  refresh-token-expiration: 604800000  # 7 days
  issuer: iwos-auth-service
```

**Security Configuration:**
```yaml
security:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:8080
```

### 10. Dependencies
All required dependencies already present in pom.xml:
- Spring Boot Web
- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- Redis
- JWT (jjwt-api, jjwt-impl, jjwt-jackson)
- Validation
- OpenAPI/Swagger
- Kafka
- MapStruct

## API Usage Examples

### 1. Register New User
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass123",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass123"
  }'
```

### 3. Refresh Token
```bash
curl -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token-here"
  }'
```

### 4. Get Current User
```bash
curl -X GET http://localhost:8081/auth/me \
  -H "Authorization: Bearer your-access-token-here"
```

### 5. Logout
```bash
curl -X POST http://localhost:8081/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your-refresh-token-here"
  }'
```

## Security Features

1. **Password Security**
   - BCrypt password hashing
   - Minimum 6 characters password requirement
   - Never stores plain-text passwords

2. **Token Security**
   - HMAC-SHA256 signing algorithm
   - Short-lived access tokens (30 minutes)
   - Long-lived refresh tokens (7 days)
   - Token revocation support
   - Token type validation

3. **API Security**
   - Stateless JWT authentication
   - CORS protection
   - CSRF disabled (stateless API)
   - Role-based access control ready
   - Rate limiting ready (Redis available)

4. **Input Validation**
   - Jakarta validation on all DTOs
   - Email format validation
   - Username/password requirements
   - SQL injection protection (JPA)

## Architecture Principles

### SOLID Principles Applied:
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extensible through interfaces
- **Liskov Substitution**: Proper inheritance hierarchy
- **Interface Segregation**: Focused repository interfaces
- **Dependency Inversion**: Services depend on repository abstractions

### Design Patterns Used:
- **Builder Pattern**: DTOs and entities
- **Repository Pattern**: Data access layer
- **Service Layer Pattern**: Business logic separation
- **Filter Chain Pattern**: JWT authentication filter
- **DTO Pattern**: Data transfer objects

## Database Schema

### Tables Created:
- **users** - User accounts
- **roles** - User roles
- **user_roles** - Many-to-many join table
- **refresh_tokens** - Refresh token storage

### Indexes:
- users: username, email
- refresh_tokens: token, user_id

## Testing Recommendations

1. **Unit Tests**
   - JwtTokenProvider token generation and validation
   - UserService registration and authentication
   - RefreshTokenService token management

2. **Integration Tests**
   - AuthController endpoints
   - Security configuration
   - Database operations

3. **Security Tests**
   - JWT token validation
   - Password hashing
   - Authorization rules

## Production Considerations

1. **Environment Variables**
   - Set JWT_SECRET environment variable
   - Use strong secret key (min 256 bits)
   - Configure proper CORS origins

2. **Database**
   - Run database migrations
   - Create initial roles (USER, ADMIN, etc.)
   - Set up proper indexes

3. **Monitoring**
   - Enable actuator endpoints
   - Configure Prometheus metrics
   - Set up logging aggregation

4. **Performance**
   - Enable Redis caching for tokens
   - Implement rate limiting
   - Monitor token validation performance

## Next Steps

1. **Implement Role Management**
   - Admin endpoints for role creation
   - Role assignment functionality
   - Permission-based access control

2. **Add Features**
   - Email verification
   - Password reset
   - Two-factor authentication
   - OAuth2 integration

3. **Monitoring & Logging**
   - Failed login attempt tracking
   - Token usage analytics
   - Security audit logs

4. **Testing**
   - Write comprehensive unit tests
   - Add integration tests
   - Perform security testing

## Files Created/Modified

### Created:
- /dto/LoginRequest.java
- /dto/RegisterRequest.java
- /dto/LoginResponse.java
- /dto/RefreshTokenRequest.java
- /dto/UserInfo.java
- /dto/MessageResponse.java
- /util/JwtTokenProvider.java
- /config/SecurityConfig.java
- /config/JwtAuthenticationFilter.java
- /config/JpaAuditingConfig.java
- /exception/AuthenticationException.java
- /exception/TokenException.java
- /exception/UserAlreadyExistsException.java
- /exception/GlobalExceptionHandler.java

### Modified:
- /entity/User.java (added roles, audit fields)
- /entity/Role.java (added audit fields)
- /entity/RefreshToken.java (proper relationships)
- /repository/UserRepository.java (custom queries)
- /repository/RoleRepository.java (custom queries)
- /repository/RefreshTokenRepository.java (custom queries)
- /service/UserService.java (authentication logic)
- /service/RefreshTokenService.java (JWT logic)
- /controller/AuthController.java (complete implementation)

## Conclusion

The Auth Service is now fully functional with:
- Complete JWT authentication
- User registration and login
- Token refresh mechanism
- Secure password handling
- Role-based authorization foundation
- Production-ready security configuration
- Comprehensive error handling
- API documentation with Swagger

All code follows SOLID principles, uses proper design patterns, and is ready for production deployment after proper testing and environment configuration.
