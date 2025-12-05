package com.acms.dto;

import com.acms.model.Payment.PaymentMethod;
import com.acms.model.Payment.PaymentStatus;
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
@Schema(description = "Payment data transfer object")
public class PaymentDTO {

    @Schema(description = "Unique identifier of the payment", example = "1")
    private Long id;

    @NotNull(message = "Commission ID is required")
    @Schema(description = "ID of the associated commission", example = "1", required = true)
    private Long commissionId;

    @Schema(description = "Commission details (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private CommissionSummaryDTO commission;

    @NotNull(message = "Agent ID is required")
    @Schema(description = "ID of the associated agent", example = "1", required = true)
    private Long agentId;

    @Schema(description = "Agent details (read-only)", accessMode = Schema.AccessMode.READ_ONLY)
    private AgentSummaryDTO agent;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Payment amount must have valid monetary format")
    @Schema(description = "Payment amount", example = "225.00", required = true)
    private BigDecimal paymentAmount;

    @Size(max = 100, message = "Payment reference must be less than 100 characters")
    @Schema(description = "Payment reference number", example = "PAY-2023-001")
    private String paymentReference;

    @Schema(description = "Payment method", example = "BANK_TRANSFER", required = true)
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment status", example = "PENDING", required = true)
    private PaymentStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Payment date (YYYY-MM-DD)", example = "2023-02-15", type = "string", format = "date")
    private LocalDate paymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Processed date (YYYY-MM-DD)", example = "2023-02-16", type = "string", format = "date")
    private LocalDate processedDate;

    @Size(max = 50, message = "Bank account must be less than 50 characters")
    @Schema(description = "Bank account number", example = "1234567890")
    private String bankAccount;

    @Size(max = 100, message = "Bank name must be less than 100 characters")
    @Schema(description = "Bank name", example = "National Bank")
    private String bankName;

    @Size(max = 100, message = "Transaction ID must be less than 100 characters")
    @Schema(description = "Transaction ID", example = "TXN-123456789")
    private String transactionId;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    @Schema(description = "Additional notes about the payment", example = "Q1 2023 commission payment")
    private String notes;

    @Schema(description = "Indicates if the payment is active", example = "true", required = true)
    private boolean isActive;

    @Schema(description = "Version for optimistic locking", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Commission summary information")
    public static class CommissionSummaryDTO {
        @Schema(description = "Commission ID", example = "1")
        private Long id;
        
        @Schema(description = "Commission amount", example = "225.00")
        private BigDecimal commissionAmount;
        
        @Schema(description = "Commission status", example = "PAID")
        private String status;
        
        @Schema(description = "Policy number", example = "POL-2023-001")
        private String policyNumber;
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
