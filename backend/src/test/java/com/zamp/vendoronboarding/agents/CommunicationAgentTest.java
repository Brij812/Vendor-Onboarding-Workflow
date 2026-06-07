package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.communication.CommunicationDraft;
import com.zamp.vendoronboarding.communication.CommunicationGenerationResult;
import com.zamp.vendoronboarding.communication.CommunicationGenerationService;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.enums.CommunicationType;
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
class CommunicationAgentTest {

    @Mock
    private CommunicationGenerationService generationService;

    private CommunicationAgent agent;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agent = new CommunicationAgent(generationService, objectMapper);
    }

    @Test
    void execute_llmGeneration_returnsCompletedWithSnapshot() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);
        context.setDecision(pendingDecision());

        CommunicationDraft draft = CommunicationDraft.fromLlm(
                CommunicationType.VENDOR_FOLLOW_UP,
                "Action required for vendor onboarding submission",
                "Dear vendor, please review the requested changes.",
                "{\"subject\":\"Action required for vendor onboarding submission\"}"
        );
        when(generationService.generate(any(), any(), any()))
                .thenReturn(CommunicationGenerationResult.success(draft));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertEquals("Vendor communication generated using AI.", result.summary());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals("LLM", snapshot.get("generationMethod").asText());
        assertEquals("Action required for vendor onboarding submission", snapshot.get("subject").asText());
    }

    @Test
    void execute_fallbackGeneration_returnsWarning() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);
        context.setDecision(pendingDecision());

        CommunicationDraft draft = CommunicationDraft.fromFallback(
                CommunicationType.VENDOR_FOLLOW_UP,
                "Action required for vendor onboarding submission",
                "Template body",
                "LLM API key is not configured."
        );
        when(generationService.generate(any(), any(), any()))
                .thenReturn(CommunicationGenerationResult.fallback(draft, "LLM API key is not configured."));

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
                List.of("Confirm bank account ownership."),
                List.of("HIGH_OR_MEDIUM_ISSUE_PRESENT"),
                1,
                0,
                0,
                1,
                0
        );
    }
}
