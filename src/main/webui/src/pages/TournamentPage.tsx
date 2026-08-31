import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Trophy } from 'lucide-react';
import { api } from '../api/client';
import type { TournamentList as TournamentListData } from '../api/types';
import { TournamentList, type Selection } from './tournament/TournamentList';
import { OpenTournamentDetail } from './tournament/OpenTournamentDetail';
import { FinalsTournamentDetail } from './tournament/FinalsTournamentDetail';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { MasterDetailView } from '../components/ui/MasterDetailView';
import { useInvalidate } from '../api/useInvalidate';

const TOURNAMENT_LIST_QUERY_KEY = ['tournament', 'list'];
const TOURNAMENT_REGISTERED_QUERY_KEY = ['tournament', 'registered'];

// Each widget below fetches its own slice (see tournament/*.tsx) —
// registering a tournament deck no longer forces the tournament list to
// refetch, and vice versa. See TournamentResource for the backend side of
// this split (was previously one combined TournamentBean).
export function TournamentPage() {
  const [selection, setSelection] = useState<Selection>(null);

  const { data } = useQuery({
    queryKey: TOURNAMENT_LIST_QUERY_KEY,
    queryFn: () => api.get<TournamentListData>('/tournament/list'),
  });

  // Join/leave change tournament.registered on the list itself; deck
  // registration doesn't, so it only needs to invalidate ['tournament','registered'].
  const refreshList = useInvalidate(TOURNAMENT_LIST_QUERY_KEY);
  const refreshRegistered = useInvalidate(TOURNAMENT_REGISTERED_QUERY_KEY);

  if (!data) {
    return (
      <div className="jt-scope jt:flex jt:flex-1 jt:min-h-0 jt:items-center jt:justify-center jt:bg-base">
        <Spinner />
      </div>
    );
  }

  const openTournament =
    selection?.type === 'open' ? data.tournaments.find((t) => t.name === selection.name) : null;
  const finalsTournament =
    selection?.type === 'finals' ? data.finalsInvites.find((t) => t.name === selection.name) : null;
  const hasSelection = Boolean(openTournament || finalsTournament);

  return (
    <div className="jt-scope jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:p-4 jt:bg-base jt:text-ink">
      <MasterDetailView
        breakpoint="lg"
        columns="340px minmax(360px, 1fr)"
        activeKey={hasSelection ? 'detail' : 'list'}
        onActiveKeyChange={(k) => {
          if (k === 'list') setSelection(null);
        }}
        panels={[
          {
            key: 'list',
            label: 'Tournaments',
            content: (
              <TournamentList
                tournaments={data.tournaments}
                finalsInvites={data.finalsInvites}
                selection={selection}
                onSelect={setSelection}
              />
            ),
          },
          {
            key: 'detail',
            label: 'Details',
            content: openTournament ? (
              <OpenTournamentDetail
                tournament={openTournament}
                onJoinedOrLeft={() => {
                  refreshList();
                  refreshRegistered();
                }}
                onDeckChanged={refreshRegistered}
              />
            ) : finalsTournament ? (
              <FinalsTournamentDetail tournament={finalsTournament} />
            ) : (
              <EmptyState icon={Trophy} title="Select a tournament" />
            ),
          },
        ]}
      />
    </div>
  );
}
