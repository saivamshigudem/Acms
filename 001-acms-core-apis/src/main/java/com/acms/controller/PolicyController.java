package com.acms.controller;

import com.acms.dto.PolicyDTO;
import com.acms.dto.ApiResponse;
import com.acms.model.Policy;
import com.acms.service.PolicyService;
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

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing insurance policies")
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    @Operation(summary = "Get all policies", description = "Retrieve a paginated list of all policies")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> getAllPolicies(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.getAllPolicies(pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID", description = "Retrieve a policy by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Policy found",
                    content = @Content(schema = @Schema(implementation = PolicyDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Policy not found")
    })
    public ResponseEntity<ApiResponse<PolicyDTO>> getPolicyById(
            @Parameter(description = "ID of the policy to be retrieved") @PathVariable Long id) {
        PolicyDTO policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.success(policy));
    }

    @GetMapping("/number/{policyNumber}")
    @Operation(summary = "Get policy by number", description = "Retrieve a policy by its unique policy number")
    public ResponseEntity<ApiResponse<PolicyDTO>> getPolicyByNumber(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        PolicyDTO policy = policyService.getPolicyByNumber(policyNumber);
        return ResponseEntity.ok(ApiResponse.success(policy));
    }

    @PostMapping
    @Operation(summary = "Create a new policy", description = "Register a new insurance policy")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Policy created successfully",
                    content = @Content(schema = @Schema(implementation = PolicyDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<PolicyDTO>> createPolicy(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Policy details to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PolicyDTO.class)))
            @Valid @RequestBody PolicyDTO policyDTO) {
        PolicyDTO createdPolicy = policyService.createPolicy(policyDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/policies/" + createdPolicy.getId()))
                .body(ApiResponse.withStatus(HttpStatus.CREATED, "Policy created successfully", createdPolicy));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a policy", description = "Update an existing policy's information")
    public ResponseEntity<ApiResponse<PolicyDTO>> updatePolicy(
            @Parameter(description = "ID of the policy to be updated") @PathVariable Long id,
            @Valid @RequestBody PolicyDTO policyDTO) {
        PolicyDTO updatedPolicy = policyService.updatePolicy(id, policyDTO);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Policy updated successfully", updatedPolicy));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a policy", description = "Soft delete a policy by its ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePolicy(
            @Parameter(description = "ID of the policy to be deleted") @PathVariable Long id) {
        policyService.deletePolicy(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update policy status", description = "Update the status of an existing policy")
    public ResponseEntity<ApiResponse<PolicyDTO>> updatePolicyStatus(
            @Parameter(description = "ID of the policy") @PathVariable Long id,
            @RequestParam Policy.PolicyStatus status) {
        PolicyDTO updatedPolicy = policyService.updatePolicyStatus(id, status);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Policy status updated successfully", updatedPolicy));
    }

    @GetMapping("/search")
    @Operation(summary = "Search policies", description = "Search policies by number, type, group name, or group number")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> searchPolicies(
            @Parameter(description = "Search query") @RequestParam String query,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.searchPolicies(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get policies by status", description = "Retrieve policies filtered by status")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> getPoliciesByStatus(
            @Parameter(description = "Policy status") @PathVariable Policy.PolicyStatus status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.getPoliciesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get policies by agent", description = "Retrieve policies assigned to a specific agent")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> getPoliciesByAgent(
            @Parameter(description = "Agent ID") @PathVariable Long agentId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.getPoliciesByAgent(agentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/effective-date-range")
    @Operation(summary = "Get policies by effective date range", description = "Retrieve policies within an effective date range")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> getPoliciesByEffectiveDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.getPoliciesByEffectiveDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/expiration-date-range")
    @Operation(summary = "Get policies by expiration date range", description = "Retrieve policies within an expiration date range")
    public ResponseEntity<ApiResponse<Page<PolicyDTO>>> getPoliciesByExpirationDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<PolicyDTO> policies = policyService.getPoliciesByExpirationDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Process expired policies", description = "Mark expired policies as expired")
    public ResponseEntity<ApiResponse<Void>> processExpiredPolicies() {
        policyService.processExpiredPolicies();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Expired policies processed successfully", null));
    }

    @PostMapping("/process-renewal-notifications")
    @Operation(summary = "Process renewal notifications", description = "Send renewal notifications for policies due for renewal")
    public ResponseEntity<ApiResponse<Void>> processRenewalNotifications() {
        policyService.processRenewalNotifications();
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Renewal notifications processed successfully", null));
    }
}
