# AI-Powered API Testing Agent

Automated functional test generation using Ollama + Llama3.

## 🚀 Quick Start

```bash
# 1. Install Ollama (https://ollama.ai/download)
# 2. Download Llama3
ollama pull llama3

# 3. Start Ollama (keep running)
ollama serve

# 4. Generate tests (new terminal)
python generate_tests_ai.py --resource agents

# 5. Run tests
cd generated_tests
pytest test_agents_ai.py -v
```

## 📚 Documentation

See `docs/` folder:
- **QUICK_START.md** - 5-minute setup
- **REFERENCE.md** - All commands
- **AI_AGENT_GUIDE.md** - Detailed guide
- **FUNCTIONAL_TESTING_GUIDE.md** - Testing guide

## 📁 Project Structure

```
├── README.md                  # This file
├── requirements.txt           # Dependencies
├── pytest.ini                 # Pytest config
├── setup.py                   # Package setup
├── .env.example               # Environment template
│
├── docs/                      # Documentation
├── src/                       # Source code
├── generated_tests/           # Generated tests
├── examples/                  # Examples
├── templates/                 # Templates
│
├── generate_tests_ai.py       # Main script
├── check_ollama.py            # Setup check
├── verify_setup.py            # Verify environment
├── mock_api_server.py         # Mock server
└── test_agents_mock.py        # Mock tests
```

## ✅ What It Does

- Reads Constitution.md (governance rules)
- Reads Spec.md (requirements)
- Uses AI to understand requirements
- Generates comprehensive pytest tests
- Covers all user stories and acceptance scenarios

## 🎯 Main Commands

```bash
python verify_setup.py                              # Verify setup
python check_ollama.py                              # Check Ollama
python generate_tests_ai.py --resource agents       # Generate tests
cd generated_tests && pytest test_agents_ai.py -v   # Run tests
python mock_api_server.py                           # Start mock server
```

## 📖 For More Information

See `docs/QUICK_START.md` or `docs/REFERENCE.md`

**Status:** Ready to Use ✅
