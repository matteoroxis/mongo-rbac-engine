# mongo-rbac-engine

A Spring Boot application that exposes full CRUD REST APIs for **Users** and **Orders**, secured by a custom **Role-Based Access Control (RBAC)** engine backed by MongoDB.

Authentication is handled via **JWT Bearer tokens**. Authorization is enforced at the service layer through an `AuthorizationService` that checks per-role permissions — with no dependency on Spring Security's built-in authorization mechanism.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 25 |
| Framework | Spring Boot 3.5.11 |
| Database | MongoDB (Spring Data MongoDB) |
| Authentication | JWT — JJWT 0.12.6 |
| Validation | Jakarta Validation (Hibernate Validator) |
| Security | Spring Security (HTTP layer disabled, RBAC is application-level) |
| Testing | JUnit 5 · Mockito · MockMvc · AssertJ |
| Build | Maven (Maven Wrapper) |

---

## Project Structure

```
src/main/java/it/matteoroxis/mongo_rbac_engine/
├── config/
│   └── SecurityConfig.java          # Disables Spring Security HTTP layer
├── controller/
│   ├── AuthController.java          # POST /auth/login
│   ├���─ UserController.java          # CRUD /users
│   └── OrderController.java         # CRUD /orders
├── document/
│   ��── UserDocument.java            # MongoDB "users" collection
│   └── OrderDocument.java           # MongoDB "orders" collection
├── domain/
│   ├── Permission.java              # Permission enum
│   ├── UserRole.java                # Role enum with permission sets
│   └── UserPrincipal.java           # Authenticated caller representation
├── dto/
│   ├── request/                     # UserRequest · OrderRequest · LoginRequest
│   └── response/                    # UserResponse · OrderResponse · LoginResponse
├── exception/
│   ├── ForbiddenException.java
│   ├── UnauthorizedException.java
│   ├── UserNotFoundException.java
│   ├── OrderNotFoundException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── mapper/
│   ├── UserMapper.java
│   └── OrderMapper.java
├── repository/
│   ├── UserRepository.java
│   └── OrderRepository.java
├── resolver/
│   └── UserPrincipalResolver.java   # Extracts UserPrincipal from JWT
├── security/
│   └── JwtService.java              # JWT sign / verify (JJWT)
└── service/
    ├── AuthorizationService.java    # Core RBAC check
    ├── UserService.java
    └── OrderService.java
```

---

## RBAC Model

### Roles and Permissions

| Permission | CUSTOMER | FINANCE | ADMIN |
|------------|:--------:|:-------:|:-----:|
| `ORDER_CREATE` | ✅ | ❌ | ✅ |
| `ORDER_CANCEL` | ✅ | ❌ | ✅ |
| `ORDER_VIEW` | ✅ | ✅ | ✅ |
| `REFUND_APPROVE` | ❌ | ✅ | ✅ |
| `USER_MANAGE` | ❌ | ❌ | ✅ |

Users can have **multiple roles** — permissions are merged from all assigned roles.

### How it works

1. Every request must carry an `Authorization: Bearer <token>` header.
2. `UserPrincipalResolver` verifies the JWT signature, extracts the `userId` and loads the `UserDocument` from MongoDB.
3. The resolved `UserPrincipal` is passed to the service layer.
4. `AuthorizationService.checkPermission(principal, permission)` checks whether any of the user's roles grant the required permission. If not, a `ForbiddenException` (HTTP 403) is thrown.

---

## Getting Started

### Prerequisites

- Java 25 (JDK)
- Maven 3.9+ (or use the included wrapper `./mvnw`)
- A running MongoDB instance (local or Atlas)

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=rbac_engine

# JWT
jwt.secret=4d6f6e676f52626163456e67696e655365637265744b657932303236212121
jwt.expiration-ms=86400000
```

> ⚠️ Replace `jwt.secret` with a secure random hex string of at least 32 bytes in production.

### Build & Run

```bash
# Using the Maven wrapper
./mvnw spring-boot:run

# Or build a JAR and run it
./mvnw package -DskipTests
java -jar target/mongo-rbac-engine-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**.

### Seed Data

On startup, the application creates a default **ADMIN** user if none exists (via `DataInitializer`). The generated MongoDB `_id` is printed in the logs:

