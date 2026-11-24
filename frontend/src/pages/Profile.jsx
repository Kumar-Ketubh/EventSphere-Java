import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '@/components/Navbar.jsx';
import DateBlock from '@/components/DateBlock.jsx';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AnimatedList } from '@/components/ui/animated-list';
import { BlurFade } from '@/components/ui/blur-fade';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { TextAnimate } from '@/components/ui/text-animate';
import { getProfile } from '@/services/api.js';
import { formatMoment, formatRange } from '@/utils/dates.js';

/** My bookings: the student's details, then every event they hold a seat for. */
export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getProfile()
      .then((res) => setProfile(res.data))
      .catch(() =>
        setError('We could not load your bookings. Check your connection and refresh the page. If it keeps failing, log out and log in again.'),
      )
      .finally(() => setLoading(false));
  }, []);

  const registrations = profile?.registrations || [];
  const user = profile?.user;

  return (
    <div className="min-h-dvh">
      <Navbar />
      <main className="mx-auto max-w-3xl px-5 pt-12 pb-20 sm:px-8">
        <h1 className="font-display text-6xl leading-none tracking-tight text-mist sm:text-7xl">
          <TextAnimate as="span" by="word" animation="blurInUp" once startOnView={false} className="font-extralight">
            My bookings
          </TextAnimate>
        </h1>

        {error && (
          <Alert role="alert" className="mt-8 border-clay/50 bg-clay/10">
            <AlertDescription className="text-mist">{error}</AlertDescription>
          </Alert>
        )}
        {loading && (
          <p className="mt-8 text-ash" role="status">
            Loading your bookings...
          </p>
        )}

        {!loading && user && (
          <>
            <BlurFade delay={0.15}>
              <Card className="mt-10 gap-4 px-6" aria-labelledby="details-heading">
                <h2 id="details-heading" className="font-display text-2xl font-extrabold text-mist">
                  Your details
                </h2>
                <dl className="grid gap-4 sm:grid-cols-3">
                  {[
                    ['Name', user.name],
                    ['Email', user.email],
                    ['Roll number', user.rollNumber || 'Not provided'],
                  ].map(([term, value]) => (
                    <div key={term} className="min-w-0">
                      <dt className="text-sm text-ash">{term}</dt>
                      <dd className="font-semibold break-words text-mist">{value}</dd>
                    </div>
                  ))}
                </dl>
              </Card>
            </BlurFade>

            <section aria-labelledby="bookings-heading" className="mt-14">
              <h2 id="bookings-heading" className="font-display text-3xl font-extrabold text-mist">
                <span className="text-sand">{registrations.length}</span>{' '}
                {registrations.length === 1 ? 'booked event' : 'booked events'}
              </h2>

              {registrations.length === 0 ? (
                <Card className="mt-5 items-start gap-4 px-6">
                  <p className="text-mist">You have not booked any events yet.</p>
                  <Button asChild variant="accent" size="lg">
                    <Link to="/dashboard">Browse events</Link>
                  </Button>
                </Card>
              ) : (
                <AnimatedList delay={250} className="mt-5 items-stretch gap-4">
                  {registrations.map((reg) => (
                    <Card key={reg.registrationId} className="flex-row gap-4 px-5 py-4">
                      <DateBlock iso={reg.eventStartTime} />
                      <div className="min-w-0">
                        <h3 className="font-display text-xl font-extrabold text-mist">{reg.eventTitle}</h3>
                        <p className="text-sm font-semibold text-mist/90">{reg.eventVenue}</p>
                        <p className="text-xs text-ash tabular-nums">{formatRange(reg.eventStartTime, reg.eventEndTime)}</p>
                        <p className="mt-2 text-sm text-sand">Booked {formatMoment(reg.registeredAt)}</p>
                        <Button asChild variant="accent" size="sm" className="mt-3">
                          <Link to={`/tickets/${reg.registrationId}`}>View ticket</Link>
                        </Button>
                      </div>
                    </Card>
                  ))}
                </AnimatedList>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}
