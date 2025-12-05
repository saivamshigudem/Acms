package com.acms.dto;

import com.acms.model.Policy.PolicyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Policy data transfer object")
public class PolicyDTO {

    @Schema(description = "Unique identifier of the policy", example = "1")
    private Long id;

    @NotBlank(message = "Policy number is required")
    @Size(max = 50, message = "Policy number must be less than 50 characters")
    @Schema(description = "Unique policy number", example = "POL-2023-001", required = true)
    private String policyNumber;

    @NotBlank(message = "Policy type is required")
    @Size(max = 100, message = "Policy type must be less than 100 characters")
    @Schema(description = "Type of insurance policy", example = "Group Health Insurance", required = true)
    private String policyType;

    @Schema(description = "Policy status", example = "ACTIVE", required = true)
    private PolicyStatus status;

    @NotNull(message = "Agent ID is required")
    @Schema(description = "ID of the assigned agent", example = "1", required = true)
    private Long agentId;

    @Schema(description = "Agent details (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private AgentSummaryDTO agent;

    @Size(max = 100, message = "Group name must be less than 100 characters")
    @Schema(description = "Name of the insured group", example = "ABC Corporation")
    private String groupName;

    @Size(max = 50, message = "Group number must be less than 50 characters")
    @Schema(description = "Group identification number", example = "GRP-12345")
    private String groupNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Policy effective date (YYYY-MM-DD)", example = "2023-01-01", type = "string", format = "date")
    private LocalDate effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Policy expiration date (YYYY-MM-DD)", example = "2023-12-31", type = "string", format = "date")
    private LocalDate expirationDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Premium must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Premium must have valid monetary format")
    @Schema(description = "Monthly premium amount", example = "1500.00")
    private BigDecimal premium;

    @Schema(description = "Coverage amount", example = "1000000.00")
    private BigDecimal coverageAmount;

    @Schema(description = "Deductible amount", example = "5000.00")
    private BigDecimal deductibleAmount;

    @Size(max = 500, message = "Description must be less than 500 characters")
    @Schema(description = "Policy description", example = "Comprehensive health coverage for employees")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Policy renewal date (YYYY-MM-DD)", example = "2023-12-01", type = "string", format = "date")
    private LocalDate renewalDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Policy cancellation date (YYYY-MM-DD)", example = "2023-06-15", type = "string", format = "date")
    private LocalDate cancellationDate;

    @Size(max = 500, message = "Cancellation reason must be less than 500 characters")
    @Schema(description = "Reason for policy cancellation", example = "Non-payment of premium")
    private String cancellationReason;

    @Schema(description = "Indicates if the policy is active", example = "true", required = true)
    private boolean isActive;

    @Schema(description = "Version for optimistic locking", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Agent summary information")
    public static class AgentSummaryDTO {
        @Schema(description = "Agent ID", example = "1")
        private Long id;
        
        @Schema(description = "Agent code", example = "AG001")
        private String agentCode;
        
        @Schema(description = "Agent full name", example = "John Doe")
        private String fullName;
        
        @Schema(description = "Agent email", example = "john.doe@example.com")
        private String email;
    }
}
