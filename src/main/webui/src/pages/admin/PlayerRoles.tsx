import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { UserRole } from '../../api/types';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';

const ROLES: { value: string; label: string }[] = [
  { value: 'JUDGE', label: 'Judge' },
  { value: 'SUPER_USER', label: 'Super User' },
  { value: 'ADMIN', label: 'Admin' },
  { value: 'TOURNAMENT_ADMIN', label: 'Tournament Admin' },
  { value: 'PLAYTESTER', label: 'Playtester' },
];

// Column order matches player-roles.jsp's table header.
const COLUMNS = ['JUDGE', 'SUPER_USER', 'PLAYTESTER', 'ADMIN', 'TOURNAMENT_ADMIN'];

export function PlayerRoles() {
  const queryClient = useQueryClient();
  const [player, setPlayer] = useState('');
  const [role, setRole] = useState(ROLES[0].value);

  const { data: userRoles = [] } = useQuery({
    queryKey: ['admin-page', 'user-roles'],
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

  const refresh = () => queryClient.invalidateQueries({ queryKey: ['admin-page', 'user-roles'] });

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
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>Player Roles</CardTitle>
      </CardHeader>
      <div className="card-body pb-2">
        <div className="d-flex gap-2 align-items-end">
          <div className="flex-grow-1">
            <label htmlFor="adminPlayerList" className="form-label mb-1">
              Player
            </label>
            <select
              id="adminPlayerList"
              className="form-select form-select-sm"
              value={player}
              onChange={(e) => setPlayer(e.target.value)}
            >
              {substitutes.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
          <div className="flex-grow-1">
            <label htmlFor="adminRoleList" className="form-label mb-1">
              Role
            </label>
            <select
              id="adminRoleList"
              className="form-select form-select-sm"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              {ROLES.map((r) => (
                <option key={r.value} value={r.value}>
                  {r.label}
                </option>
              ))}
            </select>
          </div>
          <button onClick={addRole} className="btn btn-outline-secondary btn-sm">
            Add
          </button>
        </div>
      </div>
      <div className="scrollable mhd-70" style={{ overflowX: 'auto' }}>
        <table className="table table-sm table-hover mb-0">
          <thead>
            <tr>
              <th>Name</th>
              <th>Last Online</th>
              <th>Judge</th>
              <th>Super User</th>
              <th>Playtester</th>
              <th>Admin</th>
              <th>Tournament Admin</th>
            </tr>
          </thead>
          <tbody>
            {userRoles.map((u) => (
              <tr key={u.name}>
                <td>{u.name}</td>
                <td>{adminTimestamp(u.lastOnline)}</td>
                {COLUMNS.map((col) => {
                  const hasRole = u.roles.includes(col);
                  return (
                    <td key={col} className="text-center">
                      <button
                        className="btn btn-outline-secondary btn-sm"
                        onClick={() => toggleRole(u.name, col, hasRole)}
                      >
                        <i className={`bi bi-${hasRole ? 'x' : 'plus'}`} />
                      </button>
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
