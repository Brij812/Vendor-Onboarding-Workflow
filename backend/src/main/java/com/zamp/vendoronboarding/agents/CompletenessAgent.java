package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
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
import java.util.Set;
import java.util.function.Function;

@Component
public class CompletenessAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "COMPLETENESS_AGENT";

    private record FieldRule(String fieldName, Function<VendorSubmission, String> getter,
                             String code, String message, String recommendedAction) {
    }

    private record DocumentRule(DocumentType documentType, String code, String message, String recommendedAction) {
    }

    private static final List<FieldRule> REQUIRED_FIELDS = List.of(
            new FieldRule("legalName", VendorSubmission::getLegalName, "MISSING_LEGAL_NAME",
                    "Legal name is required for vendor onboarding.",
                    "Ask the vendor to provide the registered legal business name."),
            new FieldRule("country", VendorSubmission::getCountry, "MISSING_COUNTRY",
                    "Country is required for vendor onboarding.",
                    "Ask the vendor to provide their country of registration."),
            new FieldRule("contactEmail", VendorSubmission::getContactEmail, "MISSING_CONTACT_EMAIL",
                    "Contact email is required for vendor onboarding.",
                    "Ask the vendor to provide a business contact email address."),
            new FieldRule("taxId", VendorSubmission::getTaxId, "MISSING_TAX_ID",
                    "Tax ID is required for vendor onboarding.",
                    "Ask the vendor to provide a valid tax registration number."),
            new FieldRule("bankAccountHolderName", VendorSubmission::getBankAccountHolderName,
                    "MISSING_BANK_ACCOUNT_HOLDER_NAME",
                    "Bank account holder name is required for vendor onboarding.",
                    "Ask the vendor to provide the bank account holder name."),
            new FieldRule("bankCountry", VendorSubmission::getBankCountry, "MISSING_BANK_COUNTRY",
                    "Bank country is required for vendor onboarding.",
                    "Ask the vendor to provide the bank account country."),
            new FieldRule("bankCode", VendorSubmission::getBankCode, "MISSING_BANK_CODE",
                    "Bank code is required to verify vendor payment details.",
                    "Ask the vendor to provide IFSC/SWIFT/routing code."),
            new FieldRule("bankAccountLast4", VendorSubmission::getBankAccountLast4, "MISSING_BANK_ACCOUNT_LAST4",
                    "Bank account last 4 digits are required for vendor onboarding.",
                    "Ask the vendor to provide the last 4 digits of the bank account."),
            new FieldRule("businessCategory", VendorSubmission::getBusinessCategory, "MISSING_BUSINESS_CATEGORY",
                    "Business category is required for vendor onboarding.",
                    "Ask the vendor to provide their business category.")
    );

    private static final List<DocumentRule> REQUIRED_DOCUMENTS = List.of(
            new DocumentRule(DocumentType.TAX_REGISTRATION, "MISSING_TAX_REGISTRATION_DOCUMENT",
                    "Tax registration document is required for vendor onboarding.",
                    "Ask the vendor to upload their tax registration document (PDF)."),
            new DocumentRule(DocumentType.BANK_PROOF, "MISSING_BANK_PROOF_DOCUMENT",
                    "Bank proof document is required for vendor onboarding.",
                    "Ask the vendor to upload bank proof documentation (PDF)."),
            new DocumentRule(DocumentType.COMPANY_REGISTRATION, "MISSING_COMPANY_REGISTRATION_DOCUMENT",
                    "Company registration certificate is required for vendor onboarding.",
                    "Ask the vendor to upload their company registration certificate (PDF)."),
            new DocumentRule(DocumentType.COMPLIANCE_DECLARATION, "MISSING_COMPLIANCE_DECLARATION_DOCUMENT",
                    "Compliance declaration is required for vendor onboarding.",
                    "Ask the vendor to upload the compliance declaration (PDF).")
    );

    private final ObjectMapper objectMapper;

    public CompletenessAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.COMPLETENESS_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        VendorSubmission submission = context.getVendorSubmission();
        Set<DocumentType> uploadedDocumentTypes = context.getUploadedDocumentTypes();
        List<IssueDraft> issues = new ArrayList<>();
        List<String> missingFields = new ArrayList<>();
        List<String> checkedFields = new ArrayList<>();
        List<String> checkedDocuments = new ArrayList<>();
        List<String> missingDocuments = new ArrayList<>();

        for (FieldRule rule : REQUIRED_FIELDS) {
            checkedFields.add(rule.fieldName());
            String value = submission != null ? rule.getter().apply(submission) : null;
            if (isBlank(value)) {
                missingFields.add(rule.fieldName());
                issues.add(IssueDraft.of(
                        SOURCE_STEP,
                        rule.code(),
                        IssueSeverity.MEDIUM,
                        rule.message(),
                        rule.recommendedAction(),
                        rule.fieldName()
                ));
            }
        }

        for (DocumentRule rule : REQUIRED_DOCUMENTS) {
            checkedDocuments.add(rule.documentType().name());
            if (!uploadedDocumentTypes.contains(rule.documentType())) {
                missingDocuments.add(rule.documentType().name());
                issues.add(IssueDraft.of(
                        SOURCE_STEP,
                        rule.code(),
                        IssueSeverity.MEDIUM,
                        rule.message(),
                        rule.recommendedAction(),
                        rule.documentType().name()
                ));
            }
        }

        context.addIssues(issues);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("checkedFields", checkedFields);
        snapshot.put("missingFields", missingFields);
        snapshot.put("checkedDocuments", checkedDocuments);
        snapshot.put("missingDocuments", missingDocuments);
        snapshot.put("issueCount", issues.size());

        String outputSnapshot = toJson(snapshot);
        if (issues.isEmpty()) {
            return AgentResult.completed(
                    getStep().getStepName(),
                    "All required vendor fields and documents are present.",
                    outputSnapshot
            );
        }

        return AgentResult.warning(
                getStep().getStepName(),
                buildSummary(missingFields.size(), missingDocuments.size()),
                outputSnapshot,
                issues
        );
    }

    private String buildSummary(int missingFieldCount, int missingDocumentCount) {
        List<String> parts = new ArrayList<>();
        if (missingFieldCount > 0) {
            parts.add(missingFieldCount + " missing required field(s)");
        }
        if (missingDocumentCount > 0) {
            parts.add(missingDocumentCount + " missing document(s)");
        }
        return "Found " + String.join(" and ", parts) + ".";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize completeness snapshot", ex);
        }
    }
}
