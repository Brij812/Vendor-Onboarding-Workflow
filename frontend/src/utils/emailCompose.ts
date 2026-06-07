export function buildGmailComposeUrl(to: string | undefined, subject: string, body: string): string {
  const params = new URLSearchParams({
    view: 'cm',
    fs: '1',
    su: subject,
    body,
  });
  if (to) {
    params.set('to', to);
  }
  return `https://mail.google.com/mail/?${params.toString()}`;
}

export function buildOutlookComposeUrl(to: string | undefined, subject: string, body: string): string {
  const params = new URLSearchParams({
    subject,
    body,
  });
  if (to) {
    params.set('to', to);
  }
  return `https://outlook.office.com/mail/deeplink/compose?${params.toString()}`;
}
