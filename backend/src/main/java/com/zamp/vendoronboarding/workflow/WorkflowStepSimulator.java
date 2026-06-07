package com.zamp.vendoronboarding.workflow;

public final class WorkflowStepSimulator {

    private WorkflowStepSimulator() {
    }

    public static String runningSummary(WorkflowStepDefinition step) {
        return switch (step) {
            case INTAKE_AGENT -> "Running intake and normalization checks...";
            case DOCUMENT_UNDERSTANDING_AGENT -> "Running document understanding checks...";
            case COMPLETENESS_AGENT -> "Running completeness checks...";
            case FORMAT_VALIDATION_AGENT -> "Running format validation checks...";
            case CONSISTENCY_CHECK_AGENT -> "Running consistency checks...";
            case DUPLICATE_RISK_AGENT -> "Running duplicate and risk checks...";
            case DECISION_AGENT -> "Running decision evaluation...";
            case COMMUNICATION_AGENT -> "Running communication draft generation...";
            case AUDIT_SUMMARY_AGENT -> "Running audit summary generation...";
        };
    }

    public static String outputSnapshot(WorkflowStepDefinition step) {
        return switch (step) {
            case INTAKE_AGENT ->
                    "{\"message\":\"Vendor submission received and prepared for workflow processing.\"}";
            case DOCUMENT_UNDERSTANDING_AGENT ->
                    "{\"message\":\"No PDF extraction performed in Phase 4. Document AI will be added later.\"}";
            case COMPLETENESS_AGENT ->
                    "{\"message\":\"Completeness rules will be implemented in a later phase.\"}";
            case FORMAT_VALIDATION_AGENT ->
                    "{\"message\":\"Format validation rules will be implemented in a later phase.\"}";
            case CONSISTENCY_CHECK_AGENT ->
                    "{\"message\":\"Consistency checks will be implemented in a later phase.\"}";
            case DUPLICATE_RISK_AGENT ->
                    "{\"message\":\"Duplicate and risk checks will be implemented in a later phase.\"}";
            case DECISION_AGENT ->
                    "{\"message\":\"Decision engine will be implemented in a later phase.\"}";
            case COMMUNICATION_AGENT ->
                    "{\"message\":\"Communication generation will be implemented in a later phase.\"}";
            case AUDIT_SUMMARY_AGENT ->
                    "{\"message\":\"Audit summary generation will be implemented in a later phase.\"}";
        };
    }

    public static WorkflowStepDefinition fromStepName(String stepName) {
        for (WorkflowStepDefinition definition : WorkflowStepDefinition.values()) {
            if (definition.getStepName().equals(stepName)) {
                return definition;
            }
        }
        throw new IllegalArgumentException("Unknown workflow step: " + stepName);
    }
}
