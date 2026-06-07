import { demoScenarios, type DemoScenario } from '../data/demoScenarios';

interface DemoScenariosPanelProps {
  onLoadScenario: (scenario: DemoScenario) => void;
  loadingScenarioId?: string | null;
}

function outcomeBadgeClass(outcome: DemoScenario['expectedOutcome']): string {
  switch (outcome) {
    case 'APPROVED':
      return 'demo-outcome-badge demo-outcome-badge--approved';
    case 'PENDING':
      return 'demo-outcome-badge demo-outcome-badge--pending';
    case 'REJECTED':
      return 'demo-outcome-badge demo-outcome-badge--rejected';
    default:
      return 'demo-outcome-badge';
  }
}

export default function DemoScenariosPanel({
  onLoadScenario,
  loadingScenarioId,
}: DemoScenariosPanelProps) {
  return (
    <aside className="card demo-scenarios-panel">
      <h2 className="section-title">Demo Scenarios</h2>
      <p className="muted demo-scenarios-intro">
        Use these demo scenarios to quickly test happy path and edge cases. Buttons fill the form
        only — they do not submit.
      </p>
      <div className="demo-scenario-list">
        {demoScenarios.map((scenario) => (
          <button
            key={scenario.id}
            type="button"
            className="btn btn-secondary demo-scenario-btn"
            disabled={loadingScenarioId === scenario.id}
            onClick={() => onLoadScenario(scenario)}
          >
            <span className="demo-scenario-btn-top">
              <span className="demo-scenario-label">{scenario.label}</span>
              <span className={outcomeBadgeClass(scenario.expectedOutcome)}>
                Expected: {scenario.expectedOutcome}
              </span>
            </span>
            <span className="demo-scenario-desc">{scenario.description}</span>
          </button>
        ))}
      </div>
    </aside>
  );
}
