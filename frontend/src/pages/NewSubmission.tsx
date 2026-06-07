import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createVendorSubmission } from '../api/submission';
import DemoScenariosPanel from '../components/DemoScenariosPanel';
import VendorSubmissionForm from '../components/VendorSubmissionForm';
import type { DemoScenario } from '../data/demoScenarios';
import type { VendorSubmissionField, VendorSubmissionPayload } from '../types/submission';
import type { DocumentUploadField } from '../types/document';
import { emptyDocumentUploads } from '../types/document';
import { loadMisSlottedSamplePdfs, loadSamplePdfs } from '../utils/samplePdfs';
import {
  emptyVendorSubmission,
  validateDocumentUploads,
  validateVendorSubmission,
} from '../utils/vendorFormValidation';

export default function NewSubmission() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<VendorSubmissionPayload>(emptyVendorSubmission);
  const [documents, setDocuments] = useState(emptyDocumentUploads);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loadingScenarioId, setLoadingScenarioId] = useState<string | null>(null);

  const handleChange = (field: VendorSubmissionField, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setFieldErrors((prev) => {
      if (!prev[field]) return prev;
      const next = { ...prev };
      delete next[field];
      return next;
    });
    setSubmitError(null);
  };

  const handleDocumentChange = (field: DocumentUploadField, file: File | null) => {
    setDocuments((prev) => ({ ...prev, [field]: file }));
    setFieldErrors((prev) => {
      if (!prev[field]) return prev;
      const next = { ...prev };
      delete next[field];
      return next;
    });
    setSubmitError(null);
  };

  const handleLoadScenario = async (scenario: DemoScenario) => {
    setLoadingScenarioId(scenario.id);
    setFormData(scenario.data);
    setFieldErrors({});
    setSubmitError(null);

    try {
      if (scenario.attachMisSlottedSamplePdfs) {
        const sampleDocuments = await loadMisSlottedSamplePdfs();
        setDocuments(sampleDocuments);
      } else if (scenario.samplePdfSet) {
        const sampleDocuments = await loadSamplePdfs(scenario.samplePdfSet);
        setDocuments(sampleDocuments);
      } else {
        setDocuments(emptyDocumentUploads());
      }
    } catch (err) {
      setDocuments(emptyDocumentUploads());
      setSubmitError(err instanceof Error ? err.message : 'Failed to load sample PDFs');
    } finally {
      setLoadingScenarioId(null);
    }
  };

  const handleSubmit = async () => {
    const formErrors = validateVendorSubmission(formData);
    const documentErrors = validateDocumentUploads(documents);
    const errors = { ...formErrors, ...documentErrors };
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    setSubmitError(null);

    try {
      const response = await createVendorSubmission(formData, documents);
      navigate(`/runs/${response.workflowRunId}`);
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Submission failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <header className="page-header">
        <h1>New Vendor Submission</h1>
        <p className="muted">Submit a vendor for automated onboarding review.</p>
      </header>

      <div className="submission-layout">
        <VendorSubmissionForm
          values={formData}
          documents={documents}
          errors={fieldErrors}
          submitError={submitError}
          submitting={submitting}
          onChange={handleChange}
          onDocumentChange={handleDocumentChange}
          onSubmit={handleSubmit}
        />
        <DemoScenariosPanel
          onLoadScenario={handleLoadScenario}
          loadingScenarioId={loadingScenarioId}
        />
      </div>
    </div>
  );
}
