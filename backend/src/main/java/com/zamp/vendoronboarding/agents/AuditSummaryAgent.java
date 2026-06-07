package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.audit.AuditSummaryDraft;
import com.zamp.vendoronboarding.audit.AuditSummaryGenerationResult;
import com.zamp.vendoronboarding.audit.AuditSummaryGenerationService;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuditSummaryAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "AUDIT_SUMMARY_AGENT";
    private static final int SUMMARY_PREVIEW_MAX_LENGTH = 120;

    private final AuditSummaryGenerationService generationService;
    private final ObjectMapper objectMapper;

    public AuditSummaryAgent(AuditSummaryGenerationService generationService, ObjectMapper objectMapper) {
        this.generationService = generationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.AUDIT_SUMMARY_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        DecisionEvaluation decision = requireDecision(context);
        AuditSummaryGenerationResult result = generationService.generate(
                context.getVendorSubmission(),
                decision,
                context.getIssues(),
                context.getExtractedDocumentExtractions()
        );
        AuditSummaryDraft draft = result.draft();
        context.setAuditSummary(draft);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("generationMethod", draft.generationMethod().name());
        snapshot.put("summaryPreview", buildSummaryPreview(draft.summary()));

        if (result.success()) {
            return AgentResult.completed(
                    SOURCE_STEP,
                    "Internal audit summary generated using AI.",
                    toJson(snapshot)
            );
        }

        return AgentResult.warning(
                SOURCE_STEP,
                "Internal audit summary generated using template fallback.",
                toJson(snapshot),
                Collections.emptyList()
        );
    }

    private DecisionEvaluation requireDecision(WorkflowContext context) {
        Object decision = context.getDecision();
        if (!(decision instanceof DecisionEvaluation evaluation)) {
            throw new IllegalStateException("Decision must be available before generating audit summary.");
        }
        return evaluation;
    }

    private String buildSummaryPreview(String summary) {
        if (summary == null || summary.isBlank()) {
            return "";
        }
        String trimmed = summary.trim();
        if (trimmed.length() <= SUMMARY_PREVIEW_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, SUMMARY_PREVIEW_MAX_LENGTH) + "...";
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize audit summary snapshot", ex);
        }
    }
}
