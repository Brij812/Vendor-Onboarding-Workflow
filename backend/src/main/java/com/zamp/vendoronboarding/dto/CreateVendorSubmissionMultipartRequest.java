package com.zamp.vendoronboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateVendorSubmissionMultipartRequest {

    @NotBlank(message = "legalName is required")
    private String legalName;

    @NotBlank(message = "country is required")
    private String country;

    private String website;

    @NotBlank(message = "contactEmail is required")
    private String contactEmail;

    private String taxId;
    private String bankAccountHolderName;
    private String bankCountry;
    private String bankCode;
    private String bankAccountLast4;
    private String businessCategory;

    public CreateVendorSubmissionRequest toJsonRequest() {
        return new CreateVendorSubmissionRequest(
                legalName,
                country,
                website,
                contactEmail,
                taxId,
                bankAccountHolderName,
                bankCountry,
                bankCode,
                bankAccountLast4,
                businessCategory
        );
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
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
}
