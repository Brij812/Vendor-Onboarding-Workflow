package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.decision.DecisionEngine;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DecisionAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "DECISION_AGENT";

    private final DecisionEngine decisionEngine;
    private final ObjectMapper objectMapper;

    public DecisionAgent(DecisionEngine decisionEngine, ObjectMapper objectMapper) {
        this.decisionEngine = decisionEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.DECISION_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        List<IssueDraft> issues = context.getIssues();
        DecisionEvaluation evaluation = decisionEngine.evaluate(issues);
        context.setDecision(evaluation);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("decisionStatus", evaluation.status().name());
        snapshot.put("riskScore", evaluation.riskScore());
        snapshot.put("issueCount", evaluation.issueCount());
        snapshot.put("criticalIssueCount", evaluation.criticalIssueCount());
        snapshot.put("highIssueCount", evaluation.highIssueCount());
        snapshot.put("mediumIssueCount", evaluation.mediumIssueCount());
        snapshot.put("lowIssueCount", evaluation.lowIssueCount());
        snapshot.put("triggeredRules", evaluation.triggeredRules());
        snapshot.put("requiredActions", evaluation.requiredActions());

        return AgentResult.completed(
                SOURCE_STEP,
                evaluation.reasonSummary(),
                toJson(snapshot)
        );
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize decision snapshot", ex);
        }
    }
}
