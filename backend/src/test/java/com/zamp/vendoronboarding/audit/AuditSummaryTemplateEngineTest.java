package com.zamp.vendoronboarding.audit;

import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditSummaryTemplateEngineTest {

    private AuditSummaryTemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AuditSummaryTemplateEngine();
    }

    @Test
    void generate_approvedDecision_mentionsNoManualAction() {
        VendorSubmission submission = vendor("Acme Cloud Services Pvt Ltd");
        DecisionEvaluation decision = decision(DecisionStatus.APPROVED, 0);

        AuditSummaryDraft draft = engine.generate(submission, decision, List.of());

        assertTrue(draft.summary().contains("Vendor Acme Cloud Services Pvt Ltd is marked APPROVED"));
        assertTrue(draft.summary().contains("risk score 0"));
        assertTrue(draft.summary().contains("no manual procurement action is required"));
    }

    @Test
    void generate_pendingDecision_mentionsIssueAndAction() {
        VendorSubmission submission = vendor("Acme Cloud Services Pvt Ltd");
        DecisionEvaluation decision = decision(
                DecisionStatus.PENDING,
                65,
                List.of("Request corrected bank proof before approval.")
        );
        List<IssueDraft> issues = List.of(
                IssueDraft.of("CONSISTENCY_CHECK_AGENT", "BANK_NAME_MISMATCH", IssueSeverity.MEDIUM,
                        "Bank account holder name does not match legal name.",
                        "Request corrected bank proof before approval.", "bankAccountHolderName")
        );

        AuditSummaryDraft draft = engine.generate(submission, decision, issues);

        assertTrue(draft.summary().contains("marked PENDING with risk score 65"));
        assertTrue(draft.summary().contains("Bank account holder name does not match legal name."));
        assertTrue(draft.summary().contains("request corrected bank proof before approval"));
    }

    @Test
    void generate_rejectedDecision_mentionsEscalation() {
        VendorSubmission submission = vendor("Blackstone Imports");
        DecisionEvaluation decision = decision(DecisionStatus.REJECTED, 80);
        List<IssueDraft> issues = List.of(
                IssueDraft.of("DUPLICATE_RISK_AGENT", "BLOCKED_VENDOR_MATCH", IssueSeverity.CRITICAL,
                        "Vendor matches a blocked record.", "Do not onboard.", "taxId")
        );

        AuditSummaryDraft draft = engine.generate(submission, decision, issues);

        assertTrue(draft.summary().contains("marked REJECTED with risk score 80"));
        assertTrue(draft.summary().contains("Vendor matches a blocked record."));
        assertTrue(draft.summary().contains("Escalate to procurement leadership"));
    }

    private VendorSubmission vendor(String legalName) {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName(legalName);
        return submission;
    }

    private DecisionEvaluation decision(DecisionStatus status, int riskScore) {
        return decision(status, riskScore, List.of("Procurement manual review required."));
    }

    private DecisionEvaluation decision(DecisionStatus status, int riskScore, List<String> requiredActions) {
        return new DecisionEvaluation(
                status,
                riskScore,
                "Final decision generated: " + status.name() + " with risk score " + riskScore + ".",
                requiredActions,
                List.of(),
                0,
                0,
                0,
                0,
                0
        );
    }
}
