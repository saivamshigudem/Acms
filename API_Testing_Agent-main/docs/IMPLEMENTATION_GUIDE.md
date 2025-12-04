# ACMS API Testing Agent - Implementation Guide

**Status**: Complete ✅  
**Version**: 1.0.0  
**Date**: December 2, 2025

---

## Overview

This guide explains the complete API Testing Agent implementation for ACMS, including:
- Architecture and components
- Setup and installation
- Usage instructions
- Troubleshooting

---

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                   ACMS Testing Agent                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   │
│  │   Ollama     │   │   Spec       │   │   Mock Data  │   │
│  │   Agent      │   │   Parser     │   │   Generator  │   │
│  │ (Llama3)     │   │              │   │              │   │
│  └──────────────┘   └──────────────┘   └──────────────┘   │
│         │                   │                   │          │
│         └───────────────────┼───────────────────┘          │
│                             │                              │
│                    ┌────────▼────────┐                     │
│                    │  Test Generator │                     │
│                    │  (LLM-based)    │                     │
│                    └────────┬────────┘                     │
│                             │                              │
│                    ┌────────▼────────┐                     │
│                    │  Pytest Code    │                     │
│                    │  Generator      │                     │
│                    └────────┬────────┘                     │
│                             │                              │
│                    ┌────────▼────────┐                     │
│                    │  Test Runner    │                     │
│                    │  (Pytest)       │                     │
│                    └────────┬────────┘                     │
│                             │                              │
│          ┌──────────────────┼──────────────────┐           │
│          │                  │                  │           │
│    ┌─────▼─────┐    ┌──────▼──────┐    ┌─────▼──────┐   │
│    │ Validator │    │  Report     │    │ Result     │   │
│    │ & Checker │    │ Generator   │    │ Collector  │   │
│    └───────────┘    └─────────────┘    └────────────┘   │
│                                                           │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. **ollama_agent.py**
- Connects to Ollama/Llama3 service
- Sends prompts for test generation
- Handles streaming and non-streaming responses

#### 2. **mock_data.py**
- Generates realistic mock data
- Supports all ACMS entities (agents, policies, commissions, payments)
- Reproducible data generation with seeds

#### 3. **test_generator.py**
- Parses OpenAPI specifications
- Parses user stories
- Generates comprehensive test cases

#### 4. **test_runner.py**
- Executes pytest tests
- Parses pytest output
- Collects execution results

#### 5. **validators.py**
- Response validation
- Schema validation
- Business logic validation
- Security validation
- Compliance validation

#### 6. **report_generator.py**
- Generates HTML reports
- Generates Markdown reports
- Generates JSON reports

#### 7. **run_agent.py** (Main Entry Point)
- CLI interface
- Orchestrates complete workflow
- Coordinates all components

---

## Setup & Installation

### Prerequisites

1. **Python 3.11+**
   ```bash
   python --version
   # Should show Python 3.11.x or higher
   ```

2. **Ollama with Llama3**
   ```bash
   # Download Ollama from https://ollama.ai/download
   
   # Start Ollama service (in separate terminal)
   ollama serve
   
   # Download Llama3 model (first time)
   ollama pull llama3
   ```

3. **Git Repository**
   - Should be in the ACMS project root
   - Access to spec files (specs/main/spec.md, etc.)

### Installation Steps

1. **Clone/Navigate to Repository**
   ```bash
   cd /path/to/Acms/API-testing-agent_penguinalpha
   ```

2. **Create Python Virtual Environment**
   ```bash
   python -m venv venv
   
   # Activate venv
   # On Windows:
   venv\Scripts\activate
   
   # On macOS/Linux:
   source venv/bin/activate
   ```

3. **Install Dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Verify Setup**
   ```bash
   python run_agent.py check
   ```

   Expected output:
   ```
   ============================================================
   ACMS API Testing Agent - Prerequisites Check
   ============================================================
   
   Checking prerequisites...
     Checking Ollama connection... ✓
     Checking Llama3 model... ✓
     Checking Constitution file... ✓
     Checking pytest... ✓
   
   ============================================================
   ✓ All prerequisites met!
   ============================================================
   ```

---

## Usage

### Quick Start (5 minutes)

```bash
# 1. Check prerequisites
python run_agent.py check

# 2. Run complete workflow (generate + run + report)
python run_agent.py full --resource agents --mock-agents 5

# 3. Check generated reports
# - generated_tests/test_report.html (open in browser)
# - generated_tests/test_report.md (markdown)
# - generated_tests/test_report.json (raw data)
```

