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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoResetServiceTest {

    @Mock
    private DocumentExtractionRepository documentExtractionRepository;
    @Mock
    private UploadedDocumentRepository uploadedDocumentRepository;
    @Mock
    private IssueRepository issueRepository;
    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private CommunicationRepository communicationRepository;
    @Mock
    private AuditSummaryRepository auditSummaryRepository;
    @Mock
    private WorkflowStepLogRepository workflowStepLogRepository;
    @Mock
    private WorkflowRunRepository workflowRunRepository;
    @Mock
    private VendorSubmissionRepository vendorSubmissionRepository;
    @Mock
    private DocumentStorageService documentStorageService;

    @InjectMocks
    private DemoResetService demoResetService;

    @BeforeEach
    void setUp() {
        UploadedDocument document = new UploadedDocument();
        document.setStoragePath("run-id/sample.pdf");
        when(workflowRunRepository.count()).thenReturn(2L);
        when(vendorSubmissionRepository.count()).thenReturn(2L);
        when(uploadedDocumentRepository.findAll()).thenReturn(List.of(document));
        when(documentStorageService.deleteStoredFiles(List.of("run-id/sample.pdf"))).thenReturn(1L);
    }

    @Test
    void resetAllRuns_deletesInFkSafeOrderAndReturnsCounts() {
        DemoResetResponse response = demoResetService.resetAllRuns();

        verify(documentExtractionRepository).deleteAllInBatch();
        verify(uploadedDocumentRepository).deleteAllInBatch();
        verify(issueRepository).deleteAllInBatch();
        verify(decisionRepository).deleteAllInBatch();
        verify(communicationRepository).deleteAllInBatch();
        verify(auditSummaryRepository).deleteAllInBatch();
        verify(workflowStepLogRepository).deleteAllInBatch();
        verify(workflowRunRepository).deleteAllInBatch();
        verify(vendorSubmissionRepository).deleteAllInBatch();

        assertEquals(2L, response.deletedRuns());
        assertEquals(2L, response.deletedSubmissions());
        assertEquals(1L, response.deletedFiles());
    }
}
