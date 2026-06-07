package com.zamp.vendoronboarding.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowStepSimulatorTest {

    @Test
    void outputSnapshot_containsMessageForEachStep() {
        for (WorkflowStepDefinition step : WorkflowStepDefinition.values()) {
            String snapshot = WorkflowStepSimulator.outputSnapshot(step);
            assertTrue(snapshot.contains("\"message\""), step.name());
        }
    }

    @Test
    void runningSummary_isNotBlankForEachStep() {
        for (WorkflowStepDefinition step : WorkflowStepDefinition.values()) {
            String summary = WorkflowStepSimulator.runningSummary(step);
            assertTrue(summary != null && !summary.isBlank(), step.name());
        }
    }
}
