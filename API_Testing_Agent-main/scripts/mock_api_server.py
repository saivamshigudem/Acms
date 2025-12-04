"""
Mock API Server for Testing
Run this server to test the generated integration tests.

Usage:
    python mock_api_server.py
"""

from flask import Flask, request, jsonify
import threading
import time
import sys

app = Flask(__name__)

# Mock data storage
agents = {
    "agent-123": {
        "id": "agent-123",
        "name": "John Doe",
        "email": "john@example.com",
        "commissionTier": 10,
        "status": "active",
        "createdDate": "2023-01-01T00:00:00Z",
        "lastModifiedDate": "2023-01-01T00:00:00Z"
    },
    "agent-456": {
        "id": "agent-456",
        "name": "Jane Smith",
        "email": "jane@example.com",
        "commissionTier": 15,
        "status": "active",
        "createdDate": "2023-01-02T00:00:00Z",
        "lastModifiedDate": "2023-01-02T00:00:00Z"
    }
}

# Global counter for new agents
agent_counter = 1000


def check_auth():
    """Check if authorization header is present and valid."""
    auth_header = request.headers.get('Authorization')
    if not auth_header:
        return False, "Missing authorization header"
    
    # Simple token validation (in production, use proper JWT validation)
    if auth_header == "Bearer valid-token-123":
        return True, None
    elif auth_header == "Bearer expired-token":
        return False, "Token expired"
    elif auth_header == "Bearer invalid-token":
        return False, "Invalid token"
    else:
        return False, "Invalid authorization token"


@app.route('/agents', methods=['GET'])
def list_agents():
    """List all agents."""
    is_valid, error = check_auth()
    if not is_valid:
        return jsonify({"error": "Unauthorized", "message": error}), 401
    
    # Handle query parameters
    limit = request.args.get('limit', 10, type=int)
    offset = request.args.get('offset', 0, type=int)
    
    agent_list = list(agents.values())
    
    # Apply pagination
    paginated = agent_list[offset:offset + limit]
    
    return jsonify(paginated)


@app.route('/agents', methods=['POST'])
def create_agent():
    """Create a new agent."""
    is_valid, error = check_auth()
    if not is_valid:
        return jsonify({"error": "Unauthorized", "message": error}), 401
    
    data = request.get_json()
    
    # Validation
    if not data:
        return jsonify({"error": "Validation failed", "details": ["Request body is required"]}), 400
    
    name = data.get('name')
    email = data.get('email')
    commission_tier = data.get('commissionTier')
    
    errors = []
    if not name or not name.strip():
        errors.append("Name cannot be empty")
    if not email or '@' not in email:
        errors.append("Invalid email format")
    if commission_tier is None or commission_tier < 0 or commission_tier > 100:
        errors.append("Commission tier must be between 0 and 100")
    
    if errors:
        return jsonify({"error": "Validation failed", "details": errors}), 400
    
    # Create new agent
    global agent_counter
    agent_counter += 1
    agent_id = f"agent-{agent_counter}"
    
    new_agent = {
        "id": agent_id,
        "name": name.strip(),
        "email": email.strip(),
        "commissionTier": commission_tier,
        "status": "active",
        "createdDate": "2023-01-01T00:00:00Z",
        "lastModifiedDate": "2023-01-01T00:00:00Z"
    }
    
    agents[agent_id] = new_agent
    return jsonify(new_agent), 201


@app.route('/agents/<agent_id>', methods=['GET'])
def get_agent(agent_id):
    """Get agent by ID."""
    is_valid, error = check_auth()
    if not is_valid:
        return jsonify({"error": "Unauthorized", "message": error}), 401
    
    agent = agents.get(agent_id)
    if not agent:
        return jsonify({"error": "Agent not found", "message": f"Agent with ID '{agent_id}' not found"}), 404
    
    return jsonify(agent)


@app.route('/agents/<agent_id>', methods=['PUT'])
def update_agent(agent_id):
    """Update agent."""
    is_valid, error = check_auth()
    if not is_valid:
        return jsonify({"error": "Unauthorized", "message": error}), 401
    
    agent = agents.get(agent_id)
    if not agent:
        return jsonify({"error": "Agent not found", "message": f"Agent with ID '{agent_id}' not found"}), 404
    
    data = request.get_json()
    if not data:
        return jsonify({"error": "Validation failed", "details": ["Request body is required"]}), 400
    
    # Update fields
    if 'name' in data:
        name = data['name']
        if not name or not name.strip():
            return jsonify({"error": "Validation failed", "details": ["Name cannot be empty"]}), 400
        agent['name'] = name.strip()
    
    if 'email' in data:
        email = data['email']
        if not email or '@' not in email:
            return jsonify({"error": "Validation failed", "details": ["Invalid email format"]}), 400
        agent['email'] = email.strip()
    
    if 'commissionTier' in data:
        commission_tier = data['commissionTier']
        if commission_tier is None or commission_tier < 0 or commission_tier > 100:
            return jsonify({"error": "Validation failed", "details": ["Commission tier must be between 0 and 100"]}), 400
        agent['commissionTier'] = commission_tier
    
    if 'status' in data:
        status = data['status']
        if status not in ['active', 'inactive']:
            return jsonify({"error": "Validation failed", "details": ["Status must be 'active' or 'inactive'"]}), 400
        agent['status'] = status
    
    agent['lastModifiedDate'] = "2023-01-02T00:00:00Z"
    
    return jsonify(agent)


@app.route('/agents/<agent_id>', methods=['DELETE'])
def delete_agent(agent_id):
    """Delete agent."""
    is_valid, error = check_auth()
    if not is_valid:
        return jsonify({"error": "Unauthorized", "message": error}), 401
    
    agent = agents.get(agent_id)
    if not agent:
        return jsonify({"error": "Agent not found", "message": f"Agent with ID '{agent_id}' not found"}), 404
    
    del agents[agent_id]
    return "", 204


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint."""
    return jsonify({"status": "healthy", "agents_count": len(agents)})


def run_server():
    """Run the mock server."""
    print("🚀 Starting Mock API Server...")
    print("📍 Server will be available at: http://localhost:8080")
    print("🔧 Health check: http://localhost:8080/health")
    print("📝 Agents endpoint: http://localhost:8080/agents")
    print("🔑 Use 'Bearer valid-token-123' for valid authentication")
    print("⏹️  Press Ctrl+C to stop the server")
    print("-" * 50)
    
    # Run the Flask app
    app.run(host='localhost', port=8080, debug=False)


if __name__ == '__main__':
    try:
        run_server()
    except KeyboardInterrupt:
        print("\n🛑 Mock API Server stopped")
    except Exception as e:
        print(f"❌ Error starting server: {e}")
        sys.exit(1)
