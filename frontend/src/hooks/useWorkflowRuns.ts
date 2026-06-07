import { useCallback, useEffect, useMemo, useState } from 'react';
import { getWorkflowRuns } from '../api/workflow';
import type { RunSummaryCounts, WorkflowRunListItem } from '../types/workflow';

function computeCounts(runs: WorkflowRunListItem[]): RunSummaryCounts {
  return {
    total: runs.length,
    running: runs.filter((r) => r.runStatus === 'RUNNING').length,
    completed: runs.filter((r) => r.runStatus === 'COMPLETED').length,
    failed: runs.filter((r) => r.runStatus === 'FAILED').length,
    approved: runs.filter((r) => r.finalDecisionStatus === 'APPROVED').length,
    pendingDecision: runs.filter((r) => r.finalDecisionStatus === 'PENDING').length,
    rejected: runs.filter((r) => r.finalDecisionStatus === 'REJECTED').length,
  };
}

export function useWorkflowRuns() {
  const [runs, setRuns] = useState<WorkflowRunListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getWorkflowRuns();
      setRuns(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load workflow runs');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const counts = useMemo(() => computeCounts(runs), [runs]);

  return { runs, counts, loading, error, refresh };
}
