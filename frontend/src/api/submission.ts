import type {
  ApiErrorBody,
  CreateVendorSubmissionResponse,
  VendorSubmissionPayload,
} from '../types/submission';
import type { DocumentUploads } from '../types/document';
import { hasAnyDocumentUploads } from '../types/document';

function formatApiError(body: ApiErrorBody, status: number): string {
  const parts: string[] = [];
  if (body.message) {
    parts.push(body.message);
  }
  if (body.errors) {
    const fieldMessages = Object.values(body.errors);
    if (fieldMessages.length > 0) {
      parts.push(fieldMessages.join(' '));
    }
  }
  if (parts.length > 0) {
    return parts.join(': ');
  }
  return `Request failed (${status})`;
}

async function parseErrorResponse(response: Response): Promise<string> {
  let message = `Request failed (${response.status})`;
  try {
    const body = (await response.json()) as ApiErrorBody;
    message = formatApiError(body, response.status);
  } catch {
    // use default message
  }
  return message;
}

export async function createVendorSubmission(
  payload: VendorSubmissionPayload,
  documents?: DocumentUploads
): Promise<CreateVendorSubmissionResponse> {
  if (documents && hasAnyDocumentUploads(documents)) {
    return createVendorSubmissionWithDocuments(payload, documents);
  }

  const response = await fetch('/api/vendor-submissions/review', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseErrorResponse(response));
  }

  return response.json() as Promise<CreateVendorSubmissionResponse>;
}

async function createVendorSubmissionWithDocuments(
  payload: VendorSubmissionPayload,
  documents: DocumentUploads
): Promise<CreateVendorSubmissionResponse> {
  const formData = new FormData();

  Object.entries(payload).forEach(([key, value]) => {
    formData.append(key, value ?? '');
  });

  if (documents.taxRegistration) {
    formData.append('taxRegistration', documents.taxRegistration);
  }
  if (documents.bankProof) {
    formData.append('bankProof', documents.bankProof);
  }
  if (documents.companyRegistration) {
    formData.append('companyRegistration', documents.companyRegistration);
  }
  if (documents.complianceDeclaration) {
    formData.append('complianceDeclaration', documents.complianceDeclaration);
  }

  const response = await fetch('/api/vendor-submissions/review', {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error(await parseErrorResponse(response));
  }

  return response.json() as Promise<CreateVendorSubmissionResponse>;
}
