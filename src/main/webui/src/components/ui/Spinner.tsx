/** Inline loading indicator, ported from jol-quarkus. Tailwind `jt:` -prefixed. */
interface Props {
  message?: string;
  className?: string;
}

export function Spinner({ message = 'Loading…', className = '' }: Props) {
  return (
    <div className={`jt:flex jt:items-center jt:gap-2 jt:p-4 jt:text-sm jt:text-ink-muted ${className}`}>
      <svg className="jt:animate-spin jt:w-4 jt:h-4 jt:text-accent jt:shrink-0" fill="none" viewBox="0 0 24 24">
        <circle className="jt:opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="jt:opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      {message && <span>{message}</span>}
    </div>
  );
}
