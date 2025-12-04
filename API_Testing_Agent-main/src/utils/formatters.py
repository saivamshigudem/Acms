"""
Output Formatting Module

Provides formatting utilities for test names, endpoints, and other output.
"""

import logging
import re
from typing import Optional

logger = logging.getLogger(__name__)


def format_test_name(base_name: str, scenario: str) -> str:
    """
    Format a test name following Python naming conventions.
    
    Args:
        base_name: Base name for the test
        scenario: Test scenario description
        
    Returns:
        Formatted test name (snake_case)
        
    Example:
        >>> format_test_name("Create Agent", "Happy Path")
        "test_create_agent_happy_path"
    """
    # Combine base name and scenario
    full_name = f"{base_name}_{scenario}"
    
    # Convert to lowercase
    name = full_name.lower()
    
    # Replace spaces and special characters with underscores
    name = re.sub(r'[^a-z0-9]+', '_', name)
    
    # Remove leading/trailing underscores
    name = name.strip('_')
    
    # Ensure it starts with 'test_'
    if not name.startswith('test_'):
        name = f"test_{name}"
    
    return name


def format_endpoint_path(path: str, method: str) -> str:
    """
    Format endpoint path with method.
    
    Args:
        path: Endpoint path
        method: HTTP method
        
    Returns:
        Formatted endpoint description
        
    Example:
        >>> format_endpoint_path("/agents/{id}", "GET")
        "GET /agents/{id}"
    """
    method = method.upper()
    return f"{method} {path}"


def format_status_code(code: int) -> str:
    """
    Format HTTP status code with description.
    
    Args:
        code: HTTP status code
        
    Returns:
        Formatted status code description
        
    Example:
        >>> format_status_code(201)
        "201 Created"
    """
    status_descriptions = {
        200: "OK",
        201: "Created",
        204: "No Content",
        400: "Bad Request",
        401: "Unauthorized",
        403: "Forbidden",
        404: "Not Found",
        409: "Conflict",
        500: "Internal Server Error",
        502: "Bad Gateway",
        503: "Service Unavailable",
    }
    
    description = status_descriptions.get(code, "Unknown")
    return f"{code} {description}"


def format_parameter_name(param_name: str) -> str:
    """
    Format parameter name for display.
    
    Args:
        param_name: Parameter name
        
    Returns:
        Formatted parameter name
        
    Example:
        >>> format_parameter_name("commission_tier")
        "Commission Tier"
    """
    # Replace underscores with spaces
    name = param_name.replace('_', ' ')
    
    # Capitalize each word
    name = ' '.join(word.capitalize() for word in name.split())
    
    return name


def format_test_description(endpoint: str, method: str, scenario: str) -> str:
    """
    Format a complete test description.
    
    Args:
        endpoint: API endpoint path
        method: HTTP method
        scenario: Test scenario
        
    Returns:
        Formatted test description
        
    Example:
        >>> format_test_description("/agents", "POST", "Happy Path")
        "POST /agents - Happy Path"
    """
    return f"{method.upper()} {endpoint} - {scenario}"


def format_assertion_message(assertion: str, expected: str, actual: str) -> str:
    """
    Format assertion failure message.
    
    Args:
        assertion: Assertion type
        expected: Expected value
        actual: Actual value
        
    Returns:
        Formatted assertion message
        
    Example:
        >>> format_assertion_message("Status Code", "201", "400")
        "Status Code: expected 201 but got 400"
    """
    return f"{assertion}: expected {expected} but got {actual}"


def format_json_example(data: dict, indent: int = 2) -> str:
    """
    Format JSON data for display.
    
    Args:
        data: Dictionary to format
        indent: Indentation level
        
    Returns:
        Formatted JSON string
    """
    import json
    return json.dumps(data, indent=indent, sort_keys=True)


def format_markdown_table(headers: list, rows: list) -> str:
    """
    Format data as Markdown table.
    
    Args:
        headers: Column headers
        rows: List of row data (each row is a list)
        
    Returns:
        Markdown table string
        
    Example:
        >>> headers = ["Method", "Endpoint", "Status"]
        >>> rows = [["GET", "/agents", "200"], ["POST", "/agents", "201"]]
        >>> print(format_markdown_table(headers, rows))
        | Method | Endpoint | Status |
        |--------|----------|--------|
        | GET    | /agents  | 200    |
        | POST   | /agents  | 201    |
    """
    # Create header row
    header_row = "| " + " | ".join(headers) + " |"
    
    # Create separator row
    separator_row = "|" + "|".join(["-" * (len(h) + 2) for h in headers]) + "|"
    
    # Create data rows
    data_rows = []
    for row in rows:
        data_rows.append("| " + " | ".join(str(cell) for cell in row) + " |")
    
    # Combine all rows
    table = "\n".join([header_row, separator_row] + data_rows)
    
    return table


def format_code_block(code: str, language: str = "python") -> str:
    """
    Format code as Markdown code block.
    
    Args:
        code: Code content
        language: Programming language
        
    Returns:
        Markdown code block
    """
    return f"```{language}\n{code}\n```"
