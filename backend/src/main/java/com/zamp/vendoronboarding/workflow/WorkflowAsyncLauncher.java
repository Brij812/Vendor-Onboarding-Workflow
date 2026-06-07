package com.zamp.vendoronboarding.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkflowAsyncLauncher {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAsyncLauncher.class);

    private final WorkflowOrchestrator workflowOrchestrator;

    public WorkflowAsyncLauncher(WorkflowOrchestrator workflowOrchestrator) {
        this.workflowOrchestrator = workflowOrchestrator;
    }

    @Async("workflowTaskExecutor")
    public void runWorkflowAsync(UUID workflowRunId) {
        log.info("Async workflow execution queued | workflowRunId={}", workflowRunId);
        workflowOrchestrator.executeWorkflow(workflowRunId);
    }
}
