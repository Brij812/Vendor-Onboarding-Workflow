package com.zamp.vendoronboarding.service;

import com.zamp.vendoronboarding.audit.AuditSummaryDraft;
import com.zamp.vendoronboarding.dto.AuditSummaryResponse;
import com.zamp.vendoronboarding.entity.AuditSummary;
import com.zamp.vendoronboarding.entity.WorkflowRun;
import com.zamp.vendoronboarding.repository.AuditSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuditSummaryService {

    private final AuditSummaryRepository auditSummaryRepository;

    public AuditSummaryService(AuditSummaryRepository auditSummaryRepository) {
        this.auditSummaryRepository = auditSummaryRepository;
    }

    @Transactional
    public AuditSummary persistAuditSummary(WorkflowRun workflowRun, AuditSummaryDraft draft) {
        AuditSummary auditSummary = new AuditSummary();
        auditSummary.setWorkflowRun(workflowRun);
        auditSummary.setSummary(draft.summary());
        auditSummary.setGenerationMethod(draft.generationMethod());
        auditSummary.setRawLlmResponse(draft.rawLlmResponse());
        return auditSummaryRepository.save(auditSummary);
    }

    @Transactional(readOnly = true)
    public Optional<AuditSummaryResponse> findResponseByWorkflowRunId(UUID workflowRunId) {
        return auditSummaryRepository.findByWorkflowRun_Id(workflowRunId).map(this::toResponse);
    }

    private AuditSummaryResponse toResponse(AuditSummary auditSummary) {
        return new AuditSummaryResponse(
                auditSummary.getSummary(),
                auditSummary.getGenerationMethod()
        );
    }
}
