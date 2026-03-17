package com.fitnessclub.membershipportal.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "enrollment_addons")
public class EnrollmentAddOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private MembershipEnrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "addon_type", nullable = false, length = 30)
    private AddOnType addOnType;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    public EnrollmentAddOn() {
    }

    public EnrollmentAddOn(AddOnType addOnType, int quantity, BigDecimal unitPrice) {
        this.addOnType = addOnType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MembershipEnrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(MembershipEnrollment enrollment) {
        this.enrollment = enrollment;
    }

    public AddOnType getAddOnType() {
        return addOnType;
    }

    public void setAddOnType(AddOnType addOnType) {
        this.addOnType = addOnType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
