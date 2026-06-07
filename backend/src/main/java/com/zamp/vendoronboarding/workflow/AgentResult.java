package com.zamp.vendoronboarding.workflow;

import com.zamp.vendoronboarding.entity.enums.StepStatus;

import java.util.Collections;
import java.util.List;

public record AgentResult(
        String stepName,
        StepStatus status,
        String summary,
        String outputSnapshot,
        List<IssueDraft> issues
) {
    public static AgentResult completed(String stepName, String summary, String outputSnapshot) {
        return new AgentResult(stepName, StepStatus.COMPLETED, summary, outputSnapshot, Collections.emptyList());
    }

    public static AgentResult warning(String stepName, String summary, String outputSnapshot, List<IssueDraft> issues) {
        return new AgentResult(stepName, StepStatus.WARNING, summary, outputSnapshot, issues);
    }

    public static AgentResult failed(String stepName, String summary) {
        return new AgentResult(stepName, StepStatus.FAILED, summary, null, Collections.emptyList());
    }

    public static AgentResult skipped(String stepName, String summary, String outputSnapshot) {
        return new AgentResult(stepName, StepStatus.SKIPPED, summary, outputSnapshot, Collections.emptyList());
    }
}
