package com.acms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "commissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE commissions SET is_active = false WHERE id=?")
@FilterDef(
    name = "commissionActiveFilter",
    parameters = @ParamDef(name = "isActive", type = Boolean.class)
)
@Filter(
    name = "commissionActiveFilter",
    condition = "is_active = :isActive"
)
public class Commission extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Policy is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @NotNull(message = "Agent is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "commission_rate", precision = 8, scale = 4)
    private BigDecimal commissionRate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Commission amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Commission amount must have valid monetary format")
    @Column(precision = 14, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "premium_amount", precision = 14, scale = 2)
    private BigDecimal premiumAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_type", length = 20)
    private CommissionType commissionType = CommissionType.PERCENTAGE;

    @Column(name = "calculation_date")
    private LocalDate calculationDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String notes;

    public enum CommissionType {
        PERCENTAGE,
        FIXED,
        TIERED,
        BONUS
    }

    public enum CommissionStatus {
        PENDING,
        APPROVED,
        PAID,
        CANCELLED,
        HELD,
        FORFEITED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commission commission = (Commission) o;
        return id != null && Objects.equals(id, commission.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Commission{" +
                "id=" + id +
                ", commissionAmount=" + commissionAmount +
                ", commissionType=" + commissionType +
                ", status=" + status +
                ", agent=" + (agent != null ? agent.getId() : "null") +
                ", policy=" + (policy != null ? policy.getId() : "null") +
                '}';
    }
}
