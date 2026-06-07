package com.zamp.vendoronboarding.document;

public record DocumentTextExtractionResult(
        boolean success,
        String extractedText,
        int characterCount,
        String errorMessage
) {
    public static DocumentTextExtractionResult success(String extractedText) {
        String value = extractedText != null ? extractedText : "";
        return new DocumentTextExtractionResult(true, value, value.length(), null);
    }

    public static DocumentTextExtractionResult failure(String errorMessage) {
        return new DocumentTextExtractionResult(false, null, 0, errorMessage);
    }
}
