import { Link } from 'react-router-dom';
import { motion } from 'motion/react';
import { AnimatedShinyText } from '@/components/ui/animated-shiny-text';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { MagicCard } from '@/components/ui/magic-card';
import { NumberTicker } from '@/components/ui/number-ticker';
import DateBlock from '@/components/DateBlock.jsx';
import { cn } from '@/lib/utils';
import { cssVar } from '@/lib/theme';
import { formatRange } from '@/utils/dates.js';

/**
 * A ticket: date block and details, then a dashed stub with seats left and the booking action.
 * The card's spotlight follows the pointer. type is 'live', 'upcoming' or 'previous'.
 */
export default function EventCard({ event, type, onRegister, isBooking, isBooked }) {
  const seatsLeft = Math.max(0, event.totalSeats - event.bookedSeats);
  const isFull = seatsLeft <= 0;
  const ended = type === 'previous';

  return (
    <MagicCard
      gradientColor={cssVar('raised')}
      gradientFrom={cssVar('sand')}
      gradientTo={cssVar('ash')}
      gradientOpacity={0.6}
      className={cn('h-full rounded-2xl', ended && 'opacity-75')}
    >
      <div className="flex h-full flex-col">
        <div className="flex flex-1 gap-4 p-5">
          <DateBlock iso={event.startTime} muted={ended} />
          <div className="min-w-0">
            {type === 'live' && (
              <Badge className="mb-2 border-clay/40 bg-clay/15 text-clay">
                <span className="size-1.5 rounded-full bg-clay motion-safe:animate-pulse" aria-hidden="true" />
                <AnimatedShinyText className="mx-0 text-clay">On now</AnimatedShinyText>
              </Badge>
            )}
            {ended && (
              <Badge variant="outline" className="mb-2 border-line text-ash">
                Ended
              </Badge>
            )}
            <h3 className="font-display text-xl leading-snug font-extrabold text-mist">{event.title}</h3>
            <p className="mt-1 text-sm font-semibold text-mist/90">{event.venue}</p>
            <p className="text-xs text-ash tabular-nums">{formatRange(event.startTime, event.endTime)}</p>
            {event.description && <p className="mt-3 text-sm text-ash">{event.description}</p>}
          </div>
        </div>

        {type === 'upcoming' && (
          <div className="flex items-center justify-between gap-3 border-t border-dashed border-line px-5 py-3">
            <p className={cn('text-sm tabular-nums', isFull ? 'text-clay' : 'text-mist')}>
              {isFull ? (
                'Full'
              ) : (
                <>
                  <NumberTicker value={seatsLeft} className="font-semibold text-mist" /> of {event.totalSeats} seats left
                </>
              )}
            </p>
            {isBooked ? (
              <motion.span initial={{ scale: 0.7, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ type: 'spring', stiffness: 380, damping: 20 }}>
                <Link
                  to="/profile"
                  className="inline-flex h-10 shrink-0 items-center rounded-lg border border-sand/50 px-3 text-sm font-semibold whitespace-nowrap text-sand hover:bg-sand/10"
                >
                  You have a seat
                </Link>
              </motion.span>
            ) : (
              <Button variant="accent" size="lg" onClick={() => onRegister(event.id)} disabled={isFull || isBooking} className="active:scale-95">
                {isBooking ? 'Booking...' : 'Book seat'}
              </Button>
            )}
          </div>
        )}

        {ended && (
          <div className="border-t border-dashed border-line px-5 py-3">
            <p className="text-sm text-ash">{event.bookedSeats} attended</p>
          </div>
        )}
      </div>
    </MagicCard>
  );
}
