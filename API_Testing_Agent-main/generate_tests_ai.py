#!/usr/bin/env python3
"""
AI-Powered API Test Generator

Generates test cases from Constitution and Specification files using Ollama/Llama3.

Usage:
    python generate_tests_ai.py [--resource agents] [--output generated_tests/test_agents_ai.py]
"""

import sys
import argparse
import logging
from pathlib import Path

# Add src to path
sys.path.insert(0, str(Path(__file__).parent / "src"))

from ollama_agent import OllamaAgent, TestCaseGenerator


def setup_logging():
    """Setup logging configuration."""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )


def main():
    """Main function."""
    parser = argparse.ArgumentParser(
        description="Generate API test cases using AI (Ollama/Llama3)"
    )
    parser.add_argument(
        "--resource",
        default="agents",
        help="API resource to test (default: agents)"
    )
    parser.add_argument(
        "--output",
        default="generated_tests/test_agents_ai.py",
        help="Output file path"
    )
    parser.add_argument(
        "--constitution",
        default="../.specify/memory/constitution.md",
        help="Path to constitution file"
    )
    parser.add_argument(
        "--spec",
        default="../specs/main/spec.md",
        help="Path to specification file"
    )
    parser.add_argument(
        "--ollama-url",
        default="http://localhost:11434",
        help="Ollama server URL"
    )
    parser.add_argument(
        "--model",
        default="llama3",
        help="Model name (default: llama3)"
    )
    
    args = parser.parse_args()
    
    setup_logging()
    logger = logging.getLogger(__name__)
    
    print("\n" + "="*70)
    print("  AI-Powered API Test Generator (Ollama/Llama3)")
    print("="*70 + "\n")
    
    # Initialize agent
    print(f"Initializing Ollama Agent...")
    print(f"  URL: {args.ollama_url}")
    print(f"  Model: {args.model}\n")
    
    agent = OllamaAgent(base_url=args.ollama_url, model=args.model)
    
    # Check connection
    print("Checking Ollama connection...")
    if not agent.check_connection():
        print("\n❌ ERROR: Cannot connect to Ollama")
        print("\nMake sure Ollama is running:")
        print("  1. Open a terminal")
        print("  2. Run: ollama serve")
        print("  3. Keep it running in the background")
        sys.exit(1)
    
    print("✓ Connected to Ollama\n")
    
    # Check model
    print(f"Checking if '{args.model}' model is available...")
    if not agent.check_model_available():
        print(f"\n❌ ERROR: Model '{args.model}' not found")
        print(f"\nInstall it with:")
        print(f"  ollama pull {args.model}")
        sys.exit(1)
    
    print(f"✓ Model '{args.model}' available\n")
    
    # Check input files
    print("Checking input files...")
    if not Path(args.constitution).exists():
        print(f"❌ ERROR: Constitution file not found: {args.constitution}")
        sys.exit(1)
    print(f"✓ Constitution: {args.constitution}")
    
    if not Path(args.spec).exists():
        print(f"❌ ERROR: Specification file not found: {args.spec}")
        sys.exit(1)
    print(f"✓ Specification: {args.spec}\n")
    
    # Generate test cases
    print(f"Generating test cases for '{args.resource}' resource...")
    print("(This may take a few minutes - Llama3 is thinking...)\n")
    
    generator = TestCaseGenerator(agent)
    test_code = generator.generate_test_cases_chat(
        args.constitution,
        args.spec,
        resource=args.resource
    )
    
    if not test_code or len(test_code.strip()) < 100:
        print("\n❌ ERROR: Failed to generate test cases")
        print("Possible reasons:")
        print("  1. Ollama server not responding")
        print("  2. Model not fully loaded")
        print("  3. Network timeout")
        print("\nTry again or check Ollama logs")
        sys.exit(1)
    
    # Create output directory
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    # Save test code
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(test_code)
    
    print("\n" + "="*70)
    print("✓ Test cases generated successfully!")
    print("="*70)
    print(f"\nOutput file: {output_path}")
    print(f"Lines of code: {len(test_code.splitlines())}")
    print(f"File size: {len(test_code)} bytes")
    
    # Show preview
    lines = test_code.splitlines()
    print(f"\nPreview (first 30 lines):")
    print("-" * 70)
    for i, line in enumerate(lines[:30], 1):
        print(f"{i:3d}: {line}")
    if len(lines) > 30:
        print(f"... ({len(lines) - 30} more lines)")
    print("-" * 70)
    
    # Next steps
    print(f"\nNext steps:")
    print(f"1. Review the generated test file:")
    print(f"   cat {output_path}")
    print(f"\n2. Run the tests:")
    print(f"   cd generated_tests")
    print(f"   pytest {output_path.name} -v")
    print(f"\n3. If tests fail, review and fix them")
    print(f"4. Commit to version control")
    
    print("\n" + "="*70 + "\n")


if __name__ == "__main__":
    main()
