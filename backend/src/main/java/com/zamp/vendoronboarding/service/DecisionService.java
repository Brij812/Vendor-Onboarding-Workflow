package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.dto.DecisionResponse;
import com.zamp.vendoronboarding.entity.Decision;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.exception.WorkflowRunNotFoundException;
import com.zamp.vendoronboarding.repository.DecisionRepository;
import com.zamp.vendoronboarding.repository.WorkflowRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final WorkflowRunRepository workflowRunRepository;

    public DecisionService(DecisionRepository decisionRepository,
                           WorkflowRunRepository workflowRunRepository) {
        this.decisionRepository = decisionRepository;
        this.workflowRunRepository = workflowRunRepository;
    }

    @Transactional
    public Decision persistDecision(WorkflowRun workflowRun, DecisionEvaluation evaluation) {
        WorkflowRun managedRun = workflowRunRepository.findById(workflowRun.getId())
                .orElseThrow(() -> new WorkflowRunNotFoundException(workflowRun.getId()));

        managedRun.setFinalDecisionStatus(evaluation.status());
        managedRun.setRiskScore(evaluation.riskScore());

        Decision decision = new Decision();
        decision.setWorkflowRun(managedRun);
        decision.setStatus(evaluation.status());
        decision.setRiskScore(evaluation.riskScore());
        decision.setReasonSummary(evaluation.reasonSummary());
        decision.setRequiredActions(joinLines(evaluation.requiredActions()));
        decision.setTriggeredRules(joinLines(evaluation.triggeredRules()));

        workflowRunRepository.save(managedRun);
        return decisionRepository.save(decision);
    }

    @Transactional(readOnly = true)
    public Optional<DecisionResponse> findResponseByWorkflowRunId(UUID workflowRunId) {
        return decisionRepository.findByWorkflowRun_Id(workflowRunId).map(this::toResponse);
    }

    private DecisionResponse toResponse(Decision decision) {
        return new DecisionResponse(
                decision.getStatus(),
                decision.getRiskScore(),
                decision.getReasonSummary(),
                decision.getRequiredActions(),
                decision.getTriggeredRules()
        );
    }

    private String joinLines(java.util.List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining("\n"));
    }
}
