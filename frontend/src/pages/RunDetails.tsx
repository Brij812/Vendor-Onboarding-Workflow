import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { rerunWorkflowRun } from '../api/workflow';
import DocumentsPanel from '../components/DocumentsPanel';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import ManualReviewCard from '../components/ManualReviewCard';
import RunProgressBar from '../components/RunProgressBar';
import RunResultSections from '../components/RunResultSections';
import StatusBadge from '../components/StatusBadge';
import VendorDetailsPanel from '../components/VendorDetailsPanel';
import WorkflowTimeline from '../components/WorkflowTimeline';
import { useWorkflowRunDetail } from '../hooks/useWorkflowRunDetail';
import { formatRiskScore } from '../utils/format';

export default function RunDetails() {
  const { id } = useParams<{ id: string }>();
  const { run, loading, error, isPolling, progress, refresh } = useWorkflowRunDetail(id);
  const [rerunning, setRerunning] = useState(false);
  const [rerunError, setRerunError] = useState<string | null>(null);

  if (loading && !run) {
    return <LoadingState message="Loading workflow run..." />;
  }

  if (error && !run) {
    return <ErrorState message={error} onRetry={refresh} />;
  }

  if (!run) {
    return <ErrorState message="Workflow run not found." />;
  }

  const riskScore = formatRiskScore(run.decision?.riskScore);
  const finalDecisionStatus = run.decision?.status;
  const showManualReview =
    finalDecisionStatus === 'PENDING' || finalDecisionStatus === 'REJECTED';

  const handleRerun = async () => {
    if (!id) {
      return;
    }
    const confirmed = window.confirm(
      'Re-run this workflow? Previous issues, decision, communication, and audit summary will be cleared. Vendor submission and uploaded documents will be kept.'
    );
    if (!confirmed) {
      return;
    }

    setRerunning(true);
    setRerunError(null);
    try {
      await rerunWorkflowRun(id);
      await refresh();
    } catch (e) {
      setRerunError(e instanceof Error ? e.message : 'Failed to re-run workflow');
    } finally {
      setRerunning(false);
    }
  };

  return (
    <div className="page run-details-page">
      <div className="page-toolbar">
        <Link to="/" className="btn btn-link">
          Back to Dashboard
        </Link>
        <div className="page-toolbar-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={handleRerun}
            disabled={rerunning || run.runStatus === 'RUNNING'}
          >
            {rerunning ? 'Re-running...' : 'Re-run Workflow'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={refresh}>
            Refresh
          </button>
        </div>
      </div>

      {rerunError && <p className="inline-error">{rerunError}</p>}

      <header className="card run-header-card">
        <div className="run-header-top">
          <div>
            <h1>{run.displayRunId}</h1>
            <p className="muted">{run.vendor.legalName ?? 'Unknown vendor'}</p>
          </div>
          <div className="run-header-badges">
            <StatusBadge status={run.runStatus} kind="run" />
            {finalDecisionStatus && (
              <StatusBadge status={finalDecisionStatus} kind="decision" />
            )}
          </div>
        </div>
        <dl className="detail-grid run-header-meta">
          <div>
            <dt>Current Step</dt>
            <dd>{run.currentStep ?? '—'}</dd>
          </div>
          <div>
            <dt>Polling</dt>
            <dd>{isPolling ? 'Active (every 1s)' : 'Stopped'}</dd>
          </div>
          <div>
            <dt>Workflow Run ID</dt>
            <dd className="mono">{run.workflowRunId}</dd>
          </div>
        </dl>
      </header>

      <RunProgressBar finished={progress.finished} total={progress.total} />
      <WorkflowTimeline steps={run.steps} />
      <VendorDetailsPanel vendor={run.vendor} />
      <DocumentsPanel documents={run.documents ?? []} />

      {run.decision && (
        <section className="card decision-hero">
          <div className="decision-hero-main">
            <span className="decision-hero-label">Final Decision</span>
            <StatusBadge status={run.decision.status ?? finalDecisionStatus} kind="decision" />
          </div>
          <div className="decision-hero-meta">
            <span className="decision-hero-label">Risk Score</span>
            <span className={riskScore.className}>{riskScore.label}</span>
          </div>
          {run.decision.reasonSummary && (
            <p className="decision-hero-summary">{run.decision.reasonSummary}</p>
          )}
        </section>
      )}

      {showManualReview && (
        <ManualReviewCard
          runId={run.workflowRunId}
          existingReview={run.manualReview}
          onSaved={refresh}
        />
      )}

      <RunResultSections
        issues={run.issues}
        decision={run.decision}
        communication={run.communication}
        auditSummary={run.auditSummary}
        recipientEmail={run.vendor.contactEmail}
      />
    </div>
  );
}
