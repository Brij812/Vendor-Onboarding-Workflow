package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.util.NormalizationRules;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_submissions")
public class VendorSubmission extends AuditableEntity {

    private String legalName;
    private String normalizedLegalName;
    private String country;
    private String website;
    private String contactEmail;
    private String taxId;
    private String bankAccountHolderName;
    private String normalizedBankAccountHolderName;
    private String bankCountry;
    private String bankCode;
    private String bankAccountLast4;
    private String businessCategory;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    @PrePersist
    @PreUpdate
    void normalizeNames() {
        normalizedLegalName = NormalizationRules.normalizeName(legalName);
        normalizedBankAccountHolderName = NormalizationRules.normalizeName(bankAccountHolderName);
        if (country != null) {
            country = NormalizationRules.normalizeCountry(country);
        }
        if (bankCountry != null) {
            bankCountry = NormalizationRules.normalizeCountry(bankCountry);
        }
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBankAccountHolderName() {
        return bankAccountHolderName;
    }

    public void setBankAccountHolderName(String bankAccountHolderName) {
        this.bankAccountHolderName = bankAccountHolderName;
    }

    public String getNormalizedBankAccountHolderName() {
        return normalizedBankAccountHolderName;
    }

    public void setNormalizedBankAccountHolderName(String normalizedBankAccountHolderName) {
        this.normalizedBankAccountHolderName = normalizedBankAccountHolderName;
    }

    public String getBankCountry() {
        return bankCountry;
    }

    public void setBankCountry(String bankCountry) {
        this.bankCountry = bankCountry;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankAccountLast4() {
        return bankAccountLast4;
    }

    public void setBankAccountLast4(String bankAccountLast4) {
        this.bankAccountLast4 = bankAccountLast4;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }
}
