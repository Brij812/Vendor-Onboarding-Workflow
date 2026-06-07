package com.zamp.vendoronboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateVendorSubmissionRequest(
        @NotBlank(message = "legalName is required")
        String legalName,
        @NotBlank(message = "country is required")
        String country,
        String website,
        @NotBlank(message = "contactEmail is required")
        String contactEmail,
        String taxId,
        String bankAccountHolderName,
        String bankCountry,
        String bankCode,
        String bankAccountLast4,
        String businessCategory
) {
}