```
✅ Default ADMIN user created with id: 6650a1b2c3d4e5f607080900
```

Use that id to obtain a JWT at `POST /auth/login`.

---

## API Reference

All endpoints except `POST /auth/login` require the header:

```
Authorization: Bearer <jwt_token>
```

### Authentication

| Method | Path | Description | Auth required |
|--------|------|-------------|:---:|
| `POST` | `/auth/login` | Issue a JWT for the given `userId` | ❌ |

**Request body:**
```json
{ "userId": "6650a1b2c3d4e5f607080900" }
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

---

### Users `/users`

| Method | Path | Required permission | Description |
|--------|------|:------------------:|-------------|
| `GET` | `/users` | *(any authenticated user)* | List all users |
| `GET` | `/users/{id}` | *(any authenticated user)* | Get user by id |
| `POST` | `/users` | `USER_MANAGE` | Create a new user |
| `PUT` | `/users/{id}` | `USER_MANAGE` | Update an existing user |
| `DELETE` | `/users/{id}` | `USER_MANAGE` | Delete a user |

**User request body (POST / PUT):**
```json
{
  "email": "alice@example.com",
  "roles": ["CUSTOMER"],
  "status": "ACTIVE"
}
```

**User response:**
```json
{
  "id": "6650a1b2c3d4e5f607080901",
  "email": "alice@example.com",
  "roles": ["CUSTOMER"],
  "status": "ACTIVE"
}
```

---

### Orders `/orders`

| Method | Path | Required permission | Description |
|--------|------|:------------------:|-------------|
| `GET` | `/orders` | `ORDER_VIEW` | List all orders |
| `GET` | `/orders/{id}` | `ORDER_VIEW` | Get order by id |
| `POST` | `/orders` | `ORDER_CREATE` | Create a new order |
| `PUT` | `/orders/{id}` | `ORDER_CREATE` | Update an existing order |
| `DELETE` | `/orders/{id}` | `ORDER_CANCEL` | Cancel an order (sets status to `CANCELLED`) |

**Order request body (POST / PUT):**
```json
{
  "userId": "6650a1b2c3d4e5f607080901",
  "description": "Order for product XYZ"
}
```

**Order response:**
```json
{
  "id": "6650a1b2c3d4e5f607080902",
  "userId": "6650a1b2c3d4e5f607080901",
  "description": "Order for product XYZ",
  "status": "PENDING",
  "createdAt": "2026-02-22T10:30:00Z"
}
```

---

## Error Responses

All errors follow a consistent JSON structure:

```json
{
  "timestamp": "2026-02-22T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: missing permission USER_MANAGE"
}
```

| HTTP Status | Cause |
|-------------|-------|
| `400 Bad Request` | Validation failure on request body |
| `401 Unauthorized` | Missing, malformed or expired JWT |
| `403 Forbidden` | Authenticated user lacks the required permission |
| `404 Not Found` | Resource (user or order) does not exist |
| `500 Internal Server Error` | Unexpected server-side error |

---

## Running the Tests

```bash
./mvnw test
```

39 tests across 7 test classes:

| Class | Type | Tests |
|-------|------|:-----:|
| `JwtServiceTest` | Unit | 4 |
| `AuthorizationServiceTest` | Unit | 5 |
| `UserServiceTest` | Unit (Mockito) | 8 |
| `OrderServiceTest` | Unit (Mockito) | 9 |
| `UserPrincipalResolverTest` | Unit (Mockito) | 5 |
| `UserControllerTest` | Web (MockMvc) | 7 |
| `OrderControllerTest` | Web (MockMvc) | 8 |

---

## Example Flow (cURL)

```bash
# 1. Login and get a JWT
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"<admin_mongo_id>"}' | jq .

# 2. Create a user (requires ADMIN)
curl -s -X POST http://localhost:8080/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","roles":["CUSTOMER"],"status":"ACTIVE"}' | jq .

# 3. Create an order (requires ORDER_CREATE)
curl -s -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":"<user_id>","description":"My first order"}' | jq .

# 4. Cancel an order (requires ORDER_CANCEL)
curl -s -X DELETE http://localhost:8080/orders/<order_id> \
  -H "Authorization: Bearer <token>"
```

---

## License

This project is provided for educational and demonstration purposes.

