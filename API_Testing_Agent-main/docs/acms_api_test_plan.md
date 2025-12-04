# ACMS API Test Plan

This document summarizes **all API tests** you need to perform for the ACMS (Agent Commission Management System) backend based on the project specification, plan, tasks, and constitution.

Tests are structured **by User Story**, **by endpoint**, and **by scenario**, ensuring complete coverage.

---

# 1. Authentication & Security Tests
_All endpoints require OAuth2/JWT. These tests apply globally._

### 1.1 Authentication
- Request any endpoint **without token** → expect **401 Unauthorized**
- Request any endpoint with **expired token** → expect **401 Unauthorized**
- Request any endpoint with **invalid signature** → expect **401 Unauthorized**

### 1.2 Authorization
(RBAC rules: Admin-only operations like create/update/delete)
- Agent user tries admin-only endpoint → **403 Forbidden**
- Admin performs standard operation → **200 OK**

### 1.3 CORS
- Requests from **allowed origin** → allowed
- Requests from **non-whitelisted origin** → expect **403 Forbidden**

### 1.4 Rate Limiting
- Exceed 100 req/min → expect **429 Too Many Requests**

### 1.5 CSRF (if enabled)
- POST/PUT/DELETE without CSRF token → **403 Forbidden**

---

# 2. Agent API Tests (User Story 1)
Endpoints:
- POST /agents
- GET /agents/{id}
- PUT /agents/{id}
- GET /agents
- DELETE /agents/{id}

### 2.1 Create Agent (POST /agents)
✔ Valid creation → 201 Created
✔ Missing name → 400
✔ Invalid email → 400
✔ Commission tier <0 or >100 → 400
✔ Duplicate email → 409

### 2.2 Retrieve Agent (GET /agents/{id})
✔ Valid ID → 200 & correct response
✔ Nonexistent ID → 404

### 2.3 Update Agent (PUT /agents/{id})
✔ Valid update (email, tier) → 200
✔ Change email to invalid → 400
✔ Change commission tier beyond range → 400
✔ Agent inactive but updated → allowed with restrictions

### 2.4 List Agents (GET /agents)
✔ Pagination: limit/offset
✔ Filtering (if implemented)

### 2.5 Deactivate Agent (DELETE /agents/{id})
✔ Valid deactivation → 200
✔ Agent with commissions → status changes, commissions remain
✔ Already inactive → 200 idempotent

---

# 3. Policy API Tests (User Story 2)
Endpoints:
- POST /policies
- GET /policies/{id}
- PUT /policies/{id}
- GET /policies
- DELETE /policies/{id}

### 3.1 Create Policy
✔ Valid creation → 201
✔ Invalid agent ID → 400
✔ Premium <= 0 → 400
✔ Invalid date format → 400

### 3.2 Retrieve Policy
✔ Valid ID → 200
✔ Nonexistent ID → 404

### 3.3 Update Policy
✔ Update premium → 200
✔ Update coverage type → 200
✔ Negative premium → 400
✔ Invalid agent reassignment → 400

### 3.4 List Policies
✔ Filter by agentId
✔ Filter by status
✔ Pagination

### 3.5 Deactivate Policy
✔ Valid operation → status becomes inactive
✔ Policy already inactive → idempotent

---

# 4. Commission API Tests (User Story 3)
Endpoints:
- POST /commissions
- GET /commissions/{id}
- PUT /commissions/{id}
- GET /commissions

### 4.1 Commission Calculation
✔ Policy premium = 1000, agent tier = 10 → commission = 100
✔ Commission created only once per policy (depending on design)
✔ Commission data includes: policyId, agentId, tier snapshot

### 4.2 Retrieve Commission
✔ Valid ID → 200
✔ Nonexistent ID → 404

### 4.3 Update Commission Amount
✔ Valid update → 200
✔ Audit trail created
✔ Negative amount → 400

### 4.4 List Commissions
✔ Filter by agent
✔ Filter by policy
✔ Filter by date range

### 4.5 Edge Cases
✔ Policy premium updated AFTER commission → old commission unchanged
✔ Agent made inactive → commission retrieval still works

---

# 5. Payment API Tests (User Story 4)
Endpoints:
- POST /payments
- GET /payments/{id}
- PUT /payments/{id}
- GET /payments

### 5.1 Create Payment
✔ Valid payment creation → 201
✔ Payment amount = commission amount → valid
✔ Payment > commission → 400
✔ Invalid commission ID → 400

### 5.2 Retrieve Payment
✔ Valid ID → 200
✔ Nonexistent → 404

### 5.3 Update Payment Status
✔ Pending → Completed → 200
✔ Completed → Pending → invalid (unless allowed)
✔ Version mismatch → 409 (optimistic locking)

### 5.4 List Payments
✔ Filter by agent
✔ Filter by status
✔ Filter by date

---

# 6. Performance Summary API Tests (User Story 5)
Endpoints:
- GET /performance/agents/{id}
- GET /performance/agents

### 6.1 Per-Agent Summary
✔ With multiple commissions → totals correct
✔ With payments → paid vs pending correct
✔ No commissions → all zeros

### 6.2 All Agents Summary Report
✔ Agents sorted by total earned
✔ Large dataset (<500ms)

---

# 7. Cross-Story Integration Tests
These are your end-to-end workflows.

### 7.1 Full Business Flow
1. Create agent
2. Create policy linked to agent
3. Commission calculated automatically
4. Payment recorded
5. Performance summary updates

Expected: All stages return correct aggregated results.

### 7.2 Invalid Flow Tests
- Create policy for non-existent agent → 400
- Create commission when policy inactive → 400
- Payment for nonexistent commission → 400
- Update policy after commission → commission unchanged

---

# 8. Error Format Tests
All errors MUST follow ACMS error schema:
- errorCode
- message
- timestamp
- field errors

Test cases:
✔ Validation failure returns structured error
✔ Authentication failure returns structured error
✔ 500 internal server error is masked (no stack traces)

---

# 9. Database Persistence Tests
✔ Data persists after restart
✔ Commission calculations stored with audit trail
✔ Payments reflect updated version numbers

---

# 10. Performance Tests
- All CRUD operations <200ms
- Summary endpoint <500ms
- 10K requests/min sustained load

---

# Summary of What You Must Implement in Tests
| API Module | Test Type | Required |
|------------|-----------|----------|
| Agents | CRUD + validation + listing + deactivate | ✔|
| Policies | CRUD + validation + linking + deactivate | ✔|
| Commissions | Calculation + retrieval + update + list | ✔|
| Payments | CRUD + optimistic locking + list | ✔|
| Performance | Aggregations + summaries | ✔|
| Security | JWT + CORS + rate limit + CSRF | ✔|
| Integration | Full workflow | ✔|
| Error Schema | Consistency tests | ✔|

This is the **complete test catalog** required for ACMS according to spec.md, plan.md, tasks.md, and constitution.md.

