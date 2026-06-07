package com.zamp.vendoronboarding.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmClientConfig {

    @Bean
    @Primary
    public LlmClient llmClient(
            @Value("${app.llm.provider:openai}") String provider,
            @Value("${app.llm.api-key:}") String apiKey,
            OpenAiCompatibleLlmClient openAiCompatibleLlmClient,
            NoOpLlmClient noOpLlmClient) {
        if (apiKey == null || apiKey.isBlank()) {
            return new LoggingLlmClient(noOpLlmClient);
        }
        if ("openai".equalsIgnoreCase(provider)) {
            return new LoggingLlmClient(openAiCompatibleLlmClient);
        }
        return new LoggingLlmClient(noOpLlmClient);
    }
}
