const STEP_LABELS: Record<string, string> = {
  INTAKE_AGENT: 'Intake Agent',
  DOCUMENT_UNDERSTANDING_AGENT: 'Document Understanding Agent',
  COMPLETENESS_AGENT: 'Completeness Agent',
  FORMAT_VALIDATION_AGENT: 'Format Validation Agent',
  CONSISTENCY_CHECK_AGENT: 'Consistency Check Agent',
  DUPLICATE_RISK_AGENT: 'Duplicate & Risk Agent',
  DECISION_AGENT: 'Decision Agent',
  COMMUNICATION_AGENT: 'Communication Agent',
  AUDIT_SUMMARY_AGENT: 'Audit Summary Agent',
};

export function getStepLabel(stepName: string): string {
  return STEP_LABELS[stepName] ?? stepName;
}
