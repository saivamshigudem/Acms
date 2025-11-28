package com.acms.controller;

import com.acms.controllers.AgentController;
import com.acms.dto.AgentDTO;
import com.acms.models.Agent;
import com.acms.services.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Autowired
    private ObjectMapper objectMapper;

    private AgentDTO agentDTO;

    @BeforeEach
    public void setUp() {
        agentDTO = new AgentDTO();
        agentDTO.setId(1L);
        agentDTO.setName("John Doe");
        agentDTO.setEmail("john@example.com");
        agentDTO.setPhoneNumber("+1234567890");
        agentDTO.setCommissionTier(new BigDecimal("5.50"));
        agentDTO.setStatus(Agent.AgentStatus.ACTIVE);
        agentDTO.setCreatedAt(LocalDateTime.now());
        agentDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAgentById() throws Exception {
        when(agentService.getAgentById(1L)).thenReturn(agentDTO);

        mockMvc.perform(get("/api/agents/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testSearchAgents() throws Exception {
        Page<AgentDTO> page = new PageImpl<>(Arrays.asList(agentDTO), PageRequest.of(0, 20), 1);
        when(agentService.searchAgents(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/agents?query=john")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testCreateAgent() throws Exception {
        when(agentService.createAgent(any())).thenReturn(agentDTO);

        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    public void testUpdateAgent() throws Exception {
        when(agentService.updateAgent(eq(1L), any())).thenReturn(agentDTO);

        mockMvc.perform(put("/api/agents/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteAgent() throws Exception {
        mockMvc.perform(delete("/api/agents/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
