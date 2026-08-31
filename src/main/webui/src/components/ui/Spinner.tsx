/** Inline loading indicator, ported from jol-quarkus. Tailwind Tailwind-based. */
interface Props {
  message?: string;
  className?: string;
}

export function Spinner({ message = 'Loading…', className = '' }: Props) {
  return (
    <div className={`flex items-center gap-2 p-4 text-sm text-ink-muted ${className}`}>
      <svg className="animate-spin w-4 h-4 text-accent shrink-0" fill="none" viewBox="0 0 24 24">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      {message && <span>{message}</span>}
    </div>
  );
}
