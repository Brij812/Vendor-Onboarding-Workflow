package com.zamp.vendoronboarding.workflow;

import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DocumentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorkflowContext {

    private final UUID workflowRunId;
    private final VendorSubmission vendorSubmission;
    private String normalizedLegalName;
    private String normalizedBankAccountHolderName;
    private final List<IssueDraft> issues = new ArrayList<>();
    private final Map<String, String> stepOutputs = new HashMap<>();
    private final Map<String, Object> extractedDocumentData = new HashMap<>();
    private List<DocumentExtraction> extractedDocumentExtractions = new ArrayList<>();
    private Object decision;
    private Object communication;
    private Object auditSummary;
    private Set<DocumentType> uploadedDocumentTypes = EnumSet.noneOf(DocumentType.class);

    public WorkflowContext(UUID workflowRunId, VendorSubmission vendorSubmission) {
        this.workflowRunId = workflowRunId;
        this.vendorSubmission = vendorSubmission;
        if (vendorSubmission != null) {
            this.normalizedLegalName = vendorSubmission.getNormalizedLegalName();
            this.normalizedBankAccountHolderName = vendorSubmission.getNormalizedBankAccountHolderName();
        }
    }

    public UUID getWorkflowRunId() {
        return workflowRunId;
    }

    public VendorSubmission getVendorSubmission() {
        return vendorSubmission;
    }

    public String getNormalizedLegalName() {
        return normalizedLegalName;
    }

    public void setNormalizedLegalName(String normalizedLegalName) {
        this.normalizedLegalName = normalizedLegalName;
    }

    public String getNormalizedBankAccountHolderName() {
        return normalizedBankAccountHolderName;
    }

    public void setNormalizedBankAccountHolderName(String normalizedBankAccountHolderName) {
        this.normalizedBankAccountHolderName = normalizedBankAccountHolderName;
    }

    public List<IssueDraft> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public void addIssues(List<IssueDraft> newIssues) {
        if (newIssues != null && !newIssues.isEmpty()) {
            issues.addAll(newIssues);
        }
    }

    public Map<String, String> getStepOutputs() {
        return Collections.unmodifiableMap(stepOutputs);
    }

    public void putStepOutput(String stepName, String outputSnapshot) {
        stepOutputs.put(stepName, outputSnapshot);
    }

    public Map<String, Object> getExtractedDocumentData() {
        return Collections.unmodifiableMap(extractedDocumentData);
    }

    public void putExtractedDocumentData(String key, Object value) {
        extractedDocumentData.put(key, value);
    }

    public List<DocumentExtraction> getExtractedDocumentExtractions() {
        return Collections.unmodifiableList(extractedDocumentExtractions);
    }

    public void setExtractedDocumentExtractions(List<DocumentExtraction> extractions) {
        if (extractions == null || extractions.isEmpty()) {
            this.extractedDocumentExtractions = new ArrayList<>();
        } else {
            this.extractedDocumentExtractions = new ArrayList<>(extractions);
        }
    }

    public Object getDecision() {
        return decision;
    }

    public void setDecision(Object decision) {
        this.decision = decision;
    }

    public Object getCommunication() {
        return communication;
    }

    public void setCommunication(Object communication) {
        this.communication = communication;
    }

    public Object getAuditSummary() {
        return auditSummary;
    }

    public void setAuditSummary(Object auditSummary) {
        this.auditSummary = auditSummary;
    }

    public Set<DocumentType> getUploadedDocumentTypes() {
        return Collections.unmodifiableSet(uploadedDocumentTypes);
    }

    public void setUploadedDocumentTypes(Set<DocumentType> uploadedDocumentTypes) {
        if (uploadedDocumentTypes == null || uploadedDocumentTypes.isEmpty()) {
            this.uploadedDocumentTypes = EnumSet.noneOf(DocumentType.class);
        } else {
            this.uploadedDocumentTypes = EnumSet.copyOf(uploadedDocumentTypes);
        }
    }
}
