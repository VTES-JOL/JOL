import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import type { PlayerStanding, TournamentRegistration } from '../../api/types';
import { draggableChip, dropTarget, type DragPayload } from './dragDrop';

// tournament-admin/tournament-final.jsp's #finalSeeding crypt-reveal panel
// (ds.js's startSeeding()/callbackFinalSeeding()) is never jsp:include'd by
// tournament-admin/layout.jsp — dead markup, same as links.jsp/dark-pack.jsp
// were for main. Not ported; the player-facing tournament/tournament-final.jsp
// is the live version of that feature, out of scope here.

function Chip({ registration, from }: { registration: TournamentRegistration; from: 'pool' | 'table' }) {
  return (
    <li
      className="border rounded p-2 border-secondary d-flex justify-content-between align-items-center"
      {...draggableChip({ player: registration.player, from })}
    >
      <div className="d-flex flex-column">
        <span>{registration.player}</span>
        <span className="fw-bold">{registration.vekn}</span>
      </div>
      <i className="bi bi-grip-vertical" />
    </li>
  );
}

function FinalistSelection({ tournamentName, onSaved }: { tournamentName: string; onSaved: () => void }) {
  const [standings, setStandings] = useState<PlayerStanding[]>([]);
  const [selected, setSelected] = useState<Set<string>>(new Set());

  useEffect(() => {
    api
      .get<PlayerStanding[]>(`/tournament/${encodeURIComponent(tournamentName)}/standings`)
      .then(setStandings)
      .catch((err) => console.error('Failed to load standings', err));
  }, [tournamentName]);

  const toggle = (player: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(player)) next.delete(player);
      else next.add(player);
      return next;
    });
  };

  const save = () => {
    if (selected.size !== 5) {
      alert('Please select exactly 5 players for the finals.');
      return;
    }
    api
      .put(`/tournament/${encodeURIComponent(tournamentName)}/final-players`, [...selected])
      .then(onSaved)
      .catch((err) => console.error('Failed to save finalist selection', err));
  };

  return (
    <div>
      <p className="mb-2">Select 5 players for the finals:</p>
      <div className="mb-3">
        {standings.map((s, i) => (
          <div className="form-check" key={s.player}>
            <input
              className="form-check-input"
              type="checkbox"
              id={`finalist-${i}`}
              checked={selected.has(s.player)}
              onChange={() => toggle(s.player)}
            />
            <label className="form-check-label" htmlFor={`finalist-${i}`}>
              {s.rank}. {s.player}
              {s.vekn ? ` (${s.vekn})` : ''} — {s.gw} GW, {s.vp} VP
            </label>
          </div>
        ))}
      </div>
      <button className="btn btn-success btn-sm" onClick={save}>
        Save Finals Selection
      </button>
    </div>
  );
}

function FinalTableBuilder({ tournamentName, onSaved }: { tournamentName: string; onSaved: () => void }) {
  const [pool, setPool] = useState<TournamentRegistration[]>([]);
  const [table, setTable] = useState<TournamentRegistration[]>([]);
  const [started, setStarted] = useState(false);

  const load = () => {
    Promise.all([
      api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/final-players`),
      api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/final-delta`),
      api.get<boolean>(`/tournament/${encodeURIComponent(tournamentName)}/game-started`),
    ])
      .then(([finalPlayers, delta, isStarted]) => {
        setTable(finalPlayers);
        setPool(delta);
        setStarted(isStarted);
      })
      .catch((err) => console.error('Failed to load final table', err));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(load, [tournamentName]);

  const move = (payload: DragPayload, to: 'pool' | 'table') => {
    if (payload.from === to) return;
    const fromList = payload.from === 'pool' ? pool : table;
    const reg = fromList.find((r) => r.player === payload.player);
    if (!reg) return;
    if (payload.from === 'pool') setPool((prev) => prev.filter((r) => r.player !== payload.player));
    else setTable((prev) => prev.filter((r) => r.player !== payload.player));
    if (to === 'pool') setPool((prev) => [...prev, reg]);
    else setTable((prev) => [...prev, reg]);
  };

  const shuffle = () => {
    setTable((prev) => {
      if (prev.length === 0) return prev;
      const start = Math.floor(Math.random() * prev.length);
      return [...prev.slice(start), ...prev.slice(0, start)];
    });
  };

  const saveFinal = () => {
    api
      .put(
        `/tournament/${encodeURIComponent(tournamentName)}/final-players`,
        table.map((r) => r.player),
      )
      .then(() => {
        onSaved();
        load();
      })
      .catch((err) => console.error('Failed to save final table', err));
  };

  const startFinal = () => {
    if (!confirm('Are you sure you want to START the FINAL?')) return;
    api
      .post(`/tournament/${encodeURIComponent(tournamentName)}/final`)
      .then(load)
      .catch((err) => console.error('Failed to start final', err));
  };

  if (started) {
    return <div className="alert alert-info mt-2">Finals already started — see seating below.</div>;
  }

  return (
    <div>
      <div className="d-flex gap-1 flex-wrap mb-2">
        <button onClick={saveFinal} className="btn btn-outline-secondary btn-sm">
          Save Final
        </button>
        <button onClick={startFinal} className="btn btn-outline-success btn-sm">
          Start Final
        </button>
      </div>
      <span className="h4">Tournament Players</span>
      <ul className="list-unstyled d-flex flex-wrap gap-2 p-1" {...dropTarget((payload) => move(payload, 'pool'))}>
        {pool.map((r) => (
          <Chip key={r.player} registration={r} from="pool" />
        ))}
      </ul>
      <div className="card-body p-1">
        <span className="h4">
          Final Table <i className="bi bi-shuffle ms-2" role="button" onClick={shuffle} />
        </span>
        <ul
          className="border list-group"
          style={{ minHeight: 38 }}
          {...dropTarget((payload) => move(payload, 'table'))}
        >
          {table.map((r) => (
            <Chip key={r.player} registration={r} from="table" />
          ))}
        </ul>
      </div>
    </div>
  );
}

export function FinalsManager({ tournamentName }: { tournamentName: string }) {
  const [seeding, setSeeding] = useState<string[] | null>(null);

  const load = () => {
    api
      .get<string[]>(`/tournament/${encodeURIComponent(tournamentName)}/seeding`)
      .then(setSeeding)
      .catch((err) => console.error('Failed to load finals seeding', err));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(load, [tournamentName]);

  if (seeding === null) return null;

  return (
    <div className="flex-fill overflow-auto mt-2 min-h-0">
      {seeding.length > 0 ? (
        <FinalTableBuilder tournamentName={tournamentName} onSaved={load} />
      ) : (
        <FinalistSelection tournamentName={tournamentName} onSaved={load} />
      )}
    </div>
  );
}
