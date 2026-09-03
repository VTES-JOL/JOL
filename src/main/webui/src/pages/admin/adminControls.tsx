import type { ReactNode } from 'react';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';
import { Select } from '../../components/ui/Select';

export interface Option {
  value: string;
  label: string;
}

/** `['a', 'b']` → `[{ value: 'a', label: 'a' }, …]` for the plain string lists
 *  (players, substitutes, turns) the admin cards feed into <AdminSelect>. */
export function toOptions(values: readonly string[]): Option[] {
  return values.map((v) => ({ value: v, label: v }));
}

/**
 * Match a free-typed name against the known player list, case-insensitively,
 * and return it in its canonical casing (or null if there's no such player).
 * The admin player fields are `<input list>` datalists — the user can type
 * anything — and the backend rejects an unknown name with a 500, so callers
 * guard the submit on this instead.
 */
export function resolvePlayerName(input: string, players: readonly string[]): string | null {
  const q = input.trim().toLowerCase();
  if (!q) return null;
  return players.find((p) => p.toLowerCase() === q) ?? null;
}

/**
 * The active-games picker for the admin Games tab. One selection drives the
 * whole tab (game brief + end-turn / rollback / replace actions). The default
 * is derived, not an effect: `gameId` falls back to the first game until the
 * user picks another.
 */
export function useAdminGames() {
  const { data: games = {} } = useQuery({
    queryKey: ['admin-page', 'games'],
    queryFn: () => api.get<Record<string, string>>('/admin-page/games'),
  });
  const gameOptions = Object.entries(games).map(([value, label]) => ({ value, label }));
  const [picked, setPicked] = useState('');
  const gameId = picked || gameOptions[0]?.value || '';
  return { games, gameOptions, gameId, setGameId: setPicked };
}

interface AdminSelectProps {
  id: string;
  label: ReactNode;
  value: string;
  onChange: (value: string) => void;
  options: readonly Option[];
}

/** Labelled `<select>` for the admin cards — thin wrapper over the Tailwind
 *  `ui/Select` that takes an `Option[]` instead of `<option>` children. */
export function AdminSelect({ id, label, value, onChange, options }: AdminSelectProps) {
  return (
    <Select id={id} label={label} value={value} onChange={(e) => onChange(e.target.value)}>
      {options.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </Select>
  );
}
