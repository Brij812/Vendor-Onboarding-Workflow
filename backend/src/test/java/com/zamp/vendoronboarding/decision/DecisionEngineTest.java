package com.zamp.vendoronboarding.decision;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionEngineTest {

    private DecisionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DecisionEngine();
    }

    @Test
    void evaluate_noIssues_returnsApprovedWithZeroScore() {
        DecisionEvaluation result = engine.evaluate(List.of());

        assertEquals(DecisionStatus.APPROVED, result.status());
        assertEquals(0, result.riskScore());
        assertEquals(0, result.issueCount());
        assertTrue(result.requiredActions().contains("Proceed to ERP vendor creation."));
    }

    @Test
    void evaluate_publicEmailDomainOnly_returnsApprovedWithWarning() {
        List<IssueDraft> issues = List.of(issue("PUBLIC_EMAIL_DOMAIN", IssueSeverity.LOW));

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.APPROVED, result.status());
        assertEquals(10, result.riskScore());
        assertTrue(result.reasonSummary().contains("low-risk warning"));
        assertTrue(result.triggeredRules().contains("LOW_RISK_ONLY"));
        assertTrue(result.triggeredRules().contains("PUBLIC_EMAIL_DOMAIN"));
    }

    @Test
    void evaluate_bankNameMismatch_returnsPending() {
        List<IssueDraft> issues = List.of(issue("BANK_NAME_MISMATCH", IssueSeverity.MEDIUM));

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.PENDING, result.status());
        assertEquals(25, result.riskScore());
        assertTrue(result.triggeredRules().contains("HIGH_OR_MEDIUM_ISSUE_PRESENT"));
        assertTrue(result.triggeredRules().contains("BANK_NAME_MISMATCH"));
        assertTrue(result.requiredActions().contains("Procurement manual review required."));
    }

    @Test
    void evaluate_invalidGstinFormat_returnsPending() {
        List<IssueDraft> issues = List.of(issue("INVALID_GSTIN_FORMAT", IssueSeverity.HIGH));

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.PENDING, result.status());
        assertEquals(40, result.riskScore());
        assertTrue(result.triggeredRules().contains("INVALID_GSTIN_FORMAT"));
    }

    @Test
    void evaluate_duplicateTaxId_returnsRejected() {
        List<IssueDraft> issues = List.of(issue("DUPLICATE_TAX_ID", IssueSeverity.CRITICAL));

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.REJECTED, result.status());
        assertEquals(80, result.riskScore());
        assertTrue(result.triggeredRules().contains("CRITICAL_ISSUE_PRESENT"));
        assertTrue(result.triggeredRules().contains("DUPLICATE_TAX_ID"));
    }

    @Test
    void evaluate_blockedVendorMatch_returnsRejected() {
        List<IssueDraft> issues = List.of(issue("BLOCKED_VENDOR_MATCH", IssueSeverity.CRITICAL));

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.REJECTED, result.status());
        assertTrue(result.triggeredRules().contains("BLOCKED_VENDOR_MATCH"));
    }

    @Test
    void evaluate_multipleIssues_capsRiskScoreAt100() {
        List<IssueDraft> issues = List.of(
                issue("DUPLICATE_TAX_ID", IssueSeverity.CRITICAL),
                issue("BLOCKED_VENDOR_MATCH", IssueSeverity.CRITICAL)
        );

        DecisionEvaluation result = engine.evaluate(issues);

        assertEquals(DecisionStatus.REJECTED, result.status());
        assertEquals(100, result.riskScore());
        assertEquals(2, result.criticalIssueCount());
    }

    private IssueDraft issue(String code, IssueSeverity severity) {
        return IssueDraft.of(
                "TEST_AGENT",
                code,
                severity,
                "Test issue message for " + code,
                "Recommended action for " + code,
                "testField"
        );
    }
}
