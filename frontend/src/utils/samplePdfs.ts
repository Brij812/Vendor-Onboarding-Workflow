import type { DocumentUploadField, DocumentUploads } from '../types/document';
import { emptyDocumentUploads } from '../types/document';

const SAMPLE_FILES: { field: DocumentUploadField; path: string }[] = [
  { field: 'taxRegistration', path: '/samples/tax-registration.pdf' },
  { field: 'bankProof', path: '/samples/bank-proof.pdf' },
  { field: 'companyRegistration', path: '/samples/company-registration.pdf' },
  { field: 'complianceDeclaration', path: '/samples/compliance-declaration.pdf' },
];

async function fetchSamplePdf(path: string): Promise<File> {
  const response = await fetch(path);
  if (!response.ok) {
    throw new Error(`Failed to load sample PDF: ${path}`);
  }
  const blob = await response.blob();
  const filename = path.split('/').pop() ?? 'sample.pdf';
  return new File([blob], filename, { type: 'application/pdf' });
}

export async function loadSamplePdfs(): Promise<DocumentUploads> {
  const uploads = emptyDocumentUploads();

  await Promise.all(
    SAMPLE_FILES.map(async ({ field, path }) => {
      uploads[field] = await fetchSamplePdf(path);
    }),
  );

  return uploads;
}

export async function loadMisSlottedSamplePdfs(): Promise<DocumentUploads> {
  const uploads = await loadSamplePdfs();
  uploads.taxRegistration = uploads.bankProof;
  uploads.bankProof = await fetchSamplePdf('/samples/bank-proof.pdf');
  return uploads;
}
