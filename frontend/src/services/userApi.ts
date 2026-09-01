import { ApiErrorResponse, LoginUserDTO, RegisterUserDTO, User } from '../models/user';

const BASE_URL: string = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080/api/v1/users';

/**
 * Helper to extract error message from failed HTTP response
 */
async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const errorBody = (await response.json()) as ApiErrorResponse;
    if (errorBody && errorBody.message) {
      return errorBody.message;
    }
  } catch {
    // If not valid JSON, use standard HTTP status
  }
  return `Server Error: HTTP ${response.status} (${response.statusText})`;
}

/**
 * Fetch all registered users
 */
export async function fetchUsers(): Promise<User[]> {
  const response = await fetch(BASE_URL);
  if (!response.ok) {
    const message = await parseErrorMessage(response);
    throw new Error(message || `Failed to load users: HTTP ${response.status}`);
  }
  return (await response.json()) as User[];
}

/**
 * Get user by unique ID
 */
export async function getUserById(id: number): Promise<User> {
  const response = await fetch(`${BASE_URL}/${id}`);
  if (!response.ok) {
    const message = await parseErrorMessage(response);
    throw new Error(message || `User not found (ID: ${id})`);
  }
  return (await response.json()) as User;
}

/**
 * Register a new user
 */
export async function registerUser(dto: RegisterUserDTO): Promise<User> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response);
    throw new Error(message);
  }

  return (await response.json()) as User;
}

/**
 * Authenticate user credentials
 */
export async function loginUser(dto: LoginUserDTO): Promise<User> {
  const response = await fetch(`${BASE_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dto),
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response);
    throw new Error(message);
  }

  return (await response.json()) as User;
}

/**
 * Delete a user by ID
 */
export async function deleteUser(id: number): Promise<void> {
  const response = await fetch(`${BASE_URL}/${id}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    const message = await parseErrorMessage(response);
    throw new Error(message || `Failed to delete user ID ${id}`);
  }
}
