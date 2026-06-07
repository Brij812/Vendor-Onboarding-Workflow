import { useState } from 'react';
import type { ReactNode } from 'react';
import type { AuditSummary, Communication, Decision, Issue } from '../types/workflow';
import {
  copyToClipboard,
  formatGenerationMethod,
  formatMultilineList,
  formatRiskScore,
} from '../utils/format';
import { DOCUMENT_TYPE_LABELS } from '../utils/documentLabels';
import EmptyState from './EmptyState';
import StatusBadge from './StatusBadge';

interface RunResultSectionsProps {
  issues: Issue[];
  decision: Decision | null;
  communication: Communication | null;
  auditSummary: AuditSummary | null;
}

function SectionCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="card result-section">
      <h2 className="section-title">{title}</h2>
      {children}
    </section>
  );
}

function issueSeverityClass(severity: string | null | undefined, code?: string | null): string {
  const baseClass = (() => {
    switch (severity?.toUpperCase()) {
      case 'CRITICAL':
        return 'issue-item issue-item--critical';
      case 'HIGH':
        return 'issue-item issue-item--high';
      case 'MEDIUM':
        return 'issue-item issue-item--medium';
      case 'LOW':
        return 'issue-item issue-item--low';
      default:
        return 'issue-item';
    }
  })();

  if (code === 'WRONG_DOCUMENT_TYPE') {
    return `${baseClass} issue-item--document-type-mismatch`;
  }

  return baseClass;
}

function formatConfidence(value: number | null | undefined): string | null {
  if (value == null) {
    return null;
  }
  return `${Math.round(value * 100)}%`;
}

function formatEvidenceSource(source: string | null | undefined): string | null {
  if (!source) {
    return null;
  }
  return source.replace(/_/g, ' ');
}

function formatIssueDocumentType(value: string | null | undefined, issueCode?: string | null): string {
  if (!value) {
    return '';
  }
  if (issueCode === 'WRONG_DOCUMENT_TYPE' && value in DOCUMENT_TYPE_LABELS) {
    return DOCUMENT_TYPE_LABELS[value as keyof typeof DOCUMENT_TYPE_LABELS];
  }
  return value;
}

