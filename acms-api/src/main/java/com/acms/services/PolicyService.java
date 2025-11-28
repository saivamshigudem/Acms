package com.acms.services;

import com.acms.dto.PolicyDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.models.Agent;
import com.acms.models.Policy;
import com.acms.repositories.AgentRepository;
import com.acms.repositories.PolicyRepository;
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
public class PolicyService {
    private static final Logger logger = LoggerFactory.getLogger(PolicyService.class);
    
    private final PolicyRepository policyRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;
    
    @Autowired
    public PolicyService(PolicyRepository policyRepository, AgentRepository agentRepository, ModelMapper modelMapper) {
        this.policyRepository = policyRepository;
        this.agentRepository = agentRepository;
        this.modelMapper = modelMapper;
    }
    
    @Cacheable(value = "policies", key = "#id")
    public PolicyDTO getPolicyById(Long id) {
        logger.info("Fetching policy with id: {}", id);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        return convertToDto(policy);
    }
    
    public Page<PolicyDTO> searchPolicies(String query, Pageable pageable) {
        logger.info("Searching policies with query: {}", query);
        return policyRepository.searchPolicies(query, pageable)
                .map(this::convertToDto);
    }
    
    public Page<PolicyDTO> getPoliciesByAgent(Long agentId, Pageable pageable) {
        logger.info("Fetching policies for agent: {}", agentId);
        return policyRepository.findByAgentId(agentId, pageable)
                .map(this::convertToDto);
    }
    
    @CacheEvict(value = "policies", allEntries = true)
    public PolicyDTO createPolicy(PolicyDTO policyDTO) {
        logger.info("Creating new policy for agent: {}", policyDTO.getAgentId());
        
        Agent agent = agentRepository.findById(policyDTO.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + policyDTO.getAgentId()));
        
        if (policyDTO.getEffectiveDate().isAfter(policyDTO.getExpirationDate())) {
            throw new IllegalArgumentException("Effective date must be before expiration date");
        }
        
        Policy policy = convertToEntity(policyDTO);
        policy.setAgent(agent);
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        
        Policy savedPolicy = policyRepository.save(policy);
        logger.info("Created policy with id: {}", savedPolicy.getId());
        
        return convertToDto(savedPolicy);
    }
    
    @CacheEvict(value = "policies", key = "#id")
    public PolicyDTO updatePolicy(Long id, PolicyDTO policyDTO) {
        logger.info("Updating policy with id: {}", id);
        
        Policy existingPolicy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        
        if (policyDTO.getEffectiveDate().isAfter(policyDTO.getExpirationDate())) {
            throw new IllegalArgumentException("Effective date must be before expiration date");
        }
        
        modelMapper.map(policyDTO, existingPolicy);
        existingPolicy.setUpdatedAt(LocalDateTime.now());
        
        Policy updatedPolicy = policyRepository.save(existingPolicy);
        logger.info("Updated policy with id: {}", updatedPolicy.getId());
        
        return convertToDto(updatedPolicy);
    }
    
    @CacheEvict(value = "policies", key = "#id")
    public void deletePolicy(Long id) {
        logger.info("Deleting policy with id: {}", id);
        
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        
        policy.setDeleted(true);
        policy.setUpdatedAt(LocalDateTime.now());
        policyRepository.save(policy);
        
        logger.info("Soft deleted policy with id: {}", id);
    }
    
    private PolicyDTO convertToDto(Policy policy) {
        return modelMapper.map(policy, PolicyDTO.class);
    }
    
    private Policy convertToEntity(PolicyDTO policyDTO) {
        return modelMapper.map(policyDTO, Policy.class);
    }
}
