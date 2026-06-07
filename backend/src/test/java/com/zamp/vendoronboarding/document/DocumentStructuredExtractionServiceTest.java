package com.zamp.vendoronboarding.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentStructuredExtractionServiceTest {

    @Mock
    private LlmClient llmClient;

    private DocumentStructuredExtractionService service;

    @BeforeEach
    void setUp() {
        service = new DocumentStructuredExtractionService(llmClient, new ObjectMapper());
    }

    @Test
    void extract_validJsonResponse_returnsParsedFields() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("""
                {
                  "legalEntityName": "BrightLayer Technologies Pvt Ltd",
                  "taxId": "29ABCDE1234F1Z5",
                  "bankAccountHolderName": null,
                  "country": "India",
                  "documentType": "TAX_REGISTRATION",
                  "documentDate": "2024-01-15",
                  "confidenceScore": 0.91
                }
                """));

        StructuredDocumentExtractionResult result =
                service.extract("Tax registration certificate for BrightLayer", DocumentType.TAX_REGISTRATION);

        assertTrue(result.success());
        assertEquals("BrightLayer Technologies Pvt Ltd", result.fields().legalEntityName());
        assertEquals("29ABCDE1234F1Z5", result.fields().taxId());
        assertNull(result.fields().bankAccountHolderName());
        assertEquals("India", result.fields().country());
        assertEquals(DocumentType.TAX_REGISTRATION, result.fields().documentType());
        assertEquals(LocalDate.of(2024, 1, 15), result.fields().documentDate());
        assertEquals(0.91, result.fields().confidenceScore());
    }

    @Test
    void parseResponse_stripsMarkdownFences() {
        StructuredDocumentExtractionResult result = service.parseResponse("""
                ```json
                {
                  "legalEntityName": "Acme Pvt Ltd",
                  "taxId": null,
                  "bankAccountHolderName": null,
                  "country": null,
                  "documentType": "UNKNOWN",
                  "documentDate": null,
                  "confidenceScore": null
                }
                ```
                """, DocumentType.COMPANY_REGISTRATION);

        assertTrue(result.success());
        assertEquals("Acme Pvt Ltd", result.fields().legalEntityName());
        assertEquals(DocumentType.UNKNOWN, result.fields().documentType());
    }

    @Test
    void extract_invalidJson_returnsFailure() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("not json"));

        StructuredDocumentExtractionResult result =
                service.extract("Some document text", DocumentType.BANK_PROOF);

        assertFalse(result.success());
        assertTrue(result.errorMessage().startsWith("Failed to parse LLM JSON:"));
    }

    @Test
    void extract_blankText_returnsFailureWithoutCallingLlm() {
        StructuredDocumentExtractionResult result = service.extract("   ", DocumentType.TAX_REGISTRATION);

        assertFalse(result.success());
        assertEquals("No extractable text for structured extraction", result.errorMessage());
    }

    @Test
    void stripMarkdownFences_removesFenceWrapper() {
        assertEquals("{\"taxId\":\"123\"}", service.stripMarkdownFences("```json\n{\"taxId\":\"123\"}\n```"));
    }
}
