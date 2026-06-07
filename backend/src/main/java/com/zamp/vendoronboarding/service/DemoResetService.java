package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.DemoResetResponse;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.repository.AuditSummaryRepository;
import com.zamp.vendoronboarding.repository.CommunicationRepository;
import com.zamp.vendoronboarding.repository.DecisionRepository;
import com.zamp.vendoronboarding.repository.DocumentExtractionRepository;
import com.zamp.vendoronboarding.repository.IssueRepository;
import com.zamp.vendoronboarding.repository.UploadedDocumentRepository;
import com.zamp.vendoronboarding.repository.VendorSubmissionRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import com.zamp.vendoronboarding.repository.WorkflowStepLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DemoResetService {

    private final DocumentExtractionRepository documentExtractionRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final IssueRepository issueRepository;
    private final DecisionRepository decisionRepository;
    private final CommunicationRepository communicationRepository;
    private final AuditSummaryRepository auditSummaryRepository;
    private final WorkflowStepLogRepository workflowStepLogRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final VendorSubmissionRepository vendorSubmissionRepository;
    private final DocumentStorageService documentStorageService;

    public DemoResetService(DocumentExtractionRepository documentExtractionRepository,
                            UploadedDocumentRepository uploadedDocumentRepository,
                            IssueRepository issueRepository,
                            DecisionRepository decisionRepository,
                            CommunicationRepository communicationRepository,
                            AuditSummaryRepository auditSummaryRepository,
                            WorkflowStepLogRepository workflowStepLogRepository,
                            WorkflowRunRepository workflowRunRepository,
                            VendorSubmissionRepository vendorSubmissionRepository,
                            DocumentStorageService documentStorageService) {
        this.documentExtractionRepository = documentExtractionRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.issueRepository = issueRepository;
        this.decisionRepository = decisionRepository;
        this.communicationRepository = communicationRepository;
        this.auditSummaryRepository = auditSummaryRepository;
        this.workflowStepLogRepository = workflowStepLogRepository;
        this.workflowRunRepository = workflowRunRepository;
        this.vendorSubmissionRepository = vendorSubmissionRepository;
        this.documentStorageService = documentStorageService;
    }

    @Transactional
    public DemoResetResponse resetAllRuns() {
        long deletedRuns = workflowRunRepository.count();
        long deletedSubmissions = vendorSubmissionRepository.count();

        List<String> storagePaths = new ArrayList<>();
        for (UploadedDocument document : uploadedDocumentRepository.findAll()) {
            if (document.getStoragePath() != null && !document.getStoragePath().isBlank()) {
                storagePaths.add(document.getStoragePath());
            }
        }

        documentExtractionRepository.deleteAllInBatch();
        uploadedDocumentRepository.deleteAllInBatch();
        issueRepository.deleteAllInBatch();
        decisionRepository.deleteAllInBatch();
        communicationRepository.deleteAllInBatch();
        auditSummaryRepository.deleteAllInBatch();
        workflowStepLogRepository.deleteAllInBatch();
        workflowRunRepository.deleteAllInBatch();
        vendorSubmissionRepository.deleteAllInBatch();

        long deletedFiles = documentStorageService.deleteStoredFiles(storagePaths);

        return new DemoResetResponse(deletedRuns, deletedSubmissions, deletedFiles);
    }
}
