package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.StepStatus;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsistencyCheckAgentTest {

    private final ConsistencyCheckAgent agent = new ConsistencyCheckAgent(new ObjectMapper());

    @Test
    void execute_consistentVendor_returnsCompleted() {
        WorkflowContext context = contextWithSubmission(approvedSubmission());

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void execute_bankNameMismatch_returnsWarning() {
        VendorSubmission submission = approvedSubmission();
        submission.setLegalName("Acme Cloud Services Pvt Ltd");
        submission.setBankAccountHolderName("Acme Consulting LLP");
        submission.setNormalizedLegalName("acme cloud services private limited");
        submission.setNormalizedBankAccountHolderName("acme consulting llp");

        AgentResult result = agent.execute(new WorkflowContext(UUID.randomUUID(), submission));

        assertEquals(StepStatus.WARNING, result.status());
        IssueDraft bankIssue = result.issues().stream()
                .filter(issue -> "BANK_NAME_MISMATCH".equals(issue.code()))
                .findFirst()
                .orElseThrow();
        assertEquals("vendor_submission", bankIssue.evidenceSource());
        assertEquals(0.95, bankIssue.confidence());
        assertTrue(bankIssue.evidenceText().contains("vendor submission"));
    }

    @Test
    void execute_publicEmailDomain_returnsWarning() {
        VendorSubmission submission = approvedSubmission();
        submission.setContactEmail("vendor@gmail.com");
        submission.setWebsite("https://somevendor.in");

        AgentResult result = agent.execute(new WorkflowContext(UUID.randomUUID(), submission));

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.issues().stream().anyMatch(issue -> "PUBLIC_EMAIL_DOMAIN".equals(issue.code())));
    }

    @Test
    void execute_documentTaxIdMismatch_returnsHighSeverityIssue() {
        VendorSubmission submission = approvedSubmission();
        submission.setTaxId("29ABCDE1234F1Z5");

        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);
        context.setExtractedDocumentExtractions(List.of(llmExtraction(
                DocumentType.TAX_REGISTRATION,
                null,
                "29DIFFER1234F1Z5",
                null,
                null
        )));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.issues().stream().anyMatch(issue ->
                "DOCUMENT_TAX_ID_MISMATCH".equals(issue.code())
                        && issue.severity() == IssueSeverity.HIGH));
    }

    @Test
    void execute_documentLegalNameMismatch_returnsMediumSeverityIssue() {
        VendorSubmission submission = approvedSubmission();
        submission.setLegalName("BrightLayer Technologies Pvt Ltd");
        submission.setNormalizedLegalName("brightlayer technologies private limited");

        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);
        context.setExtractedDocumentExtractions(List.of(llmExtraction(
                DocumentType.COMPANY_REGISTRATION,
                "Acme Cloud Services Pvt Ltd",
                null,
                null,
                null
        )));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        assertTrue(result.issues().stream().anyMatch(issue ->
                "DOCUMENT_LEGAL_NAME_MISMATCH".equals(issue.code())
                        && issue.severity() == IssueSeverity.MEDIUM));
    }

    @Test
    void execute_noStructuredExtractionData_skipsDocumentChecks() {
        VendorSubmission submission = approvedSubmission();
        submission.setTaxId("29ABCDE1234F1Z5");

        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), submission);
        context.setExtractedDocumentExtractions(List.of(fallbackExtraction()));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.COMPLETED, result.status());
        assertTrue(result.issues().stream().noneMatch(issue -> issue.code().startsWith("DOCUMENT_")));
    }

    @Test
    void execute_wrongDocumentType_returnsMediumIssue() throws Exception {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), approvedSubmission());
        context.setExtractedDocumentExtractions(List.of(llmExtraction(
                uploadedDocument(DocumentType.TAX_REGISTRATION, "bank-proof.pdf"),
                DocumentType.BANK_PROOF,
                null,
                null,
                null,
                null
        )));

        AgentResult result = agent.execute(context);

        assertEquals(StepStatus.WARNING, result.status());
        IssueDraft issue = result.issues().stream()
                .filter(candidate -> "WRONG_DOCUMENT_TYPE".equals(candidate.code()))
                .findFirst()
                .orElseThrow();
        assertEquals(IssueSeverity.MEDIUM, issue.severity());
        assertEquals("documentType", issue.fieldName());
        assertEquals("TAX_REGISTRATION", issue.expectedValue());
        assertEquals("BANK_PROOF", issue.actualValue());
        assertEquals("Uploaded document appears to be a different type than expected.", issue.message());
        assertEquals("Ask the vendor to upload the correct tax registration document.", issue.recommendedAction());

        JsonNode snapshot = new ObjectMapper().readTree(result.outputSnapshot());
        assertTrue(snapshot.get("wrongDocumentTypeDetected").asBoolean());
        assertEquals(1, snapshot.get("wrongDocumentTypeChecks").size());
        assertEquals("TAX_REGISTRATION",
                snapshot.get("wrongDocumentTypeChecks").get(0).get("expectedDocumentType").asText());
        assertEquals("BANK_PROOF",
                snapshot.get("wrongDocumentTypeChecks").get(0).get("detectedDocumentType").asText());
        assertEquals("bank-proof.pdf",
                snapshot.get("wrongDocumentTypeChecks").get(0).get("affectedFilename").asText());
    }

    @Test
    void execute_unknownDetectedType_skipsWrongDocumentIssue() {
        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), approvedSubmission());
        context.setExtractedDocumentExtractions(List.of(llmExtraction(
                uploadedDocument(DocumentType.TAX_REGISTRATION, "tax-registration.pdf"),
                DocumentType.TAX_REGISTRATION,
                null,
                null,
                null,
                null
        )));

        AgentResult result = agent.execute(context);

        assertFalse(result.issues().stream().anyMatch(issue -> "WRONG_DOCUMENT_TYPE".equals(issue.code())));
    }

    @Test
    void execute_fallbackExtraction_skipsWrongDocumentIssue() {
        UploadedDocument uploadedDocument = uploadedDocument(DocumentType.TAX_REGISTRATION, "bank-proof.pdf");
        DocumentExtraction extraction = fallbackExtraction();
        extraction.setUploadedDocument(uploadedDocument);

        WorkflowContext context = new WorkflowContext(UUID.randomUUID(), approvedSubmission());
        context.setExtractedDocumentExtractions(List.of(extraction));

        AgentResult result = agent.execute(context);

        assertFalse(result.issues().stream().anyMatch(issue -> "WRONG_DOCUMENT_TYPE".equals(issue.code())));
    }

    private WorkflowContext contextWithSubmission(VendorSubmission submission) {
        return new WorkflowContext(UUID.randomUUID(), submission);
    }

    private VendorSubmission approvedSubmission() {
        VendorSubmission submission = IntakeAgentTest.approvedVendor();
        submission.setNormalizedLegalName("brightlayer technologies private limited");
        submission.setNormalizedBankAccountHolderName("brightlayer technologies private limited");
        return submission;
    }

    private DocumentExtraction llmExtraction(UploadedDocument uploadedDocument,
                                             DocumentType documentType,
                                             String legalEntityName,
                                             String taxId,
                                             String bankAccountHolderName,
                                             String country) {
        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setExtractionMethod(ExtractionMethod.LLM);
        extraction.setDocumentType(documentType);
        extraction.setLegalEntityName(legalEntityName);
        extraction.setTaxId(taxId);
        extraction.setBankAccountHolderName(bankAccountHolderName);
        extraction.setCountry(country);
        extraction.setUploadedDocument(uploadedDocument);
        return extraction;
    }

    private DocumentExtraction llmExtraction(DocumentType documentType,
                                             String legalEntityName,
                                             String taxId,
                                             String bankAccountHolderName,
                                             String country) {
        return llmExtraction(null, documentType, legalEntityName, taxId, bankAccountHolderName, country);
    }

    private UploadedDocument uploadedDocument(DocumentType documentType, String filename) {
        UploadedDocument uploadedDocument = new UploadedDocument();
        uploadedDocument.setDocumentType(documentType);
        uploadedDocument.setOriginalFilename(filename);
        return uploadedDocument;
    }

    private DocumentExtraction fallbackExtraction() {
        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setExtractionMethod(ExtractionMethod.FALLBACK);
        extraction.setDocumentType(DocumentType.TAX_REGISTRATION);
        extraction.setRawLlmResponse("LLM API key is not configured.");
        return extraction;
    }
}
