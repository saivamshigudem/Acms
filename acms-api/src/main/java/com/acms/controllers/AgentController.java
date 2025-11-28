package com.acms.controllers;

import com.acms.dto.AgentDTO;
import com.acms.services.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/agents")
@SecurityRequirement(name = "bearerAuth")
public class AgentController {
    private static final Logger logger = LoggerFactory.getLogger(AgentController.class);
    
    private final AgentService agentService;
    
    @Autowired
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get agent by ID", description = "Retrieves an agent by their ID")
    public ResponseEntity<AgentDTO> getAgentById(@PathVariable Long id) {
        logger.info("GET /api/agents/{}", id);
        AgentDTO agentDTO = agentService.getAgentById(id);
        return ResponseEntity.ok(agentDTO);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Search agents", description = "Searches agents by name, email, or phone number")
    public ResponseEntity<Page<AgentDTO>> searchAgents(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/agents?query={}&page={}&size={}", query, pageable.getPageNumber(), pageable.getPageSize());
        Page<AgentDTO> agents = agentService.searchAgents(query != null ? query : "", pageable);
        return ResponseEntity.ok(agents);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new agent", description = "Creates a new agent with the provided details")
    public ResponseEntity<AgentDTO> createAgent(@Valid @RequestBody AgentDTO agentDTO) {
        logger.info("POST /api/agents - Creating new agent");
        AgentDTO createdAgent = agentService.createAgent(agentDTO);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAgent.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdAgent);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Update an agent", description = "Updates an existing agent's details")
    public ResponseEntity<AgentDTO> updateAgent(
            @PathVariable Long id,
            @Valid @RequestBody AgentDTO agentDTO) {
        logger.info("PUT /api/agents/{} - Updating agent", id);
        AgentDTO updatedAgent = agentService.updateAgent(id, agentDTO);
        return ResponseEntity.ok(updatedAgent);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an agent", description = "Soft deletes an agent by ID")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        logger.info("DELETE /api/agents/{} - Deleting agent", id);
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }
}
