package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.StepStatus;

import java.time.Instant;

public record WorkflowStepLogResponse(
        String stepName,
        int stepOrder,
        StepStatus status,
        String summary,
        Instant startedAt,
        Instant completedAt,
        Long durationMs,
        String outputSnapshot,
        String errorMessage
) {
}
