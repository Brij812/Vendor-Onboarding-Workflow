package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.VendorStatus;

import java.util.UUID;

public record ExistingVendorResponse(
        UUID id,
        String legalName,
        String country,
        String taxId,
        String bankAccountLast4,
        VendorStatus status,
        String riskNote
) {
}
