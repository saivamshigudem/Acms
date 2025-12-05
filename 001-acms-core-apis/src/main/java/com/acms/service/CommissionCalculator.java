package com.acms.service;

import com.acms.model.Commission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@Slf4j
public class CommissionCalculator {

    @Value("${commission.default.rate:0.1500}")
    private BigDecimal defaultCommissionRate;

    @Value("${commission.minimum.amount:10.00}")
    private BigDecimal minimumCommissionAmount;

    @Value("${commission.maximum.amount:10000.00}")
    private BigDecimal maximumCommissionAmount;

    @Value("${commission.tier1.threshold:1000.00}")
    private BigDecimal tier1Threshold;

    @Value("${commission.tier1.rate:0.1000}")
    private BigDecimal tier1Rate;

    @Value("${commission.tier2.threshold:5000.00}")
    private BigDecimal tier2Threshold;

    @Value("${commission.tier2.rate:0.1500}")
    private BigDecimal tier2Rate;

    @Value("${commission.tier3.rate:0.2000}")
    private BigDecimal tier3Rate;

    /**
     * Calculate commission based on premium amount and commission type
     */
    public CommissionCalculationResult calculateCommission(
            BigDecimal premiumAmount,
            Commission.CommissionType commissionType,
            BigDecimal customRate) {
        
        log.debug("Calculating commission for premium: {}, type: {}, custom rate: {}", 
                premiumAmount, commissionType, customRate);

        BigDecimal commissionAmount = BigDecimal.ZERO;
        BigDecimal effectiveRate = BigDecimal.ZERO;

        switch (commissionType) {
            case PERCENTAGE:
                effectiveRate = customRate != null ? customRate : defaultCommissionRate;
                commissionAmount = premiumAmount.multiply(effectiveRate)
                        .setScale(2, RoundingMode.HALF_UP);
                break;
                
            case FIXED:
                commissionAmount = customRate != null ? customRate : defaultCommissionRate.multiply(premiumAmount)
                        .setScale(2, RoundingMode.HALF_UP);
                effectiveRate = commissionAmount.divide(premiumAmount, 4, RoundingMode.HALF_UP);
                break;
                
            case TIERED:
                commissionAmount = calculateTieredCommission(premiumAmount);
                effectiveRate = commissionAmount.divide(premiumAmount, 4, RoundingMode.HALF_UP);
                break;
                
            case BONUS:
                commissionAmount = calculateBonusCommission(premiumAmount);
                effectiveRate = commissionAmount.divide(premiumAmount, 4, RoundingMode.HALF_UP);
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported commission type: " + commissionType);
        }

        // Apply minimum and maximum constraints
        commissionAmount = commissionAmount.max(minimumCommissionAmount);
        if (maximumCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
            commissionAmount = commissionAmount.min(maximumCommissionAmount);
        }

        log.debug("Calculated commission: {}, effective rate: {}", commissionAmount, effectiveRate);

        return CommissionCalculationResult.builder()
                .commissionAmount(commissionAmount)
                .effectiveRate(effectiveRate)
                .premiumAmount(premiumAmount)
                .commissionType(commissionType)
                .calculationDate(LocalDate.now())
                .build();
    }

    /**
     * Calculate tiered commission based on premium amount
     */
    private BigDecimal calculateTieredCommission(BigDecimal premiumAmount) {
        BigDecimal commission = BigDecimal.ZERO;

        if (premiumAmount.compareTo(tier1Threshold) <= 0) {
            // Tier 1: Up to tier1Threshold
            commission = premiumAmount.multiply(tier1Rate);
        } else if (premiumAmount.compareTo(tier2Threshold) <= 0) {
            // Tier 2: tier1Threshold to tier2Threshold
            BigDecimal tier1Amount = tier1Threshold.multiply(tier1Rate);
            BigDecimal tier2Amount = premiumAmount.subtract(tier1Threshold).multiply(tier2Rate);
            commission = tier1Amount.add(tier2Amount);
        } else {
            // Tier 3: Above tier2Threshold
            BigDecimal tier1Amount = tier1Threshold.multiply(tier1Rate);
            BigDecimal tier2Amount = tier2Threshold.subtract(tier1Threshold).multiply(tier2Rate);
            BigDecimal tier3Amount = premiumAmount.subtract(tier2Threshold).multiply(tier3Rate);
            commission = tier1Amount.add(tier2Amount).add(tier3Amount);
        }

        return commission.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate bonus commission (e.g., for high-performing agents)
     */
    private BigDecimal calculateBonusCommission(BigDecimal premiumAmount) {
        // Bonus commission is typically a higher percentage or fixed bonus
        BigDecimal bonusRate = defaultCommissionRate.multiply(BigDecimal.valueOf(1.5)); // 50% bonus
        return premiumAmount.multiply(bonusRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validate commission calculation parameters
     */
    public void validateCalculationParameters(
            BigDecimal premiumAmount,
            Commission.CommissionType commissionType,
            BigDecimal customRate) {
        
        if (premiumAmount == null || premiumAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Premium amount must be greater than 0");
        }

        if (commissionType == null) {
            throw new IllegalArgumentException("Commission type is required");
        }

        if (customRate != null) {
            if (customRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Custom rate must be greater than 0");
            }
            
            if (customRate.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("Custom rate cannot exceed 100%");
            }
        }
    }

    /**
     * Check if commission calculation is valid for the given policy and agent
     */
    public boolean isValidForCalculation(
            LocalDate policyEffectiveDate,
            LocalDate policyExpirationDate,
            LocalDate calculationDate) {
        
        // Don't calculate commission for expired policies
        if (policyExpirationDate != null && calculationDate.isAfter(policyExpirationDate)) {
            return false;
        }

        // Don't calculate commission before policy effective date
        if (policyEffectiveDate != null && calculationDate.isBefore(policyEffectiveDate)) {
            return false;
        }

        return true;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CommissionCalculationResult {
        private BigDecimal commissionAmount;
        private BigDecimal effectiveRate;
        private BigDecimal premiumAmount;
        private Commission.CommissionType commissionType;
        private LocalDate calculationDate;
        private String calculationMethod;
    }
}
