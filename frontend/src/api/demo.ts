import type { DemoResetResponse, DemoReseedResponse } from '../types/demo';

async function postDemo<T>(path: string): Promise<T> {
  const response = await fetch(path, { method: 'POST' });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Demo request failed (${response.status})`);
  }
  return response.json() as Promise<T>;
}

export function resetDemoRuns(): Promise<DemoResetResponse> {
  return postDemo<DemoResetResponse>('/api/demo/reset-runs');
}

export function reseedExistingVendors(): Promise<DemoReseedResponse> {
  return postDemo<DemoReseedResponse>('/api/demo/reseed-existing-vendors');
}
