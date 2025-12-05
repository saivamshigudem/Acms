# Implementation Plan: ACMS Core APIs

**Feature Branch**: `001-acms-core-apis` | **Date**: 2025-12-04 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-acms-core-apis/spec.md`
**Conforms to**: [ACMS Constitution](../.specify/memory/constitution.md)

## Summary

Create comprehensive REST APIs for Agent Commission Management System (ACMS) with CRUD operations for agents, policies, commissions, and payments. The system will provide OpenAPI 3.0 compliant endpoints with automatic commission calculation, payment tracking, and agent performance summaries. Built using Java Spring Boot 3.1.3 with H2 in-memory database for development.

## Technical Context

**Language/Version**: Java 17 (LTS)
**Primary Dependencies**: Spring Boot 3.1.3, Spring Data JPA, Springdoc OpenAPI 1.6.14, H2 Database, Caffeine 3.1.8
**Storage**: H2 in-memory database (development), configurable for production databases
**Testing**: JUnit 5, Mockito, Spring Boot Test
**Target Platform**: Linux server (Java 17 runtime)
**Project Type**: web (REST API backend)
**Performance Goals**: 200ms average response time, 10,000 requests per minute
**Constraints**: <200ms p95 response time, 100% commission calculation accuracy, OpenAPI 3.0 compliance
**Scale/Scope**: 10,000 requests/minute, 4 core entities (Agent, Policy, Commission, Payment)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### API-First Design Compliance
✅ **PASSED**: All system features exposed via RESTful APIs compliant with OpenAPI 3.0
- SpringDoc OpenAPI 1.6.14 configuration for interactive documentation
- OpenAPI 3.0 spec at `/v3/api-docs`
- Operation-level documentation with examples
- Request/Response schemas with validation rules
- API versioning information
- Error response model

### Specification-Driven Development Compliance
✅ **PASSED**: OpenAPI specification as single source of truth
- All code generation, implementation, and testing follow approved specs
- Changes in business logic or data model require prior spec updates
- Spec-It agents verify code aligns with spec

### Test-First Automation Compliance
✅ **PASSED**: Automated test cases mandatory, generated from specs before implementation
- 80% minimum code coverage enforced by JaCoCo
- Tests cover happy path, edge cases, and error handling
- JUnit 5, Mockito, Spring Boot Test for comprehensive testing

### Metadata-Driven Governance Compliance
✅ **PASSED**: All schemas and APIs embed governance metadata
- JSON Schema with governance metadata (x-owner, x-version, x-description)
- Changes require Data Architect and Solution Architect approval

### Observability and Monitoring Compliance
✅ **PASSED**: Structured logging with correlation IDs
- Monitor response times (<200ms target), throughput (≥10K req/min)
- Micrometer, Prometheus, Grafana integration
- SLF4J + Logback with JSON formatting

### Simplicity and Pragmatism Compliance
✅ **PASSED**: Avoid over-engineering, use proven frameworks
- Spring Boot 3.1.3 with standard starters
- Generate clean, maintainable code stubs
- Follow YAGNI—implement only what is specified

## Project Structure

### Documentation (this feature)

```text
specs/001-acms-core-apis/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/
│   └── java/
│       └── com/acms/
│           ├── controller/     # REST endpoints with @RestController
│           ├── dto/           # Request/Response DTOs with validation
│           ├── service/       # Business logic implementation
│           ├── repository/    # Data access (Spring Data JPA)
│           ├── model/         # JPA entities
│           ├── config/        # Configuration classes
│           ├── exception/     # Global exception handling with @ControllerAdvice
│           └── cache/         # Cache configurations
└── test/
    └── java/
        └── com/acms/
            ├── controller/    # API tests
            ├── service/       # Business logic tests
            ├── repository/    # Data access tests
            └── integration/   # Integration tests

