# ACMS Constitution

<!-- Agent Commission Management System - Governance & Development Standards -->
<!-- Version: 1.0.0 | Ratified: 2025-11-24 | Last Amended: 2025-11-24 -->

## Core Principles

### I. API-First Design
Every feature and capability must be exposed through well-defined REST APIs compliant with OpenAPI 3.0. APIs are the primary contract; implementation details follow from the specification. All data models must be defined using JSON Schema with governance metadata (x-owner, x-version, x-description). APIs must be versioned semantically and maintain backward compatibility unless a MAJOR version bump is justified and documented.

### II. Specification-Driven Development
Specifications are the source of truth. All code generation, testing, and implementation must derive from approved OpenAPI specifications. Changes to business logic or data models must first be reflected in the specification before implementation. Spec-It agents validate that generated code matches the specification contract.

### III. Test-First Automation (NON-NEGOTIABLE)
Automated testing is mandatory and must be driven by specifications. Test Agent generates test cases from OpenAPI specs before implementation. All APIs must have comprehensive test coverage including happy-path, edge cases, and error scenarios. Tests validate against the specification as the single source of truth. Manual testing supplements but does not replace automated tests.

### IV. Metadata-Driven Governance
All schemas and APIs must include governance metadata: x-owner (responsible team), x-version (schema version), x-description (business purpose). Metadata enables traceability from BRD through implementation to production. Changes to governance metadata require approval from Data Architect and Solution Architect.

### V. Secure by Default
Authentication (OAuth2/JWT) is mandatory for all APIs. Authorization uses role-based access control (RBAC). Input validation via JSON Schema is enforced. Sensitive data is encrypted at rest and in transit. Security review is required before any API reaches production. Compliance with OWASP API Top 10 is non-negotiable.

### VI. Observability and Monitoring
All APIs must include structured logging with correlation IDs. Response times and throughput must be monitored (target: <200ms response time, 10K req/min capacity). Failed authentication attempts and traffic anomalies must be logged and monitored. Latency metrics must be tracked per endpoint.

### VII. Simplicity and Pragmatism
Avoid over-engineering. Use existing frameworks (Spring Boot, SQLite) rather than building custom solutions. Follow YAGNI principles—implement only what is specified. Code generation should produce clean, maintainable stubs that developers can extend without fighting the generator.

## Technology Stack & Standards

### Backend Implementation
- **Framework**: Java Spring Boot (REST API)
- **Database**: SQLite3
- **ORM**: Spring Data JPA
- **Logging**: SLF4J
- **Exception Handling**: Global @ControllerAdvice with consistent error responses
- **API Documentation**: Springdoc (OpenAPI integration)

### API Specification & Governance
- **Specification Format**: OpenAPI 3.0
- **Schema Format**: JSON Schema
- **Versioning**: Semantic versioning (MAJOR.MINOR.PATCH)
- **Metadata Standards**: x-owner, x-version, x-description on all schemas
- **Security Schemes**: OAuth2/JWT bearer token authentication

### Testing & Quality Assurance
- **Test Framework**: Python Pytest
- **API Testing**: Automated test generation from OpenAPI specs
- **Collections**: Postman/Newman (future phase)
- **Coverage Target**: Minimum 80% endpoint coverage
- **Performance Testing**: Response time <200ms, throughput 10K req/min

### Code Generation & Automation
- **Spec Agent**: Generates OpenAPI specs from BRD and user stories
- **CodeGen Agent**: Generates Spring Boot stubs from approved specs
- **Test Agent**: Generates Python test suites from OpenAPI specs
- **Framework**: Spec-It orchestration platform

