package com.zamp.vendoronboarding.communication;

public record CommunicationGenerationResult(
        boolean success,
        CommunicationDraft draft,
        String errorMessage
) {
    public static CommunicationGenerationResult success(CommunicationDraft draft) {
        return new CommunicationGenerationResult(true, draft, null);
    }

    public static CommunicationGenerationResult fallback(CommunicationDraft draft, String errorMessage) {
        return new CommunicationGenerationResult(false, draft, errorMessage);
    }
}
