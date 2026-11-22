import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import confetti from 'canvas-confetti';
import Navbar from '@/components/Navbar.jsx';
import EventCard from '@/components/EventCard.jsx';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { BlurFade } from '@/components/ui/blur-fade';
import { Button } from '@/components/ui/button';
import { TextAnimate } from '@/components/ui/text-animate';
import { cn } from '@/lib/utils';
import { token } from '@/lib/theme';
import { getDashboard, getProfile, registerForEvent } from '@/services/api.js';


function readFirstName() {
  try {
    const user = JSON.parse(localStorage.getItem('user'));
    return (user && typeof user.name === 'string' && user.name.trim().split(/\s+/)[0]) || '';
  } catch {
    return '';
  }
}

function Section({ title, events, empty, children }) {
  return (
    <section className="mt-14">
      <BlurFade delay={0.35}>
        <h2 className="font-display text-3xl font-extrabold text-mist">
          {title} <span className="font-extralight text-ash">({events.length})</span>
        </h2>
      </BlurFade>
      {events.length === 0 ? (
        <p className="mt-3 text-ash">{empty}</p>
      ) : (
        <ul className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">{children}</ul>
      )}
    </section>
  );
}

/** Dashboard: what is on now, coming up (bookable) and past. */
export default function Dashboard() {
  const [data, setData] = useState({ live: [], upcoming: [], previous: [] });
  const [loading, setLoading] = useState(true);
  const [bookingId, setBookingId] = useState(null);
  const [bookedIds, setBookedIds] = useState(() => new Set());
  const [alert, setAlert] = useState(null); // { type: 'success' | 'error', message }
  const firstName = readFirstName();
  const markBooked = (id) => setBookedIds((prev) => new Set(prev).add(id));

  const fetchEvents = useCallback(async () => {
    try {
      const response = await getDashboard();
      setData(response.data);
    } catch {
      setAlert({ type: 'error', message: 'The events list did not load. Check your connection and refresh the page.' });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEvents();
    // Which events this student already booked, so their tickets say so.
    getProfile()
      .then((res) => setBookedIds(new Set((res.data.registrations || []).map((r) => r.eventId))))
      .catch(() => {});
  }, [fetchEvents]);

  const handleBook = async (eventId) => {
    setBookingId(eventId);
    setAlert(null);
    try {
      const res = await registerForEvent(eventId);
      markBooked(eventId);
      setAlert({ type: 'success', message: 'Seat booked. You can find it under My bookings.', registrationId: res.data?.registrationId });
      if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        confetti({ particleCount: 90, spread: 70, origin: { y: 0.7 }, colors: ['sand', 'ash', 'mist'].map(token) });
      }
      await fetchEvents();
    } catch (err) {
      if (err.response?.status === 409) markBooked(eventId);
      setAlert({ type: 'error', message: err.response?.data?.message || 'Seat booking failed. Please try again.' });
    } finally {
      setBookingId(null);
    }
  };

  const live = data.live || [];
  const upcoming = data.upcoming || [];
  const previous = data.previous || [];
  const isError = alert?.type === 'error';

  return (
    <div className="min-h-dvh">
      <Navbar />
      <main className="mx-auto max-w-6xl px-5 pt-12 pb-20 sm:px-8">
        <h1 className="font-display text-6xl leading-none tracking-tight text-mist sm:text-7xl">
          <TextAnimate as="span" by="word" animation="blurInUp" once startOnView={false} className="font-extralight">
            {firstName ? `Hi, ${firstName}` : 'Hi there'}
          </TextAnimate>
        </h1>
        <BlurFade delay={0.3}>
          <p className="mt-4 max-w-xl text-ash">Pick an upcoming event and book your seat. Your seats are under My bookings.</p>
        </BlurFade>

        {alert && (
          <BlurFade>
            <Alert
              role={isError ? 'alert' : 'status'}
              className={cn('mt-8 flex items-center justify-between gap-4', isError ? 'border-clay/50 bg-clay/10' : 'border-sand/40 bg-sand/10')}
            >
              <AlertDescription className="text-mist">
                <span className="font-semibold">{isError ? 'Booking failed: ' : 'Booked: '}</span>
                {alert.message}
                {alert.registrationId && (
                  <Link to={`/tickets/${alert.registrationId}`} className="ml-2 font-semibold text-sand underline underline-offset-4">
                    View ticket
                  </Link>
                )}
              </AlertDescription>
              <Button variant="ghost" size="sm" onClick={() => setAlert(null)} className="text-mist hover:bg-raised">
                Dismiss
              </Button>
            </Alert>
          </BlurFade>
        )}

        {loading ? (
          <p className="mt-14 text-ash" role="status">
            Loading events...
          </p>
        ) : (
          <>
            <Section title="On now" events={live} empty="Nothing on right now. Check what is coming up below.">
              {live.map((e, i) => (
                <BlurFade key={e.id} delay={0.45 + i * 0.08}>
                  <li className="h-full">
                    <EventCard event={e} type="live" />
                  </li>
                </BlurFade>
              ))}
            </Section>
            <Section title="Coming up" events={upcoming} empty="No upcoming events yet. Check back soon for new ones.">
              {upcoming.map((e, i) => (
                <BlurFade key={e.id} delay={0.45 + i * 0.08}>
                  <li className="h-full">
                    <EventCard
                      event={e}
                      type="upcoming"
                      onRegister={handleBook}
                      isBooking={bookingId === e.id}
                      isBooked={bookedIds.has(e.id)}
                    />
                  </li>
                </BlurFade>
              ))}
            </Section>
            <Section title="Past events" events={previous} empty="No past events yet.">
              {previous.map((e, i) => (
                <BlurFade key={e.id} delay={0.45 + i * 0.08}>
                  <li className="h-full">
                    <EventCard event={e} type="previous" />
                  </li>
                </BlurFade>
              ))}
            </Section>
          </>
        )}
      </main>
    </div>
  );
}
