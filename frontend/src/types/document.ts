export type DocumentType =
  | 'TAX_REGISTRATION'
  | 'BANK_PROOF'
  | 'COMPANY_REGISTRATION'
  | 'COMPLIANCE_DECLARATION'
  | 'UNKNOWN';

export type ExtractionMethod = 'LLM' | 'FALLBACK' | 'RULE_BASED';

export interface DocumentExtraction {
  extractionMethod: ExtractionMethod;
  documentType: DocumentType;
  legalEntityName?: string | null;
  taxId?: string | null;
  bankAccountHolderName?: string | null;
  country?: string | null;
  documentDate?: string | null;
  confidenceScore?: number | null;
}

export type DocumentUploadField =
  | 'taxRegistration'
  | 'bankProof'
  | 'companyRegistration'
  | 'complianceDeclaration';

export interface UploadedDocument {
  documentType: DocumentType;
  originalFilename: string;
  contentType?: string | null;
  fileSize?: number | null;
  uploadedAt?: string | null;
  extractedText?: string | null;
  extraction?: DocumentExtraction | null;
}

export type DocumentUploads = Record<DocumentUploadField, File | null>;

export const emptyDocumentUploads = (): DocumentUploads => ({
  taxRegistration: null,
  bankProof: null,
  companyRegistration: null,
  complianceDeclaration: null,
});

export function hasAnyDocumentUploads(documents: DocumentUploads): boolean {
  return Object.values(documents).some((file) => file != null);
}
