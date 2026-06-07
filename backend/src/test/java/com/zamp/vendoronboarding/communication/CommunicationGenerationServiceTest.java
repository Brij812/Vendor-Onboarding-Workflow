package com.zamp.vendoronboarding.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunicationGenerationServiceTest {

    @Mock
    private LlmClient llmClient;

    private CommunicationGenerationService service;

    @BeforeEach
    void setUp() {
        service = new CommunicationGenerationService(
                llmClient,
                new ObjectMapper(),
                new CommunicationTemplateEngine()
        );
    }

    @Test
    void generate_validJsonResponse_returnsLlmDraft() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("""
                {
                  "subject": "Additional review required for vendor onboarding",
                  "body": "Dear Acme team,\\n\\nYour submission requires additional review."
                }
                """));

        CommunicationGenerationResult result = service.generate(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue())
        );

        assertTrue(result.success());
        assertEquals(GenerationMethod.LLM, result.draft().generationMethod());
        assertEquals(CommunicationType.VENDOR_FOLLOW_UP, result.draft().communicationType());
        assertEquals("Additional review required for vendor onboarding", result.draft().subject());
        assertTrue(result.draft().body().contains("requires additional review"));
    }

    @Test
    void generate_invalidJson_returnsFallbackTemplate() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("not json"));

        CommunicationGenerationResult result = service.generate(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue())
        );

        assertFalse(result.success());
        assertEquals(GenerationMethod.FALLBACK, result.draft().generationMethod());
        assertEquals("Action required for vendor onboarding submission", result.draft().subject());
        assertTrue(result.draft().body().contains("pending approval"));
    }

    @Test
    void generate_pendingDecisionWithApprovedLanguage_returnsFallbackTemplate() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("""
                {
                  "subject": "Your submission is approved",
                  "body": "Dear vendor, your submission is approved."
                }
                """));

        CommunicationGenerationResult result = service.generate(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue())
        );

        assertFalse(result.success());
        assertEquals(GenerationMethod.FALLBACK, result.draft().generationMethod());
    }

    @Test
    void parseResponse_stripsMarkdownFences() {
        CommunicationDraft draft = service.parseResponse("""
                ```json
                {
                  "subject": "Vendor onboarding update",
                  "body": "Please review the requested changes."
                }
                ```
                """, DecisionStatus.PENDING);

        assertEquals("Vendor onboarding update", draft.subject());
        assertEquals("Please review the requested changes.", draft.body());
    }

    private VendorSubmission vendor(String legalName) {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName(legalName);
        return submission;
    }

    private DecisionEvaluation decision(DecisionStatus status, int riskScore) {
        return new DecisionEvaluation(
                status,
                riskScore,
                "Final decision generated: " + status.name() + " with risk score " + riskScore + ".",
                List.of("Confirm bank account ownership."),
                List.of("HIGH_OR_MEDIUM_ISSUE_PRESENT"),
                1,
                0,
                0,
                1,
                0
        );
    }

    private IssueDraft issue() {
        return IssueDraft.of(
                "CONSISTENCY_CHECK_AGENT",
                "BANK_NAME_MISMATCH",
                IssueSeverity.MEDIUM,
                "Bank account holder name does not match legal name.",
                "Confirm bank account ownership.",
                "bankAccountHolderName"
        );
    }
}
