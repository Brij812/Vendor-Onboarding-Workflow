package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.document.StructuredDocumentFields;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.repository.DocumentExtractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentExtractionServiceTest {

    @Mock
    private DocumentExtractionRepository documentExtractionRepository;

    @InjectMocks
    private DocumentExtractionService documentExtractionService;

    @Test
    void saveLlmExtraction_updatesExistingRuleBasedStub() {
        UUID documentId = UUID.randomUUID();
        WorkflowRun workflowRun = workflowRun();
        UploadedDocument uploadedDocument = uploadedDocument(documentId, workflowRun);

        DocumentExtraction existing = new DocumentExtraction();
        existing.setId(UUID.randomUUID());
        existing.setExtractionMethod(ExtractionMethod.RULE_BASED);
        existing.setDocumentType(DocumentType.TAX_REGISTRATION);

        when(documentExtractionRepository.findByUploadedDocument_Id(documentId)).thenReturn(Optional.of(existing));
        when(documentExtractionRepository.save(any(DocumentExtraction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StructuredDocumentFields fields = new StructuredDocumentFields(
                "BrightLayer Technologies Pvt Ltd",
                "29ABCDE1234F1Z5",
                null,
                "India",
                DocumentType.TAX_REGISTRATION,
                LocalDate.of(2024, 1, 15),
                0.88
        );

        DocumentExtraction saved = documentExtractionService.saveLlmExtraction(
                workflowRun,
                uploadedDocument,
                fields,
                "{\"taxId\":\"29ABCDE1234F1Z5\"}"
        );

        assertEquals(ExtractionMethod.LLM, saved.getExtractionMethod());
        assertEquals("29ABCDE1234F1Z5", saved.getTaxId());
        assertEquals("BrightLayer Technologies Pvt Ltd", saved.getLegalEntityName());
        assertEquals(0.88, saved.getConfidenceScore());
        assertEquals("{\"taxId\":\"29ABCDE1234F1Z5\"}", saved.getRawLlmResponse());
    }

    @Test
    void saveFallbackExtraction_clearsStructuredFields() {
        UUID documentId = UUID.randomUUID();
        WorkflowRun workflowRun = workflowRun();
        UploadedDocument uploadedDocument = uploadedDocument(documentId, workflowRun);

        when(documentExtractionRepository.findByUploadedDocument_Id(documentId)).thenReturn(Optional.empty());
        when(documentExtractionRepository.save(any(DocumentExtraction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentExtraction saved = documentExtractionService.saveFallbackExtraction(
                workflowRun,
                uploadedDocument,
                "LLM API key is not configured."
        );

        assertEquals(ExtractionMethod.FALLBACK, saved.getExtractionMethod());
        assertNull(saved.getTaxId());
        assertNull(saved.getLegalEntityName());
        assertEquals("LLM API key is not configured.", saved.getRawLlmResponse());

        ArgumentCaptor<DocumentExtraction> captor = ArgumentCaptor.forClass(DocumentExtraction.class);
        verify(documentExtractionRepository).save(captor.capture());
        assertEquals(workflowRun, captor.getValue().getWorkflowRun());
        assertEquals(uploadedDocument, captor.getValue().getUploadedDocument());
    }

    private WorkflowRun workflowRun() {
        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setId(UUID.randomUUID());
        return workflowRun;
    }

    private UploadedDocument uploadedDocument(UUID documentId, WorkflowRun workflowRun) {
        UploadedDocument document = new UploadedDocument();
        document.setId(documentId);
        document.setWorkflowRun(workflowRun);
        document.setDocumentType(DocumentType.TAX_REGISTRATION);
        return document;
    }
}
