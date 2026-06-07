import type { DocumentType } from '../types/document';

export const DOCUMENT_TYPE_LABELS: Record<DocumentType, string> = {
  TAX_REGISTRATION: 'Tax Registration Document',
  BANK_PROOF: 'Bank Proof',
  COMPANY_REGISTRATION: 'Company Registration Certificate',
  COMPLIANCE_DECLARATION: 'Compliance Declaration',
  UNKNOWN: 'Unknown Document',
};
