package com.zamp.vendoronboarding.audit;

import com.zamp.vendoronboarding.entity.enums.GenerationMethod;

public record AuditSummaryDraft(
        String summary,
        GenerationMethod generationMethod,
        String rawLlmResponse
) {
    public static AuditSummaryDraft fromLlm(String summary, String rawLlmResponse) {
        return new AuditSummaryDraft(summary, GenerationMethod.LLM, rawLlmResponse);
    }

    public static AuditSummaryDraft fromFallback(String summary, String errorMessage) {
        return new AuditSummaryDraft(summary, GenerationMethod.FALLBACK, errorMessage);
    }
}
