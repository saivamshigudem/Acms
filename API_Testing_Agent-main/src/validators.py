"""
Validators and Response Checkers for ACMS API Testing

Validates API responses, business logic, and compliance.
"""

import json
import logging
import re
from typing import Dict, List, Any, Optional, Tuple
from datetime import datetime
from enum import Enum

logger = logging.getLogger(__name__)


class ValidationError(Exception):
    """Validation error exception."""
    pass


class ValidationResult:
    """Result of a validation check."""
    
    def __init__(self, is_valid: bool, message: str = "", details: Optional[Dict[str, Any]] = None):
        """
        Initialize validation result.
        
        Args:
            is_valid: Whether validation passed
            message: Validation message
            details: Optional additional details
        """
        self.is_valid = is_valid
        self.message = message
        self.details = details or {}
    
    def __str__(self) -> str:
        """String representation."""
        status = "✓ PASS" if self.is_valid else "✗ FAIL"
        return f"{status}: {self.message}"


class ResponseValidator:
    """Validate API responses."""
    
    @staticmethod
    def validate_status_code(response_code: int, expected_code: int) -> ValidationResult:
        """
        Validate HTTP status code.
        
        Args:
            response_code: Actual response code
            expected_code: Expected response code
            
        Returns:
            ValidationResult
        """
        is_valid = response_code == expected_code
        message = f"Status code is {response_code} (expected {expected_code})"
        
        if not is_valid:
            logger.warning(f"Status code mismatch: {response_code} != {expected_code}")
        
        return ValidationResult(is_valid, message, {"actual": response_code, "expected": expected_code})
    
    @staticmethod
    def validate_content_type(content_type: str, expected: str = "application/json") -> ValidationResult:
        """
        Validate response content type.
        
        Args:
            content_type: Actual content type
            expected: Expected content type
            
        Returns:
            ValidationResult
        """
        is_valid = expected in content_type if content_type else False
        message = f"Content-Type is {content_type}"
        
        return ValidationResult(is_valid, message, {"actual": content_type, "expected": expected})
    
    @staticmethod
    def validate_json_response(response_body: str) -> ValidationResult:
        """
        Validate that response is valid JSON.
        
        Args:
            response_body: Response body
            
        Returns:
            ValidationResult
        """
        try:
            json.loads(response_body)
            return ValidationResult(True, "Response is valid JSON")
        except json.JSONDecodeError as e:
            logger.warning(f"Invalid JSON response: {e}")
            return ValidationResult(False, f"Invalid JSON: {str(e)}")
    
    @staticmethod
    def validate_response_not_empty(response_body: str) -> ValidationResult:
        """
        Validate that response is not empty.
        
        Args:
            response_body: Response body
            
        Returns:
            ValidationResult
        """
        is_valid = bool(response_body and response_body.strip())
        message = "Response body is not empty" if is_valid else "Response body is empty"
        
        return ValidationResult(is_valid, message)
    
    @staticmethod
    def validate_response_time(response_time_ms: float, max_time_ms: float = 200) -> ValidationResult:
        """
        Validate response time.
        
        Args:
            response_time_ms: Actual response time in milliseconds
            max_time_ms: Maximum acceptable time
            
        Returns:
            ValidationResult
        """
        is_valid = response_time_ms <= max_time_ms
        message = f"Response time is {response_time_ms}ms (max {max_time_ms}ms)"
        
        if not is_valid:
            logger.warning(f"Response time exceeded: {response_time_ms}ms > {max_time_ms}ms")
        
        return ValidationResult(is_valid, message, {"actual": response_time_ms, "expected_max": max_time_ms})


