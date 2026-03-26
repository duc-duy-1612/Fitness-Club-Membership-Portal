package com.fitnessclub.membershipportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple data for the enrollment result page (no entity references).
 */
public class EnrollmentResultDto {

    private String memberName;
    private String planType;
    private String branchInfo;
    private String startDate;
    private String contractDuration;
    private String billingType;
    private List<String> addOnLines = new ArrayList<>();
    private String totalAmount;
    private String finalizeUrl;
    private String status; // DRAFT, FINALIZED

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getBranchInfo() {
        return branchInfo;
    }

    public void setBranchInfo(String branchInfo) {
        this.branchInfo = branchInfo;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getContractDuration() {
        return contractDuration;
    }

    public void setContractDuration(String contractDuration) {
        this.contractDuration = contractDuration;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public List<String> getAddOnLines() {
        return addOnLines;
    }

    public void setAddOnLines(List<String> addOnLines) {
        this.addOnLines = addOnLines != null ? addOnLines : new ArrayList<>();
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getFinalizeUrl() {
        return finalizeUrl;
    }

    public void setFinalizeUrl(String finalizeUrl) {
        this.finalizeUrl = finalizeUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
