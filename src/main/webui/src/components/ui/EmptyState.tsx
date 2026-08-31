import type { ComponentType, ReactNode } from 'react';

/**
 * "Nothing here yet" placeholder for detail panes, ported from jol-quarkus.
 * Tailwind Tailwind-based. Use this on pages migrated to Tailwind (the
 * Bootstrap pages that still exist inline their own empty states).
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
      className={`flex flex-col items-center justify-center gap-3 p-8 text-center flex-1 ${className ?? ''}`}
    >
      {Icon && <Icon size={32} className="text-ink-muted/40" />}
      <div className="space-y-1">
        <p className="text-sm text-ink-muted">{title}</p>
        {description && <p className="text-xs text-ink-muted/70">{description}</p>}
      </div>
      {action}
    </div>
  );
}
