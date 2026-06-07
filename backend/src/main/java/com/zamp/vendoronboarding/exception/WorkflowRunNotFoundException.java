package com.zamp.vendoronboarding.exception;

import java.util.UUID;

public class WorkflowRunNotFoundException extends RuntimeException {

    public WorkflowRunNotFoundException(String id) {
        super("Workflow run not found: " + id);
    }

    public WorkflowRunNotFoundException(UUID id) {
        super("Workflow run not found: " + id);
    }
}
