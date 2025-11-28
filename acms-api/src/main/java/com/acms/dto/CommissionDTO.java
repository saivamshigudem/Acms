package com.acms.dto;

import com.acms.models.Commission.CommissionStatus;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionDTO {
    private Long id;
    
    @NotNull(message = "Policy ID is required")
    private Long policyId;
    
    @NotNull(message = "Agent ID is required")
    private Long agentId;
    
    @NotNull(message = "Commission tier is required")
    @DecimalMin(value = "0.0", message = "Commission tier must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Commission tier must be less than or equal to 100")
    private BigDecimal commissionTier;
    
    @NotNull(message = "Calculated amount is required")
    @Positive(message = "Calculated amount must be greater than 0")
    private BigDecimal calculatedAmount;
    
    @NotNull(message = "Calculation date is required")
    private LocalDateTime calculationDate;
    
    private CommissionStatus status = CommissionStatus.CALCULATED;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
