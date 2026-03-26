package com.fitnessclub.membershipportal.dto;

import com.fitnessclub.membershipportal.entity.AddOnType;
import com.fitnessclub.membershipportal.entity.BillingType;
import com.fitnessclub.membershipportal.entity.ContractDuration;
import com.fitnessclub.membershipportal.entity.PlanType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request body for creating or updating an enrollment (plan + cart).
 */
public class EnrollmentRequest {

    // Member info (for new enrollment)
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String healthGoals;
    private String email;

    // Plan
    private PlanType planType;
    private Integer primaryBranchId;  // Required for BASIC; ignored for PREMIUM
    private LocalDate startDate;
    private ContractDuration contractDuration;
    private BillingType billingType;

    // Add-ons: list of { type, quantity }
    private List<AddOnItem> addOns = new ArrayList<>();

    public static class AddOnItem {
        private AddOnType type;
        private int quantity = 1;

        public AddOnType getType() {
            return type;
        }

        public void setType(AddOnType type) {
            this.type = type;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getHealthGoals() {
        return healthGoals;
    }

    public void setHealthGoals(String healthGoals) {
        this.healthGoals = healthGoals;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public Integer getPrimaryBranchId() {
        return primaryBranchId;
    }

    public void setPrimaryBranchId(Integer primaryBranchId) {
        this.primaryBranchId = primaryBranchId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public ContractDuration getContractDuration() {
        return contractDuration;
    }

    public void setContractDuration(ContractDuration contractDuration) {
        this.contractDuration = contractDuration;
    }

    public BillingType getBillingType() {
        return billingType;
    }

    public void setBillingType(BillingType billingType) {
        this.billingType = billingType;
    }

    public List<AddOnItem> getAddOns() {
        return addOns;
    }

    public void setAddOns(List<AddOnItem> addOns) {
        this.addOns = addOns != null ? addOns : new ArrayList<>();
    }
}
