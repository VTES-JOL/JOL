import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ShieldOff } from 'lucide-react';
import { api, ApiError } from '../api/client';
import type { UserRole } from '../api/types';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { TabBar, type TabDef } from '../components/TabBar';
import { PlayerRoles } from './admin/PlayerRoles';
import { ReplacePlayer } from './admin/ReplacePlayer';
import { EndTurn } from './admin/EndTurn';
import { RollbackGame } from './admin/RollbackGame';
import { IdleGames } from './admin/IdleGames';
import { SiteNotesEditor } from './admin/SiteNotesEditor';

// The admin tools split into two jobs with different urgency: live-game
// triage (reactive, usually a player complaint) and site/people admin
// (deliberate, rare). One flat 3-column grid interleaved them and let the
// Player Roles table — huge with real data — dominate the top-left. Tabs
// give each job the full page width.
type AdminTab = 'games' | 'people';

const TABS: TabDef<AdminTab>[] = [
  { id: 'games', label: 'Games' },
  { id: 'people', label: 'People & site' },
];

// No page-level query beyond the admin check — each panel below fetches its
// own slice (see admin/*.tsx), so editing one doesn't force its siblings to
// refetch. A 403 here means "not an admin", not a load failure worth a
// toast; the query is otherwise redundant with PlayerRoles' own identical
// fetch, which TanStack Query dedupes for free.
export function AdminPage() {
  const [tab, setTab] = useState<AdminTab>('games');

  const { data, error } = useQuery({
    queryKey: ['admin-page', 'user-roles'],
    queryFn: () => api.get<UserRole[]>('/admin-page/user-roles'),
    retry: false,
    meta: { silent: true },
  });

  const forbidden = error instanceof ApiError && error.status === 403;

  if (forbidden) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <EmptyState icon={ShieldOff} title="You don't have access to this page." />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="flex flex-col flex-1 min-h-0 gap-3 p-4 bg-base">
      <div className="shrink-0 rounded-lg border border-line-accent bg-surface/85 px-2 backdrop-blur-md">
        <TabBar tabs={TABS} active={tab} onChange={setTab} />
      </div>

      <div className="flex-1 min-h-0 overflow-y-auto">
        {tab === 'games' && (
          <div className="flex flex-col gap-3">
            <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3 content-start">
              <ReplacePlayer />
              <EndTurn />
              <RollbackGame />
            </div>
            <IdleGames />
          </div>
        )}

        {tab === 'people' && (
          <div className="grid gap-3 content-start lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
            <PlayerRoles />
            <SiteNotesEditor />
          </div>
        )}
      </div>
    </div>
  );
}
