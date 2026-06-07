import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { getWorkflowRunById } from '../api/workflow';
import type { RunStatus, WorkflowRunDetails } from '../types/workflow';

const FINISHED_STEP_STATUSES = new Set(['COMPLETED', 'WARNING', 'FAILED', 'SKIPPED']);

function shouldPoll(status: RunStatus | undefined): boolean {
  return status === 'RUNNING' || status === 'PENDING';
}

export function useWorkflowRunDetail(id: string | undefined) {
  const [run, setRun] = useState<WorkflowRunDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isPolling, setIsPolling] = useState(false);
  const runStatusRef = useRef<RunStatus | undefined>();

  const fetchRun = useCallback(async (showLoading = false) => {
    if (!id) {
      return;
    }
    if (showLoading) {
      setLoading(true);
    }
    setError(null);
    try {
      const data = await getWorkflowRunById(id);
      setRun(data);
      runStatusRef.current = data.runStatus;
      setIsPolling(shouldPoll(data.runStatus));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load workflow run');
      setIsPolling(false);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }, [id]);

  useEffect(() => {
    runStatusRef.current = undefined;
    setRun(null);
    fetchRun(true);
  }, [fetchRun]);

  useEffect(() => {
    if (!id) {
      return;
    }

    const interval = window.setInterval(() => {
      if (shouldPoll(runStatusRef.current)) {
        fetchRun(false);
      } else {
        setIsPolling(false);
      }
    }, 1000);

    return () => window.clearInterval(interval);
  }, [id, fetchRun]);

  const progress = useMemo(() => {
    const steps = run?.steps ?? [];
    const finished = steps.filter((s) => FINISHED_STEP_STATUSES.has(s.status)).length;
    return { finished, total: steps.length };
  }, [run]);

  return { run, loading, error, isPolling, progress, refresh: () => fetchRun(true) };
}
