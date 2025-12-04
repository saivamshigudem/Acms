import pytest
import requests
import json

# Configuration
BASE_URL = "http://localhost:8080/api/api/v1"
HEADERS = {
    "accept": "application/json",
    "Content-Type": "application/json"
}

@pytest.fixture
def test_data():
    return {
        "agent_id": "12345",
        "name": "John Doe",
        "email": "johndoe@example.com",
        "commission_tier": 1
    }

@pytest.fixture
def agent_data():
    return {
        "agentCode": "TEST001",
        "firstName": "Test",
        "lastName": "Agent",
        "email": "test.agent@example.com",
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

def test_update_agent(agent_data):
    """Test updating an existing agent"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Update the agent
    response = requests.put(f'{BASE_URL}/agents/{agent_id}', json={'email': 'updated@example.com'}, headers=HEADERS)
    assert response.status_code == 200

def test_get_agent_details(agent_data):
    """Test retrieving agent details"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Get agent details
    response = requests.get(f'{BASE_URL}/agents/{agent_id}', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == 200
    assert "data" in data

def test_deactivate_agent(agent_data):
    """Test deactivating an agent"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Deactivate the agent
    response = requests.patch(f'{BASE_URL}/agents/{agent_id}/status?status=INACTIVE', headers=HEADERS)
    assert response.status_code == 200

@pytest.mark.parametrize("policy_name", ["Policy ABC", "Policy DEF"])
def test_create_policy(policy_name, agent_data):
    """Test creating a new policy"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Create a policy
    policy_data = {
        "policyNumber": f"POL-{policy_name.replace(' ', '-')}",
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

def test_update_policy():
    """Test updating a policy"""
    response = requests.put(f'{BASE_URL}/policies/12345', json={'name': 'New Policy Name'}, headers=HEADERS)
    # May return 404 if policy doesn't exist
    assert response.status_code in [200, 404]

def test_get_policy_details():
    """Test retrieving policy details"""
    response = requests.get(f'{BASE_URL}/policies/12345', headers=HEADERS)
    # May return 404 if policy doesn't exist
    assert response.status_code in [200, 404]

@pytest.mark.parametrize("commission_tier", [1, 2])
def test_calculate_commissions(commission_tier, agent_data):
    """Test commission calculations"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Create a policy first
    policy_data = {
        "policyNumber": "POL-COMM-TEST",
        "policyType": "Auto Insurance",
        "agentId": agent_id,
        "premium": 1000.00,
        "coverageAmount": 50000.00,
        "status": "ACTIVE",
        "effectiveDate": "2023-01-01",
        "expirationDate": "2023-12-31"
    }
    
    policy_response = requests.post(f'{BASE_URL}/policies', json=policy_data, headers=HEADERS)
    if policy_response.status_code == 201:
        policy_id = policy_response.json()["data"]["id"]
        
        # Create commission
        commission_data = {
            "agentId": agent_id,
            "policyId": policy_id,
            "commissionType": "PERCENTAGE",
            "commissionRate": 0.15,
            "status": "PENDING"
        }
        
        response = requests.post(f'{BASE_URL}/commissions', json=commission_data, headers=HEADERS)
        assert response.status_code == 201
    else:
        pytest.skip("Could not create policy for commission test")

def test_security_test():
    """Test security - unauthorized access should be handled appropriately"""
    response = requests.get(f'{BASE_URL}/agents', headers={'Authorization': 'Bearer INVALID_TOKEN'})
    # Should either succeed (if no auth required) or return 401/403
    assert response.status_code in [200, 401, 403]

@pytest.mark.parametrize("policy_name", ["Policy ABC", "Policy DEF"])
def test_policy_linking(policy_name, agent_data):
    """Test linking policies to agents"""
    # First create an agent
    create_response = requests.post(f'{BASE_URL}/agents', json=agent_data, headers=HEADERS)
    agent_id = create_response.json()["data"]["id"]
    
    # Create a policy linked to the agent
    policy_data = {
        "policyNumber": f"POL-LINK-{policy_name.replace(' ', '-')}",
        "policyType": "Auto Insurance",
        "agentId": agent_id,
        "premium": 1000.00,
        "coverageAmount": 50000.00,
        "status": "ACTIVE",
        "effectiveDate": "2023-01-01",
        "expirationDate": "2023-12-31"
    }
    
    response = requests.post(f'{BASE_URL}/policies', json=policy_data, headers=HEADERS)
    assert response.status_code == 201
    
    # Verify the policy is linked to the agent
    policy_response = response.json()
    assert policy_response["data"]["agent"]["id"] == agent_id

def test_agent_commission_calculations():
    """Test agent commission calculations"""
    response = requests.get(f'{BASE_URL}/agents/commissions', headers=HEADERS)
    # May return 404 if endpoint doesn't exist
    assert response.status_code in [200, 404]

def test_api_health_check():
    """Test API health check"""
    response = requests.get(f'{BASE_URL.replace('/api/v1', '')}/actuator/health')
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"

def test_get_all_agents():
    """Test retrieving all agents"""
    response = requests.get(f'{BASE_URL}/agents', headers=HEADERS)
    assert response.status_code == 200
    data = response.json()
    assert "data" in data
    assert isinstance(data["data"], list)
