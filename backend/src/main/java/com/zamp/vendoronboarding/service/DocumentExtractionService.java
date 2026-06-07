package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.document.StructuredDocumentFields;
import com.zamp.vendoronboarding.dto.DocumentExtractionResponse;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.repository.DocumentExtractionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DocumentExtractionService {

    private final DocumentExtractionRepository documentExtractionRepository;

    public DocumentExtractionService(DocumentExtractionRepository documentExtractionRepository) {
        this.documentExtractionRepository = documentExtractionRepository;
    }

    @Transactional
    public DocumentExtraction saveLlmExtraction(WorkflowRun workflowRun,
                                               UploadedDocument uploadedDocument,
                                               StructuredDocumentFields fields,
                                               String rawLlmResponse) {
        DocumentExtraction extraction = findOrCreate(workflowRun, uploadedDocument);
        applyFields(extraction, fields, uploadedDocument.getDocumentType());
        extraction.setExtractionMethod(ExtractionMethod.LLM);
        extraction.setRawLlmResponse(rawLlmResponse);
        return documentExtractionRepository.save(extraction);
    }

    @Transactional
    public DocumentExtraction saveFallbackExtraction(WorkflowRun workflowRun,
                                                     UploadedDocument uploadedDocument,
                                                     String rawLlmResponse) {
        DocumentExtraction extraction = findOrCreate(workflowRun, uploadedDocument);
        extraction.setDocumentType(uploadedDocument.getDocumentType());
        extraction.setLegalEntityName(null);
        extraction.setTaxId(null);
        extraction.setBankAccountHolderName(null);
        extraction.setCountry(null);
        extraction.setDocumentDate(null);
        extraction.setConfidenceScore(null);
        extraction.setExtractionMethod(ExtractionMethod.FALLBACK);
        extraction.setRawLlmResponse(rawLlmResponse);
        return documentExtractionRepository.save(extraction);
    }

    @Transactional(readOnly = true)
    public List<DocumentExtraction> findByWorkflowRunId(UUID workflowRunId) {
        return documentExtractionRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(workflowRunId);
    }

    @Transactional(readOnly = true)
    public Map<UUID, DocumentExtractionResponse> findResponsesByWorkflowRunId(UUID workflowRunId) {
        List<DocumentExtraction> extractions = findByWorkflowRunId(workflowRunId);
        Map<UUID, DocumentExtractionResponse> responses = new HashMap<>();
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getUploadedDocument() != null && extraction.getUploadedDocument().getId() != null) {
                responses.put(extraction.getUploadedDocument().getId(), toResponse(extraction));
            }
        }
        return responses;
    }

    public DocumentExtractionResponse toResponse(DocumentExtraction extraction) {
        if (extraction == null) {
            return null;
        }
        return new DocumentExtractionResponse(
                extraction.getExtractionMethod(),
                extraction.getDocumentType(),
                extraction.getLegalEntityName(),
                extraction.getTaxId(),
                extraction.getBankAccountHolderName(),
                extraction.getCountry(),
                extraction.getDocumentDate(),
                extraction.getConfidenceScore()
        );
    }

    private DocumentExtraction findOrCreate(WorkflowRun workflowRun, UploadedDocument uploadedDocument) {
        return documentExtractionRepository.findByUploadedDocument_Id(uploadedDocument.getId())
                .orElseGet(() -> {
                    DocumentExtraction extraction = new DocumentExtraction();
                    extraction.setWorkflowRun(workflowRun);
                    extraction.setUploadedDocument(uploadedDocument);
                    return extraction;
                });
    }

    private void applyFields(DocumentExtraction extraction,
                             StructuredDocumentFields fields,
                             DocumentType uploadedDocumentType) {
        DocumentType resolvedType = fields.documentType() != null && fields.documentType() != DocumentType.UNKNOWN
                ? fields.documentType()
                : uploadedDocumentType;
        extraction.setDocumentType(resolvedType);
        extraction.setLegalEntityName(fields.legalEntityName());
        extraction.setTaxId(fields.taxId());
        extraction.setBankAccountHolderName(fields.bankAccountHolderName());
        extraction.setCountry(fields.country());
        extraction.setDocumentDate(fields.documentDate());
        extraction.setConfidenceScore(fields.confidenceScore());
    }
}
