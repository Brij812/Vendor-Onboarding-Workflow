package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.decision.DecisionEngine;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionAgentTest {

    private DecisionAgent agent;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        agent = new DecisionAgent(new DecisionEngine(), objectMapper);
    }

    @Test
    void execute_pendingIssues_returnsCompletedWithSnapshot() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);
        context.addIssues(List.of(
                IssueDraft.of("CONSISTENCY_CHECK_AGENT", "BANK_NAME_MISMATCH", IssueSeverity.MEDIUM,
                        "Bank name mismatch", "Confirm bank ownership", "bankAccountHolderName")
        ));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertEquals("DECISION_AGENT", result.stepName());
        assertTrue(result.summary().startsWith("Final decision generated: PENDING"));
        assertTrue(result.issues().isEmpty());

        DecisionEvaluation evaluation = (DecisionEvaluation) context.getDecision();
        assertNotNull(evaluation);
        assertEquals(DecisionStatus.PENDING, evaluation.status());

        JsonNode snapshot = objectMapper.readTree(result.outputSnapshot());
        assertEquals("PENDING", snapshot.get("decisionStatus").asText());
        assertEquals(25, snapshot.get("riskScore").asInt());
        assertEquals(1, snapshot.get("issueCount").asInt());
        assertEquals(0, snapshot.get("criticalIssueCount").asInt());
        assertEquals(0, snapshot.get("highIssueCount").asInt());
        assertEquals(1, snapshot.get("mediumIssueCount").asInt());
        assertTrue(snapshot.get("triggeredRules").isArray());
        assertTrue(snapshot.get("requiredActions").isArray());
    }

    @Test
    void execute_noIssues_returnsApprovedDecision() {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), null);

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertTrue(result.summary().contains("APPROVED"));
        DecisionEvaluation evaluation = (DecisionEvaluation) context.getDecision();
        assertEquals(DecisionStatus.APPROVED, evaluation.status());
        assertEquals(0, evaluation.riskScore());
    }
}
