package com.acms.repositories;

import com.acms.models.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PolicyRepository extends BaseRepository<Policy, Long> {
    
    Page<Policy> findByAgentId(Long agentId, Pageable pageable);
    
    @Query("SELECT p FROM Policy p WHERE p.agent.id = :agentId AND p.status = 'ACTIVE'")
    List<Policy> findActivePoliciesByAgent(@Param("agentId") Long agentId);
    
    @Query("SELECT p FROM Policy p WHERE p.expirationDate < :date AND p.status = 'ACTIVE'")
    List<Policy> findExpiredPolicies(@Param("date") LocalDateTime date);
    
    @Query("SELECT p FROM Policy p WHERE " +
           "LOWER(p.coverageType) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Policy> searchPolicies(@Param("query") String query, Pageable pageable);
}
