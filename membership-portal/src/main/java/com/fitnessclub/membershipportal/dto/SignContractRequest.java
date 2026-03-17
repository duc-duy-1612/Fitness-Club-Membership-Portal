package com.fitnessclub.membershipportal.dto;

/**
 * Request body for POST /api/enrollments/{id}/sign-pdf
 */
public class SignContractRequest {
    private String signatureDataUrl;

    public String getSignatureDataUrl() {
        return signatureDataUrl;
    }

    public void setSignatureDataUrl(String signatureDataUrl) {
        this.signatureDataUrl = signatureDataUrl;
    }
}
