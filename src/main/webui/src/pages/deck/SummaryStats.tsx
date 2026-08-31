import type { DeckSummary } from './deckKit';

/**
 * Crypt / Library / Groups chip row, ported from jol-quarkus. Tailwind-based.
 * With `validate`, cells whose value breaks a construction rule
 * (crypt < 12, library outside 60–90, non-consecutive groups) are tinted red.
 */
interface Props {
  summary: DeckSummary;
  validate?: boolean;
  className?: string;
}

const isValidCrypt = (n: number) => n >= 12;
const isValidLibrary = (n: number) => n >= 60 && n <= 90;
const isValidGroups = (g: string | null) => {
  if (!g) return true;
  const parts = g.split('/').map(Number);
  if (parts.length > 2) return false;
  if (parts.length === 1) return true;
  return parts[1] === parts[0] + 1;
};

export function SummaryStats({ summary, validate = false, className = '' }: Props) {
  const cryptInvalid = validate && !isValidCrypt(summary.crypt);
  const libInvalid = validate && !isValidLibrary(summary.library);
  const groupsInvalid = validate && !isValidGroups(summary.groups);
  const hasError = cryptInvalid || libInvalid || groupsInvalid;

  const chip = (label: string, value: string | number, invalid: boolean) => (
    <span className={`inline-flex items-center gap-1 px-1.5 py-0.5 ${invalid ? 'bg-blood-soft/10' : ''}`}>
      <span className={invalid ? 'text-blood' : 'text-ink-muted'}>{label}</span>
      <span className={`font-semibold ${invalid ? 'text-blood' : 'text-ink-secondary'}`}>{value}</span>
    </span>
  );

  return (
    <div
      className={[
        'inline-flex items-center rounded border overflow-hidden text-[11px] leading-none tabular-nums',
        hasError
          ? 'border-blood/40 bg-hover/60 divide-x divide-blood/20'
          : 'border-line/60 bg-hover/60 divide-x divide-line/60',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      {chip('Crypt', summary.crypt, cryptInvalid)}
      {chip('Library', summary.library, libInvalid)}
      {summary.groups && chip('Groups', summary.groups, groupsInvalid)}
    </div>
  );
}
