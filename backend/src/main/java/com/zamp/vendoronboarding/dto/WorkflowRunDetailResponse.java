package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.RunStatus;

import java.util.List;
import java.util.UUID;
public record WorkflowRunDetailResponse(
        UUID workflowRunId,
        String displayRunId,
        RunStatus runStatus,
        String currentStep,
        VendorSubmissionDetailResponse vendor,
        List<WorkflowStepLogResponse> steps,
        List<IssueResponse> issues,
        List<UploadedDocumentResponse> documents,
        Object decision,
        Object communication,
        Object auditSummary,
        ManualReviewResponse manualReview
) {
}