### Step-by-Step Usage

#### Step 1: Generate Test Cases and Mock Data
```bash
python run_agent.py generate --resource agents --mock-agents 5 --output ./generated_tests
```

**Options**:
- `--resource`: API resource to test (agents, policies, commissions, payments, performance)
- `--mock-agents`: Number of mock agents to generate (default: 5)
- `--output`: Output directory (default: ./generated_tests)

**Output Files**:
- `generated_tests/tests/python/test_agents_generated.py` - Generated test code
- `generated_tests/mock_data.json` - Mock test data

#### Step 2: Run Tests
```bash
python run_agent.py run --pattern "test_agents" --output ./generated_tests
```

**Options**:
- `--pattern`: Filter tests by pattern (optional)
- `--output`: Output directory

**Output Files**:
- `generated_tests/test_execution_summary.json` - Raw results
- `generated_tests/test_report.html` - Visual report
- `generated_tests/test_report.md` - Markdown report
- `generated_tests/test_report.json` - Detailed report

#### Step 3: View Reports
```bash
# Open HTML report in browser
open generated_tests/test_report.html

# Or view Markdown report
cat generated_tests/test_report.md
```

### Advanced Commands

#### Generate for Multiple Resources
```bash
# Generate tests for all endpoints
for resource in agents policies commissions payments performance; do
  python run_agent.py generate --resource $resource
done
```

#### Run Specific Tests
```bash
# Run only agent tests
python run_agent.py run --pattern "test_agents"

# Run only error scenario tests
python run_agent.py run --pattern "error_scenario"

# Run only security tests
python run_agent.py run --pattern "security"
```

#### Generate Custom Reports
```bash
# Generate HTML report only
python src/report_generator.py

# Generate with custom mock data size
python run_agent.py generate --resource agents --mock-agents 20
```

---

## Test Generation Details

### Test Types Generated

1. **Happy Path Tests** (✓)
   - Valid input data
   - Expected successful response
   - Validates basic functionality

2. **Error Scenario Tests** (✗)
   - Invalid input data
   - Expected error responses
   - Validates error handling

3. **Edge Case Tests** (⚠)
   - Boundary conditions
   - Empty collections
   - Null values
   - Large datasets

4. **Security Tests** (🔒)
   - Missing authentication
   - Invalid token
   - Insufficient permissions
   - CORS validation

5. **Business Logic Tests** (💼)
   - Commission calculations
   - Premium validation
   - Date range validation
   - Payment constraints

6. **Integration Tests** (🔗)
   - Cross-entity workflows
   - Data consistency
   - Referential integrity

### Expected Test Output Structure

```
test_agents_generated.py
├── Imports & Setup
├── Fixtures (API client, auth, mock data)
├── Happy Path Tests
│   ├── test_create_agent_valid()
│   ├── test_get_agent_valid()
│   ├── test_update_agent_valid()
│   └── test_delete_agent_valid()
├── Error Scenario Tests
│   ├── test_create_agent_invalid_email()
│   ├── test_get_agent_not_found()
│   ├── test_update_agent_invalid_tier()
│   └── test_delete_agent_not_found()
├── Edge Case Tests
│   ├── test_list_agents_empty()
│   ├── test_list_agents_pagination()
│   └── test_list_agents_large_dataset()
├── Security Tests
│   ├── test_agent_requires_auth()
│   ├── test_agent_requires_permission()
│   └── test_agent_cors_allowed()
└── Business Logic Tests
    ├── test_agent_commission_tier_valid()
    ├── test_agent_email_format_valid()
    └── test_agent_deactivation_idempotent()
```

---

## Mock Data Structure

