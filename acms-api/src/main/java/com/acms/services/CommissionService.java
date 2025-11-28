package com.acms.services;

import com.acms.dto.CommissionDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.models.Agent;
import com.acms.models.Commission;
import com.acms.models.Policy;
import com.acms.repositories.AgentRepository;
import com.acms.repositories.CommissionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class CommissionService {
    private static final Logger logger = LoggerFactory.getLogger(CommissionService.class);
    
    private final CommissionRepository commissionRepository;
    private final PolicyRepository policyRepository;
    private final AgentRepository agentRepository;
    private final ModelMapper modelMapper;
    
    @Autowired
    public CommissionService(CommissionRepository commissionRepository, PolicyRepository policyRepository,
                            AgentRepository agentRepository, ModelMapper modelMapper) {
        this.commissionRepository = commissionRepository;
        this.policyRepository = policyRepository;
        this.agentRepository = agentRepository;
        this.modelMapper = modelMapper;
    }
    
    @Cacheable(value = "commissions", key = "#id")
    public CommissionDTO getCommissionById(Long id) {
        logger.info("Fetching commission with id: {}", id);
        Commission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
        return convertToDto(commission);
    }
    
    public Page<CommissionDTO> getCommissionsByAgent(Long agentId, Pageable pageable) {
        logger.info("Fetching commissions for agent: {}", agentId);
        return commissionRepository.findByAgentId(agentId, pageable)
                .map(this::convertToDto);
    }
    
    public Page<CommissionDTO> getCommissionsByPolicy(Long policyId, Pageable pageable) {
        logger.info("Fetching commissions for policy: {}", policyId);
        return commissionRepository.findByPolicyId(policyId, pageable)
                .map(this::convertToDto);
    }
    
    @CacheEvict(value = "commissions", allEntries = true)
    public CommissionDTO calculateCommission(Long policyId, Long agentId) {
        logger.info("Calculating commission for policy: {} and agent: {}", policyId, agentId);
        
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        
        BigDecimal commissionAmount = policy.getPremium()
                .multiply(agent.getCommissionTier())
                .divide(BigDecimal.valueOf(100));
        
        Commission commission = new Commission();
        commission.setPolicy(policy);
        commission.setAgent(agent);
        commission.setCommissionTier(agent.getCommissionTier());
        commission.setCalculatedAmount(commissionAmount);
        commission.setCalculationDate(LocalDateTime.now());
        commission.setStatus(Commission.CommissionStatus.CALCULATED);
        commission.setCreatedAt(LocalDateTime.now());
        commission.setUpdatedAt(LocalDateTime.now());
        
        Commission savedCommission = commissionRepository.save(commission);
        logger.info("Created commission with id: {} and amount: {}", savedCommission.getId(), commissionAmount);
        
        return convertToDto(savedCommission);
    }
    
    @CacheEvict(value = "commissions", key = "#id")
    public CommissionDTO updateCommission(Long id, CommissionDTO commissionDTO) {
        logger.info("Updating commission with id: {}", id);
        
        Commission existingCommission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
        
        modelMapper.map(commissionDTO, existingCommission);
        existingCommission.setUpdatedAt(LocalDateTime.now());
        
        Commission updatedCommission = commissionRepository.save(existingCommission);
        logger.info("Updated commission with id: {}", updatedCommission.getId());
        
        return convertToDto(updatedCommission);
    }
    
    @CacheEvict(value = "commissions", key = "#id")
    public void deleteCommission(Long id) {
        logger.info("Deleting commission with id: {}", id);
        
        Commission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
        
        commission.setDeleted(true);
        commission.setUpdatedAt(LocalDateTime.now());
        commissionRepository.save(commission);
        
        logger.info("Soft deleted commission with id: {}", id);
    }
    
    private CommissionDTO convertToDto(Commission commission) {
        return modelMapper.map(commission, CommissionDTO.class);
    }
    
    private Commission convertToEntity(CommissionDTO commissionDTO) {
        return modelMapper.map(commissionDTO, Commission.class);
    }
}
