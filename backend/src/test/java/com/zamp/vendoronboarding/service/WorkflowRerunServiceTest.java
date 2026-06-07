package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.dto.WorkflowRerunResponse;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.repository.AuditSummaryRepository;
import com.zamp.vendoronboarding.repository.CommunicationRepository;
import com.zamp.vendoronboarding.repository.DecisionRepository;
import com.zamp.vendoronboarding.repository.DocumentExtractionRepository;
import com.zamp.vendoronboarding.repository.IssueRepository;
import com.zamp.vendoronboarding.repository.ManualReviewRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import com.zamp.vendoronboarding.workflow.WorkflowAsyncLauncher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowRerunServiceTest {

    @Mock
    private WorkflowRunRepository workflowRunRepository;
    @Mock
    private DocumentExtractionRepository documentExtractionRepository;
    @Mock
    private IssueRepository issueRepository;
    @Mock
    private DecisionRepository decisionRepository;
    @Mock
    private CommunicationRepository communicationRepository;
    @Mock
    private AuditSummaryRepository auditSummaryRepository;
    @Mock
    private ManualReviewRepository manualReviewRepository;
    @Mock
    private WorkflowStepLogService workflowStepLogService;
    @Mock
    private WorkflowAsyncLauncher workflowAsyncLauncher;

    @InjectMocks
    private WorkflowRerunService workflowRerunService;

    @Test
    void rerunWorkflow_runningRun_throwsConflict() {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        run.setId(runId);
        run.setStatus(RunStatus.RUNNING);
        when(workflowRunRepository.findById(runId)).thenReturn(Optional.of(run));

        assertThrows(ResponseStatusException.class, () -> workflowRerunService.rerunWorkflow(runId.toString()));
        verify(workflowAsyncLauncher, never()).runWorkflowAsync(any());
    }

    @Test
    void rerunWorkflow_completedRun_resetsAndLaunchesAsync() {
        UUID runId = UUID.randomUUID();
        WorkflowRun run = new WorkflowRun();
        run.setId(runId);
        run.setStatus(RunStatus.COMPLETED);
        run.setFinalDecisionStatus(com.zamp.vendoronboarding.entity.enums.DecisionStatus.PENDING);
        run.setRiskScore(40);

        when(workflowRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(workflowRunRepository.save(run)).thenReturn(run);

        WorkflowRerunResponse response = workflowRerunService.rerunWorkflow(runId.toString());

        assertEquals(runId, response.runId());
        verify(documentExtractionRepository).deleteByWorkflowRun_Id(runId);
        verify(issueRepository).deleteByWorkflowRun_Id(runId);
        verify(decisionRepository).deleteByWorkflowRun_Id(runId);
        verify(communicationRepository).deleteByWorkflowRun_Id(runId);
        verify(auditSummaryRepository).deleteByWorkflowRun_Id(runId);
        verify(manualReviewRepository).deleteByWorkflowRun_Id(runId);
        verify(workflowStepLogService).resetStepsForRerun(run);
        verify(workflowAsyncLauncher).runWorkflowAsync(runId);
        assertEquals(RunStatus.PENDING, run.getStatus());
    }
}
