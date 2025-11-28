package com.acms.controllers;

import com.acms.dto.PolicyDTO;
import com.acms.services.PolicyService;
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
@RequestMapping("/api/policies")
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {
    private static final Logger logger = LoggerFactory.getLogger(PolicyController.class);
    
    private final PolicyService policyService;
    
    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get policy by ID", description = "Retrieves a policy by their ID")
    public ResponseEntity<PolicyDTO> getPolicyById(@PathVariable Long id) {
        logger.info("GET /api/policies/{}", id);
        PolicyDTO policyDTO = policyService.getPolicyById(id);
        return ResponseEntity.ok(policyDTO);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Search policies", description = "Searches policies by coverage type")
    public ResponseEntity<Page<PolicyDTO>> searchPolicies(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/policies?query={}&page={}&size={}", query, pageable.getPageNumber(), pageable.getPageSize());
        Page<PolicyDTO> policies = policyService.searchPolicies(query != null ? query : "", pageable);
        return ResponseEntity.ok(policies);
    }
    
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get policies by agent", description = "Retrieves all policies for a specific agent")
    public ResponseEntity<Page<PolicyDTO>> getPoliciesByAgent(
            @PathVariable Long agentId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/policies/agent/{}", agentId);
        Page<PolicyDTO> policies = policyService.getPoliciesByAgent(agentId, pageable);
        return ResponseEntity.ok(policies);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new policy", description = "Creates a new policy with the provided details")
    public ResponseEntity<PolicyDTO> createPolicy(@Valid @RequestBody PolicyDTO policyDTO) {
        logger.info("POST /api/policies - Creating new policy");
        PolicyDTO createdPolicy = policyService.createPolicy(policyDTO);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPolicy.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdPolicy);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Update a policy", description = "Updates an existing policy's details")
    public ResponseEntity<PolicyDTO> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody PolicyDTO policyDTO) {
        logger.info("PUT /api/policies/{} - Updating policy", id);
        PolicyDTO updatedPolicy = policyService.updatePolicy(id, policyDTO);
        return ResponseEntity.ok(updatedPolicy);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a policy", description = "Soft deletes a policy by ID")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        logger.info("DELETE /api/policies/{} - Deleting policy", id);
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
