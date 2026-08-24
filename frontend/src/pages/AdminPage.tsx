import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import type { AdminPage as AdminPageData } from '../api/types';
import { showError } from '../components/toast';
import { PageLoading } from '../components/PageLoading';
import { PlayerRoles } from './admin/PlayerRoles';
import { ReplacePlayer } from './admin/ReplacePlayer';
import { EndTurn } from './admin/EndTurn';
import { RollbackGame } from './admin/RollbackGame';
import { IdleGames } from './admin/IdleGames';
import { SiteNotesEditor } from './admin/SiteNotesEditor';

// No WebSocket subscription — admin actions here are only ever triggered by
// the admin viewing this page themselves (unlike main's chat/games, nothing
// external needs to push a refresh), so a plain fetch-on-mount plus
// refetch-after-each-mutation covers it.
export function AdminPage() {
  const [data, setData] = useState<AdminPageData | null>(null);
  const [forbidden, setForbidden] = useState(false);

  const refresh = () => {
    api
      .get<AdminPageData>('/admin-page')
      .then(setData)
      .catch((err) => {
        if (err instanceof ApiError && err.status === 403) {
          setForbidden(true);
        } else {
          console.error('Failed to load admin page', err);
          showError('Failed to load admin page.');
        }
      });
  };

  useEffect(refresh, []);

  if (forbidden) {
    return (
      <div className="p-4 text-center text-muted">
        <i className="bi bi-shield-lock fs-1 d-block mb-2" />
        <p className="mb-0">You don't have access to this page.</p>
      </div>
    );
  }

  if (!data) return <PageLoading />;

  return (
    <div className="row g-2 p-3">
      <div className="col-sm-4">
        <PlayerRoles userRoles={data.userRoles} substitutes={data.substitutes} onSaved={refresh} />
      </div>
      <div className="col-sm-4">
        <ReplacePlayer games={data.games} substitutes={data.substitutes} onSaved={refresh} />
        <EndTurn games={data.games} onSaved={refresh} />
        <RollbackGame games={data.games} onSaved={refresh} />
      </div>
      <div className="col-sm-4">
        <IdleGames idleGames={data.idleGames} onSaved={refresh} />
        <SiteNotesEditor notes={data.siteNotes} onSaved={refresh} />
      </div>
    </div>
  );
}
