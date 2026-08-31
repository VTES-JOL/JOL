import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useInvalidate } from '../api/useInvalidate';
import type { TournamentList as TournamentListData } from '../api/types';
import { TournamentList, type Selection } from './tournament/TournamentList';
import { OpenTournamentDetail } from './tournament/OpenTournamentDetail';
import { FinalsTournamentDetail } from './tournament/FinalsTournamentDetail';
import { PageLoading } from '../components/PageLoading';
import { PanelPlaceholder } from '../components/PanelPlaceholder';
import { SplitLayout } from '../components/SplitLayout';

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

  if (!data) return <PageLoading />;

  const openTournament = selection?.type === 'open' ? data.tournaments.find((t) => t.name === selection.name) : null;
  const finalsTournament =
    selection?.type === 'finals' ? data.finalsInvites.find((t) => t.name === selection.name) : null;

  return (
    <SplitLayout
      stackBelowLg
      left={
        <TournamentList
          tournaments={data.tournaments}
          finalsInvites={data.finalsInvites}
          selection={selection}
          onSelect={setSelection}
        />
      }
      right={
        <>
          {openTournament && (
            <OpenTournamentDetail
              tournament={openTournament}
              onJoinedOrLeft={() => {
                refreshList();
                refreshRegistered();
              }}
              onDeckChanged={refreshRegistered}
            />
          )}
          {finalsTournament && <FinalsTournamentDetail tournament={finalsTournament} />}
          {!openTournament && !finalsTournament && <PanelPlaceholder icon="bi-trophy" message="Select a tournament" />}
        </>
      }
    />
  );
}
