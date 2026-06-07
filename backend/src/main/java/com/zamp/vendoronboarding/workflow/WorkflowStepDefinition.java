package com.zamp.vendoronboarding.workflow;

public enum WorkflowStepDefinition {

    INTAKE_AGENT(1, "INTAKE_AGENT", "Receives and normalizes vendor submission data."),
    DOCUMENT_UNDERSTANDING_AGENT(2, "DOCUMENT_UNDERSTANDING_AGENT",
            "Extracts structured fields from uploaded vendor documents."),
    COMPLETENESS_AGENT(3, "COMPLETENESS_AGENT",
            "Checks whether required fields and documents are present."),
    FORMAT_VALIDATION_AGENT(4, "FORMAT_VALIDATION_AGENT",
            "Validates tax ID, bank code, email, and website formats."),
    CONSISTENCY_CHECK_AGENT(5, "CONSISTENCY_CHECK_AGENT",
            "Compares submitted fields against each other and against extracted document data."),
    DUPLICATE_RISK_AGENT(6, "DUPLICATE_RISK_AGENT",
            "Checks existing vendor records for duplicates or blocked vendors."),
    DECISION_AGENT(7, "DECISION_AGENT", "Creates final Approved, Pending, or Rejected decision."),
    COMMUNICATION_AGENT(8, "COMMUNICATION_AGENT", "Generates vendor follow-up communication."),
    AUDIT_SUMMARY_AGENT(9, "AUDIT_SUMMARY_AGENT", "Creates internal procurement audit summary.");

    private final int stepOrder;
    private final String stepName;
    private final String summary;

    WorkflowStepDefinition(int stepOrder, String stepName, String summary) {
        this.stepOrder = stepOrder;
        this.stepName = stepName;
        this.summary = summary;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getStepName() {
        return stepName;
    }

    public String getSummary() {
        return summary;
    }
}
