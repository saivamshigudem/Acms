package com.acms.repositories;

import com.acms.models.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends BaseRepository<Agent, Long> {
    Optional<Agent> findByEmail(String email);
    Boolean existsByEmail(String email);
    
    @Query("SELECT a FROM Agent a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Agent> searchAgents(@Param("query") String query, Pageable pageable);
}
