import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';

/**
 * Navigation Bar Component.
 *
 * Provides links to Dashboard and Profile, shows the logged-in user's name,
 * and allows logging out to clear the session.
 *
 * @returns {JSX.Element}
 */
function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm py-2">
      <div className="container">
        <Link className="navbar-brand d-flex align-items-center gap-2" to="/dashboard">
          <i className="bi bi-calendar-event text-primary"></i>
          <span>EventSphere</span>
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <Link
                className={`nav-link ${location.pathname === '/dashboard' ? 'active text-primary fw-semibold' : ''}`}
                to="/dashboard"
              >
                <i className="bi bi-grid-fill me-1"></i> Dashboard
              </Link>
            </li>
            <li className="nav-item">
              <Link
                className={`nav-link ${location.pathname === '/profile' ? 'active text-primary fw-semibold' : ''}`}
                to="/profile"
              >
                <i className="bi bi-person-circle me-1"></i> My Profile
              </Link>
            </li>
          </ul>

          <div className="d-flex align-items-center gap-3">
            <div className="text-white-50 small">
              <i className="bi bi-person-fill text-light me-1"></i>
              <span className="text-light fw-medium">{user.name || 'User'}</span>
            </div>
            <button
              onClick={handleLogout}
              className="btn btn-outline-danger btn-sm px-3"
            >
              <i className="bi bi-box-arrow-right me-1"></i> Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
