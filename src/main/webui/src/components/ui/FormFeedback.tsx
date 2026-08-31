import type { ReactNode } from 'react';

/** Muted help text under a field — the `jt:` counterpart of `.form-text`. */
export function FieldHint({ id, children }: { id?: string; children: ReactNode }) {
  return (
    <p id={id} className="jt:text-xs jt:text-ink-muted jt:mt-1">
      {children}
    </p>
  );
}

/** Inline result banner — the `jt:` counterpart of `.alert.alert-success` / `.alert-danger`. */
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
    kind === 'success' ? 'jt:bg-online/15 jt:text-online' : 'jt:bg-blood/15 jt:text-blood-soft';
  return (
    <div className={`jt:rounded jt:px-3 jt:py-2 jt:text-sm ${tone} ${className ?? ''}`.trim()} role="status">
      {children}
    </div>
  );
}
