package com.zamp.vendoronboarding.communication;

import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.CommunicationType;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CommunicationTemplateEngine {

    private static final int MAX_ISSUE_MESSAGES = 5;

    public CommunicationDraft generate(VendorSubmission submission,
                                         DecisionEvaluation decision,
                                         List<IssueDraft> issues) {
        if (decision == null) {
            throw new IllegalStateException("Decision is required to generate vendor communication.");
        }

        String greeting = buildGreeting(submission);
        return switch (decision.status()) {
            case APPROVED -> new CommunicationDraft(
                    CommunicationType.APPROVAL_NOTE,
                    "Vendor onboarding submission approved",
                    buildApprovedBody(greeting),
                    GenerationMethod.TEMPLATE,
                    null
            );
            case PENDING -> new CommunicationDraft(
                    CommunicationType.VENDOR_FOLLOW_UP,
                    "Action required for vendor onboarding submission",
                    buildPendingBody(greeting, decision, issues),
                    GenerationMethod.TEMPLATE,
                    null
            );
            case REJECTED -> new CommunicationDraft(
                    CommunicationType.VENDOR_FOLLOW_UP,
                    "Vendor onboarding submission could not be approved",
                    buildRejectedBody(greeting, decision, issues),
                    GenerationMethod.TEMPLATE,
                    null
            );
        };
    }

    private String buildGreeting(VendorSubmission submission) {
        if (submission != null && submission.getLegalName() != null && !submission.getLegalName().isBlank()) {
            return "Dear " + submission.getLegalName().trim() + " team,";
        }
        return "Dear vendor,";
    }

    private String buildApprovedBody(String greeting) {
        return greeting + "\n\n"
                + "Thank you for submitting your vendor onboarding details. "
                + "Your submission has passed the onboarding checks and is approved for procurement review.\n\n"
                + "Best regards,\n"
                + "Procurement Team";
    }

    private String buildPendingBody(String greeting, DecisionEvaluation decision, List<IssueDraft> issues) {
        StringBuilder body = new StringBuilder();
        body.append(greeting).append("\n\n");
        body.append("Thank you for submitting your vendor onboarding details. ");
        body.append("Your submission is currently pending approval and requires additional review.\n\n");

        List<String> issueMessages = collectIssueMessages(issues);
        if (!issueMessages.isEmpty()) {
            body.append("Main reasons for review:\n");
            for (String message : issueMessages) {
                body.append("- ").append(message).append("\n");
            }
            body.append("\n");
        }

        List<String> requiredActions = decision.requiredActions();
        if (requiredActions != null && !requiredActions.isEmpty()) {
            body.append("Required actions:\n");
            for (String action : requiredActions) {
                body.append("- ").append(action).append("\n");
            }
            body.append("\n");
        }

        body.append("Please review the items above and respond with any corrections or supporting documentation.\n\n");
        body.append("Best regards,\n");
        body.append("Procurement Team");
        return body.toString();
    }

    private String buildRejectedBody(String greeting, DecisionEvaluation decision, List<IssueDraft> issues) {
        StringBuilder body = new StringBuilder();
        body.append(greeting).append("\n\n");
        body.append("Thank you for submitting your vendor onboarding details. ");
        body.append("Unfortunately, your submission could not be approved at this time.\n\n");

        List<String> reasons = collectRejectionReasons(decision, issues);
        if (!reasons.isEmpty()) {
            body.append("Main reason(s):\n");
            for (String reason : reasons) {
                body.append("- ").append(reason).append("\n");
            }
            body.append("\n");
        }

        body.append("If you believe this outcome is incorrect, please contact our procurement team "
                + "with any supporting information for further review.\n\n");
        body.append("Best regards,\n");
        body.append("Procurement Team");
        return body.toString();
    }

    private List<String> collectIssueMessages(List<IssueDraft> issues) {
        Set<String> messages = new LinkedHashSet<>();
        if (issues == null) {
            return List.of();
        }
        for (IssueDraft issue : issues) {
            if (issue.message() != null && !issue.message().isBlank()) {
                messages.add(issue.message().trim());
                if (messages.size() >= MAX_ISSUE_MESSAGES) {
                    break;
                }
            }
        }
        return new ArrayList<>(messages);
    }

    private List<String> collectRejectionReasons(DecisionEvaluation decision, List<IssueDraft> issues) {
        Set<String> reasons = new LinkedHashSet<>();

        if (issues != null) {
            for (IssueSeverity severity : List.of(IssueSeverity.CRITICAL, IssueSeverity.HIGH, IssueSeverity.MEDIUM)) {
                for (IssueDraft issue : issues) {
                    if (issue.severity() == severity && issue.message() != null && !issue.message().isBlank()) {
                        reasons.add(issue.message().trim());
                        if (reasons.size() >= MAX_ISSUE_MESSAGES) {
                            return new ArrayList<>(reasons);
                        }
                    }
                }
            }
        }

        if (reasons.isEmpty() && decision.reasonSummary() != null && !decision.reasonSummary().isBlank()) {
            reasons.add(decision.reasonSummary().trim());
        }

        return new ArrayList<>(reasons);
    }
}
