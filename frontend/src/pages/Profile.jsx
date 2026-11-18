import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar.jsx';
import { getProfile } from '../services/api.js';

/**
 * Profile Page Component.
 *
 * Displays user identity and confirmed event registrations for cross-verification.
 *
 * @returns {JSX.Element}
 */
function Profile() {
  const navigate = useNavigate();
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    if (!currentUser.id) {
      navigate('/');
      return;
    }

    const loadProfile = async () => {
      try {
        const response = await getProfile(currentUser.id);
        setProfileData(response.data);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
        setError('Failed to retrieve profile data from server.');
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [currentUser.id, navigate]);

  const formatDate = (isoString) => {
    if (!isoString) return '—';
    const date = new Date(isoString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="min-vh-100 bg-light">
      <Navbar />

      <main className="container py-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 className="fw-bold text-dark mb-1">
              <i className="bi bi-person-badge-fill text-primary me-2"></i>
              Attendee Profile
            </h3>
            <p className="text-muted small mb-0">
              Manage your identity and verify all confirmed seat bookings.
            </p>
          </div>
          <Link to="/dashboard" className="btn btn-outline-primary btn-sm">
            <i className="bi bi-arrow-left me-1"></i> Back to Dashboard
          </Link>
        </div>

        {error && (
          <div className="alert alert-danger py-2 small d-flex align-items-center mb-4">
            <i className="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
            <div>{error}</div>
          </div>
        )}

        {loading ? (
          <div className="text-center py-5">
            <div className="spinner-border text-primary mb-3" role="status"></div>
            <p className="text-muted">Loading your profile and bookings...</p>
          </div>
        ) : profileData ? (
          <div className="row g-4">
            {/* User Identity Card */}
            <div className="col-lg-4">
              <div className="card profile-card p-4 h-100">
                <div className="text-center mb-4">
                  <div
                    className="d-inline-flex align-items-center justify-content-center bg-primary text-white rounded-circle mb-3 shadow-sm"
                    style={{ width: '80px', height: '80px', fontSize: '2rem' }}
                  >
                    {profileData.user.name?.charAt(0).toUpperCase()}
                  </div>
                  <h5 className="fw-bold mb-1">{profileData.user.name}</h5>
                  <span className="badge bg-secondary-subtle text-secondary border">
                    {profileData.user.rollNumber || 'College Member'}
                  </span>
                </div>

                <ul className="list-group list-group-flush small">
                  <li className="list-group-item d-flex justify-content-between px-0 py-2">
                    <span className="text-muted">User ID:</span>
                    <span className="fw-semibold">#{profileData.user.id}</span>
                  </li>
                  <li className="list-group-item d-flex justify-content-between px-0 py-2">
                    <span className="text-muted">Email:</span>
                    <span className="fw-semibold">{profileData.user.email}</span>
                  </li>
                  <li className="list-group-item d-flex justify-content-between px-0 py-2">
                    <span className="text-muted">Roll / ID:</span>
                    <span className="fw-semibold">{profileData.user.rollNumber || 'N/A'}</span>
                  </li>
                  <li className="list-group-item d-flex justify-content-between px-0 py-2">
                    <span className="text-muted">Total Bookings:</span>
                    <span className="badge bg-success rounded-pill px-2">
                      {profileData.registrations?.length || 0}
                    </span>
                  </li>
                </ul>
              </div>
            </div>

            {/* Confirmed Registrations Verification Table */}
            <div className="col-lg-8">
              <div className="card profile-card shadow-sm">
                <div className="card-header bg-white border-bottom py-3 d-flex justify-content-between align-items-center">
                  <h5 className="mb-0 fw-bold text-dark">
                    <i className="bi bi-ticket-detailed-fill text-success me-2"></i>
                    Confirmed Event Registrations
                  </h5>
                  <span className="badge bg-success">
                    {profileData.registrations?.length || 0} Registered
                  </span>
                </div>

                <div className="card-body p-0">
                  {profileData.registrations?.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                      <i className="bi bi-ticket-perforated fs-1 text-secondary mb-2"></i>
                      <p className="mb-2">No event bookings found.</p>
                      <Link to="/dashboard" className="btn btn-primary btn-sm">
                        Browse & Book Events
                      </Link>
                    </div>
                  ) : (
                    <div className="table-responsive">
                      <table className="table table-hover align-middle mb-0">
                        <thead className="table-light small">
                          <tr>
                            <th className="ps-3">Reg ID</th>
                            <th>Event Title</th>
                            <th>Venue</th>
                            <th>Event Timing</th>
                            <th>Booked At</th>
                            <th className="text-center pe-3">Status</th>
                          </tr>
                        </thead>
                        <tbody className="small">
                          {profileData.registrations.map((reg) => (
                            <tr key={reg.registrationId}>
                              <td className="ps-3 fw-bold text-secondary">
                                #{reg.registrationId}
                              </td>
                              <td className="fw-semibold text-dark">
                                {reg.eventTitle}
                              </td>
                              <td className="text-muted">
                                <i className="bi bi-geo-alt me-1 text-danger"></i>
                                {reg.eventVenue}
                              </td>
                              <td className="text-muted">
                                {formatDate(reg.eventStartTime)}
                              </td>
                              <td className="text-muted">
                                {formatDate(reg.registeredAt)}
                              </td>
                              <td className="text-center pe-3">
                                <span className="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                  <i className="bi bi-patch-check-fill me-1"></i>
                                  Confirmed
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        ) : null}
      </main>
    </div>
  );
}

export default Profile;
