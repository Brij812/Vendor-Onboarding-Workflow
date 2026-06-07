import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import { useReviewQueue } from '../hooks/useReviewQueue';
import { formatDateTime, formatRiskScore } from '../utils/format';

export default function ReviewQueue() {
  const { runs, loading, error, refresh } = useReviewQueue();

  if (loading && runs.length === 0) {
    return <LoadingState message="Loading review queue..." />;
  }

  if (error && runs.length === 0) {
    return <ErrorState message={error} onRetry={refresh} />;
  }

  return (
    <div className="page review-queue-page">
      <header className="page-header">
        <div>
          <h1>Review Queue</h1>
          <p className="muted">Workflow runs awaiting manual procurement review.</p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={refresh}>
          Refresh
        </button>
      </header>

      {error && <p className="inline-error">{error}</p>}

      {runs.length === 0 ? (
        <EmptyState
          title="No runs pending review"
          message="Runs with a PENDING final decision will appear here."
        />
      ) : (
        <div className="table-wrap card">
          <table className="data-table">
            <thead>
              <tr>
                <th>Run ID</th>
                <th>Vendor Name</th>
                <th>Risk Score</th>
                <th>Main Reason</th>
                <th>Created At</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {runs.map((run) => {
                const riskScore = formatRiskScore(run.riskScore);
                return (
                  <tr key={run.workflowRunId}>
                    <td>{run.displayRunId}</td>
                    <td>{run.vendorName ?? '—'}</td>
                    <td>
                      <span className={riskScore.className}>{riskScore.label}</span>
                    </td>
                    <td className="main-reason-cell">{run.mainReason ?? '—'}</td>
                    <td>{formatDateTime(run.createdAt)}</td>
                    <td>
                      <Link className="btn btn-link" to={`/runs/${run.workflowRunId}`}>
                        View
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
