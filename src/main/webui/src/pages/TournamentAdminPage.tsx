import { useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Wrench } from 'lucide-react';
import { api } from '../api/client';
import { useInvalidate } from '../api/useInvalidate';
import type { TournamentMetadata } from '../api/types';
import { TournamentAdminList } from './tournamentAdmin/TournamentAdminList';
import { TournamentEditor } from './tournamentAdmin/TournamentEditor';
import { TournamentManager } from './tournamentAdmin/TournamentManager';
import { confirmDialog } from '../stores/dialog';
import { EmptyState } from '../components/ui/EmptyState';
import { MasterDetailView } from '../components/ui/MasterDetailView';

type View = { mode: 'edit'; name: string | null } | { mode: 'tables'; name: string } | null;

// Shares the ['tournament'] WS invalidation key with the player-facing
// TournamentPage (see TournamentResource's player/join, /leave, /deck
// handlers) — TanStack Query's default prefix matching means a push there
// also refreshes this list, on top of the explicit refreshList() calls below
// after admin-only mutations (which the backend doesn't push for).
const TOURNAMENT_ADMIN_LIST_KEY = ['tournament', 'admin-list'];

// Mirrors ds.js's tournamentAdminClick(): EDIT tournaments (and STARTING ones
// still inside their own registration window) open the editor; STARTING
// tournaments past registration, and ACTIVE ones, open the table/round
// manager instead.
function decide(t: TournamentMetadata): View {
  const now = new Date();
  const regEnd = new Date(t.registrationEndTime);
  if (t.status === 'EDIT') return { mode: 'edit', name: t.name };
  if (t.status === 'STARTING') return now > regEnd ? { mode: 'tables', name: t.name } : { mode: 'edit', name: t.name };
  return { mode: 'tables', name: t.name };
}

export function TournamentAdminPage() {
  const [view, setView] = useState<View>(null);
  // Set by TournamentEditor's onDirtyChange — a ref, not state, since it's
  // only ever read at the moment of a new selection, never during render.
  const editorDirtyRef = useRef(false);

  const { data: tournaments = [] } = useQuery({
    queryKey: TOURNAMENT_ADMIN_LIST_KEY,
    queryFn: () => api.get<TournamentMetadata[]>('/tournament/admin-list'),
  });

  const refreshList = useInvalidate(TOURNAMENT_ADMIN_LIST_KEY);

  // Derived from the live query result each render instead of a copy
  // snapshotted into `view` — so it never goes stale relative to `tournaments`.
  const tablesTournament = view?.mode === 'tables' ? tournaments.find((t) => t.name === view.name) : null;

  // TournamentEditor can't guard its own `tournamentName` prop changing out
  // from under it, so the confirm has to live here, at the two places that
  // actually change it.
  const confirmDiscardIfDirty = async () =>
    !editorDirtyRef.current ||
    confirmDialog('Your edits to this tournament will be lost.', {
      title: 'Discard unsaved changes?',
      confirmLabel: 'Discard',
      danger: true,
    });

  const selectTournament = async (t: TournamentMetadata) => {
    if (!(await confirmDiscardIfDirty())) return;
    setView(decide(t));
  };

  const newTournament = async () => {
    if (!(await confirmDiscardIfDirty())) return;
    setView({ mode: 'edit', name: null });
  };

  let detail;
  if (view?.mode === 'edit') {
    detail = (
      <TournamentEditor
        tournamentName={view.name}
        onSaved={refreshList}
        onDirtyChange={(dirty) => {
          editorDirtyRef.current = dirty;
        }}
      />
    );
  } else if (tablesTournament) {
    detail = <TournamentManager tournament={tablesTournament} onClose={() => setView(null)} onChanged={refreshList} />;
  } else {
    detail = <EmptyState icon={Wrench} title="Select a tournament to edit or manage" />;
  }

  return (
    <div className="flex flex-col flex-1 min-h-0 p-4 bg-base text-ink">
      <MasterDetailView
        breakpoint="lg"
        columns="320px minmax(360px, 1fr)"
        activeKey={view ? 'detail' : 'list'}
        onActiveKeyChange={(k) => {
          if (k === 'list') void confirmDiscardIfDirty().then((ok) => ok && setView(null));
        }}
        panels={[
          {
            key: 'list',
            label: 'Tournaments',
            content: <TournamentAdminList tournaments={tournaments} onSelect={selectTournament} onNew={newTournament} />,
          },
          { key: 'detail', label: 'Details', content: detail },
        ]}
      />
    </div>
  );
}
