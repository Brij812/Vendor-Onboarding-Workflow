interface RunProgressBarProps {
  finished: number;
  total: number;
}

export default function RunProgressBar({ finished, total }: RunProgressBarProps) {
  const percent = total > 0 ? Math.round((finished / total) * 100) : 0;

  return (
    <div className="card progress-card">
      <div className="progress-header">
        <span className="progress-label">
          {finished} of {total} steps finished
        </span>
        <span className="muted">{percent}%</span>
      </div>
      <div className="progress-track">
        <div className="progress-fill" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}
