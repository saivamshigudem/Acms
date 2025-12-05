package com.acms.repository;

import com.acms.model.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends BaseRepository<Agent, Long> {
    
    Optional<Agent> findByAgentCode(String agentCode);
    
    boolean existsByAgentCode(String agentCode);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Agent a WHERE " +
           "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "a.phone LIKE CONCAT('%', :query, '%')")
    Page<Agent> search(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT a FROM Agent a WHERE a.status = :status")
    Page<Agent> findByStatus(@Param("status") Agent.AgentStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Agent a WHERE a.active = :isActive")
    List<Agent> findAllByActive(@Param("isActive") boolean isActive);
}
