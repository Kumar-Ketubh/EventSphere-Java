import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Navbar from '@/components/Navbar.jsx';
import DateBlock from '@/components/DateBlock.jsx';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { BlurFade } from '@/components/ui/blur-fade';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { cancelBooking, downloadCalendar, getTicket } from '@/services/api.js';
import { formatRange } from '@/utils/dates.js';

/** My ticket: the event, who it is for and the booking code, with a button to print or save as PDF. */
export default function Ticket() {
  const { id } = useParams();
  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [calendarError, setCalendarError] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState('');
  const navigate = useNavigate();
  const panelRef = useRef(null);

  useEffect(() => {
    if (!confirming || !panelRef.current) return;
    const reduce = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    panelRef.current.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'center' });
    panelRef.current.focus({ preventScroll: true });
  }, [confirming]);

  useEffect(() => {
    getTicket(id)
      .then((res) => setTicket(res.data))
      .catch((err) => {
        const status = err.response?.status;
        setError(
          status === 403 || status === 404
            ? 'We could not find that ticket under your account. Open it from My bookings.'
            : 'We could not load your ticket. Check your connection and refresh the page.',
        );
      })
      .finally(() => setLoading(false));
  }, [id]);

  const addToCalendar = async () => {
    setCalendarError('');
    try {
      const res = await downloadCalendar(id);
      const url = URL.createObjectURL(res.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${ticket.bookingCode}.ics`;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      setCalendarError('We could not download the calendar file. Please try again.');
    }
  };

  const started = ticket ? new Date(ticket.eventStartTime) <= new Date() : false;

  const confirmCancel = () => {
    setCancelling(true);
    setCancelError('');
    cancelBooking(id)
      .then(() => navigate('/profile'))
      .catch((err) => {
        setCancelError(err.response?.data?.message || 'We could not cancel your booking. Please try again.');
        setCancelling(false);
      });
  };

  return (
    <div className="min-h-dvh">
      <Navbar />
      <main className="mx-auto max-w-2xl px-5 pt-12 pb-20 sm:px-8">
        <h1 data-print="hide" className="font-display text-6xl leading-none font-extralight tracking-tight text-mist sm:text-7xl">
          My ticket
        </h1>

        {error && (
          <Alert role="alert" className="mt-8 border-clay/50 bg-clay/10">
            <AlertDescription className="text-mist">{error}</AlertDescription>
          </Alert>
        )}
        {loading && (
          <p className="mt-8 text-ash" role="status">
            Loading your ticket...
          </p>
        )}

        {ticket && (
          <BlurFade delay={0.1}>
            <Card data-print="ticket" className="mt-10 gap-6 px-6" aria-labelledby="ticket-heading">
              <div className="flex gap-4">
                <DateBlock iso={ticket.eventStartTime} />
                <div className="min-w-0">
                  <h2 id="ticket-heading" className="font-display text-3xl font-extrabold text-mist">
                    {ticket.eventTitle}
                  </h2>
                  <p className="mt-1 font-semibold text-mist/90">{ticket.eventVenue}</p>
                  <p className="text-sm text-ash tabular-nums">{formatRange(ticket.eventStartTime, ticket.eventEndTime)}</p>
                </div>
              </div>
              <dl className="grid gap-4 sm:grid-cols-2">
                {[
                  ['Name', ticket.studentName],
                  ['Roll number', ticket.rollNumber || 'Not provided'],
                ].map(([term, value]) => (
                  <div key={term} className="min-w-0">
                    <dt className="text-sm text-ash">{term}</dt>
                    <dd className="font-semibold break-words text-mist">{value}</dd>
                  </div>
                ))}
              </dl>
              <div className="rounded-xl border border-sand/30 bg-raised px-4 py-3">
                <p className="text-sm text-ash">Booking code</p>
                <p className="font-display text-3xl font-extrabold tracking-wider text-sand">{ticket.bookingCode}</p>
              </div>
            </Card>
            <div data-print="hide" className="mt-6 flex flex-wrap gap-3">
              <Button variant="accent" size="lg" onClick={() => window.print()}>
                Download ticket
              </Button>
              <Button variant="outline" size="lg" onClick={addToCalendar}>
                Add to calendar
              </Button>
              <Button asChild variant="ghost" size="lg" className="text-mist hover:bg-raised">
                <Link to="/profile">Back to My bookings</Link>
              </Button>
              {!started && !confirming && (
                <Button variant="ghost" size="lg" className="text-clay hover:bg-raised" onClick={() => setConfirming(true)}>
                  Cancel booking
                </Button>
              )}
            </div>
            {calendarError && (
              <Alert role="alert" data-print="hide" className="mt-4 border-clay/50 bg-clay/10">
                <AlertDescription className="text-mist">{calendarError}</AlertDescription>
              </Alert>
            )}
            {confirming && (
              <div ref={panelRef} tabIndex={-1} data-print="hide" className="mt-6 outline-none">
              <Alert role="alertdialog" className="border-clay/50 bg-clay/10">
                <AlertDescription className="text-mist">
                  <p className="font-semibold">Cancel your seat at {ticket.eventTitle}?</p>
                  <p className="mt-1 text-ash">Your booking will be removed and the seat freed for someone else.</p>
                  {cancelError && (
                    <p role="alert" className="mt-2 text-clay">
                      {cancelError}
                    </p>
                  )}
                  <div className="mt-4 flex flex-wrap gap-3">
                    <Button variant="destructive" onClick={confirmCancel} disabled={cancelling}>
                      {cancelling ? 'Cancelling...' : 'Yes, cancel booking'}
                    </Button>
                    <Button variant="ghost" className="text-mist hover:bg-raised" onClick={() => setConfirming(false)} disabled={cancelling}>
                      Keep my seat
                    </Button>
                  </div>
                </AlertDescription>
              </Alert>
              </div>
            )}
          </BlurFade>
        )}
      </main>
    </div>
  );
}
