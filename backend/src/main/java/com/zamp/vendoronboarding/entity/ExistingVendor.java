package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.util.NameNormalizer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "existing_vendors")
public class ExistingVendor extends AuditableEntity {

    @Column(nullable = false)
    private String legalName;

    private String normalizedLegalName;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false, unique = true)
    private String taxId;

    private String bankAccountLast4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status;

    @Column(columnDefinition = "TEXT")
    private String riskNote;

    @PrePersist
    @PreUpdate
    void normalizeName() {
        normalizedLegalName = NameNormalizer.normalize(legalName);
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getNormalizedLegalName() {
        return normalizedLegalName;
    }

    public void setNormalizedLegalName(String normalizedLegalName) {
        this.normalizedLegalName = normalizedLegalName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBankAccountLast4() {
        return bankAccountLast4;
    }

    public void setBankAccountLast4(String bankAccountLast4) {
        this.bankAccountLast4 = bankAccountLast4;
    }

    public VendorStatus getStatus() {
        return status;
    }

    public void setStatus(VendorStatus status) {
        this.status = status;
    }

    public String getRiskNote() {
        return riskNote;
    }

    public void setRiskNote(String riskNote) {
        this.riskNote = riskNote;
    }
}
