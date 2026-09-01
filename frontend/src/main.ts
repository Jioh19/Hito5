import { AlertType, generateAlertHtml } from './components/Alert';
import { generateUserCardHtml } from './components/UserCard';
import { LoginUserDTO, RegisterUserDTO } from './models/user';
import { deleteUser, fetchUsers, loginUser, registerUser } from './services/userApi';

// DOM Elements
const registerForm = document.getElementById('register-form') as HTMLFormElement | null;
const regUsernameInput = document.getElementById('txt-reg-username') as HTMLInputElement | null;
const regEmailInput = document.getElementById('txt-reg-email') as HTMLInputElement | null;
const regPasswordInput = document.getElementById('txt-reg-password') as HTMLInputElement | null;
const btnRegisterSubmit = document.getElementById('btn-register-submit') as HTMLButtonElement | null;

const loginForm = document.getElementById('login-form') as HTMLFormElement | null;
const loginUsernameInput = document.getElementById('txt-login-username') as HTMLInputElement | null;
const loginPasswordInput = document.getElementById('txt-login-password') as HTMLInputElement | null;
const btnLoginSubmit = document.getElementById('btn-login-submit') as HTMLButtonElement | null;

const feedbackContainer = document.getElementById('feedback-message');
const listContainer = document.getElementById('users-list');
const userCountBadge = document.getElementById('user-count');
const btnRefresh = document.getElementById('btn-refresh') as HTMLButtonElement | null;

let feedbackTimeout: number | undefined;

/**
 * Display a user feedback notification banner
 */
function showFeedback(message: string, type: AlertType): void {
  if (!feedbackContainer) return;

  if (feedbackTimeout) {
    window.clearTimeout(feedbackTimeout);
  }

  feedbackContainer.className = `feedback ${type} active`;
  feedbackContainer.innerHTML = generateAlertHtml(message, type);

  // Auto-dismiss info and success messages after 5 seconds
  if (type !== 'error') {
    feedbackTimeout = window.setTimeout(() => {
      feedbackContainer.className = 'feedback';
      feedbackContainer.innerHTML = '';
    }, 5000);
  }
}

/**
 * 1. Fetch remote users and render cards in real-time
 */
async function loadAndRenderUsers(): Promise<void> {
  if (!listContainer) return;

  listContainer.innerHTML = "<p class='loading'>Fetching users from PostgreSQL...</p>";

  try {
    const users = await fetchUsers();

    if (userCountBadge) {
      userCountBadge.textContent = users.length.toString();
    }

    if (users.length === 0) {
      listContainer.innerHTML = "<p class='empty-state'>No users registered yet. Create the first user using the form above!</p>";
      return;
    }

    listContainer.innerHTML = users.map(generateUserCardHtml).join('');
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error loading users';
    listContainer.innerHTML = `
      <div class="error-box">
        <p><strong>Error loading data from backend:</strong></p>
        <small>${errorMessage}</small>
      </div>
    `;
    if (userCountBadge) {
      userCountBadge.textContent = '!';
    }
  }
}

/**
 * 2. Handle User Registration Form Submission
 */
if (registerForm) {
  registerForm.addEventListener('submit', async (event: Event) => {
    event.preventDefault();

    if (!regUsernameInput || !regEmailInput || !regPasswordInput || !btnRegisterSubmit) {
      return;
    }

    const username = regUsernameInput.value.trim();
    const email = regEmailInput.value.trim().toLowerCase();
    const password = regPasswordInput.value;

    // Strict client-side validation
    const usernameRegex = /^[a-zA-Z0-9]{4,}$/;
    if (!usernameRegex.test(username)) {
      showFeedback('Username must be at least 4 alphanumeric characters (no spaces or special chars).', 'error');
      regUsernameInput.focus();
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showFeedback('Please provide a valid email address.', 'error');
      regEmailInput.focus();
      return;
    }

    if (!password || password.trim().length === 0) {
      showFeedback('Password cannot be empty.', 'error');
      regPasswordInput.focus();
      return;
    }

    const payload: RegisterUserDTO = { username, email, password };

    try {
      btnRegisterSubmit.disabled = true;
      showFeedback('Processing registration in PostgreSQL...', 'info');

      const createdUser = await registerUser(payload);

      showFeedback(`User "${createdUser.username}" successfully registered and stored in PostgreSQL! (ID: #${createdUser.id})`, 'success');

      // Reset form fields
      registerForm.reset();

      // Refresh list in real time
      await loadAndRenderUsers();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : 'Registration failed';
      showFeedback(errorMessage, 'error');
    } finally {
      btnRegisterSubmit.disabled = false;
    }
  });
}

/**
 * 3. Handle User Login / Authentication Check
 */
if (loginForm) {
  loginForm.addEventListener('submit', async (event: Event) => {
    event.preventDefault();

    if (!loginUsernameInput || !loginPasswordInput || !btnLoginSubmit) {
      return;
    }

    const username = loginUsernameInput.value.trim();
    const password = loginPasswordInput.value;

    if (!username) {
      showFeedback('Please enter your username to authenticate.', 'error');
      loginUsernameInput.focus();
      return;
    }

    if (!password) {
      showFeedback('Please enter your password to authenticate.', 'error');
      loginPasswordInput.focus();
      return;
    }

    const payload: LoginUserDTO = { username, password };

    try {
      btnLoginSubmit.disabled = true;
      showFeedback('Authenticating credentials with backend...', 'info');

      const authenticatedUser = await loginUser(payload);

      showFeedback(`Authentication successful! Welcome back, ${authenticatedUser.username} (ID: #${authenticatedUser.id}, Email: ${authenticatedUser.email})`, 'success');
      loginForm.reset();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : 'Authentication failed';
      showFeedback(errorMessage, 'error');
    } finally {
      btnLoginSubmit.disabled = false;
    }
  });
}

/**
 * 4. Handle Card Actions (Event Delegation for Deletion)
 */
if (listContainer) {
  listContainer.addEventListener('click', async (event: Event) => {
    const target = event.target as HTMLElement | null;
    if (!target) return;

    const deleteBtn = target.closest('button[data-action="delete"]') as HTMLButtonElement | null;
    if (!deleteBtn) return;

    const userIdStr = deleteBtn.getAttribute('data-id');
    const username = deleteBtn.getAttribute('data-username') || 'this user';
    if (!userIdStr) return;

    const userId = parseInt(userIdStr, 10);
    if (isNaN(userId)) return;

    const confirmed = window.confirm(`Are you sure you want to delete user "${username}" (ID: #${userId})?`);
    if (!confirmed) return;

    try {
      deleteBtn.disabled = true;
      showFeedback(`Deleting user "${username}" from database...`, 'info');

      await deleteUser(userId);

      showFeedback(`User "${username}" (ID: #${userId}) has been deleted successfully.`, 'success');

      // Reload users list
      await loadAndRenderUsers();
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to delete user';
      showFeedback(errorMessage, 'error');
      deleteBtn.disabled = false;
    }
  });
}

/**
 * 5. Manual Refresh Button
 */
if (btnRefresh) {
  btnRefresh.addEventListener('click', () => {
    void loadAndRenderUsers();
  });
}

/**
 * 6. Initialization
 */
document.addEventListener('DOMContentLoaded', () => {
  void loadAndRenderUsers();
});
