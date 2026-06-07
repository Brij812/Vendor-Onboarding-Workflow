package com.zamp.vendoronboarding.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingLlmClient.class);

    private final LlmClient delegate;

    public LoggingLlmClient(LlmClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public LlmCompletionResult complete(LlmRequest request) {
        String purpose = request.purposeOrDefault();
        int systemPromptChars = length(request.systemPrompt());
        int userPromptChars = length(request.userPrompt());

        log.info(
                "LLM call started | purpose={} | systemPromptChars={} | userPromptChars={}",
                purpose,
                systemPromptChars,
                userPromptChars
        );

        long startedAt = System.nanoTime();
        try {
            LlmCompletionResult result = delegate.complete(request);
            long durationMs = elapsedMs(startedAt);

            if (result.success()) {
                log.info(
                        "LLM call completed | purpose={} | durationMs={} | responseChars={}",
                        purpose,
                        durationMs,
                        length(result.content())
                );
            } else {
                log.warn(
                        "LLM call failed | purpose={} | durationMs={} | error={}",
                        purpose,
                        durationMs,
                        result.errorMessage()
                );
            }

            return result;
        } catch (RuntimeException ex) {
            long durationMs = elapsedMs(startedAt);
            log.error(
                    "LLM call threw exception | purpose={} | durationMs={} | error={}",
                    purpose,
                    durationMs,
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }

    private static long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }
}
