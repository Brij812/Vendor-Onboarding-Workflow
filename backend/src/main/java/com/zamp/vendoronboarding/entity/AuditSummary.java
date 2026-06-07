package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_summaries")
public class AuditSummary extends TimestampedEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_run_id", nullable = false, unique = true)
    private WorkflowRun workflowRun;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationMethod generationMethod;

    @Column(columnDefinition = "TEXT")
    private String rawLlmResponse;

    public WorkflowRun getWorkflowRun() {
        return workflowRun;
    }

    public void setWorkflowRun(WorkflowRun workflowRun) {
        this.workflowRun = workflowRun;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public GenerationMethod getGenerationMethod() {
        return generationMethod;
    }

    public void setGenerationMethod(GenerationMethod generationMethod) {
        this.generationMethod = generationMethod;
    }

    public String getRawLlmResponse() {
        return rawLlmResponse;
    }

    public void setRawLlmResponse(String rawLlmResponse) {
        this.rawLlmResponse = rawLlmResponse;
    }
}
