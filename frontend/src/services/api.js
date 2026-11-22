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

// Attach the login token to every request so the server knows who is calling.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If the token is missing or expired, the server answers 401: clear it and go to login.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthCall = error.config?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

/** Saves the { token, user } returned by login or sign-up. */
export const saveSession = ({ token, user }) => {
  localStorage.setItem('token', token);
  localStorage.setItem('user', JSON.stringify(user));
};

/** Clears the saved login. */
export const clearSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

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

/** What is on now and coming up, without logging in. Used by the landing page. */
export const getPublicEvents = () => api.get('/events/public');

// ─── Seat Registration API ────────────────────────────────────────────────

/**
 * Books a seat at an event for the logged-in user (safe seat booking with pessimistic locking).
 * @param {number|string} eventId - Target event ID
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const registerForEvent = (eventId) =>
  api.post(`/events/${eventId}/registrations`);

// ─── Profile API ──────────────────────────────────────────────────────────

/**
 * Fetches the logged-in user's profile and confirmed registration records.
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const getProfile = () => api.get('/me/profile');

// ─── Ticket API ───────────────────────────────────────────────────────────

/** Fetches one of the logged-in user's tickets (event, student, booking code). */
export const getTicket = (id) => api.get(`/registrations/${id}`);

export default api;
