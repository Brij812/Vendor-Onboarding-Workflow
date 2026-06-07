import { fetchJson } from './client';
import type {
  ManualReview,
  ManualReviewRequest,
  WorkflowRunDetails,
  WorkflowRunListItem,
} from '../types/workflow';

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
