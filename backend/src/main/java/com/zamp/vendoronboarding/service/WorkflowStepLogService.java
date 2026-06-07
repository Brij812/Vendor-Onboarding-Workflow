package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.repository.WorkflowStepLogRepository;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowStepLogService {

    private final WorkflowStepLogRepository workflowStepLogRepository;

    public WorkflowStepLogService(WorkflowStepLogRepository workflowStepLogRepository) {
        this.workflowStepLogRepository = workflowStepLogRepository;
    }

    @Transactional
    public List<WorkflowStepLog> createPendingSteps(WorkflowRun workflowRun) {
        List<WorkflowStepLog> stepLogs = new ArrayList<>();
        for (WorkflowStepDefinition definition : WorkflowStepDefinition.values()) {
            WorkflowStepLog stepLog = new WorkflowStepLog();
            stepLog.setWorkflowRun(workflowRun);
            stepLog.setStepName(definition.getStepName());
            stepLog.setStepOrder(definition.getStepOrder());
            stepLog.setStatus(StepStatus.PENDING);
            stepLog.setSummary(definition.getSummary());
            stepLogs.add(workflowStepLogRepository.save(stepLog));
        }
        return stepLogs;
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepLog> findByWorkflowRunId(java.util.UUID workflowRunId) {
        return workflowStepLogRepository.findByWorkflowRun_IdOrderByStepOrderAsc(workflowRunId);
    }

    @Transactional
    public void resetStepsForRerun(WorkflowRun workflowRun) {
        List<WorkflowStepLog> stepLogs =
                workflowStepLogRepository.findByWorkflowRun_IdOrderByStepOrderAsc(workflowRun.getId());
        for (WorkflowStepLog stepLog : stepLogs) {
            WorkflowStepDefinition definition = WorkflowStepDefinition.valueOf(stepLog.getStepName());
            stepLog.setStatus(StepStatus.PENDING);
            stepLog.setSummary(definition.getSummary());
            stepLog.setStartedAt(null);
            stepLog.setCompletedAt(null);
            stepLog.setDurationMs(null);
            stepLog.setOutputSnapshot(null);
            stepLog.setErrorMessage(null);
            workflowStepLogRepository.save(stepLog);
        }
    }
}
