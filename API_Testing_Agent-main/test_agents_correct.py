import pytest
import requests
import json
import random
import string

# Configuration
BASE_URL = "http://localhost:8080/api/api/v1"
HEADERS = {
    "accept": "application/json",
    "Content-Type": "application/json"
}

def generate_unique_code(prefix="TEST"):
    """Generate a unique agent code"""
    suffix = ''.join(random.choices(string.digits, k=4))
    return f"{prefix}{suffix}"

@pytest.fixture
def agent_data():
    return {
        "agentCode": generate_unique_code(),
        "firstName": "Test",
        "lastName": "Agent",
        "email": f"test.agent.{generate_unique_code()}@example.com",
        "phone": "+1234567890",
        "address": "123 Test St",
        "city": "Test City",
        "state": "TC",
        "postalCode": "12345",
        "country": "USA",
        "status": "ACTIVE",
        "dateOfBirth": "1990-01-01",
        "hireDate": "2023-01-01",
        "notes": "Test agent",
        "active": True
    }

def test_create_agent(agent_data):
    """Test creating a new agent"""
    response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    assert response.status_code == 201
    data = response.json()
    assert data["status"] == 201
    assert "data" in data

def test_get_all_agents():
    """Test retrieving all agents"""
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "data" in data
    assert "content" in data["data"]
    assert isinstance(data["data"]["content"], list)

def test_get_agent_by_id():
    """Test retrieving agent by ID"""
    # Get existing agents first
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    agents = response.json()["data"]["content"]
    
    if agents:
        agent_id = agents[0]["id"]
        response = requests.get(f'{BASE_URL}/agents/{agent_id}', headers=HEADERS)
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == 200
        assert "data" in data
        assert data["data"]["id"] == agent_id

def test_update_agent():
    """Test updating an existing agent"""
    # Get existing agents first
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    agents = response.json()["data"]["content"]
    
    if agents:
        agent_id = agents[0]["id"]
        update_data = {
            "firstName": "Updated",
            "lastName": "Name",
            "email": "updated@example.com",
            "phone": "+9876543210",
            "status": "ACTIVE"
        }
        
        response = requests.put(f'{BASE_URL}/agents/{agent_id}', json=update_data, headers=HEADERS)
        assert response.status_code == 200

def test_update_agent_status():
    """Test updating agent status"""
    # Get existing agents first
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    agents = response.json()["data"]["content"]
    
    if agents:
        agent_id = agents[0]["id"]
        
        response = requests.patch(f'{BASE_URL}/agents/{agent_id}/status?status=INACTIVE', headers=HEADERS)
        assert response.status_code == 200
        
        # Update back to ACTIVE
        response = requests.patch(f'{BASE_URL}/agents/{agent_id}/status?status=ACTIVE', headers=HEADERS)
        assert response.status_code == 200

