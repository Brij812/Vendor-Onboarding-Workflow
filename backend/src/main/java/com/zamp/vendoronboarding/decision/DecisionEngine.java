package com.zamp.vendoronboarding.decision;

import com.zamp.vendoronboarding.entity.enums.DecisionStatus;
import com.zamp.vendoronboarding.entity.enums.IssueSeverity;
import com.zamp.vendoronboarding.workflow.IssueDraft;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DecisionEngine {

    private static final int MAX_RISK_SCORE = 100;

    public DecisionEvaluation evaluate(List<IssueDraft> issues) {
        List<IssueDraft> safeIssues = issues != null ? issues : List.of();

        int criticalCount = countBySeverity(safeIssues, IssueSeverity.CRITICAL);
        int highCount = countBySeverity(safeIssues, IssueSeverity.HIGH);
        int mediumCount = countBySeverity(safeIssues, IssueSeverity.MEDIUM);
        int lowCount = countBySeverity(safeIssues, IssueSeverity.LOW);

        int riskScore = calculateRiskScore(safeIssues);
        DecisionStatus status = determineStatus(criticalCount, highCount, mediumCount);
        List<String> triggeredRules = buildTriggeredRules(safeIssues, status, criticalCount, highCount, mediumCount, lowCount);
        List<String> requiredActions = buildRequiredActions(safeIssues, status);
        String reasonSummary = buildReasonSummary(status, riskScore, safeIssues, lowCount);

        return new DecisionEvaluation(
                status,
                riskScore,
                reasonSummary,
                requiredActions,
                triggeredRules,
                safeIssues.size(),
                criticalCount,
                highCount,
                mediumCount,
                lowCount
        );
    }

    private int calculateRiskScore(List<IssueDraft> issues) {
        int score = 0;
        for (IssueDraft issue : issues) {
            score += severityPoints(issue.severity());
        }
        return Math.min(score, MAX_RISK_SCORE);
    }

    private int severityPoints(IssueSeverity severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case LOW -> 10;
            case MEDIUM -> 25;
            case HIGH -> 40;
            case CRITICAL -> 80;
        };
    }

    private DecisionStatus determineStatus(int criticalCount, int highCount, int mediumCount) {
        if (criticalCount > 0) {
            return DecisionStatus.REJECTED;
        }
        if (highCount > 0 || mediumCount > 0) {
            return DecisionStatus.PENDING;
        }
        return DecisionStatus.APPROVED;
    }

    private List<String> buildTriggeredRules(List<IssueDraft> issues,
                                             DecisionStatus status,
                                             int criticalCount,
                                             int highCount,
                                             int mediumCount,
                                             int lowCount) {
        Set<String> rules = new LinkedHashSet<>();

        if (criticalCount > 0) {
            rules.add("CRITICAL_ISSUE_PRESENT");
        } else if (highCount > 0 || mediumCount > 0) {
            rules.add("HIGH_OR_MEDIUM_ISSUE_PRESENT");
        } else if (lowCount > 0) {
            rules.add("LOW_RISK_ONLY");
        }

        for (IssueDraft issue : issues) {
            String code = issue.code();
            if (code == null) {
                continue;
            }
            if ("BLOCKED_VENDOR_MATCH".equals(code) || "BLOCKED_VENDOR_NAME_MATCH".equals(code)) {
                rules.add("BLOCKED_VENDOR_MATCH");
            } else if ("DUPLICATE_TAX_ID".equals(code)) {
                rules.add("DUPLICATE_TAX_ID");
            } else if ("INVALID_GSTIN_FORMAT".equals(code)) {
                rules.add("INVALID_GSTIN_FORMAT");
            } else if ("BANK_NAME_MISMATCH".equals(code)) {
                rules.add("BANK_NAME_MISMATCH");
            } else if (code.startsWith("MISSING_")) {
                rules.add("MISSING_REQUIRED_FIELD");
            } else if ("PUBLIC_EMAIL_DOMAIN".equals(code)) {
                rules.add("PUBLIC_EMAIL_DOMAIN");
            }
        }

        return List.copyOf(rules);
    }

    private List<String> buildRequiredActions(List<IssueDraft> issues, DecisionStatus status) {
        Set<String> actions = new LinkedHashSet<>();

        if (status != DecisionStatus.APPROVED) {
            for (IssueDraft issue : issues) {
                if (issue.recommendedAction() != null && !issue.recommendedAction().isBlank()) {
                    actions.add(issue.recommendedAction().trim());
                }
            }
        }

        switch (status) {
            case REJECTED -> actions.add("Do not onboard; contact vendor regarding block or duplicate status.");
            case PENDING -> actions.add("Procurement manual review required.");
            case APPROVED -> {
                actions.add("Proceed to ERP vendor creation.");
                if (hasCode(issues, "PUBLIC_EMAIL_DOMAIN")) {
                    actions.add("Consider requesting a corporate email domain from the vendor.");
                }
            }
        }

        return List.copyOf(actions);
    }

    private String buildReasonSummary(DecisionStatus status,
                                      int riskScore,
                                      List<IssueDraft> issues,
                                      int lowCount) {
        StringBuilder summary = new StringBuilder()
                .append("Final decision generated: ")
                .append(status.name())
                .append(" with risk score ")
                .append(riskScore)
                .append(".");

        if (status == DecisionStatus.APPROVED && lowCount > 0 && hasCode(issues, "PUBLIC_EMAIL_DOMAIN")) {
            summary.append(" Approved with low-risk warning: public email domain detected.");
        }

        return summary.toString();
    }

    private boolean hasCode(List<IssueDraft> issues, String code) {
        return issues.stream().anyMatch(issue -> code.equals(issue.code()));
    }

    private int countBySeverity(List<IssueDraft> issues, IssueSeverity severity) {
        int count = 0;
        for (IssueDraft issue : issues) {
            if (severity == issue.severity()) {
                count++;
            }
        }
        return count;
    }
}
