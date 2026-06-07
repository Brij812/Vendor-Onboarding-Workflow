package com.zamp.vendoronboarding.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import com.zamp.vendoronboarding.service.ExistingVendorLookupService;
import com.zamp.vendoronboarding.util.NameMatcher;
import com.zamp.vendoronboarding.workflow.AgentResult;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import com.zamp.vendoronboarding.workflow.WorkflowAgent;
import com.zamp.vendoronboarding.workflow.WorkflowContext;
import com.zamp.vendoronboarding.workflow.WorkflowStepDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class DuplicateRiskAgent implements WorkflowAgent {

    private static final String SOURCE_STEP = "DUPLICATE_RISK_AGENT";
    private static final String EVIDENCE_EXISTING_VENDOR = "existing_vendor_registry";
    private static final double MATCH_CONFIDENCE = 0.98;

    private final ExistingVendorLookupService existingVendorLookupService;
    private final ObjectMapper objectMapper;

    public DuplicateRiskAgent(ExistingVendorLookupService existingVendorLookupService,
                              ObjectMapper objectMapper) {
        this.existingVendorLookupService = existingVendorLookupService;
        this.objectMapper = objectMapper;
    }

    @Override
    public WorkflowStepDefinition getStep() {
        return WorkflowStepDefinition.DUPLICATE_RISK_AGENT;
    }

    @Override
    public AgentResult execute(WorkflowContext context) {
        VendorSubmission submission = context.getVendorSubmission();
        List<IssueDraft> issues = new ArrayList<>();
        List<Map<String, String>> matchedVendors = new ArrayList<>();
        Set<String> matchedVendorKeys = new HashSet<>();

        List<ExistingVendor> existingVendors = existingVendorLookupService.findAll();
        boolean blockedVendorMatchFound = checkBlockedVendorMatch(context, submission, issues, matchedVendors, matchedVendorKeys);
        boolean duplicateTaxIdFound = checkDuplicateTaxId(submission, issues, matchedVendors, matchedVendorKeys);
        boolean duplicateBankAccountLast4Found = checkDuplicateBankAccountLast4(submission, issues, matchedVendors, matchedVendorKeys);
        boolean similarVendorNameFound = checkSimilarVendorName(context, submission, issues, matchedVendors, matchedVendorKeys);

        context.addIssues(issues);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("checkedExistingVendorCount", existingVendors.size());
        snapshot.put("duplicateTaxIdFound", duplicateTaxIdFound);
        snapshot.put("duplicateBankAccountLast4Found", duplicateBankAccountLast4Found);
        snapshot.put("blockedVendorMatchFound", blockedVendorMatchFound);
        snapshot.put("similarVendorNameFound", similarVendorNameFound);
        snapshot.put("matchedVendors", matchedVendors);
        snapshot.put("issueCount", issues.size());

        String outputSnapshot = toJson(snapshot);
        if (issues.isEmpty()) {
            return AgentResult.completed(
                    getStep().getStepName(),
                    "No duplicate or blocked vendor risks found.",
                    outputSnapshot
            );
        }

        return AgentResult.warning(
                getStep().getStepName(),
                "Found " + issues.size() + " duplicate/risk issue(s).",
                outputSnapshot,
                issues
        );
    }

    private boolean checkBlockedVendorMatch(WorkflowContext context, VendorSubmission submission,
                                              List<IssueDraft> issues, List<Map<String, String>> matchedVendors,
                                              Set<String> matchedVendorKeys) {
        if (submission == null) {
            return false;
        }

        boolean found = false;
        Optional<ExistingVendor> taxMatch = existingVendorLookupService.findByTaxId(submission.getTaxId());
        if (taxMatch.isPresent() && taxMatch.get().getStatus() == VendorStatus.BLOCKED) {
            ExistingVendor vendor = taxMatch.get();
            found = true;
            addMatchedVendor(matchedVendors, matchedVendorKeys, vendor);
            issues.add(duplicateIssue(
                    "BLOCKED_VENDOR_MATCH",
                    IssueSeverity.CRITICAL,
                    "Vendor matches a previously blocked vendor record.",
                    "Do not onboard this vendor without procurement/compliance escalation.",
                    "taxId",
                    "No blocked vendor match",
                    vendor.getLegalName(),
                    vendor
            ));
        }

        String normalizedSubmissionName = context.getNormalizedLegalName();
        for (ExistingVendor blockedVendor : existingVendorLookupService.findBlockedVendors()) {
            if (taxMatch.isPresent() && blockedVendor.getId().equals(taxMatch.get().getId())) {
                continue;
            }
            if (NameMatcher.isSimilarNormalizedName(normalizedSubmissionName, blockedVendor.getNormalizedLegalName())) {
                found = true;
                addMatchedVendor(matchedVendors, matchedVendorKeys, blockedVendor);
                issues.add(duplicateIssue(
                        "BLOCKED_VENDOR_NAME_MATCH",
                        IssueSeverity.CRITICAL,
                        "Vendor matches a previously blocked vendor record.",
                        "Do not onboard this vendor without procurement/compliance escalation.",
                        "legalName",
                        "No blocked vendor match",
                        blockedVendor.getLegalName(),
                        blockedVendor
                ));
            }
        }

        return found;
    }

    private boolean checkDuplicateTaxId(VendorSubmission submission, List<IssueDraft> issues,
                                        List<Map<String, String>> matchedVendors, Set<String> matchedVendorKeys) {
        if (submission == null || submission.getTaxId() == null || submission.getTaxId().isBlank()) {
            return false;
        }

        Optional<ExistingVendor> match = existingVendorLookupService.findByTaxId(submission.getTaxId());
        if (match.isEmpty() || match.get().getStatus() != VendorStatus.ACTIVE) {
            return false;
        }

        ExistingVendor vendor = match.get();
        addMatchedVendor(matchedVendors, matchedVendorKeys, vendor);
        issues.add(duplicateIssue(
                "DUPLICATE_TAX_ID",
                IssueSeverity.CRITICAL,
                "Tax ID already exists for an active vendor.",
                "Do not onboard as a new vendor. Route to procurement admin to review the existing vendor record.",
                "taxId",
                "Unique tax ID",
                submission.getTaxId(),
                vendor
        ));
        return true;
    }

    private boolean checkDuplicateBankAccountLast4(VendorSubmission submission, List<IssueDraft> issues,
                                                   List<Map<String, String>> matchedVendors,
                                                   Set<String> matchedVendorKeys) {
        if (submission == null || submission.getBankAccountLast4() == null || submission.getBankAccountLast4().isBlank()) {
            return false;
        }

        List<ExistingVendor> matches = existingVendorLookupService.findByBankAccountLast4(submission.getBankAccountLast4());
        if (matches.isEmpty()) {
            return false;
        }

        boolean found = false;
        for (ExistingVendor vendor : matches) {
            if (vendor.getStatus() != VendorStatus.ACTIVE && vendor.getStatus() != VendorStatus.BLOCKED) {
                continue;
            }

            found = true;
            addMatchedVendor(matchedVendors, matchedVendorKeys, vendor);
            IssueSeverity severity = vendor.getStatus() == VendorStatus.BLOCKED
                    ? IssueSeverity.CRITICAL
                    : IssueSeverity.HIGH;
            issues.add(duplicateIssue(
                    "DUPLICATE_BANK_ACCOUNT_LAST4",
                    severity,
                    "Bank account last 4 digits match an existing vendor record.",
                    "Review bank ownership before approval.",
                    "bankAccountLast4",
                    "Unique bank account last 4",
                    submission.getBankAccountLast4(),
                    vendor
            ));
        }

        return found;
    }

    private boolean checkSimilarVendorName(WorkflowContext context, VendorSubmission submission,
                                             List<IssueDraft> issues, List<Map<String, String>> matchedVendors,
                                             Set<String> matchedVendorKeys) {
        if (submission == null) {
            return false;
        }

        String normalizedSubmissionName = context.getNormalizedLegalName();
        String submissionTaxId = submission.getTaxId();
        boolean found = false;

        for (ExistingVendor vendor : existingVendorLookupService.findActiveVendors()) {
            if (submissionTaxId != null && submissionTaxId.equals(vendor.getTaxId())) {
                continue;
            }
            if (matchedVendorKeys.contains(vendorKey(vendor))) {
                continue;
            }
            if (!NameMatcher.isSimilarNormalizedName(normalizedSubmissionName, vendor.getNormalizedLegalName())) {
                continue;
            }

            found = true;
            addMatchedVendor(matchedVendors, matchedVendorKeys, vendor);
            issues.add(duplicateIssue(
                    "SIMILAR_VENDOR_NAME",
                    IssueSeverity.MEDIUM,
                    "Vendor name is similar to an existing active vendor but tax ID differs.",
                    "Review whether this is a new legal entity or a duplicate vendor setup.",
                    "legalName",
                    "No similar existing vendor",
                    vendor.getLegalName(),
                    vendor
            ));
        }

        return found;
    }

    private void addMatchedVendor(List<Map<String, String>> matchedVendors, Set<String> matchedVendorKeys,
                                  ExistingVendor vendor) {
        String key = vendorKey(vendor);
        if (!matchedVendorKeys.add(key)) {
            return;
        }

        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("legalName", vendor.getLegalName());
        summary.put("taxId", vendor.getTaxId());
        summary.put("status", vendor.getStatus().name());
        matchedVendors.add(summary);
    }

    private String vendorKey(ExistingVendor vendor) {
        return vendor.getId().toString();
    }

    private IssueDraft duplicateIssue(String code, IssueSeverity severity, String message,
                                      String recommendedAction, String fieldName,
                                      String expectedValue, String actualValue, ExistingVendor vendor) {
        return IssueDraft.withEvidence(
                SOURCE_STEP,
                code,
                severity,
                message,
                recommendedAction,
                fieldName,
                expectedValue,
                actualValue,
                MATCH_CONFIDENCE,
                EVIDENCE_EXISTING_VENDOR,
                vendorEvidenceText(vendor)
        );
    }

    private String vendorEvidenceText(ExistingVendor vendor) {
        return String.format("Matched existing vendor: %s (tax ID: %s, status: %s)",
                vendor.getLegalName(), vendor.getTaxId(), vendor.getStatus().name());
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize duplicate risk snapshot", ex);
        }
    }
}
