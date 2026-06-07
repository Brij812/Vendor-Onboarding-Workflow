package com.zamp.vendoronboarding.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class DocumentStructuredExtractionService {

    private static final int MAX_TEXT_LENGTH = 8000;

    private static final String SYSTEM_PROMPT = """
            You extract structured vendor document fields from plain text.
            Return only valid JSON with no markdown fences or commentary.
            Do not invent missing values. Use JSON null when a field is not found.
            documentType must be one of: TAX_REGISTRATION, BANK_PROOF, COMPANY_REGISTRATION, COMPLIANCE_DECLARATION, UNKNOWN.
            documentDate must be YYYY-MM-DD or null.
            confidenceScore must be a number between 0 and 1 or null.
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public DocumentStructuredExtractionService(LlmClient llmClient, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public StructuredDocumentExtractionResult extract(String extractedText, DocumentType uploadedDocumentType) {
        if (extractedText == null || extractedText.isBlank()) {
            return StructuredDocumentExtractionResult.failure(
                    null,
                    "No extractable text for structured extraction"
            );
        }

        String truncatedText = truncate(extractedText);
        String userPrompt = buildUserPrompt(truncatedText, uploadedDocumentType);
        LlmCompletionResult completion = llmClient.complete(
                new LlmRequest(SYSTEM_PROMPT, userPrompt, "document_structured_extraction:" + uploadedDocumentType.name())
        );

        if (!completion.success()) {
            return StructuredDocumentExtractionResult.failure(null, completion.errorMessage());
        }

        return parseResponse(completion.content(), uploadedDocumentType);
    }

    StructuredDocumentExtractionResult parseResponse(String rawResponse, DocumentType uploadedDocumentType) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return StructuredDocumentExtractionResult.failure(rawResponse, "LLM returned blank structured response.");
        }

        try {
            String json = stripMarkdownFences(rawResponse.trim());
            JsonNode root = objectMapper.readTree(json);

            StructuredDocumentFields fields = new StructuredDocumentFields(
                    nullableText(root.get("legalEntityName")),
                    nullableText(root.get("taxId")),
                    nullableText(root.get("bankAccountHolderName")),
                    nullableText(root.get("country")),
                    parseDocumentType(root.get("documentType"), uploadedDocumentType),
                    parseDocumentDate(root.get("documentDate")),
                    parseConfidenceScore(root.get("confidenceScore"))
            );

            return StructuredDocumentExtractionResult.success(fields, rawResponse);
        } catch (Exception ex) {
            return StructuredDocumentExtractionResult.failure(rawResponse, "Failed to parse LLM JSON: " + ex.getMessage());
        }
    }

    private String buildUserPrompt(String extractedText, DocumentType uploadedDocumentType) {
        return """
                Extract structured fields from this vendor document text.
                Uploaded document type hint: %s

                Return JSON with exactly these keys:
                legalEntityName, taxId, bankAccountHolderName, country, documentType, documentDate, confidenceScore

                Document text:
                %s
                """.formatted(uploadedDocumentType.name(), extractedText);
    }

    private String truncate(String text) {
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH);
    }

    String stripMarkdownFences(String value) {
        if (value.startsWith("```")) {
            int firstNewline = value.indexOf('\n');
            if (firstNewline >= 0) {
                value = value.substring(firstNewline + 1);
            }
            int closingFence = value.lastIndexOf("```");
            if (closingFence >= 0) {
                value = value.substring(0, closingFence);
            }
        }
        return value.trim();
    }

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private DocumentType parseDocumentType(JsonNode node, DocumentType uploadedDocumentType) {
        if (node == null || node.isNull()) {
            return uploadedDocumentType;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return uploadedDocumentType;
        }
        try {
            return DocumentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return DocumentType.UNKNOWN;
        }
    }

    private LocalDate parseDocumentDate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private Double parseConfidenceScore(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return clampConfidence(node.asDouble());
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return clampConfidence(Double.parseDouble(value.trim()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double clampConfidence(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
