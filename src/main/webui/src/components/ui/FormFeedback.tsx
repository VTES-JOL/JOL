import type { ReactNode } from 'react';

/** Muted help text under a field — the Tailwind counterpart of `.form-text`. */
export function FieldHint({ id, children }: { id?: string; children: ReactNode }) {
  return (
    <p id={id} className="text-xs text-ink-muted mt-1">
      {children}
    </p>
  );
}

/** Inline result banner — the Tailwind counterpart of `.alert.alert-success` / `.alert-danger`. */
export function InlineAlert({
  kind,
  children,
  className,
}: {
  kind: 'success' | 'danger';
  children: ReactNode;
  className?: string;
}) {
  const tone =
    kind === 'success' ? 'bg-online/15 text-online' : 'bg-blood/15 text-blood-soft';
  return (
    <div className={`rounded px-3 py-2 text-sm ${tone} ${className ?? ''}`.trim()} role="status">
      {children}
    </div>
  );
}
