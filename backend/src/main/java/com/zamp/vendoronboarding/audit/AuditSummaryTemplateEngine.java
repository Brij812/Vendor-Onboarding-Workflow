package com.zamp.vendoronboarding.audit;

import com.zamp.vendoronboarding.decision.DecisionEvaluation;
import com.zamp.vendoronboarding.entity.VendorSubmission;
import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.GenerationMethod;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditSummaryTemplateEngine {

    public AuditSummaryDraft generate(VendorSubmission submission,
                                      DecisionEvaluation decision,
                                      List<IssueDraft> issues) {
        if (decision == null) {
            throw new IllegalStateException("Decision is required to generate audit summary.");
        }

        String vendorName = resolveVendorName(submission);
        String summary = switch (decision.status()) {
            case APPROVED -> String.format(
                    "Vendor %s is marked APPROVED with risk score %d. "
                            + "All onboarding checks passed and no manual procurement action is required.",
                    vendorName,
                    decision.riskScore()
            );
            case PENDING -> String.format(
                    "Vendor %s is marked PENDING with risk score %d. The main issue is %s. "
                            + "Procurement should %s before approval.",
                    vendorName,
                    decision.riskScore(),
                    selectPrimaryIssueMessage(issues),
                    selectPrimaryAction(decision)
            );
            case REJECTED -> String.format(
                    "Vendor %s is marked REJECTED with risk score %d. Critical issue: %s. "
                            + "Escalate to procurement leadership and do not proceed with onboarding.",
                    vendorName,
                    decision.riskScore(),
                    selectCriticalIssueMessage(issues, decision)
            );
        };

        return new AuditSummaryDraft(summary, GenerationMethod.TEMPLATE, null);
    }

    private String resolveVendorName(VendorSubmission submission) {
        if (submission != null && submission.getLegalName() != null && !submission.getLegalName().isBlank()) {
            return submission.getLegalName().trim();
        }
        return "Unknown Vendor";
    }

    private String selectPrimaryIssueMessage(List<IssueDraft> issues) {
        if (issues == null || issues.isEmpty()) {
            return "unresolved onboarding issues";
        }

        for (IssueSeverity severity : List.of(IssueSeverity.CRITICAL, IssueSeverity.HIGH, IssueSeverity.MEDIUM)) {
            for (IssueDraft issue : issues) {
                if (issue.severity() == severity && issue.message() != null && !issue.message().isBlank()) {
                    return issue.message().trim();
                }
            }
        }

        for (IssueDraft issue : issues) {
            if (issue.message() != null && !issue.message().isBlank()) {
                return issue.message().trim();
            }
        }

        return "unresolved onboarding issues";
    }

    private String selectCriticalIssueMessage(List<IssueDraft> issues, DecisionEvaluation decision) {
        if (issues != null) {
            for (IssueDraft issue : issues) {
                if (issue.severity() == IssueSeverity.CRITICAL
                        && issue.message() != null
                        && !issue.message().isBlank()) {
                    return issue.message().trim();
                }
            }
        }
        return selectPrimaryIssueMessage(issues);
    }

    private String selectPrimaryAction(DecisionEvaluation decision) {
        List<String> actions = decision.requiredActions();
        if (actions != null) {
            for (String action : actions) {
                if (action != null && !action.isBlank()) {
                    return action.trim().toLowerCase();
                }
            }
        }
        return "complete manual review";
    }
}
