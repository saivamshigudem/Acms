package com.acms.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Getter
@Setter
public class Policy extends BaseEntity {
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
    
    @NotBlank
    @Column(name = "coverage_type", nullable = false)
    private String coverageType;
    
    @NotNull
    @Positive
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal premium;
    
    @NotNull
    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;
    
    @NotNull
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PolicyStatus status = PolicyStatus.ACTIVE;
    
    public enum PolicyStatus {
        ACTIVE, INACTIVE, EXPIRED, CANCELLED
    }
}
