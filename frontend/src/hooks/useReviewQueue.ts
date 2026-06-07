import { useCallback, useEffect, useState } from 'react';
import { getReviewQueueRuns } from '../api/workflow';
import type { WorkflowRunListItem } from '../types/workflow';

export function useReviewQueue() {
  const [runs, setRuns] = useState<WorkflowRunListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getReviewQueueRuns();
      setRuns(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load review queue');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { runs, loading, error, refresh };
}
