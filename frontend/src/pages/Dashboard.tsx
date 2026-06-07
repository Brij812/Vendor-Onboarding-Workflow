import { Link } from 'react-router-dom';
import { useState } from 'react';
import { reseedExistingVendors, resetDemoRuns } from '../api/demo';
import HealthCard from '../components/HealthCard';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import RunHistoryTable from '../components/RunHistoryTable';
import SummaryCards from '../components/SummaryCards';
import { useWorkflowRuns } from '../hooks/useWorkflowRuns';
import type { WorkflowRunListItem } from '../types/workflow';

type StatusFilter = 'ALL' | 'RUNNING' | 'COMPLETED' | 'FAILED';

function matchesFilter(run: WorkflowRunListItem, filter: StatusFilter): boolean {
  if (filter === 'ALL') {
    return true;
  }
  return run.runStatus === filter;
}

export default function Dashboard() {
  const { runs, counts, loading, error, refresh } = useWorkflowRuns();
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [demoMessage, setDemoMessage] = useState<string | null>(null);
  const [demoError, setDemoError] = useState<string | null>(null);
  const [demoBusy, setDemoBusy] = useState(false);

  const filteredRuns = runs.filter((run) => matchesFilter(run, statusFilter));

  const handleResetRuns = async () => {
    if (!window.confirm('Delete all workflow runs and submissions? Existing vendors are preserved.')) {
      return;
    }

    setDemoBusy(true);
    setDemoMessage(null);
    setDemoError(null);
    try {
      const result = await resetDemoRuns();
      setDemoMessage(
        `Reset complete: ${result.deletedRuns} run(s), ${result.deletedSubmissions} submission(s), ${result.deletedFiles} file(s) removed.`,
      );
      await refresh();
    } catch (err) {
      setDemoError(err instanceof Error ? err.message : 'Failed to reset demo runs');
    } finally {
      setDemoBusy(false);
    }
  };

  const handleReseedVendors = async () => {
    if (!window.confirm('Replace all existing vendors with the default seed data?')) {
      return;
    }

    setDemoBusy(true);
    setDemoMessage(null);
    setDemoError(null);
    try {
      const result = await reseedExistingVendors();
      setDemoMessage(`Reseeded ${result.seededVendors} existing vendor record(s).`);
    } catch (err) {
      setDemoError(err instanceof Error ? err.message : 'Failed to reseed existing vendors');
    } finally {
      setDemoBusy(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header page-header-row">
        <div>
          <h1>Dashboard</h1>
          <p className="muted">Workflow run history and live operations overview.</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={refresh} disabled={loading}>
          Refresh
        </button>
      </header>

      <SummaryCards counts={counts} />

      <section className="card demo-tools-card">
        <h2 className="section-title">Demo Tools</h2>
        <p className="muted">Reset workflow history or restore seeded existing vendors for repeatable demos.</p>
        <div className="demo-tools-actions">
          <button type="button" className="btn btn-secondary" onClick={handleResetRuns} disabled={demoBusy}>
            Reset all runs
          </button>
          <button type="button" className="btn btn-secondary" onClick={handleReseedVendors} disabled={demoBusy}>
            Reseed existing vendors
          </button>
        </div>
        {demoMessage && <p className="demo-tools-message">{demoMessage}</p>}
        {demoError && <ErrorState message={demoError} />}
      </section>

      <section className="section-block">
        <div className="section-header-row">
          <h2 className="section-title">Run History</h2>
          <label className="filter-control">
            <span className="filter-label">Status</span>
            <select
              className="filter-select"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value as StatusFilter)}
            >
              <option value="ALL">All</option>
              <option value="RUNNING">Running</option>
              <option value="COMPLETED">Completed</option>
              <option value="FAILED">Failed</option>
            </select>
          </label>
        </div>
        {loading && runs.length === 0 && <LoadingState message="Loading workflow runs..." />}
        {error && <ErrorState message={error} onRetry={refresh} />}
        {!loading && !error && runs.length === 0 && (
          <EmptyState
            title="No workflow runs yet"
            message="Submit a vendor from New Submission or load a demo scenario to create your first run."
            action={<Link to="/new-submission" className="btn btn-primary">New Submission</Link>}
          />
        )}
        {!error && filteredRuns.length > 0 && <RunHistoryTable runs={filteredRuns} />}
        {!error && runs.length > 0 && filteredRuns.length === 0 && (
          <EmptyState message="No runs match the selected status filter." />
        )}
      </section>

      <section className="section-block">
        <HealthCard />
      </section>
    </div>
  );
}
