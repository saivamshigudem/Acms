# Tasks: ACMS Core APIs

**Input**: Design documents from `/specs/main/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Maven project structure per implementation plan in `acms-api/`
- [x] T002 Initialize Spring Boot 3.x project with dependencies in `acms-api/pom.xml`
- [x] T003 [P] Configure code formatting (Google Java Format) and linting (Checkstyle)
- [x] T004 [P] Setup Git repository and .gitignore for Java/Maven projects

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Setup SQLite3 database schema in `acms-api/src/main/resources/schema.sql`
- [x] T006 [P] Configure Spring Security with OAuth2/JWT in `acms-api/src/main/java/com/acms/config/SecurityConfig.java`
- [x] T007 [P] Setup Spring Data JPA repositories base configuration in `acms-api/src/main/java/com/acms/repository/`
- [x] T008 [P] Create GlobalExceptionHandler in `acms-api/src/main/java/com/acms/exception/GlobalExceptionHandler.java`
- [x] T009 Configure SLF4J logging with correlation IDs in `acms-api/src/main/resources/application.yml`
- [x] T010 [P] Setup OpenAPI/Springdoc configuration in `acms-api/src/main/java/com/acms/config/OpenApiConfig.java`
- [x] T011 Create base ApiException class in `acms-api/src/main/java/com/acms/exception/ApiException.java`
- [x] T012 Setup application.yml with database, security, and logging configuration in `acms-api/src/main/resources/application.yml`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Agent Management (Priority: P1) 🎯 MVP

**Goal**: Enable administrators to manage agent records with full CRUD operations and lifecycle tracking

**Independent Test**: Create an agent, retrieve it, update details, and deactivate it. Verify all operations work independently.

### Implementation for User Story 1

- [x] T013 [P] [US1] Create Agent JPA entity in `acms-api/src/main/java/com/acms/model/Agent.java` with fields: id, name, email, commissionTier, status, createdDate, lastModifiedDate
- [x] T014 [P] [US1] Create AgentDTO in `acms-api/src/main/java/com/acms/dto/AgentDTO.java` for API requests/responses
- [x] T015 [P] [US1] Create AgentRepository extending JpaRepository in `acms-api/src/main/java/com/acms/repository/AgentRepository.java`
- [x] T016 [US1] Implement AgentService in `acms-api/src/main/java/com/acms/service/AgentService.java` with methods: createAgent, getAgent, updateAgent, listAgents, deactivateAgent (depends on T013, T014, T015)
- [x] T017 [US1] Implement AgentController in `acms-api/src/main/java/com/acms/controller/AgentController.java` with endpoints: POST /agents, GET /agents/{id}, PUT /agents/{id}, GET /agents, DELETE /agents/{id}
- [x] T018 [US1] Add input validation for Agent fields in AgentService (name not empty, valid email format, commission tier 0-100)
- [x] T019 [US1] Add logging for all agent operations in AgentService and AgentController
- [x] T020 [P] [US1] Create Python test file `tests/python/test_agents.py` with test cases for all agent endpoints

**Checkpoint**: User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Policy Management (Priority: P1)

**Goal**: Enable administrators to create and manage group insurance policies linked to agents

**Independent Test**: Create a policy linked to an agent, retrieve it, update details, and mark as inactive. Verify all operations work independently.

### Implementation for User Story 2

- [x] T021 [P] [US2] Create Policy JPA entity in `acms-api/src/main/java/com/acms/model/Policy.java` with fields: id, agentId, coverageType, premium, effectiveDate, expirationDate, status, createdDate
- [x] T022 [P] [US2] Create PolicyDTO in `acms-api/src/main/java/com/acms/dto/PolicyDTO.java` for API requests/responses
- [x] T023 [P] [US2] Create PolicyRepository extending JpaRepository in `acms-api/src/main/java/com/acms/repository/PolicyRepository.java` with custom query methods
- [x] T024 [US2] Implement PolicyService in `acms-api/src/main/java/com/acms/service/PolicyService.java` with methods: createPolicy, getPolicy, updatePolicy, listPolicies, deactivatePolicy (depends on T021, T022, T023, T016 for agent validation)
- [x] T025 [US2] Implement PolicyController in `acms-api/src/main/java/com/acms/controller/PolicyController.java` with endpoints: POST /policies, GET /policies/{id}, PUT /policies/{id}, GET /policies, DELETE /policies/{id}
- [x] T026 [US2] Add input validation for Policy fields (agent exists, premium > 0, valid coverage type, valid dates)
- [x] T027 [US2] Add logging for all policy operations in PolicyService and PolicyController
- [x] T028 [P] [US2] Create Python test file `tests/python/test_policies.py` with test cases for all policy endpoints

**Checkpoint**: User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Commission Calculation (Priority: P1)

**Goal**: Automatically calculate commissions based on policy premiums and agent tiers, creating auditable records

**Independent Test**: Create a policy with known premium, verify commission is calculated correctly, and retrieve the commission record.

### Implementation for User Story 3

- [x] T029 [P] [US3] Create Commission JPA entity in `acms-api/src/main/java/com/acms/model/Commission.java` with fields: id, policyId, agentId, commissionTier, calculatedAmount, calculationDate, status, createdDate
- [x] T030 [P] [US3] Create CommissionDTO in `acms-api/src/main/java/com/acms/dto/CommissionDTO.java` for API requests/responses
- [x] T031 [P] [US3] Create CommissionRepository extending JpaRepository in `acms-api/src/main/java/com/acms/repository/CommissionRepository.java` with custom query methods
- [x] T032 [US3] Implement CommissionService in `acms-api/src/main/java/com/acms/service/CommissionService.java` with methods: calculateCommission, getCommission, listCommissions, updateCommissionAmount (depends on T029, T030, T031, T016, T024)
- [x] T033 [US3] Implement commission calculation logic: amount = policy.premium * (agent.commissionTier / 100) in CommissionService
- [x] T034 [US3] Implement CommissionController in `acms-api/src/main/java/com/acms/controller/CommissionController.java` with endpoints: POST /commissions, GET /commissions/{id}, PUT /commissions/{id}, GET /commissions
- [x] T035 [US3] Add audit trail for commission updates (store original amount, update reason, timestamp) in CommissionService
- [x] T036 [US3] Add logging for all commission operations with calculation details
- [x] T037 [P] [US3] Create Python test file `tests/python/test_commissions.py` with test cases including calculation accuracy verification

**Checkpoint**: User Stories 1, 2, AND 3 should all work independently

---

## Phase 6: User Story 4 - Payment Tracking (Priority: P2)

**Goal**: Record commission payments to agents and maintain payment history for reconciliation

**Independent Test**: Create a payment record for a commission, retrieve it, update status, and verify payment history.

### Implementation for User Story 4

- [x] T038 [P] [US4] Create Payment JPA entity in `acms-api/src/main/java/com/acms/model/Payment.java` with fields: id, commissionId, paymentAmount, paymentDate, status, createdDate, lastModifiedDate
- [x] T039 [P] [US4] Create PaymentDTO in `acms-api/src/main/java/com/acms/dto/PaymentDTO.java` for API requests/responses
- [x] T040 [P] [US4] Create PaymentRepository extending JpaRepository in `acms-api/src/main/java/com/acms/repository/PaymentRepository.java` with custom query methods
- [x] T041 [US4] Implement PaymentService in `acms-api/src/main/java/com/acms/service/PaymentService.java` with methods: createPayment, getPayment, updatePaymentStatus, listPayments (depends on T038, T039, T040, T032)
- [x] T042 [US4] Implement PaymentController in `acms-api/src/main/java/com/acms/controller/PaymentController.java` with endpoints: POST /payments, GET /payments/{id}, PUT /payments/{id}, GET /payments
- [x] T043 [US4] Add validation for payment amounts and status transitions in PaymentService
- [x] T044 [US4] Add logging for all payment operations with reconciliation details
- [x] T045 [P] [US4] Create Python test file `tests/python/test_payments.py` with test cases for all payment endpoints

**Checkpoint**: User Stories 1-4 should all work independently

---

## Phase 7: User Story 5 - Agent Performance Summary (Priority: P2)

**Goal**: Provide managers with aggregated commission and payment metrics for business intelligence

**Independent Test**: Create multiple commissions for an agent, query the performance summary, and verify aggregations are correct.

### Implementation for User Story 5

- [x] T046 [US5] Create PerformanceSummaryDTO in `acms-api/src/main/java/com/acms/dto/PerformanceSummaryDTO.java` with fields: agentId, totalEarned, totalPaid, totalPending, commissionCount
- [x] T047 [US5] Implement performance summary queries in CommissionRepository and PaymentRepository with custom @Query methods
- [x] T048 [US5] Implement getAgentPerformanceSummary method in CommissionService (depends on T046, T047, T032)
- [x] T049 [US5] Implement getSummaryReport method for all agents in CommissionService with sorting by total earned
- [x] T050 [US5] Implement PerformanceSummaryController in `acms-api/src/main/java/com/acms/controller/PerformanceSummaryController.java` with endpoints: GET /performance/agents/{id}, GET /performance/agents
- [x] T051 [US5] Add caching for performance summary queries to meet <500ms performance target
- [x] T052 [US5] Add logging for performance summary operations
- [x] T053 [P] [US5] Create Python test file `tests/python/test_performance.py` with test cases for summary aggregations

**Checkpoint**: All user stories should now be independently functional

---

## Phase 8: Security Hardening & Code Quality

**Purpose**: Implement security controls and code quality standards

### Security Implementation

- [ ] T054 [P] Implement rate limiting in `acms-api/src/main/java/com/acms/config/RateLimitingConfig.java` (100 req/min per client)
- [ ] T055 [P] Implement data encryption for sensitive fields in `acms-api/src/main/java/com/acms/util/EncryptionUtil.java` using AES-256
- [ ] T056 [P] Configure CORS policy in `acms-api/src/main/java/com/acms/config/CorsConfig.java` with whitelisted origins
- [ ] T057 [P] Implement CSRF protection in `acms-api/src/main/java/com/acms/config/SecurityConfig.java` for state-changing operations
- [ ] T058 Implement JWT token validation interceptor in `acms-api/src/main/java/com/acms/security/JwtTokenValidator.java`
- [ ] T059 Add PII masking utility in `acms-api/src/main/java/com/acms/util/PiiMaskingUtil.java` for logs and error messages
- [ ] T060 Configure secrets management in `acms-api/src/main/resources/application.yml` (environment variables for API keys)
- [ ] T061 Create security test file `tests/python/test_security.py` with JWT validation, rate limiting, and CORS tests

### Code Quality & Standards

- [ ] T062 [P] Setup SonarQube configuration in `acms-api/pom.xml` for static code analysis
- [ ] T063 [P] Configure Checkstyle in `acms-api/checkstyle.xml` for code formatting enforcement
- [ ] T064 Add Lombok dependency and annotations to all DTOs and entities in `acms-api/pom.xml`
- [ ] T065 Implement validation annotations (@Valid, @NotNull, @Email, @Min, @Max) in all DTOs in `acms-api/src/main/java/com/acms/dto/`
- [ ] T066 Add @Transactional annotations to all service methods in `acms-api/src/main/java/com/acms/service/`
- [ ] T067 Implement caching with @Cacheable and @CachePut for performance summary queries in CommissionService
- [ ] T068 Create code review checklist in `ACMS/CODE_REVIEW_CHECKLIST.md` for SOLID principles and standards compliance

---

## Phase 9: Integration & Testing

**Purpose**: End-to-end testing and cross-story validation

- [ ] T069 [P] Create integration test file `tests/python/test_integration.py` with end-to-end workflows
- [ ] T070 Create test scenario: Agent creation → Policy creation → Commission calculation → Payment recording
- [ ] T071 [P] Create performance tests in `tests/python/test_performance.py` verifying <200ms response times
- [ ] T072 [P] Create security tests verifying OAuth2/JWT authentication on all endpoints
- [ ] T073 Create test for concurrent payment updates to same commission
- [ ] T074 Create test for policy premium updates after commission calculation

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T075 [P] Generate OpenAPI specification from Springdoc in `specs/main/contracts/openapi.yaml`
- [ ] T076 Update README.md in `acms-api/` with setup instructions, API documentation, and examples
- [ ] T077 [P] Code cleanup and refactoring across all services
- [ ] T078 [P] Add comprehensive unit tests in `acms-api/src/test/java/com/acms/` for services
- [ ] T079 Final security hardening: review all endpoints for OWASP API Top 10 compliance
- [ ] T080 Performance optimization: add database indexes for frequently queried fields
- [ ] T081 Add correlation ID tracking to all API requests in GlobalExceptionHandler
- [ ] T082 Create quickstart guide in `specs/main/quickstart.md` with curl examples for all endpoints
- [ ] T083 [P] Add Postman collection export capability for future Newman integration

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 (Agent Management): Can start after Foundational
  - User Story 2 (Policy Management): Can start after Foundational (may integrate with US1)
  - User Story 3 (Commission Calculation): Can start after Foundational (depends on US1 and US2)
  - User Story 4 (Payment Tracking): Can start after Foundational (depends on US3)
  - User Story 5 (Performance Summary): Can start after Foundational (depends on US3 and US4)
- **Security Hardening & Code Quality (Phase 8)**: Depends on all user stories completion
- **Integration & Testing (Phase 9)**: Depends on Security Hardening phase
- **Polish (Phase 10)**: Depends on Integration phase

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational - Validates agent exists but independently testable
- **User Story 3 (P1)**: Depends on US1 and US2 (needs agents and policies to calculate commissions)
- **User Story 4 (P2)**: Depends on US3 (needs commissions to record payments)
- **User Story 5 (P2)**: Depends on US3 and US4 (aggregates commission and payment data)

### Within Each User Story

- Models before services
- Services before controllers
- Core implementation before integration
- Tests (if included) should be written first and fail before implementation
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1 (Setup)**:
- All [P] tasks can run in parallel

**Phase 2 (Foundational)**:
- T006, T007, T008, T010 marked [P] can run in parallel
- T005, T009, T012 can run sequentially or in parallel (independent concerns)

**Phase 3-5 (User Stories 1-3)**:
- US1 and US2 can run in parallel (independent)
- US3 must wait for US1 and US2
- Within each story, all [P] model/DTO/repository tasks can run in parallel

**Phase 6-7 (User Stories 4-5)**:
- US4 must wait for US3
- US5 must wait for US3 and US4

---

## Parallel Example: User Story 1 (Agent Management)

```bash
# Launch all models and DTOs for User Story 1 together (parallel):
Task T013: Create Agent JPA entity
Task T014: Create AgentDTO
Task T015: Create AgentRepository

