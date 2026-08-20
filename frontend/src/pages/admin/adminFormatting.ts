// Mirrors ds.js's moment(...).tz("UTC").format("D-MMM-YY HH:mm z") used
// throughout the admin tables — e.g. "3-Aug-26 14:05 UTC".
const PARTS_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  year: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
});

export function adminTimestamp(iso: string): string {
  const parts = PARTS_FORMAT.formatToParts(new Date(iso));
  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? '';
  return `${get('day')}-${get('month')}-${get('year')} ${get('hour')}:${get('minute')} UTC`;
}
