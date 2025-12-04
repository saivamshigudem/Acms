#!/usr/bin/env python3
"""
Setup Verification Script for API Testing Agent

This script verifies that all necessary components are installed and configured
correctly for running the API Testing Agent tests.

Usage:
    python verify_setup.py
"""

import sys
import os
import subprocess
from pathlib import Path


class Colors:
    """ANSI color codes for terminal output."""
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'


def print_header(text):
    """Print a header."""
    print(f"\n{Colors.BOLD}{Colors.BLUE}{'=' * 60}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.BLUE}{text}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.BLUE}{'=' * 60}{Colors.RESET}\n")


def print_success(text):
    """Print success message."""
    print(f"{Colors.GREEN}✅ {text}{Colors.RESET}")


def print_error(text):
    """Print error message."""
    print(f"{Colors.RED}❌ {text}{Colors.RESET}")


def print_warning(text):
    """Print warning message."""
    print(f"{Colors.YELLOW}⚠️  {text}{Colors.RESET}")


def print_info(text):
    """Print info message."""
    print(f"{Colors.BLUE}ℹ️  {text}{Colors.RESET}")


def check_python_version():
    """Check Python version."""
    print_header("1. Checking Python Version")
    
    version = sys.version_info
    version_str = f"{version.major}.{version.minor}.{version.micro}"
    
    if version.major >= 3 and version.minor >= 11:
        print_success(f"Python {version_str} installed")
        return True
    else:
        print_error(f"Python {version_str} found, but 3.11+ required")
        return False


def check_required_packages():
    """Check if required packages are installed."""
    print_header("2. Checking Required Packages")
    
    required_packages = {
        'pytest': 'pytest',
        'requests': 'requests',
        'flask': 'flask',
        'dotenv': 'python-dotenv',
        'jinja2': 'jinja2',
        'yaml': 'pyyaml',
    }
    
    all_installed = True
    
    for package_name, pip_name in required_packages.items():
        try:
            __import__(package_name)
            print_success(f"{pip_name} installed")
        except ImportError:
            print_error(f"{pip_name} NOT installed")
            all_installed = False
    
    if not all_installed:
        print_warning("Run: pip install -r requirements.txt")
    
    return all_installed


def check_file_structure():
    """Check if required files exist."""
    print_header("3. Checking File Structure")
    
    required_files = {
        'README.md': 'Project README',
        'QUICK_START.md': 'Quick start guide',
        'GETTING_STARTED.md': 'Detailed setup guide',
        'requirements.txt': 'Python dependencies',
        'pytest.ini': 'Pytest configuration',
        '.env.example': 'Environment template',
        'mock_api_server.py': 'Mock API server',
        'test_agents_mock.py': 'Mock tests',
        'generated_tests/conftest.py': 'Pytest fixtures',
        'generated_tests/test_agents_functional.py': 'Functional tests',
        'generated_tests/.env': 'Test environment config',
    }
    
    all_exist = True
    
    for file_path, description in required_files.items():
        if Path(file_path).exists():
            print_success(f"{description}: {file_path}")
        else:
            print_error(f"{description} NOT FOUND: {file_path}")
            all_exist = False
    
    return all_exist


def check_environment_config():
    """Check environment configuration."""
    print_header("4. Checking Environment Configuration")
    
    env_file = Path("generated_tests/.env")
    
    if not env_file.exists():
        print_error(".env file not found in generated_tests/")
        return False
    
    print_success(".env file found")
    
    # Read and check configuration
    with open(env_file, 'r') as f:
        content = f.read()
    
    required_vars = [
        'API_BASE_URL',
        'AUTH_TOKEN',
        'AUTH_HEADER',
    ]
    
    all_configured = True
    
    for var in required_vars:
        if var in content:
            print_success(f"Configuration variable: {var}")
        else:
            print_error(f"Configuration variable NOT FOUND: {var}")
            all_configured = False
    
    return all_configured


