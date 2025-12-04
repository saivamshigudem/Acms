# API Testing Agent – Final Overview Document

This document explains **what we are building**, **what the API Testing Agent should do**, **how it will work with Llama 3 (Ollama)**, and **what functional/API testing responsibilities it will automate**.

It is written for complete beginners and keeps everything simple, structured, and clear.

---

# 1. What Are We Building?
We are building an **API Testing Agent**.

Here “agent” means:

> **An AI-driven tester that automatically understands, generates, and executes tests for backend REST APIs.**

It is NOT an insurance agent.  
It is an **AI agent** that:
- Reads project specifications (spec.md, plan.md, tasks.md, etc)
- Understands endpoints
- Creates the required test cases
- Generates Python tests
- Executes tests
- Reports results

This agent replaces most manual API testing work.

---

# 2. What Problem Are We Solving?
You have a backend (Spring Boot ACMS API) with many endpoints:
- Agents
- Policies
- Commissions
- Payments
- Performance Summary

Testing these manually is slow and difficult.

Your **API Testing Agent** will:
- Understand the system
- Generate valid + invalid API calls
- Verify responses
- Return PASS/FAIL output

This dramatically reduces testing time.

---

# 3. Why Are We Using Llama 3 (Ollama)?
Because:
- You don’t have API keys for OpenAI
- Ollama allows **local Llama 3 inference**
- It runs fully offline
- It is free
- It is ideal for building local agents

Your API Testing Agent will send prompts to Ollama instead of OpenAI.

---

# 4. What Should the API Testing Agent Do?
Below is the **complete responsibility list**.

## ✔ 4.1 Understand the Project
The agent must:
- Read spec.md, plan.md, tasks.md, constitution.md
- Identify all available API endpoints
- Understand data models and rules
- Understand the functional testing requirements

## ✔ 4.2 Generate API Test Plans (Functional Testing)
For each endpoint, the agent generates:
- Happy path test cases
- Edge case test cases
- Error test cases
- Input validation tests
- Business rule tests
- Response schema tests

This results in a complete **test catalog**.

## ✔ 4.3 Generate Python Test Code
The agent should generate test files such as:
- test_agents.py
- test_policies.py
- test_commissions.py
- test_payments.py
- test_performance.py

Using libraries like:
```python
import requests
import pytest
```

## ✔ 4.4 Execute the Tests Automatically
The agent should:
- Run the tests
- Collect results
- Identify failures
- Explain what failed and why
- Suggest fixes if possible

## ✔ 4.5 Provide Test Coverage Summary
The agent should summarize:
- Total test cases
- Passed
- Failed
- Missed scenarios
- API contract mismatches

## ✔ 4.6 Continuously Update Tests
Whenever:
- The spec changes
- A new endpoint is added
- A field is added/removed

The agent should:
- Detect the change
- Regenerate test cases
- Update Python files

---

# 5. Functional Testing That the Agent Will Perform
Since our focus is **functional testing**, here is everything the agent will test.

## ✔ 5.1 CRUD Functionality
Does each API endpoint behave correctly?
- Create
- Read
- Update
- Delete/Deactivate

## ✔ 5.2 Response Validation
Check:
- Status codes
- JSON response fields
- Data types
- Required fields

## ✔ 5.3 Business Rule Logic
Examples:
- Commission = premium × tier
- Policy must have valid agent
- Payment amount cannot exceed commission
- No negative premiums

## ✔ 5.4 Error Handling
Test situations like:
- Invalid IDs
- Missing fields
- Wrong data types
- Constraint violations

## ✔ 5.5 Security Flags (Basic Ones)
Even though you are not doing full security testing, functional tests include:
- 401 without token
- 403 for forbidden roles

---

# 6. How the Agent Will Work Internally
Below is the architecture.

```
ACMS Specs ───────────────▶ Parsing Layer
                                  │
                                  ▼
                           Understanding Layer
                                  │
                                  ▼
                          Test Case Generator
                                  │
                                  ▼
                          Python Test Generator
                                  │
                                  ▼
                          Test Executor (Pytest)
                                  │
                                  ▼
                         Results Analyzer + Report
```

---

# 7. How You Will Use Llama 3 & Ollama
Your workflow:

1. Start Ollama server:
```
ollama serve
```
2. Call the agent using a simple script:
```bash
python run_agent.py
```
3. The agent sends prompts like:
```
"Read these files and generate tests."
```
4. Llama reads the files, understands endpoints, generates tests.
5. Tests are executed and results returned.

This becomes your **hands-off API testing workflow**.

---

# 8. What Will the Agent’s Output Look Like?
A typical output:

```
=== Running Functional Tests ===
Collected 32 tests

test_agents.py::test_create_agent_valid     PASSED
test_agents.py::test_create_agent_invalid   PASSED
...
test_policies.py::test_policy_missing_agent FAILED
    Reason: Expected 400, got 500

=== SUMMARY ===
PASSED: 30
FAILED: 2

Fix Suggestions:
- PolicyService: validate agentId exists before DB insert
```

This gives you:
- What passed
- What failed
- Why it failed
- Suggestions

---

# 9. What You Need to Prepare (Simple Checklist)
Before building the agent, prepare:

### ✔ ACMS API server running
Spring Boot API listening on:  
`http://localhost:8080`

### ✔ File Inputs
- spec.md
- plan.md
- tasks.md
- constitution.md

### ✔ Python environment
```
pip install requests pytest
```

### ✔ A simple wrapper to call Llama 3
A Python script like:
```python
from ollama import chat
```

### ✔ Prompt templates
- "Read these files and generate test cases"
- "Write Python pytest code"
- "Execute tests and summarize results"

---

# 10. Final Expected Capabilities of the API Testing Agent
Here is the complete, final capabilities list.

| Capability | Description |
|-----------|-------------|
| ✔ Read Specs | Reads all md files and understands APIs |
| ✔ Identify Endpoints | Finds all routes from spec | 
| ✔ Generate Test Cases | Happy path + edge cases + errors |
| ✔ Generate Test Code | Python pytest files |
| ✔ Run Tests | Automatically execute tests |
| ✔ Analyze Failures | Explain what & why |
| ✔ Suggest Fixes | Developer-friendly debugging hints |
| ✔ Regenerate Tests | Whenever API spec changes |
| ✔ Version Awareness | Tracks changes in endpoints |

This is the **complete scope** of your API Testing Agent.

---

# 11. What You Should Build First
Your step-by-step plan:

### **Step 1** – Build a basic test generator  
Take spec.md → extract endpoints → generate test list.

### **Step 2** – Generate Python tests  
Llama writes pytest code.

### **Step 3** – Execute the tests  
Use subprocess or pytest programmatically.

### **Step 4** – Build the feedback loop  
Feed test results back into Llama.

### **Step 5** – Build full agent workflow
Connect all steps with a single command.

---

# 12. Final Summary
You are building:

> A functional API Testing Agent powered by Llama 3 that reads specifications, generates tests, executes them, analyzes results, and automates QA for the ACMS backend.

It focuses only on:
- Functional testing
- API contract validation
- Business logic validation
- Error handling tests

It does **not** handle:
- Load testing
- Security testing (beyond 401/403)
- UI tests
- Complex DevOps

This document gives you the full picture of:
- What needs to be tested
- What the agent must do
- How it will work
- How to proceed step-by-step

You can now confidently start building your API Testing Agent.

---
If you'd like next, I can generate:
- The **system architecture diagram**
- The **test agent prompt templates**
- The **run_agent.py script** to integrate with Llama 3
- Complete **pytest boilerplate files**
- A **phase-wise implementation plan**