def test_create_policy(agent_data):
    """Test creating a new policy"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    
    if create_response.status_code == 201:
        agent_id = create_response.json()["data"]["id"]
        
        policy_data = {
            "policyNumber": "POL-TEST-001",
            "policyType": "Auto Insurance",
            "agentId": agent_id,
            "premium": 1000.00,
            "coverageAmount": 50000.00,
            "status": "ACTIVE",
            "effectiveDate": "2023-01-01",
            "expirationDate": "2023-12-31",
            "groupName": "Test Group",
            "groupNumber": "GRP-001"
        }
        
        response = requests.post(f'{BASE_URL}/policies', json=policy_data, headers=HEADERS)
        assert response.status_code == 201
    else:
        pytest.skip("Could not create agent for policy test")

def test_get_all_policies():
    """Test retrieving all policies"""
    response = requests.get(f'{BASE_URL}/policies', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "data" in data
    assert "content" in data["data"]

def test_create_commission():
    """Test creating a commission"""
    # Get existing agent and policy
    agents_response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    agents = agents_response.json()["data"]["content"]
    
    policies_response = requests.get(f'{BASE_URL}/policies', headers=HEADERS)
    policies = policies_response.json()["data"]["content"]
    
    if agents and policies:
        # Create a new policy for this test to avoid conflicts
        policy_data = {
            "policyNumber": f"POL-COMM-{generate_unique_code()}",
            "policyType": "Auto Insurance",
            "agentId": agents[0]["id"],
            "premium": 1000.00,
            "coverageAmount": 50000.00,
            "status": "ACTIVE",
            "effectiveDate": "2025-01-01",  # Use current year
            "expirationDate": "2025-12-31",
            "groupName": "Test Group",
            "groupNumber": f"GRP-{generate_unique_code()}"
        }
        
        policy_response = requests.post(f'{BASE_URL}/policies', json=policy_data, headers=HEADERS)
        
        if policy_response.status_code == 201:
            new_policy_id = policy_response.json()["data"]["id"]
            
            commission_data = {
                "agentId": agents[0]["id"],
                "policyId": new_policy_id,
                "commissionType": "PERCENTAGE",
                "commissionRate": 0.15,
                "status": "PENDING"
            }
            
            response = requests.post(f'{BASE_URL}/commissions', json=commission_data, headers=HEADERS)
            assert response.status_code == 201
        else:
            pytest.skip("Could not create policy for commission test")
    else:
        pytest.skip("No agents or policies available for commission test")

def test_get_all_commissions():
    """Test retrieving all commissions"""
    response = requests.get(f'{BASE_URL}/commissions', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "data" in data

def test_update_commission_status():
    """Test updating commission status"""
    # Get existing commissions
    response = requests.get(f'{BASE_URL}/commissions', headers=HEADERS)
    
    if response.status_code == 200:
        commissions = response.json()["data"].get("content", [])
        
        if commissions:
            commission_id = commissions[0]["id"]
            
            # Update to APPROVED
            response = requests.patch(f'{BASE_URL}/commissions/{commission_id}/status?status=APPROVED', headers=HEADERS)
            assert response.status_code == 200
            
            # Update to PAID
            response = requests.patch(f'{BASE_URL}/commissions/{commission_id}/status?status=PAID', headers=HEADERS)
            assert response.status_code == 200
        else:
            pytest.skip("No commissions available")
    else:
        pytest.skip("Could not retrieve commissions")

def test_create_payment():
    """Test creating a payment"""
    # Get existing commissions with PAID status
    response = requests.get(f'{BASE_URL}/commissions', headers=HEADERS)
    
    if response.status_code == 200:
        commissions = response.json()["data"].get("content", [])
        
        # Find a paid commission
        paid_commission = None
        for commission in commissions:
            if commission.get("status") == "PAID":
                paid_commission = commission
                break
        
        if paid_commission:
            payment_data = {
                "agentId": paid_commission["agent"]["id"],
                "commissionId": paid_commission["id"],
                "paymentAmount": 150.00,
                "paymentMethod": "BANK_TRANSFER",
                "paymentDate": "2023-06-16",
                "status": "PENDING",
                "bankName": "Test Bank",
                "accountNumber": "1234567890",
                "transactionId": "TXN-TEST-001",
                "notes": "Test payment"
            }
            
            response = requests.post(f'{BASE_URL}/payments', json=payment_data, headers=HEADERS)
            assert response.status_code == 201
        else:
            pytest.skip("No paid commissions available for payment test")
    else:
        pytest.skip("Could not retrieve commissions")

def test_get_all_payments():
    """Test retrieving all payments"""
    response = requests.get(f'{BASE_URL}/payments', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "data" in data

def test_soft_delete_agent():
    """Test soft deleting an agent"""
    # Create a test agent first
    agent_data = {
        "agentCode": generate_unique_code("DELETE"),
        "firstName": "Delete",
        "lastName": "Me",
        "email": f"delete.test.{generate_unique_code()}@example.com",
        "phone": "+1234567890",
        "status": "ACTIVE",
        "active": True
    }
    
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    
    if create_response.status_code == 201:
        agent_id = create_response.json()["data"]["id"]
        
        # Soft delete
        response = requests.delete(f'{BASE_URL}/agents/{agent_id}', headers=HEADERS)
        assert response.status_code == 204
        
        # Verify it's soft deleted
        response = requests.get(f'{BASE_URL}/agents/{agent_id}', headers=HEADERS)
        data = response.json()
        assert data["data"]["active"] == False
    else:
        pytest.skip("Could not create agent for delete test")

def test_api_health_check():
    """Test API health check endpoint"""
    response = requests.get(f'{BASE_URL.replace('/api/v1', '')}/actuator/health')
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"

def test_invalid_endpoint():
    """Test accessing invalid endpoint"""
    response = requests.get(f'{BASE_URL}/invalid', headers=HEADERS)
    assert response.status_code == 404

def test_create_agent_duplicate_email():
    """Test creating agent with duplicate email should fail"""
    # Get existing agent
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    agents = response.json()["data"]["content"]
    
    if agents:
        existing_agent = agents[0]
        duplicate_data = {
            "agentCode": "DUPLICATE001",
            "firstName": "Duplicate",
            "lastName": "Test",
            "email": existing_agent["email"],  # Use existing email
            "phone": "+1234567890",
            "status": "ACTIVE",
            "active": True
        }
        
        response = requests.post(f'{BASE_URL}/agents', json=duplicate_data, headers=HEADERS)
        assert response.status_code == 409
    else:
        pytest.skip("No existing agents to test duplicate email")
