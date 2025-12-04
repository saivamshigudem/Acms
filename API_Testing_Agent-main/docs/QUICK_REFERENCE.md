# ACMS API Testing Agent - Quick Reference

**Last Updated**: December 2, 2025

---

## One-Line Quick Start

```bash
python run_agent.py full --resource agents --mock-agents 5
```

---

## Installation (2 minutes)

```bash
# 1. Prerequisites
pip install -r requirements.txt
ollama pull llama3  # one time only

# 2. Verify
python run_agent.py check
```

---

## Commands

### Check Setup
```bash
python run_agent.py check
```
Validates Ollama, Llama3, spec files, pytest.

### Generate Tests
```bash
python run_agent.py generate --resource agents
```
Creates test_agents_generated.py and mock_data.json.

### Run Tests
```bash
python run_agent.py run
```
Executes tests and generates reports.

### Full Workflow
```bash
python run_agent.py full
```
Generate + Run + Report (complete workflow).

---

## Options

```bash
--resource    agents|policies|commissions|payments|performance
--mock-agents number of agents to generate (default: 5)
--output      output directory (default: ./generated_tests)
--pattern     filter tests by pattern (e.g., "test_agents")
```

---

## Output Files

```
generated_tests/
├── tests/python/
│   └── test_agents_generated.py      Generated test code
├── mock_data.json                    Mock test data
├── test_report.html                  Visual report
├── test_report.md                    Markdown report
├── test_report.json                  Raw data
└── test_execution_summary.json       Summary
```

---

## Reports

### HTML Report
- **Open**: `open generated_tests/test_report.html`
- **Features**: Charts, tables, color coding, interactive

### Markdown Report
- **View**: `cat generated_tests/test_report.md`
- **Features**: GitHub-compatible, sortable tables

### JSON Report
- **View**: `cat generated_tests/test_report.json`
- **Use**: CI/CD integration, data extraction

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Ollama not running | `ollama serve` (new terminal) |
| Llama3 not found | `ollama pull llama3` |
| Spec files not found | `cd` to repo root |
| pytest not found | `pip install pytest` |
| No tests found | Run `generate` first |

---

## Test Coverage

**Per Endpoint**:
- ✅ Happy path (valid data)
- ✅ Error scenarios (400, 401, 403, 404)
- ✅ Edge cases (empty, null, large)
- ✅ Security (auth, permissions)
- ✅ Business logic (calculations)

**Resources**:
- ✅ Agents (CREATE, READ, UPDATE, LIST, DELETE)
- ✅ Policies (CREATE, READ, UPDATE, LIST, DELETE)
- ✅ Commissions (CREATE, READ, UPDATE, LIST)
- ✅ Payments (CREATE, READ, UPDATE, LIST)
- ✅ Performance (SUMMARY)

---

## Validators Included

- ✅ HTTP status codes
- ✅ JSON responses
- ✅ Field presence & types
- ✅ Commission calculations
- ✅ Premium validation
- ✅ Date ranges
- ✅ Email format
- ✅ Security checks
- ✅ API compliance

---

## Environment Variables (.env)

```bash
API_BASE_URL=http://localhost:8080
API_TIMEOUT=30
AUTH_TOKEN=your_token_here
OLLAMA_URL=http://localhost:11434
```

---

## CLI Help

```bash
python run_agent.py --help        Main help
python run_agent.py check --help  Check help
python run_agent.py generate --help  Generate help
python run_agent.py run --help    Run help
python run_agent.py full --help   Full workflow help
```

---

## Example Workflows

### 1. Quick Test (2 minutes)
```bash
python run_agent.py full --resource agents --mock-agents 3
```

### 2. Full Suite (10 minutes)
```bash
for resource in agents policies commissions payments; do
  python run_agent.py generate --resource $resource
  python run_agent.py run --pattern "test_$resource"
done
```

### 3. Specific Tests
```bash
python run_agent.py run --pattern "error_scenario"
python run_agent.py run --pattern "security"
python run_agent.py run --pattern "test_agents"
```

### 4. CI/CD Pipeline
```bash
python run_agent.py check  || exit 1
python run_agent.py full   || exit 1
# Archive reports as artifacts
```

---

## File Structure

```
src/
├── mock_data.py           Mock data generation
├── test_runner.py         Pytest execution
├── validators.py          Response validation
├── report_generator.py    Report creation
└── (existing files)       Config, Ollama, etc.

run_agent.py              Main entry point
ANALYSIS.md               Deep dive
IMPLEMENTATION_GUIDE.md   Setup & usage
PROJECT_SUMMARY.md        Overview
VERIFICATION_REPORT.md    Verification
requirements.txt          Dependencies
```

---

## Performance Targets

| Operation | Time | Status |
|-----------|------|--------|
| Mock data | < 1s | ✅ |
| Test generation | 30-60s | ✅ |
| Test execution | 5-15s | ✅ |
| Report generation | < 2s | ✅ |
| **Total** | 1-2m | ✅ |

---

## Feature Highlights

- ✨ AI-powered test generation (Llama3)
- ✨ Realistic mock data
- ✨ 50-80+ comprehensive tests
- ✨ Multiple report formats
- ✨ Business logic validation
- ✨ Security testing
- ✨ Professional CLI
- ✨ Extensive documentation

---

## Support

- **Analysis**: See ANALYSIS.md
- **Setup**: See IMPLEMENTATION_GUIDE.md
- **Overview**: See PROJECT_SUMMARY.md
- **Verification**: See VERIFICATION_REPORT.md
- **Code Help**: `python -m pydoc src.mock_data`

---

## Next Steps

1. ✅ Setup: `pip install -r requirements.txt`
2. ✅ Check: `python run_agent.py check`
3. ✅ Run: `python run_agent.py full`
4. ✅ Review: Open `generated_tests/test_report.html`
5. ✅ Iterate: Refine tests as needed

---

**Status**: ✅ Ready to Use  
**Version**: 1.0.0  
**Quality**: Production Grade  

