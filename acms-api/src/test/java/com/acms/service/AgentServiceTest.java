package com.acms.service;

import com.acms.dto.AgentDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.models.Agent;
import com.acms.repositories.AgentRepository;
import com.acms.services.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AgentService agentService;

    private Agent agent;
    private AgentDTO agentDTO;

    @BeforeEach
    public void setUp() {
        agent = new Agent();
        agent.setId(1L);
        agent.setName("John Doe");
        agent.setEmail("john@example.com");
        agent.setPhoneNumber("+1234567890");
        agent.setCommissionTier(new BigDecimal("5.50"));

        agentDTO = new AgentDTO();
        agentDTO.setId(1L);
        agentDTO.setName("John Doe");
        agentDTO.setEmail("john@example.com");
        agentDTO.setPhoneNumber("+1234567890");
        agentDTO.setCommissionTier(new BigDecimal("5.50"));
    }

    @Test
    public void testGetAgentById_Success() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(modelMapper.map(agent, AgentDTO.class)).thenReturn(agentDTO);

        AgentDTO result = agentService.getAgentById(1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(agentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAgentById_NotFound() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agentService.getAgentById(1L));
    }

    @Test
    public void testCreateAgent_Success() {
        when(agentRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(modelMapper.map(agentDTO, Agent.class)).thenReturn(agent);
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);
        when(modelMapper.map(agent, AgentDTO.class)).thenReturn(agentDTO);

        AgentDTO result = agentService.createAgent(agentDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(agentRepository, times(1)).save(any(Agent.class));
    }

    @Test
    public void testCreateAgent_EmailAlreadyExists() {
        when(agentRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> agentService.createAgent(agentDTO));
    }

    @Test
    public void testUpdateAgent_Success() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);
        when(modelMapper.map(agent, AgentDTO.class)).thenReturn(agentDTO);

        AgentDTO result = agentService.updateAgent(1L, agentDTO);

        assertNotNull(result);
        verify(agentRepository, times(1)).save(any(Agent.class));
    }

    @Test
    public void testDeleteAgent_Success() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);

        agentService.deleteAgent(1L);

        assertTrue(agent.isDeleted());
        verify(agentRepository, times(1)).save(any(Agent.class));
    }
}
