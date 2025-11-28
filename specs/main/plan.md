# Implementation Plan: ACMS Core APIs

**Branch**: `1-acms-core-apis` | **Date**: 2025-11-24 | **Spec**: [specs/main/spec.md](spec.md)
**Input**: Feature specification from `/specs/main/spec.md`

**Note**: This plan is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build comprehensive REST APIs for Agent Commission Management System (ACMS) with CRUD operations for agents, policies, commissions, and payments. The system will provide core transaction processing APIs and business analytics endpoints, all compliant with OpenAPI 3.0 and secured with OAuth2/JWT authentication. Implementation uses Java Spring Boot with SQLite3 database, Python Pytest for testing, and Spec-It agents for automated code generation and test creation.

## Technical Context

**Language/Version**: Java 17+ (Spring Boot 3.x)  
**Primary Dependencies**: Spring Boot 3.x, Spring Data JPA, Springdoc (OpenAPI), SLF4J  
**Storage**: SQLite3 with Spring Data JPA ORM  
**Testing**: Python 3.11+ with Pytest, future Postman/Newman  
**Target Platform**: Linux/Windows server (REST API)  
**Project Type**: Single backend API service  
**Performance Goals**: <200ms average response time, 10,000 requests/minute throughput  
**Constraints**: <200ms p95 latency, 100% commission calculation accuracy, OWASP API Top 10 compliance  
**Scale/Scope**: Support 10K+ agents, 100K+ policies, 1M+ commission records

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

✅ **API-First Design**: All capabilities exposed via REST APIs compliant with OpenAPI 3.0  
✅ **Specification-Driven Development**: All code generation derives from OpenAPI specs  
✅ **Test-First Automation**: Test Agent generates test cases from specs before implementation  
✅ **Metadata-Driven Governance**: All schemas include x-owner, x-version, x-description  
✅ **Secure by Default**: OAuth2/JWT authentication mandatory, RBAC authorization, input validation via JSON Schema  
✅ **Observability and Monitoring**: Structured logging with correlation IDs, latency tracking per endpoint  
✅ **Simplicity and Pragmatism**: Uses Spring Boot and SQLite, no over-engineering, YAGNI principles  

**GATE STATUS**: ✅ PASSED - All principles satisfied by specification

## Project Structure

### Documentation (this feature)

```text
specs/main/
├── spec.md              # Feature specification (user stories, requirements)
├── plan.md              # This file (implementation plan)
├── research.md          # Phase 0 output (design decisions, alternatives)
├── data-model.md        # Phase 1 output (entities, relationships, validation)
├── quickstart.md        # Phase 1 output (getting started guide)
├── contracts/           # Phase 1 output (OpenAPI specifications)
│   ├── agents.yaml
│   ├── policies.yaml
│   ├── commissions.yaml
│   └── payments.yaml
└── tasks.md             # Phase 2 output (task breakdown by user story)
```

### Source Code (repository root)

```text
acms-api/
├── src/main/java/com/acms/
│   ├── controller/          # REST endpoints
│   │   ├── AgentController.java
│   │   ├── PolicyController.java
│   │   ├── CommissionController.java
│   │   └── PaymentController.java
│   ├── service/             # Business logic
│   │   ├── AgentService.java
│   │   ├── PolicyService.java
│   │   ├── CommissionService.java
│   │   └── PaymentService.java
│   ├── repository/          # Data access (Spring Data JPA)
│   │   ├── AgentRepository.java
│   │   ├── PolicyRepository.java
│   │   ├── CommissionRepository.java
│   │   └── PaymentRepository.java
│   ├── model/               # JPA entities
│   │   ├── Agent.java
│   │   ├── Policy.java
│   │   ├── Commission.java
│   │   └── Payment.java
│   ├── dto/                 # Request/Response DTOs
│   │   ├── AgentDTO.java
│   │   ├── PolicyDTO.java
│   │   ├── CommissionDTO.java
│   │   └── PaymentDTO.java
│   ├── config/              # Spring configuration
│   │   ├── SecurityConfig.java
│   │   └── OpenApiConfig.java
│   ├── exception/           # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── ApiException.java
│   └── AcmsApplication.java # Main application class
├── src/main/resources/
│   ├── application.yml      # Spring Boot configuration
│   └── schema.sql           # Database schema
├── src/test/java/com/acms/
│   ├── controller/          # Controller tests
│   ├── service/             # Service tests
│   └── integration/         # Integration tests
├── pom.xml                  # Maven configuration
└── README.md                # Project documentation

tests/python/
├── conftest.py              # Pytest configuration
├── test_agents.py           # Agent API tests
├── test_policies.py         # Policy API tests
├── test_commissions.py      # Commission API tests
├── test_payments.py         # Payment API tests
└── test_integration.py      # End-to-end tests
```

