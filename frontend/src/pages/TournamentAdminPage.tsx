import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { TournamentMetadata } from '../api/types';
import { TournamentAdminList } from './tournamentAdmin/TournamentAdminList';
import { TournamentEditor } from './tournamentAdmin/TournamentEditor';
import { TournamentManager } from './tournamentAdmin/TournamentManager';
import { showError } from '../components/toast';

type View = { mode: 'edit'; name: string | null } | { mode: 'tables'; tournament: TournamentMetadata } | null;

// Mirrors ds.js's tournamentAdminClick(): EDIT tournaments (and STARTING ones
// still inside their own registration window) open the editor; STARTING
// tournaments past registration, and ACTIVE ones, open the table/round
// manager instead.
function decide(t: TournamentMetadata): View {
  const now = new Date();
  const regEnd = new Date(t.registrationEndTime);
  if (t.status === 'EDIT') return { mode: 'edit', name: t.name };
  if (t.status === 'STARTING') return now > regEnd ? { mode: 'tables', tournament: t } : { mode: 'edit', name: t.name };
  return { mode: 'tables', tournament: t };
}

export function TournamentAdminPage() {
  const [tournaments, setTournaments] = useState<TournamentMetadata[]>([]);
  const [view, setView] = useState<View>(null);

  const refreshList = () => {
    api
      .get<TournamentMetadata[]>('/tournament/admin-list')
      .then((list) => {
        setTournaments(list);
        setView((prev) => {
          if (prev?.mode !== 'tables') return prev;
          const updated = list.find((t) => t.name === prev.tournament.name);
          return updated ? { mode: 'tables', tournament: updated } : prev;
        });
      })
      .catch((err) => {
        console.error('Failed to load tournament admin list', err);
        showError('Failed to load tournament admin list.');
      });
  };

  useEffect(refreshList, []);

  return (
    <div className="tour-admin-layout p-3">
      <div className="tour-admin-col-left">
        <TournamentAdminList
          tournaments={tournaments}
          onSelect={(t) => setView(decide(t))}
          onNew={() => setView({ mode: 'edit', name: null })}
        />
      </div>
      <div className="tour-admin-col-right">
        {view?.mode === 'edit' && (
          <div className="d-flex flex-column flex-fill min-h-0">
            <TournamentEditor tournamentName={view.name} onSaved={refreshList} />
          </div>
        )}
        {view?.mode === 'tables' && (
          <div className="d-flex flex-column flex-fill min-h-0">
            <TournamentManager
              tournament={view.tournament}
              onClose={() => setView(null)}
              onChanged={refreshList}
            />
          </div>
        )}
      </div>
    </div>
  );
}
