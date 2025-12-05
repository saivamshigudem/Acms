package com.acms.service;

import com.acms.dto.CommissionDTO;
import com.acms.exception.ResourceNotFoundException;
import com.acms.mapper.CommissionMapper;
import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Policy;
import com.acms.repository.AgentRepository;
import com.acms.repository.CommissionRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.acms.config.CacheConfig.COMMISSION_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionService {

    private final CommissionRepository commissionRepository;
    private final AgentRepository agentRepository;
    private final PolicyRepository policyRepository;
    private final CommissionMapper commissionMapper;
    private final CommissionCalculator commissionCalculator;

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getAllCommissions(Pageable pageable) {
        return commissionRepository.findAll(pageable)
                .map(commissionMapper::toDto);
    }

    @Cacheable(value = COMMISSION_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public CommissionDTO getCommissionById(Long id) {
        log.info("Fetching commission with id: {}", id);
        return commissionRepository.findById(id)
                .map(commissionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionsByPolicyId(Long policyId, Pageable pageable) {
        log.info("Fetching commissions for policy with id: {}", policyId);
        return commissionRepository.findByPolicyId(policyId, pageable)
                .map(commissionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionsByAgentId(Long agentId, Pageable pageable) {
        log.info("Fetching commissions for agent with id: {}", agentId);
        return commissionRepository.findByAgentId(agentId, pageable)
                .map(commissionMapper::toDto);
    }

    @Transactional
    public CommissionDTO calculateAndCreateCommission(Long policyId, Long agentId, 
            Commission.CommissionType commissionType, BigDecimal customRate) {
        log.info("Calculating commission for policy: {}, agent: {}, type: {}", 
                policyId, agentId, commissionType);

        // Validate policy and agent exist
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));

        // Check if commission already exists for this policy and agent
        if (commissionRepository.findByPolicyIdAndAgentId(policyId, agentId).isPresent()) {
            throw new IllegalArgumentException("Commission already exists for policy " + policyId + 
                    " and agent " + agentId);
        }

        // Validate calculation parameters
        commissionCalculator.validateCalculationParameters(policy.getPremium(), commissionType, customRate);

        // Check if calculation is valid for policy dates
        LocalDate today = LocalDate.now();
        if (!commissionCalculator.isValidForCalculation(policy.getEffectiveDate(), 
                policy.getExpirationDate(), today)) {
            throw new IllegalArgumentException("Commission calculation is not valid for current policy dates");
        }

        // Calculate commission
        CommissionCalculator.CommissionCalculationResult result = 
                commissionCalculator.calculateCommission(policy.getPremium(), commissionType, customRate);

        // Create commission entity
        Commission commission = Commission.builder()
                .policy(policy)
                .agent(agent)
                .commissionAmount(result.getCommissionAmount())
                .premiumAmount(result.getPremiumAmount())
                .commissionRate(result.getEffectiveRate())
                .commissionType(result.getCommissionType())
                .calculationDate(result.getCalculationDate())
                .effectiveDate(today)
                .status(Commission.CommissionStatus.PENDING)
                .build();

        Commission savedCommission = commissionRepository.save(commission);
        log.info("Created commission with id: {} for policy: {}, agent: {}", 
                savedCommission.getId(), policyId, agentId);

        return commissionMapper.toDto(savedCommission);
    }

    @CachePut(value = COMMISSION_CACHE, key = "#id")
    @Transactional
    public CommissionDTO updateCommission(Long id, CommissionDTO commissionDTO) {
        log.info("Updating commission with id: {}", id);
        Commission existingCommission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));

        // Prevent updating policy and agent references
        if (commissionDTO.getPolicyId() != null && 
            !existingCommission.getPolicy().getId().equals(commissionDTO.getPolicyId())) {
            throw new IllegalArgumentException("Policy reference cannot be changed");
        }

        if (commissionDTO.getAgentId() != null && 
            !existingCommission.getAgent().getId().equals(commissionDTO.getAgentId())) {
            throw new IllegalArgumentException("Agent reference cannot be changed");
        }

        commissionMapper.updateCommissionFromDto(commissionDTO, existingCommission);
        
        // Validate status transitions
        validateCommissionStatusTransition(existingCommission.getStatus(), commissionDTO.getStatus());
        
        Commission updatedCommission = commissionRepository.save(existingCommission);
        log.info("Updated commission with id: {}", id);
        return commissionMapper.toDto(updatedCommission);
    }

    @CacheEvict(value = COMMISSION_CACHE, key = "#id")
    @Transactional
    public void deleteCommission(Long id) {
        log.info("Deleting commission with id: {}", id);
        Commission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
        
        commission.setActive(false);
        commissionRepository.save(commission);
        log.info("Soft deleted commission with id: {}", id);
    }

    @CacheEvict(value = COMMISSION_CACHE, key = "#id")
    @Transactional
    public CommissionDTO updateCommissionStatus(Long id, Commission.CommissionStatus status) {
        log.info("Updating status to {} for commission with id: {}", status, id);
        Commission commission = commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission not found with id: " + id));
        
        validateCommissionStatusTransition(commission.getStatus(), status);
        
        // Handle status-specific logic
        if (status == Commission.CommissionStatus.PAID) {
            commission.setPaymentDate(LocalDate.now());
        } else if (status == Commission.CommissionStatus.CANCELLED) {
            commission.setExpiryDate(LocalDate.now());
        }
        
        commission.setStatus(status);
        Commission updatedCommission = commissionRepository.save(commission);
        log.info("Updated status to {} for commission with id: {}", status, id);
        return commissionMapper.toDto(updatedCommission);
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionsByStatus(Commission.CommissionStatus status, Pageable pageable) {
        log.info("Fetching commissions with status: {}", status);
        return commissionRepository.findByStatus(status, pageable)
                .map(commissionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching commissions with calculation date between {} and {}", startDate, endDate);
        return commissionRepository.findByCalculationDateBetween(startDate, endDate, pageable)
                .map(commissionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionsByPaymentDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching commissions with payment date between {} and {}", startDate, endDate);
        return commissionRepository.findByPaymentDateBetween(startDate, endDate, pageable)
                .map(commissionMapper::toDto);
    }

    @Transactional
    public void processPendingCommissions() {
        log.info("Processing pending commissions for approval");
        List<Commission> pendingCommissions = commissionRepository.findPendingCommissionsForApproval(LocalDate.now());
        
        for (Commission commission : pendingCommissions) {
            // Business logic for approving commissions
            if (shouldAutoApprove(commission)) {
                commission.setStatus(Commission.CommissionStatus.APPROVED);
                commissionRepository.save(commission);
                log.info("Auto-approved commission: {}", commission.getId());
            }
        }
    }

    @Transactional
    public void processApprovedCommissionsForPayment() {
        log.info("Processing approved commissions for payment");
        List<Commission> approvedCommissions = commissionRepository.findApprovedCommissionsForPayment();
        
        for (Commission commission : approvedCommissions) {
            // Business logic for processing payments
            if (shouldAutoPay(commission)) {
                commission.setStatus(Commission.CommissionStatus.PAID);
                commission.setPaymentDate(LocalDate.now());
                commission.setPaymentReference(generatePaymentReference(commission));
                commissionRepository.save(commission);
                log.info("Auto-paid commission: {}", commission.getId());
            }
        }
    }

    @Transactional
    public void processExpiredCommissions() {
        log.info("Processing expired commissions");
        List<Commission> expiredCommissions = commissionRepository.findExpiredPendingCommissions(LocalDate.now());
        
        for (Commission commission : expiredCommissions) {
            commission.setStatus(Commission.CommissionStatus.FORFEITED);
            commissionRepository.save(commission);
            log.info("Marked commission as forfeited: {}", commission.getId());
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidCommissionsByAgent(Long agentId) {
        BigDecimal total = commissionRepository.sumPaidCommissionsByAgent(agentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingCommissionsByAgent(Long agentId) {
        BigDecimal total = commissionRepository.sumPendingCommissionsByAgent(agentId);
        return total != null ? total : BigDecimal.ZERO;
    }

    private void validateCommissionStatusTransition(Commission.CommissionStatus currentStatus, 
            Commission.CommissionStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Commission.CommissionStatus.APPROVED && 
                    newStatus != Commission.CommissionStatus.CANCELLED && 
                    newStatus != Commission.CommissionStatus.FORFEITED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case APPROVED:
                if (newStatus != Commission.CommissionStatus.PAID && 
                    newStatus != Commission.CommissionStatus.CANCELLED && 
                    newStatus != Commission.CommissionStatus.HELD) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case PAID:
                if (newStatus != Commission.CommissionStatus.CANCELLED) {
                    throw new IllegalArgumentException("Paid commissions can only be cancelled");
                }
                break;
            case HELD:
                if (newStatus != Commission.CommissionStatus.APPROVED && 
                    newStatus != Commission.CommissionStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CANCELLED:
            case FORFEITED:
                throw new IllegalArgumentException("Cannot change status from " + currentStatus);
        }
    }

    private boolean shouldAutoApprove(Commission commission) {
        // Business logic for auto-approval
        // For example: auto-approve commissions below a certain threshold
        return commission.getCommissionAmount().compareTo(new BigDecimal("1000.00")) < 0;
    }

    private boolean shouldAutoPay(Commission commission) {
        // Business logic for auto-payment
        // For example: auto-pay commissions approved more than 7 days ago
        return commission.getCalculationDate().plusDays(7).isBefore(LocalDate.now());
    }

    private String generatePaymentReference(Commission commission) {
        return "PAY-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", commission.getId());
    }
}
