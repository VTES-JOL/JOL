import { Check, Palette } from 'lucide-react';
import { api } from '../api/client';
import { showError } from '../stores/toast';
import { useNavRefresh } from '../auth/useNav';
import { applyTheme, getTheme, useTheme, THEMES, type Theme } from '../theme';

const MENU_ITEM =
  'flex w-full items-start gap-2 px-3 py-1.5 text-left text-sm text-ink hover:bg-hover';

/**
 * Top-bar appearance switcher. Applies the chosen theme optimistically (so it
 * takes effect the instant you click), then persists it to the server; on a
 * failed write it reverts and toasts. The server copy is authoritative — see
 * src/theme.ts and TopBar's /nav reconciliation.
 */
export function ThemePicker({
  open,
  onToggle,
  onClose,
}: {
  open: boolean;
  onToggle: () => void;
  onClose: () => void;
}) {
  const current = useTheme();
  const refreshNav = useNavRefresh();

  const choose = (theme: Theme) => {
    onClose();
    if (theme === getTheme()) return;
    const previous = getTheme();
    applyTheme(theme);
    api.put<void>('/profile/theme', { theme }).then(refreshNav, (err) => {
      console.error('Failed to save theme', err);
      showError('Failed to save theme.');
      applyTheme(previous);
    });
  };

  return (
    <div className="relative">
      <button
        type="button"
        aria-label="Appearance"
        aria-expanded={open}
        className="flex items-center rounded-full border border-white/15 p-1.5 text-white hover:bg-white/10"
        onClick={onToggle}
      >
        <Palette size={16} />
      </button>
      {open && (
        <ul className="absolute right-0 z-50 mt-1 w-64 list-none overflow-hidden rounded border border-line bg-panel shadow-xl">
          {THEMES.map((t) => (
            <li key={t.value}>
              <button type="button" className={MENU_ITEM} onClick={() => choose(t.value)}>
                <span className="mt-0.5 w-3.5 shrink-0">
                  {current === t.value && <Check size={14} className="text-accent-soft" />}
                </span>
                <span className="flex flex-col">
                  <span className={current === t.value ? 'text-ink' : 'text-ink-secondary'}>{t.label}</span>
                  <span className="text-xs text-ink-muted">{t.hint}</span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
