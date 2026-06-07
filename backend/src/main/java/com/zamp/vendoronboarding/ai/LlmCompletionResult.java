package com.zamp.vendoronboarding.ai;

public record LlmCompletionResult(
        boolean success,
        String content,
        String errorMessage
) {
    public static LlmCompletionResult success(String content) {
        return new LlmCompletionResult(true, content, null);
    }

    public static LlmCompletionResult failure(String errorMessage) {
        return new LlmCompletionResult(false, null, errorMessage);
    }
}
