import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../services/api.js';

/**
 * Login Page Component.
 *
 * Authenticates user credentials and stores the user profile in localStorage.
 *
 * @returns {JSX.Element}
 */
function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('student@college.edu');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await loginUser(email, password);
      // Persist user session to localStorage
      localStorage.setItem('user', JSON.stringify(response.data));
      navigate('/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Login failed. Please verify credentials.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="card auth-card p-4 p-sm-5">
        <div className="text-center mb-4">
          <div className="d-inline-flex align-items-center justify-content-center bg-primary text-white rounded-circle mb-3" style={{ width: '60px', height: '60px' }}>
            <i className="bi bi-calendar2-check-fill fs-3"></i>
          </div>
          <h3 className="fw-bold text-dark mb-1">EventSphere</h3>
          <p className="text-muted small">Campus Event Booking Portal</p>
        </div>

        {error && (
          <div className="alert alert-danger py-2 small d-flex align-items-center mb-3">
            <i className="bi bi-exclamation-octagon-fill me-2 fs-5"></i>
            <div>{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label small fw-semibold text-secondary">Email Address</label>
            <div className="input-group">
              <span className="input-group-text bg-light text-muted">
                <i className="bi bi-envelope-fill"></i>
              </span>
              <input
                type="email"
                className="form-control"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="student@college.edu"
                required
              />
            </div>
          </div>

          <div className="mb-4">
            <label className="form-label small fw-semibold text-secondary">Password</label>
            <div className="input-group">
              <span className="input-group-text bg-light text-muted">
                <i className="bi bi-key-fill"></i>
              </span>
              <input
                type="password"
                className="form-control"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-100 py-2 fw-semibold mb-3 shadow-sm"
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                Signing in...
              </>
            ) : (
              'Sign In'
            )}
          </button>
        </form>

        <div className="card bg-light border-0 p-3 mb-3 text-center">
          <span className="text-muted small">
            <strong>Demo Account:</strong> <br />
            <code>student@college.edu</code> / <code>password123</code>
          </span>
        </div>

        <div className="text-center small text-secondary">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary text-decoration-none fw-semibold">
            Create an Account
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
