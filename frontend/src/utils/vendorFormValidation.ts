import type { VendorSubmissionPayload } from '../types/submission';
import type { DocumentUploadField, DocumentUploads } from '../types/document';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const BANK_LAST4_PATTERN = /^\d{4}$/;

const REQUIRED_FIELDS: (keyof VendorSubmissionPayload)[] = [
  'legalName',
  'country',
  'contactEmail',
  'taxId',
  'bankAccountHolderName',
  'bankCountry',
  'bankCode',
  'bankAccountLast4',
  'businessCategory',
];

const REQUIRED_LABELS: Record<string, string> = {
  legalName: 'Legal name',
  country: 'Country',
  contactEmail: 'Contact email',
  taxId: 'Tax ID',
  bankAccountHolderName: 'Bank account holder name',
  bankCountry: 'Bank country',
  bankCode: 'Bank code',
  bankAccountLast4: 'Bank account last 4',
  businessCategory: 'Business category',
};

export function validateVendorSubmission(
  form: VendorSubmissionPayload
): Record<string, string> {
  const errors: Record<string, string> = {};

  for (const field of REQUIRED_FIELDS) {
    if (!form[field].trim()) {
      errors[field] = `${REQUIRED_LABELS[field]} is required`;
    }
  }

  if (form.contactEmail.trim() && !EMAIL_PATTERN.test(form.contactEmail.trim())) {
    errors.contactEmail = 'Contact email must be a valid email address';
  }

  if (form.website.trim()) {
    const website = form.website.trim();
    if (!website.startsWith('http://') && !website.startsWith('https://')) {
      errors.website = 'Website must start with http:// or https://';
    }
  }

  if (form.bankAccountLast4.trim() && !BANK_LAST4_PATTERN.test(form.bankAccountLast4.trim())) {
    errors.bankAccountLast4 = 'Bank account last 4 must be exactly 4 digits';
  }

  return errors;
}

export function validateDocumentUploads(documents: DocumentUploads): Record<string, string> {
  const errors: Record<string, string> = {};

  (Object.entries(documents) as [DocumentUploadField, File | null][]).forEach(([field, file]) => {
    if (!file) {
      return;
    }
    const isPdf =
      file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      errors[field] = 'Only PDF documents are supported';
    }
  });

  return errors;
}

export function emptyVendorSubmission(): VendorSubmissionPayload {
  return {
    legalName: '',
    country: '',
    website: '',
    contactEmail: '',
    taxId: '',
    bankAccountHolderName: '',
    bankCountry: '',
    bankCode: '',
    bankAccountLast4: '',
    businessCategory: '',
  };
}
