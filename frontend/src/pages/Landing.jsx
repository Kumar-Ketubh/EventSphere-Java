import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CalendarCheck, Mic, PartyPopper, Search, Ticket, Wrench } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { AnimatedShinyText } from '@/components/ui/animated-shiny-text';
import { BentoCard, BentoGrid } from '@/components/ui/bento-grid';
import { BlurFade } from '@/components/ui/blur-fade';
import { BorderBeam } from '@/components/ui/border-beam';
import { Marquee } from '@/components/ui/marquee';
import { NumberTicker } from '@/components/ui/number-ticker';
import { ScrollProgress } from '@/components/ui/scroll-progress';
import { ShimmerButton } from '@/components/ui/shimmer-button';
import { TextAnimate } from '@/components/ui/text-animate';
import { WordRotate } from '@/components/ui/word-rotate';
import { cn } from '@/lib/utils';
import { cssVar } from '@/lib/theme';
import { getPublicEvents } from '@/services/api.js';
import { dateBlock, formatMoment } from '@/utils/dates.js';

const SAND = cssVar('sand');
const ASH = cssVar('ash');
const SAND_LIGHT = cssVar('sand-light');

/** Picks an icon from the event's title, so the grid reads at a glance. */
function iconFor(title = '') {
  const t = title.toLowerCase();
  if (/fest|cultural|music|night/.test(t)) return PartyPopper;
  if (/workshop|lab|hands|robot|hack/.test(t)) return Wrench;
  return Mic;
}

/** Column spans that always fill the 3-column grid, whatever the number of events (up to 5). */
const SPANS = { 1: [3], 2: [2, 1], 3: [2, 1, 3], 4: [2, 1, 1, 2], 5: [2, 1, 1, 1, 1] };
const SPAN_CLASS = { 1: 'md:col-span-1', 2: 'md:col-span-2', 3: 'md:col-span-3' };

function seatsLeft(e) {
  return Math.max(0, e.totalSeats - e.bookedSeats);
}

const STEPS = [
  { Icon: Search, title: 'Find', text: 'Every talk, workshop and fest on campus is in one list, with the seats left.' },
  { Icon: Ticket, title: 'Book', text: 'One tap holds your seat. It shows up under My bookings straight away.' },
  { Icon: CalendarCheck, title: 'Turn up', text: 'Go on the day. Your booking is saved against your name and roll number.' },
];

/** The ticker of real events under the hero. */
function EventTicker({ events }) {
  if (!events.length) return null;
  return (
    <div className="relative mt-20 border-y border-line bg-surface/60 py-4">
      <Marquee pauseOnHover className="[--duration:40s] [--gap:3rem]">
        {events.map((e) => (
          <span key={e.id} className="flex items-center gap-3 whitespace-nowrap text-base">
            <span className={cn('size-2 rounded-full', e.live ? 'bg-clay' : 'bg-sand')} aria-hidden="true" />
            <span className="font-semibold text-mist">{e.title}</span>
            <span className="text-ash">{e.live ? 'On now' : formatMoment(e.startTime)}</span>
          </span>
        ))}
      </Marquee>
      <div className="pointer-events-none absolute inset-y-0 left-0 w-24 bg-linear-to-r from-void" aria-hidden="true" />
      <div className="pointer-events-none absolute inset-y-0 right-0 w-24 bg-linear-to-l from-void" aria-hidden="true" />
    </div>
  );
}

/**
 * Landing page: calm black and sand. Real events from the backend fill the ticker and the
 * "This week" grid; booking needs a login.
 */
