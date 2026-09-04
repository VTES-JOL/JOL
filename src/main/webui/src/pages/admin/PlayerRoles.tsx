import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, ShieldCheck } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Switch } from '../../components/ui/Switch';
import { EmptyState } from '../../components/ui/EmptyState';
import { api } from '../../api/client';
import type { UserRole } from '../../api/types';
import { useInvalidate } from '../../api/useInvalidate';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';
import { AdminSelect, resolvePlayerName } from './adminControls';

const ROLES = [
  { value: 'JUDGE', label: 'Judge' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'TOURNAMENT_ADMIN', label: 'Tournament Admin' },
];

// Column order for the table.
const ROLE_COLUMNS = ['JUDGE', 'ADMIN', 'TOURNAMENT_ADMIN'];
const ROLE_LABELS = Object.fromEntries(ROLES.map((r) => [r.value, r.label]));
// Granting one of these is as consequential as removing it — confirm both directions.
const PRIVILEGED_ROLES = new Set(['ADMIN', 'TOURNAMENT_ADMIN']);
const USER_ROLES_KEY = ['admin-page', 'user-roles'];

export function PlayerRoles() {
  const [player, setPlayer] = useState('');
  const [role, setRole] = useState(ROLES[0].value);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<string | null>(null);
  // `${player}:${role}` of an in-flight change — disables that one control so a
  // double-click can't fire the PUT twice.
  const [pending, setPending] = useState<string | null>(null);

  // /admin-page/user-roles is already filtered to players with ≥1 role
  // server-side (UserSummaryBean::isSpecialUser).
  const { data: elevated = [] } = useQuery({
    queryKey: USER_ROLES_KEY,
    queryFn: () => api.get<UserRole[]>('/admin-page/user-roles'),
  });
  // Every player name — the grant target isn't limited to recently-active users.
  const { data: allPlayers = [] } = useQuery({
    queryKey: ['admin-page', 'players'],
    queryFn: () => api.get<string[]>('/admin-page/players'),
  });

  const refresh = useInvalidate(USER_ROLES_KEY);

  const applyRole = async (targetPlayer: string, targetRole: string, next: boolean) => {
    const label = ROLE_LABELS[targetRole] ?? targetRole;
    if (!next) {
      if (
        !(await confirmDialog(`${targetPlayer} loses the ${label} permission on their next request.`, {
          title: 'Remove this role?',
          confirmLabel: 'Remove',
          danger: true,
        }))
      )
        return;
    } else if (PRIVILEGED_ROLES.has(targetRole)) {
      if (
        !(await confirmDialog(`${targetPlayer} gains the ${label} permission on their next request.`, {
          title: `Grant ${label}?`,
          confirmLabel: 'Grant',
          danger: true,
        }))
      )
        return;
    }
    const key = `${targetPlayer}:${targetRole}`;
    setPending(key);
    await runRequest(
      api.put(`/admin-page/roles/${encodeURIComponent(targetPlayer)}`, { role: targetRole, value: next }),
      next ? 'Failed to grant role' : 'Failed to update role',
      refresh,
    );
    setPending((k) => (k === key ? null : k));
  };

  const resolvedTarget = resolvePlayerName(player, allPlayers);
  const grantRole = () => {
    if (!resolvedTarget) return;
    void applyRole(resolvedTarget, role, true);
  };
  const grantKey = `${resolvedTarget ?? ''}:${role}`;

  const visible = useMemo(() => {
    const q = search.trim().toLowerCase();
    return elevated.filter(
      (u) =>
        (!q || u.name.toLowerCase().includes(q)) && (!roleFilter || u.roles.includes(roleFilter)),
    );
  }, [elevated, search, roleFilter]);

  const th = 'sticky top-0 bg-panel px-3 py-1.5 text-left font-semibold text-ink-muted border-b border-line whitespace-nowrap';
  const td = 'px-3 py-1.5 border-b border-line/50 align-middle';

  return (
    <Card>
      <CardHeader>
        <CardTitle>Player Roles</CardTitle>
      </CardHeader>

      <CardBody className="flex flex-col gap-3">
        <div className="flex flex-wrap items-end gap-2">
          <div className="min-w-40 flex-1">
            <Input
              id="adminPlayerList"
              label="Grant to"
              size="sm"
              list="admin-all-players"
              placeholder="Type a player name…"
              autoComplete="off"
              value={player}
              onChange={(e) => setPlayer(e.target.value)}
              error={player.trim() && !resolvedTarget ? 'No player by that name' : undefined}
            />
            <datalist id="admin-all-players">
              {allPlayers.map((p) => (
                <option key={p} value={p} />
              ))}
            </datalist>
          </div>
          <div className="min-w-40 flex-1">
            <AdminSelect id="adminRoleList" label="Role" value={role} onChange={setRole} options={ROLES} />
          </div>
          <Button
            variant="secondary"
            size="sm"
            onClick={grantRole}
            disabled={!resolvedTarget || pending === grantKey}
          >
            Grant
          </Button>
        </div>
        <p className="text-xs text-ink-muted">
          Admin and Tournament Admin are independent — neither implies the other.
        </p>

        <div className="flex flex-wrap items-center gap-2 border-t border-line pt-3">
          <div className="w-full sm:w-56">
            <Input
              size="sm"
              srLabel="Search players"
              placeholder="Search players…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              right={<Search size={13} className="text-ink-muted" />}
            />
          </div>
          <div className="flex flex-wrap gap-1">
            <FilterChip label="All" active={roleFilter === null} onClick={() => setRoleFilter(null)} />
            {ROLE_COLUMNS.map((r) => (
              <FilterChip
                key={r}
                label={ROLE_LABELS[r] ?? r}
                active={roleFilter === r}
                onClick={() => setRoleFilter(roleFilter === r ? null : r)}
              />
            ))}
          </div>
          <span className="ml-auto whitespace-nowrap text-xs text-ink-muted">
            {visible.length === elevated.length
              ? `${elevated.length} players`
              : `${visible.length} of ${elevated.length}`}
          </span>
        </div>
      </CardBody>

      <div className="max-h-[70dvh] overflow-auto border-t border-line">
        {visible.length === 0 ? (
          <EmptyState
            icon={ShieldCheck}
            title={elevated.length === 0 ? 'No players have elevated roles' : 'No players match'}
            description={elevated.length === 0 ? 'Grant one above to get started.' : 'Try a different search or filter.'}
          />
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr>
                <th className={th}>Player</th>
                <th className={th}>Last seen</th>
                {ROLE_COLUMNS.map((r) => (
                  <th key={r} className={`${th} text-center`}>
                    {ROLE_LABELS[r] ?? r}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visible.map((u) => (
                <tr key={u.name} className="hover:bg-hover">
                  <td className={`${td} font-medium text-ink whitespace-nowrap`}>{u.name}</td>
                  <td className={`${td} text-ink-secondary whitespace-nowrap`}>{adminTimestamp(u.lastOnline)}</td>
                  {ROLE_COLUMNS.map((r) => {
                    const has = u.roles.includes(r);
                    return (
                      <td key={r} className={`${td} text-center`}>
                        <span className="inline-flex justify-center">
                          <Switch
                            id={`role-${u.name}-${r}`}
                            label={<span className="sr-only">{`${u.name} — ${ROLE_LABELS[r] ?? r}`}</span>}
                            checked={has}
                            disabled={pending === `${u.name}:${r}`}
                            onChange={() => applyRole(u.name, r, !has)}
                          />
                        </span>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Card>
  );
}

function FilterChip({ label, active, onClick }: { label: string; active: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      aria-pressed={active}
      onClick={onClick}
      className={`rounded-full border px-2.5 py-0.5 text-xs transition-colors ${
        active
          ? 'border-accent bg-accent text-surface'
          : 'border-line-accent text-ink-secondary hover:bg-hover'
      }`}
    >
      {label}
    </button>
  );
}
