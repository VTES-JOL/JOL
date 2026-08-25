import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { TournamentBean } from '../api/types';
import { TournamentList, type Selection } from './tournament/TournamentList';
import { OpenTournamentDetail } from './tournament/OpenTournamentDetail';
import { FinalsTournamentDetail } from './tournament/FinalsTournamentDetail';
import { PageLoading } from '../components/PageLoading';
import { EmptyState } from '../components/EmptyState';

const TOURNAMENT_QUERY_KEY = ['tournament', 'player-list'];

export function TournamentPage() {
  const queryClient = useQueryClient();
  const [selection, setSelection] = useState<Selection>(null);

  const { data } = useQuery({
    queryKey: TOURNAMENT_QUERY_KEY,
    queryFn: () => api.get<TournamentBean>('/tournament/player-list'),
  });

  const applyUpdate = (updated: TournamentBean) => queryClient.setQueryData(TOURNAMENT_QUERY_KEY, updated);

  if (!data) return <PageLoading />;

  const openTournament = selection?.type === 'open' ? data.tournaments.find((t) => t.name === selection.name) : null;
  const finalsTournament =
    selection?.type === 'finals' ? data.finalsInvites.find((t) => t.name === selection.name) : null;

  return (
    <div className="row g-2 flex-fill align-items-stretch min-h-0 p-3">
      <div className="col-lg-4 d-flex flex-column">
        <TournamentList
          tournaments={data.tournaments}
          finalsInvites={data.finalsInvites}
          selection={selection}
          onSelect={setSelection}
        />
      </div>
      <div className="col-lg-8 d-flex flex-column">
        {openTournament && (
          <OpenTournamentDetail
            tournament={openTournament}
            veknLinked={data.veknLinked}
            registeredGames={data.registeredGames}
            decks={data.decks}
            onChanged={applyUpdate}
          />
        )}
        {finalsTournament && <FinalsTournamentDetail tournament={finalsTournament} />}
        {!openTournament && !finalsTournament && <EmptyState icon="bi-trophy" message="Select a tournament" />}
      </div>
    </div>
  );
}
