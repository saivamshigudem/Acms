package com.acms.repository;

import com.acms.model.Agent;
import com.acms.model.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends BaseRepository<Policy, Long> {
    
    Optional<Policy> findByPolicyNumber(String policyNumber);
    
    boolean existsByPolicyNumber(String policyNumber);
    
    @Query("SELECT p FROM Policy p WHERE p.agent.id = :agentId")
    Page<Policy> findByAgentId(@Param("agentId") Long agentId, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE " +
           "LOWER(p.policyNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.policyType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.groupName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.groupNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Policy> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.status = :status")
    Page<Policy> findByStatus(@Param("status") Policy.PolicyStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.effectiveDate BETWEEN :startDate AND :endDate")
    Page<Policy> findByEffectiveDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.expirationDate BETWEEN :startDate AND :endDate")
    Page<Policy> findByExpirationDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.renewalDate BETWEEN :startDate AND :endDate")
    Page<Policy> findByRenewalDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.expirationDate < :date AND p.status = 'ACTIVE'")
    List<Policy> findExpiredPolicies(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Policy p WHERE p.renewalDate <= :date AND p.renewalDate IS NOT NULL AND p.status = 'ACTIVE'")
    List<Policy> findPoliciesDueForRenewal(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(p) FROM Policy p WHERE p.agent.id = :agentId AND p.status = 'ACTIVE'")
    Long countActivePoliciesByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT SUM(p.premium) FROM Policy p WHERE p.agent.id = :agentId AND p.status = 'ACTIVE'")
    Double sumPremiumByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT p FROM Policy p WHERE p.agent = :agent")
    Page<Policy> findByAgent(@Param("agent") Agent agent, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.active = :isActive")
    List<Policy> findAllByActive(@Param("isActive") boolean isActive);
}
