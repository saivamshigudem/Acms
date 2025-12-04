"""
Mock Data Generator for ACMS API Testing

Generates realistic test data for all ACMS endpoints.
Supports creating agents, policies, commissions, and payments.
"""

import json
import random
import logging
from datetime import datetime, timedelta
from typing import Dict, List, Any, Optional
from dataclasses import dataclass, asdict
from enum import Enum

logger = logging.getLogger(__name__)


class AgentStatus(str, Enum):
    """Agent status enumeration."""
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    ON_LEAVE = "ON_LEAVE"
    TERMINATED = "TERMINATED"


class PolicyStatus(str, Enum):
    """Policy status enumeration."""
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    EXPIRED = "EXPIRED"
    CANCELLED = "CANCELLED"


class CommissionStatus(str, Enum):
    """Commission status enumeration."""
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    PAID = "PAID"
    CANCELLED = "CANCELLED"


class PaymentStatus(str, Enum):
    """Payment status enumeration."""
    PENDING = "PENDING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class CoverageType(str, Enum):
    """Coverage type enumeration."""
    HEALTH = "HEALTH"
    LIFE = "LIFE"
    DENTAL = "DENTAL"
    VISION = "VISION"
    DISABILITY = "DISABILITY"


@dataclass
class MockAgent:
    """Mock Agent data."""
    name: str
    email: str
    phoneNumber: str
    commissionTier: float
    hireDate: str
    address: str
    city: str
    state: str
    postalCode: str
    country: str
    status: str = AgentStatus.ACTIVE.value
    isActive: bool = True
    totalCommissions: float = 0.0
    notes: str = ""
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return asdict(self)


@dataclass
class MockPolicy:
    """Mock Policy data."""
    agentId: int
    coverageType: str
    premium: float
    effectiveDate: str
    expirationDate: str
    status: str = PolicyStatus.ACTIVE.value
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return asdict(self)


@dataclass
class MockCommission:
    """Mock Commission data."""
    policyId: int
    agentId: int
    commissionTier: float
    calculatedAmount: float
    calculationDate: str
    status: str = CommissionStatus.PENDING.value
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return asdict(self)


