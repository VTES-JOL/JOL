interface Props {
  title: string;
  subtitle?: string;
}

export function SectionHeader({ title, subtitle }: Props) {
  return (
    <div className="px-3 py-2 border-b border-line/50">
      <p className="text-[11px] font-semibold uppercase tracking-wider text-ink-muted">{title}</p>
      {subtitle && <p className="text-[11px] text-ink-muted mt-0.5">{subtitle}</p>}
    </div>
  );
}
