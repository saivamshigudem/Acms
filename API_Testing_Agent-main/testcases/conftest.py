"""
Pytest configuration and fixtures for ACMS API Testing.

Provides fixtures for API client, authentication, and test data.
"""

import pytest
import requests
import os
from pathlib import Path
from dotenv import load_dotenv


class APIClient:
    """Simple API client wrapper."""
    
    def __init__(self, base_url, timeout=30):
        self.base_url = base_url
        self.timeout = timeout
        self.session = requests.Session()
    
    def request(self, method, url, **kwargs):
        """Make HTTP request."""
        if not url.startswith("http"):
            url = f"{self.base_url}{url}"
        
        # Set default timeout if not provided
        if "timeout" not in kwargs:
            kwargs["timeout"] = self.timeout
        
        return self.session.request(method, url, **kwargs)
    
    def get(self, url, **kwargs):
        """GET request."""
        return self.request("GET", url, **kwargs)
    
    def post(self, url, **kwargs):
        """POST request."""
        return self.request("POST", url, **kwargs)
    
    def put(self, url, **kwargs):
        """PUT request."""
        return self.request("PUT", url, **kwargs)
    
    def delete(self, url, **kwargs):
        """DELETE request."""
        return self.request("DELETE", url, **kwargs)


@pytest.fixture(scope="session")
def config():
    """Load configuration from environment."""
    # Load .env file if it exists
    env_file = Path(".env")
    if env_file.exists():
        load_dotenv(env_file)
    
    # Get configuration from environment
    config = {
        "api_base_url": os.getenv("API_BASE_URL", "http://localhost:8080"),
        "api_timeout": int(os.getenv("API_TIMEOUT", "30")),
        "auth_token": os.getenv("AUTH_TOKEN", "Bearer valid-token-123"),
        "auth_header": os.getenv("AUTH_HEADER", "Authorization"),
    }
    
    return config


@pytest.fixture
def api_client(config):
    """Create API client with base URL and timeout."""
    return APIClient(config["api_base_url"], config["api_timeout"])


@pytest.fixture
def auth_headers(config):
    """Get authentication headers."""
    return {
        config["auth_header"]: config["auth_token"]
    }


@pytest.fixture
def test_data():
    """Provide test data for agents."""
    return {
        "valid_agent": {
            "name": "Test Agent",
            "email": "test@example.com",
            "commissionTier": 10,
        },
        "valid_agent_2": {
            "name": "Another Agent",
            "email": "another@example.com",
            "commissionTier": 15,
        },
        "invalid_agent_missing_email": {
            "name": "Test Agent",
            "commissionTier": 10,
        },
        "invalid_agent_missing_name": {
            "email": "test@example.com",
            "commissionTier": 10,
        },
        "invalid_agent_missing_tier": {
            "name": "Test Agent",
            "email": "test@example.com",
        },
        "invalid_agent_bad_email": {
            "name": "Test Agent",
            "email": "invalid-email",
            "commissionTier": 10,
        },
        "invalid_agent_bad_tier": {
            "name": "Test Agent",
            "email": "test@example.com",
            "commissionTier": 150,  # Out of range
        },
    }


@pytest.fixture
def cleanup_agents(api_client, auth_headers):
    """Fixture to cleanup created agents after tests."""
    created_agents = []
    
    yield created_agents
    
    # Cleanup: Delete all created agents
    for agent_id in created_agents:
        try:
            api_client.delete(f"/agents/{agent_id}", headers=auth_headers)
        except Exception:
            pass  # Ignore cleanup errors
