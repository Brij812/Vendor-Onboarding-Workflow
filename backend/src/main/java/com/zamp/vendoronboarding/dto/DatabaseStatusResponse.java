package com.zamp.vendoronboarding.dto;

import java.time.Instant;

public record DatabaseStatusResponse(
        boolean databaseConnected,
        long existingVendorCount,
        Instant timestamp
) {
}
