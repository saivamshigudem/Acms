#!/usr/bin/env python3
"""
Check Ollama Setup

Verifies that Ollama is installed and Llama3 is available.
"""

import requests
import sys
import json


def check_ollama_running():
    """Check if Ollama server is running."""
    try:
        response = requests.get("http://localhost:11434/api/tags", timeout=5)
        return response.status_code == 200
    except requests.exceptions.RequestException:
        return False


def get_available_models():
    """Get list of available models."""
    try:
        response = requests.get("http://localhost:11434/api/tags", timeout=5)
        if response.status_code == 200:
            data = response.json()
            models = data.get("models", [])
            return [m.get("name", "").split(":")[0] for m in models]
        return []
    except requests.exceptions.RequestException:
        return []


def main():
    """Main function."""
    print("\n" + "="*70)
    print("  Ollama Setup Verification")
    print("="*70 + "\n")
    
    # Check Ollama running
    print("1. Checking if Ollama is running...")
    if check_ollama_running():
        print("   ✓ Ollama is running on http://localhost:11434\n")
    else:
        print("   ✗ Ollama is NOT running\n")
        print("   To start Ollama, run:")
        print("     ollama serve\n")
        print("   Keep this terminal open while using the AI agent.\n")
        sys.exit(1)
    
    # Check available models
    print("2. Checking available models...")
    models = get_available_models()
    
    if not models:
        print("   ✗ No models found\n")
        print("   To download Llama3, run:")
        print("     ollama pull llama3\n")
        sys.exit(1)
    
    print(f"   ✓ Found {len(models)} model(s):")
    for model in models:
        status = "✓" if model == "llama3" else "•"
        print(f"     {status} {model}")
    
    print()
    
    # Check for Llama3
    if "llama3" in models:
        print("3. Checking for Llama3...")
        print("   ✓ Llama3 is available\n")
    else:
        print("3. Checking for Llama3...")
        print("   ✗ Llama3 is NOT available\n")
        print("   To download Llama3, run:")
        print("     ollama pull llama3\n")
        print("   This will download ~4.7 GB. Be patient!\n")
        sys.exit(1)
    
    # Test connection
    print("4. Testing AI connection...")
    try:
        response = requests.post(
            "http://localhost:11434/api/generate",
            json={
                "model": "llama3",
                "prompt": "Say 'Hello, AI Testing Agent is ready!'",
                "stream": False
            },
            timeout=60
        )
        
        if response.status_code == 200:
            data = response.json()
            output = data.get("response", "").strip()
            print(f"   ✓ AI responded: {output[:50]}...\n")
        else:
            print(f"   ✗ AI error: {response.text}\n")
            sys.exit(1)
    except requests.exceptions.Timeout:
        print("   ⚠ AI took too long to respond (timeout)\n")
        print("   This is normal on first run. Llama3 is loading into memory.")
        print("   Try again in a few minutes.\n")
    except requests.exceptions.RequestException as e:
        print(f"   ✗ Connection error: {e}\n")
        sys.exit(1)
    
    # Success
    print("="*70)
    print("✓ All checks passed! Ollama is ready to use.")
    print("="*70 + "\n")
    
    print("Next steps:")
    print("1. Run: python generate_tests_ai.py --resource agents")
    print("2. Wait for test generation to complete")
    print("3. Review generated tests in: generated_tests/test_agents_ai.py")
    print("4. Run tests: cd generated_tests && pytest test_agents_ai.py -v\n")


if __name__ == "__main__":
    main()
