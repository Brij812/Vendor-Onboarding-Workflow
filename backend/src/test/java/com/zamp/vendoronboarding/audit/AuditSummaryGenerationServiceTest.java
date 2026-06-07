package com.zamp.vendoronboarding.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.DocumentType;
import com.zamp.vendoronboarding.entity.enums.ExtractionMethod;
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
class AuditSummaryGenerationServiceTest {

    @Mock
    private LlmClient llmClient;

    private AuditSummaryGenerationService service;

    @BeforeEach
    void setUp() {
        service = new AuditSummaryGenerationService(
                llmClient,
                new ObjectMapper(),
                new AuditSummaryTemplateEngine()
        );
    }

    @Test
    void generate_validJsonResponse_returnsLlmDraft() {
        when(llmClient.complete(any(LlmRequest.class))).thenReturn(LlmCompletionResult.success("""
                {
                  "summary": "Vendor requires manual procurement review due to bank name mismatch."
                }
                """));

        AuditSummaryGenerationResult result = service.generate(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue()),
                List.of(extraction())
        );

        assertTrue(result.success());
        assertEquals(GenerationMethod.LLM, result.draft().generationMethod());
        assertTrue(result.draft().summary().contains("manual procurement review"));
    }

    @Test
    void generate_llmFailure_returnsFallbackTemplate() {
        when(llmClient.complete(any(LlmRequest.class)))
                .thenReturn(LlmCompletionResult.failure("LLM API key is not configured."));

        AuditSummaryGenerationResult result = service.generate(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue()),
                List.of(extraction())
        );

        assertFalse(result.success());
        assertEquals(GenerationMethod.FALLBACK, result.draft().generationMethod());
        assertTrue(result.draft().summary().contains("marked PENDING with risk score 65"));
        assertEquals("LLM API key is not configured.", result.draft().rawLlmResponse());
    }

    @Test
    void buildUserPrompt_includesDocumentExtractionSummary() {
        String prompt = service.buildUserPrompt(
                vendor("Acme Cloud Services Pvt Ltd"),
                decision(DecisionStatus.PENDING, 65),
                List.of(issue()),
                List.of(extraction())
        );

        assertTrue(prompt.contains("Document extraction summary"));
        assertTrue(prompt.contains("TAX_REGISTRATION"));
        assertTrue(prompt.contains("29ABCDE1234F1Z5"));
    }

    private VendorSubmission vendor(String legalName) {
        VendorSubmission submission = new VendorSubmission();
        submission.setLegalName(legalName);
        submission.setCountry("India");
        submission.setTaxId("29ABCDE1234F1Z5");
        submission.setBusinessCategory("Software Services");
        return submission;
    }

    private DecisionEvaluation decision(DecisionStatus status, int riskScore) {
        return new DecisionEvaluation(
                status,
                riskScore,
                "Final decision generated: " + status.name() + " with risk score " + riskScore + ".",
                List.of("Request corrected bank proof before approval."),
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
                "Request corrected bank proof before approval.",
                "bankAccountHolderName"
        );
    }

    private DocumentExtraction extraction() {
        DocumentExtraction extraction = new DocumentExtraction();
        extraction.setDocumentType(DocumentType.TAX_REGISTRATION);
        extraction.setExtractionMethod(ExtractionMethod.LLM);
        extraction.setTaxId("29ABCDE1234F1Z5");
        extraction.setLegalEntityName("Acme Cloud Services Pvt Ltd");
        extraction.setConfidenceScore(0.91);
        return extraction;
    }
}
