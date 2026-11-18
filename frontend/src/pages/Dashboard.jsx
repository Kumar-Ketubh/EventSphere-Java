import React, { useEffect, useState, useCallback } from 'react';
import Navbar from '../components/Navbar.jsx';
import EventCard from '../components/EventCard.jsx';
import { getDashboard, registerForEvent } from '../services/api.js';

/**
 * Dashboard Page Component.
 *
 * Fetches dynamic categorized event lists from Spring Boot backend:
 * - Live Events (active now)
 * - Upcoming Events (open for booking)
 * - Previous Events (concluded)
 *
 * @returns {JSX.Element}
 */
function Dashboard() {
  const [dashboardData, setDashboardData] = useState({
    live: [],
    upcoming: [],
    previous: [],
  });
  const [loading, setLoading] = useState(true);
  const [bookingId, setBookingId] = useState(null);
  const [alertInfo, setAlertInfo] = useState(null); // { type: 'success'|'danger', message: '' }

  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');

  const fetchEvents = useCallback(async () => {
    try {
      const response = await getDashboard();
      setDashboardData(response.data);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
      setAlertInfo({
        type: 'danger',
        message: 'Could not connect to backend server. Make sure Spring Boot is running on port 8080.',
      });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const handleRegisterSeat = async (eventId) => {
    if (!currentUser.id) {
      setAlertInfo({ type: 'danger', message: 'User session not found. Please re-login.' });
      return;
    }

    setBookingId(eventId);
    setAlertInfo(null);

    try {
      await registerForEvent(eventId, currentUser.id);
      setAlertInfo({
        type: 'success',
        message: '🎉 Seat successfully booked! You can verify your booking in your Profile.',
      });
      // Refresh event seat counts
      await fetchEvents();
    } catch (err) {
      setAlertInfo({
        type: 'danger',
        message: err.response?.data?.message || 'Seat booking failed. Please try again.',
      });
    } finally {
      setBookingId(null);
    }
  };

  return (
    <div className="min-vh-100 bg-light">
      <Navbar />

      <main className="container py-4">
        {/* Welcome Banner */}
        <div className="bg-white border rounded-3 p-4 mb-4 shadow-sm">
          <div className="row align-items-center">
            <div className="col-md-8">
              <h4 className="fw-bold text-dark mb-1">
                Welcome back, {currentUser.name || 'Attendee'}! 👋
              </h4>
              <p className="text-muted small mb-0">
                Explore campus happenings, reserve seats in advance, and manage your registrations.
              </p>
            </div>
            <div className="col-md-4 text-md-end mt-3 mt-md-0">
              <span className="badge bg-primary-subtle text-primary border border-primary-subtle p-2 px-3 rounded-pill">
                <i className="bi bi-clock-history me-1"></i>
                Real-Time Dynamic Timeline
              </span>
            </div>
          </div>
        </div>

        {/* Global Alert Notification */}
        {alertInfo && (
          <div
            className={`alert alert-${alertInfo.type} alert-dismissible fade show shadow-sm mb-4`}
            role="alert"
          >
            <div className="d-flex align-items-center">
              <i
                className={`bi ${
                  alertInfo.type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill'
                } me-2 fs-5`}
              ></i>
              <div>{alertInfo.message}</div>
            </div>
            <button
              type="button"
              className="btn-close"
              onClick={() => setAlertInfo(null)}
              aria-label="Close"
            ></button>
          </div>
        )}

        {loading ? (
          <div className="text-center py-5">
            <div className="spinner-border text-primary mb-3" role="status"></div>
            <p className="text-muted">Loading campus events...</p>
          </div>
        ) : (
          <>
            {/* 1. Live Events Section */}
            <section className="mb-5">
              <div className="d-flex align-items-center mb-3">
                <h5 className="section-header live mb-0">
                  <i className="bi bi-broadcast text-success me-2"></i>
                  Live Events Now
                </h5>
                <span className="badge bg-success ms-2">
                  {dashboardData.live?.length || 0}
                </span>
              </div>

              {dashboardData.live?.length === 0 ? (
                <div className="card bg-white border-0 shadow-sm p-4 text-center text-muted">
                  <i className="bi bi-calendar-event fs-3 mb-2 text-secondary"></i>
                  <p className="mb-0">No events are happening at this precise moment.</p>
                </div>
              ) : (
                <div className="row g-3">
                  {dashboardData.live.map((event) => (
                    <div key={event.id} className="col-md-6 col-lg-4">
                      <EventCard event={event} type="live" />
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* 2. Upcoming Events Section */}
            <section className="mb-5">
              <div className="d-flex align-items-center mb-3">
                <h5 className="section-header upcoming mb-0">
                  <i className="bi bi-calendar-plus text-primary me-2"></i>
                  Upcoming Events (Registration Open)
                </h5>
                <span className="badge bg-primary ms-2">
                  {dashboardData.upcoming?.length || 0}
                </span>
              </div>

              {dashboardData.upcoming?.length === 0 ? (
                <div className="card bg-white border-0 shadow-sm p-4 text-center text-muted">
                  <i className="bi bi-calendar-check fs-3 mb-2 text-secondary"></i>
                  <p className="mb-0">No upcoming events scheduled right now.</p>
                </div>
              ) : (
                <div className="row g-3">
                  {dashboardData.upcoming.map((event) => (
                    <div key={event.id} className="col-md-6 col-lg-4">
                      <EventCard
                        event={event}
                        type="upcoming"
                        onRegister={handleRegisterSeat}
                        isBooking={bookingId === event.id}
                      />
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* 3. Previous Events Section */}
            <section className="mb-4">
              <div className="d-flex align-items-center mb-3">
                <h5 className="section-header previous mb-0">
                  <i className="bi bi-archive text-secondary me-2"></i>
                  Previous Events (Concluded)
                </h5>
                <span className="badge bg-secondary ms-2">
                  {dashboardData.previous?.length || 0}
                </span>
              </div>

              {dashboardData.previous?.length === 0 ? (
                <div className="card bg-white border-0 shadow-sm p-4 text-center text-muted">
                  <p className="mb-0">No past events found.</p>
                </div>
              ) : (
                <div className="row g-3">
                  {dashboardData.previous.map((event) => (
                    <div key={event.id} className="col-md-6 col-lg-4">
                      <EventCard event={event} type="previous" />
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}

export default Dashboard;
