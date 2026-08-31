import { useQuery } from '@tanstack/react-query';
import { api, ApiError } from '../api/client';
import type { UserRole } from '../api/types';
import { PageLoading } from '../components/PageLoading';
import { PanelPlaceholder } from '../components/PanelPlaceholder';
import { PlayerRoles } from './admin/PlayerRoles';
import { ReplacePlayer } from './admin/ReplacePlayer';
import { EndTurn } from './admin/EndTurn';
import { RollbackGame } from './admin/RollbackGame';
import { IdleGames } from './admin/IdleGames';
import { SiteNotesEditor } from './admin/SiteNotesEditor';

// No page-level query — each panel below fetches its own slice (see
// admin/*.tsx), so editing one doesn't force its siblings to refetch. This
// page only probes /user-roles to gate the whole page on the admin check
// (a 403 here means "not an admin", not a load failure worth a toast); the
// query is otherwise redundant with PlayerRoles' own identical fetch, which
// TanStack Query dedupes for free.
export function AdminPage() {
  const { data, error } = useQuery({
    queryKey: ['admin-page', 'user-roles'],
    queryFn: () => api.get<UserRole[]>('/admin-page/user-roles'),
    retry: false,
    meta: { silent: true },
  });

  const forbidden = error instanceof ApiError && error.status === 403;

  if (forbidden) {
    return (
      <div className="p-4">
        <PanelPlaceholder icon="bi-shield-lock" message="You don't have access to this page." />
      </div>
    );
  }

  if (!data) return <PageLoading />;

  return (
    <div className="row g-3 p-3">
      <div className="col-12 col-lg-4">
        <PlayerRoles />
      </div>
      <div className="col-12 col-md-6 col-lg-4">
        <ReplacePlayer />
        <EndTurn />
        <RollbackGame />
      </div>
      <div className="col-12 col-md-6 col-lg-4">
        <SiteNotesEditor />
        <IdleGames />
      </div>
    </div>
  );
}
