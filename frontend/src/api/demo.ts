import { fetchJson } from './client';
import type { DemoResetResponse, DemoReseedResponse } from '../types/demo';

export function resetDemoRuns(): Promise<DemoResetResponse> {
  return fetchJson<DemoResetResponse>('/api/demo/reset-runs', { method: 'POST' });
}

export function reseedExistingVendors(): Promise<DemoReseedResponse> {
  return fetchJson<DemoReseedResponse>('/api/demo/reseed-existing-vendors', { method: 'POST' });
}
