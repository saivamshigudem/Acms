package com.acms.dto;

import com.acms.models.Payment.PaymentStatus;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    
    @NotNull(message = "Commission ID is required")
    private Long commissionId;
    
    @NotNull(message = "Agent ID is required")
    private Long agentId;
    
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be greater than 0")
    private BigDecimal paymentAmount;
    
    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;
    
    @Size(max = 50, message = "Payment method must be less than 50 characters")
    private String paymentMethod;
    
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
