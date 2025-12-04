import pytest

@pytest.fixture
def acms_url():
    return "https://acms.com/api/v1"

@pytest.fixture
def agent_data():
    return {
        "name": "John Doe",
        "email": "johndoe@example.com",
        "commission_tier": 2,
        "status": "active"
    }

@pytest.fixture
def policy_data():
    return {
        "policy_name": "Group Life Insurance",
        "coverage_type": "life",
        "premium_amount": 500.00,
        "effective_date": "2025-01-01",
        "agent_id": 123
    }

def test_create_agent_happy_path(acms_url, agent_data):
    response = requests.post(f"{acms_url}/agents", json=agent_data)
    assert response.status_code == 201

def test_create_agent_invalid_name(acms_url, agent_data):
    agent_data["name"] = "Invalid Name"
    response = requests.post(f"{acms_url}/agents", json=agent_data)
    assert response.status_code == 400

def test_get_agent_details_happy_path(acms_url, agent_data):
    response = requests.get(f"{acms_url}/agents/{agent_data['id']}")
    assert response.status_code == 200

def test_get_agent_details_not_found(acms_url, agent_data):
    response = requests.get(f"{acms_url}/agents/123456")
    assert response.status_code == 404

def test_update_agent_happy_path(acms_url, agent_data):
    updated_name = "John Smith"
    response = requests.patch(f"{acms_url}/agents/{agent_data['id']}", json={"name": updated_name})
    assert response.status_code == 200

def test_update_agent_invalid_email(acms_url, agent_data):
    agent_data["email"] = "Invalid Email"
    response = requests.patch(f"{acms_url}/agents/{agent_data['id']}", json=agent_data)
    assert response.status_code == 400

def test_deactivate_agent_happy_path(acms_url, agent_data):
    response = requests.put(f"{acms_url}/agents/{agent_data['id']}/deactivate")
    assert response.status_code == 200

def test_deactivate_agent_not_found(acms_url, agent_data):
    response = requests.put(f"{acms_url}/agents/123456/deactivate")
    assert response.status_code == 404

def test_policy_create_happy_path(acms_url, policy_data):
    response = requests.post(f"{acms_url}/policies", json=policy_data)
    assert response.status_code == 201

def test_policy_create_invalid_coverage_type(acms_url, policy_data):
    policy_data["coverage_type"] = "Invalid Coverage Type"
    response = requests.post(f"{acms_url}/policies", json=policy_data)
    assert response.status_code == 400

def test_policy_get_details_happy_path(acms_url, policy_data):
    response = requests.get(f"{acms_url}/policies/{policy_data['id']}")
    assert response.status_code == 200

def test_policy_get_details_not_found(acms_url, policy_data):
    response = requests.get(f"{acms_url}/policies/123456")
    assert response.status_code == 404

def test_policy_update_happy_path(acms_url, policy_data):
    updated_premium_amount = 1000.00
    response = requests.patch(f"{acms_url}/policies/{policy_data['id']}", json={"premium_amount": updated_premium_amount})
    assert response.status_code == 200

def test_policy_update_invalid_effective_date(acms_url, policy_data):
    policy_data["effective_date"] = "Invalid Date"
    response = requests.patch(f"{acms_url}/policies/{policy_data['id']}", json=policy_data)
    assert response.status_code == 400