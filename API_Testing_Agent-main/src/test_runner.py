"""
Test Runner and Execution Engine for ACMS API Testing

Executes generated test cases and collects results.
"""

import subprocess
import json
import logging
import re
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple
from dataclasses import dataclass, asdict, field
from enum import Enum
from datetime import datetime

logger = logging.getLogger(__name__)


class TestStatus(str, Enum):
    """Test execution status."""
    PASSED = "PASSED"
    FAILED = "FAILED"
    SKIPPED = "SKIPPED"
    ERROR = "ERROR"


class TestType(str, Enum):
    """Test types."""
    HAPPY_PATH = "HAPPY_PATH"
    EDGE_CASE = "EDGE_CASE"
    ERROR_SCENARIO = "ERROR_SCENARIO"
    SECURITY = "SECURITY"
    INTEGRATION = "INTEGRATION"
    PERFORMANCE = "PERFORMANCE"


@dataclass
class TestResult:
    """Result of a single test execution."""
    test_name: str
    test_file: str
    status: TestStatus
    duration_ms: float
    error_message: Optional[str] = None
    assertions: List[str] = field(default_factory=list)
    tags: List[str] = field(default_factory=list)
    test_type: Optional[TestType] = None
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return {
            "test_name": self.test_name,
            "test_file": self.test_file,
            "status": self.status.value,
            "duration_ms": self.duration_ms,
            "error_message": self.error_message,
            "assertions": self.assertions,
            "tags": self.tags,
            "test_type": self.test_type.value if self.test_type else None
        }


@dataclass
class ExecutionSummary:
    """Summary of test execution."""
    total_tests: int = 0
    passed_tests: int = 0
    failed_tests: int = 0
    skipped_tests: int = 0
    error_tests: int = 0
    total_duration_ms: float = 0.0
    test_results: List[TestResult] = field(default_factory=list)
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    errors: List[str] = field(default_factory=list)
    
    @property
    def success_rate(self) -> float:
        """Calculate success rate percentage."""
        if self.total_tests == 0:
            return 0.0
        return (self.passed_tests / self.total_tests) * 100
    
    @property
    def execution_time_seconds(self) -> float:
        """Get total execution time in seconds."""
        return self.total_duration_ms / 1000
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return {
            "total_tests": self.total_tests,
            "passed_tests": self.passed_tests,
            "failed_tests": self.failed_tests,
            "skipped_tests": self.skipped_tests,
            "error_tests": self.error_tests,
            "total_duration_ms": self.total_duration_ms,
            "success_rate": self.success_rate,
            "execution_time_seconds": self.execution_time_seconds,
            "start_time": self.start_time.isoformat() if self.start_time else None,
            "end_time": self.end_time.isoformat() if self.end_time else None,
            "test_results": [r.to_dict() for r in self.test_results],
            "errors": self.errors
        }


