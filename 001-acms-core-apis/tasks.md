# Tasks: ACMS Core APIs

**Input**: Design documents from [/specs/001-acms-core-apis/](cci:7://file:///C:/Users/SaiVamshiGudem/specs/001-acms-core-apis:0:0-0:0)  
**Prerequisites**: plan.md, spec.md, research.md  
**Tests**: Included as per Test-First Automation requirement  
**Organization**: Tasks are grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Maven project structure per implementation plan
  - Created Maven project structure with standard Maven layout
  - Set up parent POM with Spring Boot 3.1.3 and Java 17
- [X] T002 Initialize Spring Boot 3.1.3 project with Java 17
  - Created main application class `AcmsApplication` with `@SpringBootApplication`
  - Configured main class with `@EnableCaching`, `@EnableAsync`, and `@EnableScheduling`
- [X] T003 [P] Configure Maven dependencies in `pom.xml`
  - Added Spring Boot starters (web, data-jpa, validation, actuator)
  - Added H2 database, Flyway, Lombok, MapStruct, and other required dependencies
  - Configured Maven plugins (compiler, jacoco, etc.)
- [X] T004 [P] Setup `application.yml` with H2 database configuration
  - Configured H2 database with console access
  - Set up JPA/Hibernate properties
  - Configured logging, server settings, and other application properties
- [X] T005 [P] Configure Lombok annotation processing in `pom.xml`
  - Added Lombok dependency and annotation processor
  - Configured Maven compiler plugin to work with Lombok
- [X] T006 [P] Setup MapStruct annotation processing in `pom.xml`
  - Added MapStruct dependency and annotation processor
  - Configured Maven compiler plugin for MapStruct
- [X] T007 [P] Configure Spring Boot DevTools for auto-reload
  - Added spring-boot-devtools dependency
  - Configured for development-time auto-reload
- [X] T008 Create package structure: `com.acms.controller`, `dto`, `service`, `repository`, `model`, `config`, `exception`, `cache`
  - Created all required packages
  - Set up base classes and configurations in respective packages
- [X] T009 [P] Setup logging configuration with JSON formatting
  - Configured JSON logging in application.yml
  - Set up log levels and file output
- [X] T010 [P] Configure SpringDoc OpenAPI documentation
  - Added springdoc-openapi-starter-webmvc-ui dependency
  - Configured OpenAPI 3.0 documentation in application.yml

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story

- [X] T011 Create base exception classes in `com.acms.exception`
  - Created `BaseException` as the root exception
  - Implemented `ResourceNotFoundException` for 404 scenarios
  - Created `ApiError` class for standardized error responses
- [X] T012 [P] Implement `GlobalExceptionHandler` with `@ControllerAdvice`
  - Created comprehensive exception handler with proper HTTP status codes
  - Added support for validation errors, data integrity violations, etc.
  - Implemented detailed error responses with timestamps and error details
- [X] T013 [P] Create `ApiResponse` wrapper class in `com.acms.dto`
  - Implemented generic `ApiResponse` class for consistent API responses
  - Added builder pattern and static factory methods
  - Included support for pagination and error responses
- [X] T018 [P] Create base repository interface with common methods
  - Created `BaseRepository` interface extending `JpaRepository`
  - Added common methods like `findAllByActiveTrue`
  - Implemented soft delete functionality
- [X] T019 Implement audit fields (`createdAt`, `updatedAt`) with `@EntityListeners`
  - Created `AuditEntity` base class with audit fields
  - Configured `@EntityListeners(AuditingEntityListener.class)`
  - Added `@PrePersist` and `@PreUpdate` hooks

## Phase 2: In Progress (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story

- [X] T014 [P] Setup database migration with Flyway
  - Created initial migration script `V1.0.0__initial_schema.sql`
  - Configured Flyway in application.yml
  - Set up tables for agents, policies, commissions, and payments
  - Added necessary indexes for performance
- [X] T015 Configure CORS in `WebConfig`
  - Implemented `WebMvcConfigurer` for CORS configuration
  - Allowed origins: http://localhost:3000 (React) and http://localhost:4200 (Angular)
  - Configured allowed methods, headers, and credentials
- [X] T016 [P] Setup monitoring with Actuator and Prometheus
  - Added Actuator and Micrometer Prometheus dependencies
  - Configured detailed metrics collection and exposure
  - Set up health checks and monitoring endpoints
  - Added custom metrics configuration for HTTP requests
- [X] T017 [P] Configure Caffeine cache in `CacheConfig`
  - Created `CacheConfig` class with Caffeine settings
  - Configured cache names for agents, policies, and commissions
  - Set up cache eviction policies and statistics
  - Enabled Spring's caching abstraction
- [X] T020 Setup test containers for integration testing
  - Added Testcontainers with PostgreSQL
  - Created `TestContainerConfig` for test database setup
  - Configured test properties for containerized testing
  - Set up test database connection

## Phase 3: User Story 1 - Agent Management (Priority: P1) MVP

**Goal**: Manage agent records with CRUD operations

### Implementation Plan
1. Create Agent entity and DTOs
2. Implement repository with custom queries
3. Create service layer with business logic
4. Implement REST controller with validation
5. Add exception handling and logging
6. Write comprehensive tests

### Tests for User Story 1

- [X] T021 [P] [US1] Create `AgentControllerTest` with test cases
  - Test CRUD operations
  - Test validation and error cases
  - Test pagination and filtering
  - Test security constraints
- [X] T022 [P] [US1] Create `AgentServiceTest` with test cases
  - Test business logic for agent operations
  - Test validation and error handling
  - Test caching behavior
  - Test transaction management
- [X] T023 [P] [US1] Create `AgentRepositoryTest` with test cases
  - Test custom query methods
  - Test soft delete functionality
  - Test pagination and search
  - Test database interactions

### Implementation for User Story 1

- [X] T024 [P] [US1] Create `Agent` entity in `com.acms.model.Agent`
  - Created with all required fields and validations
  - Added JPA annotations and audit fields
  - Implemented soft delete functionality
  - Added status enum and helper methods

- [X] T025 [P] [US1] Create `AgentDTO` in `com.acms.dto.AgentDTO`
  - Created request/response DTO with validation
  - Added Swagger/OpenAPI documentation
  - Included proper date formatting
  - Added null-safety with @JsonInclude

- [X] T026 [P] [US1] Create `AgentMapper` in `com.acms.mapper.AgentMapper`
  - Implemented mapping between entity and DTO
  - Added custom mapping logic for complex fields
  - Included null property handling
  - Added after-mapping hooks for defaults

- [X] T027 [P] [US1] Create `AgentRepository` in `com.acms.repository.AgentRepository`
  - Extended BaseRepository
  - Added custom query methods
  - Added pagination support
  - Added filtering capabilities

- [X] T028 [US1] Implement `AgentService` in `com.acms.service.AgentService`
  - Business logic for agent operations
  - Validation and error handling
  - Transaction management

- [X] T029 [US1] Implement `AgentController` in `com.acms.controller.AgentController`
  - REST endpoints for agent management
  - Input validation
  - Error handling
  - Response formatting

- [X] T030 [US1] Add validation annotations to `AgentDTO`
  - Added @NotBlank, @Size, @Email, @Pattern validations
  - Included custom validation messages
  - Added cross-field validation where needed
  - Integrated with Spring's validation framework

- [X] T031 [US1] Implement agent status update endpoint
  - Status transition validation
  - Business rules enforcement
  - Audit logging
  - Notifications

- [X] T032 [US1] Add OpenAPI documentation for agent endpoints
  - Operation descriptions
  - Request/response schemas
  - Security requirements
  - Example values

- [X] T033 [US1] Add request/response examples in OpenAPI
  - Sample agent creation
  - Status update examples
  - Error responses
  - Filtering examples

## Phase 4: User Story 2 - Policy Management (Priority: P1)

**Goal**: Create and manage group insurance policies

**Independent Test**: Create, retrieve, update, and deactivate a policy via API

### Implementation Plan
1. Create Policy entity and DTOs with validation
2. Implement repository with custom queries for policies
5. Add policy status management
6. Write comprehensive tests

### Tests for User Story 2

- [X] T034 [P] [US2] Create `PolicyControllerTest` with test cases
  - Test policy CRUD operations
  - Test policy status transitions
  - Test validation and error cases
  - Test policy-agent relationships

- [X] T035 [P] [US2] Create `PolicyServiceTest` with test cases
  - Test business logic for policy operations
  - Test commission calculations
  - Test concurrent policy updates
  - Test validation and business rules

- [X] T036 [P] [US2] Create `PolicyRepositoryTest` with test cases
  - Test custom query methods
  - Test soft delete functionality
  - Test query performance
  - Test database interactions

### Implementation for User Story 2

- [X] T037 [P] [US2] Create `Policy` entity in `com.acms.model.Policy`
  - Core policy details (number, type, status)
  - Coverage details and limits
  - Effective/expiration dates
  - Relationships with Agent and Commission
  - Status enum and validation

- [X] T038 [P] [US2] Create `PolicyDTO` in `com.acms.dto.PolicyDTO`
  - Request/response DTOs
  - Validation constraints
  - Swagger documentation
  - Proper date handling
  - Agent summary nested DTO

- [X] T039 [P] [US2] Create `PolicyMapper` in `com.acms.mapper.PolicyMapper`
  - Entity-DTO mapping
  - Custom mapping logic
  - Null safety
  - Nested object mapping
  - Agent summary mapping

- [X] T040 [P] [US2] Create `PolicyRepository` in `com.acms.repository.PolicyRepository`
  - Extend BaseRepository
  - Add custom query methods
  - Add pagination support
  - Add filtering capabilities
  - Date range queries

- [X] T041 [US2] Implement `PolicyService` in `com.acms.service.PolicyService`
  - Business logic for policy operations
  - Commission calculations
  - Status management
  - Transaction management
  - Policy date validation
  - Status transition validation

- [X] T042 [US2] Implement `PolicyController` in `com.acms.controller.PolicyController`
  - REST endpoints for policy management
  - Input validation
  - Error handling
  - Response formatting
  - Date range filtering

- [X] T043 [US2] Add validation annotations to `PolicyDTO`
  - Field-level validations
  - Cross-field validations
  - Custom validators
  - Error messages
  - Monetary format validation

- [X] T044 [US2] Implement policy status update endpoint
  - Status transition validation
  - Business rules enforcement
  - Audit logging
  - Notifications
  - Cancellation handling

- [X] T045 [US2] Add OpenAPI documentation for policy endpoints
  - Operation descriptions
  - Request/response schemas
  - Security requirements
  - Example values
  - Parameter documentation

- [X] T046 [US2] Add request/response examples in OpenAPI
  - Sample policy creation
  - Status update examples
  - Error responses
  - Filtering examples
  - Date range examples

### Tests for User Story 3

- [ ] T047 [P] [US3] Create `CommissionServiceTest` with test cases
- [ ] T048 [P] [US3] Create `CommissionCalculatorTest` with test cases

### Implementation for User Story 3

- [ ] T049 [P] [US3] Create `Commission` entity in `com.acms.model.Commission`
- [ ] T050 [P] [US3] Create `CommissionDTO` in `com.acms.dto.CommissionDTO`
- [ ] T051 [P] [US3] Create `CommissionMapper` in `com.acms.mapper.CommissionMapper`
- [ ] T052 [P] [US3] Create `CommissionRepository` in `com.acms.repository.CommissionRepository`
- [ ] T053 [US3] Implement `CommissionCalculator` in `com.acms.service.CommissionCalculator`
- [ ] T054 [US3] Implement `CommissionService` in `com.acms.service.CommissionService`
- [ ] T055 [US3] Create `CommissionController` in `com.acms.controller.CommissionController`
- [ ] T056 [US3] Implement commission calculation endpoint
- [ ] T057 [US3] Add commission calculation rules in `application.yml`
- [ ] T058 [US3] Add OpenAPI documentation for commission endpoints

## Phase 6: User Story 4 - Payment Tracking (Priority: P2)

**Goal**: Record and track commission payments

**Independent Test**: Create and retrieve payment records

### Tests for User Story 4

- [ ] T059 [P] [US4] Create `PaymentControllerTest` with test cases
- [ ] T060 [P] [US4] Create `PaymentServiceTest` with test cases

### Implementation for User Story 4

- [ ] T061 [P] [US4] Create `Payment` entity in `com.acms.model.Payment`
- [ ] T062 [P] [US4] Create `PaymentDTO` in `com.acms.dto.PaymentDTO`
- [ ] T063 [P] [US4] Create `PaymentMapper` in `com.acms.mapper.PaymentMapper`
- [ ] T064 [P] [US4] Create `PaymentRepository` in `com.acms.repository.PaymentRepository`
- [ ] T065 [US4] Implement `PaymentService` in `com.acms.service.PaymentService`
- [ ] T066 [US4] Implement `PaymentController` in `com.acms.controller.PaymentController`
- [ ] T067 [US4] Implement payment status update endpoint
- [ ] T068 [US4] Add OpenAPI documentation for payment endpoints

## Phase 7: User Story 5 - Agent Performance Summary (Priority: P2)

**Goal**: View agent performance metrics and summaries

**Independent Test**: Retrieve agent performance summary

### Tests for User Story 5

- [ ] T069 [P] [US5] Create `ReportServiceTest` with test cases
- [ ] T070 [P] [US5] Create `ReportControllerTest` with test cases

### Implementation for User Story 5

- [ ] T071 [P] [US5] Create `PerformanceSummaryDTO` in `com.acms.dto.PerformanceSummaryDTO`
- [ ] T072 [P] [US5] Create `ReportService` in `com.acms.service.ReportService`
- [ ] T073 [US5] Implement `ReportController` in `com.acms.controller.ReportController`
- [ ] T074 [US5] Add performance summary endpoint
- [ ] T075 [US5] Implement commission summary by date range
- [ ] T076 [US5] Add OpenAPI documentation for report endpoints

## Phase 8: Polish & Cross-Cutting Concerns

- [ ] T077 [P] Add API versioning
- [ ] T078 [P] Implement request/response logging
- [ ] T079 [P] Add rate limiting
- [ ] T080 [P] Implement API key authentication
- [ ] T081 [P] Add health check endpoints
- [ ] T082 [P] Create API documentation in `README.md`
- [ ] T083 [P] Add Swagger UI customization
- [ ] T084 [P] Implement request validation
- [ ] T085 [P] Add performance metrics
- [ ] T086 [P] Create database indexes for performance

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: No dependencies
- **Foundational (Phase 2)**: Depends on Setup
- **User Stories (Phases 3-7)**: Depend on Foundational
- **Polish (Phase 8)**: Depends on all User Stories

### Parallel Opportunities
- All [P] marked tasks can run in parallel
- User stories can be developed in parallel after Foundational phase
- Within each user story: models → repositories → services → controllers

## Implementation Strategy

1. **MVP First (User Story 1 Only)**
   - Complete Phases 1-3
   - Test and validate core agent management
   - Deploy as initial MVP

2. **Incremental Delivery**
   - Add User Story 2 (Policies) → Test → Deploy
   - Add User Story 3 (Commissions) → Test → Deploy
   - Add remaining stories

3. **Parallel Team Strategy**
   - Team A: Core features (Phases 1-3)
   - Team B: Advanced features (Phases 4-5)
   - Team C: Reporting & Analytics (Phases 6-7)

## Notes
- All tasks include file paths for clarity
- Each user story is independently testable
- Follow test-first approach where tests are defined
- Commit after each task or logical group
- Run integration tests before each commit
