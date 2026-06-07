package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.service.NormalizationService;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class IntakeAgent implements WorkflowAgent {

    private final NormalizationService normalizationService;
    private final ObjectMapper objectMapper;

    public IntakeAgent(NormalizationService normalizationService, ObjectMapper objectMapper) {
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.INTAKE_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        VendorSubmission submission = context.getVendorSubmission();
        if (submission == null) {
            return AgentResult.failed(getStep().getStepName(), "Vendor submission data is missing.");
        }

        normalizationService.applyToSubmission(submission);
        context.setNormalizedLegalName(submission.getNormalizedLegalName());
        context.setNormalizedBankAccountHolderName(submission.getNormalizedBankAccountHolderName());

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("legalName", submission.getLegalName());
        snapshot.put("normalizedLegalName", submission.getNormalizedLegalName());
        snapshot.put("bankAccountHolderName", submission.getBankAccountHolderName());
        snapshot.put("normalizedBankAccountHolderName", submission.getNormalizedBankAccountHolderName());
        snapshot.put("country", submission.getCountry());
        snapshot.put("bankCountry", submission.getBankCountry());

        return AgentResult.completed(
                getStep().getStepName(),
                "Vendor submission received and normalized.",
                toJson(snapshot)
        );
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize intake snapshot", ex);
        }
    }
}
