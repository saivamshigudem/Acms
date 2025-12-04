"""
Ollama AI Agent for API Test Generation

This module integrates with Ollama/Llama3 to generate test cases
from Constitution and Specification files.
"""

import requests
import json
import logging
from typing import Dict, List, Any, Optional
from pathlib import Path

logger = logging.getLogger(__name__)


class OllamaAgent:
    """AI Agent using Ollama/Llama3 for test generation."""
    
    def __init__(self, base_url: str = "http://localhost:11434", model: str = "llama3"):
        """
        Initialize Ollama Agent.
        
        Args:
            base_url: Ollama server URL
            model: Model name (default: llama3)
        """
        self.base_url = base_url
        self.model = model
        self.api_endpoint = f"{base_url}/api/generate"
        self.chat_endpoint = f"{base_url}/api/chat"
        
    def check_connection(self) -> bool:
        """Check if Ollama is running and accessible."""
        try:
            response = requests.get(f"{self.base_url}/api/tags", timeout=5)
            return response.status_code == 200
        except requests.exceptions.RequestException as e:
            logger.error(f"Cannot connect to Ollama: {e}")
            return False
    
    def check_model_available(self) -> bool:
        """Check if the specified model is available."""
        try:
            response = requests.get(f"{self.base_url}/api/tags", timeout=5)
            if response.status_code == 200:
                models = response.json().get("models", [])
                model_names = [m.get("name", "").split(":")[0] for m in models]
                return self.model in model_names
            return False
        except requests.exceptions.RequestException as e:
            logger.error(f"Error checking models: {e}")
            return False
    
    def generate_text(self, prompt: str, stream: bool = False) -> str:
        """
        Generate text using Ollama.
        
        Args:
            prompt: Input prompt
            stream: Whether to stream response
            
        Returns:
            Generated text
        """
        try:
            payload = {
                "model": self.model,
                "prompt": prompt,
                "stream": stream,
                "options": {
                    "num_predict": 4000,  # Limit output tokens
                    "temperature": 0.7,    # Balanced creativity
                    "top_p": 0.9,
                    "top_k": 40
                }
            }
            
            response = requests.post(self.api_endpoint, json=payload, timeout=300)
            
            if response.status_code != 200:
                logger.error(f"Ollama error: {response.text}")
                return ""
            
            if stream:
                full_response = ""
                for line in response.iter_lines():
                    if line:
                        data = json.loads(line)
                        full_response += data.get("response", "")
                return full_response
            else:
                data = response.json()
                return data.get("response", "")
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Request error: {e}")
            return ""
        except json.JSONDecodeError as e:
            logger.error(f"JSON decode error: {e}")
            return ""
    
    def chat(self, messages: List[Dict[str, str]]) -> str:
        """
        Chat with the model.
        
        Args:
            messages: List of message dicts with 'role' and 'content'
            
        Returns:
            Model response
        """
        try:
            payload = {
                "model": self.model,
                "messages": messages,
                "stream": False,
                "options": {
                    "num_predict": 4000,  # Limit output tokens
                    "temperature": 0.7,    # Balanced creativity
                    "top_p": 0.9,
                    "top_k": 40
                }
            }
            
            response = requests.post(self.chat_endpoint, json=payload, timeout=300)
            
            if response.status_code != 200:
                logger.error(f"Ollama error: {response.text}")
                return ""
            
            data = response.json()
            return data.get("message", {}).get("content", "")
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Request error: {e}")
            return ""
        except json.JSONDecodeError as e:
            logger.error(f"JSON decode error: {e}")
            return ""


