package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.GenerationMethod;

public record AuditSummaryResponse(
        String summary,
        GenerationMethod generationMethod
) {
}
