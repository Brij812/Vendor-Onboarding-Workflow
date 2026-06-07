package com.zamp.vendoronboarding.document;

public record StructuredDocumentExtractionResult(
        boolean success,
        StructuredDocumentFields fields,
        String rawLlmResponse,
        String errorMessage
) {
    public static StructuredDocumentExtractionResult success(StructuredDocumentFields fields, String rawLlmResponse) {
        return new StructuredDocumentExtractionResult(true, fields, rawLlmResponse, null);
    }

    public static StructuredDocumentExtractionResult failure(String rawLlmResponse, String errorMessage) {
        return new StructuredDocumentExtractionResult(false, null, rawLlmResponse, errorMessage);
    }
}
