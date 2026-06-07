package com.zamp.vendoronboarding.logging;

import org.slf4j.MDC;

import java.util.UUID;

public final class WorkflowMdc {

    public static final String WORKFLOW_RUN_ID = "workflowRunId";
    public static final String VENDOR_NAME = "vendorName";
    public static final String STEP_NAME = "stepName";
    public static final String STEP_ORDER = "stepOrder";
    public static final String AGENT = "agent";

    private WorkflowMdc() {
    }

    public static void setWorkflow(UUID workflowRunId, String vendorName) {
        put(WORKFLOW_RUN_ID, workflowRunId != null ? workflowRunId.toString() : null);
        put(VENDOR_NAME, vendorName);
    }

    public static void setStep(String stepName, int stepOrder) {
        put(STEP_NAME, stepName);
        put(AGENT, stepName);
        put(STEP_ORDER, String.valueOf(stepOrder));
    }

    public static void clear() {
        MDC.remove(WORKFLOW_RUN_ID);
        MDC.remove(VENDOR_NAME);
        MDC.remove(STEP_NAME);
        MDC.remove(STEP_ORDER);
        MDC.remove(AGENT);
    }

    private static void put(String key, String value) {
        if (value == null || value.isBlank()) {
            MDC.remove(key);
        } else {
            MDC.put(key, value);
        }
    }
}
