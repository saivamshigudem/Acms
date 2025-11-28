package com.acms.repositories;

import com.acms.models.Commission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommissionRepository extends BaseRepository<Commission, Long> {
    
    Page<Commission> findByAgentId(Long agentId, Pageable pageable);
    
    Page<Commission> findByPolicyId(Long policyId, Pageable pageable);
    
    @Query("SELECT SUM(c.calculatedAmount) FROM Commission c WHERE c.agent.id = :agentId AND c.status = 'PAID'")
    BigDecimal getTotalCommissionsByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT c FROM Commission c WHERE c.agent.id = :agentId AND c.calculationDate BETWEEN :startDate AND :endDate")
    List<Commission> findCommissionsByAgentAndDateRange(
            @Param("agentId") Long agentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM Commission c WHERE c.status = :status")
    Page<Commission> findByStatus(@Param("status") String status, Pageable pageable);
}
