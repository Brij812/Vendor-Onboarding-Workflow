import type {
  ManualReview,
  ManualReviewRequest,
  WorkflowRunDetails,
  WorkflowRunListItem,
} from '../types/workflow';

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init);
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const message =
      body && typeof body === 'object' && 'message' in body
        ? String((body as { message: string }).message)
        : `Request failed (${response.status}): ${url}`;
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export async function getWorkflowRuns(): Promise<WorkflowRunListItem[]> {
  return fetchJson<WorkflowRunListItem[]>('/api/workflow-runs');
}

export async function getReviewQueueRuns(): Promise<WorkflowRunListItem[]> {
  return fetchJson<WorkflowRunListItem[]>('/api/workflow-runs/review-queue');
}

export async function getWorkflowRunById(id: string): Promise<WorkflowRunDetails> {
  return fetchJson<WorkflowRunDetails>(`/api/workflow-runs/${encodeURIComponent(id)}`);
}

export async function saveManualReview(
  runId: string,
  payload: ManualReviewRequest
): Promise<ManualReview> {
  return fetchJson<ManualReview>(`/api/workflow-runs/${encodeURIComponent(runId)}/manual-review`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

export async function rerunWorkflowRun(runId: string): Promise<{ runId: string }> {
  return fetchJson<{ runId: string }>(`/api/workflow-runs/${encodeURIComponent(runId)}/rerun`, {
    method: 'POST',
  });
}
