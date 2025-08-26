# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.3 authentication service built with Java 21, using JWT tokens for authentication and PostgreSQL for persistence. The service provides user registration, login, email verification, password reset, and file upload capabilities with MinIO storage integration.

## Common Development Commands

### Build & Run
```bash
# Build the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Build and package
./mvnw clean package

# Run tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceApplicationTests
```

### Development Setup
- Java 21 is required
- PostgreSQL database connection configured in `application.properties`
- MinIO for file storage (configuration in `minio-data/`)
- Environment variables handled via `spring-dotenv` dependency

## Architecture Overview

### Package Structure
- **config/**: Security, JWT, MinIO, Mail, and Keycloak configurations
- **controller/**: REST API endpoints (`AuthController`, `FileUploadController`, `UserProfileController`)
- **service/**: Business logic layer with interface/implementation pattern
- **repository/**: JPA repositories extending Spring Data interfaces
- **entity/**: JPA entities with auditing support via `BaseEntity`
- **dto/**: Data Transfer Objects for API requests/responses
- **mapper/**: MapStruct mappers for entity-DTO conversions
- **jwt/**: JWT token handling, filters, and security components
- **constants/**: Application enums and constants
- **exceptions/**: Custom exception classes and global exception handler

### Key Architecture Patterns

#### Entity Design
- All entities extend `BaseEntity` which provides:
  - Auto-generated ID with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - Auditing fields (createdAt, updatedAt, createdBy, updatedBy)
  - Soft delete support with `deleted` boolean flag
  - Optimistic locking with `@Version`

#### Security Configuration
- JWT-based stateless authentication
- Role-based access control with permissions
- CORS configuration allowing all origins with credentials
- Method-level security enabled via `@EnableMethodSecurity(prePostEnabled = true)`
- Custom permission validation using `@RequirePermission` annotation with AOP

#### Service Layer Pattern
- Interface/implementation separation for all services
- Transaction management at service layer
- MapStruct for entity-DTO mapping

### Database Integration
- JPA with Hibernate as ORM
- PostgreSQL as primary database
- Auditing enabled via `@EnableJpaAuditing`
- Entity relationships properly mapped with JPA annotations

### File Storage
- MinIO integration for file uploads
- Separate buckets for different file types (profile-images, patient-documents, etc.)
- File upload handling via dedicated controller and service

### Dependencies & Frameworks
- **Spring Boot 3.5.3** with Spring Security, Data JPA, Web, Mail
- **JWT**: io.jsonwebtoken (jjwt) version 0.12.3
- **MapStruct 1.5.5** for entity-DTO mapping
- **Lombok** for reducing boilerplate code
- **MinIO 8.5.17** for object storage
- **PostgreSQL** driver for database connectivity
- **Jackson** for JSON processing and XML support
- **Spring Validation** for request validation

### Testing
- Single test file: `AuthServiceApplicationTests.java`
- Uses Spring Boot Test framework
- Reactive test support included

### Configuration Management
- Main configuration in `application.properties` (currently minimal)
- Environment-specific configuration via `spring-dotenv`
- Keycloak integration prepared but currently commented out in dependencies