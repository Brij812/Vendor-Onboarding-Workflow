package com.zamp.vendoronboarding.ai;

public record LlmRequest(
        String systemPrompt,
        String userPrompt,
        String purpose
) {
    public LlmRequest(String systemPrompt, String userPrompt) {
        this(systemPrompt, userPrompt, null);
    }

    public String purposeOrDefault() {
        return purpose != null && !purpose.isBlank() ? purpose : "general";
    }
}
