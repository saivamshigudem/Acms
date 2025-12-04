"""
Helper Functions Module

Provides utility functions for test generation and data processing.
"""

import logging
import uuid
import re
from typing import Dict, Any, List, Optional
from pathlib import Path

logger = logging.getLogger(__name__)


def generate_test_id(prefix: str = "test") -> str:
    """
    Generate a unique test ID.
    
    Args:
        prefix: Prefix for the test ID
        
    Returns:
        Unique test ID
        
    Example:
        >>> test_id = generate_test_id("agent")
        >>> test_id.startswith("agent_")
        True
    """
    unique_id = str(uuid.uuid4())[:8]
    return f"{prefix}_{unique_id}"


def extract_endpoint_info(path: str, method: str) -> Dict[str, Any]:
    """
    Extract information from endpoint path and method.
    
    Args:
        path: Endpoint path (e.g., "/agents/{id}")
        method: HTTP method (e.g., "GET")
        
    Returns:
        Dictionary with endpoint information
        
    Example:
        >>> info = extract_endpoint_info("/agents/{id}", "GET")
        >>> info["resource"]
        "agents"
        >>> info["has_path_params"]
        True
    """
    method = method.upper()
    
    # Extract resource name from path
    parts = path.strip('/').split('/')
    resource = parts[0] if parts else "unknown"
    
    # Check for path parameters
    path_params = re.findall(r'\{(\w+)\}', path)
    
    # Determine operation type
    operation_type = "unknown"
    if method == "GET":
        operation_type = "retrieve" if path_params else "list"
    elif method == "POST":
        operation_type = "create"
    elif method == "PUT":
        operation_type = "update"
    elif method == "DELETE":
        operation_type = "delete"
    elif method == "PATCH":
        operation_type = "partial_update"
    
    return {
        "path": path,
        "method": method,
        "resource": resource,
        "path_params": path_params,
        "has_path_params": bool(path_params),
        "operation_type": operation_type,
    }


def clean_test_data(data: Dict[str, Any]) -> Dict[str, Any]:
    """
    Clean test data by removing None values and empty strings.
    
    Args:
        data: Test data dictionary
        
    Returns:
        Cleaned data dictionary
    """
    cleaned = {}
    for key, value in data.items():
        if value is not None and value != "":
            if isinstance(value, dict):
                cleaned[key] = clean_test_data(value)
            elif isinstance(value, list):
                cleaned[key] = [
                    clean_test_data(item) if isinstance(item, dict) else item
                    for item in value
                ]
            else:
                cleaned[key] = value
    return cleaned


def merge_dicts(dict1: Dict[str, Any], dict2: Dict[str, Any]) -> Dict[str, Any]:
    """
    Merge two dictionaries recursively.
    
    Args:
        dict1: First dictionary
        dict2: Second dictionary (values override dict1)
        
    Returns:
        Merged dictionary
    """
    result = dict1.copy()
    
    for key, value in dict2.items():
        if key in result and isinstance(result[key], dict) and isinstance(value, dict):
            result[key] = merge_dicts(result[key], value)
        else:
            result[key] = value
    
    return result


def get_file_extension(file_path: Path) -> str:
    """
    Get file extension.
    
    Args:
        file_path: Path to file
        
    Returns:
        File extension (without dot)
    """
    return file_path.suffix.lstrip('.')


def ensure_file_exists(file_path: Path, create_if_missing: bool = True) -> bool:
    """
    Ensure a file exists.
    
    Args:
        file_path: Path to file
        create_if_missing: Create file if it doesn't exist
        
    Returns:
        True if file exists
    """
    if file_path.exists():
        return True
    
    if create_if_missing:
        file_path.parent.mkdir(parents=True, exist_ok=True)
        file_path.touch()
        logger.info(f"Created file: {file_path}")
        return True
    
    return False


def get_relative_path(file_path: Path, base_path: Path) -> Path:
    """
    Get relative path from base path.
    
    Args:
        file_path: File path
        base_path: Base path
        
    Returns:
        Relative path
    """
    try:
        return file_path.relative_to(base_path)
    except ValueError:
        return file_path


def sanitize_filename(filename: str) -> str:
    """
    Sanitize filename by removing invalid characters.
    
    Args:
        filename: Original filename
        
    Returns:
        Sanitized filename
    """
    # Replace invalid characters with underscores
    sanitized = re.sub(r'[^a-zA-Z0-9._-]', '_', filename)
    
    # Remove leading/trailing dots and spaces
    sanitized = sanitized.strip('. ')
    
    return sanitized


def truncate_string(text: str, max_length: int = 100, suffix: str = "...") -> str:
    """
    Truncate string to maximum length.
    
    Args:
        text: Text to truncate
        max_length: Maximum length
        suffix: Suffix to add if truncated
        
    Returns:
        Truncated text
    """
    if len(text) <= max_length:
        return text
    
    return text[:max_length - len(suffix)] + suffix


def count_occurrences(text: str, pattern: str) -> int:
    """
    Count occurrences of pattern in text.
    
    Args:
        text: Text to search
        pattern: Pattern to find
        
    Returns:
        Number of occurrences
    """
    return len(re.findall(pattern, text))


def get_nested_value(data: Dict[str, Any], key_path: str, default: Any = None) -> Any:
    """
    Get nested value from dictionary using dot notation.
    
    Args:
        data: Dictionary
        key_path: Dot-separated key path (e.g., "user.profile.name")
        default: Default value if key not found
        
    Returns:
        Value at key path or default
        
    Example:
        >>> data = {"user": {"profile": {"name": "John"}}}
        >>> get_nested_value(data, "user.profile.name")
        "John"
    """
    keys = key_path.split('.')
    current = data
    
    for key in keys:
        if isinstance(current, dict) and key in current:
            current = current[key]
        else:
            return default
    
    return current


def set_nested_value(data: Dict[str, Any], key_path: str, value: Any) -> Dict[str, Any]:
    """
    Set nested value in dictionary using dot notation.
    
    Args:
        data: Dictionary
        key_path: Dot-separated key path
        value: Value to set
        
    Returns:
        Updated dictionary
        
    Example:
        >>> data = {"user": {"profile": {}}}
        >>> set_nested_value(data, "user.profile.name", "John")
        {"user": {"profile": {"name": "John"}}}
    """
    keys = key_path.split('.')
    current = data
    
    for key in keys[:-1]:
        if key not in current:
            current[key] = {}
        current = current[key]
    
    current[keys[-1]] = value
    return data
