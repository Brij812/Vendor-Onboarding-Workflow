package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.ManualReviewRequest;
import com.zamp.vendoronboarding.dto.ManualReviewResponse;
import com.zamp.vendoronboarding.entity.Decision;
import com.zamp.vendoronboarding.entity.ManualReview;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.ReviewerOutcome;
import com.zamp.vendoronboarding.exception.WorkflowRunNotFoundException;
import com.zamp.vendoronboarding.repository.DecisionRepository;
import com.zamp.vendoronboarding.repository.ManualReviewRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManualReviewService {

    private final ManualReviewRepository manualReviewRepository;
    private final DecisionRepository decisionRepository;
    private final WorkflowRunRepository workflowRunRepository;

    public ManualReviewService(ManualReviewRepository manualReviewRepository,
                               DecisionRepository decisionRepository,
                               WorkflowRunRepository workflowRunRepository) {
        this.manualReviewRepository = manualReviewRepository;
        this.decisionRepository = decisionRepository;
        this.workflowRunRepository = workflowRunRepository;
    }

    @Transactional
    public ManualReviewResponse saveManualReview(String runId, ManualReviewRequest request) {
        WorkflowRun run = resolveRun(runId);
        Decision decision = decisionRepository.findByWorkflowRun_Id(run.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No automated decision exists for this workflow run."));

        DecisionStatus updatedStatus = mapOutcomeToDecisionStatus(request.reviewerOutcome());
        decision.setStatus(updatedStatus);
        String manualSummary = buildManualSummary(request.reviewerOutcome(), request.reviewerNote());
        decision.setReasonSummary(appendManualReviewSummary(decision.getReasonSummary(), manualSummary));
        decisionRepository.save(decision);

        run.setFinalDecisionStatus(updatedStatus);
        workflowRunRepository.save(run);

        ManualReview manualReview = manualReviewRepository.findByWorkflowRun_Id(run.getId())
                .orElseGet(ManualReview::new);
        manualReview.setWorkflowRun(run);
        manualReview.setReviewerOutcome(request.reviewerOutcome());
        manualReview.setReviewerNote(request.reviewerNote());
        manualReview.setReviewedAt(Instant.now());
        ManualReview saved = manualReviewRepository.save(manualReview);

        return toResponse(run.getId(), saved);
    }

    @Transactional(readOnly = true)
    public Optional<ManualReviewResponse> findResponseByWorkflowRunId(UUID workflowRunId) {
        return manualReviewRepository.findByWorkflowRun_Id(workflowRunId)
                .map(review -> toResponse(workflowRunId, review));
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

    private DecisionStatus mapOutcomeToDecisionStatus(ReviewerOutcome outcome) {
        return switch (outcome) {
            case APPROVED_AFTER_REVIEW -> DecisionStatus.APPROVED;
            case REJECTED_AFTER_REVIEW -> DecisionStatus.REJECTED;
            case REQUEST_MORE_INFO -> DecisionStatus.PENDING;
        };
    }

    private String buildManualSummary(ReviewerOutcome outcome, String note) {
        String trimmedNote = note != null ? note.trim() : "";
        if (trimmedNote.isEmpty()) {
            return "Manual review: " + outcome.name();
        }
        return "Manual review: " + outcome.name() + " — " + trimmedNote;
    }

    private String appendManualReviewSummary(String existingSummary, String manualSummary) {
        if (existingSummary == null || existingSummary.isBlank()) {
            return manualSummary;
        }
        return existingSummary + " " + manualSummary;
    }

    private ManualReviewResponse toResponse(UUID workflowRunId, ManualReview review) {
        return new ManualReviewResponse(
                workflowRunId,
                review.getReviewerOutcome(),
                review.getReviewerNote(),
                review.getReviewedAt()
        );
    }
}
