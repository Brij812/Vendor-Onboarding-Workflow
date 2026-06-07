import type { SamplePdfSet } from '../data/demoScenarios';
import type { DocumentUploadField, DocumentUploads } from '../types/document';
import { emptyDocumentUploads } from '../types/document';

const SAMPLE_FILES: { field: DocumentUploadField; filename: string }[] = [
  { field: 'taxRegistration', filename: 'tax-registration.pdf' },
  { field: 'bankProof', filename: 'bank-proof.pdf' },
  { field: 'companyRegistration', filename: 'company-registration.pdf' },
  { field: 'complianceDeclaration', filename: 'compliance-declaration.pdf' },
];

async function fetchSamplePdf(set: SamplePdfSet, filename: string): Promise<File> {
  const path = `/samples/${set}/${filename}`;
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(`Failed to load sample PDF: ${path}`);
  }
  const blob = await response.blob();
  return new File([blob], filename, { type: 'application/pdf' });
}

export async function loadSamplePdfs(set: SamplePdfSet = 'nexora'): Promise<DocumentUploads> {
  const uploads = emptyDocumentUploads();

  await Promise.all(
    SAMPLE_FILES.map(async ({ field, filename }) => {
      uploads[field] = await fetchSamplePdf(set, filename);
    }),
  );

  return uploads;
}

export async function loadMisSlottedSamplePdfs(): Promise<DocumentUploads> {
  const uploads = await loadSamplePdfs('nexora');
  uploads.taxRegistration = uploads.bankProof;
  uploads.bankProof = await fetchSamplePdf('nexora', 'bank-proof.pdf');
  return uploads;
}
