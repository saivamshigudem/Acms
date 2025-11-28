# Feature Specification: ACMS Core APIs

**Feature Branch**: `1-acms-core-apis`  
**Created**: 2025-11-24  
**Status**: Draft  
**Input**: Create comprehensive REST APIs for Agent Commission Management System (ACMS) with CRUD operations for agents, policies, commissions, and payments.

## User Scenarios & Testing

### User Story 1 - Agent Management (Priority: P1)

Insurance administrators need to manage agent records in the system, including creating new agents, updating their information, retrieving agent details, and deactivating agents when they leave the organization.

**Why this priority**: Agent management is foundational—all commission tracking depends on having accurate agent records. This is the first capability needed to bootstrap the system.

**Independent Test**: Can be fully tested by creating an agent, retrieving it, updating details, and deactivating it. Delivers complete agent lifecycle management.

**Acceptance Scenarios**:

1. **Given** an authenticated administrator, **When** they create a new agent with valid name, email, and commission tier, **Then** the agent is stored with a unique ID and can be retrieved
2. **Given** an existing agent record, **When** an administrator updates the agent's email or commission tier, **Then** the changes are persisted and retrievable
3. **Given** an agent record, **When** an administrator requests agent details by ID, **Then** all agent information is returned accurately
4. **Given** an agent with active commissions, **When** an administrator deactivates the agent, **Then** the agent status changes but historical commission records remain intact

---

### User Story 2 - Policy Management (Priority: P1)

Insurance administrators need to create and manage group insurance policies, linking them to agents and tracking policy details such as coverage type, premium amounts, and effective dates.

**Why this priority**: Policies are the basis for commission calculations. Without policy records, commission tracking cannot function. This is equally critical as agent management.

**Independent Test**: Can be fully tested by creating a policy, linking it to an agent, retrieving policy details, and updating policy information. Delivers complete policy lifecycle.

**Acceptance Scenarios**:

1. **Given** an authenticated administrator and an existing agent, **When** they create a new policy with valid coverage type, premium, and effective date, **Then** the policy is stored with a unique ID and linked to the agent
2. **Given** an existing policy, **When** an administrator updates the policy premium or coverage details, **Then** changes are persisted
3. **Given** a policy ID, **When** an administrator retrieves the policy, **Then** all policy details including linked agent are returned
4. **Given** a policy, **When** an administrator marks it as inactive, **Then** the policy status changes but commission records remain traceable

---

### User Story 3 - Commission Calculation (Priority: P1)

The system must automatically calculate commissions based on policy premiums and agent commission tiers, creating commission records that can be retrieved and tracked.

**Why this priority**: Commission calculation is the core business function. Without accurate calculations, the system has no value. This must work correctly from day one.

**Independent Test**: Can be fully tested by creating a policy with a known premium, verifying the commission is calculated correctly based on agent tier, and retrieving the commission record.

**Acceptance Scenarios**:

1. **Given** a policy with a premium of $1000 and an agent with a 10% commission tier, **When** the commission is calculated, **Then** a commission record is created with amount $100
2. **Given** multiple policies linked to the same agent, **When** commissions are calculated, **Then** each policy generates a separate commission record
3. **Given** a commission record, **When** an administrator retrieves it, **Then** the calculation details (policy, agent, tier, amount) are all visible
4. **Given** a commission with an error in calculation, **When** an administrator updates the commission amount, **Then** the change is recorded with an audit trail

---

### User Story 4 - Payment Tracking (Priority: P2)

Administrators need to record commission payments to agents, track payment status, and maintain payment history for audit and reconciliation purposes.

**Why this priority**: Payment tracking is essential for financial accuracy but can be implemented after core commission calculation. Enables payment workflows and reconciliation.

**Independent Test**: Can be fully tested by creating a payment record for a commission, retrieving payment details, and updating payment status.

**Acceptance Scenarios**:

1. **Given** an unpaid commission, **When** an administrator creates a payment record with amount and payment date, **Then** the payment is linked to the commission and status changes to "Paid"
2. **Given** a payment record, **When** an administrator retrieves it, **Then** payment details including commission reference, amount, and date are returned
3. **Given** a payment, **When** an administrator updates the payment status (e.g., from "Pending" to "Completed"), **Then** the change is recorded with timestamp
4. **Given** multiple payments for an agent, **When** an administrator queries payments by agent, **Then** all payment records are returned with correct totals

---

### User Story 5 - Agent Performance Summary (Priority: P2)

Managers need to view summary data showing each agent's total commissions earned, payment status, and performance metrics to make business decisions.

**Why this priority**: Analytics and reporting are important for business intelligence but secondary to core transaction processing. Enables management dashboards.

**Independent Test**: Can be fully tested by creating multiple commissions for an agent and verifying the summary aggregates correctly.

**Acceptance Scenarios**:

1. **Given** an agent with multiple commissions, **When** a manager requests the agent performance summary, **Then** total earned commissions, paid amount, and pending amount are calculated correctly
2. **Given** an agent with no commissions, **When** a manager requests the summary, **Then** all totals return zero with no errors
3. **Given** multiple agents, **When** a manager requests a performance summary report, **Then** all agents are ranked by total commissions earned

---

### Edge Cases & Resolutions

