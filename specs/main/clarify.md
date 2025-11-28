# Specification Clarification Report: ACMS Core APIs

**Generated**: 2025-11-24  
**Feature Specification**: specs/main/spec.md  
**Status**: Clarifications Identified & Recommended

---

## Purpose

Identify and resolve underspecified areas in the ACMS Core APIs specification before implementation begins. This ensures the team has clear, unambiguous requirements.

---

## Analysis Results

Based on review of `spec.md`, I identified **3 areas requiring clarification**. These are prioritized by impact: scope > security/privacy > UX > technical details.

---

## Clarification Questions

### Question 1: Policy-Agent Relationship Validation

**Context**: 
```
Assumption (spec.md:L153): "Policies are always linked to exactly one agent at creation time"
Edge Case (spec.md:L96): "What happens when a policy is created without an agent assignment?"
```

**What we need to know**: 
Should the system allow policy creation without a valid agent ID, or should it reject the request?

**Suggested Answers**:

| Option | Answer | Implications |
|--------|--------|--------------|
| A | **Strict Validation**: Reject policy creation with 400 Bad Request if agent ID missing or invalid | Simpler implementation; prevents orphaned policies; matches assumption L153 |
| B | **Permissive**: Allow policy creation with null agent; require agent assignment before commission calculation | More flexible; adds complexity to commission logic; violates assumption L153 |
| C | **Hybrid**: Allow policy creation with optional agent; auto-assign to default agent if missing | Adds default agent concept; increases complexity; unclear business value |

**Recommendation**: **Option A (Strict Validation)** - Aligns with stated assumption and simplifies implementation. Update spec.md to explicitly state this.

---

### Question 2: Commission Recalculation After Premium Update

**Context**:
```
Edge Case (spec.md:L100): "What happens if a policy premium is updated after commission has been calculated?"
Constraint (spec.md:L167): "Commission calculations must be immutable once recorded (updates create new records with audit trail)"
```

**What we need to know**: 
When a policy premium is updated, should the system automatically recalculate commissions, or should it only apply to future commissions?

**Suggested Answers**:

| Option | Answer | Implications |
|--------|--------|--------------|
| A | **Immutable**: Do NOT recalculate existing commissions; only apply new premium to future commission calculations | Simpler; maintains audit trail; existing commissions unchanged |
| B | **Recalculate**: Create new commission records with updated premium; mark old records as superseded | More accurate; adds complexity; requires reconciliation logic |
| C | **Manual Override**: Allow admin to manually trigger recalculation with approval workflow | Most control; significant complexity; requires approval tracking |

**Recommendation**: **Option A (Immutable)** - Matches constraint L167 and simplifies implementation. Existing commissions remain unchanged; new commissions use updated premium.

---

### Question 3: Concurrent Payment Status Updates

**Context**:
```
Edge Case (spec.md:L99): "How does the system handle concurrent payment updates to the same commission?"
Requirement (FR-015): "System MUST provide a REST API endpoint to update payment status"
```

**What we need to know**: 
When two requests try to update the same payment status simultaneously, which one should win?

**Suggested Answers**:

| Option | Answer | Implications |
|--------|--------|--------------|
| A | **Last-Write-Wins**: Accept both updates; last request overwrites previous state | Simplest; may lose intermediate state; acceptable for status transitions |
| B | **Optimistic Locking**: Use version field; reject second update with 409 Conflict | Prevents data loss; requires client retry logic; more robust |
| C | **Pessimistic Locking**: Lock payment record during update; queue concurrent requests | Most robust; may impact performance; adds complexity |

**Recommendation**: **Option B (Optimistic Locking)** - Balances simplicity with data integrity. Add `version` field to Payment entity; increment on each update; reject updates with stale version.

---

## Clarification Decisions

Based on analysis, here are the **recommended clarifications to encode back into spec.md**:

### 1. Update Assumptions (spec.md after L158)

Add these clarified assumptions:
```
- Policy creation MUST fail with 400 Bad Request if agent ID is invalid or missing
- Existing commission records are immutable; premium updates only affect future commissions
- Payment updates use optimistic locking with version field to prevent concurrent conflicts
```

### 2. Update Edge Cases (spec.md:L94-100)

Replace with explicit resolutions:
```
- Policy without agent: REJECTED with 400 Bad Request (see Assumptions)
- Commission recalculation: NOT performed; new commissions use updated premium
- Concurrent payment updates: REJECTED with 409 Conflict if version mismatch (optimistic locking)
- Zero/negative premiums: REJECTED with 400 Bad Request (validation in T026)
- Deactivated agent with unpaid commissions: ALLOWED; commissions remain traceable (see US1 acceptance scenario 4)
```

### 3. Add to Requirements (spec.md after FR-021)

Add these new functional requirements:
```
- **FR-022**: Payment entity MUST include version field for optimistic locking
- **FR-023**: Policy creation MUST validate agent exists; return 400 if agent ID invalid or missing
```

### 4. Update Key Entities (spec.md:L135)

Update Payment entity definition:
```
- **Payment**: Represents a commission payment with attributes: ID, commission ID, payment amount, 
  payment date, status (pending/completed/failed), version (for optimistic locking), created date, 
  last modified date
```

---

## Summary Table

| Clarification | Recommendation | Impact | Effort |
|---------------|----------------|--------|--------|
| Policy-Agent Validation | Strict validation (Option A) | Prevents orphaned policies | Low |
| Commission Recalculation | Immutable (Option A) | Maintains audit trail | Low |
| Concurrent Updates | Optimistic Locking (Option B) | Prevents data loss | Medium |

---

## Implementation Impact

### Affected Requirements
- FR-015: Update payment status (now requires version field)
- FR-023: New requirement for agent validation

### Affected Tasks
- T026: Add agent validation logic
- T042: Implement optimistic locking in payment update
- T043: Add version field validation

### Affected Entities
- Payment: Add version field (Long type, @Version annotation in JPA)

---

## Next Steps

**Option 1: Apply Clarifications Now**
- Update spec.md with clarified assumptions, edge cases, and new requirements
- Update tasks.md to reflect version field requirement
- Proceed to implementation

**Option 2: Proceed Without Clarifications**
- Continue with current spec
- Risk: Implementation may diverge from intent; rework may be needed

**Recommendation**: Apply clarifications now to ensure alignment and reduce rework risk.

---

## Validation Checklist

After applying clarifications, verify:

- [ ] All 5 edge cases have explicit resolutions
- [ ] All assumptions are unambiguous
- [ ] All new requirements (FR-022, FR-023) are clear
- [ ] Payment entity includes version field
- [ ] No remaining [NEEDS CLARIFICATION] markers in spec.md
- [ ] Tasks updated to reflect new requirements

---

## Sign-Off

**Clarification Status**: ✅ COMPLETE

**Ambiguities Identified**: 3  
**Ambiguities Resolved**: 3  
**Remaining Ambiguities**: 0  

**Recommendation**: ✅ Ready for implementation with clarifications applied

---

**Prepared By**: Cascade AI  
**Date**: 2025-11-24  
**Version**: 1.0.0
