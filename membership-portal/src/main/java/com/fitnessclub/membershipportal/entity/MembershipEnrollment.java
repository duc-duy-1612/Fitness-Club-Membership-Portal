package com.fitnessclub.membershipportal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One enrollment = one member + plan + branch + start date + duration + billing + add-ons.
 * Status DRAFT = in cart; FINALIZED = contract signed/PDF generated.
 */
@Entity
@Table(name = "membership_enrollments")
public class MembershipEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private PlanType planType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_branch_id")
    private Branch primaryBranch; // Basic: 1 branch; Premium: null (all branches)

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_duration", nullable = false, length = 20)
    private ContractDuration contractDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_type", nullable = false, length = 20)
    private BillingType billingType;

    @Column(name = "plan_base_amount", precision = 10, scale = 2)
    private BigDecimal planBaseAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 20, nullable = false)
    private String status = "DRAFT"; // DRAFT, FINALIZED

    @Column(name = "contract_pdf_path", length = 500)
    private String contractPdfPath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnrollmentAddOn> addOns = new ArrayList<>();

    public MembershipEnrollment() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public Branch getPrimaryBranch() {
        return primaryBranch;
    }

    public void setPrimaryBranch(Branch primaryBranch) {
        this.primaryBranch = primaryBranch;
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

    public BigDecimal getPlanBaseAmount() {
        return planBaseAmount;
    }

    public void setPlanBaseAmount(BigDecimal planBaseAmount) {
        this.planBaseAmount = planBaseAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContractPdfPath() {
        return contractPdfPath;
    }

    public void setContractPdfPath(String contractPdfPath) {
        this.contractPdfPath = contractPdfPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<EnrollmentAddOn> getAddOns() {
        return addOns;
    }

    public void setAddOns(List<EnrollmentAddOn> addOns) {
        this.addOns = addOns;
    }

    public void addAddOn(EnrollmentAddOn addOn) {
        addOns.add(addOn);
        addOn.setEnrollment(this);
    }
}
