package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.ReviewerOutcome;

import java.time.Instant;
import java.util.UUID;

public record ManualReviewResponse(
        UUID workflowRunId,
        ReviewerOutcome reviewerOutcome,
        String reviewerNote,
        Instant reviewedAt
) {
}
