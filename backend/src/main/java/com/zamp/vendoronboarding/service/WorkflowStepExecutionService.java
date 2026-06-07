package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.repository.WorkflowStepLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class WorkflowStepExecutionService {

    private final WorkflowStepLogRepository workflowStepLogRepository;
    private final WorkflowRunService workflowRunService;

    public WorkflowStepExecutionService(WorkflowStepLogRepository workflowStepLogRepository,
                                        WorkflowRunService workflowRunService) {
        this.workflowStepLogRepository = workflowStepLogRepository;
        this.workflowRunService = workflowRunService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startStep(UUID stepLogId, String summary) {
        WorkflowStepLog stepLog = workflowStepLogRepository.findByIdWithWorkflowRun(stepLogId)
                .orElseThrow(() -> new IllegalStateException("Step log not found: " + stepLogId));

        stepLog.setStatus(StepStatus.RUNNING);
        stepLog.setStartedAt(Instant.now());
        stepLog.setSummary(summary);
        workflowStepLogRepository.saveAndFlush(stepLog);

        workflowRunService.updateCurrentStep(stepLog.getWorkflowRun().getId(), stepLog.getStepName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeStep(UUID stepLogId, String outputSnapshot, String summary) {
        completeStep(stepLogId, StepStatus.COMPLETED, outputSnapshot, summary);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeStep(UUID stepLogId, StepStatus status, String outputSnapshot, String summary) {
        WorkflowStepLog stepLog = workflowStepLogRepository.findById(stepLogId)
                .orElseThrow(() -> new IllegalStateException("Step log not found: " + stepLogId));

        Instant completedAt = Instant.now();
        stepLog.setStatus(status);
        stepLog.setCompletedAt(completedAt);
        stepLog.setSummary(summary);
        stepLog.setOutputSnapshot(outputSnapshot);
        if (stepLog.getStartedAt() != null) {
            stepLog.setDurationMs(Duration.between(stepLog.getStartedAt(), completedAt).toMillis());
        }
        workflowStepLogRepository.saveAndFlush(stepLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failStep(UUID stepLogId, String errorMessage) {
        WorkflowStepLog stepLog = workflowStepLogRepository.findById(stepLogId)
                .orElseThrow(() -> new IllegalStateException("Step log not found: " + stepLogId));

        Instant failedAt = Instant.now();
        stepLog.setStatus(StepStatus.FAILED);
        stepLog.setErrorMessage(errorMessage);
        stepLog.setCompletedAt(failedAt);
        if (stepLog.getStartedAt() != null) {
            stepLog.setDurationMs(Duration.between(stepLog.getStartedAt(), failedAt).toMillis());
        }
        workflowStepLogRepository.saveAndFlush(stepLog);
    }
}
