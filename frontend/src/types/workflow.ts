import type { UploadedDocument } from './document';

export type RunStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
export type StepStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'WARNING' | 'FAILED' | 'SKIPPED';
export type DecisionStatus = 'APPROVED' | 'PENDING' | 'REJECTED';
export type ReviewerOutcome = 'APPROVED_AFTER_REVIEW' | 'REJECTED_AFTER_REVIEW' | 'REQUEST_MORE_INFO';

export interface WorkflowRunListItem {
  workflowRunId: string;
  displayRunId: string;
  vendorName: string | null;
  runStatus: RunStatus;
  currentStep: string | null;
  finalDecisionStatus: DecisionStatus | null;
  riskScore: number | null;
  mainReason?: string | null;
  createdAt: string;
  completedAt: string | null;
}

export interface VendorSubmissionDetails {
  legalName: string | null;
  normalizedLegalName: string | null;
  country: string | null;
  website: string | null;
  contactEmail: string | null;
  taxId: string | null;
  bankAccountHolderName: string | null;
  normalizedBankAccountHolderName: string | null;
  bankCountry: string | null;
  bankCode: string | null;
  bankAccountLast4: string | null;
  businessCategory: string | null;
}

export interface WorkflowStepLog {
  stepName: string;
  stepOrder: number;
  status: StepStatus;
  summary: string | null;
  startedAt: string | null;
  completedAt: string | null;
  durationMs: number | null;
  outputSnapshot: string | null;
  errorMessage: string | null;
}

export interface Issue {
  id?: string;
  sourceStep?: string;
  code?: string;
  message?: string;
  severity?: string;
  fieldName?: string;
  expectedValue?: string;
  actualValue?: string;
  recommendedAction?: string;
  confidence?: number | null;
  evidenceSource?: string | null;
  evidenceText?: string | null;
  createdAt?: string;
  [key: string]: unknown;
}

export interface Decision {
  status?: DecisionStatus;
  riskScore?: number;
  reasonSummary?: string;
  triggeredRules?: string;
  requiredActions?: string;
  [key: string]: unknown;
}

export type GenerationMethod = 'TEMPLATE' | 'LLM' | 'FALLBACK';

export interface Communication {
  subject?: string;
  body?: string;
  generationMethod?: GenerationMethod | string;
  [key: string]: unknown;
}

export interface AuditSummary {
  summary?: string;
  generationMethod?: GenerationMethod | string;
  [key: string]: unknown;
}

export interface ManualReview {
  workflowRunId: string;
  reviewerOutcome: ReviewerOutcome;
  reviewerNote: string | null;
  reviewedAt: string;
}

export interface ManualReviewRequest {
  reviewerOutcome: ReviewerOutcome;
  reviewerNote?: string;
}

export interface WorkflowRunDetails {
  workflowRunId: string;
  displayRunId: string;
  runStatus: RunStatus;
  currentStep: string | null;
  vendor: VendorSubmissionDetails;
  steps: WorkflowStepLog[];
  issues: Issue[];
  documents: UploadedDocument[];
  decision: Decision | null;
  communication: Communication | null;
  auditSummary: AuditSummary | null;
  manualReview: ManualReview | null;
}

export interface RunSummaryCounts {
  total: number;
  running: number;
  completed: number;
  failed: number;
  approved: number;
  pendingDecision: number;
  rejected: number;
}
