package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.CreateVendorSubmissionRequest;
import com.zamp.vendoronboarding.dto.CreateVendorSubmissionResponse;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.repository.VendorSubmissionRepository;
import com.zamp.vendoronboarding.workflow.WorkflowAsyncLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VendorSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(VendorSubmissionService.class);

    private final VendorSubmissionRepository vendorSubmissionRepository;
    private final NormalizationService normalizationService;
    private final WorkflowRunService workflowRunService;
    private final WorkflowStepLogService workflowStepLogService;
    private final WorkflowAsyncLauncher workflowAsyncLauncher;
    private final DocumentStorageService documentStorageService;
    private final UploadedDocumentService uploadedDocumentService;
    private final ObjectMapper objectMapper;

    public VendorSubmissionService(VendorSubmissionRepository vendorSubmissionRepository,
                                   NormalizationService normalizationService,
                                   WorkflowRunService workflowRunService,
                                   WorkflowStepLogService workflowStepLogService,
                                   WorkflowAsyncLauncher workflowAsyncLauncher,
                                   DocumentStorageService documentStorageService,
                                   UploadedDocumentService uploadedDocumentService,
                                   ObjectMapper objectMapper) {
        this.vendorSubmissionRepository = vendorSubmissionRepository;
        this.normalizationService = normalizationService;
        this.workflowRunService = workflowRunService;
        this.workflowStepLogService = workflowStepLogService;
        this.workflowAsyncLauncher = workflowAsyncLauncher;
        this.documentStorageService = documentStorageService;
        this.uploadedDocumentService = uploadedDocumentService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreateVendorSubmissionResponse createSubmissionAndInitializeWorkflow(
            CreateVendorSubmissionRequest request) {
        return createSubmissionAndInitializeWorkflow(request, Map.of());
    }

    @Transactional
    public CreateVendorSubmissionResponse createSubmissionAndInitializeWorkflow(
            CreateVendorSubmissionRequest request,
            Map<DocumentType, MultipartFile> documents) {
        VendorSubmission submission = mapRequestToEntity(request);
        normalizationService.applyToSubmission(submission);
        submission.setRawPayload(toRawPayload(request));
        submission = vendorSubmissionRepository.save(submission);

        WorkflowRun workflowRun = workflowRunService.createPendingRun(submission);
        workflowStepLogService.createPendingSteps(workflowRun);

        if (documents != null && !documents.isEmpty()) {
            persistDocuments(submission, workflowRun, documents);
        }

        UUID workflowRunId = workflowRun.getId();
        int documentCount = documents != null ? documents.size() : 0;
        log.info(
                "Vendor submission created | submissionId={} | workflowRunId={} | displayRunId={} | vendor={} | documentCount={}",
                submission.getId(),
                workflowRunId,
                workflowRun.getDisplayRunId(),
                submission.getLegalName(),
                documentCount
        );
        scheduleWorkflowAfterCommit(workflowRunId);

        return new CreateVendorSubmissionResponse(
                submission.getId(),
                workflowRunId,
                workflowRun.getDisplayRunId(),
                RunStatus.RUNNING,
                "Workflow started. Poll GET /api/workflow-runs/{id} for live progress."
        );
    }

    private void persistDocuments(VendorSubmission submission,
                                  WorkflowRun workflowRun,
                                  Map<DocumentType, MultipartFile> documents) {
        for (Map.Entry<DocumentType, MultipartFile> entry : documents.entrySet()) {
            MultipartFile file = entry.getValue();
            if (file == null || file.isEmpty()) {
                continue;
            }
            StoredDocumentFile storedFile = documentStorageService.storePdf(file, workflowRun.getId());
            uploadedDocumentService.save(submission, workflowRun, entry.getKey(), storedFile);
        }
    }

    public Map<DocumentType, MultipartFile> mapDocumentUploads(MultipartFile taxRegistration,
                                                               MultipartFile bankProof,
                                                               MultipartFile companyRegistration,
                                                               MultipartFile complianceDeclaration) {
        Map<DocumentType, MultipartFile> documents = new LinkedHashMap<>();
        if (taxRegistration != null && !taxRegistration.isEmpty()) {
            documents.put(DocumentType.TAX_REGISTRATION, taxRegistration);
        }
        if (bankProof != null && !bankProof.isEmpty()) {
            documents.put(DocumentType.BANK_PROOF, bankProof);
        }
        if (companyRegistration != null && !companyRegistration.isEmpty()) {
            documents.put(DocumentType.COMPANY_REGISTRATION, companyRegistration);
        }
        if (complianceDeclaration != null && !complianceDeclaration.isEmpty()) {
            documents.put(DocumentType.COMPLIANCE_DECLARATION, complianceDeclaration);
        }
        return documents;
    }

    private void scheduleWorkflowAfterCommit(UUID workflowRunId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Scheduling async workflow execution | workflowRunId={}", workflowRunId);
                workflowAsyncLauncher.runWorkflowAsync(workflowRunId);
            }
        });
    }

    private VendorSubmission mapRequestToEntity(CreateVendorSubmissionRequest request) {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName(request.legalName());
        submission.setCountry(request.country());
        submission.setWebsite(request.website());
        submission.setContactEmail(request.contactEmail());
        submission.setTaxId(request.taxId());
        submission.setBankAccountHolderName(request.bankAccountHolderName());
        submission.setBankCountry(request.bankCountry());
        submission.setBankCode(request.bankCode());
        submission.setBankAccountLast4(request.bankAccountLast4());
        submission.setBusinessCategory(request.businessCategory());
        return submission;
    }

    private String toRawPayload(CreateVendorSubmissionRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize vendor submission request", ex);
        }
    }
}
