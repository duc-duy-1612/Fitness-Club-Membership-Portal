package com.fitnessclub.membershipportal.service;

import com.fitnessclub.membershipportal.entity.*;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Simple pricing: plan base by type + duration, then add-ons. */
@Service
public class PricingService {

    private static final BigDecimal BASIC_MONTHLY = new BigDecimal("29.99");
    private static final BigDecimal PREMIUM_MONTHLY = new BigDecimal("59.99");
    private static final BigDecimal PERSONAL_TRAINING_PER_SESSION = new BigDecimal("25.00");
    private static final BigDecimal LOCKER_RENTAL_PER_MONTH = new BigDecimal("15.00");

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
     * Total for add-ons over the contract period.
     * Personal training: one-time per session. Locker: monthly for contract length.
     */
    public BigDecimal addOnsTotal(List<EnrollmentAddOn> addOns, ContractDuration duration) {
        int months = durationMonths(duration);
        BigDecimal total = BigDecimal.ZERO;
        for (EnrollmentAddOn line : addOns) {
            if (line.getAddOnType() == AddOnType.PERSONAL_TRAINING) {
                total = total.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
            } else {
                total = total.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity() * months)));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Total amount: plan base + add-ons. For monthly billing we still store total;
     * UI can show "pay monthly" or "pay upfront" from billingType.
     */
    public BigDecimal computeTotal(MembershipEnrollment enrollment) {
        BigDecimal planBase = planBaseForPeriod(enrollment.getPlanType(), enrollment.getContractDuration());
        BigDecimal addOnTotal = addOnsTotal(enrollment.getAddOns(), enrollment.getContractDuration());
        return planBase.add(addOnTotal).setScale(2, RoundingMode.HALF_UP);
    }
}
