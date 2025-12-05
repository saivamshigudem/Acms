package com.acms.repository;

import com.acms.model.Agent;
import com.acms.model.Commission;
import com.acms.model.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends BaseRepository<Commission, Long> {
    
    Optional<Commission> findByPolicyIdAndAgentId(Long policyId, Long agentId);
    
    @Query("SELECT c FROM Commission c WHERE c.policy.id = :policyId")
    Page<Commission> findByPolicyId(@Param("policyId") Long policyId, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.agent.id = :agentId")
    Page<Commission> findByAgentId(@Param("agentId") Long agentId, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.status = :status")
    Page<Commission> findByStatus(@Param("status") Commission.CommissionStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.commissionType = :type")
    Page<Commission> findByCommissionType(@Param("type") Commission.CommissionType type, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.calculationDate BETWEEN :startDate AND :endDate")
    Page<Commission> findByCalculationDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.paymentDate BETWEEN :startDate AND :endDate")
    Page<Commission> findByPaymentDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.effectiveDate <= :date AND (c.expiryDate IS NULL OR c.expiryDate >= :date)")
    List<Commission> findActiveCommissions(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Commission c WHERE c.status = 'PENDING' AND c.calculationDate <= :date")
    List<Commission> findPendingCommissionsForApproval(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Commission c WHERE c.status = 'APPROVED' AND c.paymentDate IS NULL")
    List<Commission> findApprovedCommissionsForPayment();
    
    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.agent.id = :agentId AND c.status = 'PAID'")
    BigDecimal sumPaidCommissionsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT SUM(c.commissionAmount) FROM Commission c WHERE c.agent.id = :agentId AND c.status = 'PENDING'")
    BigDecimal sumPendingCommissionsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT COUNT(c) FROM Commission c WHERE c.agent.id = :agentId AND c.status = :status")
    Long countCommissionsByAgentAndStatus(@Param("agentId") Long agentId, @Param("status") Commission.CommissionStatus status);
    
    @Query("SELECT c FROM Commission c WHERE c.policy = :policy")
    Page<Commission> findByPolicy(@Param("policy") Policy policy, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.agent = :agent")
    Page<Commission> findByAgent(@Param("agent") Agent agent, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE " +
           "LOWER(c.paymentReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.notes) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Commission> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.active = :isActive")
    List<Commission> findAllByActive(@Param("isActive") boolean isActive);
    
    @Query("SELECT c FROM Commission c WHERE c.expiryDate <= :date AND c.status = 'PENDING'")
    List<Commission> findExpiredPendingCommissions(@Param("date") LocalDate date);
    
    @Query("SELECT c FROM Commission c WHERE c.agent.id = :agentId AND c.calculationDate BETWEEN :startDate AND :endDate")
    Page<Commission> findByAgentAndDateRange(
            @Param("agentId") Long agentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT c FROM Commission c WHERE c.policy.id = :policyId AND c.status = :status")
    List<Commission> findByPolicyIdAndStatus(@Param("policyId") Long policyId, @Param("status") Commission.CommissionStatus status);
}
