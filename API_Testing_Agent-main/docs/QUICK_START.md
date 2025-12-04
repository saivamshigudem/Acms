# Quick Start Guide

## 5-Minute Setup

### 1. Install Ollama
Download from: https://ollama.ai/download

### 2. Download Llama3
```bash
ollama pull llama3
```

### 3. Start Ollama
```bash
ollama serve
```

### 4. Verify Setup
```bash
python verify_setup.py
python check_ollama.py
```

### 5. Generate Tests
```bash
python generate_tests_ai.py --resource agents
```

### 6. Run Tests
```bash
cd generated_tests
pytest test_agents_ai.py -v
```

---

## Main Commands

```bash
# Generate tests for different resources
python generate_tests_ai.py --resource agents
python generate_tests_ai.py --resource policies
python generate_tests_ai.py --resource commissions
python generate_tests_ai.py --resource payments

# Run tests
cd generated_tests
pytest -v

# Start mock server
python mock_api_server.py
```

---

For more details, see other documentation files.
