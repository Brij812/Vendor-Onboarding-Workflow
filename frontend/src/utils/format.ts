export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  return new Date(value).toLocaleString();
}

export function formatDuration(ms: number | null | undefined): string {
  if (ms == null) {
    return '—';
  }
  if (ms < 1000) {
    return `${ms} ms`;
  }
  return `${(ms / 1000).toFixed(1)} s`;
}

export function formatValue(value: string | null | undefined): string {
  if (value == null || value.trim() === '') {
    return '—';
  }
  return value;
}

export function formatFileSize(bytes: number | null | undefined): string {
  if (bytes == null) {
    return '—';
  }
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function formatGenerationMethod(value: string | null | undefined): string {
  if (value == null || value.trim() === '') {
    return '—';
  }
  switch (value) {
    case 'LLM':
      return 'AI-generated';
    case 'FALLBACK':
      return 'Template (fallback)';
    case 'TEMPLATE':
      return 'Template';
    default:
      return value;
  }
}

export function formatMultilineList(value: string | null | undefined): string[] {
  if (value == null || value.trim() === '') {
    return [];
  }
  return value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
}

export function formatRiskScore(score: number | null | undefined): {
  label: string;
  className: string;
} {
  if (score == null) {
    return { label: '—', className: 'risk-score' };
  }
  if (score >= 70) {
    return { label: String(score), className: 'risk-score risk-score--high' };
  }
  if (score >= 40) {
    return { label: String(score), className: 'risk-score risk-score--medium' };
  }
  return { label: String(score), className: 'risk-score risk-score--low' };
}

export async function copyToClipboard(text: string): Promise<void> {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }

  const textarea = document.createElement('textarea');
  textarea.value = text;
  textarea.setAttribute('readonly', '');
  textarea.style.position = 'absolute';
  textarea.style.left = '-9999px';
  document.body.appendChild(textarea);
  textarea.select();
  document.execCommand('copy');
  document.body.removeChild(textarea);
}

export function parseOutputSnapshot(snapshot: string | null): string {
  if (!snapshot) {
    return '';
  }
  try {
    const parsed = JSON.parse(snapshot) as { message?: string };
    return parsed.message ?? snapshot;
  } catch {
    return snapshot;
  }
}
