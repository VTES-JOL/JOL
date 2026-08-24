import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { UserRole } from '../../api/types';
import { confirmDialog } from '../../components/dialog';
import { showError } from '../../components/toast';
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

export function PlayerRoles({
  userRoles,
  substitutes,
  onSaved,
}: {
  userRoles: UserRole[];
  substitutes: string[];
  onSaved: () => void;
}) {
  const [player, setPlayer] = useState('');
  const [role, setRole] = useState(ROLES[0].value);

  // Recently-active players list (same source as the replace-player dropdown,
  // matching ds.js's callbackAdmin exactly) only arrives after the page's
  // first fetch — default to the first one once it does.
  useEffect(() => {
    if (!player && substitutes.length > 0) setPlayer(substitutes[0]);
  }, [substitutes, player]);

  const toggleRole = async (targetPlayer: string, targetRole: string, hasRole: boolean) => {
    if (hasRole && !(await confirmDialog('Are you sure you want to remove this role?'))) return;
    api
      .put(`/admin-page/roles/${encodeURIComponent(targetPlayer)}`, { role: targetRole, value: !hasRole })
      .then(onSaved)
      .catch((err) => {
        console.error('Failed to update role', err);
        showError('Failed to update role.');
      });
  };

  const addRole = () => {
    if (!player) return;
    api
      .put(`/admin-page/roles/${encodeURIComponent(player)}`, { role, value: true })
      .then(onSaved)
      .catch((err) => {
        console.error('Failed to add role', err);
        showError('Failed to add role.');
      });
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
