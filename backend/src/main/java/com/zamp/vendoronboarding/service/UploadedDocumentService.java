package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.DocumentExtractionResponse;
import com.zamp.vendoronboarding.dto.UploadedDocumentResponse;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadedDocumentService {

    private final com.zamp.vendoronboarding.repository.UploadedDocumentRepository uploadedDocumentRepository;
    private final DocumentExtractionService documentExtractionService;

    public UploadedDocumentService(
            com.zamp.vendoronboarding.repository.UploadedDocumentRepository uploadedDocumentRepository,
            DocumentExtractionService documentExtractionService) {
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.documentExtractionService = documentExtractionService;
    }

    @Transactional
    public UploadedDocument save(VendorSubmission submission,
                                 WorkflowRun workflowRun,
                                 DocumentType documentType,
                                 StoredDocumentFile storedFile) {
        UploadedDocument document = new UploadedDocument();
        document.setVendorSubmission(submission);
        document.setWorkflowRun(workflowRun);
        document.setDocumentType(documentType);
        document.setOriginalFilename(storedFile.originalFilename());
        document.setContentType(storedFile.contentType());
        document.setFileSize(storedFile.fileSize());
        document.setStoragePath(storedFile.storagePath());
        document.setExtractedText(null);
        document.setUploadedAt(Instant.now());
        return uploadedDocumentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<UploadedDocument> findByWorkflowRunId(UUID workflowRunId) {
        return uploadedDocumentRepository.findByWorkflowRun_IdOrderByUploadedAtAsc(workflowRunId);
    }

    @Transactional
    public void updateExtractedText(UUID documentId, String extractedText) {
        UploadedDocument document = uploadedDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Uploaded document not found: " + documentId));
        document.setExtractedText(extractedText);
        uploadedDocumentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<UploadedDocumentResponse> findResponsesByWorkflowRunId(UUID workflowRunId) {
        Map<UUID, DocumentExtractionResponse> extractions =
                documentExtractionService.findResponsesByWorkflowRunId(workflowRunId);
        return uploadedDocumentRepository.findByWorkflowRun_IdOrderByUploadedAtAsc(workflowRunId).stream()
                .map(document -> toResponse(document, extractions.get(document.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<DocumentType> findDocumentTypesByWorkflowRunId(UUID workflowRunId) {
        List<UploadedDocument> documents =
                uploadedDocumentRepository.findByWorkflowRun_IdOrderByUploadedAtAsc(workflowRunId);
        Set<DocumentType> types = EnumSet.noneOf(DocumentType.class);
        for (UploadedDocument document : documents) {
            types.add(document.getDocumentType());
        }
        return types;
    }

    private UploadedDocumentResponse toResponse(UploadedDocument document,
                                                DocumentExtractionResponse extraction) {
        return new UploadedDocumentResponse(
                document.getDocumentType(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getUploadedAt(),
                document.getExtractedText(),
                extraction
        );
    }
}
