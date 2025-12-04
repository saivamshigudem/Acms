# AI Agent Guide

## Overview

The AI Testing Agent uses Ollama + Llama3 to generate functional tests from specifications.

## How It Works

```
Constitution.md + Spec.md
         ↓
generate_tests_ai.py
         ↓
src/ollama_agent.py
         ↓
Ollama Server (localhost:11434)
         ↓
Llama3 Model
         ↓
Generated Test Cases (pytest)
```

## Setup

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

## Usage

### Generate Tests
```bash
python generate_tests_ai.py --resource agents
```

### Options
```bash
--resource RESOURCE          # Resource to test (agents, policies, etc.)
--output FILE               # Output file path
--constitution FILE         # Path to constitution.md
--spec FILE                 # Path to spec.md
--ollama-url URL            # Ollama server URL
--model MODEL               # Model name (default: llama3)
```

## What Gets Generated

For each resource:
- Pytest fixtures (API client, auth, test data)
- 15-20 comprehensive test cases
- Happy path, error, edge case, security, and integration tests
- Production-ready code with docstrings

## Troubleshooting

### "Cannot connect to Ollama"
- Make sure `ollama serve` is running
- Check URL is correct (default: http://localhost:11434)

### "Model not found"
- Run: `ollama pull llama3`

### "Request timeout"
- First run takes 2-5 minutes (model loading)
- Subsequent runs are faster

### "Generated code is empty"
- Check Ollama is running
- Check Llama3 is installed
- Try again

## Performance

- First run: 2-5 minutes
- Subsequent runs: 30 seconds - 2 minutes
- System requirements: 8GB RAM minimum

## Next Steps

1. Generate tests: `python generate_tests_ai.py --resource agents`
2. Review generated code: `cat generated_tests/test_agents_ai.py`
3. Run tests: `cd generated_tests && pytest test_agents_ai.py -v`
4. Fix any issues and rerun
