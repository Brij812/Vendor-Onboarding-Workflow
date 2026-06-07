package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "workflow_runs")
public class WorkflowRun extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_submission_id", nullable = false)
    private VendorSubmission vendorSubmission;

    @Column(name = "run_number", unique = true)
    private Long runNumber;

    @Column(name = "display_run_id", unique = true)
    private String displayRunId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RunStatus status = RunStatus.PENDING;

    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision_status")
    private DecisionStatus finalDecisionStatus;

    private Integer riskScore;

    private Instant startedAt;
    private Instant completedAt;
    private Instant failedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public VendorSubmission getVendorSubmission() {
        return vendorSubmission;
    }

    public void setVendorSubmission(VendorSubmission vendorSubmission) {
        this.vendorSubmission = vendorSubmission;
    }

    public Long getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(Long runNumber) {
        this.runNumber = runNumber;
    }

    public String getDisplayRunId() {
        return displayRunId;
    }

    public void setDisplayRunId(String displayRunId) {
        this.displayRunId = displayRunId;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public DecisionStatus getFinalDecisionStatus() {
        return finalDecisionStatus;
    }

    public void setFinalDecisionStatus(DecisionStatus finalDecisionStatus) {
        this.finalDecisionStatus = finalDecisionStatus;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
