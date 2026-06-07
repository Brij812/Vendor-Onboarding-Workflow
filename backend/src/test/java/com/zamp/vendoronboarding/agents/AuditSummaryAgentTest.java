package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.audit.AuditSummaryDraft;
import com.zamp.vendoronboarding.audit.AuditSummaryGenerationResult;
import com.zamp.vendoronboarding.audit.AuditSummaryGenerationService;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditSummaryAgentTest {

    @Mock
    private AuditSummaryGenerationService generationService;

    private AuditSummaryAgent agent;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agent = new AuditSummaryAgent(generationService, objectMapper);
    }

    @Test
    void execute_llmGeneration_returnsCompletedWithSnapshot() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);
        context.setDecision(pendingDecision());

        AuditSummaryDraft draft = AuditSummaryDraft.fromLlm(
                "Vendor requires manual procurement review due to bank name mismatch.",
                "{\"summary\":\"Vendor requires manual procurement review due to bank name mismatch.\"}"
        );
        when(generationService.generate(any(), any(), any(), any()))
                .thenReturn(AuditSummaryGenerationResult.success(draft));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertEquals("Internal audit summary generated using AI.", result.summary());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals("LLM", snapshot.get("generationMethod").asText());
        assertTrue(snapshot.get("summaryPreview").asText().contains("manual procurement review"));
    }

    @Test
    void execute_fallbackGeneration_returnsWarning() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);
        context.setDecision(rejectedDecision());

        AuditSummaryDraft draft = AuditSummaryDraft.fromFallback(
                "Vendor Unknown Vendor is marked REJECTED with risk score 80.",
                "LLM API key is not configured."
        );
        when(generationService.generate(any(), any(), any(), any()))
                .thenReturn(AuditSummaryGenerationResult.fallback(draft, "LLM API key is not configured."));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.summary().contains("template fallback"));

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals("FALLBACK", snapshot.get("generationMethod").asText());
    }

    private DecisionEvaluation pendingDecision() {
        return new DecisionEvaluation(
                com.zamp.vendoronboarding.entity.enums.DecisionStatus.PENDING,
                65,
                "Final decision generated: PENDING with risk score 65.",
                List.of("Request corrected bank proof before approval."),
                List.of("HIGH_OR_MEDIUM_ISSUE_PRESENT"),
                1,
                0,
                0,
                1,
                0
        );
    }

    private DecisionEvaluation rejectedDecision() {
        return new DecisionEvaluation(
                com.zamp.vendoronboarding.entity.enums.DecisionStatus.REJECTED,
                80,
                "Final decision generated: REJECTED with risk score 80.",
                List.of("Do not onboard; contact vendor regarding block or duplicate status."),
                List.of("CRITICAL_ISSUE_PRESENT"),
                1,
                1,
                0,
                0,
                0
        );
    }
}
