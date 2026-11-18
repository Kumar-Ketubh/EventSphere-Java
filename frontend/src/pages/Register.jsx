import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../services/api.js';

/**
 * Registration Page Component.
 *
 * Captures user profile fields and creates a new account via /api/auth/register.
 *
 * @returns {JSX.Element}
 */
function Register() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    rollNumber: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await registerUser(formData);
      localStorage.setItem('user', JSON.stringify(response.data));
      navigate('/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Registration failed. Please try again.'
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
            <i className="bi bi-person-plus-fill fs-3"></i>
          </div>
          <h3 className="fw-bold text-dark mb-1">Create Account</h3>
          <p className="text-muted small">Register as Attendee (Student/Faculty)</p>
        </div>

        {error && (
          <div className="alert alert-danger py-2 small d-flex align-items-center mb-3">
            <i className="bi bi-exclamation-octagon-fill me-2 fs-5"></i>
            <div>{error}</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label small fw-semibold text-secondary">Full Name</label>
            <div className="input-group">
              <span className="input-group-text bg-light text-muted">
                <i className="bi bi-person-fill"></i>
              </span>
              <input
                type="text"
                name="name"
                className="form-control"
                value={formData.name}
                onChange={handleChange}
                placeholder="Alex Johnson"
                required
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label small fw-semibold text-secondary">Roll Number / Faculty ID</label>
            <div className="input-group">
              <span className="input-group-text bg-light text-muted">
                <i className="bi bi-card-text"></i>
              </span>
              <input
                type="text"
                name="rollNumber"
                className="form-control"
                value={formData.rollNumber}
                onChange={handleChange}
                placeholder="CS2024001"
                required
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label small fw-semibold text-secondary">College Email Address</label>
            <div className="input-group">
              <span className="input-group-text bg-light text-muted">
                <i className="bi bi-envelope-fill"></i>
              </span>
              <input
                type="email"
                name="email"
                className="form-control"
                value={formData.email}
                onChange={handleChange}
                placeholder="alex@college.edu"
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
                name="password"
                className="form-control"
                value={formData.password}
                onChange={handleChange}
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
                Creating account...
              </>
            ) : (
              'Complete Registration'
            )}
          </button>
        </form>

        <div className="text-center small text-secondary">
          Already registered?{' '}
          <Link to="/" className="text-primary text-decoration-none fw-semibold">
            Sign In here
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
