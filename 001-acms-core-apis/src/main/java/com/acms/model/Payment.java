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
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE payments SET is_active = false WHERE id=?")
@FilterDef(
    name = "paymentActiveFilter",
    parameters = @ParamDef(name = "isActive", type = Boolean.class)
)
@Filter(
    name = "paymentActiveFilter",
    condition = "is_active = :isActive"
)
public class Payment extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Commission is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_id", nullable = false)
    private Commission commission;

    @NotNull(message = "Agent is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Payment amount must have valid monetary format")
    @Column(precision = 14, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_reference", length = 100, unique = true)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(length = 500)
    private String notes;

    public enum PaymentMethod {
        BANK_TRANSFER,
        CHECK,
        CASH,
        WIRE_TRANSFER,
        DIRECT_DEPOSIT,
        ELECTRONIC_FUND_TRANSFER
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REVERSED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return id != null && Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", paymentAmount=" + paymentAmount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", paymentReference='" + paymentReference + '\'' +
                ", agent=" + (agent != null ? agent.getId() : "null") +
                ", commission=" + (commission != null ? commission.getId() : "null") +
                '}';
    }
}
