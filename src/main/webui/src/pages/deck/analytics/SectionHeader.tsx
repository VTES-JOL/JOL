interface Props {
  title: string;
  subtitle?: string;
}

export function SectionHeader({ title, subtitle }: Props) {
  return (
    <div className="jt:px-3 jt:py-2 jt:border-b jt:border-line/50">
      <p className="jt:text-[11px] jt:font-semibold jt:uppercase jt:tracking-wider jt:text-ink-muted">{title}</p>
      {subtitle && <p className="jt:text-[11px] jt:text-ink-muted jt:mt-0.5">{subtitle}</p>}
    </div>
  );
}
