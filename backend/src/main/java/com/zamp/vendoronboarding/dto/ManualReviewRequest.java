package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.ReviewerOutcome;
import jakarta.validation.constraints.NotNull;

public record ManualReviewRequest(
        @NotNull ReviewerOutcome reviewerOutcome,
        String reviewerNote
) {
}
