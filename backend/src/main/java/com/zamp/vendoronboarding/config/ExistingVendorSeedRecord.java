package com.zamp.vendoronboarding.config;

import com.zamp.vendoronboarding.entity.enums.VendorStatus;

public record ExistingVendorSeedRecord(
        String legalName,
        String country,
        String taxId,
        String bankAccountLast4,
        VendorStatus status,
        String riskNote
) {
}
