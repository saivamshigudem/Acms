"""
Mock ACMS API Module for Testing

Provides a mock API client that can be used in test generation.
"""

from dataclasses import dataclass
from typing import Optional, Dict, Any
import json
from pathlib import Path


@dataclass
class MockResponse:
    """Mock HTTP response."""
    status_code: int
    data: Dict[str, Any]
    
    def json(self) -> Dict[str, Any]:
        """Return JSON data."""
        return self.data


class ACMSAPI:
    """Mock ACMS API Client."""
    
    def __init__(self, base_url: str = "http://localhost:8080", token: Optional[str] = None):
        """
        Initialize API client.
        
        Args:
            base_url: Base URL for API
            token: Optional authentication token
        """
        self.base_url = base_url
        self.token = token
        self.agents = {}
        self.policies = {}
        self.commissions = {}
        self.payments = {}
        self._load_mock_data()
    
    def _load_mock_data(self) -> None:
        """Load mock data from generated_tests/mock_data.json if available."""
        mock_file = Path("generated_tests/mock_data.json")
        if mock_file.exists():
            try:
                with open(mock_file, 'r') as f:
                    data = json.load(f)
                    self.agents = {str(a.get('id', i)): a for i, a in enumerate(data.get('agents', []))}
                    self.policies = {str(p.get('id', i)): p for i, p in enumerate(data.get('policies', []))}
                    self.commissions = {str(c.get('id', i)): c for i, c in enumerate(data.get('commissions', []))}
                    self.payments = {str(p.get('id', i)): p for i, p in enumerate(data.get('payments', []))}
            except Exception as e:
                print(f"Warning: Could not load mock data: {e}")
    
    # Agent endpoints
    
    def create_agent(self, data: Dict[str, Any]) -> MockResponse:
        """Create a new agent."""
        # Handle string input (from generated tests)
        if isinstance(data, str):
            data = {"name": data, "email": f"{data.lower().replace(' ', '.')}@example.com", "commission_tier": 1}
        
        # Validate input
        if not data.get('name') or not data.get('email'):
            return MockResponse(400, {"error": "Name and email are required"})
        
        if '@' not in data.get('email', ''):
            return MockResponse(400, {"error": "Invalid email format"})
        
        # Create agent
        agent_id = str(len(self.agents) + 1)
        agent = {
            "id": agent_id,
            **data,
            "created_at": "2025-01-01T00:00:00Z"
        }
        self.agents[agent_id] = agent
        
        return MockResponse(201, agent)
    
    def get_agent(self, agent_id: str) -> MockResponse:
        """Retrieve an agent by ID."""
        if agent_id not in self.agents:
            return MockResponse(404, {"error": f"Agent {agent_id} not found"})
        
        return MockResponse(200, self.agents[agent_id])
    
    def update_agent(self, agent_id: str, data: Optional[Dict[str, Any]] = None) -> MockResponse:
        """Update an agent."""
        # Handle positional argument
        if data is None or isinstance(data, str):
            return MockResponse(400, {"error": "Invalid update data"})
        
        if agent_id not in self.agents:
            return MockResponse(404, {"error": f"Agent {agent_id} not found"})
        
        self.agents[agent_id].update(data)
        return MockResponse(200, self.agents[agent_id])
    
    def deactivate_agent(self, agent_id: str) -> MockResponse:
        """Deactivate an agent."""
        if agent_id not in self.agents:
            return MockResponse(404, {"error": f"Agent {agent_id} not found"})
        
        self.agents[agent_id]["active"] = False
        return MockResponse(204, {})
    
    def list_agents(self) -> MockResponse:
        """List all agents."""
        return MockResponse(200, {"agents": list(self.agents.values())})
    
    # Policy endpoints
    
    def create_policy(self, data: Dict[str, Any]) -> MockResponse:
        """Create a new policy."""
        # Handle string input (from generated tests)
        if isinstance(data, str):
            data = {"policy_name": data, "coverage_type": "general", "premium_amount": 100.0}
        
        if not data.get('policy_name') or not data.get('coverage_type'):
            return MockResponse(400, {"error": "Policy name and coverage type are required"})
        
        policy_id = str(len(self.policies) + 1)
        policy = {
            "id": policy_id,
            **data,
            "created_at": "2025-01-01T00:00:00Z"
        }
        self.policies[policy_id] = policy
        
        return MockResponse(201, policy)
    
    def get_policy(self, policy_id: str) -> MockResponse:
        """Retrieve a policy by ID."""
        if policy_id not in self.policies:
            return MockResponse(404, {"error": f"Policy {policy_id} not found"})
        
        return MockResponse(200, self.policies[policy_id])
    
    def update_policy(self, policy_id: str, data: Dict[str, Any]) -> MockResponse:
        """Update a policy."""
        if policy_id not in self.policies:
            return MockResponse(404, {"error": f"Policy {policy_id} not found"})
        
        self.policies[policy_id].update(data)
        return MockResponse(200, self.policies[policy_id])
    
    def list_policies(self) -> MockResponse:
        """List all policies."""
        return MockResponse(200, {"policies": list(self.policies.values())})
    
    # Commission endpoints
    
    def get_commission(self, commission_id: str) -> MockResponse:
        """Retrieve a commission by ID."""
        if commission_id not in self.commissions:
            return MockResponse(404, {"error": f"Commission {commission_id} not found"})
        
        return MockResponse(200, self.commissions[commission_id])
    
    def calculate_commission(self, agent_id: str, policy_id: str) -> MockResponse:
        """Calculate commission for an agent-policy pair."""
        if agent_id not in self.agents:
            return MockResponse(404, {"error": f"Agent {agent_id} not found"})
        
        if policy_id not in self.policies:
            return MockResponse(404, {"error": f"Policy {policy_id} not found"})
        
        # Calculate commission (10% of premium for example)
        policy = self.policies[policy_id]
        premium = policy.get('premium_amount', 100)
        commission_amount = premium * 0.1
        
        result = {
            "agent_id": agent_id,
            "policy_id": policy_id,
            "commission_amount": commission_amount,
            "calculated_at": "2025-01-01T00:00:00Z"
        }
        
        return MockResponse(200, result)
    
    def list_commissions(self) -> MockResponse:
        """List all commissions."""
        return MockResponse(200, {"commissions": list(self.commissions.values())})
    
    # Payment endpoints
    
    def process_payment(self, data: Dict[str, Any]) -> MockResponse:
        """Process a payment."""
        if not data.get('agent_id') or not data.get('amount'):
            return MockResponse(400, {"error": "Agent ID and amount are required"})
        
        if data.get('amount', 0) <= 0:
            return MockResponse(400, {"error": "Amount must be positive"})
        
        payment_id = str(len(self.payments) + 1)
        payment = {
            "id": payment_id,
            **data,
            "status": "PROCESSED",
            "created_at": "2025-01-01T00:00:00Z"
        }
        self.payments[payment_id] = payment
        
        return MockResponse(201, payment)
    
    def get_payment(self, payment_id: str) -> MockResponse:
        """Retrieve a payment by ID."""
        if payment_id not in self.payments:
            return MockResponse(404, {"error": f"Payment {payment_id} not found"})
        
        return MockResponse(200, self.payments[payment_id])
    
    def list_payments(self) -> MockResponse:
        """List all payments."""
        return MockResponse(200, {"payments": list(self.payments.values())})
    
    # Health check
    
    def health_check(self) -> MockResponse:
        """Check API health."""
        return MockResponse(200, {"status": "healthy", "version": "1.0.0"})
