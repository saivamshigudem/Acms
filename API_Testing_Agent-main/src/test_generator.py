"""
Test Specification Generator Module

Generates comprehensive test specifications from OpenAPI specs and user stories.
"""

import logging
from typing import Dict, Any, List, Optional
from dataclasses import dataclass, field

from .spec_parser import SpecParser
from .story_parser import StoryParser
from .utils.formatters import format_test_name, format_endpoint_path, format_status_code

logger = logging.getLogger(__name__)


@dataclass
class TestCase:
    """Represents a single test case."""
    
    id: str
    name: str
    endpoint: str
    method: str
    description: str
    scenario_type: str  # happy_path, edge_case, error_scenario, security, integration, performance
    input_data: Dict[str, Any] = field(default_factory=dict)
    expected_status: int = 200
    expected_response: Dict[str, Any] = field(default_factory=dict)
    assertions: List[str] = field(default_factory=list)
    tags: List[str] = field(default_factory=list)
    story_id: Optional[str] = None
    story_title: Optional[str] = None


class TestGenerator:
    """
    Generates test specifications from OpenAPI specs and user stories.
    """
    
    def __init__(self, spec_parser: SpecParser, story_parser: Optional[StoryParser] = None):
        """
        Initialize the test generator.
        
        Args:
            spec_parser: Initialized SpecParser instance
            story_parser: Optional initialized StoryParser instance
        """
        self.spec_parser = spec_parser
        self.story_parser = story_parser
        self.test_cases: List[TestCase] = []
    
    def generate_all_tests(self) -> List[TestCase]:
        """
        Generate all test cases.
        
        Returns:
            List of generated test cases
        """
        self.test_cases = []
        
        endpoints = self.spec_parser.get_endpoints()
        
        for endpoint in endpoints:
            # Generate happy path tests
            if self._should_generate("happy_path"):
                self.test_cases.extend(self._generate_happy_path_tests(endpoint))
            
            # Generate edge case tests
            if self._should_generate("edge_case"):
                self.test_cases.extend(self._generate_edge_case_tests(endpoint))
            
            # Generate error scenario tests
            if self._should_generate("error_scenario"):
                self.test_cases.extend(self._generate_error_scenario_tests(endpoint))
            
            # Generate security tests
            if self._should_generate("security"):
                self.test_cases.extend(self._generate_security_tests(endpoint))
        
        logger.info(f"Generated {len(self.test_cases)} test cases")
        return self.test_cases
    
    def _should_generate(self, test_type: str) -> bool:
        """Check if test type should be generated."""
        return True  # Can be extended with configuration
    
    def _generate_happy_path_tests(self, endpoint: Dict[str, Any]) -> List[TestCase]:
        """Generate happy path tests for an endpoint."""
        tests = []
        
        path = endpoint["path"]
        method = endpoint["method"]
        
        # Determine expected status code
        responses = endpoint.get("responses", {})
        expected_status = 200
        if method == "POST":
            expected_status = 201
        elif method == "DELETE":
            expected_status = 204
        
        # Get first successful response status
        for status_str in responses.keys():
            try:
                status = int(status_str)
                if 200 <= status < 300:
                    expected_status = status
                    break
            except ValueError:
                pass
        
        test = TestCase(
            id=f"test_{len(self.test_cases) + 1}",
            name=format_test_name(endpoint.get("summary", path), "happy_path"),
            endpoint=path,
            method=method,
            description=f"Happy path test for {format_endpoint_path(path, method)}",
            scenario_type="happy_path",
            expected_status=expected_status,
            assertions=[
                f"Status code is {expected_status}",
                "Response body is not empty",
                "Response contains expected fields",
            ],
            tags=["happy_path"] + endpoint.get("tags", []),
        )
        
        tests.append(test)
        return tests
    
    def _generate_edge_case_tests(self, endpoint: Dict[str, Any]) -> List[TestCase]:
        """Generate edge case tests for an endpoint."""
        tests = []
        
        path = endpoint["path"]
        method = endpoint["method"]
        
        # Generate tests for boundary conditions
        edge_cases = [
            {
                "name": "with_empty_collection",
                "description": "Test with empty collection response",
                "expected_status": 200,
            },
            {
                "name": "with_null_values",
                "description": "Test with null values in response",
                "expected_status": 200,
            },
            {
                "name": "with_large_dataset",
                "description": "Test with large dataset",
                "expected_status": 200,
            },
        ]
        
        for case in edge_cases:
            test = TestCase(
                id=f"test_{len(self.test_cases) + 1}",
                name=format_test_name(endpoint.get("summary", path), case["name"]),
                endpoint=path,
                method=method,
                description=case["description"],
                scenario_type="edge_case",
                expected_status=case["expected_status"],
                assertions=[
                    f"Status code is {case['expected_status']}",
                    "Response handles edge case correctly",
                ],
                tags=["edge_case"] + endpoint.get("tags", []),
            )
            tests.append(test)
        
        return tests
    
    def _generate_error_scenario_tests(self, endpoint: Dict[str, Any]) -> List[TestCase]:
        """Generate error scenario tests for an endpoint."""
        tests = []
        
        path = endpoint["path"]
        method = endpoint["method"]
        
        # Common error scenarios
        error_scenarios = [
            {"status": 400, "name": "invalid_input", "description": "Invalid input data"},
            {"status": 401, "name": "missing_auth", "description": "Missing authentication"},
            {"status": 403, "name": "insufficient_permissions", "description": "Insufficient permissions"},
            {"status": 404, "name": "not_found", "description": "Resource not found"},
            {"status": 409, "name": "conflict", "description": "Conflict/version mismatch"},
            {"status": 500, "name": "server_error", "description": "Server error"},
        ]
        
        # Check which errors are documented in the spec
        responses = endpoint.get("responses", {})
        
        for scenario in error_scenarios:
            status = scenario["status"]
            
            # Only generate if documented or for common errors
            if str(status) in responses or status in [400, 401, 403, 404]:
                test = TestCase(
                    id=f"test_{len(self.test_cases) + 1}",
                    name=format_test_name(endpoint.get("summary", path), scenario["name"]),
                    endpoint=path,
                    method=method,
                    description=scenario["description"],
                    scenario_type="error_scenario",
                    expected_status=status,
                    assertions=[
                        f"Status code is {status}",
                        "Error message is clear",
                        "Error response follows standard format",
                    ],
                    tags=["error_scenario"] + endpoint.get("tags", []),
                )
                tests.append(test)
        
        return tests
    
    def _generate_security_tests(self, endpoint: Dict[str, Any]) -> List[TestCase]:
        """Generate security tests for an endpoint."""
        tests = []
        
        path = endpoint["path"]
        method = endpoint["method"]
        
        # Check if endpoint requires authentication
        security = endpoint.get("security", [])
        
        if security or True:  # Generate security tests for all endpoints
            security_tests = [
                {
                    "name": "missing_auth_token",
                    "description": "Test without authentication token",
                    "expected_status": 401,
                },
                {
                    "name": "invalid_auth_token",
                    "description": "Test with invalid authentication token",
                    "expected_status": 401,
                },
                {
                    "name": "expired_auth_token",
                    "description": "Test with expired authentication token",
                    "expected_status": 401,
                },
                {
                    "name": "insufficient_permissions",
                    "description": "Test with insufficient permissions",
                    "expected_status": 403,
                },
            ]
            
            for test_def in security_tests:
                test = TestCase(
                    id=f"test_{len(self.test_cases) + 1}",
                    name=format_test_name(endpoint.get("summary", path), test_def["name"]),
                    endpoint=path,
                    method=method,
                    description=test_def["description"],
                    scenario_type="security",
                    expected_status=test_def["expected_status"],
                    assertions=[
                        f"Status code is {test_def['expected_status']}",
                        "Security validation is enforced",
                    ],
                    tags=["security"] + endpoint.get("tags", []),
                )
                tests.append(test)
        
        return tests
    
    def get_test_cases(self) -> List[TestCase]:
        """Get all generated test cases."""
        return self.test_cases
    
    def get_test_cases_by_type(self, scenario_type: str) -> List[TestCase]:
        """Get test cases by scenario type."""
        return [t for t in self.test_cases if t.scenario_type == scenario_type]
    
    def get_test_cases_by_endpoint(self, path: str, method: str) -> List[TestCase]:
        """Get test cases for a specific endpoint."""
        return [
            t for t in self.test_cases 
            if t.endpoint == path and t.method.upper() == method.upper()
        ]
    
    def __repr__(self) -> str:
        """String representation of generator."""
        return f"TestGenerator(test_cases={len(self.test_cases)})"
