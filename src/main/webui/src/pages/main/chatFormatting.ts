// Mirrors ds.js's renderGlobalChat(): day-break grouping and the visible
// per-message time are both in UTC (a shared multiplayer chat showing every
// viewer the same absolute time avoids "whose timezone is this?" confusion);
// the local-time equivalent is only ever shown as a hover title.

const DAY_LABEL_FORMAT = new Intl.DateTimeFormat('en-US', { timeZone: 'UTC', day: 'numeric', month: 'long' });
const UTC_TIME_FORMAT = new Intl.DateTimeFormat('en-US', {
  timeZone: 'UTC',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
});
const LOCAL_TIME_FORMAT = new Intl.DateTimeFormat(undefined, {
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
  timeZoneName: 'short',
});

export function dayLabel(timestamp: string): string {
  return DAY_LABEL_FORMAT.format(new Date(timestamp));
}

export function utcTime(timestamp: string): string {
  return UTC_TIME_FORMAT.format(new Date(timestamp));
}

export function localTimeTitle(timestamp: string): string {
  return LOCAL_TIME_FORMAT.format(new Date(timestamp));
}
