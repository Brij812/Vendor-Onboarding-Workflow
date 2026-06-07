import type { WorkflowStepLog } from '../types/workflow';
import EmptyState from './EmptyState';
import TimelineStepCard from './TimelineStepCard';

interface WorkflowTimelineProps {
  steps: WorkflowStepLog[];
}

export default function WorkflowTimeline({ steps }: WorkflowTimelineProps) {
  const ordered = [...steps].sort((a, b) => a.stepOrder - b.stepOrder);

  return (
    <section className="timeline-section">
      <h2 className="section-title">Workflow Timeline</h2>
      {ordered.length === 0 ? (
        <EmptyState message="Workflow steps will appear here once the run starts." />
      ) : (
        <div className="timeline-list">
          {ordered.map((step) => (
            <TimelineStepCard key={`${step.stepOrder}-${step.stepName}`} step={step} />
          ))}
        </div>
      )}
    </section>
  );
}
