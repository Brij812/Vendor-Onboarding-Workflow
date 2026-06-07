package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.document.DocumentStructuredExtractionService;
import com.zamp.vendoronboarding.document.DocumentTextExtractionResult;
import com.zamp.vendoronboarding.document.DocumentTextExtractionService;
import com.zamp.vendoronboarding.document.StructuredDocumentExtractionResult;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.service.DocumentExtractionService;
import com.zamp.vendoronboarding.service.UploadedDocumentService;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentUnderstandingAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "DOCUMENT_UNDERSTANDING_AGENT";
    private static final int TEXT_PREVIEW_LENGTH = 200;

    private final UploadedDocumentService uploadedDocumentService;
    private final DocumentTextExtractionService documentTextExtractionService;
    private final DocumentStructuredExtractionService documentStructuredExtractionService;
    private final DocumentExtractionService documentExtractionService;
    private final ObjectMapper objectMapper;
    private final boolean llmEnabled;

    public DocumentUnderstandingAgent(UploadedDocumentService uploadedDocumentService,
                                      DocumentTextExtractionService documentTextExtractionService,
                                      DocumentStructuredExtractionService documentStructuredExtractionService,
                                      DocumentExtractionService documentExtractionService,
                                      ObjectMapper objectMapper,
                                      @Value("${app.llm.api-key:}") String apiKey) {
        this.uploadedDocumentService = uploadedDocumentService;
        this.documentTextExtractionService = documentTextExtractionService;
        this.documentStructuredExtractionService = documentStructuredExtractionService;
        this.documentExtractionService = documentExtractionService;
        this.objectMapper = objectMapper;
        this.llmEnabled = apiKey != null && !apiKey.isBlank();
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.DOCUMENT_UNDERSTANDING_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        List<UploadedDocument> documents =
                uploadedDocumentService.findByWorkflowRunId(context.getWorkflowRunId());

        if (documents.isEmpty()) {
            Map<String, Object> snapshot = buildSnapshot(0, 0, 0, 0, 0, List.of(), fieldCounts(List.of()), null);
            context.setExtractedDocumentExtractions(List.of());
            return AgentResult.skipped(
                    SOURCE_STEP,
                    "Skipped: no documents uploaded.",
                    toJson(snapshot)
            );
        }

        int processedDocuments = 0;
        int failedDocuments = 0;
        int successfulAiExtractions = 0;
        int fallbackExtractions = 0;
        List<Map<String, Object>> documentSummaries = new ArrayList<>();
        List<DocumentExtraction> savedExtractions = new ArrayList<>();

        for (UploadedDocument document : documents) {
            DocumentTextExtractionResult textResult =
                    documentTextExtractionService.extract(document.getStoragePath());

            if (!textResult.success()) {
                failedDocuments++;
                documentSummaries.add(buildDocumentSummary(
                        document,
                        "FAILED",
                        null,
                        null,
                        null
                ));
                continue;
            }

            uploadedDocumentService.updateExtractedText(document.getId(), textResult.extractedText());
            processedDocuments++;

            StructuredDocumentExtractionResult structuredResult =
                    documentStructuredExtractionService.extract(textResult.extractedText(), document.getDocumentType());

            DocumentExtraction savedExtraction;
            if (structuredResult.success()) {
                savedExtraction = documentExtractionService.saveLlmExtraction(
                        document.getWorkflowRun(),
                        document,
                        structuredResult.fields(),
                        structuredResult.rawLlmResponse()
                );
                successfulAiExtractions++;
                documentSummaries.add(buildDocumentSummary(
                        document,
                        "SUCCESS",
                        ExtractionMethod.LLM.name(),
                        textResult.extractedText(),
                        structuredResult.fields().confidenceScore()
                ));
            } else {
                savedExtraction = documentExtractionService.saveFallbackExtraction(
                        document.getWorkflowRun(),
                        document,
                        structuredResult.rawLlmResponse() != null
                                ? structuredResult.rawLlmResponse()
                                : structuredResult.errorMessage()
                );
                fallbackExtractions++;
                documentSummaries.add(buildDocumentSummary(
                        document,
                        "SUCCESS",
                        ExtractionMethod.FALLBACK.name(),
                        textResult.extractedText(),
                        null
                ));
            }
            savedExtractions.add(savedExtraction);
        }

        context.setExtractedDocumentExtractions(savedExtractions);

        Map<String, Object> snapshot = buildSnapshot(
                documents.size(),
                processedDocuments,
                failedDocuments,
                successfulAiExtractions,
                fallbackExtractions,
                documentSummaries,
                fieldCounts(savedExtractions),
                confidenceSummary(savedExtractions)
        );

        if (!llmEnabled && processedDocuments > 0 && successfulAiExtractions == 0) {
            return AgentResult.skipped(
                    SOURCE_STEP,
                    String.format(
                            "Skipped AI extraction: no API key configured. Used deterministic fallback for %d document(s).",
                            processedDocuments),
                    toJson(snapshot)
            );
        }

        if (processedDocuments > 0 && failedDocuments == 0 && fallbackExtractions == 0) {
            return AgentResult.completed(
                    SOURCE_STEP,
                    String.format("Extracted text from %d uploaded document(s).", processedDocuments),
                    toJson(snapshot)
            );
        }

        String summary;
        if (processedDocuments == 0) {
            summary = "All document extractions failed.";
        } else if (failedDocuments > 0) {
            summary = String.format("Extracted text from %d document(s), %d failed.",
                    processedDocuments, failedDocuments);
        } else if (fallbackExtractions > 0) {
            summary = String.format("Extracted text from %d document(s); %d used AI fallback.",
                    processedDocuments, fallbackExtractions);
        } else {
            summary = String.format("Extracted text from %d uploaded document(s).", processedDocuments);
        }

        return AgentResult.warning(SOURCE_STEP, summary, toJson(snapshot), Collections.emptyList());
    }

    private Map<String, Object> buildSnapshot(int totalDocuments,
                                              int processedDocuments,
                                              int failedDocuments,
                                              int successfulAiExtractions,
                                              int fallbackExtractions,
                                              List<Map<String, Object>> documentSummaries,
                                              Map<String, Integer> extractedFields,
                                              Map<String, Object> confidenceSummary) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("totalDocuments", totalDocuments);
        snapshot.put("processedDocuments", processedDocuments);
        snapshot.put("failedDocuments", failedDocuments);
        snapshot.put("successfulAiExtractions", successfulAiExtractions);
        snapshot.put("fallbackExtractions", fallbackExtractions);
        snapshot.put("extractedFields", extractedFields);
        snapshot.put("confidenceSummary", confidenceSummary);
        snapshot.put("documentSummaries", documentSummaries);
        return snapshot;
    }

    private Map<String, Integer> fieldCounts(List<DocumentExtraction> extractions) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("documentsWithLegalEntityName", countField(extractions, DocumentExtraction::getLegalEntityName));
        counts.put("documentsWithTaxId", countField(extractions, DocumentExtraction::getTaxId));
        counts.put("documentsWithBankAccountHolderName",
                countField(extractions, DocumentExtraction::getBankAccountHolderName));
        counts.put("documentsWithCountry", countField(extractions, DocumentExtraction::getCountry));
        return counts;
    }

    private int countField(List<DocumentExtraction> extractions,
                           java.util.function.Function<DocumentExtraction, String> getter) {
        int count = 0;
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() != ExtractionMethod.LLM) {
                continue;
            }
            String value = getter.apply(extraction);
            if (value != null && !value.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Object> confidenceSummary(List<DocumentExtraction> extractions) {
        List<Double> scores = new ArrayList<>();
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() == ExtractionMethod.LLM && extraction.getConfidenceScore() != null) {
                scores.add(extraction.getConfidenceScore());
            }
        }
        if (scores.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        double min = scores.get(0);
        double max = scores.get(0);
        for (Double score : scores) {
            sum += score;
            min = Math.min(min, score);
            max = Math.max(max, score);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("averageConfidence", sum / scores.size());
        summary.put("minConfidence", min);
        summary.put("maxConfidence", max);
        return summary;
    }

    private Map<String, Object> buildDocumentSummary(UploadedDocument document,
                                                    String extractionStatus,
                                                    String extractionMethod,
                                                    String extractedText,
                                                    Double confidenceScore) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("documentType", document.getDocumentType().name());
        summary.put("filename", document.getOriginalFilename());
        summary.put("extractionStatus", extractionStatus);
        summary.put("extractionMethod", extractionMethod);
        summary.put("textPreview", previewText(extractedText));
        summary.put("confidenceScore", confidenceScore);
        return summary;
    }

    private String previewText(String extractedText) {
        if (extractedText == null || extractedText.isEmpty()) {
            return "";
        }
        if (extractedText.length() <= TEXT_PREVIEW_LENGTH) {
            return extractedText;
        }
        return extractedText.substring(0, TEXT_PREVIEW_LENGTH) + "...";
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize document understanding snapshot", ex);
        }
    }
}
