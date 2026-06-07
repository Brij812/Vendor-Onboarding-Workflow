package com.zamp.vendoronboarding.dto;

import com.zamp.vendoronboarding.entity.enums.RunStatus;

import java.util.UUID;

public record CreateVendorSubmissionResponse(
        UUID submissionId,
        UUID workflowRunId,
        String displayRunId,
        RunStatus status,
        String message
) {
}
