package com.zamp.vendoronboarding.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NoOpLlmClientTest {

    private final NoOpLlmClient client = new NoOpLlmClient();

    @Test
    void complete_returnsFailureWhenApiKeyNotConfigured() {
        LlmCompletionResult result = client.complete(new LlmRequest("system", "user"));

        assertFalse(result.success());
        assertEquals("LLM API key is not configured.", result.errorMessage());
    }
}
