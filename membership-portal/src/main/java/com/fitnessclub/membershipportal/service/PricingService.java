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
     * Total for add-ons.
     * PT: PT_PER_SESSION * quantity (days/sessions).
     * Locker: LOCKER_RENTAL_PER_MONTH * (MONTHLY billing ? (quantity > 0 ? 1 : 0) : quantity)
     */
    public BigDecimal addOnsTotal(List<EnrollmentAddOn> addOns, BillingType billingType) {
        BigDecimal total = BigDecimal.ZERO;
        for (EnrollmentAddOn line : addOns) {
            BigDecimal unit = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
            int qty = line.getQuantity() != null ? line.getQuantity() : 0;

            if (billingType == BillingType.MONTHLY) {
                if (line.getAddOnType() == AddOnType.PERSONAL_TRAINING) {
                    // MONTHLY billing charges PT only for the first 30 days
                    int chargeQty = Math.min(qty, 30);
                    total = total.add(unit.multiply(BigDecimal.valueOf(chargeQty)));
                } else if (line.getAddOnType() == AddOnType.LOCKER_RENTAL) {
                    // MONTHLY billing charges locker only for the first month (has/not)
                    int chargeQty = qty > 0 ? 1 : 0;
                    total = total.add(unit.multiply(BigDecimal.valueOf(chargeQty)));
                } else {
                    // Fallback: charge raw quantity if an unknown add-on type appears
                    total = total.add(unit.multiply(BigDecimal.valueOf(qty)));
                }
            } else {
                total = total.add(unit.multiply(BigDecimal.valueOf(qty)));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Total amount: plan base + add-ons. For monthly billing we still store total;
     * UI can show "pay monthly" or "pay upfront" from billingType.
     */
    public BigDecimal computeTotal(MembershipEnrollment enrollment) {
        int planMonths = enrollment.getBillingType() == BillingType.MONTHLY
                ? 1
                : durationMonths(enrollment.getContractDuration());

        BigDecimal planBase = monthlyRateForPlan(enrollment.getPlanType())
                .multiply(BigDecimal.valueOf(planMonths))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal addOnTotal = addOnsTotal(enrollment.getAddOns(), enrollment.getBillingType());
        return planBase.add(addOnTotal).setScale(2, RoundingMode.HALF_UP);
    }
}
