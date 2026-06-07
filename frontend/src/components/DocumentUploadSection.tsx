import type { DocumentUploadField, DocumentUploads } from '../types/document';

interface DocumentFieldConfig {
  name: DocumentUploadField;
  label: string;
}

const DOCUMENT_FIELDS: DocumentFieldConfig[] = [
  { name: 'taxRegistration', label: 'Tax Registration Document' },
  { name: 'bankProof', label: 'Bank Proof' },
  { name: 'companyRegistration', label: 'Company Registration Certificate' },
  { name: 'complianceDeclaration', label: 'Compliance Declaration' },
];

interface DocumentUploadSectionProps {
  documents: DocumentUploads;
  errors: Record<string, string>;
  onChange: (field: DocumentUploadField, file: File | null) => void;
}

export default function DocumentUploadSection({
  documents,
  errors,
  onChange,
}: DocumentUploadSectionProps) {
  return (
    <section className="card form-section">
      <h2 className="section-title">Documents</h2>
      <p className="muted form-helper">
        Upload optional PDF documents. Each document type accepts one PDF file.
      </p>
      <div className="form-grid">
        {DOCUMENT_FIELDS.map((field) => {
          const selectedFile = documents[field.name];
          return (
            <div key={field.name} className="form-field">
              <label className="form-label" htmlFor={field.name}>
                {field.label}
              </label>
              <input
                id={field.name}
                name={field.name}
                type="file"
                accept="application/pdf,.pdf"
                className={`form-input form-file-input${errors[field.name] ? ' form-input-error' : ''}`}
                onChange={(event) => {
                  const file = event.target.files?.[0] ?? null;
                  onChange(field.name, file);
                }}
              />
              {selectedFile && (
                <p className="muted file-selected-name">{selectedFile.name}</p>
              )}
              {errors[field.name] && <p className="field-error">{errors[field.name]}</p>}
            </div>
          );
        })}
      </div>
    </section>
  );
}
