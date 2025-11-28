package com.acms.controllers;

import com.acms.dto.CommissionDTO;
import com.acms.services.CommissionService;
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
@RequestMapping("/api/commissions")
public class CommissionController {
    private static final Logger logger = LoggerFactory.getLogger(CommissionController.class);
    
    private final CommissionService commissionService;
    
    @Autowired
    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
        System.out.println("CommissionController initialized!");
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        System.out.println("Test endpoint hit!");
        return ResponseEntity.ok("Test endpoint works!");
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get commission by ID", description = "Retrieves a commission by their ID")
    public ResponseEntity<CommissionDTO> getCommissionById(@PathVariable Long id) {
        logger.info("GET /api/commissions/{}", id);
        CommissionDTO commissionDTO = commissionService.getCommissionById(id);
        return ResponseEntity.ok(commissionDTO);
    }
    
    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get commissions by agent", description = "Retrieves all commissions for a specific agent")
    public ResponseEntity<Page<CommissionDTO>> getCommissionsByAgent(
            @PathVariable Long agentId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/commissions/agent/{}", agentId);
        Page<CommissionDTO> commissions = commissionService.getCommissionsByAgent(agentId, pageable);
        return ResponseEntity.ok(commissions);
    }
    
    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get commissions by policy", description = "Retrieves all commissions for a specific policy")
    public ResponseEntity<Page<CommissionDTO>> getCommissionsByPolicy(
            @PathVariable Long policyId,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.info("GET /api/commissions/policy/{}", policyId);
        Page<CommissionDTO> commissions = commissionService.getCommissionsByPolicy(policyId, pageable);
        return ResponseEntity.ok(commissions);
    }
    
    @PostMapping("/calculate")
    @Operation(summary = "Calculate commission", description = "Calculates commission for a policy and agent")
    public ResponseEntity<CommissionDTO> calculateCommission(
            @RequestParam Long policyId,
            @RequestParam Long agentId) {
        logger.info("POST /api/commissions/calculate - Calculating commission for policy: {} and agent: {}", policyId, agentId);
        CommissionDTO commission = commissionService.calculateCommission(policyId, agentId);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(commission.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(commission);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a commission", description = "Updates an existing commission's details")
    public ResponseEntity<CommissionDTO> updateCommission(
            @PathVariable Long id,
            @Valid @RequestBody CommissionDTO commissionDTO) {
        logger.info("PUT /api/commissions/{} - Updating commission", id);
        CommissionDTO updatedCommission = commissionService.updateCommission(id, commissionDTO);
        return ResponseEntity.ok(updatedCommission);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a commission", description = "Soft deletes a commission by ID")
    public ResponseEntity<Void> deleteCommission(@PathVariable Long id) {
        logger.info("DELETE /api/commissions/{} - Deleting commission", id);
        commissionService.deleteCommission(id);
        return ResponseEntity.noContent().build();
    }
}