### Agents
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "555-1234-5678",
  "commissionTier": 12.5,
  "hireDate": "2024-01-15T00:00:00",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "status": "ACTIVE",
  "isActive": true
}
```

### Policies
```json
{
  "agentId": 1,
  "coverageType": "HEALTH",
  "premium": 1000.00,
  "effectiveDate": "2024-01-15T00:00:00",
  "expirationDate": "2025-01-15T00:00:00",
  "status": "ACTIVE"
}
```

### Commissions
```json
{
  "policyId": 1,
  "agentId": 1,
  "commissionTier": 12.5,
  "calculatedAmount": 125.00,
  "calculationDate": "2024-01-15T00:00:00",
  "status": "PENDING"
}
```

### Payments
```json
{
  "commissionId": 1,
  "amount": 125.00,
  "paymentDate": "2024-01-20T00:00:00",
  "status": "COMPLETED"
}
```

---

## Report Examples

### HTML Report
- Professional visualization
- Summary statistics with charts
- Detailed test results table
- Color-coded pass/fail status
- Execution timeline

### Markdown Report
- GitHub-compatible format
- Table-based results
- Sortable data
- Copy-paste friendly

### JSON Report
- Machine-readable format
- Complete execution data
- Timing information
- Error details
- Suitable for CI/CD integration

---

## Troubleshooting

### Issue 1: Ollama Connection Failed
```
Error: Cannot connect to Ollama at http://localhost:11434
```

**Solution**:
```bash
# Check if Ollama is running
ollama serve

# If not installed, download from https://ollama.ai

# Check connection
curl http://localhost:11434/api/tags
```

### Issue 2: Llama3 Model Not Found
```
Error: Model 'llama3' not found
```

**Solution**:
```bash
# Download Llama3 model
ollama pull llama3

# Verify installation
ollama list
```

### Issue 3: Spec Files Not Found
```
Warning: Spec file not found at specs/main/spec.md
```

**Solution**:
```bash
# Check current directory
pwd

# Navigate to repo root
cd ../..

# Verify files exist
ls -la specs/main/spec.md
```

### Issue 4: pytest Not Found
```
Error: pytest not found
```

**Solution**:
```bash
# Install with pip
pip install pytest

# Verify installation
pytest --version
```

### Issue 5: API Connection Failed
```
Error: Cannot connect to API at http://localhost:8080
```

**Solution**:
```bash
# Start ACMS API
cd ../acms-api
./mvnw spring-boot:run

# Or use Docker
docker-compose up acms-api
```

### Issue 6: Generated Tests Won't Run
```
ImportError: No module named 'requests'
```

**Solution**:
```bash
# Reinstall dependencies
pip install -r requirements.txt

# Or install specific package
pip install requests
```

---

## Best Practices

### 1. **Test Data Management**
- Use `--seed` parameter for reproducible data
- Generate appropriate data volume (5-20 agents)
- Save mock data for later analysis

### 2. **Test Execution**
- Run tests in isolation before full suite
- Use patterns to test specific resources
- Monitor execution time

### 3. **Report Analysis**
- Always review HTML report visually
- Check failed tests for patterns
- Keep historical reports for comparison

### 4. **CI/CD Integration**
```yaml
# Example GitHub Actions
- name: Run API Tests
  run: |
    python run_agent.py full --resource agents
    
- name: Upload Report
  uses: actions/upload-artifact@v2
  with:
    name: test-report
    path: generated_tests/test_report.html
```

---

## Performance Considerations

### Expected Execution Times
- Test Generation: 30-60 seconds (LLM dependent)
- Mock Data Generation: < 1 second
- Test Execution (20 tests): 5-15 seconds
- Report Generation: < 2 seconds

**Total Time**: 1-2 minutes per resource

### Optimization Tips
- Generate tests for one resource at a time
- Use `--mock-agents 3` for quick testing
- Run specific patterns instead of full suite
- Cache generated test files for reuse

---

## Configuration

### Environment Variables (.env)
```bash
API_BASE_URL=http://localhost:8080
API_TIMEOUT=30
AUTH_TOKEN=your_jwt_token_here
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=llama3
```

### Configuration File (config.yaml)
```yaml
api_base_url: "http://localhost:8080"
api_timeout: 30
test_framework: "pytest"
generate_happy_path: true
generate_edge_cases: true
generate_error_scenarios: true
generate_security_tests: true
performance_sla_ms: 200
```

---

## Next Steps

1. ✅ **Setup Complete** - All components installed
2. ⏳ **Run Agent** - Execute `python run_agent.py full`
3. ⏳ **Review Reports** - Check generated reports
4. ⏳ **Fix Issues** - Address any test failures
5. ⏳ **Iterate** - Refine tests as needed

---

## Support & Documentation

- **ANALYSIS.md** - Detailed analysis of all components
- **README.md** - Quick reference
- **API Reference** - Endpoint documentation
- **Spec Files** - Business requirements

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-12-02 | Initial release with full feature set |

---

**End of Implementation Guide**
