package com.zamp.vendoronboarding.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.DocumentExtraction;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditSummaryGenerationService {

    private static final String SYSTEM_PROMPT = """
            You write internal procurement audit summaries for vendor onboarding reviews.
            Return only valid JSON with no markdown fences or commentary.
            Explain business risk clearly for a procurement reviewer.
            Do not invent facts that are not provided in the prompt.
            Do not change the decision status.
            JSON format: { "summary": "..." }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final AuditSummaryTemplateEngine templateEngine;

    public AuditSummaryGenerationService(LlmClient llmClient,
                                          ObjectMapper objectMapper,
                                          AuditSummaryTemplateEngine templateEngine) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.templateEngine = templateEngine;
    }

    public AuditSummaryGenerationResult generate(VendorSubmission submission,
                                                 DecisionEvaluation decision,
                                                 List<IssueDraft> issues,
                                                 List<DocumentExtraction> extractions) {
        String userPrompt = buildUserPrompt(submission, decision, issues, extractions);
        LlmCompletionResult completion = llmClient.complete(
                new LlmRequest(SYSTEM_PROMPT, userPrompt, "audit_summary_generation")
        );

        if (!completion.success()) {
            return fallbackFromTemplate(submission, decision, issues, completion.errorMessage());
        }

        String summary = parseSummary(completion.content());
        if (summary == null) {
            return fallbackFromTemplate(submission, decision, issues, "Failed to parse LLM audit summary JSON.");
        }

        return AuditSummaryGenerationResult.success(
                AuditSummaryDraft.fromLlm(summary, completion.content())
        );
    }

    String parseSummary(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }

        try {
            String json = stripMarkdownFences(rawResponse.trim());
            JsonNode root = objectMapper.readTree(json);
            return nullableText(root.get("summary"));
        } catch (Exception ex) {
            return null;
        }
    }

    String buildUserPrompt(VendorSubmission submission,
                           DecisionEvaluation decision,
                           List<IssueDraft> issues,
                           List<DocumentExtraction> extractions) {
        String vendorName = submission != null && submission.getLegalName() != null
                ? submission.getLegalName().trim()
                : "Unknown Vendor";

        StringBuilder prompt = new StringBuilder();
        prompt.append("Write an internal audit summary using only the facts below.\n\n");
        prompt.append("Vendor submission:\n");
        prompt.append("- Legal name: ").append(vendorName).append('\n');
        if (submission != null) {
            prompt.append("- Country: ").append(nullable(submission.getCountry())).append('\n');
            prompt.append("- Tax ID: ").append(nullable(submission.getTaxId())).append('\n');
            prompt.append("- Business category: ").append(nullable(submission.getBusinessCategory())).append('\n');
        }

        prompt.append("\nDecision:\n");
        prompt.append("- Status: ").append(decision.status().name()).append('\n');
        prompt.append("- Risk score: ").append(decision.riskScore()).append('\n');
        prompt.append("- Reason summary: ").append(decision.reasonSummary()).append('\n');
        prompt.append("- Triggered rules: ").append(formatList(decision.triggeredRules())).append('\n');
        prompt.append("- Required actions: ").append(formatList(decision.requiredActions())).append('\n');

        prompt.append("\nIssues:\n");
        if (issues == null || issues.isEmpty()) {
            prompt.append("- None\n");
        } else {
            for (IssueDraft issue : issues) {
                prompt.append("- [").append(issue.severity()).append("] ")
                        .append(issue.code()).append(": ")
                        .append(issue.message()).append('\n');
            }
        }

        prompt.append("\nDocument extraction summary:\n");
        prompt.append(buildExtractionSummary(extractions));

        return prompt.toString();
    }

    String buildExtractionSummary(List<DocumentExtraction> extractions) {
        if (extractions == null || extractions.isEmpty()) {
            return "- No uploaded documents or extractions available.\n";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("- Total extractions: ").append(extractions.size()).append('\n');
        for (DocumentExtraction extraction : extractions) {
            summary.append("- Document type: ").append(extraction.getDocumentType().name())
                    .append(" | Method: ").append(extraction.getExtractionMethod().name());
            if (extraction.getLegalEntityName() != null) {
                summary.append(" | Legal entity: ").append(extraction.getLegalEntityName());
            }
            if (extraction.getTaxId() != null) {
                summary.append(" | Tax ID: ").append(extraction.getTaxId());
            }
            if (extraction.getBankAccountHolderName() != null) {
                summary.append(" | Bank holder: ").append(extraction.getBankAccountHolderName());
            }
            if (extraction.getCountry() != null) {
                summary.append(" | Country: ").append(extraction.getCountry());
            }
            if (extraction.getConfidenceScore() != null) {
                summary.append(" | Confidence: ").append(extraction.getConfidenceScore());
            }
            summary.append('\n');
        }
        return summary.toString();
    }

    private AuditSummaryGenerationResult fallbackFromTemplate(VendorSubmission submission,
                                                              DecisionEvaluation decision,
                                                              List<IssueDraft> issues,
                                                              String errorMessage) {
        AuditSummaryDraft templateDraft = templateEngine.generate(submission, decision, issues);
        AuditSummaryDraft fallbackDraft = AuditSummaryDraft.fromFallback(templateDraft.summary(), errorMessage);
        return AuditSummaryGenerationResult.fallback(fallbackDraft, errorMessage);
    }

    String stripMarkdownFences(String value) {
        if (value.startsWith("```")) {
            int firstNewline = value.indexOf('\n');
            if (firstNewline >= 0) {
                value = value.substring(firstNewline + 1);
            }
            int closingFence = value.lastIndexOf("```");
            if (closingFence >= 0) {
                value = value.substring(0, closingFence);
            }
        }
        return value.trim();
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? "Not provided" : value.trim();
    }

    private String formatList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "None";
        }
        return String.join("; ", values);
    }

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }
}