class SchemaValidator:
    """Validate response schemas."""
    
    @staticmethod
    def validate_required_fields(data: Dict[str, Any], required_fields: List[str]) -> ValidationResult:
        """
        Validate that response contains required fields.
        
        Args:
            data: Response data
            required_fields: Required field names
            
        Returns:
            ValidationResult
        """
        missing_fields = [f for f in required_fields if f not in data]
        
        is_valid = len(missing_fields) == 0
        message = f"All required fields present" if is_valid else f"Missing fields: {missing_fields}"
        
        return ValidationResult(is_valid, message, {"required": required_fields, "missing": missing_fields})
    
    @staticmethod
    def validate_field_types(data: Dict[str, Any], field_types: Dict[str, type]) -> ValidationResult:
        """
        Validate field types.
        
        Args:
            data: Response data
            field_types: Map of field name to expected type
            
        Returns:
            ValidationResult
        """
        type_errors = []
        
        for field_name, expected_type in field_types.items():
            if field_name not in data:
                continue
            
            value = data[field_name]
            actual_type = type(value)
            
            # Handle nullable fields
            if value is None:
                continue
            
            # Handle type mismatches
            if not isinstance(value, expected_type):
                type_errors.append({
                    "field": field_name,
                    "expected": expected_type.__name__,
                    "actual": actual_type.__name__
                })
        
        is_valid = len(type_errors) == 0
        message = "All field types correct" if is_valid else f"Type errors: {len(type_errors)}"
        
        return ValidationResult(is_valid, message, {"type_errors": type_errors})
    
    @staticmethod
    def validate_field_values(data: Dict[str, Any], validations: Dict[str, callable]) -> ValidationResult:
        """
        Validate field values using callable validators.
        
        Args:
            data: Response data
            validations: Map of field name to validation callable
            
        Returns:
            ValidationResult
        """
        value_errors = []
        
        for field_name, validator_func in validations.items():
            if field_name not in data:
                continue
            
            value = data[field_name]
            
            try:
                is_valid = validator_func(value)
                if not is_valid:
                    value_errors.append({"field": field_name, "value": value})
            except Exception as e:
                value_errors.append({"field": field_name, "value": value, "error": str(e)})
        
        is_valid = len(value_errors) == 0
        message = "All field values valid" if is_valid else f"Value errors: {len(value_errors)}"
        
        return ValidationResult(is_valid, message, {"value_errors": value_errors})


class BusinessLogicValidator:
    """Validate business logic."""
    
    @staticmethod
    def validate_commission_calculation(
        policy_premium: float,
        agent_commission_tier: float,
        calculated_amount: float
    ) -> ValidationResult:
        """
        Validate commission calculation: amount = premium × tier.
        
        Args:
            policy_premium: Policy premium
            agent_commission_tier: Agent commission tier (percentage)
            calculated_amount: Calculated commission amount
            
        Returns:
            ValidationResult
        """
        expected_amount = round(policy_premium * (agent_commission_tier / 100), 2)
        is_valid = abs(calculated_amount - expected_amount) < 0.01  # Allow rounding
        
        message = f"Commission calculation correct: {policy_premium} × {agent_commission_tier}% = {calculated_amount}"
        
        if not is_valid:
            logger.warning(f"Commission calculation error: {calculated_amount} != {expected_amount}")
        
        return ValidationResult(
            is_valid,
            message,
            {"premium": policy_premium, "tier": agent_commission_tier, "expected": expected_amount, "actual": calculated_amount}
        )
    
    @staticmethod
    def validate_commission_tier_range(commission_tier: float) -> ValidationResult:
        """
        Validate commission tier is in valid range (0-100).
        
        Args:
            commission_tier: Commission tier percentage
            
        Returns:
            ValidationResult
        """
        is_valid = 0 <= commission_tier <= 100
        message = f"Commission tier {commission_tier}% is in valid range (0-100)" if is_valid else f"Commission tier {commission_tier}% is out of range"
        
        return ValidationResult(is_valid, message, {"tier": commission_tier, "min": 0, "max": 100})
    
    @staticmethod
    def validate_premium_positive(premium: float) -> ValidationResult:
        """
        Validate premium is positive.
        
        Args:
            premium: Policy premium
            
        Returns:
            ValidationResult
        """
        is_valid = premium > 0
        message = f"Premium {premium} is positive" if is_valid else f"Premium {premium} must be positive"
        
        return ValidationResult(is_valid, message, {"premium": premium})
    
    @staticmethod
    def validate_payment_not_exceeds_commission(
        payment_amount: float,
        commission_amount: float
    ) -> ValidationResult:
        """
        Validate payment amount does not exceed commission amount.
        
        Args:
            payment_amount: Payment amount
            commission_amount: Commission amount
            
        Returns:
            ValidationResult
        """
        is_valid = payment_amount <= commission_amount
        message = f"Payment amount {payment_amount} does not exceed commission {commission_amount}" if is_valid else f"Payment amount {payment_amount} exceeds commission {commission_amount}"
        
        return ValidationResult(is_valid, message, {"payment": payment_amount, "commission": commission_amount})
    
    @staticmethod
    def validate_date_range(start_date_str: str, end_date_str: str) -> ValidationResult:
        """
        Validate date range (end date after start date).
        
        Args:
            start_date_str: Start date ISO string
            end_date_str: End date ISO string
            
        Returns:
            ValidationResult
        """
        try:
            start_date = datetime.fromisoformat(start_date_str.replace('Z', '+00:00'))
            end_date = datetime.fromisoformat(end_date_str.replace('Z', '+00:00'))
            
            is_valid = end_date > start_date
            message = f"Date range valid: {start_date_str} to {end_date_str}" if is_valid else f"End date must be after start date"
            
            return ValidationResult(is_valid, message)
        except ValueError as e:
            return ValidationResult(False, f"Invalid date format: {e}")


