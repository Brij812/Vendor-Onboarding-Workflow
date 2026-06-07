package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.util.FormatValidators;
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
public class FormatValidationAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "FORMAT_VALIDATION_AGENT";

    private final ObjectMapper objectMapper;

    public FormatValidationAgent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.FORMAT_VALIDATION_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        VendorSubmission submission = context.getVendorSubmission();
        List<IssueDraft> issues = new ArrayList<>();

        boolean emailValid = validateEmail(submission, issues);
        boolean websiteValid = validateWebsite(submission, issues);
        boolean bankAccountLast4Valid = validateBankAccountLast4(submission, issues);
        boolean taxIdFormatValid = validateTaxId(submission, issues);
        boolean bankCodeFormatValid = validateBankCode(submission, issues);

        context.addIssues(issues);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("emailValid", emailValid);
        snapshot.put("websiteValid", websiteValid);
        snapshot.put("taxIdFormatValid", taxIdFormatValid);
        snapshot.put("bankCodeFormatValid", bankCodeFormatValid);
        snapshot.put("bankAccountLast4Valid", bankAccountLast4Valid);
        snapshot.put("issueCount", issues.size());

        String outputSnapshot = toJson(snapshot);
        if (issues.isEmpty()) {
            return AgentResult.completed(
                    getStep().getStepName(),
                    "Email, website, tax ID, bank code, and bank account fields passed format validation.",
                    outputSnapshot
            );
        }

        return AgentResult.warning(
                getStep().getStepName(),
                "Found " + issues.size() + " format validation issue(s).",
                outputSnapshot,
                issues
        );
    }

    private boolean validateEmail(VendorSubmission submission, List<IssueDraft> issues) {
        String email = submission != null ? submission.getContactEmail() : null;
        if (FormatValidators.isValidEmail(email)) {
            return true;
        }
        issues.add(IssueDraft.of(
                SOURCE_STEP,
                "INVALID_EMAIL_FORMAT",
                IssueSeverity.MEDIUM,
                "Contact email does not appear to be valid.",
                "Ask the vendor to provide a valid business email address.",
                "contactEmail"
        ));
        return false;
    }

    private boolean validateWebsite(VendorSubmission submission, List<IssueDraft> issues) {
        String website = submission != null ? submission.getWebsite() : null;
        if (website == null || website.isBlank()) {
            return true;
        }
        if (FormatValidators.isValidWebsite(website)) {
            return true;
        }
        issues.add(IssueDraft.of(
                SOURCE_STEP,
                "INVALID_WEBSITE_FORMAT",
                IssueSeverity.LOW,
                "Website URL does not appear to be valid.",
                "Ask the vendor to provide a complete website URL including http:// or https://.",
                "website"
        ));
        return false;
    }

    private boolean validateBankAccountLast4(VendorSubmission submission, List<IssueDraft> issues) {
        String last4 = submission != null ? submission.getBankAccountLast4() : null;
        if (FormatValidators.isValidBankAccountLast4(last4)) {
            return true;
        }
        issues.add(IssueDraft.of(
                SOURCE_STEP,
                "INVALID_BANK_ACCOUNT_LAST4",
                IssueSeverity.MEDIUM,
                "Bank account last 4 digits must be exactly 4 numeric digits.",
                "Ask the vendor to provide the last 4 digits of the bank account.",
                "bankAccountLast4"
        ));
        return false;
    }

    private boolean validateTaxId(VendorSubmission submission, List<IssueDraft> issues) {
        if (submission == null || !FormatValidators.isIndia(submission.getCountry())) {
            return true;
        }
        String taxId = submission.getTaxId();
        if (taxId == null || taxId.isBlank()) {
            return true;
        }
        if (FormatValidators.isValidGstin(taxId)) {
            return true;
        }
        issues.add(IssueDraft.of(
                SOURCE_STEP,
                "INVALID_GSTIN_FORMAT",
                IssueSeverity.HIGH,
                "Tax ID does not match the expected GSTIN format for India.",
                "Ask the vendor to provide a valid GSTIN.",
                "taxId"
        ));
        return false;
    }

    private boolean validateBankCode(VendorSubmission submission, List<IssueDraft> issues) {
        if (submission == null || !FormatValidators.isIndia(submission.getBankCountry())) {
            return true;
        }
        String bankCode = submission.getBankCode();
        if (bankCode == null || bankCode.isBlank()) {
            return true;
        }
        if (FormatValidators.isValidIfsc(bankCode)) {
            return true;
        }
        issues.add(IssueDraft.of(
                SOURCE_STEP,
                "INVALID_IFSC_FORMAT",
                IssueSeverity.HIGH,
                "Bank code does not match the expected IFSC format for India.",
                "Ask the vendor to provide a valid IFSC code.",
                "bankCode"
        ));
        return false;
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize format validation snapshot", ex);
        }
    }
}