class PytestResultParser:
    """Parse pytest output and extract test results."""
    
    def __init__(self, output: str):
        """
        Initialize parser with pytest output.
        
        Args:
            output: Raw pytest output
        """
        # Remove ANSI color codes
        self.output = re.sub(r'\x1b\[[0-9;]*m', '', output)
        self.results: List[TestResult] = []
    
    def parse(self) -> List[TestResult]:
        """
        Parse pytest output and extract results.
        
        Returns:
            List of TestResult objects
        """
        lines = self.output.split('\n')
        
        # Look for test result lines
        # pytest output format: "test_file.py::test_name PASSED|FAILED|SKIPPED|ERROR [duration] [percentage]"
        # Updated to handle test names with underscores, brackets, etc.
        test_pattern = r'(.+?\.py)::([^\s\[]+)\s+(PASSED|FAILED|SKIPPED|ERROR)(?:\s+\[(.+?)\])?(?:\s+\[\s*\d+%\])?'
        
        for line in lines:
            match = re.search(test_pattern, line)
            if match:
                test_file = match.group(1)
                test_name = match.group(2)
                status_str = match.group(3)
                duration_str = match.group(4)
                
                # Parse duration
                duration_ms = 0.0
                if duration_str:
                    try:
                        # Handle different duration formats (e.g., "0.50s", "100ms")
                        if 's' in duration_str:
                            duration_ms = float(duration_str.replace('s', '')) * 1000
                        elif 'ms' in duration_str:
                            duration_ms = float(duration_str.replace('ms', ''))
                    except ValueError:
                        pass
                
                result = TestResult(
                    test_name=test_name,
                    test_file=test_file,
                    status=TestStatus[status_str],
                    duration_ms=duration_ms
                )
                
                self.results.append(result)
        
        # Parse error messages
        self._parse_error_messages()
        
        return self.results
    
    def _parse_error_messages(self) -> None:
        """Extract error messages for failed tests."""
        lines = self.output.split('\n')
        
        current_test: Optional[TestResult] = None
        error_lines: List[str] = []
        
        for line in lines:
            # Check if this is a test result line
            if '::' in line and any(status in line for status in ['FAILED', 'ERROR']):
                # Save previous error
                if current_test and error_lines:
                    current_test.error_message = '\n'.join(error_lines).strip()
                    error_lines = []
                
                # Find matching result
                for result in self.results:
                    if result.test_name in line and result.status in [TestStatus.FAILED, TestStatus.ERROR]:
                        current_test = result
                        break
            
            elif current_test and line.strip() and not line.startswith('='):
                error_lines.append(line)


