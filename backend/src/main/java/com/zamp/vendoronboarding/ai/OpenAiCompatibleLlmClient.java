package com.zamp.vendoronboarding.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleLlmClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleLlmClient(
            ObjectMapper objectMapper,
            @Value("${app.llm.api-key:}") String apiKey,
            @Value("${app.llm.model:gpt-4o-mini}") String model,
            @Value("${app.llm.base-url:https://api.openai.com}") String baseUrl,
            @Value("${app.llm.timeout-ms:30000}") long timeoutMs) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(createRequestFactory(timeoutMs))
                .build();
    }

    @Override
    public LlmCompletionResult complete(LlmRequest request) {
        if (apiKey.isBlank()) {
            return LlmCompletionResult.failure("LLM API key is not configured.");
        }

        String purpose = request.purposeOrDefault();
        long startedAt = System.nanoTime();

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "temperature", 0,
                    "messages", List.of(
                            Map.of("role", "system", "content", request.systemPrompt()),
                            Map.of("role", "user", "content", request.userPrompt())
                    )
            );

            log.info(
                    "External LLM HTTP request | purpose={} | model={} | endpoint=/v1/chat/completions",
                    purpose,
                    model
            );

            String responseBody = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;

            if (responseBody == null || responseBody.isBlank()) {
                log.warn(
                        "External LLM HTTP response empty | purpose={} | model={} | durationMs={}",
                        purpose,
                        model,
                        durationMs
                );
                return LlmCompletionResult.failure("LLM returned an empty response.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                log.warn(
                        "External LLM HTTP response missing content | purpose={} | model={} | durationMs={}",
                        purpose,
                        model,
                        durationMs
                );
                return LlmCompletionResult.failure("LLM response did not contain message content.");
            }

            String content = contentNode.asText();
            if (content.isBlank()) {
                log.warn(
                        "External LLM HTTP response blank content | purpose={} | model={} | durationMs={}",
                        purpose,
                        model,
                        durationMs
                );
                return LlmCompletionResult.failure("LLM returned blank message content.");
            }

            log.info(
                    "External LLM HTTP response received | purpose={} | model={} | durationMs={} | responseChars={}",
                    purpose,
                    model,
                    durationMs,
                    content.length()
            );

            return LlmCompletionResult.success(content);
        } catch (RestClientException ex) {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            log.warn(
                    "External LLM HTTP request failed | purpose={} | model={} | durationMs={} | error={}",
                    purpose,
                    model,
                    durationMs,
                    ex.getMessage()
            );
            return LlmCompletionResult.failure("LLM request failed: " + ex.getMessage());
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            log.warn(
                    "External LLM response parse failed | purpose={} | model={} | durationMs={} | error={}",
                    purpose,
                    model,
                    durationMs,
                    ex.getMessage(),
                    ex
            );
            return LlmCompletionResult.failure("Failed to parse LLM response: " + ex.getMessage());
        }
    }

    private org.springframework.http.client.SimpleClientHttpRequestFactory createRequestFactory(long timeoutMs) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        int timeout = (int) Math.min(Math.max(timeoutMs, 1000L), Integer.MAX_VALUE);
        factory.setConnectTimeout(Duration.ofMillis(timeout));
        factory.setReadTimeout(Duration.ofMillis(timeout));
        return factory;
    }
}
