package com.zamp.vendoronboarding.workflow;

import com.zamp.vendoronboarding.agents.AuditSummaryAgent;
import com.zamp.vendoronboarding.agents.CommunicationAgent;
import com.zamp.vendoronboarding.agents.CompletenessAgent;
import com.zamp.vendoronboarding.agents.ConsistencyCheckAgent;
import com.zamp.vendoronboarding.agents.DecisionAgent;
import com.zamp.vendoronboarding.agents.DocumentUnderstandingAgent;
import com.zamp.vendoronboarding.agents.DuplicateRiskAgent;
import com.zamp.vendoronboarding.agents.FormatValidationAgent;
import com.zamp.vendoronboarding.agents.IntakeAgent;
import com.zamp.vendoronboarding.audit.AuditSummaryDraft;
import com.zamp.vendoronboarding.communication.CommunicationDraft;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.entity.WorkflowStepLog;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.exception.WorkflowRunNotFoundException;
import com.zamp.vendoronboarding.repository.VendorSubmissionRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import com.zamp.vendoronboarding.service.AuditSummaryService;
import com.zamp.vendoronboarding.service.CommunicationService;
import com.zamp.vendoronboarding.service.DecisionService;
import com.zamp.vendoronboarding.service.IssueService;
import com.zamp.vendoronboarding.service.UploadedDocumentService;
import com.zamp.vendoronboarding.service.WorkflowRunService;
import com.zamp.vendoronboarding.service.WorkflowStepExecutionService;
import com.zamp.vendoronboarding.service.WorkflowStepLogService;
import com.zamp.vendoronboarding.logging.WorkflowMdc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrator.class);

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowRunService workflowRunService;
    private final WorkflowStepLogService workflowStepLogService;
    private final WorkflowStepExecutionService workflowStepExecutionService;
    private final IssueService issueService;
    private final DecisionService decisionService;
    private final CommunicationService communicationService;
    private final AuditSummaryService auditSummaryService;
    private final UploadedDocumentService uploadedDocumentService;
    private final VendorSubmissionRepository vendorSubmissionRepository;
    private final Map<WorkflowStepDefinition, WorkflowAgent> agentsByStep;
    private final long stepDelayMs;

    public WorkflowOrchestrator(WorkflowRunRepository workflowRunRepository,
                                WorkflowRunService workflowRunService,
                                WorkflowStepLogService workflowStepLogService,
                                WorkflowStepExecutionService workflowStepExecutionService,
                                IssueService issueService,
                                DecisionService decisionService,
                                CommunicationService communicationService,
                                AuditSummaryService auditSummaryService,
                                UploadedDocumentService uploadedDocumentService,
                                VendorSubmissionRepository vendorSubmissionRepository,
                                IntakeAgent intakeAgent,
                                DocumentUnderstandingAgent documentUnderstandingAgent,
                                CompletenessAgent completenessAgent,
                                FormatValidationAgent formatValidationAgent,
                                ConsistencyCheckAgent consistencyCheckAgent,
                                DuplicateRiskAgent duplicateRiskAgent,
                                DecisionAgent decisionAgent,
                                CommunicationAgent communicationAgent,
                                AuditSummaryAgent auditSummaryAgent,
                                @Value("${app.workflow.step-delay-ms:800}") long stepDelayMs) {
        this.workflowRunRepository = workflowRunRepository;
        this.workflowRunService = workflowRunService;
        this.workflowStepLogService = workflowStepLogService;
        this.workflowStepExecutionService = workflowStepExecutionService;
        this.issueService = issueService;
        this.decisionService = decisionService;
        this.communicationService = communicationService;
        this.auditSummaryService = auditSummaryService;
        this.uploadedDocumentService = uploadedDocumentService;
        this.vendorSubmissionRepository = vendorSubmissionRepository;
        this.stepDelayMs = stepDelayMs;

        Map<WorkflowStepDefinition, WorkflowAgent> agents = new EnumMap<>(WorkflowStepDefinition.class);
        agents.put(WorkflowStepDefinition.INTAKE_AGENT, intakeAgent);
        agents.put(WorkflowStepDefinition.DOCUMENT_UNDERSTANDING_AGENT, documentUnderstandingAgent);
        agents.put(WorkflowStepDefinition.COMPLETENESS_AGENT, completenessAgent);
        agents.put(WorkflowStepDefinition.FORMAT_VALIDATION_AGENT, formatValidationAgent);
        agents.put(WorkflowStepDefinition.CONSISTENCY_CHECK_AGENT, consistencyCheckAgent);
        agents.put(WorkflowStepDefinition.DUPLICATE_RISK_AGENT, duplicateRiskAgent);
        agents.put(WorkflowStepDefinition.DECISION_AGENT, decisionAgent);
        agents.put(WorkflowStepDefinition.COMMUNICATION_AGENT, communicationAgent);
        agents.put(WorkflowStepDefinition.AUDIT_SUMMARY_AGENT, auditSummaryAgent);
        this.agentsByStep = Map.copyOf(agents);
    }

    public void executeWorkflow(UUID workflowRunId) {
        WorkflowRun workflowRun = workflowRunRepository.findWithVendorSubmissionById(workflowRunId)
                .orElseThrow(() -> new WorkflowRunNotFoundException(workflowRunId));

        String vendorName = resolveVendorName(workflowRun);
        List<WorkflowStepLog> stepLogs = workflowStepLogService.findByWorkflowRunId(workflowRunId);

        WorkflowMdc.setWorkflow(workflowRunId, vendorName);
        try {
            log.info(
                    "Workflow run started | totalSteps={} | displayRunId={}",
                    stepLogs.size(),
                    workflowRun.getDisplayRunId()
            );

            workflowRunService.markRunning(workflowRunId);
            WorkflowContext context = buildContext(workflowRun);

            String previousStep = null;
            for (WorkflowStepLog stepLog : stepLogs) {
                WorkflowStepDefinition definition = WorkflowStepSimulator.fromStepName(stepLog.getStepName());
                if (previousStep != null) {
                    log.info(
                            "Transitioning to next agent | from={} | to={} | step={}/{}",
                            previousStep,
                            definition.getStepName(),
                            definition.getStepOrder(),
                            stepLogs.size()
                    );
                }
                executeStep(workflowRun, context, stepLog);
                previousStep = definition.getStepName();
            }

            workflowRunService.markCompleted(workflowRunId);
            log.info("Workflow run completed successfully");
        } catch (Exception ex) {
            log.error("Workflow run failed | error={}", ex.getMessage(), ex);
            workflowRunService.markFailed(
                    workflowRunId,
                    ex.getMessage() != null ? ex.getMessage() : "Workflow failed unexpectedly"
            );
        } finally {
            WorkflowMdc.clear();
        }
    }

    private String resolveVendorName(WorkflowRun workflowRun) {
        VendorSubmission submission = workflowRun.getVendorSubmission();
        if (submission == null || submission.getLegalName() == null || submission.getLegalName().isBlank()) {
            return "Unknown Vendor";
        }
        return submission.getLegalName().trim();
    }

    private WorkflowContext buildContext(WorkflowRun workflowRun) {
        VendorSubmission submission = workflowRun.getVendorSubmission();
        WorkflowContext context = new WorkflowContext(workflowRun.getId(), submission);
        context.setUploadedDocumentTypes(
                uploadedDocumentService.findDocumentTypesByWorkflowRunId(workflowRun.getId()));
        return context;
    }

    private void executeStep(WorkflowRun workflowRun, WorkflowContext context, WorkflowStepLog stepLog) {
        WorkflowStepDefinition definition = WorkflowStepSimulator.fromStepName(stepLog.getStepName());
        WorkflowAgent agent = agentsByStep.get(definition);

        try {
            if (agent != null) {
                executeRealStep(workflowRun, context, stepLog, agent);
            } else {
                executeSimulatedStep(stepLog, definition);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            workflowStepExecutionService.failStep(stepLog.getId(), "Workflow interrupted");
            workflowRunService.markFailed(workflowRun.getId(), "Workflow interrupted");
            throw new IllegalStateException("Workflow interrupted", ex);
        } catch (Exception ex) {
            workflowStepExecutionService.failStep(stepLog.getId(), ex.getMessage());
            workflowRunService.markFailed(workflowRun.getId(), ex.getMessage());
            throw ex;
        }
    }

    private void executeRealStep(WorkflowRun workflowRun, WorkflowContext context,
                                   WorkflowStepLog stepLog, WorkflowAgent agent) {
        WorkflowStepDefinition step = agent.getStep();
        WorkflowMdc.setStep(step.getStepName(), step.getStepOrder());

        log.info(
                "Agent execution started | agent={} | stepOrder={} | description={}",
                step.getStepName(),
                step.getStepOrder(),
                step.getSummary()
        );

        long startedAt = System.nanoTime();
        workflowStepExecutionService.startStep(stepLog.getId(), "Running " + step.getStepName() + "...");

        AgentResult result = agent.execute(context);
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;

        if (result.status() == StepStatus.FAILED) {
            log.error(
                    "Agent execution failed | agent={} | durationMs={} | summary={}",
                    step.getStepName(),
                    durationMs,
                    result.summary()
            );
            workflowStepExecutionService.failStep(stepLog.getId(), result.summary());
            workflowRunService.markFailed(workflowRun.getId(), result.summary());
            throw new IllegalStateException(result.summary());
        }

        issueService.persistIssues(workflowRun, result.issues());
        context.putStepOutput(result.stepName(), result.outputSnapshot());

        if (agent.getStep() == WorkflowStepDefinition.INTAKE_AGENT) {
            vendorSubmissionRepository.save(context.getVendorSubmission());
        }

        if (agent.getStep() == WorkflowStepDefinition.DECISION_AGENT) {
            decisionService.persistDecision(workflowRun, (DecisionEvaluation) context.getDecision());
        }

        if (agent.getStep() == WorkflowStepDefinition.COMMUNICATION_AGENT) {
            communicationService.persistCommunication(workflowRun,
                    (CommunicationDraft) context.getCommunication());
        }

        if (agent.getStep() == WorkflowStepDefinition.AUDIT_SUMMARY_AGENT) {
            auditSummaryService.persistAuditSummary(workflowRun,
                    (AuditSummaryDraft) context.getAuditSummary());
        }

        workflowStepExecutionService.completeStep(
                stepLog.getId(),
                result.status(),
                result.outputSnapshot(),
                result.summary()
        );

        log.info(
                "Agent execution finished | agent={} | status={} | durationMs={} | issueCount={} | summary={}",
                step.getStepName(),
                result.status(),
                durationMs,
                result.issues() != null ? result.issues().size() : 0,
                result.summary()
        );
    }

    private void executeSimulatedStep(WorkflowStepLog stepLog, WorkflowStepDefinition definition)
            throws InterruptedException {
        WorkflowMdc.setStep(definition.getStepName(), definition.getStepOrder());
        log.info(
                "Simulated step started | step={} | stepOrder={}",
                definition.getStepName(),
                definition.getStepOrder()
        );

        long startedAt = System.nanoTime();
        workflowStepExecutionService.startStep(stepLog.getId(), WorkflowStepSimulator.runningSummary(definition));
        sleep(stepDelayMs);
        workflowStepExecutionService.completeStep(
                stepLog.getId(),
                WorkflowStepSimulator.outputSnapshot(definition),
                definition.getSummary()
        );

        log.info(
                "Simulated step finished | step={} | durationMs={}",
                definition.getStepName(),
                (System.nanoTime() - startedAt) / 1_000_000L
        );
    }

    private void sleep(long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
    }
}
