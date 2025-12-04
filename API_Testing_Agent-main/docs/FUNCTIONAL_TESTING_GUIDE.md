# Functional Testing Guide

## What is Functional Testing?

Functional testing verifies that the API works according to the specification.

- Tests **what the API does** (not how it does it)
- Tests **all user stories** from the spec
- Tests **all acceptance scenarios**
- Tests **happy paths**, **error cases**, **edge cases**, and **security**

## Test Categories

### Happy Path Tests
- Normal operations with valid data
- Expected success responses

### Error Scenario Tests
- Invalid input handling
- Missing required fields
- Not found errors (404)
- Unauthorized errors (401)

### Edge Case Tests
- Boundary conditions
- Empty values
- Maximum/minimum values

### Security Tests
- Authentication required
- Invalid tokens
- Authorization checks

### Integration Tests
- Multi-step workflows
- Data consistency across operations

## Running Tests

### Run All Tests
```bash
cd generated_tests
pytest -v
```

### Run Specific Test File
```bash
pytest test_agents_ai.py -v
```

### Run Specific Test Class
```bash
pytest test_agents_ai.py::TestAgentManagement -v
```

### Run by Marker
```bash
pytest -v -m happy_path
pytest -v -m error_case
pytest -v -m edge_case
pytest -v -m security
pytest -v -m integration
```

### Run with Coverage
```bash
pytest -v --cov=. --cov-report=html
```

## Understanding Results

### PASSED
✅ Test passed - API behaves as expected

### FAILED
❌ Test failed - API doesn't behave as expected
- Either the API has a bug
- Or the test is wrong
- Or the spec changed

### ERROR
⚠️ Test error - Infrastructure issue
- API not running
- Network problem
- Configuration issue

## Workflow

1. **Update Specification** - Edit spec.md
2. **Regenerate Tests** - `python generate_tests_ai.py --resource agents`
3. **Review Tests** - Check generated test file
4. **Run Tests** - `pytest test_agents_ai.py -v`
5. **Fix Issues** - Fix API or test code
6. **Commit** - Commit tests to version control

## Test Coverage

Your tests cover:
- ✅ All CRUD operations
- ✅ All acceptance scenarios
- ✅ Error handling
- ✅ Input validation
- ✅ Authentication/Authorization
- ✅ Data persistence
- ✅ Multi-step workflows

## Best Practices

1. **Keep specs updated** - Update when requirements change
2. **Regenerate regularly** - After spec changes
3. **Review generated tests** - Ensure they match expectations
4. **Version control** - Commit tests to git
5. **Run tests regularly** - Before deployment
