package com.acms.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commissions")
@Getter
@Setter
public class Commission extends BaseEntity {
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
    
    @NotNull
    @Positive
    @Column(name = "commission_tier", precision = 5, scale = 2)
    private BigDecimal commissionTier;
    
    @NotNull
    @Positive
    @Column(name = "calculated_amount", precision = 15, scale = 2)
    private BigDecimal calculatedAmount;
    
    @NotNull
    @Column(name = "calculation_date")
    private LocalDateTime calculationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CommissionStatus status = CommissionStatus.CALCULATED;
    
    public enum CommissionStatus {
        CALCULATED, APPROVED, PAID, CANCELLED
    }
}
