import type { DeckSummary } from './deckKit';

/**
 * Crypt / Library / Groups chip row, ported from jol-quarkus. Tailwind `jt:`
 * -prefixed. With `validate`, cells whose value breaks a construction rule
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
    <span className={`jt:inline-flex jt:items-center jt:gap-1 jt:px-1.5 jt:py-0.5 ${invalid ? 'jt:bg-blood-soft/10' : ''}`}>
      <span className={invalid ? 'jt:text-blood' : 'jt:text-ink-muted'}>{label}</span>
      <span className={`jt:font-semibold ${invalid ? 'jt:text-blood' : 'jt:text-ink-secondary'}`}>{value}</span>
    </span>
  );

  return (
    <div
      className={[
        'jt:inline-flex jt:items-center jt:rounded jt:border jt:overflow-hidden jt:text-[11px] jt:leading-none jt:tabular-nums',
        hasError
          ? 'jt:border-blood/40 jt:bg-hover/60 jt:divide-x jt:divide-blood/20'
          : 'jt:border-line/60 jt:bg-hover/60 jt:divide-x jt:divide-line/60',
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
