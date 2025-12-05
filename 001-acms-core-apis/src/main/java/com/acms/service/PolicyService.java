package com.acms.service;

import com.acms.dto.PolicyDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.mapper.PolicyMapper;
import com.acms.model.Agent;
import com.acms.model.Policy;
import com.acms.repository.AgentRepository;
import com.acms.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.acms.config.CacheConfig.POLICY_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final AgentRepository agentRepository;
    private final PolicyMapper policyMapper;

    @Transactional(readOnly = true)
    public Page<PolicyDTO> getAllPolicies(Pageable pageable) {
        return policyRepository.findAll(pageable)
                .map(policyMapper::toDto);
    }

    @Cacheable(value = POLICY_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public PolicyDTO getPolicyById(Long id) {
        log.info("Fetching policy with id: {}", id);
        return policyRepository.findById(id)
                .map(policyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
    }

    @Cacheable(value = POLICY_CACHE, key = "#policyNumber")
    @Transactional(readOnly = true)
    public PolicyDTO getPolicyByNumber(String policyNumber) {
        log.info("Fetching policy with number: {}", policyNumber);
        return policyRepository.findByPolicyNumber(policyNumber)
                .map(policyMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with number: " + policyNumber));
    }

    @Transactional
    public PolicyDTO createPolicy(PolicyDTO policyDTO) {
        log.info("Creating new policy with number: {}", policyDTO.getPolicyNumber());
        
        if (policyRepository.existsByPolicyNumber(policyDTO.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number already exists: " + policyDTO.getPolicyNumber());
        }
        
        Agent agent = agentRepository.findById(policyDTO.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + policyDTO.getAgentId()));
        
        Policy policy = policyMapper.toEntity(policyDTO);
        policy.setAgent(agent);
        
        // Validate business rules
        validatePolicyDates(policy);
        
        Policy savedPolicy = policyRepository.save(policy);
        log.info("Created policy with id: {}", savedPolicy.getId());
        return policyMapper.toDto(savedPolicy);
    }

    @CachePut(value = POLICY_CACHE, key = "#id")
    @Transactional
    public PolicyDTO updatePolicy(Long id, PolicyDTO policyDTO) {
        log.info("Updating policy with id: {}", id);
        Policy existingPolicy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));

        // Prevent updating policy number
        if (!existingPolicy.getPolicyNumber().equals(policyDTO.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number cannot be changed");
        }

        // Update agent if changed
        if (!existingPolicy.getAgent().getId().equals(policyDTO.getAgentId())) {
            Agent newAgent = agentRepository.findById(policyDTO.getAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + policyDTO.getAgentId()));
            existingPolicy.setAgent(newAgent);
        }

        policyMapper.updatePolicyFromDto(policyDTO, existingPolicy);
        
        // Validate business rules
        validatePolicyDates(existingPolicy);
        validatePolicyStatusTransition(existingPolicy.getStatus(), policyDTO.getStatus());
        
        Policy updatedPolicy = policyRepository.save(existingPolicy);
        log.info("Updated policy with id: {}", id);
        return policyMapper.toDto(updatedPolicy);
    }

    @CacheEvict(value = POLICY_CACHE, key = "#id")
    @Transactional
    public void deletePolicy(Long id) {
        log.info("Deleting policy with id: {}", id);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        
        policy.setActive(false);
        policyRepository.save(policy);
        log.info("Soft deleted policy with id: {}", id);
    }

    @CacheEvict(value = POLICY_CACHE, key = "#id")
    @Transactional
    public PolicyDTO updatePolicyStatus(Long id, Policy.PolicyStatus status) {
        log.info("Updating status to {} for policy with id: {}", status, id);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        
        validatePolicyStatusTransition(policy.getStatus(), status);
        
        // Handle status-specific logic
        if (status == Policy.PolicyStatus.CANCELLED) {
            policy.setCancellationDate(LocalDate.now());
            // Cancellation reason should be set separately
        } else if (status == Policy.PolicyStatus.EXPIRED) {
            policy.setExpirationDate(LocalDate.now());
        }
        
        policy.setStatus(status);
        Policy updatedPolicy = policyRepository.save(policy);
        log.info("Updated status to {} for policy with id: {}", status, id);
        return policyMapper.toDto(updatedPolicy);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDTO> searchPolicies(String query, Pageable pageable) {
        log.info("Searching policies with query: {}", query);
        return policyRepository.search(query, pageable)
                .map(policyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDTO> getPoliciesByStatus(Policy.PolicyStatus status, Pageable pageable) {
        log.info("Fetching policies with status: {}", status);
        return policyRepository.findByStatus(status, pageable)
                .map(policyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDTO> getPoliciesByAgent(Long agentId, Pageable pageable) {
        log.info("Fetching policies for agent with id: {}", agentId);
        return policyRepository.findByAgentId(agentId, pageable)
                .map(policyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDTO> getPoliciesByEffectiveDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching policies with effective date between {} and {}", startDate, endDate);
        return policyRepository.findByEffectiveDateBetween(startDate, endDate, pageable)
                .map(policyMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<PolicyDTO> getPoliciesByExpirationDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching policies with expiration date between {} and {}", startDate, endDate);
        return policyRepository.findByExpirationDateBetween(startDate, endDate, pageable)
                .map(policyMapper::toDto);
    }

    @Transactional
    public void processExpiredPolicies() {
        log.info("Processing expired policies");
        List<Policy> expiredPolicies = policyRepository.findExpiredPolicies(LocalDate.now());
        
        for (Policy policy : expiredPolicies) {
            policy.setStatus(Policy.PolicyStatus.EXPIRED);
            policyRepository.save(policy);
            log.info("Marked policy {} as expired", policy.getPolicyNumber());
        }
    }

    @Transactional
    public void processRenewalNotifications() {
        log.info("Processing renewal notifications");
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Policy> policiesDueForRenewal = policyRepository.findPoliciesDueForRenewal(thirtyDaysFromNow);
        
        for (Policy policy : policiesDueForRenewal) {
            // Here you would typically send a notification
            log.info("Policy {} is due for renewal on {}", policy.getPolicyNumber(), policy.getRenewalDate());
        }
    }

    private void validatePolicyDates(Policy policy) {
        if (policy.getEffectiveDate() != null && policy.getExpirationDate() != null) {
            if (policy.getEffectiveDate().isAfter(policy.getExpirationDate())) {
                throw new IllegalArgumentException("Effective date cannot be after expiration date");
            }
        }
        
        if (policy.getRenewalDate() != null && policy.getExpirationDate() != null) {
            if (policy.getRenewalDate().isBefore(policy.getExpirationDate())) {
                throw new IllegalArgumentException("Renewal date cannot be before expiration date");
            }
        }
    }

    private void validatePolicyStatusTransition(Policy.PolicyStatus currentStatus, Policy.PolicyStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case ACTIVE:
                if (newStatus != Policy.PolicyStatus.INACTIVE && 
                    newStatus != Policy.PolicyStatus.CANCELLED && 
                    newStatus != Policy.PolicyStatus.EXPIRED && 
                    newStatus != Policy.PolicyStatus.SUSPENDED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PENDING:
                if (newStatus != Policy.PolicyStatus.ACTIVE && newStatus != Policy.PolicyStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CANCELLED:
                if (newStatus != Policy.PolicyStatus.ACTIVE) {
                    throw new IllegalArgumentException("Cancelled policies can only be reactivated");
                }
                break;
            case EXPIRED:
                if (newStatus != Policy.PolicyStatus.RENEWED) {
                    throw new IllegalArgumentException("Expired policies can only be renewed");
                }
                break;
            case SUSPENDED:
                if (newStatus != Policy.PolicyStatus.ACTIVE && newStatus != Policy.PolicyStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
        }
    }
}
