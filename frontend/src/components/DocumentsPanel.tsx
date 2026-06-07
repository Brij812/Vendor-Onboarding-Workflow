import type { UploadedDocument } from '../types/document';
import EmptyState from './EmptyState';
import { DOCUMENT_TYPE_LABELS } from '../utils/documentLabels';
import { formatDateTime, formatFileSize, formatValue } from '../utils/format';

interface DocumentsPanelProps {
  documents: UploadedDocument[];
}

const TEXT_PREVIEW_LENGTH = 200;

function previewExtractedText(text: string): string {
  if (text.length <= TEXT_PREVIEW_LENGTH) {
    return text;
  }
  return `${text.slice(0, TEXT_PREVIEW_LENGTH)}...`;
}

function formatConfidence(score: number | null | undefined): string {
  if (score == null) {
    return '—';
  }
  return `${Math.round(score * 100)}%`;
}

export default function DocumentsPanel({ documents }: DocumentsPanelProps) {
  return (
    <section className="card result-section">
      <h2 className="section-title">Uploaded Documents</h2>
      {documents.length === 0 ? (
        <EmptyState message="No documents were uploaded for this run." />
      ) : (
        <ul className="document-list">
          {documents.map((document, index) => {
            const detectedType = document.extraction?.documentType;
            const hasTypeMismatch =
              detectedType != null && detectedType !== document.documentType;

            return (
            <li
              key={`${document.documentType}-${document.originalFilename}-${index}`}
              className={hasTypeMismatch ? 'document-item document-item--type-mismatch' : 'document-item'}
            >
              <div className="document-item-header">
                <strong>{DOCUMENT_TYPE_LABELS[document.documentType] ?? document.documentType}</strong>
              </div>
              <dl className="detail-grid document-item-grid">
                <div>
                  <dt>Original Filename</dt>
                  <dd>{formatValue(document.originalFilename)}</dd>
                </div>
                <div>
                  <dt>Content Type</dt>
                  <dd>{formatValue(document.contentType)}</dd>
                </div>
                <div>
                  <dt>File Size</dt>
                  <dd>{formatFileSize(document.fileSize)}</dd>
                </div>
                <div>
                  <dt>Uploaded</dt>
                  <dd>{formatDateTime(document.uploadedAt)}</dd>
                </div>
              </dl>
              {document.extraction ? (
                <>
                  <h3 className="document-subheading">Structured Extraction</h3>
                  <dl className="detail-grid document-item-grid document-structured-fields">
                    <div>
                      <dt>Expected Document Type</dt>
                      <dd>{DOCUMENT_TYPE_LABELS[document.documentType] ?? document.documentType}</dd>
                    </div>
                    <div>
                      <dt>Detected Document Type</dt>
                      <dd>
                        {detectedType
                          ? DOCUMENT_TYPE_LABELS[detectedType] ?? detectedType
                          : '—'}
                        {hasTypeMismatch && (
                          <span className="document-type-mismatch-badge">Mismatch</span>
                        )}
                      </dd>
                    </div>
                    <div>
                      <dt>Extraction Method</dt>
                      <dd>{formatValue(document.extraction.extractionMethod)}</dd>
                    </div>
                    <div>
                      <dt>Confidence</dt>
                      <dd>{formatConfidence(document.extraction.confidenceScore)}</dd>
                    </div>
                    <div>
                      <dt>Legal Entity Name</dt>
                      <dd>{formatValue(document.extraction.legalEntityName)}</dd>
                    </div>
                    <div>
                      <dt>Tax ID</dt>
                      <dd>{formatValue(document.extraction.taxId)}</dd>
                    </div>
                    <div>
                      <dt>Bank Account Holder</dt>
                      <dd>{formatValue(document.extraction.bankAccountHolderName)}</dd>
                    </div>
                    <div>
                      <dt>Country</dt>
                      <dd>{formatValue(document.extraction.country)}</dd>
                    </div>
                    <div>
                      <dt>Document Date</dt>
                      <dd>{formatValue(document.extraction.documentDate)}</dd>
                    </div>
                  </dl>
                </>
              ) : (
                <p className="muted">Structured extraction not available.</p>
              )}
              <div className="document-extracted-preview">
                <h3 className="document-subheading">Extracted Text</h3>
                {document.extractedText ? (
                  <>
                    <p className="document-extracted-snippet">{previewExtractedText(document.extractedText)}</p>
                    <details>
                      <summary>View extracted text</summary>
                      <pre className="pre-wrap document-extracted-full">{document.extractedText}</pre>
                    </details>
                  </>
                ) : (
                  <p className="muted">No extracted text available.</p>
                )}
              </div>
            </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