resources/
├── application.yml          # Spring Boot configuration
├── application-dev.yml      # Development-specific config
└── application-prod.yml     # Production-specific config
```

**Structure Decision**: Single project structure with standard Spring Boot layout, following ACMS constitution package requirements (com.acms root package with specified subpackages).

## 1. Technology Stack

### Framework & Core Dependencies
- **Framework**: Spring Boot 3.1.3
- **Java Version**: 17 (LTS)
- **Build Tool**: Maven 3.8.8
- **Database**: H2 (development and production)
- **API Documentation**: SpringDoc OpenAPI 1.6.14 configuration
  - Interactive documentation at `/swagger-ui.html`
  - OpenAPI 3.0 spec at `/v3/api-docs`
  - Operation-level documentation with examples
  - Request/Response schemas with validation rules
  - API versioning information
  - Error response model
- **Monitoring**: Micrometer, Prometheus, Grafana
- **Logging**: SLF4J + Logback with JSON formatting

### Dependencies with Version Numbers
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Mapping & Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Caching -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
    
    <!-- Monitoring -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Java & Maven Configuration
- **Java Version**: 21 (LTS) - upgraded from 17 for better performance and features
- **Maven Version**: 3.9.6
- **Spring Boot Version**: 3.2.0 (latest stable)

### Jakarta API Usage
**CRITICAL**: Use Jakarta EE 10 namespaces only - NO javax.* imports

```xml
<!-- Jakarta Dependencies (automatically included with Spring Boot 3.x) -->
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
</dependency>
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
</dependency>
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>2.1.1</version>
</dependency>
```

**PROHIBITED**: Any javax.* imports will cause compilation errors
**REQUIRED**: Use jakarta.persistence.*, jakarta.validation.*, jakarta.annotation.*

## 2. Database Configuration

### H2 Database Setup
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:acmsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
      database-platform: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console
server:
  port: 8080
```

### Key Configuration Details
- **Database Type**: In-memory H2 database
- **Persistence**: JPA with Hibernate
- **Schema Management**: Auto-update schema on startup
- **Console Access**: Available at `/h2-console`
- **Connection Pool**: HikariCP (default with Spring Boot)
- **DDL Mode**: `update` (auto-creates/updates schema)

## 3. Architecture Overview

### High-Level Design
```
┌─────────────────────────────────────────────────────────┐
│                    API Layer (REST)                     │
├─────────────────────────────────────────────────────────┤
│                Service Layer (Business Logic)           │
├─────────────────────────────────────────────────────────┤
│                Repository Layer (Data Access)           │
├─────────────────────────────────────────────────────────┤
│                        Database                         │
└─────────────────────────────────────────────────────────┘
```

### Package Structure
- `com.acms` (root package)
  - `controller`: REST endpoints with `@RestController`
  - `dto`: Request/Response DTOs with validation
  - `service`: Business logic implementation
  - `repository`: Data access (Spring Data JPA)
  - `model`: JPA entities
  - `config`: Configuration classes
  - `exception`: Global exception handling with `@ControllerAdvice`
  - `cache`: Cache configurations

### Key Components
1. **API Module**: REST controllers, DTOs with validation
2. **Service Layer**: Business logic with transaction management
3. **Repository Layer**: Spring Data JPA repositories
4. **Cache Layer**: Caffeine cache for read-heavy operations
5. **Exception Handling**: Global exception handler with consistent error responses
6. **Monitoring**: Actuator endpoints for health and metrics

## 4. Data Mapping & Conversion

### DTO → Entity Mapping Strategy

#### MapStruct Configuration
- Centralized mapping configuration in `com.acms.config.MappingConfig`
- Mapper interfaces in `com.acms.mapper` package
- Bi-directional mapping where applicable
- Custom type converters for complex mappings

Example mapper interface:
```java
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgentMapper {
    AgentDTO toDto(Agent agent);
    Agent toEntity(AgentDTO dto);
    
    @Mapping(target = "id", ignore = true)
    void updateFromDto(AgentDTO dto, @MappingTarget Agent entity);
}
```

#### Mapping Rules
- Never expose entities directly in API responses
- Use separate DTOs for request/response
- Immutable DTOs for responses
- Document mapping decisions in `MAPPING_DECISIONS.md`

## 5. Business Rules

### Commission Calculation
- **Calculation Method**: Tiered percentage based on policy type and agent tier
- **Rules Source**: Database lookup table (`COMMISSION_RULES`)
- **Calculation Flow**:
  1. Look up commission rate based on policy type and agent tier
  2. Apply any applicable adjustments (seasonal, special promotions)
  3. Calculate final commission amount
- **Audit Trail**: All calculations are logged with input parameters and result

