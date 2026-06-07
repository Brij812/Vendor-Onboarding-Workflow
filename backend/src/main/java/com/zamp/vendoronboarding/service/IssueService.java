package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.IssueResponse;
import com.zamp.vendoronboarding.entity.Issue;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.repository.IssueRepository;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    private final IssueRepository issueRepository;

    public IssueService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Transactional
    public void persistIssues(WorkflowRun workflowRun, List<IssueDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return;
        }
        for (IssueDraft draft : drafts) {
            Issue issue = new Issue();
            issue.setWorkflowRun(workflowRun);
            issue.setSourceStep(draft.sourceStep());
            issue.setCode(draft.code());
            issue.setSeverity(draft.severity());
            issue.setMessage(draft.message());
            issue.setRecommendedAction(draft.recommendedAction());
            issue.setFieldName(draft.fieldName());
            issue.setExpectedValue(draft.expectedValue());
            issue.setActualValue(draft.actualValue());
            issue.setConfidence(draft.confidence());
            issue.setEvidenceSource(draft.evidenceSource());
            issue.setEvidenceText(draft.evidenceText());
            issueRepository.save(issue);
        }
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> findResponsesByWorkflowRunId(UUID workflowRunId) {
        return issueRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(workflowRunId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String findMainReasonByWorkflowRunId(UUID workflowRunId) {
        List<Issue> issues = issueRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(workflowRunId);
        if (issues.isEmpty()) {
            return null;
        }
        return issues.stream()
                .max(Comparator.comparing(Issue::getSeverity, severityRank()))
                .map(Issue::getMessage)
                .orElse(null);
    }

    private Comparator<IssueSeverity> severityRank() {
        return Comparator.comparingInt(severity -> switch (severity) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        });
    }

    private IssueResponse toResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getSourceStep(),
                issue.getCode(),
                issue.getSeverity(),
                issue.getMessage(),
                issue.getRecommendedAction(),
                issue.getFieldName(),
                issue.getExpectedValue(),
                issue.getActualValue(),
                issue.getConfidence(),
                issue.getEvidenceSource(),
                issue.getEvidenceText(),
                issue.getCreatedAt()
        );
    }
}
