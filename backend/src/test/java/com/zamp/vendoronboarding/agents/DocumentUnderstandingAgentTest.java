package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.document.DocumentStructuredExtractionService;
import com.zamp.vendoronboarding.document.DocumentTextExtractionResult;
import com.zamp.vendoronboarding.document.DocumentTextExtractionService;
import com.zamp.vendoronboarding.document.StructuredDocumentExtractionResult;
import com.zamp.vendoronboarding.document.StructuredDocumentFields;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.service.DocumentExtractionService;
import com.zamp.vendoronboarding.service.UploadedDocumentService;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentUnderstandingAgentTest {

    @Mock
    private UploadedDocumentService uploadedDocumentService;

    @Mock
    private DocumentTextExtractionService documentTextExtractionService;

    @Mock
    private DocumentStructuredExtractionService documentStructuredExtractionService;

    @Mock
    private DocumentExtractionService documentExtractionService;

    private DocumentUnderstandingAgent agent;

    private ObjectMapper objectMapper;
    private UUID workflowRunId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agent = createAgent("");
        workflowRunId = UUID.randomUUID();
    }

    private DocumentUnderstandingAgent createAgent(String apiKey) {
        return new DocumentUnderstandingAgent(
                uploadedDocumentService,
                documentTextExtractionService,
                documentStructuredExtractionService,
                documentExtractionService,
                objectMapper,
                apiKey
        );
    }

    @Test
    void execute_noDocuments_returnsSkipped() throws Exception {
        when(uploadedDocumentService.findByWorkflowRunId(workflowRunId)).thenReturn(List.of());

        WorkflowContext context = new WorkflowContext(workflowRunId, null);
        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.SKIPPED, result.status());
        assertEquals("Skipped: no documents uploaded.", result.summary());
        assertTrue(result.issues().isEmpty());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals(0, snapshot.get("totalDocuments").asInt());
        assertEquals(0, snapshot.get("successfulAiExtractions").asInt());
        assertEquals(0, snapshot.get("fallbackExtractions").asInt());
    }

    @Test
    void execute_aiExtractionSucceeds_returnsCompleted() throws Exception {
        agent = createAgent("test-api-key");
        UploadedDocument document = uploadedDocument("tax.pdf", "runs/tax.pdf");
        DocumentExtraction savedExtraction = new DocumentExtraction();
        savedExtraction.setExtractionMethod(ExtractionMethod.LLM);

        when(uploadedDocumentService.findByWorkflowRunId(workflowRunId)).thenReturn(List.of(document));
        when(documentTextExtractionService.extract("runs/tax.pdf"))
                .thenReturn(DocumentTextExtractionResult.success("Tax registration content"));
        when(documentStructuredExtractionService.extract("Tax registration content", DocumentType.TAX_REGISTRATION))
                .thenReturn(StructuredDocumentExtractionResult.success(
                        new StructuredDocumentFields(
                                "BrightLayer Technologies Pvt Ltd",
                                "29ABCDE1234F1Z5",
                                null,
                                "India",
                                DocumentType.TAX_REGISTRATION,
                                null,
                                0.9
                        ),
                        "{\"taxId\":\"29ABCDE1234F1Z5\"}"
                ));
        when(documentExtractionService.saveLlmExtraction(
                eq(document.getWorkflowRun()),
                eq(document),
                any(StructuredDocumentFields.class),
                eq("{\"taxId\":\"29ABCDE1234F1Z5\"}")
        )).thenReturn(savedExtraction);

        WorkflowContext context = new WorkflowContext(workflowRunId, null);
        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        verify(uploadedDocumentService).updateExtractedText(document.getId(), "Tax registration content");
        verify(documentExtractionService).saveLlmExtraction(
                eq(document.getWorkflowRun()),
                eq(document),
                any(StructuredDocumentFields.class),
                eq("{\"taxId\":\"29ABCDE1234F1Z5\"}")
        );
        assertEquals(1, context.getExtractedDocumentExtractions().size());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals(1, snapshot.get("successfulAiExtractions").asInt());
        assertEquals(0, snapshot.get("fallbackExtractions").asInt());
    }

    @Test
    void execute_aiExtractionFails_returnsSkippedWhenNoApiKey() throws Exception {
        UploadedDocument document = uploadedDocument("tax.pdf", "runs/tax.pdf");
        DocumentExtraction savedExtraction = new DocumentExtraction();
        savedExtraction.setExtractionMethod(ExtractionMethod.FALLBACK);

        when(uploadedDocumentService.findByWorkflowRunId(workflowRunId)).thenReturn(List.of(document));
        when(documentTextExtractionService.extract("runs/tax.pdf"))
                .thenReturn(DocumentTextExtractionResult.success("Tax registration content"));
        when(documentStructuredExtractionService.extract("Tax registration content", DocumentType.TAX_REGISTRATION))
                .thenReturn(StructuredDocumentExtractionResult.failure(null, "LLM API key is not configured."));
        when(documentExtractionService.saveFallbackExtraction(
                document.getWorkflowRun(),
                document,
                "LLM API key is not configured."
        )).thenReturn(savedExtraction);

        WorkflowContext context = new WorkflowContext(workflowRunId, null);
        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.SKIPPED, result.status());
        assertTrue(result.summary().contains("no API key"));
        verify(documentExtractionService, never()).saveLlmExtraction(any(), any(), any(), any());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals(0, snapshot.get("successfulAiExtractions").asInt());
        assertEquals(1, snapshot.get("fallbackExtractions").asInt());
    }

    @Test
    void execute_partialTextFailure_returnsSkippedWhenNoApiKey() throws Exception {
        UploadedDocument successDocument = uploadedDocument("tax.pdf", "runs/tax.pdf");
        UploadedDocument failedDocument = uploadedDocument("bank.pdf", "runs/bank.pdf");
        DocumentExtraction savedExtraction = new DocumentExtraction();

        when(uploadedDocumentService.findByWorkflowRunId(workflowRunId))
                .thenReturn(List.of(successDocument, failedDocument));
        when(documentTextExtractionService.extract("runs/tax.pdf"))
                .thenReturn(DocumentTextExtractionResult.success("Tax content"));
        when(documentTextExtractionService.extract("runs/bank.pdf"))
                .thenReturn(DocumentTextExtractionResult.failure("Failed to extract text from PDF: corrupt file"));
        when(documentStructuredExtractionService.extract("Tax content", DocumentType.TAX_REGISTRATION))
                .thenReturn(StructuredDocumentExtractionResult.failure(null, "LLM API key is not configured."));
        when(documentExtractionService.saveFallbackExtraction(any(), eq(successDocument), any()))
                .thenReturn(savedExtraction);

        WorkflowContext context = new WorkflowContext(workflowRunId, null);
        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.SKIPPED, result.status());
        verify(uploadedDocumentService).updateExtractedText(successDocument.getId(), "Tax content");
        verify(uploadedDocumentService, never()).updateExtractedText(eq(failedDocument.getId()), any());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals(2, snapshot.get("totalDocuments").asInt());
        assertEquals(1, snapshot.get("processedDocuments").asInt());
        assertEquals(1, snapshot.get("failedDocuments").asInt());
    }

    private UploadedDocument uploadedDocument(String filename, String storagePath) {
        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setId(workflowRunId);

        UploadedDocument document = new UploadedDocument();
        document.setId(UUID.randomUUID());
        document.setWorkflowRun(workflowRun);
        document.setDocumentType(DocumentType.TAX_REGISTRATION);
        document.setOriginalFilename(filename);
        document.setStoragePath(storagePath);
        return document;
    }
}
