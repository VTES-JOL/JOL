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
      <CardBody className="jt:flex jt:gap-2 jt:items-end">
        <div className="jt:flex-1">
          <AdminSelect id="adminPlayerList" label="Player" value={player} onChange={setPlayer} options={toOptions(substitutes)} />
        </div>
        <div className="jt:flex-1">
          <AdminSelect id="adminRoleList" label="Role" value={role} onChange={setRole} options={ROLES} />
        </div>
        <Button variant="secondary" size="sm" onClick={addRole}>
          Add
        </Button>
      </CardBody>

      <div className="jt:overflow-auto jt:max-h-[70dvh] jt:border-t jt:border-line">
        <table className="jt:w-full jt:text-sm">
          <thead className="jt:sticky jt:top-0 jt:bg-panel">
            <tr className="jt:text-left jt:text-ink-muted">
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Name</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Last Online</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Judge</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Super User</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Playtester</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Admin</th>
              <th className="jt:px-2 jt:py-1.5 jt:font-semibold jt:border-b jt:border-line">Tournament Admin</th>
            </tr>
          </thead>
          <tbody>
            {userRoles.map((u) => (
              <tr key={u.name} className="jt:hover:bg-hover">
                <td className="jt:px-2 jt:py-1 jt:border-b jt:border-line/50 jt:text-ink">{u.name}</td>
                <td className="jt:px-2 jt:py-1 jt:border-b jt:border-line/50 jt:text-ink-secondary">
                  {adminTimestamp(u.lastOnline)}
                </td>
                {COLUMNS.map((col) => {
                  const hasRole = u.roles.includes(col);
                  return (
                    <td key={col} className="jt:px-2 jt:py-1 jt:border-b jt:border-line/50 jt:text-center">
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
