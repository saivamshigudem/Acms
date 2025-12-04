# User Stories for ACMS API

## User Story 1: Agent Management

As an administrator, I want to create and manage agent records in the system.

### Acceptance Criteria

1. **Given** valid agent data with name, email, and commission tier, **When** I create an agent, **Then** the agent is stored with a unique ID and can be retrieved

2. **Given** an existing agent record, **When** I update the agent's email or commission tier, **Then** the changes are persisted and retrievable

3. **Given** an agent ID, **When** I retrieve the agent, **Then** all agent information is returned accurately

4. **Given** an agent record, **When** I delete the agent, **Then** the agent is removed from the system

---

## User Story 2: Policy Management

As an administrator, I want to create and manage group insurance policies linked to agents.

### Acceptance Criteria

1. **Given** an authenticated administrator and an existing agent, **When** I create a new policy with valid coverage type, premium, and effective date, **Then** the policy is stored with a unique ID and linked to the agent

2. **Given** an existing policy, **When** I update the policy premium or coverage details, **Then** changes are persisted

3. **Given** a policy ID, **When** I retrieve the policy, **Then** all policy details including linked agent are returned

4. **Given** a policy, **When** I mark it as inactive, **Then** the policy status changes but historical records remain intact

---

## User Story 3: Commission Calculation

As the system, I want to automatically calculate commissions based on policy premiums and agent commission tiers.

### Acceptance Criteria

1. **Given** a policy with a premium of $1000 and an agent with a 10% commission tier, **When** the commission is calculated, **Then** a commission record is created with amount $100

2. **Given** multiple policies linked to the same agent, **When** commissions are calculated, **Then** each policy generates a separate commission record

3. **Given** a commission record, **When** an administrator retrieves it, **Then** the calculation details (policy, agent, tier, amount) are all visible

4. **Given** a commission with an error in calculation, **When** an administrator updates the commission amount, **Then** the change is recorded with an audit trail