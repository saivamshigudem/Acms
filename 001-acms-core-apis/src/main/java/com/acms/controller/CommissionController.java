package com.acms.controller;

import com.acms.dto.CommissionDTO;
import com.acms.dto.ApiResponse;
import com.acms.model.Commission;
import com.acms.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/commissions")
@RequiredArgsConstructor
@Tag(name = "Commission Management", description = "APIs for managing insurance commissions")
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping
    @Operation(summary = "Get all commissions", description = "Retrieve a paginated list of all commissions")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getAllCommissions(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getAllCommissions(pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get commission by ID", description = "Retrieve a commission by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Commission found",
                    content = @Content(schema = @Schema(implementation = CommissionDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Commission not found")
    })
    public ResponseEntity<ApiResponse<CommissionDTO>> getCommissionById(
            @Parameter(description = "ID of the commission to be retrieved") @PathVariable Long id) {
        CommissionDTO commission = commissionService.getCommissionById(id);
        return ResponseEntity.ok(ApiResponse.success(commission));
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get commissions by policy", description = "Retrieve commissions for a specific policy")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getCommissionsByPolicyId(
            @Parameter(description = "Policy ID") @PathVariable Long policyId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getCommissionsByPolicyId(policyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get commissions by agent", description = "Retrieve commissions for a specific agent")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getCommissionsByAgentId(
            @Parameter(description = "Agent ID") @PathVariable Long agentId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getCommissionsByAgentId(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate and create commission", description = "Calculate commission for a policy and agent, then create the commission record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Commission calculated and created successfully",
                    content = @Content(schema = @Schema(implementation = CommissionDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or calculation parameters")
    })
    public ResponseEntity<ApiResponse<CommissionDTO>> calculateCommission(
            @Parameter(description = "Policy ID") @RequestParam Long policyId,
            @Parameter(description = "Agent ID") @RequestParam Long agentId,
            @Parameter(description = "Commission type") @RequestParam Commission.CommissionType commissionType,
            @Parameter(description = "Custom commission rate (optional)") @RequestParam(required = false) BigDecimal customRate) {
        
        CommissionDTO commission = commissionService.calculateAndCreateCommission(
                policyId, agentId, commissionType, customRate);
        
        return ResponseEntity
                .created(URI.create("/api/v1/commissions/" + commission.getId()))
                .body(ApiResponse.withStatus(HttpStatus.CREATED, "Commission calculated and created successfully", commission));
    }

    @PostMapping
    @Operation(summary = "Create a new commission", description = "Register a new commission record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Commission created successfully",
                    content = @Content(schema = @Schema(implementation = CommissionDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<CommissionDTO>> createCommission(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Commission details to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CommissionDTO.class)))
            @Valid @RequestBody CommissionDTO commissionDTO) {
        
        // For manual creation, we'll use the calculate endpoint logic
        CommissionDTO createdCommission = commissionService.calculateAndCreateCommission(
                commissionDTO.getPolicyId(),
                commissionDTO.getAgentId(),
                commissionDTO.getCommissionType(),
                commissionDTO.getCommissionRate());
        
        return ResponseEntity
                .created(URI.create("/api/v1/commissions/" + createdCommission.getId()))
                .body(ApiResponse.withStatus(HttpStatus.CREATED, "Commission created successfully", createdCommission));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a commission", description = "Update an existing commission's information")
    public ResponseEntity<ApiResponse<CommissionDTO>> updateCommission(
            @Parameter(description = "ID of the commission to be updated") @PathVariable Long id,
            @Valid @RequestBody CommissionDTO commissionDTO) {
        CommissionDTO updatedCommission = commissionService.updateCommission(id, commissionDTO);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Commission updated successfully", updatedCommission));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a commission", description = "Soft delete a commission by its ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommission(
            @Parameter(description = "ID of the commission to be deleted") @PathVariable Long id) {
        commissionService.deleteCommission(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update commission status", description = "Update the status of an existing commission")
    public ResponseEntity<ApiResponse<CommissionDTO>> updateCommissionStatus(
            @Parameter(description = "ID of the commission") @PathVariable Long id,
            @RequestParam Commission.CommissionStatus status) {
        CommissionDTO updatedCommission = commissionService.updateCommissionStatus(id, status);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Commission status updated successfully", updatedCommission));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get commissions by status", description = "Retrieve commissions filtered by status")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getCommissionsByStatus(
            @Parameter(description = "Commission status") @PathVariable Commission.CommissionStatus status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getCommissionsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get commissions by calculation date range", description = "Retrieve commissions within a calculation date range")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getCommissionsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getCommissionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/payment-date-range")
    @Operation(summary = "Get commissions by payment date range", description = "Retrieve commissions within a payment date range")
    public ResponseEntity<ApiResponse<Page<CommissionDTO>>> getCommissionsByPaymentDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CommissionDTO> commissions = commissionService.getCommissionsByPaymentDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(commissions));
    }

    @GetMapping("/agent/{agentId}/summary")
    @Operation(summary = "Get agent commission summary", description = "Get total paid and pending commissions for an agent")
    public ResponseEntity<ApiResponse<AgentCommissionSummary>> getAgentCommissionSummary(
            @Parameter(description = "Agent ID") @PathVariable Long agentId) {
        
        BigDecimal totalPaid = commissionService.getTotalPaidCommissionsByAgent(agentId);
        BigDecimal totalPending = commissionService.getTotalPendingCommissionsByAgent(agentId);
        
        AgentCommissionSummary summary = AgentCommissionSummary.builder()
                .agentId(agentId)
                .totalPaidCommissions(totalPaid)
                .totalPendingCommissions(totalPending)
                .totalCommissions(totalPaid.add(totalPending))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/process-pending")
    @Operation(summary = "Process pending commissions", description = "Process pending commissions for approval")
    public ResponseEntity<ApiResponse<Void>> processPendingCommissions() {
        commissionService.processPendingCommissions();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Pending commissions processed successfully", null));
    }

    @PostMapping("/process-payments")
    @Operation(summary = "Process approved commissions for payment", description = "Process approved commissions for payment")
    public ResponseEntity<ApiResponse<Void>> processApprovedCommissionsForPayment() {
        commissionService.processApprovedCommissionsForPayment();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Approved commissions processed for payment successfully", null));
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Process expired commissions", description = "Process expired commissions")
    public ResponseEntity<ApiResponse<Void>> processExpiredCommissions() {
        commissionService.processExpiredCommissions();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Expired commissions processed successfully", null));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(description = "Agent commission summary")
    public static class AgentCommissionSummary {
        @io.swagger.v3.oas.annotations.media.Schema(description = "Agent ID", example = "1")
        private Long agentId;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total paid commissions", example = "5000.00")
        private BigDecimal totalPaidCommissions;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total pending commissions", example = "1500.00")
        private BigDecimal totalPendingCommissions;
        
        @io.swagger.v3.oas.annotations.media.Schema(description = "Total commissions (paid + pending)", example = "6500.00")
        private BigDecimal totalCommissions;
    }
}
