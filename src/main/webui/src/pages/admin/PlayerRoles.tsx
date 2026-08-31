import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Plus, X } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { api } from '../../api/client';
import type { UserRole } from '../../api/types';
import { useInvalidate } from '../../api/useInvalidate';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';
import { AdminSelect, toOptions } from './adminControls';

const ROLES = [
  { value: 'JUDGE', label: 'Judge' },
  { value: 'SUPER_USER', label: 'Super User' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'TOURNAMENT_ADMIN', label: 'Tournament Admin' },
  { value: 'PLAYTESTER', label: 'Playtester' },
];

// Column order matches player-roles.jsp's table header.
const COLUMNS = ['JUDGE', 'SUPER_USER', 'PLAYTESTER', 'ADMIN', 'TOURNAMENT_ADMIN'];
const USER_ROLES_KEY = ['admin-page', 'user-roles'];

export function PlayerRoles() {
  const [player, setPlayer] = useState('');
  const [role, setRole] = useState(ROLES[0].value);

  const { data: userRoles = [] } = useQuery({
    queryKey: USER_ROLES_KEY,
    queryFn: () => api.get<UserRole[]>('/admin-page/user-roles'),
  });
  // Same source as the replace-player dropdown — TanStack Query dedupes the
  // identical concurrent fetch rather than issuing it twice.
  const { data: substitutes = [] } = useQuery({
    queryKey: ['admin-page', 'substitutes'],
    queryFn: () => api.get<string[]>('/admin-page/substitutes'),
  });

  // Recently-active players list only arrives after the page's first fetch —
  // default to the first one once it does.
  useEffect(() => {
    if (!player && substitutes.length > 0) setPlayer(substitutes[0]);
  }, [substitutes, player]);

  const refresh = useInvalidate(USER_ROLES_KEY);

  const toggleRole = async (targetPlayer: string, targetRole: string, hasRole: boolean) => {
    if (hasRole && !(await confirmDialog('Are you sure you want to remove this role?'))) return;
    runRequest(
      api.put(`/admin-page/roles/${encodeURIComponent(targetPlayer)}`, { role: targetRole, value: !hasRole }),
      'Failed to update role',
      refresh,
    );
  };

  const addRole = () => {
    if (!player) return;
    runRequest(api.put(`/admin-page/roles/${encodeURIComponent(player)}`, { role, value: true }), 'Failed to add role', refresh);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Player Roles</CardTitle>
      </CardHeader>
      <CardBody className="flex gap-2 items-end">
        <div className="flex-1">
          <AdminSelect id="adminPlayerList" label="Player" value={player} onChange={setPlayer} options={toOptions(substitutes)} />
        </div>
        <div className="flex-1">
          <AdminSelect id="adminRoleList" label="Role" value={role} onChange={setRole} options={ROLES} />
        </div>
        <Button variant="secondary" size="sm" onClick={addRole}>
          Add
        </Button>
      </CardBody>

      <div className="overflow-auto max-h-[70dvh] border-t border-line">
        <table className="w-full text-sm">
          <thead className="sticky top-0 bg-panel">
            <tr className="text-left text-ink-muted">
              <th className="px-2 py-1.5 font-semibold border-b border-line">Name</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Last Online</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Judge</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Super User</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Playtester</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Admin</th>
              <th className="px-2 py-1.5 font-semibold border-b border-line">Tournament Admin</th>
            </tr>
          </thead>
          <tbody>
            {userRoles.map((u) => (
              <tr key={u.name} className="hover:bg-hover">
                <td className="px-2 py-1 border-b border-line/50 text-ink">{u.name}</td>
                <td className="px-2 py-1 border-b border-line/50 text-ink-secondary">
                  {adminTimestamp(u.lastOnline)}
                </td>
                {COLUMNS.map((col) => {
                  const hasRole = u.roles.includes(col);
                  return (
                    <td key={col} className="px-2 py-1 border-b border-line/50 text-center">
                      <Button
                        variant="secondary"
                        size="sm"
                        aria-label={hasRole ? `Remove ${col}` : `Add ${col}`}
                        onClick={() => toggleRole(u.name, col, hasRole)}
                      >
                        {hasRole ? <X size={12} /> : <Plus size={12} />}
                      </Button>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
