package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.communication.CommunicationDraft;
import com.zamp.vendoronboarding.communication.CommunicationGenerationResult;
import com.zamp.vendoronboarding.communication.CommunicationGenerationService;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CommunicationAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "COMMUNICATION_AGENT";

    private final CommunicationGenerationService generationService;
    private final ObjectMapper objectMapper;

    public CommunicationAgent(CommunicationGenerationService generationService, ObjectMapper objectMapper) {
        this.generationService = generationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.COMMUNICATION_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        DecisionEvaluation decision = requireDecision(context);
        CommunicationGenerationResult result = generationService.generate(
                context.getVendorSubmission(),
                decision,
                context.getIssues()
        );
        CommunicationDraft draft = result.draft();
        context.setCommunication(draft);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("subject", draft.subject());
        snapshot.put("communicationType", draft.communicationType().name());
        snapshot.put("generationMethod", draft.generationMethod().name());

        if (result.success()) {
            return AgentResult.completed(
                    SOURCE_STEP,
                    "Vendor communication generated using AI.",
                    toJson(snapshot)
            );
        }

        return AgentResult.warning(
                SOURCE_STEP,
                "Vendor communication generated using template fallback.",
                toJson(snapshot),
                Collections.emptyList()
        );
    }

    private DecisionEvaluation requireDecision(WorkflowContext context) {
        Object decision = context.getDecision();
        if (!(decision instanceof DecisionEvaluation evaluation)) {
            throw new IllegalStateException("Decision must be available before generating vendor communication.");
        }
        return evaluation;
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize communication snapshot", ex);
        }
    }
}
