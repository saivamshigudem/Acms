package com.acms.integration;

import com.acms.dto.AgentDTO;
import com.acms.models.Agent;
import com.acms.repositories.AgentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AgentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AgentDTO agentDTO;

    @BeforeEach
    public void setUp() {
        agentDTO = new AgentDTO();
        agentDTO.setName("Integration Test Agent");
        agentDTO.setEmail("integration@example.com");
        agentDTO.setPhoneNumber("+1234567890");
        agentDTO.setCommissionTier(new BigDecimal("5.50"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testCreateAndRetrieveAgent() throws Exception {
        // Create agent
        var createResult = mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agentDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        AgentDTO createdAgent = objectMapper.readValue(response, AgentDTO.class);

        // Retrieve agent
        mockMvc.perform(get("/api/agents/" + createdAgent.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Test Agent"))
                .andExpect(jsonPath("$.email").value("integration@example.com"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testUpdateAgent() throws Exception {
        // Create agent first
        Agent agent = new Agent();
        agent.setName("Original Name");
        agent.setEmail("original@example.com");
        agent.setPhoneNumber("+1234567890");
        agent.setCommissionTier(new BigDecimal("5.50"));
        agent = agentRepository.save(agent);

        // Update agent
        agentDTO.setId(agent.getId());
        agentDTO.setName("Updated Name");

        mockMvc.perform(put("/api/agents/" + agent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteAgent() throws Exception {
        // Create agent first
        Agent agent = new Agent();
        agent.setName("To Delete");
        agent.setEmail("delete@example.com");
        agent.setPhoneNumber("+1234567890");
        agent.setCommissionTier(new BigDecimal("5.50"));
        agent = agentRepository.save(agent);

        // Delete agent
        mockMvc.perform(delete("/api/agents/" + agent.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Agent deletedAgent = agentRepository.findById(agent.getId()).orElse(null);
        assert deletedAgent != null;
        assert deletedAgent.isDeleted();
    }
}
