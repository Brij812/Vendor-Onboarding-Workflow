package com.zamp.vendoronboarding.dto;

public record VendorSubmissionDetailResponse(
        String legalName,
        String normalizedLegalName,
        String country,
        String website,
        String contactEmail,
        String taxId,
        String bankAccountHolderName,
        String normalizedBankAccountHolderName,
        String bankCountry,
        String bankCode,
        String bankAccountLast4,
        String businessCategory
) {
}
