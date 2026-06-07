package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.RunStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowRunSummaryResponse(
        UUID workflowRunId,
        String displayRunId,
        String vendorName,
        RunStatus runStatus,
        String currentStep,
        DecisionStatus finalDecisionStatus,
        Integer riskScore,
        String mainReason,
        Instant createdAt,
        Instant completedAt
) {
}
