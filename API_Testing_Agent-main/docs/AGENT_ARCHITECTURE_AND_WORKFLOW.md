# ACMS API Testing Agent - Architecture and Workflow

## Where is Your Agent?

Your main agent is located at: `run_agent.py`

This is the central orchestrator that controls all testing operations.

## Where are Tests Generated?

Tests are generated in: `generated_tests/` folder

Example file: `generated_tests/test_agents_ai.py`

## What Operations Does the Agent Perform?

1. **Test Generation**: Uses AI (Llama3) to create test cases
2. **Mock Data Creation**: Generates test data for testing
3. **Test Execution**: Runs the generated tests
4. **Report Generation**: Creates HTML, Markdown, and JSON reports

## Where are Results Generated?

Results are saved in multiple formats:
- HTML reports: `output/test_report.html`
- Markdown reports: `output/test_report.md`
- JSON reports: `output/test_report.json`

## Complete Workflow Process

1. **Prerequisites Check**: Verifies Ollama, Llama3, and spec files
2. **AI Test Generation**: Reads specs and creates test code
3. **Test Execution**: Runs tests against API endpoints
4. **Report Generation**: Creates comprehensive test reports

## Key Files and Their Roles

- `run_agent.py`: Main agent controller
- `generate_tests_ai.py`: Standalone test generator
- `generated_tests/test_agents_ai.py`: Generated test cases
- `src/`: Core agent modules and utilities
- `templates/`: Test generation templates
- `examples/`: Sample specification files

## How to Use

```bash
# Check everything is ready
python run_agent.py check

# Generate tests only
python run_agent.py generate

# Run existing tests
python run_agent.py run

# Full workflow (generate + run + report)
python run_agent.py full
```

This provides a complete automated testing solution for your ACMS API.
