export interface VendorSubmissionPayload {
  legalName: string;
  country: string;
  website: string;
  contactEmail: string;
  taxId: string;
  bankAccountHolderName: string;
  bankCountry: string;
  bankCode: string;
  bankAccountLast4: string;
  businessCategory: string;
}

export interface CreateVendorSubmissionResponse {
  submissionId: string;
  workflowRunId: string;
  displayRunId: string;
  status: string;
  message: string;
}

export interface ApiErrorBody {
  message?: string;
  errors?: Record<string, string>;
}

export type VendorSubmissionField = keyof VendorSubmissionPayload;
