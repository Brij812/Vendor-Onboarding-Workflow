package com.zamp.vendoronboarding.decision;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;

import java.util.List;

public record DecisionEvaluation(
        DecisionStatus status,
        int riskScore,
        String reasonSummary,
        List<String> requiredActions,
        List<String> triggeredRules,
        int issueCount,
        int criticalIssueCount,
        int highIssueCount,
        int mediumIssueCount,
        int lowIssueCount
) {
}
