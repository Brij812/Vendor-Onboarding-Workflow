import { useEffect, useState } from 'react';
import { fetchHealth, type HealthResponse } from '../api/client';

type LoadState = 'loading' | 'success' | 'error';

export default function HealthCard() {
  const [state, setState] = useState<LoadState>('loading');
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setState('loading');
      setError(null);
      try {
        const data = await fetchHealth();
        if (!cancelled) {
          setHealth(data);
          setState('success');
        }
      } catch (e) {
        if (!cancelled) {
          setHealth(null);
          setState('error');
          setError(e instanceof Error ? e.message : 'Unknown error');
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <section className="card health-card">
      <div className="card-header">
        <h2>Backend Health</h2>
        {state === 'success' && health && (
          <span className={`badge badge-${health.status.toLowerCase()}`}>{health.status}</span>
        )}
        {state === 'loading' && <span className="badge badge-loading">Checking…</span>}
        {state === 'error' && <span className="badge badge-error">DOWN</span>}
      </div>

      {state === 'loading' && <p className="muted">Connecting to API…</p>}

      {state === 'success' && health && (
        <dl className="health-details">
          <div>
            <dt>Application</dt>
            <dd>{health.appName}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{health.status}</dd>
          </div>
          <div>
            <dt>Timestamp</dt>
            <dd>{new Date(health.timestamp).toLocaleString()}</dd>
          </div>
        </dl>
      )}

      {state === 'error' && (
        <p className="error-text">
          Could not reach the backend. Ensure Spring Boot is running on port 8080.
          {error && <> ({error})</>}
        </p>
      )}
    </section>
  );
}
