package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.WorkflowRerunResponse;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.exception.WorkflowRunNotFoundException;
import com.zamp.vendoronboarding.repository.AuditSummaryRepository;
import com.zamp.vendoronboarding.repository.CommunicationRepository;
import com.zamp.vendoronboarding.repository.DecisionRepository;
import com.zamp.vendoronboarding.repository.DocumentExtractionRepository;
import com.zamp.vendoronboarding.repository.IssueRepository;
import com.zamp.vendoronboarding.repository.ManualReviewRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import com.zamp.vendoronboarding.workflow.WorkflowAsyncLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class WorkflowRerunService {

    private final WorkflowRunRepository workflowRunRepository;
    private final DocumentExtractionRepository documentExtractionRepository;
    private final IssueRepository issueRepository;
    private final DecisionRepository decisionRepository;
    private final CommunicationRepository communicationRepository;
    private final AuditSummaryRepository auditSummaryRepository;
    private final ManualReviewRepository manualReviewRepository;
    private final WorkflowStepLogService workflowStepLogService;
    private final WorkflowAsyncLauncher workflowAsyncLauncher;

    public WorkflowRerunService(WorkflowRunRepository workflowRunRepository,
                                  DocumentExtractionRepository documentExtractionRepository,
                                  IssueRepository issueRepository,
                                  DecisionRepository decisionRepository,
                                  CommunicationRepository communicationRepository,
                                  AuditSummaryRepository auditSummaryRepository,
                                  ManualReviewRepository manualReviewRepository,
                                  WorkflowStepLogService workflowStepLogService,
                                  WorkflowAsyncLauncher workflowAsyncLauncher) {
        this.workflowRunRepository = workflowRunRepository;
        this.documentExtractionRepository = documentExtractionRepository;
        this.issueRepository = issueRepository;
        this.decisionRepository = decisionRepository;
        this.communicationRepository = communicationRepository;
        this.auditSummaryRepository = auditSummaryRepository;
        this.manualReviewRepository = manualReviewRepository;
        this.workflowStepLogService = workflowStepLogService;
        this.workflowAsyncLauncher = workflowAsyncLauncher;
    }

    @Transactional
    public WorkflowRerunResponse rerunWorkflow(String runId) {
        WorkflowRun run = resolveRun(runId);
        if (run.getStatus() == RunStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workflow run is currently running.");
        }

        UUID workflowRunId = run.getId();
        documentExtractionRepository.deleteByWorkflowRun_Id(workflowRunId);
        issueRepository.deleteByWorkflowRun_Id(workflowRunId);
        decisionRepository.deleteByWorkflowRun_Id(workflowRunId);
        communicationRepository.deleteByWorkflowRun_Id(workflowRunId);
        auditSummaryRepository.deleteByWorkflowRun_Id(workflowRunId);
        manualReviewRepository.deleteByWorkflowRun_Id(workflowRunId);

        workflowStepLogService.resetStepsForRerun(run);

        run.setStatus(RunStatus.PENDING);
        run.setFinalDecisionStatus(null);
        run.setRiskScore(null);
        run.setCurrentStep(null);
        run.setCompletedAt(null);
        run.setFailedAt(null);
        run.setErrorMessage(null);
        run.setStartedAt(null);
        workflowRunRepository.save(run);

        workflowAsyncLauncher.runWorkflowAsync(workflowRunId);
        return new WorkflowRerunResponse(workflowRunId);
    }

    private WorkflowRun resolveRun(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return workflowRunRepository.findById(uuid)
                    .orElseThrow(() -> new WorkflowRunNotFoundException(id));
        } catch (IllegalArgumentException ex) {
            return workflowRunRepository.findByDisplayRunId(id)
                    .orElseThrow(() -> new WorkflowRunNotFoundException(id));
        }
    }
}
