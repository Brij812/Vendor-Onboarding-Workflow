package com.zamp.vendoronboarding.entity;

import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "document_extractions")
public class DocumentExtraction extends TimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_run_id", nullable = false)
    private WorkflowRun workflowRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_document_id", nullable = false)
    private UploadedDocument uploadedDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    private String legalEntityName;
    private String taxId;
    private String bankAccountHolderName;
    private String country;
    private LocalDate documentDate;
    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtractionMethod extractionMethod;

    @Column(columnDefinition = "TEXT")
    private String rawLlmResponse;

    public WorkflowRun getWorkflowRun() {
        return workflowRun;
    }

    public void setWorkflowRun(WorkflowRun workflowRun) {
        this.workflowRun = workflowRun;
    }

    public UploadedDocument getUploadedDocument() {
        return uploadedDocument;
    }

    public void setUploadedDocument(UploadedDocument uploadedDocument) {
        this.uploadedDocument = uploadedDocument;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getLegalEntityName() {
        return legalEntityName;
    }

    public void setLegalEntityName(String legalEntityName) {
        this.legalEntityName = legalEntityName;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getBankAccountHolderName() {
        return bankAccountHolderName;
    }

    public void setBankAccountHolderName(String bankAccountHolderName) {
        this.bankAccountHolderName = bankAccountHolderName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ExtractionMethod getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(ExtractionMethod extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getRawLlmResponse() {
        return rawLlmResponse;
    }

    public void setRawLlmResponse(String rawLlmResponse) {
        this.rawLlmResponse = rawLlmResponse;
    }
}
