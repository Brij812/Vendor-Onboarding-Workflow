import { demoScenarios, type DemoScenario } from '../data/demoScenarios';

interface DemoScenariosPanelProps {
  onLoadScenario: (scenario: DemoScenario) => void;
  loadingScenarioId?: string | null;
}

const SCENARIO_DISPLAY_TITLES: Record<string, string> = {
  'approved-vendor': 'Approved Vendor',
  'missing-document': 'Missing Document',
  'bank-name-mismatch': 'Bank Name Mismatch',
  'duplicate-vendor': 'Duplicate Vendor',
  'blocked-vendor': 'Blocked Vendor',
  'wrong-document-type': 'Wrong Document Uploaded',
};

function scenarioCategory(outcome: DemoScenario['expectedOutcome']): string {
  switch (outcome) {
    case 'APPROVED':
      return 'Happy Path';
    case 'PENDING':
      return 'Needs Review';
    case 'REJECTED':
      return 'Risk / Rejected';
    default:
      return '';
  }
}

function categoryClass(outcome: DemoScenario['expectedOutcome']): string {
  switch (outcome) {
    case 'APPROVED':
      return 'demo-scenario-category demo-scenario-category--happy';
    case 'PENDING':
      return 'demo-scenario-category demo-scenario-category--review';
    case 'REJECTED':
      return 'demo-scenario-category demo-scenario-category--risk';
    default:
      return 'demo-scenario-category';
  }
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

function outcomeLabel(outcome: DemoScenario['expectedOutcome']): string {
  switch (outcome) {
    case 'APPROVED':
      return 'Approved';
    case 'PENDING':
      return 'Pending';
    case 'REJECTED':
      return 'Rejected';
    default:
      return outcome;
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
        Click a scenario to fill the form. You can review or edit the data before submitting.
      </p>
      <div className="demo-scenario-list">
        {demoScenarios.map((scenario) => {
          const isLoading = loadingScenarioId === scenario.id;
          const title = SCENARIO_DISPLAY_TITLES[scenario.id] ?? scenario.label;

          return (
            <button
              key={scenario.id}
              type="button"
              className={`demo-scenario-card${isLoading ? ' demo-scenario-card--loading' : ''}`}
              disabled={isLoading}
              onClick={() => onLoadScenario(scenario)}
              aria-busy={isLoading}
            >
              <div className="demo-scenario-card-header">
                <div className="demo-scenario-card-heading">
                  <span className={categoryClass(scenario.expectedOutcome)}>
                    {scenarioCategory(scenario.expectedOutcome)}
                  </span>
                  <span className="demo-scenario-title">{title}</span>
                </div>
                <span className={outcomeBadgeClass(scenario.expectedOutcome)}>
                  <span className="demo-outcome-badge-prefix">Expected</span>
                  {outcomeLabel(scenario.expectedOutcome)}
                </span>
              </div>
              <p className="demo-scenario-desc">{scenario.description}</p>
            </button>
          );
        })}
      </div>
    </aside>
  );
}
