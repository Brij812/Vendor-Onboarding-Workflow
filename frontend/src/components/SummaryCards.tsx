import type { RunSummaryCounts } from '../types/workflow';

interface SummaryCardsProps {
  counts: RunSummaryCounts;
}

const CARDS: { key: keyof RunSummaryCounts; label: string; accent?: string }[] = [
  { key: 'total', label: 'Total Runs' },
  { key: 'running', label: 'Running' },
  { key: 'completed', label: 'Completed' },
  { key: 'failed', label: 'Failed', accent: 'summary-card--danger' },
  { key: 'approved', label: 'Approved', accent: 'summary-card--success' },
  { key: 'pendingDecision', label: 'Pending', accent: 'summary-card--warning' },
  { key: 'rejected', label: 'Rejected', accent: 'summary-card--danger' },
];

export default function SummaryCards({ counts }: SummaryCardsProps) {
  return (
    <div className="summary-grid">
      {CARDS.map(({ key, label, accent }) => (
        <div key={key} className={`summary-card${accent ? ` ${accent}` : ''}`}>
          <span className="summary-card-value">{counts[key]}</span>
          <span className="summary-card-label">{label}</span>
        </div>
      ))}
    </div>
  );
}
