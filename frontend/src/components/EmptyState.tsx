import type { ReactNode } from 'react';

interface EmptyStateProps {
  message: string;
  title?: string;
  action?: ReactNode;
}

export default function EmptyState({ message, title, action }: EmptyStateProps) {
  return (
    <div className="state-box empty-state">
      {title && <h3 className="empty-state-title">{title}</h3>}
      <p className="muted">{message}</p>
      {action && <div className="empty-state-action">{action}</div>}
    </div>
  );
}