### Data Retention & Archival
- **Active Data**: Last 3 years of data in primary tables
- **Archival Policy**:
  - Policies: 10 years (archived after 3 years)
  - Commissions: 7 years (archived after 3 years)
  - Payments: 7 years (archived after 3 years)
- **Backup Strategy**:
  - Daily incremental backups (kept for 30 days)
  - Weekly full backups (kept for 12 months)
  - Encrypted storage with access logging

## 6. Data Model

### Key Entities
1. **Agent**
   - id: UUID
   - name: String
   - email: String
   - commissionTier: BigDecimal
   - status: Enum(ACTIVE, INACTIVE)
   - createdAt: LocalDateTime
   - updatedAt: LocalDateTime

2. **Policy**
   - id: UUID
   - agentId: UUID (FK to Agent)
   - coverageType: String
   - premiumAmount: BigDecimal
   - effectiveDate: LocalDate
   - expirationDate: LocalDate
   - status: Enum(ACTIVE, INACTIVE)
   - createdAt: LocalDateTime
   - updatedAt: LocalDateTime

3. **Commission**
   - id: UUID
   - policyId: UUID (FK to Policy)
   - agentId: UUID (FK to Agent)
   - commissionTier: BigDecimal
   - amount: BigDecimal
   - calculationDate: LocalDateTime
   - status: Enum(PENDING, PAID)

4. **Payment**
   - id: UUID
   - commissionId: UUID (FK to Commission)
   - amount: BigDecimal
   - paymentDate: LocalDate
   - status: Enum(PENDING, COMPLETED, FAILED)
   - version: Long (for optimistic locking)

## 7. API Design

### Base URL
`/api/v1`

### Endpoints
1. **Agents**
   - `GET /agents` - List agents (paginated)
   - `POST /agents` - Create agent
   - `GET /agents/{id}` - Get agent by ID
   - `PUT /agents/{id}` - Update agent
   - `PATCH /agents/{id}/status` - Update agent status

2. **Policies**
   - `GET /policies` - List policies (with filtering)
   - `POST /policies` - Create policy
   - `GET /policies/{id}` - Get policy by ID
   - `PUT /policies/{id}` - Update policy
   - `PATCH /policies/{id}/status` - Update policy status

3. **Commissions**
   - `GET /commissions` - List commissions (with filtering)
   - `GET /commissions/{id}` - Get commission by ID
   - `POST /commissions/calculate` - Calculate commission for a policy
   - `PATCH /commissions/{id}/amount` - Update commission amount (admin only)

4. **Payments**
   - `GET /payments` - List payments (with filtering)
   - `POST /payments` - Record payment
   - `GET /payments/{id}` - Get payment by ID
   - `PATCH /payments/{id}/status` - Update payment status

5. **Reports**
   - `GET /reports/agent-performance` - Agent performance summary
   - `GET /reports/policy-commissions` - Policy commission summary

### API Standards

#### Response Wrapper
All API responses follow a standard envelope format:
```json
{
  "status": "success|error",
  "data": {},
  "timestamp": "2025-12-01T17:00:00Z",
  "requestId": "abc123-xyz-456",
  "path": "/api/v1/agents"
}
```

#### Pagination
All list endpoints support pagination with these standard parameters:
- `page`: Page number (0-based, default: 0)
- `size`: Items per page (default: 20, max: 100)
- `sort`: Sort criteria (e.g., `name,asc`, `createdAt,desc`)

Response includes pagination metadata:
```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 5,
    "totalElements": 100
  }
}
```

#### Error Responses
Follows RFC 7807 (Problem Details for HTTP APIs) with standard error codes:

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| AGENT_NOT_FOUND | 404 | Requested agent does not exist |
| POLICY_VALIDATION_FAILED | 400 | Invalid policy data provided |
| COMMISSION_CALCULATION_ERROR | 500 | Error calculating commission |
| DUPLICATE_AGENT | 409 | Agent with same email already exists |
| INVALID_INPUT | 400 | Request validation failed |

Example error response:
```json
{
  "type": "/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Agent with ID 123 not found",
  "instance": "/api/v1/agents/123",
  "code": "AGENT_NOT_FOUND",
  "timestamp": "2025-12-01T17:00:00Z"
}
```

## 8. Security

