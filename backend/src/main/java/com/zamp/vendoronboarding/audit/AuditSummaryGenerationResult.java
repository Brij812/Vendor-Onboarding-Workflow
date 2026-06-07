package com.zamp.vendoronboarding.audit;

public record AuditSummaryGenerationResult(
        boolean success,
        AuditSummaryDraft draft,
        String errorMessage
) {
    public static AuditSummaryGenerationResult success(AuditSummaryDraft draft) {
        return new AuditSummaryGenerationResult(true, draft, null);
    }

    public static AuditSummaryGenerationResult fallback(AuditSummaryDraft draft, String errorMessage) {
        return new AuditSummaryGenerationResult(false, draft, errorMessage);
    }
}
