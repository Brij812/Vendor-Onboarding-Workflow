package com.zamp.vendoronboarding.dto;

import java.time.Instant;

public record HealthResponse(
        String appName,
        String status,
        Instant timestamp
) {
}