### Security Standards
- **Authentication**: OAuth2/JWT bearer tokens mandatory for all APIs
- **Authorization**: Role-Based Access Control (RBAC) for different user roles
- **Data Protection**: AES-256 encryption for sensitive data at rest; HTTPS/TLS for data in transit
- **API Security**: Rate limiting (100 req/min per client), CORS policy, CSRF protection, SQL injection prevention
- **Secrets Management**: API keys and JWT secrets stored in secure vault (environment variables)
- **Compliance**: OWASP API Top 10, GDPR data retention policies, audit logging with correlation IDs
- **PII Protection**: Mask or redact Personally Identifiable Information in logs and error messages

### Java Spring Boot Coding Standards
- **Naming**: Classes (PascalCase), Methods/Variables (camelCase), Constants (UPPER_CASE), Endpoints (plural nouns)
- **Patterns**: DTOs for API contracts, Validation annotations (@Valid, @NotNull, @Email), Lombok for boilerplate reduction
- **Transactions**: @Transactional for service methods, Optimistic locking for concurrent updates
- **Caching**: @Cacheable and @CachePut for performance optimization
- **Logging**: SLF4J with Logback, correlation IDs for request tracing, no sensitive data in logs
- **Testing**: JUnit 5, Mockito, Spring Boot Test, 80%+ code coverage, test all error scenarios
- **Quality**: SOLID principles, SonarQube static analysis, Checkstyle enforcement, code reviews mandatory

## Development Workflow & Quality Gates

### Specification Phase
1. Business Analyst updates BRD with new requirements
2. Spec Agent generates OpenAPI 3.0 specification
3. API Designer reviews and refines specification
4. Solution Architect approves specification
5. Specification is locked and versioned

### Code Generation Phase
1. CodeGen Agent generates Spring Boot stubs from approved spec
2. Backend Engineer implements business logic
3. Code follows Spring Boot conventions (PascalCase classes, camelCase methods)
4. All DTOs and models include proper annotations
5. Code review verifies alignment with specification

### Testing Phase
1. Test Agent generates Python test suite from specification
2. QA Engineer validates test coverage
3. All tests must pass before merge
4. Security Officer reviews authentication/authorization implementation
5. Performance tests verify <200ms response time

### Deployment Phase
1. DevOps Engineer manages CI/CD pipeline
2. Automated tests run in deployment pipeline
3. Smoke tests verify deployment success
4. Monitoring and alerting are configured
5. Release notes document API changes

### Change Management
- **BRD Changes**: Proposed by Product Owner, approved by Business Sponsor
- **API Contract Changes**: Proposed by API Designer, approved by Solution Architect
- **Governance Metadata Changes**: Proposed by Data Architect, approved by Solution Architect
- **Implementation Changes**: Reviewed by team lead, must maintain backward compatibility

## Governance

### Amendment Procedure
1. Proposed amendment must be documented with rationale
2. Amendment is reviewed by Solution Architect and Spec-It Champion
3. Impact on dependent artifacts (specs, code, tests) is assessed
4. Amendment is approved by Technical Steering Committee
5. Version is bumped according to semantic versioning rules
6. All dependent templates and configurations are updated

### Versioning Policy
- **MAJOR**: Backward incompatible changes (principle removals, redefinitions)
- **MINOR**: New principles or sections added, material guidance expansion
- **PATCH**: Clarifications, wording improvements, non-semantic refinements

### Compliance Review
- Constitution compliance is reviewed quarterly
- All PRs must verify alignment with core principles
- Spec-It agent configurations must be validated against constitution
- Non-compliance issues are escalated to Solution Architect

### Traceability & Audit
- All requirements must be traceable from BRD → Specification → Implementation → Tests
- Changes must be linked to JIRA tickets or equivalent tracking
- Decision records must be maintained for architectural decisions
- Governance metadata enables audit trails for commission calculations and payments

### Documentation Standards
- All generated files must follow this constitution
- Use clear, concise English without buzzwords
- Include realistic, usable examples aligned with OpenAPI 3.0
- Maintain consistency across Specify.md, Plan.md, Tasks.md, and Constituents.md

**Version**: 1.0.0 | **Ratified**: 2025-11-24 | **Last Amended**: 2025-11-24

