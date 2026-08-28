import type { ComponentType, ReactNode } from 'react';

/**
 * "Nothing here yet" placeholder for detail panes, ported from jol-quarkus.
 * Tailwind `jt:` -prefixed. Distinct from the Bootstrap-era
 * `components/EmptyState.tsx` — use this on pages migrated to Tailwind.
 */
interface EmptyStateProps {
  icon?: ComponentType<{ size?: number; className?: string }>;
  title: string;
  description?: string;
  action?: ReactNode;
  className?: string;
}

export function EmptyState({ icon: Icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div
      className={`jt:flex jt:flex-col jt:items-center jt:justify-center jt:gap-3 jt:p-8 jt:text-center jt:flex-1 ${className ?? ''}`}
    >
      {Icon && <Icon size={32} className="jt:text-ink-muted/40" />}
      <div className="jt:space-y-1">
        <p className="jt:text-sm jt:text-ink-muted">{title}</p>
        {description && <p className="jt:text-xs jt:text-ink-muted/70">{description}</p>}
      </div>
      {action}
    </div>
  );
}