export default function Landing() {
  const loggedIn = Boolean(localStorage.getItem('token'));
  const [data, setData] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    getPublicEvents()
      .then((res) => setData(res.data))
      .catch(() => setFailed(true));
  }, []);

  const events = useMemo(() => {
    if (!data) return [];
    return [
      ...(data.live || []).map((e) => ({ ...e, live: true })),
      ...(data.upcoming || []).map((e) => ({ ...e, live: false })),
    ];
  }, [data]);

  const bookTo = loggedIn ? '/dashboard' : '/login';
  const totalSeats = events.filter((e) => !e.live).reduce((n, e) => n + seatsLeft(e), 0);

  return (
    <div className="min-h-dvh text-mist">
      <ScrollProgress className="h-0.5 bg-none bg-sand" />

      {/* Hero */}
      <section className="relative overflow-hidden bg-hero">

        <header className="relative z-10 mx-auto flex max-w-6xl items-center justify-between px-5 py-6 sm:px-8">
          <Link to="/" className="font-display text-2xl font-extrabold text-mist">
            EventSphere
          </Link>
          <nav aria-label="Main" className="flex items-center gap-2">
            <a href="#this-week" className="hidden px-3 py-2 text-ash hover:text-mist sm:inline">
              This week
            </a>
            {loggedIn ? (
              <Button asChild variant="accent" size="lg">
                <Link to="/dashboard">My events</Link>
              </Button>
            ) : (
              <>
                <Link to="/login" className="px-3 py-2 text-ash hover:text-mist">
                  Log in
                </Link>
                <Button asChild variant="accent" size="lg">
                  <Link to="/register">Create account</Link>
                </Button>
              </>
            )}
          </nav>
        </header>

        <div className="relative z-10 mx-auto max-w-6xl px-5 pt-16 pb-8 sm:px-8 sm:pt-24">
          <BlurFade delay={0.05}>
            <div className="inline-flex rounded-full border border-line bg-surface/70 px-4 py-1.5">
              <AnimatedShinyText className="mx-0 max-w-none text-sm text-ash">
                {data ? `${events.length} events on campus this week` : 'Campus events, one list'}
              </AnimatedShinyText>
            </div>
          </BlurFade>

          <h1 className="mt-8 max-w-5xl font-display text-5xl leading-[1.02] tracking-tight sm:text-6xl lg:text-7xl">
            <TextAnimate as="span" by="word" animation="blurInUp" once className="block font-extralight" startOnView={false}>
              Book your seat at every campus
            </TextAnimate>
            <span className="mt-1 block font-extrabold text-sand">
              <WordRotate words={['talk.', 'workshop.', 'fest.']} duration={2600} className="font-display" />
            </span>
          </h1>

          <BlurFade delay={0.5}>
            <p className="mt-8 max-w-xl text-lg text-ash">
              See what is on this week, how many seats are left, and book with your college email.
            </p>
          </BlurFade>

          <BlurFade delay={0.65}>
            <div className="mt-10 flex flex-wrap items-center gap-4">
              <Link to={bookTo}>
                <ShimmerButton
                  background={SAND}
                  shimmerColor={SAND_LIGHT}
                  borderRadius="12px"
                  className="h-12 px-7 text-base font-semibold text-void"
                >
                  {loggedIn ? 'Book a seat' : 'Log in to book'}
                </ShimmerButton>
              </Link>
              {!loggedIn && (
                <Link to="/register" className="text-mist underline decoration-sand underline-offset-4 hover:text-sand">
                  New here? Create account
                </Link>
              )}
            </div>
          </BlurFade>
        </div>

        <EventTicker events={events} />
      </section>

      {/* This week */}
      <section id="this-week" aria-labelledby="week-heading" className="mx-auto max-w-6xl px-5 py-24 sm:px-8 sm:py-32">
        <BlurFade inView>
          <div className="flex flex-wrap items-end justify-between gap-6">
            <h2 id="week-heading" className="font-display text-4xl font-extrabold sm:text-6xl">
              This week
            </h2>
            {totalSeats > 0 && (
              <p className="text-lg text-ash">
                <NumberTicker value={totalSeats} className="font-display text-3xl font-extrabold text-sand" /> seats still open
              </p>
            )}
          </div>
        </BlurFade>

        {failed && (
          <p role="alert" className="mt-10 rounded-xl border border-clay/50 bg-clay/10 p-5 text-mist">
            The events did not load. Check your connection and refresh the page.
          </p>
        )}
        {!failed && !data && (
          <p className="mt-10 text-ash" role="status">
            Loading this week&rsquo;s events...
          </p>
        )}
        {data && events.length === 0 && (
          <p className="mt-10 text-ash">Nothing is scheduled yet. New events appear here as soon as they are added.</p>
        )}

        {events.length > 0 && (
          <BlurFade inView delay={0.1}>
            <BentoGrid className="mt-12 auto-rows-[16rem] grid-cols-1 md:grid-cols-3">
              {events.slice(0, 5).map((e, i, shown) => (
                <BentoCard
                  key={e.id}
                  name={e.title}
                  Icon={iconFor(e.title)}
                  description={`${e.venue} · ${e.live ? 'On now' : `${formatMoment(e.startTime)} · ${seatsLeft(e)} seats left`}`}
                  href={bookTo}
                  cta={e.live ? 'See what is on' : 'Book a seat'}
                  className={SPAN_CLASS[SPANS[shown.length][i]]}
                  background={
                    <>
                      <span
                        aria-hidden="true"
                        className="absolute -top-4 right-4 font-display text-[9rem] leading-none font-extrabold text-raised transition-colors duration-500 group-hover:text-sand/15"
                      >
                        {dateBlock(e.startTime).day}
                      </span>
                      {e.live && (
                        <span className="absolute top-4 left-4 inline-flex items-center gap-2 rounded-full bg-clay/15 px-3 py-1 text-sm text-clay">
                          <span className="size-2 rounded-full bg-clay motion-safe:animate-pulse" aria-hidden="true" /> On now
                        </span>
                      )}
                    </>
                  }
                />
              ))}
            </BentoGrid>
          </BlurFade>
        )}
      </section>

      {/* How it works */}
      <section aria-labelledby="how-heading" className="border-t border-line bg-surface/40">
        <div className="mx-auto max-w-6xl px-5 py-24 sm:px-8 sm:py-32">
          <BlurFade inView>
            <h2 id="how-heading" className="font-display text-4xl font-extrabold sm:text-6xl">
              How it works
            </h2>
          </BlurFade>
          <ol className="mt-14 grid gap-10 md:grid-cols-3">
            {STEPS.map(({ Icon, title, text }, i) => (
              <BlurFade key={title} inView delay={0.1 + i * 0.12}>
                <li>
                  <Icon className="size-8 text-sand" aria-hidden="true" />
                  <h3 className="mt-5 font-display text-3xl font-extrabold">{title}</h3>
                  <p className="mt-3 max-w-xs text-ash">{text}</p>
                </li>
              </BlurFade>
            ))}
          </ol>
        </div>
      </section>

      {/* Close */}
      <section aria-labelledby="close-heading" className="mx-auto max-w-6xl px-5 py-24 sm:px-8 sm:py-32">
        <BlurFade inView>
          <div className="relative overflow-hidden rounded-3xl border border-line bg-surface p-10 sm:p-16">
            <h2 id="close-heading" className="max-w-2xl font-display text-4xl leading-tight font-extralight sm:text-6xl">
              Seats go fast for fests. <span className="font-extrabold">Save yours.</span>
            </h2>
            <div className="mt-10 flex flex-wrap items-center gap-4">
              <Link to={loggedIn ? '/dashboard' : '/register'}>
                <ShimmerButton background={SAND} shimmerColor={SAND_LIGHT} borderRadius="12px" className="h-12 px-7 text-base font-semibold text-void">
                  {loggedIn ? 'Go to events' : 'Create account'}
                </ShimmerButton>
              </Link>
              <p className="text-ash">Free for students and faculty.</p>
            </div>
            <BorderBeam size={120} duration={10} colorFrom={SAND} colorTo={ASH} />
          </div>
        </BlurFade>
      </section>

      <footer className="border-t border-line">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-4 px-5 py-10 text-ash sm:px-8">
          <span className="font-display text-xl font-extrabold text-mist">EventSphere</span>
          <span>Campus talks, workshops and fests in one place.</span>
        </div>
      </footer>
    </div>
  );
}