# Then launch service and controller (sequential, depends on models):
Task T016: Implement AgentService
Task T017: Implement AgentController

# Then add cross-cutting concerns:
Task T018: Add validation
Task T019: Add logging
Task T020: Create tests
```

---

## Parallel Example: User Stories 1 & 2 (After Foundational)

```bash
# Developer A works on User Story 1:
T013, T014, T015 (parallel)
T016, T017, T018, T019, T020 (sequential)

# Developer B works on User Story 2 (simultaneously):
T021, T022, T023 (parallel)
T024, T025, T026, T027, T028 (sequential)

# Both stories complete independently and can be tested separately
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Agent Management)
4. Complete Phase 4: User Story 2 (Policy Management)
5. Complete Phase 5: User Story 3 (Commission Calculation)
6. **STOP and VALIDATE**: Test all three stories independently and together
7. Deploy/demo if ready

**MVP Scope**: Agents, Policies, and Commission Calculation - the core business value

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo (Core functionality complete)
5. Add User Story 4 → Test independently → Deploy/Demo
6. Add User Story 5 → Test independently → Deploy/Demo (Full feature set)
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Agent Management)
   - Developer B: User Story 2 (Policy Management)
3. Once US1 and US2 complete:
   - Developer C: User Story 3 (Commission Calculation)
4. Once US3 completes:
   - Developer A: User Story 4 (Payment Tracking)
   - Developer B: User Story 5 (Performance Summary)
5. All stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies - can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All tasks include specific file paths for clarity
- Tests are generated by Test Agent from OpenAPI specs
- Performance targets: <200ms per endpoint, 10K req/min throughput
- Security: All endpoints require OAuth2/JWT authentication
