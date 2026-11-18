import axios from 'axios';

/**
 * Axios instance configured for the EventSphere Spring Boot REST API.
 * Base URL points to the local Spring Boot backend on port 8080.
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Authentication API ───────────────────────────────────────────────────

/**
 * Registers a new user account.
 * @param {Object} data - { name, email, password, rollNumber }
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const registerUser = (data) => api.post('/auth/register', data);

/**
 * Logs in an existing user.
 * @param {string} email
 * @param {string} password
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const loginUser = (email, password) =>
  api.post('/auth/login', { email, password });

// ─── Events API ───────────────────────────────────────────────────────────

/**
 * Fetches dashboard events dynamically categorized into live, upcoming, and previous.
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const getDashboard = () => api.get('/events/dashboard');

// ─── Seat Registration API ────────────────────────────────────────────────

/**
 * Registers a user for an event (safe seat booking with pessimistic locking).
 * @param {number|string} eventId - Target event ID
 * @param {number|string} userId  - Current user ID
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const registerForEvent = (eventId, userId) =>
  api.post(`/events/${eventId}/register`, { userId });

// ─── Profile API ──────────────────────────────────────────────────────────

/**
 * Fetches the user's profile and confirmed registration records.
 * @param {number|string} userId - Current user ID
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const getProfile = (userId) =>
  api.get('/profile', { params: { userId } });

export default api;
