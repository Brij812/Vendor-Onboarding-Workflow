package com.zamp.vendoronboarding.communication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zamp.vendoronboarding.ai.LlmClient;
import com.zamp.vendoronboarding.ai.LlmCompletionResult;
import com.zamp.vendoronboarding.ai.LlmRequest;
import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunicationGenerationService {

    private static final String SYSTEM_PROMPT = """
            You draft vendor-facing onboarding emails for procurement teams.
            Return only valid JSON with no markdown fences or commentary.
            Do not invent facts that are not provided in the prompt.
            Do not change the decision status.
            Do not say the submission is approved unless decision status is APPROVED.
            Do not mention or include internal risk scores in the subject or body.
            Keep the tone professional and concise.
            JSON format: { "subject": "...", "body": "..." }
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final CommunicationTemplateEngine templateEngine;

    public CommunicationGenerationService(LlmClient llmClient,
                                            ObjectMapper objectMapper,
                                            CommunicationTemplateEngine templateEngine) {
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
        this.templateEngine = templateEngine;
    }

    public CommunicationGenerationResult generate(VendorSubmission submission,
                                                  DecisionEvaluation decision,
                                                  List<IssueDraft> issues) {
        String userPrompt = buildUserPrompt(submission, decision, issues);
        LlmCompletionResult completion = llmClient.complete(
                new LlmRequest(SYSTEM_PROMPT, userPrompt, "vendor_communication_generation")
        );

        if (!completion.success()) {
            return fallbackFromTemplate(submission, decision, issues, completion.errorMessage());
        }

        CommunicationDraft parsed = parseResponse(completion.content(), decision.status());
        if (parsed == null) {
            return fallbackFromTemplate(submission, decision, issues, "Failed to parse LLM communication JSON.");
        }

        return CommunicationGenerationResult.success(
                CommunicationDraft.fromLlm(
                        resolveCommunicationType(decision.status()),
                        parsed.subject(),
                        parsed.body(),
                        completion.content()
                )
        );
    }

    CommunicationDraft parseResponse(String rawResponse, DecisionStatus decisionStatus) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }

        try {
            String json = stripMarkdownFences(rawResponse.trim());
            JsonNode root = objectMapper.readTree(json);
            String subject = nullableText(root.get("subject"));
            String body = nullableText(root.get("body"));

            if (subject == null || body == null) {
                return null;
            }

            if (decisionStatus != DecisionStatus.APPROVED && containsApprovedLanguage(subject, body)) {
                return null;
            }

            return new CommunicationDraft(
                    resolveCommunicationType(decisionStatus),
                    subject,
                    body,
                    null,
                    null
            );
        } catch (Exception ex) {
            return null;
        }
    }

    String buildUserPrompt(VendorSubmission submission,
                           DecisionEvaluation decision,
                           List<IssueDraft> issues) {
        String vendorName = submission != null && submission.getLegalName() != null
                ? submission.getLegalName().trim()
                : "Unknown Vendor";

        StringBuilder prompt = new StringBuilder();
        prompt.append("Draft a vendor email using only the facts below.\n");
        prompt.append("Do not mention or include internal risk scores in the email.\n\n");
        prompt.append("Vendor name: ").append(vendorName).append('\n');
        prompt.append("Decision status: ").append(decision.status().name()).append('\n');
        prompt.append("Reason summary: ").append(sanitizeReasonSummary(decision.reasonSummary())).append('\n');

        prompt.append("\nIssues:\n");
        if (issues == null || issues.isEmpty()) {
            prompt.append("- None\n");
        } else {
            for (IssueDraft issue : issues) {
                prompt.append("- [").append(issue.code()).append("] ")
                        .append(issue.message()).append(" | Action: ")
                        .append(issue.recommendedAction()).append('\n');
            }
        }

        prompt.append("\nRequired actions:\n");
        List<String> requiredActions = decision.requiredActions();
        if (requiredActions == null || requiredActions.isEmpty()) {
            prompt.append("- None\n");
        } else {
            for (String action : requiredActions) {
                prompt.append("- ").append(action).append('\n');
            }
        }

        return prompt.toString();
    }

    private CommunicationGenerationResult fallbackFromTemplate(VendorSubmission submission,
                                                             DecisionEvaluation decision,
                                                             List<IssueDraft> issues,
                                                             String errorMessage) {
        CommunicationDraft templateDraft = templateEngine.generate(submission, decision, issues);
        CommunicationDraft fallbackDraft = CommunicationDraft.fromFallback(
                templateDraft.communicationType(),
                templateDraft.subject(),
                templateDraft.body(),
                errorMessage
        );
        return CommunicationGenerationResult.fallback(fallbackDraft, errorMessage);
    }

    private CommunicationType resolveCommunicationType(DecisionStatus status) {
        return status == DecisionStatus.APPROVED
                ? CommunicationType.APPROVAL_NOTE
                : CommunicationType.VENDOR_FOLLOW_UP;
    }

    private boolean containsApprovedLanguage(String subject, String body) {
        return subject.toLowerCase().contains("approved") || body.toLowerCase().contains("approved");
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

    private String nullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String sanitizeReasonSummary(String reasonSummary) {
        if (reasonSummary == null || reasonSummary.isBlank()) {
            return "None";
        }
        return reasonSummary
                .replaceAll("(?i)\\s*with risk score \\d+\\.?", "")
                .replaceAll("(?i)\\brisk score:?\\s*\\d+\\.?", "")
                .trim();
    }
}