### Data Protection & Validation
- Input validation using Bean Validation (JSR-380)
- Global exception handler with consistent error responses
- Sensitive data encryption at rest
- TLS 1.3 for all communications
- Request/Response logging with sensitive data redaction

### Caching Strategy
- Use Spring Cache abstraction with Caffeine provider
- Cache configuration:
  - Maximum size: 10,000 entries
  - Default TTL: 1 hour
  - Cache invalidation on write operations
- Cached entities:
  - Agent details
  - Policy details
  - Commission calculations

## 9. Monitoring & Observability

### Logging
- Structured JSON logging
- Correlation IDs for request tracing
- Log levels: DEBUG, INFO, WARN, ERROR

### Metrics
- Prometheus metrics endpoint
- Custom business metrics
- Standard JVM and application metrics

### Health Checks
- Database connectivity
- External service status
- Custom health indicators

## 10. Testing Strategy

### Unit Testing (80% Coverage Minimum)
- Test all business logic with JUnit 5
- Mock external dependencies using Mockito
- Test validation rules and edge cases
- Test cache behavior
- Use AssertJ for fluent assertions
- Test coverage enforced by JaCoCo

### Integration Testing
- TestContainers for database tests
- Spring Boot Test for API tests
- Test security configurations

### Performance Testing
- JMeter for load testing
- Baseline performance metrics
- Performance regression testing

## 11. Operations & Maintenance

### Logging & Monitoring
- **Retention Policy**:
  - Application Logs: 30 days (ELK Stack)
  - Access Logs: 90 days (compressed)
  - Audit Logs: 7 years (immutable storage)
- **Metrics**:
  - 15 months retention in Prometheus
  - Long-term storage in Thanos

### Backup & Recovery
- **Database Backups**:
  - Development: Daily snapshots
  - Production: Hourly incremental + daily full backups
  - Test restores performed monthly
- **Disaster Recovery**:
  - RPO: 5 minutes
  - RTO: 1 hour for critical systems
  - Documented runbooks for common failure scenarios

### Data Archival
- **Archival Process**:
  - Monthly job to move old records to archive tables
  - Data remains queryable through dedicated archive endpoints
  - Compressed storage for archived data
- **Retrieval**:
  - Self-service archive access for recent data
  - Admin approval required for older archives
  - Audit trail of all archive access

## 12. Deployment

### Development
- Local H2 database
- Embedded Tomcat
- Auto-reload for development

### Staging/Production
- Docker containers
- Kubernetes orchestration
- Database replication
- Horizontal scaling

## 13. Implementation Phases

### Phase 1: Core Infrastructure (Weeks 1-2)
- Project setup with Maven 3.8.8 and Java 17
- Base package structure and configurations
- H2 database setup with JPA entities
- Global exception handling with `@ControllerAdvice`
- Basic health check and metrics endpoints
- CI/CD pipeline setup

### Phase 2: Core Domain Implementation (Weeks 3-5)
- Agent management implementation
- Policy management implementation
- Commission calculation engine
- Basic API endpoints

### Phase 3: Business Features (Weeks 6-8)
- Payment tracking system
- Reporting and analytics
- Performance optimization
- Comprehensive API documentation

## 14. Quality Gates

### Code Quality
- Static code analysis with SonarQube
- Enforce code style with Checkstyle
- Zero SonarQube blocker/critical issues
- Minimum 80% code coverage
- All tests must pass

### API Contract Testing
- Verify against OpenAPI spec
- Test request/response schemas
- Validate error responses
- Test edge cases and error conditions

## 15. Risks & Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Performance issues with commission calculations | High | Medium | Implement batch processing, optimize queries |
| Data consistency across related entities | High | Medium | Use transactions, implement proper locking |
| Complex reporting requirements | Medium | High | Start with basic reports, iterate based on feedback |
| Data migration challenges | High | Low | Develop comprehensive migration scripts, test thoroughly |
| Scope creep | Medium | High | Strict change management, regular stakeholder reviews |

## 16. Open Questions

1. Specific authentication provider requirements?
2. Disaster recovery requirements?
3. Compliance certifications needed (SOC2, ISO 27001, etc.)?

## 17. Success Metrics

- API response time < 200ms (p95)
- 99.9% uptime
- < 0.1% error rate
- 80%+ test coverage
- All security findings addressed before production