**Structure Decision**: Single backend API service (Option 1) - Spring Boot REST API with SQLite3 database. Python tests are separate for independent test execution. All code follows Spring Boot conventions with clear separation of concerns (controller → service → repository).

## Security Guidelines

### Authentication & Authorization
- OAuth2/JWT bearer token required for all APIs
- Role-Based Access Control (RBAC) for different user roles (Agent, Admin, Analytics)
- JWT token validation on every request; reject expired or invalid tokens

### Data Protection
- Encrypt sensitive fields (agent contact info, payment details) at rest using AES-256
- Enforce HTTPS/TLS for all API communications (minimum TLS 1.2)
- Store API keys and JWT secrets in secure vault (environment variables, never hardcoded)
- Mask or redact PII (Personally Identifiable Information) in logs and error messages

### API Security
- Implement rate limiting: 100 requests per minute per client
- Implement CORS policy allowing only whitelisted origins
- Use parameterized queries to prevent SQL injection attacks
- Implement CSRF protection for state-changing operations (POST, PUT, DELETE)

### Compliance & Governance
- Maintain audit logs for all API calls with correlation IDs
- Ensure GDPR compliance with data retention policies (right to be forgotten)
- Compliance with OWASP API Security Top 10

## Coding Standards

### Naming Conventions
- **Classes**: PascalCase (e.g., `AgentController`, `CommissionService`, `AgentDTO`)
- **Methods & Variables**: camelCase (e.g., `calculateCommission`, `agentId`, `paymentAmount`)
- **Constants**: UPPER_CASE (e.g., `DEFAULT_COMMISSION_RATE`, `MAX_RETRY_ATTEMPTS`)
- **REST Endpoints**: Use nouns and plural forms (e.g., `/agents`, `/policies`, `/commissions`)

### Code Structure & Patterns
- Use DTOs for request/response instead of exposing JPA entities directly
- Implement validation annotations: `@Valid`, `@NotNull`, `@Email`, `@Min`, `@Max`, `@Pattern`
- Use Lombok annotations to reduce boilerplate: `@Getter`, `@Setter`, `@Builder`, `@Data`
- Implement transaction management with `@Transactional` for service methods
- Use Spring Cache annotations (`@Cacheable`, `@CachePut`) for performance optimization
- Implement custom queries with `@Query` for complex database operations

### Exception Handling
- Implement Global Exception Handler using `@ControllerAdvice`
- Return structured error responses with: timestamp, status, error code, message, field-level details
- Never expose stack traces or sensitive information in error messages

### Logging Best Practices
- Use SLF4J with Logback for all logging
- Log at appropriate levels: INFO (normal operations), DEBUG (troubleshooting), ERROR (failures)
- Include correlation IDs in all logs for request tracing
- Never log sensitive data (PII, tokens, passwords)

### Testing Standards
- Use JUnit 5 for unit tests
- Use Mockito for mocking dependencies
- Use Spring Boot Test for integration tests
- Maintain 80%+ code coverage
- Test all error scenarios and edge cases

### Code Quality
- Follow SOLID principles (Single Responsibility, Open/Closed, Liskov, Interface Segregation, Dependency Inversion)
- Run SonarQube for static code analysis
- Use Checkstyle for code formatting enforcement
- Perform code reviews before merging to main branch

## Complexity Tracking

> **No Constitution Check violations - all principles satisfied**

| Principle | Status | Justification |
|-----------|--------|---------------|
| API-First Design | ✅ Satisfied | All functionality exposed via REST APIs with OpenAPI 3.0 specs |
| Specification-Driven | ✅ Satisfied | Code generation from approved OpenAPI specs |
| Test-First Automation | ✅ Satisfied | Test Agent generates tests from specs before implementation |
| Metadata Governance | ✅ Satisfied | All schemas include x-owner, x-version, x-description |
| Secure by Default | ✅ Satisfied | OAuth2/JWT auth, RBAC, JSON Schema validation |
| Observability | ✅ Satisfied | SLF4J logging, correlation IDs, latency tracking |
| Simplicity | ✅ Satisfied | Spring Boot + SQLite, no over-engineering |