class SecurityValidator:
    """Validate security aspects."""
    
    @staticmethod
    def validate_no_sensitive_data_in_error(error_message: str, sensitive_patterns: Optional[List[str]] = None) -> ValidationResult:
        """
        Validate error message doesn't contain sensitive data.
        
        Args:
            error_message: Error message
            sensitive_patterns: Patterns to check for (e.g., SQL keywords, stack traces)
            
        Returns:
            ValidationResult
        """
        if sensitive_patterns is None:
            sensitive_patterns = [
                "password",
                "secret",
                "token",
                "at /",
                "line ",
                "traceback",
                "exception",
                "stack trace"
            ]
        
        found_patterns = [p for p in sensitive_patterns if p.lower() in error_message.lower()]
        
        is_valid = len(found_patterns) == 0
        message = "No sensitive data in error message" if is_valid else f"Found sensitive patterns: {found_patterns}"
        
        return ValidationResult(is_valid, message, {"found_patterns": found_patterns})
    
    @staticmethod
    def validate_auth_required(response_code: int, expected_code: int = 401) -> ValidationResult:
        """
        Validate authentication is required (401 response).
        
        Args:
            response_code: Response code
            expected_code: Expected code for auth required
            
        Returns:
            ValidationResult
        """
        is_valid = response_code == expected_code
        message = f"Authentication required (status {response_code})" if is_valid else f"Expected {expected_code}, got {response_code}"
        
        return ValidationResult(is_valid, message, {"actual": response_code, "expected": expected_code})
    
    @staticmethod
    def validate_email_format(email: str) -> ValidationResult:
        """
        Validate email format.
        
        Args:
            email: Email address
            
        Returns:
            ValidationResult
        """
        # Simple email validation regex
        email_pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        is_valid = re.match(email_pattern, email) is not None
        message = f"Email format valid: {email}" if is_valid else f"Invalid email format: {email}"
        
        return ValidationResult(is_valid, message, {"email": email})
    
    @staticmethod
    def validate_no_sql_injection(user_input: str) -> ValidationResult:
        """
        Check for SQL injection patterns in user input.
        
        Args:
            user_input: User input to check
            
        Returns:
            ValidationResult
        """
        sql_patterns = [
            "'; DROP",
            "' OR '1'='1",
            "UNION SELECT",
            "'; DELETE",
            "'; INSERT",
            "'; UPDATE"
        ]
        
        found_patterns = [p for p in sql_patterns if p.upper() in user_input.upper()]
        
        is_valid = len(found_patterns) == 0
        message = "No SQL injection patterns detected" if is_valid else f"Found SQL patterns: {found_patterns}"
        
        return ValidationResult(is_valid, message, {"input": user_input, "patterns_found": found_patterns})