class TestCaseGenerator:
    """Generate test cases using AI Agent."""
    
    def __init__(self, agent: OllamaAgent):
        """Initialize test case generator."""
        self.agent = agent
        
    def read_file(self, file_path: str) -> str:
        """Read file content."""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            logger.error(f"Error reading file {file_path}: {e}")
            return ""
    
    def generate_test_cases(
        self, 
        constitution_path: str, 
        spec_path: str,
        resource: str = "agents"
    ) -> str:
        """
        Generate test cases from Constitution and Spec.
        
        Args:
            constitution_path: Path to constitution.md
            spec_path: Path to spec.md
            resource: API resource to test (e.g., 'agents', 'policies')
            
        Returns:
            Generated test code
        """
        # Read files
        constitution = self.read_file(constitution_path)
        spec = self.read_file(spec_path)
        
        if not constitution or not spec:
            logger.error("Failed to read constitution or spec files")
            return ""
        
        # Create prompt
        prompt = self._create_prompt(constitution, spec, resource)
        
        # Generate test cases
        logger.info(f"Generating test cases for {resource}...")
        test_code = self.agent.generate_text(prompt, stream=False)
        
        # Clean up markdown code blocks if present
        test_code = self._clean_markdown_blocks(test_code)
        
        return test_code
    
    def _clean_markdown_blocks(self, code: str) -> str:
        """Remove markdown code block markers from generated code."""
        # Remove opening markdown code block
        if code.startswith("```"):
            code = code[3:]
            # Remove language specifier if present (e.g., ```python)
            if code.startswith("python"):
                code = code[6:]
            code = code.lstrip("\n")
        
        # Remove closing markdown code block
        if code.endswith("```"):
            code = code[:-3]
            code = code.rstrip("\n")
        
        return code
    
    def _create_prompt(self, constitution: str, spec: str, resource: str) -> str:
        """Create prompt for test generation."""
        return f"""Generate pytest test cases for '{resource}' API.

CONSTITUTION (key points):
{constitution[:2000]}

SPECIFICATION (key points):
{spec[:2000]}

REQUIREMENTS:
- Use pytest framework
- Include fixtures for API client and auth
- Test all acceptance scenarios
- Include happy path, error cases, edge cases
- Use proper assertions
- Include docstrings
- Generate clean, production-ready code
- Target 15-20 comprehensive test cases

CRITICAL: Generate ONLY raw Python test code. NO markdown code blocks. NO triple backticks. NO explanations.

Start with: import pytest
End with: the last test function

Do NOT include:
- Triple backticks (```)
- Markdown formatting
- Explanations
- Comments about the code

Generate the complete test module now. Only Python code, nothing else:"""
    
    def generate_test_cases_chat(
        self,
        constitution_path: str,
        spec_path: str,
        resource: str = "agents"
    ) -> str:
        """
        Generate test cases using chat interface (better for complex tasks).
        
        Args:
            constitution_path: Path to constitution.md
            spec_path: Path to spec.md
            resource: API resource to test
            
        Returns:
            Generated test code
        """
        # Read files
        constitution = self.read_file(constitution_path)
        spec = self.read_file(spec_path)
        
        if not constitution or not spec:
            logger.error("Failed to read constitution or spec files")
            return ""
        
        # Create messages for chat
        messages = [
            {
                "role": "system",
                "content": "You are an expert API testing engineer. Generate ONLY raw Python pytest code. NO markdown code blocks. NO triple backticks. NO explanations or comments."
            },
            {
                "role": "user",
                "content": f"""Generate pytest test cases for '{resource}' API.

CONSTITUTION (key points):
{constitution[:2000]}

SPECIFICATION (key points):
{spec[:2000]}

Requirements:
- Use pytest with fixtures
- Test all acceptance scenarios
- Include happy path, error cases, edge cases
- Use proper assertions
- Include docstrings
- Generate 15-20 comprehensive tests
- Include security tests
- Use realistic test data

CRITICAL INSTRUCTIONS:
- Generate ONLY raw Python code
- NO markdown code blocks
- NO triple backticks (```)
- NO explanations
- Start with: import pytest
- End with: the last test function

Generate the complete test module now:"""
            }
        ]
        
        logger.info(f"Generating test cases for {resource} using chat...")
        test_code = self.agent.chat(messages)
        
        return test_code


def main():
    """Main function to demonstrate test generation."""
    import sys
    
    # Setup logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Initialize agent
    agent = OllamaAgent(base_url="http://localhost:11434", model="llama3")
    
    # Check connection
    print("Checking Ollama connection...")
    if not agent.check_connection():
        print("ERROR: Cannot connect to Ollama. Make sure Ollama is running:")
        print("  ollama serve")
        sys.exit(1)
    
    print("✓ Connected to Ollama")
    
    # Check model
    print(f"Checking if {agent.model} is available...")
    if not agent.check_model_available():
        print(f"ERROR: Model '{agent.model}' not found. Install it with:")
        print(f"  ollama pull {agent.model}")
        sys.exit(1)
    
    print(f"✓ Model '{agent.model}' available")
    
    # Generate test cases
    generator = TestCaseGenerator(agent)
    
    # Paths
    constitution_path = ".specify/memory/constitution.md"
    spec_path = "specs/main/spec.md"
    
    # Check if files exist
    if not Path(constitution_path).exists():
        print(f"ERROR: Constitution file not found: {constitution_path}")
        sys.exit(1)
    
    if not Path(spec_path).exists():
        print(f"ERROR: Spec file not found: {spec_path}")
        sys.exit(1)
    
    print(f"\nReading files...")
    print(f"  Constitution: {constitution_path}")
    print(f"  Specification: {spec_path}")
    
    # Generate tests for agents
    print("\nGenerating test cases for 'agents' resource...")
    test_code = generator.generate_test_cases_chat(
        constitution_path,
        spec_path,
        resource="agents"
    )
    
    if test_code:
        # Save to file
        output_file = "generated_tests/test_agents_ai_generated.py"
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(test_code)
        
        print(f"\n✓ Test cases generated successfully!")
        print(f"  Output: {output_file}")
        print(f"  Lines: {len(test_code.splitlines())}")
    else:
        print("\nERROR: Failed to generate test cases")
        sys.exit(1)


if __name__ == "__main__":
    main()
