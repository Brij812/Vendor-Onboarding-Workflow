import type { WorkflowStepLog } from '../types/workflow';
import { formatDateTime, formatDuration, parseOutputSnapshot } from '../utils/format';
import { getStepLabel } from '../utils/stepLabels';
import StatusBadge from './StatusBadge';

interface TimelineStepCardProps {
  step: WorkflowStepLog;
}

function stepCardClass(status: WorkflowStepLog['status']): string {
  const base = 'timeline-step';
  if (status === 'RUNNING') return `${base} step-running`;
  if (status === 'PENDING') return `${base} step-muted`;
  if (status === 'COMPLETED') return `${base} step-completed`;
  if (status === 'WARNING') return `${base} step-warning`;
  if (status === 'FAILED') return `${base} step-failed`;
  if (status === 'SKIPPED') return `${base} step-skipped`;
  return base;
}

function stepStatusLabel(status: WorkflowStepLog['status']): string {
  switch (status) {
    case 'COMPLETED':
      return 'Completed';
    case 'WARNING':
      return 'Warning';
    case 'FAILED':
      return 'Failed';
    case 'RUNNING':
      return 'Running';
    case 'SKIPPED':
      return 'Skipped';
    default:
      return 'Pending';
  }
}

export default function TimelineStepCard({ step }: TimelineStepCardProps) {
  const output = parseOutputSnapshot(step.outputSnapshot);
  const showOutputDetails = output.length > 120;

  return (
    <div className={stepCardClass(step.status)}>
      <div className="timeline-step-header">
        <div>
          <span className="timeline-step-number">Step {step.stepOrder}</span>
          <h3 className="timeline-step-title">
            {getStepLabel(step.stepName)} · {stepStatusLabel(step.status)}
          </h3>
        </div>
        <StatusBadge status={step.status} kind="step" />
      </div>
      {step.summary && <p className="timeline-step-summary">{step.summary}</p>}
      <dl className="timeline-meta">
        <div>
          <dt>Started</dt>
          <dd>{formatDateTime(step.startedAt)}</dd>
        </div>
        <div>
          <dt>Completed</dt>
          <dd>{formatDateTime(step.completedAt)}</dd>
        </div>
        <div>
          <dt>Duration</dt>
          <dd>{formatDuration(step.durationMs)}</dd>
        </div>
      </dl>
      {output && !showOutputDetails && (
        <p className="timeline-output">
          <strong>Output:</strong> {output}
        </p>
      )}
      {output && showOutputDetails && (
        <details className="timeline-output-details">
          <summary>View full output snapshot</summary>
          <pre className="pre-wrap">{output}</pre>
        </details>
      )}
      {step.errorMessage && (
        <p className="timeline-error">
          <strong>Error:</strong> {step.errorMessage}
        </p>
      )}
    </div>
  );
}
