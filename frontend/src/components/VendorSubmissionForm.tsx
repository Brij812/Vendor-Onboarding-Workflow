import type { VendorSubmissionField, VendorSubmissionPayload } from '../types/submission';
import type { DocumentUploadField, DocumentUploads } from '../types/document';
import DocumentUploadSection from './DocumentUploadSection';

interface FieldConfig {
  name: VendorSubmissionField;
  label: string;
  required?: boolean;
  placeholder?: string;
  type?: string;
}

interface SectionConfig {
  title: string;
  fields: FieldConfig[];
}

const FORM_SECTIONS: SectionConfig[] = [
  {
    title: 'Company Details',
    fields: [
      { name: 'legalName', label: 'Legal Name', required: true, placeholder: 'Acme Technologies Pvt Ltd' },
      { name: 'country', label: 'Country', required: true, placeholder: 'India' },
      { name: 'website', label: 'Website', placeholder: 'https://example.com' },
      { name: 'contactEmail', label: 'Contact Email', required: true, placeholder: 'finance@example.com', type: 'email' },
    ],
  },
  {
    title: 'Tax Details',
    fields: [
      { name: 'taxId', label: 'Tax ID', required: true, placeholder: '29ABCDE1234F1Z5' },
    ],
  },
  {
    title: 'Bank Details',
    fields: [
      { name: 'bankAccountHolderName', label: 'Bank Account Holder Name', required: true, placeholder: 'Acme Technologies Pvt Ltd' },
      { name: 'bankCountry', label: 'Bank Country', required: true, placeholder: 'India' },
      { name: 'bankCode', label: 'Bank Code', required: true, placeholder: 'HDFC0001234' },
      { name: 'bankAccountLast4', label: 'Bank Account Last 4', required: true, placeholder: '8821', type: 'text' },
    ],
  },
  {
    title: 'Business Details',
    fields: [
      { name: 'businessCategory', label: 'Business Category', required: true, placeholder: 'Software Services' },
    ],
  },
];

interface VendorSubmissionFormProps {
  values: VendorSubmissionPayload;
  documents: DocumentUploads;
  errors: Record<string, string>;
  submitError: string | null;
  submitting: boolean;
  onChange: (field: VendorSubmissionField, value: string) => void;
  onDocumentChange: (field: DocumentUploadField, file: File | null) => void;
  onSubmit: () => void;
}

function FormField({
  field,
  value,
  error,
  onChange,
}: {
  field: FieldConfig;
  value: string;
  error?: string;
  onChange: (field: VendorSubmissionField, value: string) => void;
}) {
  return (
    <div className="form-field">
      <label className="form-label" htmlFor={field.name}>
        {field.label}
        {field.required && <span className="field-required"> *</span>}
      </label>
      <input
        id={field.name}
        name={field.name}
        type={field.type ?? 'text'}
        className={`form-input${error ? ' form-input-error' : ''}`}
        value={value}
        placeholder={field.placeholder}
        onChange={(e) => onChange(field.name, e.target.value)}
      />
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}

export default function VendorSubmissionForm({
  values,
  documents,
  errors,
  submitError,
  submitting,
  onChange,
  onDocumentChange,
  onSubmit,
}: VendorSubmissionFormProps) {
  const validationSummary = Object.values(errors);

  return (
    <form
      className={`vendor-submission-form${submitting ? ' vendor-submission-form--submitting' : ''}`}
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit();
      }}
      noValidate
    >
      {(validationSummary.length > 0 || submitError) && (
        <div className="alert-error" role="alert">
          {submitError && <p>{submitError}</p>}
          {validationSummary.length > 0 && (
            <ul>
              {validationSummary.map((msg) => (
                <li key={msg}>{msg}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      {FORM_SECTIONS.map((section) => (
        <section key={section.title} className="card form-section">
          <h2 className="section-title">{section.title}</h2>
          <div className="form-grid">
            {section.fields.map((field) => (
              <FormField
                key={field.name}
                field={field}
                value={values[field.name]}
                error={errors[field.name]}
                onChange={onChange}
              />
            ))}
          </div>
        </section>
      ))}

      <DocumentUploadSection
        documents={documents}
        errors={errors}
        onChange={onDocumentChange}
      />

      <section className="card form-footer">
        <p className="muted form-helper">
          After submission, you will be redirected to the live workflow run view.
        </p>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Creating workflow run...' : 'Run Onboarding Review'}
        </button>
        {submitting && (
          <div className="submit-overlay" aria-live="polite">
            <p>Submitting vendor review...</p>
          </div>
        )}
      </section>
    </form>
  );
}