class ComplianceValidator:
    """Validate API compliance with standards."""
    
    @staticmethod
    def validate_error_response_format(error_response: Dict[str, Any]) -> ValidationResult:
        """
        Validate error response follows standard format.
        
        Expected format:
        {
            "errorCode": "string",
            "message": "string",
            "timestamp": "ISO8601",
            "fieldErrors": [{"field": "string", "message": "string"}]
        }
        
        Args:
            error_response: Error response object
            
        Returns:
            ValidationResult
        """
        required_fields = ["errorCode", "message", "timestamp"]
        missing_fields = [f for f in required_fields if f not in error_response]
        
        is_valid = len(missing_fields) == 0
        message = "Error response format valid" if is_valid else f"Missing fields: {missing_fields}"
        
        return ValidationResult(is_valid, message, {"format_valid": is_valid, "required": required_fields, "missing": missing_fields})
    
    @staticmethod
    def validate_pagination_format(response: Dict[str, Any]) -> ValidationResult:
        """
        Validate pagination format.
        
        Expected format:
        {
            "content": [...],
            "totalElements": int,
            "totalPages": int,
            "currentPage": int,
            "size": int
        }
        
        Args:
            response: Response object
            
        Returns:
            ValidationResult
        """
        required_fields = ["content", "totalElements", "totalPages"]
        missing_fields = [f for f in required_fields if f not in response]
        
        is_valid = len(missing_fields) == 0
        message = "Pagination format valid" if is_valid else f"Missing pagination fields: {missing_fields}"
        
        return ValidationResult(is_valid, message, {"format_valid": is_valid, "missing": missing_fields})
    
    @staticmethod
    def validate_api_response_structure(response: Dict[str, Any], expected_type: str = "object") -> ValidationResult:
        """
        Validate API response structure.
        
        Args:
            response: Response object
            expected_type: Expected type (object, array, etc.)
            
        Returns:
            ValidationResult
        """
        type_map = {
            "object": dict,
            "array": list,
            "string": str,
            "number": (int, float),
            "boolean": bool
        }
        
        expected_python_type = type_map.get(expected_type)
        
        if expected_python_type is None:
            return ValidationResult(False, f"Unknown expected type: {expected_type}")
        
        is_valid = isinstance(response, expected_python_type)
        message = f"Response is {expected_type}" if is_valid else f"Response is not {expected_type}"
        
        return ValidationResult(is_valid, message, {"expected_type": expected_type, "actual_type": type(response).__name__})


def main():
    """Demonstrate validators."""
    import sys
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Test ResponseValidator
    print("Testing ResponseValidator...")
    result = ResponseValidator.validate_status_code(200, 200)
    print(f"  {result}")
    
    result = ResponseValidator.validate_status_code(404, 200)
    print(f"  {result}")
    
    # Test BusinessLogicValidator
    print("\nTesting BusinessLogicValidator...")
    result = BusinessLogicValidator.validate_commission_calculation(1000, 10.5, 105.00)
    print(f"  {result}")
    
    result = BusinessLogicValidator.validate_commission_tier_range(15.0)
    print(f"  {result}")
    
    result = BusinessLogicValidator.validate_commission_tier_range(150)
    print(f"  {result}")
    
    # Test SchemaValidator
    print("\nTesting SchemaValidator...")
    data = {"id": 1, "name": "Test", "email": "test@example.com"}
    result = SchemaValidator.validate_required_fields(data, ["id", "name", "email"])
    print(f"  {result}")
    
    result = SchemaValidator.validate_required_fields(data, ["id", "name", "email", "phone"])
    print(f"  {result}")
    
    # Test SecurityValidator
    print("\nTesting SecurityValidator...")
    result = SecurityValidator.validate_email_format("test@example.com")
    print(f"  {result}")
    
    result = SecurityValidator.validate_email_format("invalid-email")
    print(f"  {result}")


if __name__ == "__main__":
    main()
