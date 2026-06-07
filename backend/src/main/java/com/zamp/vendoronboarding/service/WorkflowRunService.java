package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.AuditSummaryResponse;
import com.zamp.vendoronboarding.dto.CommunicationResponse;
import com.zamp.vendoronboarding.dto.DecisionResponse;
import com.zamp.vendoronboarding.dto.IssueResponse;
import com.zamp.vendoronboarding.dto.ManualReviewResponse;
import com.zamp.vendoronboarding.dto.UploadedDocumentResponse;
import com.zamp.vendoronboarding.dto.VendorSubmissionDetailResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunDetailResponse;
import com.zamp.vendoronboarding.dto.WorkflowRunSummaryResponse;
import com.zamp.vendoronboarding.dto.WorkflowStepLogResponse;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.exception.WorkflowRunNotFoundException;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WorkflowRunService {

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowRunIdGenerator workflowRunIdGenerator;
    private final WorkflowStepLogService workflowStepLogService;
    private final IssueService issueService;
    private final DecisionService decisionService;
    private final CommunicationService communicationService;
    private final AuditSummaryService auditSummaryService;
    private final UploadedDocumentService uploadedDocumentService;
    private final ManualReviewService manualReviewService;

    public WorkflowRunService(WorkflowRunRepository workflowRunRepository,
                              WorkflowRunIdGenerator workflowRunIdGenerator,
                              WorkflowStepLogService workflowStepLogService,
                              IssueService issueService,
                              DecisionService decisionService,
                              CommunicationService communicationService,
                              AuditSummaryService auditSummaryService,
                              UploadedDocumentService uploadedDocumentService,
                              ManualReviewService manualReviewService) {
        this.workflowRunRepository = workflowRunRepository;
        this.workflowRunIdGenerator = workflowRunIdGenerator;
        this.workflowStepLogService = workflowStepLogService;
        this.issueService = issueService;
        this.decisionService = decisionService;
        this.communicationService = communicationService;
        this.auditSummaryService = auditSummaryService;
        this.uploadedDocumentService = uploadedDocumentService;
        this.manualReviewService = manualReviewService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRunning(UUID workflowRunId) {
        WorkflowRun run = getRequiredRun(workflowRunId);
        run.setStatus(RunStatus.RUNNING);
        if (run.getStartedAt() == null) {
            run.setStartedAt(Instant.now());
        }
        run.setCurrentStep(null);
        workflowRunRepository.saveAndFlush(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(UUID workflowRunId) {
        WorkflowRun run = getRequiredRun(workflowRunId);
        run.setStatus(RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());
        run.setCurrentStep(null);
        workflowRunRepository.saveAndFlush(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID workflowRunId, String errorMessage) {
        WorkflowRun run = getRequiredRun(workflowRunId);
        run.setStatus(RunStatus.FAILED);
        run.setFailedAt(Instant.now());
        run.setErrorMessage(errorMessage);
        run.setCurrentStep(null);
        workflowRunRepository.saveAndFlush(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCurrentStep(UUID workflowRunId, String stepName) {
        WorkflowRun run = getRequiredRun(workflowRunId);
        run.setCurrentStep(stepName);
        workflowRunRepository.saveAndFlush(run);
    }

    @Transactional
    public WorkflowRun createPendingRun(VendorSubmission submission) {
        WorkflowRun run = new WorkflowRun();
        run.setVendorSubmission(submission);
        run.setStatus(RunStatus.PENDING);
        run.setCurrentStep(null);
        workflowRunIdGenerator.assignRunIdentifiers(run);
        return workflowRunRepository.save(run);
    }

    @Transactional(readOnly = true)
    public WorkflowRun findByIdOrDisplayRunId(String id) {
        return resolveRun(id)
                .orElseThrow(() -> new WorkflowRunNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public WorkflowRunDetailResponse getRunDetail(String id) {
        WorkflowRun run = findByIdOrDisplayRunId(id);
        List<WorkflowStepLog> stepLogs = workflowStepLogService.findByWorkflowRunId(run.getId());
        return toDetailResponse(run, stepLogs);
    }

    @Transactional(readOnly = true)
    public List<WorkflowRunSummaryResponse> findAllSummaries() {
        return workflowRunRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkflowRunSummaryResponse> findReviewQueueSummaries() {
        return workflowRunRepository.findByFinalDecisionStatusOrderByCreatedAtDesc(DecisionStatus.PENDING).stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private java.util.Optional<WorkflowRun> resolveRun(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return workflowRunRepository.findWithVendorSubmissionById(uuid)
                    .or(() -> workflowRunRepository.findById(uuid));
        } catch (IllegalArgumentException ex) {
            return workflowRunRepository.findWithVendorSubmissionByDisplayRunId(id)
                    .or(() -> workflowRunRepository.findByDisplayRunId(id));
        }
    }

    WorkflowRunDetailResponse toDetailResponse(WorkflowRun run, List<WorkflowStepLog> stepLogs) {
        VendorSubmission submission = run.getVendorSubmission();
        DecisionResponse decision = decisionService.findResponseByWorkflowRunId(run.getId()).orElse(null);
        CommunicationResponse communication =
                communicationService.findResponseByWorkflowRunId(run.getId()).orElse(null);
        AuditSummaryResponse auditSummary =
                auditSummaryService.findResponseByWorkflowRunId(run.getId()).orElse(null);
        List<UploadedDocumentResponse> documents =
                uploadedDocumentService.findResponsesByWorkflowRunId(run.getId());
        ManualReviewResponse manualReview =
                manualReviewService.findResponseByWorkflowRunId(run.getId()).orElse(null);
        return new WorkflowRunDetailResponse(
                run.getId(),
                run.getDisplayRunId(),
                run.getStatus(),
                run.getCurrentStep(),
                toVendorDetail(submission),
                stepLogs.stream().map(this::toStepResponse).toList(),
                issueService.findResponsesByWorkflowRunId(run.getId()),
                documents,
                decision,
                communication,
                auditSummary,
                manualReview
        );
    }

    private WorkflowRunSummaryResponse toSummaryResponse(WorkflowRun run) {
        String vendorName = run.getVendorSubmission() != null
                ? run.getVendorSubmission().getLegalName()
                : null;

        var finalDecisionStatus = run.getFinalDecisionStatus();
        var riskScore = run.getRiskScore();
        if (finalDecisionStatus == null) {
            var decision = decisionService.findResponseByWorkflowRunId(run.getId());
            if (decision.isPresent()) {
                finalDecisionStatus = decision.get().status();
                if (riskScore == null) {
                    riskScore = decision.get().riskScore();
                }
            }
        }

        String mainReason = resolveMainReason(run.getId(), finalDecisionStatus);

        return new WorkflowRunSummaryResponse(
                run.getId(),
                run.getDisplayRunId(),
                vendorName,
                run.getStatus(),
                run.getCurrentStep(),
                finalDecisionStatus,
                riskScore,
                mainReason,
                run.getCreatedAt(),
                run.getCompletedAt()
        );
    }

    private String resolveMainReason(UUID workflowRunId, DecisionStatus finalDecisionStatus) {
        var decision = decisionService.findResponseByWorkflowRunId(workflowRunId);
        if (decision.isPresent() && decision.get().reasonSummary() != null
                && !decision.get().reasonSummary().isBlank()) {
            return decision.get().reasonSummary();
        }
        return issueService.findMainReasonByWorkflowRunId(workflowRunId);
    }

    private WorkflowRun getRequiredRun(UUID workflowRunId) {
        return workflowRunRepository.findById(workflowRunId)
                .orElseThrow(() -> new WorkflowRunNotFoundException(workflowRunId));
    }

    private VendorSubmissionDetailResponse toVendorDetail(VendorSubmission submission) {
        return new VendorSubmissionDetailResponse(
                submission.getLegalName(),
                submission.getNormalizedLegalName(),
                submission.getCountry(),
                submission.getWebsite(),
                submission.getContactEmail(),
                submission.getTaxId(),
                submission.getBankAccountHolderName(),
                submission.getNormalizedBankAccountHolderName(),
                submission.getBankCountry(),
                submission.getBankCode(),
                submission.getBankAccountLast4(),
                submission.getBusinessCategory()
        );
    }

    private WorkflowStepLogResponse toStepResponse(WorkflowStepLog stepLog) {
        return new WorkflowStepLogResponse(
                stepLog.getStepName(),
                stepLog.getStepOrder(),
                stepLog.getStatus(),
                stepLog.getSummary(),
                stepLog.getStartedAt(),
                stepLog.getCompletedAt(),
                stepLog.getDurationMs(),
                stepLog.getOutputSnapshot(),
                stepLog.getErrorMessage()
        );
    }
}
