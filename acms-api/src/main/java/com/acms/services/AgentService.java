package com.acms.services;

import com.acms.dto.AgentDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.models.Agent;
import com.acms.repositories.AgentRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AgentService {
    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);
    
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;
    
    @Autowired
    public AgentService(AgentRepository agentRepository, ModelMapper modelMapper) {
        this.agentRepository = agentRepository;
        this.modelMapper = modelMapper;
    }
    
    @Cacheable(value = "agents", key = "#id")
    public AgentDTO getAgentById(Long id) {
        logger.info("Fetching agent with id: {}", id);
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        return convertToDto(agent);
    }
    
    @Cacheable(value = "agents", key = "#email")
    public AgentDTO getAgentByEmail(String email) {
        logger.info("Fetching agent with email: {}", email);
        Agent agent = agentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with email: " + email));
        return convertToDto(agent);
    }
    
    public Page<AgentDTO> searchAgents(String query, Pageable pageable) {
        logger.info("Searching agents with query: {}", query);
        return agentRepository.searchAgents(query, pageable)
                .map(this::convertToDto);
    }
    
    @CacheEvict(value = "agents", allEntries = true)
    public AgentDTO createAgent(AgentDTO agentDTO) {
        logger.info("Creating new agent with email: {}", agentDTO.getEmail());
        
        if (agentRepository.existsByEmail(agentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        Agent agent = convertToEntity(agentDTO);
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        
        Agent savedAgent = agentRepository.save(agent);
        logger.info("Created agent with id: {}", savedAgent.getId());
        
        return convertToDto(savedAgent);
    }
    
    @CacheEvict(value = "agents", key = "#id")
    public AgentDTO updateAgent(Long id, AgentDTO agentDTO) {
        logger.info("Updating agent with id: {}", id);
        
        Agent existingAgent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        
        if (!existingAgent.getEmail().equals(agentDTO.getEmail()) && 
            agentRepository.existsByEmail(agentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        modelMapper.map(agentDTO, existingAgent);
        existingAgent.setUpdatedAt(LocalDateTime.now());
        
        Agent updatedAgent = agentRepository.save(existingAgent);
        logger.info("Updated agent with id: {}", updatedAgent.getId());
        
        return convertToDto(updatedAgent);
    }
    
    @CacheEvict(value = "agents", key = "#id")
    public void deleteAgent(Long id) {
        logger.info("Deleting agent with id: {}", id);
        
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        
        agent.setDeleted(true);
        agent.setUpdatedAt(LocalDateTime.now());
        agentRepository.save(agent);
        
        logger.info("Soft deleted agent with id: {}", id);
    }
    
    private AgentDTO convertToDto(Agent agent) {
        return modelMapper.map(agent, AgentDTO.class);
    }
    
    private Agent convertToEntity(AgentDTO agentDTO) {
        return modelMapper.map(agentDTO, Agent.class);
    }
}
