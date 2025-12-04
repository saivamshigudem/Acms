# ACMS API Testing Agent - Complete Process & Organization Guide

## 📋 Table of Contents
1. [System Architecture](#system-architecture)
2. [Where Is Your Agent](#where-is-your-agent)
3. [Complete Process Flow](#complete-process-flow)
4. [Folder Organization](#folder-organization)
5. [File Purpose Reference](#file-purpose-reference)
6. [Data Flow Diagram](#data-flow-diagram)

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    ACMS API Testing Agent System                     │
│                                                                       │
│  ┌──────────────────┐      ┌─────────────────┐      ┌────────────┐  │
│  │   Input Files    │      │  Agent Engine   │      │   Output   │  │
│  │   (Specs)        │      │  (Llama3/AI)    │      │   Files    │  │
│  └────────┬─────────┘      └────────┬────────┘      └─────┬──────┘  │
│           │                         │                      │         │
│  • Constitution.md ──────> 1. Generate Tests ──────> test_*.py      │
│  • spec.md                 2. Generate Mock Data    mock_data.json   │
│  • plan.md                 3. Run Pytest           test_results.json │
│                            4. Validate Results     reports (HTML/MD) │
│                            5. Generate Reports                       │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Where Is Your Agent?

### **AGENT LOCATION**

**File:** `run_agent.py`  
**Type:** Main CLI orchestrator and entry point  
**Location:** Root of `API-testing-agent_penguinalpha/` folder  
**Purpose:** Controls the entire workflow

### **AGENT COMPONENTS**

| Component | File | Purpose |
|-----------|------|---------|
| **Core Agent** | `run_agent.py` | CLI interface & workflow orchestration |
| **Mock Data Generator** | `src/mock_data.py` | Generates realistic test data |
| **Test Runner** | `src/test_runner.py` | Executes pytest and parses results |
| **Validators** | `src/validators.py` | Validates API responses |
| **Report Generator** | `src/report_generator.py` | Creates HTML/JSON/MD reports |
| **LLM Integration** | `src/ollama_agent.py` | Connects to Llama3/Ollama |
| **Configuration** | `src/config.py` | Settings and configuration |
| **Spec Parser** | `src/spec_parser.py` | Parses API specifications |
| **Story Parser** | `src/story_parser.py` | Parses user stories |
| **Code Generator** | `src/code_generator.py` | Generates test code templates |

### **AGENT LOCATION IN CODE**

```python
# Entry point to run the agent:
python run_agent.py [command] [options]

# Commands:
python run_agent.py check                              # Check prerequisites
python run_agent.py generate --resource agents         # Generate tests
python run_agent.py run --pattern test_agents          # Run tests
python run_agent.py full --resource agents             # Complete workflow
```

---

## Complete Process Flow

### **PHASE 1: PREREQUISITES CHECK** ✓

```
START
  ↓
Check Ollama Connection
  ├─ Connects to localhost:11434
  └─ Verifies LLM service is running
  ↓
Check Llama3 Model
  ├─ Confirms model is installed
  └─ Verifies model is available
  ↓
Check Configuration Files
  ├─ Constitution file (.specify/memory/constitution.md)
  ├─ Specification file (specs/main/spec.md)
  └─ Plan file (specs/main/plan.md)
  ↓
Check pytest Installation
  └─ Ensures testing framework is available
  ↓
OUTPUT: ✓ All prerequisites met!
```

**Command:**
```bash
python run_agent.py check
```

**What Happens:**
- Verifies all system dependencies
- Checks if Ollama/Llama3 is running
- Validates input specification files exist
- Ensures pytest is installed

---

### **PHASE 2: TEST GENERATION** 🧪

```
START
  ↓
┌─ GENERATE MOCK DATA ─────────────────────┐
│                                          │
│  1. Create Mock Agents                  │
│     ├─ Name: Auto-generated            │
│     ├─ Email: Realistic                │
│     ├─ Status: Active/Inactive         │
│     └─ Commission Tier: 1-5            │
│                                          │
│  2. Create Mock Policies                │
│     ├─ Policy Names: Coverage types    │
│     ├─ Premiums: Realistic amounts     │
│     └─ Effective Dates: Date ranges    │
│                                          │
│  3. Create Mock Commissions             │
│     ├─ Agent-Policy links             │
│     ├─ Calculated amounts             │
│     └─ Status tracking                │
│                                          │
│  4. Create Mock Payments                │
│     ├─ Agent payments                 │
│     ├─ Payment amounts                │
│     └─ Payment dates                  │
│                                          │
│  OUTPUT: mock_data.json (in generated_tests/)
└──────────────────────────────────────────┘
  ↓
┌─ GENERATE TEST CODE ─────────────────────┐
│                                          │
│  1. Read specification file             │
│  2. Read constitution (optional)        │
│  3. Send to Llama3 AI                  │
│     ├─ Prompt with API requirements   │
│     ├─ Include user stories           │
│     ├─ Define test scenarios          │
│     └─ Specify test types             │
│                                          │
│  4. Llama3 generates:                  │
│     ├─ Happy path tests               │
│     ├─ Edge case tests                │
│     ├─ Error scenario tests           │
│     ├─ Security tests                 │
│     └─ Integration tests              │
│                                          │
│  OUTPUT: test_[resource]_generated.py
│          (in generated_tests/tests/python/)
└──────────────────────────────────────────┘
  ↓
OUTPUT: ✓ Generation complete!
```

**Command:**
```bash
python run_agent.py generate --resource agents --mock-agents 5
```

**What Happens:**
1. Generates 5 mock agents + related data
2. Sends API spec to Llama3
3. Llama3 creates comprehensive test code
4. Saves generated tests to file

**Generated Files:**
- `generated_tests/mock_data.json` - Mock test data
- `generated_tests/tests/python/test_[resource]_generated.py` - Generated test code

---

### **PHASE 3: TEST EXECUTION** ▶️

```
START
  ↓
┌─ FIND TEST FILES ────────────────────────┐
│                                          │
│  Search for: **/test_*.py               │
│  Location: generated_tests/tests/       │
│  Found: 1 test file                    │
│         test_agents_generated.py        │
└──────────────────────────────────────────┘
  ↓
┌─ RUN PYTEST ─────────────────────────────┐
│                                          │
│  Command:                               │
│  pytest -v --tb=short --timeout=300     │
│          generated_tests/tests/python/  │
│          test_agents_generated.py       │
│                                          │
│  Pytest:                                │
│  ├─ Collects tests                     │
│  ├─ Runs each test                     │
│  ├─ Measures duration                  │
│  ├─ Captures errors                    │
│  └─ Generates output                   │
│                                          │
│  Output includes:                       │
│  ├─ test_create_agent_happy_path ... PASSED
│  ├─ test_get_agent_details ... FAILED
│  ├─ test_invalid_input ... ERROR
│  └─ Summary: X passed, Y failed        │
└──────────────────────────────────────────┘
  ↓
┌─ PARSE PYTEST OUTPUT ────────────────────┐
│                                          │
│  Extract:                               │
│  ├─ Test names                         │
│  ├─ Test status (PASSED/FAILED/ERROR)  │
│  ├─ Duration for each test             │
│  ├─ Error messages                     │
│  └─ Summary statistics                 │
│                                          │
│  Create ExecutionSummary:               │
│  ├─ total_tests: 16                    │
│  ├─ passed_tests: 9                    │
│  ├─ failed_tests: 4                    │
│  ├─ error_tests: 2                     │
│  ├─ skipped_tests: 1                   │
│  └─ total_duration_ms: 850             │
└──────────────────────────────────────────┘
  ↓
OUTPUT: ✓ Tests completed!
        Passed: 9/16
        Failed: 4/16
        Success Rate: 56.3%
```

**Command:**
```bash
python run_agent.py run --pattern test_agents
```

**What Happens:**
1. Finds all test files matching pattern
2. Executes tests with pytest
3. Captures output and results
4. Parses execution data
5. Calculates statistics

---

### **PHASE 4: REPORT GENERATION** 📊

```
START
  ↓
┌─ GENERATE HTML REPORT ───────────────────┐
│                                          │
│  Creates: test_report.html              │
│  Location: generated_tests/             │
│                                          │
│  Contains:                              │
│  ├─ Executive Summary                  │
│  │  ├─ Total Tests: 16                │
│  │  ├─ Passed: 9 (56.3%)              │
│  │  ├─ Failed: 4 (25%)                │
│  │  └─ Errors: 2 (12.5%)              │
│  │                                    │
│  ├─ Detailed Test Results              │
│  │  ├─ Test Name                      │
│  │  ├─ Status (with color)            │
│  │  ├─ Duration                       │
│  │  └─ Error Messages                 │
│  │                                    │
│  ├─ Performance Metrics                │
│  │  ├─ Average Duration               │
│  │  ├─ Total Time                     │
│  │  └─ Success Rate                   │
│  │                                    │
│  └─ Chart/Visualization               │
│     ├─ Pass/Fail Pie Chart            │
│     └─ Timeline Graph                 │
│                                          │
│  Styling:                               │
│  ├─ Professional CSS                  │
│  ├─ Color-coded status                │
│  ├─ Responsive design                 │
│  └─ Easy to read tables               │
└──────────────────────────────────────────┘
  ↓
┌─ GENERATE MARKDOWN REPORT ───────────────┐
│                                          │
│  Creates: test_report.md                │
│  Location: generated_tests/             │
│                                          │
│  Format:                                │
│  ├─ Markdown tables                    │
│  ├─ GitHub-compatible                 │
│  ├─ Easy to version control           │
│  └─ Embeddable in documentation       │
└──────────────────────────────────────────┘
  ↓
┌─ GENERATE JSON REPORT ───────────────────┐
│                                          │
│  Creates: test_report.json              │
│  Location: generated_tests/             │
│                                          │
│  Format:                                │
│  ├─ Machine-readable                  │
│  ├─ Full test details                 │
│  ├─ Timestamps                        │
│  └─ Parseable by other tools          │
└──────────────────────────────────────────┘
  ↓
OUTPUT: ✓ Reports generated!
        ├─ HTML: test_report.html
        ├─ MD: test_report.md
        └─ JSON: test_report.json
```

**Command:**
```bash
python run_agent.py full --resource agents --mock-agents 5
```

**What Happens:**
1. Runs all 4 phases (Prerequisites → Generate → Run → Report)
2. Generates all 3 report types
3. Saves to `generated_tests/` folder

---

## Folder Organization

### **Current Structure (BEFORE REORGANIZATION)**

```
API-testing-agent_penguinalpha/
├── run_agent.py                    [MAIN CLI]
├── your_api_module.py             [MOCK API]
├── requirements.txt
├── pytest.ini
├── setup.py
├── src/
│   ├── __init__.py
│   ├── ollama_agent.py           [LLM INTEGRATION]
│   ├── config.py                 [CONFIGURATION]
│   ├── code_generator.py         [CODE GEN]
│   ├── spec_parser.py            [SPEC PARSING]
│   ├── story_parser.py           [STORY PARSING]
│   ├── test_generator.py         [TEST GEN]
│   ├── mock_data.py              [MOCK DATA]
│   ├── test_runner.py            [TEST RUNNER]
│   ├── validators.py             [VALIDATION]
│   ├── report_generator.py       [REPORTING]
│   └── utils/
│       ├── __init__.py
│       ├── formatters.py         [FORMATTERS]
│       ├── helpers.py            [HELPERS]
│       └── validators.py         [VALIDATORS]
├── templates/
│   ├── conftest.jinja2
│   ├── test_module.jinja2
│   └── test_specification.jinja2
├── examples/
│   ├── sample_openapi.yaml
│   └── sample_stories.md
├── generated_tests/              [OUTPUT FOLDER]
│   ├── mock_data.json
│   ├── test_report.html
│   ├── test_report.md
│   ├── test_report.json
│   ├── tests/
│   │   ├── python/
│   │   │   └── test_agents_generated.py
│   └── __pycache__/
├── docs/
│   ├── AI_AGENT_GUIDE.md
│   ├── FUNCTIONAL_TESTING_GUIDE.md
│   ├── QUICK_REFERENCE.txt
│   └── QUICK_START.md
└── [DOCUMENTATION FILES]
    ├── README.md
    ├── WORKFLOW.html
    ├── ANALYSIS.md
    └── etc.
```

### **RECOMMENDED STRUCTURE (AFTER REORGANIZATION)**

```
API-testing-agent_penguinalpha/
│
├── 📌 CORE / ENTRY POINTS
│   ├── run_agent.py              [Main CLI entry point]
│   ├── setup.py                  [Project setup]
│   ├── requirements.txt           [Dependencies]
│   └── pytest.ini                [Pytest config]
│
├── 🤖 AGENT CORE (src/agent/)
│   ├── __init__.py
│   ├── orchestrator.py           [Main orchestration logic]
│   ├── llm_interface.py          [Llama3/Ollama interface]
│   └── config.py                 [Configuration management]
│
├── 📝 DATA GENERATION (src/generators/)
│   ├── __init__.py
│   ├── mock_data_generator.py    [Mock data generation]
│   ├── test_code_generator.py    [Test code generation]
│   ├── spec_parser.py            [Specification parsing]
│   └── story_parser.py           [User story parsing]
│
├── 🧪 TEST EXECUTION (src/testing/)
│   ├── __init__.py
│   ├── test_runner.py            [Pytest execution]
│   ├── test_executor.py          [Test execution logic]
│   ├── pytest_parser.py          [Pytest output parsing]
│   └── result_collector.py       [Results collection]
│
├── ✅ VALIDATION (src/validation/)
│   ├── __init__.py
│   ├── validators.py             [Response validators]
│   ├── schema_validator.py       [Schema validation]
│   ├── business_logic_validator.py
│   └── security_validator.py     [Security checks]
│
├── 📊 REPORTING (src/reporting/)
│   ├── __init__.py
│   ├── report_generator.py       [Report generation]
│   ├── html_reporter.py          [HTML report formatting]
│   ├── markdown_reporter.py      [Markdown formatting]
│   └── json_reporter.py          [JSON formatting]
│
├── 🛠️ UTILITIES (src/utils/)
│   ├── __init__.py
│   ├── helpers.py                [Helper functions]
│   ├── formatters.py             [Output formatters]
│   ├── validators.py             [Utility validators]
│   └── constants.py              [Constants]
│
├── 📚 MOCK API (src/mock_api/)
│   ├── __init__.py
│   ├── api_client.py             [Mock API client]
│   └── your_api_module.py        [Mock ACMS API]
│
├── 📦 TEMPLATES (templates/)
│   ├── conftest.jinja2           [Pytest config template]
│   ├── test_module.jinja2        [Test module template]
│   └── test_specification.jinja2 [Test spec template]
│
├── 📋 EXAMPLES (examples/)
│   ├── sample_openapi.yaml       [Sample API spec]
│   ├── sample_stories.md         [Sample user stories]
│   └── README.md                 [Examples guide]
│
├── 📖 DOCUMENTATION (docs/)
│   ├── README.md                 [Main readme]
│   ├── GETTING_STARTED.md        [Setup guide]
│   ├── ARCHITECTURE.md           [Architecture]
│   ├── API_AGENT_GUIDE.md        [Agent usage]
│   ├── PROCESS_AND_ORGANIZATION.md [THIS FILE]
│   └── TROUBLESHOOTING.md        [Troubleshooting]
│
└── 📤 OUTPUT/RESULTS (generated_tests/)
    ├── mock_data/
    │   ├── agents.json
    │   ├── policies.json
    │   ├── commissions.json
    │   └── payments.json
    │
    ├── testcases/
    │   ├── agents/
    │   │   └── test_agents_generated.py
    │   ├── policies/
    │   │   └── test_policies_generated.py
    │   └── commissions/
    │       └── test_commissions_generated.py
    │
    └── reports/
        ├── latest/
        │   ├── test_report.html
        │   ├── test_report.md
        │   └── test_report.json
        │
        └── archive/
            ├── test_report_2025-01-10.html
            └── test_report_2025-01-10.json
```

---

## File Purpose Reference

### **AGENT CORE COMPONENTS**

| File | Purpose | Input | Output |
|------|---------|-------|--------|
| `run_agent.py` | Main CLI orchestrator | CLI args | Command execution |
| `src/agent/llm_interface.py` | Connects to Llama3/Ollama | API specs | Generated test code |
| `src/agent/config.py` | Configuration management | config files | Config objects |

### **DATA GENERATION**

| File | Purpose | Input | Output |
|------|---------|-------|--------|
| `src/generators/mock_data_generator.py` | Creates test data | Scenarios | `mock_data.json` |
| `src/generators/test_code_generator.py` | Generates test code | API specs | Python test file |
| `src/generators/spec_parser.py` | Parses API specs | `spec.md` | Parsed spec object |
| `src/generators/story_parser.py` | Parses user stories | `plan.md` | Story objects |

### **TEST EXECUTION**

| File | Purpose | Input | Output |
|------|---------|-------|--------|
| `src/testing/test_runner.py` | Executes pytest | Test files | Execution results |
| `src/testing/pytest_parser.py` | Parses pytest output | pytest stdout/stderr | TestResult objects |
| `src/testing/result_collector.py` | Collects results | TestResults | ExecutionSummary |

### **REPORTING**

| File | Purpose | Input | Output |
|------|---------|-------|--------|
| `src/reporting/report_generator.py` | Main report coordinator | ExecutionSummary | All report formats |
| `src/reporting/html_reporter.py` | HTML formatting | Summary data | `test_report.html` |
| `src/reporting/markdown_reporter.py` | Markdown formatting | Summary data | `test_report.md` |
| `src/reporting/json_reporter.py` | JSON formatting | Summary data | `test_report.json` |

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    COMPLETE DATA FLOW                               │
└─────────────────────────────────────────────────────────────────────┘

[INPUT PHASE]
     │
     ├─→ constitution.md ──→ ┐
     ├─→ spec.md ────────────┤
     └─→ plan.md ────────────┤
                             ├─→ spec_parser.py ──→ ParsedSpec
                             │
                             └─→ story_parser.py ──→ Stories


[GENERATION PHASE]
                        
     ParsedSpec ─┐
     Stories ───┤──→ llm_interface.py ──→ Llama3/Ollama ──→ test_code.py
                │
     Config ────┘


     ┌──→ agents scenario ──→ mock_data_generator.py ──→ mock_data.json
     │
     ├──→ policies scenario
     │
     ├──→ commissions scenario
     │
     └──→ payments scenario


[EXECUTION PHASE]

     test_code.py ──→ test_runner.py ──→ pytest command ──→ pytest output
                            │                                    │
                            └────────────────────────────────────┘
                                                  │
                                            pytest_parser.py
                                                  │
                              ┌───────────────────┴─────────────────┐
                              ↓                                       ↓
                         TestResult[]                         ExecutionSummary
                              │
                              └───────────────────┬─────────────────┐
                                                  ↓


[REPORTING PHASE]

     ExecutionSummary ──┬──→ html_reporter.py ──→ test_report.html
                        ├──→ markdown_reporter.py ──→ test_report.md
                        └──→ json_reporter.py ──→ test_report.json


[OUTPUT PHASE]

     generated_tests/
     ├── mock_data.json
     ├── testcases/
     │   └── test_agents_generated.py
     └── reports/
         ├── test_report.html
         ├── test_report.md
         └── test_report.json
```

---

## Quick Command Reference

### **Check Everything**
```bash
python run_agent.py check
```

### **Generate Tests Only**
```bash
python run_agent.py generate --resource agents --mock-agents 5
```

### **Run Tests Only**
```bash
python run_agent.py run --pattern test_agents
```

### **Complete Workflow** (Recommended)
```bash
python run_agent.py full --resource agents --mock-agents 5
```

### **View Reports**
```bash
# HTML Report (open in browser)
start generated_tests/test_report.html

# Markdown Report
cat generated_tests/test_report.md

# JSON Report
type generated_tests/test_report.json
```

---

## Summary

**Where Is The Agent?**
- **Main Entry:** `run_agent.py`
- **Core Logic:** `src/agent/`
- **Data Generation:** `src/generators/`
- **Test Execution:** `src/testing/`
- **Reporting:** `src/reporting/`

**Where Do Tests Get Generated?**
- **Location:** `generated_tests/testcases/[resource]/`
- **File Pattern:** `test_[resource]_generated.py`
- **Generated By:** Llama3 AI via `llm_interface.py`

**Where Do Results Go?**
- **Location:** `generated_tests/reports/`
- **Formats:** HTML, Markdown, JSON
- **Files:** `test_report.[html|md|json]`

**What Operations Happen?**
1. **Prerequisites Check** - Validate system setup
2. **Mock Data Generation** - Create realistic test data
3. **Test Code Generation** - Llama3 generates tests
4. **Test Execution** - Pytest runs generated tests
5. **Results Validation** - Parse and verify results
6. **Report Generation** - Create 3 report formats

---

**Last Updated:** December 3, 2025  
**Version:** 1.0