class TestRunner:
    """Execute test files and collect results."""
    
    def __init__(self, test_dir: Path = Path("./generated_tests"), api_url: str = "http://localhost:8080"):
        """
        Initialize test runner.
        
        Args:
            test_dir: Directory containing generated tests
            api_url: API base URL for tests
        """
        self.test_dir = test_dir
        self.api_url = api_url
        self.summary = ExecutionSummary()
    
    def run_all_tests(self, test_pattern: Optional[str] = None) -> ExecutionSummary:
        """
        Run all tests in test directory.
        
        Args:
            test_pattern: Optional pattern to filter tests (e.g., "test_agents")
            
        Returns:
            ExecutionSummary with all results
        """
        self.summary = ExecutionSummary(start_time=datetime.now())
        
        # Find test files
        test_files = self._find_test_files(test_pattern)
        
        if not test_files:
            self.summary.errors.append(f"No test files found in {self.test_dir}")
            self.summary.end_time = datetime.now()
            return self.summary
        
        logger.info(f"Found {len(test_files)} test file(s)")
        
        # Run pytest
        try:
            cmd = self._build_pytest_command(test_files)
            logger.info(f"Running command: {' '.join(cmd)}")
            
            output = self._run_command(cmd)
            
            # Parse results
            parser = PytestResultParser(output)
            self.summary.test_results = parser.parse()
            
            # Update summary
            self._update_summary()
            
            logger.info(f"Test execution completed: {self.summary.passed_tests} passed, {self.summary.failed_tests} failed")
            
        except Exception as e:
            logger.error(f"Error running tests: {e}")
            self.summary.errors.append(str(e))
        
        self.summary.end_time = datetime.now()
        return self.summary
    
    def run_test_file(self, test_file: str) -> ExecutionSummary:
        """
        Run a specific test file.
        
        Args:
            test_file: Path to test file
            
        Returns:
            ExecutionSummary with results
        """
        self.summary = ExecutionSummary(start_time=datetime.now())
        
        test_path = self.test_dir / test_file
        
        if not test_path.exists():
            self.summary.errors.append(f"Test file not found: {test_path}")
            self.summary.end_time = datetime.now()
            return self.summary
        
        try:
            cmd = self._build_pytest_command([test_path])
            output = self._run_command(cmd)
            
            # Parse results
            parser = PytestResultParser(output)
            self.summary.test_results = parser.parse()
            
            # Update summary
            self._update_summary()
            
        except Exception as e:
            logger.error(f"Error running test file {test_file}: {e}")
            self.summary.errors.append(str(e))
        
        self.summary.end_time = datetime.now()
        return self.summary
    
    def run_test_by_pattern(self, pattern: str) -> ExecutionSummary:
        """
        Run tests matching a pattern.
        
        Args:
            pattern: Test pattern (e.g., "test_agents" or "::test_create")
            
        Returns:
            ExecutionSummary with results
        """
        self.summary = ExecutionSummary(start_time=datetime.now())
        
        try:
            cmd = [
                "pytest",
                str(self.test_dir),
                "-k", pattern,
                "-v",
                "--tb=short",
                f"--timeout=300"
            ]
            
            # Set environment variables for API
            import os
            env = os.environ.copy()
            env["API_URL"] = self.api_url
            
            output = self._run_command(cmd, env=env)
            
            # Parse results
            parser = PytestResultParser(output)
            self.summary.test_results = parser.parse()
            
            # Update summary
            self._update_summary()
            
        except Exception as e:
            logger.error(f"Error running tests with pattern {pattern}: {e}")
            self.summary.errors.append(str(e))
        
        self.summary.end_time = datetime.now()
        return self.summary
    
    def _find_test_files(self, pattern: Optional[str] = None) -> List[Path]:
        """Find test files in test directory."""
        if not self.test_dir.exists():
            return []
        
        # Look for test files recursively
        files = list(self.test_dir.glob("**/test_*.py"))
        
        if pattern:
            files = [f for f in files if pattern in f.name]
        
        return sorted(files)
    
    def _build_pytest_command(self, test_files: List[Path]) -> List[str]:
        """Build pytest command."""
        cmd = [
            "pytest",
            "-v",
            "--tb=short",
            "--timeout=300",
            "-ra"  # Show all summary info
        ]
        
        # Add test files
        for test_file in test_files:
            cmd.append(str(test_file))
        
        return cmd
    
    def _run_command(self, cmd: List[str], env: Optional[Dict[str, str]] = None) -> str:
        """
        Run a command and return output.
        
        Args:
            cmd: Command to run
            env: Optional environment variables
            
        Returns:
            Command output
        """
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=600,
            env=env
        )
        
        return result.stdout + result.stderr
    
    def _update_summary(self) -> None:
        """Update execution summary from results."""
        self.summary.total_tests = len(self.summary.test_results)
        self.summary.passed_tests = sum(1 for r in self.summary.test_results if r.status == TestStatus.PASSED)
        self.summary.failed_tests = sum(1 for r in self.summary.test_results if r.status == TestStatus.FAILED)
        self.summary.skipped_tests = sum(1 for r in self.summary.test_results if r.status == TestStatus.SKIPPED)
        self.summary.error_tests = sum(1 for r in self.summary.test_results if r.status == TestStatus.ERROR)
        self.summary.total_duration_ms = sum(r.duration_ms for r in self.summary.test_results)


def main():
    """Demonstrate test runner usage."""
    import sys
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Create runner
    runner = TestRunner(
        test_dir=Path("./generated_tests"),
        api_url="http://localhost:8080"
    )
    
    # Run tests
    print("Running all tests...")
    summary = runner.run_all_tests()
    
    # Print summary
    print("\n" + "="*60)
    print("TEST EXECUTION SUMMARY")
    print("="*60)
    print(f"Total Tests: {summary.total_tests}")
    print(f"Passed: {summary.passed_tests}")
    print(f"Failed: {summary.failed_tests}")
    print(f"Skipped: {summary.skipped_tests}")
    print(f"Errors: {summary.error_tests}")
    print(f"Success Rate: {summary.success_rate:.1f}%")
    print(f"Total Duration: {summary.execution_time_seconds:.2f}s")
    
    if summary.errors:
        print(f"\nErrors:")
        for error in summary.errors:
            print(f"  - {error}")
    
    # Save summary
    summary_file = Path("test_execution_summary.json")
    with open(summary_file, 'w') as f:
        json.dump(summary.to_dict(), f, indent=2)
    print(f"\n✓ Summary saved to {summary_file}")


if __name__ == "__main__":
    main()
