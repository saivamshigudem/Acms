-- Create agents table
CREATE TABLE IF NOT EXISTS agents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    commission_rate DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create policies table
CREATE TABLE IF NOT EXISTS policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_number VARCHAR(50) NOT NULL UNIQUE,
    policy_holder_name VARCHAR(100) NOT NULL,
    policy_type VARCHAR(50) NOT NULL,
    premium_amount DECIMAL(15,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    agent_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- Create commissions table
CREATE TABLE IF NOT EXISTS commissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    commission_amount DECIMAL(15,2) NOT NULL,
    commission_rate DECIMAL(5,2) NOT NULL,
    calculation_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (agent_id) REFERENCES agents(id),
    FOREIGN KEY (policy_id) REFERENCES policies(id)
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_reference VARCHAR(50) NOT NULL UNIQUE,
    agent_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- Create payment_commissions join table
CREATE TABLE IF NOT EXISTS payment_commissions (
    payment_id BIGINT NOT NULL,
    commission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    PRIMARY KEY (payment_id, commission_id),
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (commission_id) REFERENCES commissions(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_agents_email ON agents(email);
CREATE INDEX idx_agents_status ON agents(status);
CREATE INDEX idx_policies_agent_id ON policies(agent_id);
CREATE INDEX idx_policies_status ON policies(status);
CREATE INDEX idx_commissions_agent_id ON commissions(agent_id);
CREATE INDEX idx_commissions_policy_id ON commissions(policy_id);
CREATE INDEX idx_commissions_status ON commissions(status);
CREATE INDEX idx_payments_agent_id ON payments(agent_id);
CREATE INDEX idx_payments_status ON payments(status);
