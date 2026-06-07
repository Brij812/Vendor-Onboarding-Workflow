import type { DecisionStatus, RunStatus, StepStatus } from '../types/workflow';

type BadgeKind = 'run' | 'step' | 'decision';

interface StatusBadgeProps {
  status: RunStatus | StepStatus | DecisionStatus | string | null | undefined;
  kind?: BadgeKind;
}

function badgeClass(status: string, kind: BadgeKind): string {
  if (kind === 'decision' && status === 'PENDING') {
    return 'badge badge-decision-pending';
  }

  switch (status) {
    case 'RUNNING':
      return 'badge badge-running';
    case 'COMPLETED':
    case 'APPROVED':
      return 'badge badge-success';
    case 'FAILED':
    case 'REJECTED':
      return 'badge badge-error';
    case 'WARNING':
      return 'badge badge-warning';
    case 'SKIPPED':
      return 'badge badge-neutral';
    case 'PENDING':
      return 'badge badge-pending';
    default:
      return 'badge badge-neutral';
  }
}

export default function StatusBadge({ status, kind = 'run' }: StatusBadgeProps) {
  if (!status) {
    return <span className="badge badge-neutral">—</span>;
  }
  return <span className={badgeClass(status, kind)}>{status}</span>;
}
