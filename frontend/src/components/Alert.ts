export type AlertType = 'error' | 'success' | 'info';

/**
 * Generate semantic HTML alert banner
 */
export function generateAlertHtml(message: string, type: AlertType): string {
  const icons: Record<AlertType, string> = {
    success: '✓',
    error: '✕',
    info: 'ℹ',
  };

  return `
<div class="alert-content">
  <span class="alert-icon" aria-hidden="true">${icons[type]}</span>
  <span class="alert-text">${message}</span>
</div>
`;
}