export default function RunResultSections({
  issues,
  decision,
  communication,
  auditSummary,
}: RunResultSectionsProps) {
  const [copyMessage, setCopyMessage] = useState<string | null>(null);

  const handleCopyEmail = async () => {
    if (!communication?.subject && !communication?.body) {
      return;
    }

    const emailText = [
      communication.subject ? `Subject: ${communication.subject}` : null,
      communication.body ?? null,
    ]
      .filter(Boolean)
      .join('\n\n');

    try {
      await copyToClipboard(emailText);
      setCopyMessage('Email copied to clipboard.');
    } catch {
      setCopyMessage('Unable to copy email to clipboard.');
    }
  };

  return (
    <>
      <SectionCard title="Issues">
        {issues.length === 0 ? (
          <EmptyState message="No issues were raised during this workflow run." />
        ) : (
          <ul className="issue-list">
            {issues.map((issue, index) => (
              <li key={issue.id ?? index} className={issueSeverityClass(issue.severity, issue.code)}>
                <div className="issue-item-header">
                  {issue.code && <strong>{issue.code}</strong>}
                  {issue.severity && <span className="issue-severity">{issue.severity}</span>}
                  {issue.sourceStep && <span className="muted issue-source">{issue.sourceStep}</span>}
                </div>
                {issue.message && <p className="issue-message">{issue.message}</p>}
                {issue.fieldName && (
                  <p className="muted issue-field">
                    Field: <code>{issue.fieldName}</code>
                  </p>
                )}
                {issue.expectedValue && (
                  <p className="muted issue-field">
                    Expected: <code>{formatIssueDocumentType(issue.expectedValue, issue.code)}</code>
                  </p>
                )}
                {issue.actualValue && (
                  <p className="muted issue-field">
                    Actual: <code>{formatIssueDocumentType(issue.actualValue, issue.code)}</code>
                  </p>
                )}
                {(issue.confidence != null || issue.evidenceSource || issue.evidenceText) && (
                  <div className="issue-evidence">
                    {issue.confidence != null && (
                      <p className="muted issue-field">
                        Confidence: <strong>{formatConfidence(issue.confidence)}</strong>
                      </p>
                    )}
                    {issue.evidenceSource && (
                      <p className="muted issue-field">
                        Evidence source: <code>{formatEvidenceSource(issue.evidenceSource)}</code>
                      </p>
                    )}
                    {issue.evidenceText && (
                      <p className="issue-evidence-text">{issue.evidenceText}</p>
                    )}
                  </div>
                )}
                {issue.recommendedAction && (
                  <p className="issue-action">{issue.recommendedAction}</p>
                )}
              </li>
            ))}
          </ul>
        )}
      </SectionCard>

      <SectionCard title="Decision">
        {!decision ? (
          <p className="muted">Decision has not been generated yet.</p>
        ) : (
          <dl className="detail-grid">
            {decision.status && (
              <div>
                <dt>Status</dt>
                <dd>
                  <StatusBadge status={decision.status} kind="decision" />
                </dd>
              </div>
            )}
            {decision.riskScore != null && (
              <div>
                <dt>Risk Score</dt>
                <dd>
                  <span className={formatRiskScore(decision.riskScore).className}>
                    {formatRiskScore(decision.riskScore).label}
                  </span>
                </dd>
              </div>
            )}
            {decision.reasonSummary && (
              <div className="full-width">
                <dt>Reason Summary</dt>
                <dd>{decision.reasonSummary}</dd>
              </div>
            )}
            {decision.triggeredRules && formatMultilineList(decision.triggeredRules).length > 0 && (
              <div className="full-width">
                <dt>Triggered Rules</dt>
                <dd>
                  <ul className="detail-list">
                    {formatMultilineList(decision.triggeredRules).map((rule) => (
                      <li key={rule}>{rule}</li>
                    ))}
                  </ul>
                </dd>
              </div>
            )}
            {decision.requiredActions && formatMultilineList(decision.requiredActions).length > 0 && (
              <div className="full-width">
                <dt>Required Actions</dt>
                <dd>
                  <ul className="detail-list">
                    {formatMultilineList(decision.requiredActions).map((action) => (
                      <li key={action}>{action}</li>
                    ))}
                  </ul>
                </dd>
              </div>
            )}
          </dl>
        )}
      </SectionCard>

      <SectionCard title="Communication">
        {!communication ? (
          <p className="muted">Vendor communication has not been generated yet.</p>
        ) : (
          <div className="communication-email">
            <div className="communication-email-toolbar">
              <button type="button" className="btn btn-secondary btn-sm" onClick={handleCopyEmail}>
                Copy email
              </button>
              {copyMessage && <span className="muted communication-copy-message">{copyMessage}</span>}
            </div>
            <dl className="detail-grid">
              {communication.subject && (
                <div className="full-width">
                  <dt>Subject</dt>
                  <dd>{communication.subject}</dd>
                </div>
              )}
              {communication.generationMethod && (
                <div>
                  <dt>Generation Method</dt>
                  <dd>{formatGenerationMethod(communication.generationMethod)}</dd>
                </div>
              )}
              {communication.body && (
                <div className="full-width">
                  <dt>Body</dt>
                  <dd className="pre-wrap communication-body">{communication.body}</dd>
                </div>
              )}
            </dl>
          </div>
        )}
      </SectionCard>

      <SectionCard title="Audit Summary">
        {!auditSummary ? (
          <p className="muted">Audit summary has not been generated yet.</p>
        ) : (
          <dl className="detail-grid">
            {auditSummary.generationMethod && (
              <div>
                <dt>Generation Method</dt>
                <dd>{formatGenerationMethod(auditSummary.generationMethod)}</dd>
              </div>
            )}
            {auditSummary.summary && (
              <div className="full-width">
                <dt>Summary</dt>
                <dd className="pre-wrap">{auditSummary.summary}</dd>
              </div>
            )}
          </dl>
        )}
      </SectionCard>
    </>
  );
}