- **Policy without agent**: REJECTED with 400 Bad Request (see Assumptions - strict validation enforced)
- **Zero/negative premiums**: REJECTED with 400 Bad Request (validation in service layer)
- **Deactivated agent with unpaid commissions**: ALLOWED; commissions remain traceable and payable (see US1 acceptance scenario 4)
- **Concurrent payment updates**: REJECTED with 409 Conflict if version mismatch (optimistic locking via @Version field)
- **Policy premium updated after commission calculated**: NOT recalculated; new commissions use updated premium, existing commissions remain immutable

## Requirements

### Functional Requirements

- **FR-001**: System MUST provide a REST API endpoint to create a new agent with name, email, and commission tier
- **FR-002**: System MUST provide a REST API endpoint to retrieve agent details by agent ID
- **FR-003**: System MUST provide a REST API endpoint to update agent information (email, commission tier, status)
- **FR-004**: System MUST provide a REST API endpoint to list all agents with pagination support
- **FR-005**: System MUST provide a REST API endpoint to create a new policy linked to an agent with coverage type, premium, and effective date
- **FR-006**: System MUST provide a REST API endpoint to retrieve policy details by policy ID
- **FR-007**: System MUST provide a REST API endpoint to update policy information (premium, coverage, status)
- **FR-008**: System MUST provide a REST API endpoint to list all policies with filtering by agent or status
- **FR-009**: System MUST automatically calculate commission amount based on policy premium and agent commission tier
- **FR-010**: System MUST store commission records with policy reference, agent reference, calculated amount, and calculation date
- **FR-011**: System MUST provide a REST API endpoint to retrieve commission details by commission ID
- **FR-012**: System MUST provide a REST API endpoint to list commissions with filtering by agent, policy, or date range
- **FR-013**: System MUST provide a REST API endpoint to create a payment record linked to a commission with amount and payment date
- **FR-014**: System MUST provide a REST API endpoint to retrieve payment details by payment ID
- **FR-015**: System MUST provide a REST API endpoint to update payment status (Pending, Completed, Failed)
- **FR-016**: System MUST provide a REST API endpoint to list payments with filtering by agent, status, or date range
- **FR-017**: System MUST provide a REST API endpoint to retrieve agent performance summary including total earned, total paid, and pending commissions
- **FR-018**: System MUST provide a REST API endpoint to retrieve policy commission summary including total commissions generated and payment status
- **FR-019**: All APIs MUST require authentication via OAuth2/JWT bearer token
- **FR-020**: All APIs MUST validate input data against JSON Schema before processing
- **FR-021**: All error responses MUST include consistent error format with error code, message, and timestamp
- **FR-022**: Payment entity MUST include version field for optimistic locking to prevent concurrent update conflicts
- **FR-023**: Policy creation MUST validate agent exists; return 400 Bad Request if agent ID invalid or missing

### Key Entities

- **Agent**: Represents an insurance agent with attributes: ID, name, email, commission tier (percentage), status (active/inactive), created date, last modified date
- **Policy**: Represents a group insurance policy with attributes: ID, agent ID, coverage type, premium amount, effective date, expiration date, status (active/inactive), created date
- **Commission**: Represents a calculated commission with attributes: ID, policy ID, agent ID, commission tier (at time of calculation), calculated amount, calculation date, status (pending/paid)
- **Payment**: Represents a commission payment with attributes: ID, commission ID, payment amount, payment date, status (pending/completed/failed), version (for optimistic locking), created date, last modified date

## Success Criteria

### Measurable Outcomes

- **SC-001**: All CRUD operations for agents, policies, commissions, and payments complete in under 200ms average response time
- **SC-002**: System can handle 10,000 requests per minute without performance degradation
- **SC-003**: Commission calculations are 100% accurate for all tier percentages (verified through automated tests)
- **SC-004**: All API endpoints return consistent error responses with proper HTTP status codes (400 for validation, 401 for auth, 500 for server errors)
- **SC-005**: 100% of API endpoints are documented in OpenAPI 3.0 specification with example requests and responses
- **SC-006**: All APIs require authentication; unauthenticated requests return 401 Unauthorized
- **SC-007**: All input validation failures return 400 Bad Request with specific field-level error messages
- **SC-008**: Payment records can be created and retrieved within 500ms end-to-end
- **SC-009**: Agent performance summary aggregates 1000+ commissions in under 500ms
- **SC-010**: All data persists correctly across system restarts (verified through integration tests)

## Assumptions

- Commission tiers are stored as percentages (e.g., 10 for 10%)
- **Policy creation MUST fail with 400 Bad Request if agent ID is invalid or missing** (strict validation)
- Payments are always linked to exactly one commission
- The system uses UTC for all timestamps
- Initial authentication is handled externally; APIs assume valid JWT tokens are provided
- SQLite3 database is used for persistence with automatic schema creation
- No external integrations are required for this phase (payments are recorded, not processed)
- **Existing commission records are immutable; premium updates only affect future commissions**
- **Payment updates use optimistic locking with version field to prevent concurrent conflicts**

## Dependencies & Constraints

- All APIs must comply with OpenAPI 3.0 specification
- All data models must include governance metadata (x-owner, x-version, x-description)
- All responses must support JSON format
- Pagination must support limit and offset parameters
- All timestamps must be ISO 8601 format
- Commission calculations must be immutable once recorded (updates create new records with audit trail)