@dataclass
class MockPayment:
    """Mock Payment data."""
    commissionId: int
    amount: float
    paymentDate: str
    status: str = PaymentStatus.PENDING.value
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary."""
        return asdict(self)


class MockDataGenerator:
    """Generate realistic mock data for ACMS testing."""
    
    # Sample data pools
    FIRST_NAMES = [
        "John", "Jane", "Robert", "Patricia", "Michael", "Jennifer",
        "William", "Linda", "David", "Barbara", "Richard", "Susan",
        "Joseph", "Jessica", "Thomas", "Sarah", "Charles", "Karen"
    ]
    
    LAST_NAMES = [
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia",
        "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez",
        "Gonzalez", "Wilson", "Anderson", "Taylor", "Thomas", "Moore"
    ]
    
    CITIES = [
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
        "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
        "Austin", "Seattle", "Denver", "Portland", "Boston"
    ]
    
    STATES = [
        "NY", "CA", "TX", "FL", "PA", "IL", "OH", "GA", "NC", "MI",
        "NJ", "VA", "WA", "AZ", "MA", "TN", "IN", "MO", "MD", "WI"
    ]
    
    POSTAL_CODES = [
        "10001", "90001", "60601", "77001", "85001", "19101", "78201",
        "92101", "75201", "95101", "73301", "98101", "80201", "97201"
    ]
    
    def __init__(self, seed: Optional[int] = None):
        """
        Initialize mock data generator.
        
        Args:
            seed: Optional seed for reproducible data
        """
        if seed is not None:
            random.seed(seed)
    
    def generate_agent(self, agent_id: Optional[int] = None) -> MockAgent:
        """
        Generate a random agent.
        
        Args:
            agent_id: Optional agent ID (not used in data, for reference)
            
        Returns:
            MockAgent instance
        """
        first_name = random.choice(self.FIRST_NAMES)
        last_name = random.choice(self.LAST_NAMES)
        
        return MockAgent(
            name=f"{first_name} {last_name}",
            email=f"{first_name.lower()}.{last_name.lower()}@acms.example.com",
            phoneNumber=f"555-{random.randint(1000, 9999)}-{random.randint(1000, 9999)}",
            commissionTier=round(random.uniform(5.0, 20.0), 2),
            hireDate=(datetime.now() - timedelta(days=random.randint(365, 1825))).isoformat(),
            address=f"{random.randint(100, 9999)} Main Street",
            city=random.choice(self.CITIES),
            state=random.choice(self.STATES),
            postalCode=random.choice(self.POSTAL_CODES),
            country="USA",
            status=random.choice([s.value for s in AgentStatus]),
            isActive=random.choice([True, True, True, False]),  # Mostly active
            notes=random.choice([
                "Top performer",
                "Reliable agent",
                "New hire",
                "Veteran agent",
                "",
                "High growth potential"
            ])
        )
    
    def generate_agents(self, count: int = 5) -> List[MockAgent]:
        """
        Generate multiple agents.
        
        Args:
            count: Number of agents to generate
            
        Returns:
            List of MockAgent instances
        """
        return [self.generate_agent(i) for i in range(count)]
    
    def generate_policy(self, agent_id: int) -> MockPolicy:
        """
        Generate a policy for an agent.
        
        Args:
            agent_id: Agent ID to link policy to
            
        Returns:
            MockPolicy instance
        """
        coverage = random.choice([c.value for c in CoverageType])
        
        # Random premium based on coverage type
        premium_ranges = {
            "HEALTH": (500, 2000),
            "LIFE": (100, 1000),
            "DENTAL": (50, 500),
            "VISION": (25, 250),
            "DISABILITY": (200, 1500)
        }
        
        min_premium, max_premium = premium_ranges.get(coverage, (100, 1000))
        premium = round(random.uniform(min_premium, max_premium), 2)
        
        effective_date = datetime.now()
        expiration_date = effective_date + timedelta(days=365)
        
        return MockPolicy(
            agentId=agent_id,
            coverageType=coverage,
            premium=premium,
            effectiveDate=effective_date.isoformat(),
            expirationDate=expiration_date.isoformat(),
            status=random.choice([PolicyStatus.ACTIVE.value, PolicyStatus.INACTIVE.value])
        )
    
    def generate_policies(self, agent_id: int, count: int = 3) -> List[MockPolicy]:
        """
        Generate multiple policies for an agent.
        
        Args:
            agent_id: Agent ID to link policies to
            count: Number of policies to generate
            
        Returns:
            List of MockPolicy instances
        """
        return [self.generate_policy(agent_id) for _ in range(count)]
    
    def generate_commission(self, policy_id: int, agent_id: int, commission_tier: float) -> MockCommission:
        """
        Generate a commission record.
        
        Args:
            policy_id: Policy ID
            agent_id: Agent ID
            commission_tier: Agent's commission tier (percentage)
            
        Returns:
            MockCommission instance
        """
        # Note: actual amount should be premium * tier, but we don't have it here
        # Mock a realistic amount (assume premium 1000)
        calculated_amount = round(1000 * (commission_tier / 100), 2)
        
        return MockCommission(
            policyId=policy_id,
            agentId=agent_id,
            commissionTier=commission_tier,
            calculatedAmount=calculated_amount,
            calculationDate=datetime.now().isoformat(),
            status=random.choice([CommissionStatus.PENDING.value, CommissionStatus.APPROVED.value])
        )
    
    def generate_commissions(
        self,
        policy_id: int,
        agent_id: int,
        commission_tier: float,
        count: int = 1
    ) -> List[MockCommission]:
        """
        Generate multiple commission records.
        
        Args:
            policy_id: Policy ID
            agent_id: Agent ID
            commission_tier: Commission tier
            count: Number of commissions to generate
            
        Returns:
            List of MockCommission instances
        """
        return [
            self.generate_commission(policy_id, agent_id, commission_tier)
            for _ in range(count)
        ]
    
    def generate_payment(self, commission_id: int, commission_amount: float) -> MockPayment:
        """
        Generate a payment record.
        
        Args:
            commission_id: Commission ID
            commission_amount: Commission amount to pay
            
        Returns:
            MockPayment instance
        """
        # Payment amount should be <= commission amount
        payment_amount = round(random.uniform(commission_amount * 0.5, commission_amount), 2)
        
        payment_date = (datetime.now() - timedelta(days=random.randint(0, 30))).isoformat()
        
        return MockPayment(
            commissionId=commission_id,
            amount=payment_amount,
            paymentDate=payment_date,
            status=random.choice([PaymentStatus.COMPLETED.value, PaymentStatus.PENDING.value])
        )
    
    def generate_payments(
        self,
        commission_id: int,
        commission_amount: float,
        count: int = 1
    ) -> List[MockPayment]:
        """
        Generate multiple payment records.
        
        Args:
            commission_id: Commission ID
            commission_amount: Commission amount
            count: Number of payments to generate
            
        Returns:
            List of MockPayment instances
        """
        return [
            self.generate_payment(commission_id, commission_amount)
            for _ in range(count)
        ]
    
    def generate_full_scenario(self, num_agents: int = 3) -> Dict[str, Any]:
        """
        Generate a complete scenario with agents, policies, commissions, and payments.
        
        Args:
            num_agents: Number of agents to generate
            
        Returns:
            Dictionary with all generated data
        """
        scenario = {
            "agents": [],
            "policies": [],
            "commissions": [],
            "payments": []
        }
        
        # Generate agents
        agents = self.generate_agents(num_agents)
        scenario["agents"] = [a.to_dict() for a in agents]
        
        # Generate policies and commissions for each agent
        policy_id_counter = 1
        commission_id_counter = 1
        
        for agent_idx, agent in enumerate(agents):
            # Generate 2-4 policies per agent
            num_policies = random.randint(2, 4)
            policies = self.generate_policies(agent_idx + 1, num_policies)
            
            for policy in policies:
                scenario["policies"].append(policy.to_dict())
                
                # Generate commission for each policy
                commission = self.generate_commission(
                    policy_id_counter,
                    agent_idx + 1,
                    agent.commissionTier
                )
                scenario["commissions"].append(commission.to_dict())
                
                # Generate 0-2 payments per commission
                num_payments = random.randint(0, 2)
                payments = self.generate_payments(
                    commission_id_counter,
                    commission.calculatedAmount,
                    num_payments
                )
                scenario["payments"].extend([p.to_dict() for p in payments])
                
                policy_id_counter += 1
                commission_id_counter += 1
        
        return scenario
    
    @staticmethod
    def save_to_file(data: Dict[str, Any], filepath: str) -> None:
        """
        Save generated data to JSON file.
        
        Args:
            data: Data to save
            filepath: Path to save file to
        """
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2)
        logger.info(f"Saved mock data to {filepath}")
    
    @staticmethod
    def load_from_file(filepath: str) -> Dict[str, Any]:
        """
        Load generated data from JSON file.
        
        Args:
            filepath: Path to load file from
            
        Returns:
            Dictionary with loaded data
        """
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
        logger.info(f"Loaded mock data from {filepath}")
        return data


# Test data for immediate use (validation tests)
TEST_AGENT_VALID = {
    "name": "Test Agent",
    "email": "test.agent@example.com",
    "phoneNumber": "555-1234-5678",
    "commissionTier": 12.5,
    "hireDate": datetime.now().isoformat(),
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "status": "ACTIVE"
}

TEST_AGENT_INVALID = {
    "name": "",  # Empty name
    "email": "invalid-email",  # Invalid email
    "commissionTier": 150,  # Out of range
}

TEST_POLICY_VALID = {
    "agentId": 1,
    "coverageType": "HEALTH",
    "premium": 1000.00,
    "effectiveDate": datetime.now().isoformat(),
    "expirationDate": (datetime.now() + timedelta(days=365)).isoformat(),
    "status": "ACTIVE"
}

TEST_POLICY_INVALID = {
    "agentId": 999999,  # Non-existent agent
    "coverageType": "INVALID",  # Invalid coverage type
    "premium": -100,  # Negative premium
}

TEST_COMMISSION_VALID = {
    "policyId": 1,
    "agentId": 1,
    "commissionTier": 10.5,
    "calculatedAmount": 105.00,
    "calculationDate": datetime.now().isoformat(),
    "status": "PENDING"
}

TEST_PAYMENT_VALID = {
    "commissionId": 1,
    "amount": 50.00,
    "paymentDate": datetime.now().isoformat(),
    "status": "PENDING"
}

TEST_PAYMENT_INVALID = {
    "commissionId": 999999,  # Non-existent commission
    "amount": -50.00,  # Negative amount
}


def main():
    """Demonstrate mock data generation."""
    import sys
    
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Create generator
    generator = MockDataGenerator(seed=42)  # Reproducible data
    
    # Generate scenario
    print("Generating mock data scenario...")
    scenario = generator.generate_full_scenario(num_agents=3)
    
    print(f"\n✓ Generated {len(scenario['agents'])} agents")
    print(f"✓ Generated {len(scenario['policies'])} policies")
    print(f"✓ Generated {len(scenario['commissions'])} commissions")
    print(f"✓ Generated {len(scenario['payments'])} payments")
    
    # Print sample data
    print("\n" + "="*60)
    print("SAMPLE AGENT:")
    print(json.dumps(scenario["agents"][0], indent=2))
    
    print("\n" + "="*60)
    print("SAMPLE POLICY:")
    print(json.dumps(scenario["policies"][0], indent=2))
    
    print("\n" + "="*60)
    print("SAMPLE COMMISSION:")
    print(json.dumps(scenario["commissions"][0], indent=2))
    
    # Save to file
    output_file = "mock_data_scenario.json"
    generator.save_to_file(scenario, output_file)
    print(f"\n✓ Saved to {output_file}")


if __name__ == "__main__":
    main()
