package com.acms.dto;

import com.acms.models.Policy.PolicyStatus;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PolicyDTO {
    private Long id;
    
    @NotNull(message = "Agent ID is required")
    private Long agentId;
    
    @NotBlank(message = "Coverage type is required")
    @Size(max = 100, message = "Coverage type must be less than 100 characters")
    private String coverageType;
    
    @NotNull(message = "Premium is required")
    @Positive(message = "Premium must be greater than 0")
    private BigDecimal premium;
    
    @NotNull(message = "Effective date is required")
    private LocalDateTime effectiveDate;
    
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expirationDate;
    
    private PolicyStatus status = PolicyStatus.ACTIVE;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
