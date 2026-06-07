package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.document.DocumentExtractionFieldResolver;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.UploadedDocument;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.util.DomainUtils;
import com.zamp.vendoronboarding.util.NameMatcher;
import com.zamp.vendoronboarding.util.NormalizationRules;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ConsistencyCheckAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "CONSISTENCY_CHECK_AGENT";
    private static final String EVIDENCE_VENDOR_SUBMISSION = "vendor_submission";
    private static final String EVIDENCE_DOCUMENT_EXTRACTION = "document_extraction";
    private static final double SUBMISSION_CONFIDENCE = 0.95;
    private static final double DOCUMENT_DEFAULT_CONFIDENCE = 0.85;

    private final ObjectMapper objectMapper;

    public ConsistencyCheckAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.CONSISTENCY_CHECK_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        VendorSubmission submission = context.getVendorSubmission();
        List<IssueDraft> issues = new ArrayList<>();

        boolean legalNameMatchesBankName = checkBankNameMatch(context, submission, issues);
        boolean countryMatchesBankCountry = checkCountryMatch(submission, issues);
        String emailDomain = submission != null ? DomainUtils.extractEmailDomain(submission.getContactEmail()) : null;
        String websiteDomain = submission != null ? DomainUtils.extractWebsiteDomain(submission.getWebsite()) : null;
        boolean emailWebsiteDomainMatch = checkEmailWebsiteDomain(submission, emailDomain, websiteDomain, issues);

        List<DocumentExtraction> extractions = context.getExtractedDocumentExtractions();
        boolean documentLegalNameMatches = checkDocumentLegalNameMatch(context, submission, extractions, issues);
        boolean documentTaxIdMatches = checkDocumentTaxIdMatch(submission, extractions, issues);
        boolean documentBankNameMatches = checkDocumentBankNameMatch(context, submission, extractions, issues);
        boolean documentCountryMatches = checkDocumentCountryMatch(submission, extractions, issues);
        List<Map<String, Object>> wrongDocumentTypeChecks = checkWrongDocumentType(extractions, issues);
        boolean wrongDocumentTypeDetected = !wrongDocumentTypeChecks.isEmpty();

        context.addIssues(issues);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("legalNameMatchesBankName", legalNameMatchesBankName);
        snapshot.put("countryMatchesBankCountry", countryMatchesBankCountry);
        snapshot.put("emailDomain", emailDomain);
        snapshot.put("websiteDomain", websiteDomain);
        snapshot.put("emailWebsiteDomainMatch", emailWebsiteDomainMatch);
        snapshot.put("documentLegalNameMatches", documentLegalNameMatches);
        snapshot.put("documentTaxIdMatches", documentTaxIdMatches);
        snapshot.put("documentBankNameMatches", documentBankNameMatches);
        snapshot.put("documentCountryMatches", documentCountryMatches);
        snapshot.put("wrongDocumentTypeDetected", wrongDocumentTypeDetected);
        snapshot.put("wrongDocumentTypeChecks", wrongDocumentTypeChecks);
        snapshot.put("issueCount", issues.size());

        String outputSnapshot = toJson(snapshot);
        if (issues.isEmpty()) {
            return AgentResult.completed(
                    getStep().getStepName(),
                    "Submitted vendor details are internally consistent.",
                    outputSnapshot
            );
        }

        return AgentResult.warning(
                getStep().getStepName(),
                "Found " + issues.size() + " consistency issue(s).",
                outputSnapshot,
                issues
        );
    }

    private boolean checkBankNameMatch(WorkflowContext context, VendorSubmission submission, List<IssueDraft> issues) {
        String normalizedLegal = context.getNormalizedLegalName();
        String normalizedBank = context.getNormalizedBankAccountHolderName();
        if (NameMatcher.namesConsistent(normalizedLegal, normalizedBank)) {
            return true;
        }

        String expected = submission != null ? submission.getLegalName() : normalizedLegal;
        String actual = submission != null ? submission.getBankAccountHolderName() : normalizedBank;
        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "BANK_NAME_MISMATCH",
                IssueSeverity.MEDIUM,
                "Bank account holder name does not match the vendor legal name.",
                "Ask the vendor to provide bank proof under the submitted legal entity name or clarify the correct legal entity.",
                "bankAccountHolderName",
                expected,
                actual,
                SUBMISSION_CONFIDENCE,
                EVIDENCE_VENDOR_SUBMISSION,
                submissionEvidenceText("legal name vs bank account holder", expected, actual)
        ));
        return false;
    }

    private boolean checkCountryMatch(VendorSubmission submission, List<IssueDraft> issues) {
        if (submission == null) {
            return true;
        }

        String country = submission.getCountry();
        String bankCountry = submission.getBankCountry();
        if (country == null || bankCountry == null) {
            return true;
        }

        if (country.trim().equalsIgnoreCase(bankCountry.trim())) {
            return true;
        }

        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "COUNTRY_MISMATCH",
                IssueSeverity.MEDIUM,
                "Bank country does not match the vendor's declared country.",
                "Ask the vendor to confirm the bank country or provide additional banking/compliance details.",
                "bankCountry",
                country,
                bankCountry,
                SUBMISSION_CONFIDENCE,
                EVIDENCE_VENDOR_SUBMISSION,
                submissionEvidenceText("country vs bank country", country, bankCountry)
        ));
        return false;
    }

    private boolean checkEmailWebsiteDomain(VendorSubmission submission, String emailDomain, String websiteDomain,
                                            List<IssueDraft> issues) {
        if (submission == null || websiteDomain == null || websiteDomain.isBlank()) {
            return true;
        }

        if (emailDomain == null || emailDomain.isBlank()) {
            return false;
        }

        if (DomainUtils.isPublicEmailDomain(emailDomain)) {
            issues.add(IssueDraft.withEvidence(
                    SOURCE_STEP,
                    "PUBLIC_EMAIL_DOMAIN",
                    IssueSeverity.LOW,
                    "Vendor contact email uses a public email domain instead of the company website domain.",
                    "Ask the vendor to provide an official business email address if available.",
                    "contactEmail",
                    websiteDomain,
                    emailDomain,
                    SUBMISSION_CONFIDENCE,
                    EVIDENCE_VENDOR_SUBMISSION,
                    submissionEvidenceText("website domain vs email domain", websiteDomain, emailDomain)
            ));
            return false;
        }

        if (DomainUtils.domainsMatch(emailDomain, websiteDomain)) {
            return true;
        }

        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "EMAIL_WEBSITE_DOMAIN_MISMATCH",
                IssueSeverity.LOW,
                "Vendor email domain does not match the submitted website domain.",
                "Confirm whether the submitted contact email belongs to the vendor organization.",
                "contactEmail",
                websiteDomain,
                emailDomain,
                SUBMISSION_CONFIDENCE,
                EVIDENCE_VENDOR_SUBMISSION,
                submissionEvidenceText("website domain vs email domain", websiteDomain, emailDomain)
        ));
        return false;
    }

    private boolean checkDocumentLegalNameMatch(WorkflowContext context,
                                                VendorSubmission submission,
                                                List<DocumentExtraction> extractions,
                                                List<IssueDraft> issues) {
        if (!DocumentExtractionFieldResolver.hasStructuredData(extractions)) {
            return true;
        }

        String extractedLegalEntityName = DocumentExtractionFieldResolver.resolveLegalEntityName(extractions);
        if (extractedLegalEntityName == null) {
            return true;
        }

        String normalizedSubmissionLegal = context.getNormalizedLegalName();
        String normalizedExtractedLegal = NormalizationRules.normalizeName(extractedLegalEntityName);
        if (NameMatcher.namesConsistent(normalizedSubmissionLegal, normalizedExtractedLegal)) {
            return true;
        }

        String expectedLegal = submission != null ? submission.getLegalName() : normalizedSubmissionLegal;
        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "DOCUMENT_LEGAL_NAME_MISMATCH",
                IssueSeverity.MEDIUM,
                "Legal entity name on uploaded documents does not match the submitted legal name.",
                "Ask the vendor to provide corrected company registration or tax documents that match the submitted legal name, or clarify the legal entity name.",
                "legalName",
                expectedLegal,
                extractedLegalEntityName,
                documentConfidence(extractions),
                EVIDENCE_DOCUMENT_EXTRACTION,
                documentEvidenceText(extractions, "legal entity name mismatch")
        ));
        return false;
    }

    private boolean checkDocumentTaxIdMatch(VendorSubmission submission,
                                            List<DocumentExtraction> extractions,
                                            List<IssueDraft> issues) {
        if (!DocumentExtractionFieldResolver.hasStructuredData(extractions)) {
            return true;
        }

        String extractedTaxId = DocumentExtractionFieldResolver.resolveTaxId(extractions);
        if (extractedTaxId == null || submission == null || submission.getTaxId() == null) {
            return true;
        }

        if (submission.getTaxId().trim().equalsIgnoreCase(extractedTaxId.trim())) {
            return true;
        }

        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "DOCUMENT_TAX_ID_MISMATCH",
                IssueSeverity.HIGH,
                "Tax ID on uploaded documents does not match the submitted tax ID.",
                "Ask the vendor to provide corrected tax registration documents or clarify the correct tax ID.",
                "taxId",
                submission.getTaxId(),
                extractedTaxId,
                documentConfidence(extractions),
                EVIDENCE_DOCUMENT_EXTRACTION,
                documentEvidenceText(extractions, "tax ID mismatch")
        ));
        return false;
    }

    private boolean checkDocumentBankNameMatch(WorkflowContext context,
                                               VendorSubmission submission,
                                               List<DocumentExtraction> extractions,
                                               List<IssueDraft> issues) {
        if (!DocumentExtractionFieldResolver.hasStructuredData(extractions)) {
            return true;
        }

        String extractedBankName = DocumentExtractionFieldResolver.resolveBankAccountHolderName(extractions);
        if (extractedBankName == null) {
            return true;
        }

        String normalizedSubmissionBank = context.getNormalizedBankAccountHolderName();
        String normalizedExtractedBank = NormalizationRules.normalizeName(extractedBankName);
        if (NameMatcher.namesConsistent(normalizedSubmissionBank, normalizedExtractedBank)) {
            return true;
        }

        String expectedBank = submission != null ? submission.getBankAccountHolderName() : normalizedSubmissionBank;
        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "DOCUMENT_BANK_NAME_MISMATCH",
                IssueSeverity.MEDIUM,
                "Bank account holder name on uploaded documents does not match the submitted bank account holder name.",
                "Ask the vendor to provide corrected bank proof documents or clarify the bank account holder name.",
                "bankAccountHolderName",
                expectedBank,
                extractedBankName,
                documentConfidence(extractions),
                EVIDENCE_DOCUMENT_EXTRACTION,
                documentEvidenceText(extractions, "bank account holder name mismatch")
        ));
        return false;
    }

    private boolean checkDocumentCountryMatch(VendorSubmission submission,
                                              List<DocumentExtraction> extractions,
                                              List<IssueDraft> issues) {
        if (!DocumentExtractionFieldResolver.hasStructuredData(extractions)) {
            return true;
        }

        String extractedCountry = DocumentExtractionFieldResolver.resolveCountry(extractions);
        if (extractedCountry == null || submission == null || submission.getCountry() == null) {
            return true;
        }

        if (submission.getCountry().trim().equalsIgnoreCase(extractedCountry.trim())) {
            return true;
        }

        issues.add(IssueDraft.withEvidence(
                SOURCE_STEP,
                "DOCUMENT_COUNTRY_MISMATCH",
                IssueSeverity.MEDIUM,
                "Country on uploaded documents does not match the submitted country.",
                "Ask the vendor to provide corrected documents showing the submitted country or clarify the country of registration.",
                "country",
                submission.getCountry(),
                extractedCountry,
                documentConfidence(extractions),
                EVIDENCE_DOCUMENT_EXTRACTION,
                documentEvidenceText(extractions, "country mismatch")
        ));
        return false;
    }

    private List<Map<String, Object>> checkWrongDocumentType(List<DocumentExtraction> extractions,
                                                              List<IssueDraft> issues) {
        List<Map<String, Object>> checks = new ArrayList<>();
        if (extractions == null || extractions.isEmpty()) {
            return checks;
        }

        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() != ExtractionMethod.LLM) {
                continue;
            }

            UploadedDocument uploadedDocument = extraction.getUploadedDocument();
            if (uploadedDocument == null) {
                continue;
            }

            DocumentType expectedType = uploadedDocument.getDocumentType();
            DocumentType detectedType = extraction.getDocumentType();
            if (detectedType == null || detectedType == DocumentType.UNKNOWN || expectedType == detectedType) {
                continue;
            }

            String filename = uploadedDocument.getOriginalFilename();
            issues.add(IssueDraft.withEvidence(
                    SOURCE_STEP,
                    "WRONG_DOCUMENT_TYPE",
                    IssueSeverity.MEDIUM,
                    "Uploaded document appears to be a different type than expected.",
                    recommendedActionForSlot(expectedType),
                    "documentType",
                    expectedType.name(),
                    detectedType.name(),
                    extraction.getConfidenceScore() != null ? extraction.getConfidenceScore() : DOCUMENT_DEFAULT_CONFIDENCE,
                    EVIDENCE_DOCUMENT_EXTRACTION,
                    String.format("Document '%s' (%s): expected type %s, detected type %s",
                            filename, expectedType.name(), expectedType.name(), detectedType.name())
            ));

            Map<String, Object> check = new LinkedHashMap<>();
            check.put("expectedDocumentType", expectedType.name());
            check.put("detectedDocumentType", detectedType.name());
            check.put("wrongDocumentTypeDetected", true);
            check.put("affectedFilename", uploadedDocument.getOriginalFilename());
            checks.add(check);
        }

        return checks;
    }

    private String recommendedActionForSlot(DocumentType expectedType) {
        return switch (expectedType) {
            case TAX_REGISTRATION ->
                    "Ask the vendor to upload the correct tax registration document.";
            case BANK_PROOF ->
                    "Ask the vendor to upload the correct bank proof document.";
            case COMPANY_REGISTRATION ->
                    "Ask the vendor to upload the correct company registration document.";
            case COMPLIANCE_DECLARATION ->
                    "Ask the vendor to upload the correct compliance declaration document.";
            case UNKNOWN ->
                    "Ask the vendor to upload the correct document for the selected slot.";
        };
    }

    private String submissionEvidenceText(String fieldLabel, String expected, String actual) {
        return String.format("Compared %s: expected '%s', actual '%s' (from vendor submission).",
                fieldLabel, expected, actual);
    }

    private String documentEvidenceText(List<DocumentExtraction> extractions, String detail) {
        DocumentExtraction extraction = firstLlmExtraction(extractions);
        if (extraction == null) {
            return detail;
        }
        UploadedDocument doc = extraction.getUploadedDocument();
        String filename = doc != null ? doc.getOriginalFilename() : "unknown";
        String docType = doc != null ? doc.getDocumentType().name() : "UNKNOWN";
        return String.format("Document '%s' (%s): %s", filename, docType, detail);
    }

    private Double documentConfidence(List<DocumentExtraction> extractions) {
        DocumentExtraction extraction = firstLlmExtraction(extractions);
        if (extraction != null && extraction.getConfidenceScore() != null) {
            return extraction.getConfidenceScore();
        }
        return DOCUMENT_DEFAULT_CONFIDENCE;
    }

    private DocumentExtraction firstLlmExtraction(List<DocumentExtraction> extractions) {
        if (extractions == null) {
            return null;
        }
        for (DocumentExtraction extraction : extractions) {
            if (extraction.getExtractionMethod() == ExtractionMethod.LLM) {
                return extraction;
            }
        }
        return extractions.isEmpty() ? null : extractions.get(0);
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize consistency snapshot", ex);
        }
    }
}
