package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;

public record DecisionResponse(
        DecisionStatus status,
        Integer riskScore,
        String reasonSummary,
        String requiredActions,
        String triggeredRules
) {
}
