package com.acms.dto;

import com.acms.model.Commission.CommissionStatus;
import com.acms.model.Commission.CommissionType;
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
@Schema(description = "Commission data transfer object")
public class CommissionDTO {

    @Schema(description = "Unique identifier of the commission", example = "1")
    private Long id;

    @NotNull(message = "Policy ID is required")
    @Schema(description = "ID of the associated policy", example = "1", required = true)
    private Long policyId;

    @Schema(description = "Policy details (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private PolicySummaryDTO policy;

    @NotNull(message = "Agent ID is required")
    @Schema(description = "ID of the associated agent", example = "1", required = true)
    private Long agentId;

    @Schema(description = "Agent details (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private AgentSummaryDTO agent;

    @DecimalMin(value = "0.0", inclusive = false, message = "Commission rate must be greater than 0")
    @Digits(integer = 4, fraction = 4, message = "Commission rate must have valid format")
    @Schema(description = "Commission rate (for percentage-based commissions)", example = "0.1500")
    private BigDecimal commissionRate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Commission amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Commission amount must have valid monetary format")
    @Schema(description = "Commission amount", example = "150.00")
    private BigDecimal commissionAmount;

    @Schema(description = "Premium amount used for calculation", example = "1000.00")
    private BigDecimal premiumAmount;

    @Schema(description = "Type of commission calculation", example = "PERCENTAGE", required = true)
    private CommissionType commissionType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date when commission was calculated (YYYY-MM-DD)", example = "2023-01-15", type = "string", format = "date")
    private LocalDate calculationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Effective date of commission (YYYY-MM-DD)", example = "2023-01-01", type = "string", format = "date")
    private LocalDate effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Expiry date of commission (YYYY-MM-DD)", example = "2023-12-31", type = "string", format = "date")
    private LocalDate expiryDate;

    @Schema(description = "Commission status", example = "PENDING", required = true)
    private CommissionStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Payment date (YYYY-MM-DD)", example = "2023-02-15", type = "string", format = "date")
    private LocalDate paymentDate;

    @Size(max = 100, message = "Payment reference must be less than 100 characters")
    @Schema(description = "Payment reference number", example = "PAY-2023-001")
    private String paymentReference;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    @Schema(description = "Additional notes about the commission", example = "First quarter bonus commission")
    private String notes;

    @Schema(description = "Indicates if the commission is active", example = "true", required = true)
    private boolean isActive;

    @Schema(description = "Version for optimistic locking", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Policy summary information")
    public static class PolicySummaryDTO {
        @Schema(description = "Policy ID", example = "1")
        private Long id;
        
        @Schema(description = "Policy number", example = "POL-2023-001")
        private String policyNumber;
        
        @Schema(description = "Policy type", example = "Group Health Insurance")
        private String policyType;
        
        @Schema(description = "Premium amount", example = "1500.00")
        private BigDecimal premium;
    }

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
