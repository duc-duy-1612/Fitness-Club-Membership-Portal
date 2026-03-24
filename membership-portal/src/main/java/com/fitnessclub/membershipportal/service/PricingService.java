package com.fitnessclub.membershipportal.service;

import com.fitnessclub.membershipportal.entity.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Simple pricing: plan base by type + duration, then add-ons. */
@Service
public class PricingService {

    // UI requirement: 500k/month (Basic), 900k/month (Premium)
    private static final BigDecimal BASIC_MONTHLY = new BigDecimal("500000");
    private static final BigDecimal PREMIUM_MONTHLY = new BigDecimal("900000");
    // Add-ons: PT = 300k/buổi, Locker = 100k/tháng
    private static final BigDecimal PERSONAL_TRAINING_PER_SESSION = new BigDecimal("300000");
    private static final BigDecimal LOCKER_RENTAL_PER_MONTH = new BigDecimal("100000");

    public BigDecimal monthlyRateForPlan(PlanType planType) {
        return planType == PlanType.PREMIUM ? PREMIUM_MONTHLY : BASIC_MONTHLY;
    }

    public int durationMonths(ContractDuration duration) {
        return switch (duration) {
            case MONTHLY -> 1;
            case SIX_MONTH -> 6;
            case ANNUAL -> 12;
        };
    }

    /**
     * Plan base for the full contract period (e.g. 12 months * monthly rate).
     */
    public BigDecimal planBaseForPeriod(PlanType planType, ContractDuration duration) {
        BigDecimal monthly = monthlyRateForPlan(planType);
        int months = durationMonths(duration);
        return monthly.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Add-on unit price by type (per session or per month depending on type).
     */
    public BigDecimal unitPriceForAddOn(AddOnType type) {
        return switch (type) {
            case PERSONAL_TRAINING -> PERSONAL_TRAINING_PER_SESSION;
            case LOCKER_RENTAL -> LOCKER_RENTAL_PER_MONTH;
        };
    }

    /**
     * Total for add-ons over full contract selection.
     * PT: PT_PER_SESSION * quantity.
     * Locker: LOCKER_RENTAL_PER_MONTH * quantity.
     */
    public BigDecimal addOnsTotal(List<EnrollmentAddOn> addOns, BillingType billingType) {
        BigDecimal total = BigDecimal.ZERO;
        for (EnrollmentAddOn line : addOns) {
            BigDecimal unit = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
            int qty = line.getQuantity() != null ? line.getQuantity() : 0;
            total = total.add(unit.multiply(BigDecimal.valueOf(qty)));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Total amount:
     * - ONE_TIME_UPFRONT: full course total
     * - MONTHLY: monthly installment = full course total / contract months
     */
    public BigDecimal computeTotal(MembershipEnrollment enrollment) {
        int months = durationMonths(enrollment.getContractDuration());
        BigDecimal planBase = monthlyRateForPlan(enrollment.getPlanType())
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal addOnTotal = addOnsTotal(enrollment.getAddOns(), enrollment.getBillingType());
        BigDecimal fullTotal = planBase.add(addOnTotal).setScale(2, RoundingMode.HALF_UP);
        if (enrollment.getBillingType() == BillingType.MONTHLY) {
            return fullTotal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        return fullTotal;
    }
}
