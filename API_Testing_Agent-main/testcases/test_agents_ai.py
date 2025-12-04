import pytest
import requests
import os
from dotenv import load_dotenv

load_dotenv()

def create_agent(api_url, agent_data):
    """Helper function to create an agent"""
    response = requests.post(api_url, json=agent_data)
    if response.status_code == 201:
        return response.json()
    return None

@pytest.fixture()
def api_url():
    return "https://api.acms.com/agents"

@pytest.fixture()
def valid_agent_data():
    return {
        "name": "John Doe",
        "email": "john.doe@example.com",
        "commission_tier": 1
    }

@pytest.fixture()
def invalid_agent_data():
    return {
        "name": "",
        "email": None,
        "commission_tier": "invalid"
    }

def test_create_agent(api_url, valid_agent_data):
    response = requests.post(f"{api_url}", json=valid_agent_data)
    assert response.status_code == 201
    assert response.json()["id"]

def test_update_agent(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    updated_data = {"email": "john.doe2@example.com"}
    response = requests.patch(f"{api_url}/{agent_id}", json=updated_data)
    assert response.status_code == 200
    assert response.json()["email"] == updated_data["email"]

def test_get_agent(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    response = requests.get(f"{api_url}/{agent_id}")
    assert response.status_code == 200
    assert response.json() == valid_agent_data

def test_deactivate_agent(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    response = requests.put(f"{api_url}/{agent_id}/deactivate")
    assert response.status_code == 204

def test_create_agent_invalid_data(api_url, invalid_agent_data):
    response = requests.post(f"{api_url}", json=invalid_agent_data)
    assert response.status_code == 400
    assert "name" in response.json()["errors"]
    assert "commission_tier" in response.json()["errors"]

def test_get_agent_nonexistent(api_url):
    response = requests.get(f"{api_url}/999")
    assert response.status_code == 404

def test_update_agent_nonexistent(api_url, valid_agent_data):
    updated_data = {"email": "john.doe2@example.com"}
    response = requests.patch(f"{api_url}/999", json=updated_data)
    assert response.status_code == 404

def test_deactivate_agent_nonexistent(api_url, valid_agent_data):
    response = requests.put(f"{api_url}/{valid_agent_data['id']}/deactivate")
    assert response.status_code == 404

def test_create_agent_unauthorized(api_url, valid_agent_data):
    response = requests.post(f"{api_url}", json=valid_agent_data, headers={"Authorization": "Bearer invalid_token"})
    assert response.status_code == 401

def test_update_agent_unauthorized(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    updated_data = {"email": "john.doe2@example.com"}
    response = requests.patch(f"{api_url}/{agent_id}", json=updated_data, headers={"Authorization": "Bearer invalid_token"})
    assert response.status_code == 401

def test_get_agent_unauthorized(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    response = requests.get(f"{api_url}/{agent_id}", headers={"Authorization": "Bearer invalid_token"})
    assert response.status_code == 401

def test_deactivate_agent_unauthorized(api_url, valid_agent_data):
    agent_id = create_agent(api_url, valid_agent_data)["id"]
    response = requests.put(f"{api_url}/{agent_id}/deactivate", headers={"Authorization": "Bearer invalid_token"})
    assert response.status_code == 401