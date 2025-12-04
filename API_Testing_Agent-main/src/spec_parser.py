"""
OpenAPI Specification Parser Module

Parses OpenAPI 3.0 specifications and extracts endpoint, schema, and metadata information.
"""

import logging
from pathlib import Path
from typing import Dict, Any, List, Optional
import yaml
import json

from .utils.validators import validate_openapi_spec, validate_endpoint

logger = logging.getLogger(__name__)


class SpecParser:
    """
    Parser for OpenAPI 3.0 specifications.
    
    Extracts endpoints, schemas, parameters, and other metadata from OpenAPI specs.
    """
    
    def __init__(self, spec_file: Path):
        """
        Initialize the specification parser.
        
        Args:
            spec_file: Path to OpenAPI specification file (YAML or JSON)
        """
        self.spec_file = Path(spec_file)
        self.spec: Dict[str, Any] = {}
        self.endpoints: List[Dict[str, Any]] = []
        self.schemas: Dict[str, Any] = {}
        
        self._load_spec()
    
    def _load_spec(self) -> None:
        """Load and parse the OpenAPI specification file."""
        if not self.spec_file.exists():
            raise FileNotFoundError(f"Specification file not found: {self.spec_file}")
        
        logger.info(f"Loading OpenAPI specification from {self.spec_file}")
        
        # Determine file format and load
        if self.spec_file.suffix.lower() in ['.yaml', '.yml']:
            with open(self.spec_file, 'r', encoding='utf-8') as f:
                self.spec = yaml.safe_load(f)
        elif self.spec_file.suffix.lower() == '.json':
            with open(self.spec_file, 'r', encoding='utf-8') as f:
                self.spec = json.load(f)
        else:
            raise ValueError(f"Unsupported file format: {self.spec_file.suffix}")
        
        # Validate specification
        validate_openapi_spec(self.spec)
        
        # Extract components
        self._extract_endpoints()
        self._extract_schemas()
        
        logger.info(f"Loaded {len(self.endpoints)} endpoints from specification")
    
    def _extract_endpoints(self) -> None:
        """Extract all endpoints from the specification."""
        paths = self.spec.get("paths", {})
        
        for path, path_item in paths.items():
            # Validate endpoint
            validate_endpoint(path_item, path)
            
            # Extract methods
            for method in ["get", "post", "put", "delete", "patch", "head", "options"]:
                if method not in path_item:
                    continue
                
                operation = path_item[method]
                
                endpoint_info = {
                    "path": path,
                    "method": method.upper(),
                    "operation_id": operation.get("operationId", f"{method}_{path}"),
                    "summary": operation.get("summary", ""),
                    "description": operation.get("description", ""),
                    "tags": operation.get("tags", []),
                    "parameters": operation.get("parameters", []),
                    "request_body": operation.get("requestBody", {}),
                    "responses": operation.get("responses", {}),
                    "security": operation.get("security", []),
                }
                
                self.endpoints.append(endpoint_info)
    
    def _extract_schemas(self) -> None:
        """Extract all schemas from the specification."""
        components = self.spec.get("components", {})
        schemas = components.get("schemas", {})
        
        self.schemas = schemas
        logger.debug(f"Extracted {len(self.schemas)} schemas")
    
    def get_endpoints(self) -> List[Dict[str, Any]]:
        """
        Get all extracted endpoints.
        
        Returns:
            List of endpoint definitions
        """
        return self.endpoints
    
    def get_endpoint_by_path(self, path: str, method: str) -> Optional[Dict[str, Any]]:
        """
        Get endpoint by path and method.
        
        Args:
            path: Endpoint path
            method: HTTP method
            
        Returns:
            Endpoint definition or None if not found
        """
        for endpoint in self.endpoints:
            if endpoint["path"] == path and endpoint["method"].upper() == method.upper():
                return endpoint
        return None
    
    def get_schemas(self) -> Dict[str, Any]:
        """
        Get all extracted schemas.
        
        Returns:
            Dictionary of schemas
        """
        return self.schemas
    
    def get_schema(self, schema_name: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific schema by name.
        
        Args:
            schema_name: Name of the schema
            
        Returns:
            Schema definition or None if not found
        """
        return self.schemas.get(schema_name)
    
    def get_spec_info(self) -> Dict[str, Any]:
        """
        Get specification metadata.
        
        Returns:
            Dictionary with title, version, description
        """
        info = self.spec.get("info", {})
        return {
            "title": info.get("title", "Unknown"),
            "version": info.get("version", "Unknown"),
            "description": info.get("description", ""),
            "contact": info.get("contact", {}),
            "license": info.get("license", {}),
        }
    
    def get_servers(self) -> List[Dict[str, Any]]:
        """
        Get server definitions from specification.
        
        Returns:
            List of server definitions
        """
        return self.spec.get("servers", [])
    
    def get_security_schemes(self) -> Dict[str, Any]:
        """
        Get security schemes from specification.
        
        Returns:
            Dictionary of security schemes
        """
        components = self.spec.get("components", {})
        return components.get("securitySchemes", {})
    
    def get_parameters_for_endpoint(self, endpoint: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        Get all parameters for an endpoint.
        
        Args:
            endpoint: Endpoint definition
            
        Returns:
            List of parameter definitions
        """
        parameters = endpoint.get("parameters", [])
        
        # Also check request body
        request_body = endpoint.get("request_body", {})
        if request_body:
            content = request_body.get("content", {})
            if content:
                # Add request body as a parameter
                parameters.append({
                    "name": "body",
                    "in": "body",
                    "required": request_body.get("required", False),
                    "schema": list(content.values())[0].get("schema", {}),
                })
        
        return parameters
    
    def get_response_schemas(self, endpoint: Dict[str, Any]) -> Dict[int, Dict[str, Any]]:
        """
        Get response schemas for an endpoint.
        
        Args:
            endpoint: Endpoint definition
            
        Returns:
            Dictionary mapping status codes to response schemas
        """
        responses = endpoint.get("responses", {})
        response_schemas = {}
        
        for status_code, response_def in responses.items():
            try:
                status_int = int(status_code)
                content = response_def.get("content", {})
                if content:
                    schema = list(content.values())[0].get("schema", {})
                    response_schemas[status_int] = schema
            except ValueError:
                # Skip non-numeric status codes like "default"
                pass
        
        return response_schemas
    
    def __repr__(self) -> str:
        """String representation of parser."""
        return f"SpecParser({self.spec_file}, endpoints={len(self.endpoints)})"
