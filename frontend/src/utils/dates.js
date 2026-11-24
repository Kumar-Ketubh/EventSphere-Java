// One date style for the whole app: "Mon 24 Nov, 10:00 to 13:00".

const DAY_FMT = { weekday: 'short', day: 'numeric', month: 'short' };
const TIME_FMT = { hour: '2-digit', minute: '2-digit', hour12: false };

function parse(iso) {
  if (!iso) return null;
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? null : d;
}

const day = (d) => d.toLocaleDateString('en-GB', DAY_FMT).replace(',', '');
const time = (d) => d.toLocaleTimeString('en-GB', TIME_FMT);

/** "Mon 24 Nov, 10:00 to 13:00", with the end date added when the event spans days. */
export function formatRange(startIso, endIso) {
  const start = parse(startIso);
  if (!start) return '';
  const end = parse(endIso);
  if (!end) return `${day(start)}, ${time(start)}`;
  if (start.toDateString() === end.toDateString()) return `${day(start)}, ${time(start)} to ${time(end)}`;
  return `${day(start)}, ${time(start)} to ${day(end)}, ${time(end)}`;
}

/** "Mon 24 Nov, 10:00" */
export function formatMoment(iso) {
  const d = parse(iso);
  return d ? `${day(d)}, ${time(d)}` : '';
}

/** Day number and short month for the date block: { day: 24, month: 'Nov' }. */
export function dateBlock(iso) {
  const d = parse(iso);
  return d ? { day: d.getDate(), month: d.toLocaleDateString('en-GB', { month: 'short' }) } : { day: '', month: '' };
}
