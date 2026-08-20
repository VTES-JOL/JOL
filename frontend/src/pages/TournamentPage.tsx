import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { TournamentBean } from '../api/types';
import { TournamentList, type Selection } from './tournament/TournamentList';
import { OpenTournamentDetail } from './tournament/OpenTournamentDetail';
import { FinalsTournamentDetail } from './tournament/FinalsTournamentDetail';

export function TournamentPage() {
  const [data, setData] = useState<TournamentBean | null>(null);
  const [selection, setSelection] = useState<Selection>(null);

  const refresh = () => {
    api
      .get<TournamentBean>('/tournament/player-list')
      .then(setData)
      .catch((err) => console.error('Failed to load tournaments', err));
  };

  useEffect(refresh, []);

  if (!data) return null;

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
            onChanged={setData}
          />
        )}
        {finalsTournament && <FinalsTournamentDetail tournament={finalsTournament} />}
      </div>
    </div>
  );
}
