import type { VendorSubmissionDetails } from '../types/workflow';
import { formatValue } from '../utils/format';

interface VendorDetailsPanelProps {
  vendor: VendorSubmissionDetails;
}

const FIELDS: { key: keyof VendorSubmissionDetails; label: string }[] = [
  { key: 'legalName', label: 'Legal Name' },
  { key: 'normalizedLegalName', label: 'Normalized Legal Name' },
  { key: 'country', label: 'Country' },
  { key: 'website', label: 'Website' },
  { key: 'contactEmail', label: 'Contact Email' },
  { key: 'taxId', label: 'Tax ID' },
  { key: 'bankAccountHolderName', label: 'Bank Account Holder Name' },
  { key: 'normalizedBankAccountHolderName', label: 'Normalized Bank Account Holder Name' },
  { key: 'bankCountry', label: 'Bank Country' },
  { key: 'bankCode', label: 'Bank Code' },
  { key: 'bankAccountLast4', label: 'Bank Account Last 4' },
  { key: 'businessCategory', label: 'Business Category' },
];

export default function VendorDetailsPanel({ vendor }: VendorDetailsPanelProps) {
  return (
    <section className="card vendor-panel">
      <h2 className="section-title">Vendor Details</h2>
      <dl className="detail-grid">
        {FIELDS.map(({ key, label }) => (
          <div key={key}>
            <dt>{label}</dt>
            <dd>{formatValue(vendor[key])}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}
