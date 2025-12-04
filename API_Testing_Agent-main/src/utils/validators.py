"""
Input Validation Module

Validates OpenAPI specifications, user stories, and other inputs
for the API Testing Agent.
"""

import logging
from typing import Dict, Any, List, Optional
import jsonschema

logger = logging.getLogger(__name__)


def validate_openapi_spec(spec: Dict[str, Any]) -> bool:
    """
    Validate OpenAPI 3.0 specification.
    
    Args:
        spec: OpenAPI specification dictionary
        
    Returns:
        True if valid
        
    Raises:
        ValueError: If specification is invalid
    """
    errors = []
    
    # Check required top-level fields
    required_fields = ["openapi", "info", "paths"]
    for field in required_fields:
        if field not in spec:
            errors.append(f"Missing required field: {field}")
    
    # Validate OpenAPI version
    if "openapi" in spec:
        version = spec["openapi"]
        if not version.startswith("3."):
            errors.append(f"Only OpenAPI 3.x is supported, got {version}")
    
    # Validate info object
    if "info" in spec:
        info = spec["info"]
        if not isinstance(info, dict):
            errors.append("'info' must be an object")
        else:
            if "title" not in info:
                errors.append("'info.title' is required")
            if "version" not in info:
                errors.append("'info.version' is required")
    
    # Validate paths object
    if "paths" in spec:
        paths = spec["paths"]
        if not isinstance(paths, dict):
            errors.append("'paths' must be an object")
        elif not paths:
            errors.append("'paths' must contain at least one endpoint")
    
    if errors:
        error_msg = f"OpenAPI specification validation failed: {'; '.join(errors)}"
        logger.error(error_msg)
        raise ValueError(error_msg)
    
    logger.info("OpenAPI specification validation passed")
    return True


def validate_user_stories(stories: List[Dict[str, Any]]) -> bool:
    """
    Validate user stories format.
    
    Args:
        stories: List of user story dictionaries
        
    Returns:
        True if valid
        
    Raises:
        ValueError: If user stories are invalid
    """
    errors = []
    
    if not isinstance(stories, list):
        errors.append("User stories must be a list")
    elif not stories:
        errors.append("User stories list must not be empty")
    else:
        for i, story in enumerate(stories):
            if not isinstance(story, dict):
                errors.append(f"User story {i} must be an object")
                continue
            
            # Check required fields
            if "title" not in story:
                errors.append(f"User story {i} missing 'title'")
            
            if "acceptance_criteria" not in story:
                errors.append(f"User story {i} missing 'acceptance_criteria'")
            elif not isinstance(story["acceptance_criteria"], list):
                errors.append(f"User story {i} 'acceptance_criteria' must be a list")
            elif not story["acceptance_criteria"]:
                errors.append(f"User story {i} 'acceptance_criteria' must not be empty")
    
    if errors:
        error_msg = f"User stories validation failed: {'; '.join(errors)}"
        logger.error(error_msg)
        raise ValueError(error_msg)
    
    logger.info("User stories validation passed")
    return True


def validate_endpoint(endpoint: Dict[str, Any], path: str) -> bool:
    """
    Validate an API endpoint definition.
    
    Args:
        endpoint: Endpoint definition dictionary
        path: Endpoint path
        
    Returns:
        True if valid
        
    Raises:
        ValueError: If endpoint is invalid
    """
    errors = []
    
    if not isinstance(endpoint, dict):
        errors.append(f"Endpoint at {path} must be an object")
        raise ValueError(f"Endpoint validation failed: {'; '.join(errors)}")
    
    # Check for at least one HTTP method
    valid_methods = ["get", "post", "put", "delete", "patch", "head", "options"]
    methods = [m for m in valid_methods if m in endpoint]
    
    if not methods:
        errors.append(f"Endpoint {path} must define at least one HTTP method")
    
    # Validate each method
    for method in methods:
        method_def = endpoint[method]
        if not isinstance(method_def, dict):
            errors.append(f"Method {method} at {path} must be an object")
    
    if errors:
        error_msg = f"Endpoint validation failed: {'; '.join(errors)}"
        logger.error(error_msg)
        raise ValueError(error_msg)
    
    return True


def validate_test_case(test_case: Dict[str, Any]) -> bool:
    """
    Validate a test case definition.
    
    Args:
        test_case: Test case dictionary
        
    Returns:
        True if valid
        
    Raises:
        ValueError: If test case is invalid
    """
    errors = []
    
    required_fields = ["name", "endpoint", "method", "expected_status"]
    for field in required_fields:
        if field not in test_case:
            errors.append(f"Test case missing required field: {field}")
    
    # Validate HTTP method
    if "method" in test_case:
        valid_methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
        if test_case["method"].upper() not in valid_methods:
            errors.append(f"Invalid HTTP method: {test_case['method']}")
    
    # Validate status code
    if "expected_status" in test_case:
        status = test_case["expected_status"]
        if not isinstance(status, int) or status < 100 or status >= 600:
            errors.append(f"Invalid HTTP status code: {status}")
    
    if errors:
        error_msg = f"Test case validation failed: {'; '.join(errors)}"
        logger.error(error_msg)
        raise ValueError(error_msg)
    
    return True