def check_mock_server():
    """Check if mock server can be imported."""
    print_header("5. Checking Mock Server")
    
    try:
        # Try to import Flask
        import flask
        print_success("Flask is installed (required for mock server)")
        
        # Check mock_api_server.py
        if Path("mock_api_server.py").exists():
            print_success("mock_api_server.py exists")
            return True
        else:
            print_error("mock_api_server.py NOT FOUND")
            return False
    except ImportError:
        print_error("Flask NOT installed (required for mock server)")
        return False


def check_test_files():
    """Check if test files are valid Python."""
    print_header("6. Checking Test Files")
    
    test_files = [
        'test_agents_mock.py',
        'generated_tests/test_agents_functional.py',
        'generated_tests/conftest.py',
    ]
    
    all_valid = True
    
    for test_file in test_files:
        if not Path(test_file).exists():
            print_error(f"Test file NOT FOUND: {test_file}")
            all_valid = False
            continue
        
        try:
            with open(test_file, 'r', encoding='utf-8') as f:
                compile(f.read(), test_file, 'exec')
            print_success(f"Test file is valid Python: {test_file}")
        except SyntaxError as e:
            print_error(f"Test file has syntax error: {test_file}")
            print_error(f"  Error: {e}")
            all_valid = False
        except UnicodeDecodeError as e:
            print_error(f"Test file has encoding error: {test_file}")
            print_error(f"  Error: {e}")
            all_valid = False
    
    return all_valid


def run_mock_tests():
    """Run mock tests to verify setup."""
    print_header("7. Running Mock Tests")
    
    try:
        print_info("Running: python test_agents_mock.py")
        result = subprocess.run(
            [sys.executable, 'test_agents_mock.py'],
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if result.returncode == 0:
            print_success("Mock tests passed!")
            # Print last few lines of output
            lines = result.stdout.strip().split('\n')
            for line in lines[-5:]:
                if line.strip():
                    print_info(line)
            return True
        else:
            print_error("Mock tests failed!")
            print_error(result.stdout)
            print_error(result.stderr)
            return False
    except subprocess.TimeoutExpired:
        print_error("Mock tests timed out")
        return False
    except Exception as e:
        print_error(f"Error running mock tests: {e}")
        return False


def print_summary(results):
    """Print verification summary."""
    print_header("Verification Summary")
    
    checks = [
        ("Python Version", results[0]),
        ("Required Packages", results[1]),
        ("File Structure", results[2]),
        ("Environment Config", results[3]),
        ("Mock Server", results[4]),
        ("Test Files", results[5]),
        ("Mock Tests", results[6]),
    ]
    
    passed = sum(1 for _, result in checks if result)
    total = len(checks)
    
    for check_name, result in checks:
        status = f"{Colors.GREEN}✅ PASS{Colors.RESET}" if result else f"{Colors.RED}❌ FAIL{Colors.RESET}"
        print(f"{check_name}: {status}")
    
    print(f"\n{Colors.BOLD}Results: {passed}/{total} checks passed{Colors.RESET}")
    
    if passed == total:
        print_success("All checks passed! Your setup is ready.")
        print_info("Next steps:")
        print_info("  1. Run mock tests: python test_agents_mock.py")
        print_info("  2. Start mock server: python mock_api_server.py")
        print_info("  3. Run functional tests: cd generated_tests && pytest -v")
        return True
    else:
        print_error(f"{total - passed} check(s) failed. Please review the errors above.")
        return False


def main():
    """Main verification function."""
    print(f"\n{Colors.BOLD}{Colors.BLUE}")
    print("╔════════════════════════════════════════════════════════════╗")
    print("║  API Testing Agent - Setup Verification Script            ║")
    print("╚════════════════════════════════════════════════════════════╝")
    print(f"{Colors.RESET}")
    
    # Run all checks
    results = [
        check_python_version(),
        check_required_packages(),
        check_file_structure(),
        check_environment_config(),
        check_mock_server(),
        check_test_files(),
        run_mock_tests(),
    ]
    
    # Print summary
    success = print_summary(results)
    
    # Exit with appropriate code
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
