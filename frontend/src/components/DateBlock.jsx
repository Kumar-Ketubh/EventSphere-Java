import { dateBlock } from '@/utils/dates.js';
import { cn } from '@/lib/utils';

/** The date block on every ticket: big day number, small month. */
export default function DateBlock({ iso, muted = false, className }) {
  const { day, month } = dateBlock(iso);
  return (
    <div
      aria-hidden="true"
      className={cn(
        'flex w-16 shrink-0 flex-col items-center justify-center self-start rounded-xl border py-2',
        muted ? 'border-line bg-void text-ash' : 'border-sand/30 bg-raised text-sand',
        className,
      )}
    >
      <span className="font-display text-4xl leading-none font-extrabold">{day}</span>
      <span className="mt-1 text-xs text-ash">{month}</span>
    </div>
  );
}
