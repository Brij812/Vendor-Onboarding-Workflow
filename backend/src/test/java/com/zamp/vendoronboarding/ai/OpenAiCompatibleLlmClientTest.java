package com.zamp.vendoronboarding.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleLlmClientTest {

    private MockRestServiceServer server;
    private OpenAiCompatibleLlmClient client;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.example.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenAiCompatibleLlmClient(
                objectMapper,
                "test-api-key",
                "gpt-4o-mini",
                "https://api.example.com",
                5000
        );
        injectRestClient(client, builder.build());
    }

    @AfterEach
    void verifyServer() {
        server.verify();
    }

    @Test
    void complete_successResponse_returnsContent() {
        server.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"taxId\\":\\"29ABCDE1234F1Z5\\"}"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        LlmCompletionResult result = client.complete(new LlmRequest("system", "user"));

        assertTrue(result.success());
        assertEquals("{\"taxId\":\"29ABCDE1234F1Z5\"}", result.content());
    }

    @Test
    void complete_httpError_returnsFailure() {
        server.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        LlmCompletionResult result = client.complete(new LlmRequest("system", "user"));

        assertFalse(result.success());
        assertTrue(result.errorMessage().startsWith("LLM request failed:"));
    }

    @Test
    void complete_missingApiKey_returnsFailureWithoutNetworkCall() {
        OpenAiCompatibleLlmClient noKeyClient = new OpenAiCompatibleLlmClient(
                new ObjectMapper(),
                "",
                "gpt-4o-mini",
                "https://api.example.com",
                5000
        );

        LlmCompletionResult result = noKeyClient.complete(new LlmRequest("system", "user"));

        assertFalse(result.success());
        assertEquals("LLM API key is not configured.", result.errorMessage());
    }

    private void injectRestClient(OpenAiCompatibleLlmClient target, RestClient restClient) {
        try {
            java.lang.reflect.Field field = OpenAiCompatibleLlmClient.class.getDeclaredField("restClient");
            field.setAccessible(true);
            field.set(target, restClient);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
