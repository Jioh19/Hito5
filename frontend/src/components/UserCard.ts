import { User } from '../models/user';

/**
 * Basic HTML sanitizer to prevent XSS injection
 */
function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
}

/**
 * Generate semantic HTML card for a user
 */
export function generateUserCardHtml(user: User): string {
  const initial = user.username ? user.username.charAt(0).toUpperCase() : '?';
  const safeUsername = escapeHtml(user.username);
  const safeEmail = escapeHtml(user.email);

  return `
<article class="user-card" data-id="${user.id}">
  <div class="card-header">
    <div class="avatar" aria-hidden="true">${initial}</div>
    <div class="user-meta">
      <h3 class="user-name">${safeUsername}</h3>
      <span class="badge active">ACTIVE</span>
    </div>
  </div>
  <div class="card-body">
    <p class="user-email"><strong>Email:</strong> ${safeEmail}</p>
    <small class="user-id"><strong>ID:</strong> #${user.id}</small>
  </div>
  <div class="card-footer">
    <button type="button" class="btn-delete" data-action="delete" data-id="${user.id}" data-username="${safeUsername}" aria-label="Delete user ${safeUsername}">
      Delete
    </button>
  </div>
</article>
`;
}
