package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.IssueSeverity;

import java.time.Instant;
import java.util.UUID;

public record IssueResponse(
        UUID id,
        String sourceStep,
        String code,
        IssueSeverity severity,
        String message,
        String recommendedAction,
        String fieldName,
        String expectedValue,
        String actualValue,
        Double confidence,
        String evidenceSource,
        String evidenceText,
        Instant createdAt
) {
}
