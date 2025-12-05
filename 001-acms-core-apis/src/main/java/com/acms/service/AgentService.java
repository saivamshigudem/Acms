package com.acms.service;

import com.acms.dto.AgentDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.mapper.AgentMapper;
import com.acms.model.Agent;
import com.acms.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.acms.config.CacheConfig.AGENT_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    @Transactional(readOnly = true)
    public Page<AgentDTO> getAllAgents(Pageable pageable) {
        return agentRepository.findAll(pageable)
                .map(agentMapper::toDto);
    }

    @Cacheable(value = AGENT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public AgentDTO getAgentById(Long id) {
        log.info("Fetching agent with id: {}", id);
        return agentRepository.findById(id)
                .map(agentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
    }

    @Cacheable(value = AGENT_CACHE, key = "#agentCode")
    @Transactional(readOnly = true)
    public AgentDTO getAgentByCode(String agentCode) {
        log.info("Fetching agent with code: {}", agentCode);
        return agentRepository.findByAgentCode(agentCode)
                .map(agentMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with code: " + agentCode));
    }

    @Transactional
    public AgentDTO createAgent(AgentDTO agentDTO) {
        log.info("Creating new agent with code: {}", agentDTO.getAgentCode());
        if (agentRepository.existsByAgentCode(agentDTO.getAgentCode())) {
            throw new IllegalArgumentException("Agent code already exists: " + agentDTO.getAgentCode());
        }
        if (agentRepository.existsByEmail(agentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + agentDTO.getEmail());
        }
        
        Agent agent = agentMapper.toEntity(agentDTO);
        Agent savedAgent = agentRepository.save(agent);
        log.info("Created agent with id: {}", savedAgent.getId());
        return agentMapper.toDto(savedAgent);
    }

    @CachePut(value = AGENT_CACHE, key = "#id")
    @Transactional
    public AgentDTO updateAgent(Long id, AgentDTO agentDTO) {
        log.info("Updating agent with id: {}", id);
        Agent existingAgent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));

        // Prevent updating agent code
        if (!existingAgent.getAgentCode().equals(agentDTO.getAgentCode())) {
            throw new IllegalArgumentException("Agent code cannot be changed");
        }

        // Prevent email duplication
        if (!existingAgent.getEmail().equals(agentDTO.getEmail()) && 
            agentRepository.existsByEmail(agentDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + agentDTO.getEmail());
        }

        agentMapper.updateAgentFromDto(agentDTO, existingAgent);
        Agent updatedAgent = agentRepository.save(existingAgent);
        log.info("Updated agent with id: {}", id);
        return agentMapper.toDto(updatedAgent);
    }

    @CacheEvict(value = AGENT_CACHE, key = "#id")
    @Transactional
    public void deleteAgent(Long id) {
        log.info("Deleting agent with id: {}", id);
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        
        agent.setActive(false);
        agentRepository.save(agent);
        log.info("Soft deleted agent with id: {}", id);
    }

    @CacheEvict(value = AGENT_CACHE, key = "#id")
    @Transactional
    public AgentDTO updateAgentStatus(Long id, Agent.AgentStatus status) {
        log.info("Updating status to {} for agent with id: {}", status, id);
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + id));
        
        agent.setStatus(status);
        Agent updatedAgent = agentRepository.save(agent);
        log.info("Updated status to {} for agent with id: {}", status, id);
        return agentMapper.toDto(updatedAgent);
    }

    @Transactional(readOnly = true)
    public Page<AgentDTO> searchAgents(String query, Pageable pageable) {
        log.info("Searching agents with query: {}", query);
        return agentRepository.search(query, pageable)
                .map(agentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AgentDTO> getAgentsByStatus(Agent.AgentStatus status, Pageable pageable) {
        log.info("Fetching agents with status: {}", status);
        return agentRepository.findByStatus(status, pageable)
                .map(agentMapper::toDto);
    }
}
