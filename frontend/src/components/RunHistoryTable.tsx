import { Link } from 'react-router-dom';
import type { WorkflowRunListItem } from '../types/workflow';
import { formatDateTime, formatRiskScore } from '../utils/format';
import StatusBadge from './StatusBadge';

interface RunHistoryTableProps {
  runs: WorkflowRunListItem[];
}

export default function RunHistoryTable({ runs }: RunHistoryTableProps) {
  return (
    <div className="table-wrap card">
      <table className="data-table">
        <thead>
          <tr>
            <th>Display Run ID</th>
            <th>Vendor Name</th>
            <th>Run Status</th>
            <th>Current Step</th>
            <th>Final Decision</th>
            <th>Risk Score</th>
            <th>Created At</th>
            <th>Completed At</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {runs.map((run) => {
            const riskScore = formatRiskScore(run.riskScore);
            return (
              <tr key={run.workflowRunId}>
                <td>{run.displayRunId}</td>
                <td>{run.vendorName ?? '—'}</td>
                <td>
                  <StatusBadge status={run.runStatus} kind="run" />
                </td>
                <td>{run.currentStep ?? '—'}</td>
                <td>
                  {run.finalDecisionStatus ? (
                    <StatusBadge status={run.finalDecisionStatus} kind="decision" />
                  ) : (
                    '—'
                  )}
                </td>
                <td>
                  <span className={riskScore.className}>{riskScore.label}</span>
                </td>
                <td>{formatDateTime(run.createdAt)}</td>
                <td>{formatDateTime(run.completedAt)}</td>
                <td>
                  <Link className="btn btn-link" to={`/runs/${run.workflowRunId}`}>
                    View
                  </Link>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
