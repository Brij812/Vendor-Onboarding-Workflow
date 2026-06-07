import { useState } from 'react';
import { saveManualReview } from '../api/workflow';
import type { ManualReview, ReviewerOutcome } from '../types/workflow';
import { formatDateTime } from '../utils/format';

const OUTCOME_OPTIONS: { value: ReviewerOutcome; label: string }[] = [
  { value: 'APPROVED_AFTER_REVIEW', label: 'Approved after review' },
  { value: 'REJECTED_AFTER_REVIEW', label: 'Rejected after review' },
  { value: 'REQUEST_MORE_INFO', label: 'Request more info' },
];

interface ManualReviewCardProps {
  runId: string;
  existingReview: ManualReview | null;
  onSaved: () => void;
}

export default function ManualReviewCard({
  runId,
  existingReview,
  onSaved,
}: ManualReviewCardProps) {
  const [outcome, setOutcome] = useState<ReviewerOutcome>(
    existingReview?.reviewerOutcome ?? 'REQUEST_MORE_INFO'
  );
  const [note, setNote] = useState(existingReview?.reviewerNote ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      await saveManualReview(runId, {
        reviewerOutcome: outcome,
        reviewerNote: note.trim() || undefined,
      });
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save manual review');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="card manual-review-card">
      <h2 className="section-title">Manual Review</h2>
      <p className="muted">
        Record a procurement reviewer decision for this workflow run.
      </p>

      {existingReview && (
        <div className="manual-review-existing">
          <p>
            <strong>Last review:</strong> {existingReview.reviewerOutcome.replace(/_/g, ' ')}
          </p>
          {existingReview.reviewerNote && <p>{existingReview.reviewerNote}</p>}
          <p className="muted">Reviewed at {formatDateTime(existingReview.reviewedAt)}</p>
        </div>
      )}

      <div className="manual-review-form">
        <label className="form-field">
          <span>Reviewer outcome</span>
          <select
            value={outcome}
            onChange={(e) => setOutcome(e.target.value as ReviewerOutcome)}
            disabled={saving}
          >
            {OUTCOME_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </label>

        <label className="form-field">
          <span>Reviewer note</span>
          <textarea
            rows={4}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Add context for this review decision..."
            disabled={saving}
          />
        </label>

        {error && <p className="inline-error">{error}</p>}

        <button
          type="button"
          className="btn btn-primary"
          onClick={handleSave}
          disabled={saving}
        >
          {saving ? 'Saving...' : 'Save review decision'}
        </button>
      </div>
    </section>
  );
}
