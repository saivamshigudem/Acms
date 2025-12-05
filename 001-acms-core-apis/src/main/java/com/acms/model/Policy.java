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
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE policies SET is_active = false WHERE id=?")
@FilterDef(
    name = "policyActiveFilter",
    parameters = @ParamDef(name = "isActive", type = Boolean.class)
)
@Filter(
    name = "policyActiveFilter",
    condition = "is_active = :isActive"
)
public class Policy extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Policy number is required")
    @Size(max = 50, message = "Policy number must be less than 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String policyNumber;

    @NotBlank(message = "Policy type is required")
    @Size(max = 100, message = "Policy type must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String policyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PolicyStatus status = PolicyStatus.ACTIVE;

    @NotNull(message = "Agent is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "group_number", length = 50)
    private String groupNumber;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Premium must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Premium must have valid monetary format")
    @Column(precision = 14, scale = 2)
    private BigDecimal premium;

    @Column(name = "coverage_amount", precision = 14, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "deductible_amount", precision = 14, scale = 2)
    private BigDecimal deductibleAmount;

    @Column(length = 500)
    private String description;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public enum PolicyStatus {
        ACTIVE,
        INACTIVE,
        PENDING,
        CANCELLED,
        EXPIRED,
        RENEWED,
        SUSPENDED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return id != null && Objects.equals(id, policy.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Policy{" +
                "id=" + id +
                ", policyNumber='" + policyNumber + '\'' +
                ", policyType='" + policyType + '\'' +
                ", status=" + status +
                ", agent=" + (agent != null ? agent.getId() : "null") +
                ", premium=" + premium +
                '}';
    }
}
