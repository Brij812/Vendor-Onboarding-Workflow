package com.zamp.vendoronboarding.communication;

import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunicationTemplateEngineTest {

    private CommunicationTemplateEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CommunicationTemplateEngine();
    }

    @Test
    void generate_approvedDecision_returnsApprovalNote() {
        VendorSubmission submission = vendor("Acme Cloud Services Pvt Ltd");
        DecisionEvaluation decision = decision(DecisionStatus.APPROVED, 0, List.of("Proceed to ERP vendor creation."));

        CommunicationDraft draft = engine.generate(submission, decision, List.of());

        assertEquals(CommunicationType.APPROVAL_NOTE, draft.communicationType());
        assertEquals("Vendor onboarding submission approved", draft.subject());
        assertTrue(draft.body().contains("approved for procurement review"));
        assertTrue(draft.body().contains("Dear Acme Cloud Services Pvt Ltd team,"));
    }

    @Test
    void generate_pendingDecision_listsIssuesAndRequiredActions() {
        VendorSubmission submission = vendor("Acme Cloud Services Pvt Ltd");
        DecisionEvaluation decision = decision(
                DecisionStatus.PENDING,
                65,
                List.of("Confirm bank account ownership.", "Procurement manual review required.")
        );
        List<IssueDraft> issues = List.of(
                IssueDraft.of("CONSISTENCY_CHECK_AGENT", "BANK_NAME_MISMATCH", IssueSeverity.MEDIUM,
                        "Bank account holder name does not match legal name.",
                        "Confirm bank account ownership.", "bankAccountHolderName")
        );

        CommunicationDraft draft = engine.generate(submission, decision, issues);

        assertEquals(CommunicationType.VENDOR_FOLLOW_UP, draft.communicationType());
        assertEquals("Action required for vendor onboarding submission", draft.subject());
        assertTrue(draft.body().contains("pending approval"));
        assertTrue(draft.body().contains("Bank account holder name does not match legal name."));
        assertTrue(draft.body().contains("Confirm bank account ownership."));
        assertTrue(draft.body().contains("Procurement manual review required."));
    }

    @Test
    void generate_rejectedDecision_returnsRejectionMessage() {
        VendorSubmission submission = vendor("Blackstone Imports");
        DecisionEvaluation decision = decision(
                DecisionStatus.REJECTED,
                80,
                List.of("Do not onboard; contact vendor regarding block or duplicate status.")
        );
        List<IssueDraft> issues = List.of(
                IssueDraft.of("DUPLICATE_RISK_AGENT", "DUPLICATE_TAX_ID", IssueSeverity.CRITICAL,
                        "Tax ID matches an existing vendor record.",
                        "Do not onboard duplicate vendor.", "taxId")
        );

        CommunicationDraft draft = engine.generate(submission, decision, issues);

        assertEquals(CommunicationType.VENDOR_FOLLOW_UP, draft.communicationType());
        assertEquals("Vendor onboarding submission could not be approved", draft.subject());
        assertTrue(draft.body().contains("could not be approved"));
        assertTrue(draft.body().contains("Tax ID matches an existing vendor record."));
        assertTrue(draft.body().contains("contact our procurement team"));
    }

    private VendorSubmission vendor(String legalName) {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName(legalName);
        return submission;
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
