package com.zamp.vendoronboarding.workflow;

import com.zamp.vendoronboarding.entity.enums.IssueSeverity;

public record IssueDraft(
        String sourceStep,
        String code,
        IssueSeverity severity,
        String message,
        String recommendedAction,
        String fieldName,
        String expectedValue,
        String actualValue,
        Double confidence,
        String evidenceSource,
        String evidenceText
) {
    public static IssueDraft of(String sourceStep, String code, IssueSeverity severity,
                                String message, String recommendedAction, String fieldName) {
        return new IssueDraft(sourceStep, code, severity, message, recommendedAction, fieldName,
                null, null, null, null, null);
    }

    public static IssueDraft withValues(String sourceStep, String code, IssueSeverity severity,
                                        String message, String recommendedAction, String fieldName,
                                        String expectedValue, String actualValue) {
        return new IssueDraft(sourceStep, code, severity, message, recommendedAction, fieldName,
                expectedValue, actualValue, null, null, null);
    }

    public static IssueDraft withEvidence(String sourceStep, String code, IssueSeverity severity,
                                          String message, String recommendedAction, String fieldName,
                                          String expectedValue, String actualValue,
                                          Double confidence, String evidenceSource, String evidenceText) {
        return new IssueDraft(sourceStep, code, severity, message, recommendedAction, fieldName,
                expectedValue, actualValue, confidence, evidenceSource, evidenceText);
    }
}
