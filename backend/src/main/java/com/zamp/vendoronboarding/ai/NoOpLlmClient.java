package com.zamp.vendoronboarding.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(NoOpLlmClient.class);

    @Override
    public LlmCompletionResult complete(LlmRequest request) {
        log.warn(
                "LLM call skipped (no API key configured) | purpose={}",
                request.purposeOrDefault()
        );
        return LlmCompletionResult.failure("LLM API key is not configured.");
    }
}
