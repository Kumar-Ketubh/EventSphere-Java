import React from 'react';

/**
 * Event Card Component.
 *
 * Renders an event card configured for Live, Upcoming, or Previous timelines.
 * Handles the "Register Seat" trigger for upcoming events.
 *
 * @param {Object} props
 * @param {Object} props.event - The event data object
 * @param {'live'|'upcoming'|'previous'} props.type - The status category
 * @param {Function} [props.onRegister] - Callback function to book a seat
 * @param {boolean} [props.isBooking] - True if this specific card is currently booking
 * @returns {JSX.Element}
 */
function EventCard({ event, type, onRegister, isBooking }) {
  const availableSeats = event.totalSeats - event.bookedSeats;
  const isSoldOut = availableSeats <= 0;

  const formatDate = (isoString) => {
    if (!isoString) return '';
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
    <div className="card event-card h-100 d-flex flex-column">
      <div className="card-body d-flex flex-column">
        {/* Header with Title and Badge */}
        <div className="d-flex justify-content-between align-items-start mb-2">
          <h5 className="card-title fw-bold text-dark mb-0 me-2">{event.title}</h5>
          {type === 'live' && (
            <span className="badge bg-success badge-live text-uppercase px-2 py-1">
              <i className="bi bi-record-circle-fill me-1"></i> Live
            </span>
          )}
          {type === 'upcoming' && (
            <span className="badge bg-primary text-uppercase px-2 py-1">
              Upcoming
            </span>
          )}
          {type === 'previous' && (
            <span className="badge bg-secondary text-uppercase px-2 py-1">
              Ended
            </span>
          )}
        </div>

        {/* Description */}
        <p className="card-text text-muted small flex-grow-1 mb-3">
          {event.description}
        </p>

        {/* Metadata Details */}
        <div className="border-top pt-2 mt-auto small">
          <div className="text-secondary mb-1">
            <i className="bi bi-geo-alt-fill text-danger me-1"></i>
            <span>{event.venue}</span>
          </div>
          <div className="text-secondary mb-1">
            <i className="bi bi-clock-fill text-primary me-1"></i>
            <span>Starts: {formatDate(event.startTime)}</span>
          </div>
          <div className="text-secondary mb-2">
            <i className="bi bi-hourglass-bottom text-warning me-1"></i>
            <span>Ends: {formatDate(event.endTime)}</span>
          </div>

          {/* Seat Capacity Status */}
          <div className="d-flex justify-content-between align-items-center mb-2">
            <span className="text-muted">Seats Available:</span>
            {type === 'previous' ? (
              <span className="badge bg-light text-secondary border">
                {event.bookedSeats} / {event.totalSeats} attended
              </span>
            ) : isSoldOut ? (
              <span className="badge bg-danger">Housefull (0 left)</span>
            ) : (
              <span className="badge bg-info-subtle text-info-emphasis border border-info-subtle fw-semibold">
                {availableSeats} / {event.totalSeats} seats
              </span>
            )}
          </div>

          {/* Action Button */}
          {type === 'upcoming' && (
            <button
              onClick={() => onRegister(event.id)}
              disabled={isSoldOut || isBooking}
              className={`btn w-100 fw-semibold mt-1 ${
                isSoldOut ? 'btn-secondary disabled' : 'btn-primary'
              }`}
            >
              {isBooking ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                  Reserving Seat...
                </>
              ) : isSoldOut ? (
                'Sold Out'
              ) : (
                <>
                  <i className="bi bi-ticket-perforated-fill me-1"></i> Register Seat
                </>
              )}
            </button>
          )}

          {type === 'live' && (
            <button className="btn btn-outline-success w-100 fw-semibold mt-1 disabled">
              <i className="bi bi-broadcast me-1"></i> In Progress
            </button>
          )}

          {type === 'previous' && (
            <button className="btn btn-outline-secondary w-100 fw-semibold mt-1 disabled">
              <i className="bi bi-check2-all me-1"></i> Event Concluded
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default EventCard;
