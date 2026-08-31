import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { GripVertical, Shuffle } from 'lucide-react';
import { api } from '../../api/client';
import type { PlayerStanding, TournamentRegistration } from '../../api/types';
import { alertDialog, confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { Button } from '../../components/ui/Button';
import { Switch } from '../../components/ui/Switch';
import { InlineAlert } from '../../components/ui/FormFeedback';
import { draggableChip, dropTarget, type DragPayload } from './dragDrop';

// tournament-admin/tournament-final.jsp's #finalSeeding crypt-reveal panel
// (ds.js's startSeeding()/callbackFinalSeeding()) is never jsp:include'd by
// tournament-admin/layout.jsp — dead markup. Not ported; the player-facing
// tournament/tournament-final.jsp is the live version, out of scope here.

function Chip({ registration, from }: { registration: TournamentRegistration; from: 'pool' | 'table' }) {
  return (
    <li
      className="border border-line-accent rounded p-2 flex justify-between items-center gap-2 bg-surface cursor-grab"
      {...draggableChip({ player: registration.player, from })}
    >
      <div className="flex flex-col">
        <span>{registration.player}</span>
        <span className="font-bold text-xs">{registration.vekn}</span>
      </div>
      <GripVertical size={14} className="text-ink-muted" />
    </li>
  );
}

function FinalistSelection({ tournamentName, onSaved }: { tournamentName: string; onSaved: () => void }) {
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const { data: standings = [] } = useQuery({
    queryKey: ['tournament', tournamentName, 'standings'],
    queryFn: () => api.get<PlayerStanding[]>(`/tournament/${encodeURIComponent(tournamentName)}/standings`),
  });

  const toggle = (player: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(player)) next.delete(player);
      else next.add(player);
      return next;
    });
  };

  const save = async () => {
    if (selected.size !== 5) {
      await alertDialog('Please select exactly 5 players for the finals.');
      return;
    }
    runRequest(
      api.put(`/tournament/${encodeURIComponent(tournamentName)}/final-players`, [...selected]),
      'Failed to save finalist selection',
      onSaved,
    );
  };

  return (
    <div>
      <p className="mb-2">Select 5 players for the finals:</p>
      <div className="mb-3 flex flex-col gap-1">
        {standings.map((s, i) => (
          <Switch
            key={s.player}
            id={`finalist-${i}`}
            checked={selected.has(s.player)}
            onChange={() => toggle(s.player)}
            label={`${s.rank}. ${s.player}${s.vekn ? ` (${s.vekn})` : ''} — ${s.gw} GW, ${s.vp} VP`}
          />
        ))}
      </div>
      <Button size="sm" className="bg-online text-surface hover:opacity-90" onClick={save}>
        Save Finals Selection
      </Button>
    </div>
  );
}

function FinalTableBuilder({ tournamentName, onSaved }: { tournamentName: string; onSaved: () => void }) {
  const queryClient = useQueryClient();
  const [pool, setPool] = useState<TournamentRegistration[]>([]);
  const [table, setTable] = useState<TournamentRegistration[]>([]);

  const queryKey = ['tournament', tournamentName, 'final-table'];
  const { data } = useQuery({
    queryKey,
    queryFn: () =>
      Promise.all([
        api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/final-players`),
        api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/final-delta`),
        api.get<boolean>(`/tournament/${encodeURIComponent(tournamentName)}/game-started`),
      ]).then(([finalPlayers, delta, isStarted]) => ({ finalPlayers, delta, isStarted })),
  });

  const started = data?.isStarted ?? false;

  useEffect(() => {
    if (!data) return;
    setTable(data.finalPlayers);
    setPool(data.delta);
  }, [data]);

  const load = () => queryClient.invalidateQueries({ queryKey });

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
    runRequest(
      api.put(
        `/tournament/${encodeURIComponent(tournamentName)}/final-players`,
        table.map((r) => r.player),
      ),
      'Failed to save final table',
      () => {
        onSaved();
        load();
      },
    );
  };

  const startFinal = async () => {
    if (!(await confirmDialog('Are you sure you want to START the FINAL?'))) return;
    runRequest(api.post(`/tournament/${encodeURIComponent(tournamentName)}/final`), 'Failed to start final', load);
  };

  if (started) {
    return (
      <InlineAlert kind="success" className="mt-2">
        Finals already started — see seating below.
      </InlineAlert>
    );
  }

  return (
    <div>
      <div className="flex gap-1 flex-wrap mb-2">
        <Button variant="secondary" size="sm" onClick={saveFinal}>
          Save Final
        </Button>
        <Button size="sm" className="bg-online text-surface hover:opacity-90" onClick={startFinal}>
          Start Final
        </Button>
      </div>
      <span className="text-lg font-semibold">Tournament Players</span>
      <ul className="list-none flex flex-wrap gap-2 p-1 min-h-8" {...dropTarget((payload) => move(payload, 'pool'))}>
        {pool.map((r) => (
          <Chip key={r.player} registration={r} from="pool" />
        ))}
      </ul>
      <div className="p-1">
        <span className="text-lg font-semibold flex items-center gap-2">
          Final Table <Shuffle size={16} role="button" className="cursor-pointer text-ink-muted hover:text-ink" onClick={shuffle} />
        </span>
        <ul
          className="border border-line rounded list-none min-h-9 p-1 flex flex-col gap-1"
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
  const queryClient = useQueryClient();
  const queryKey = ['tournament', tournamentName, 'seeding'];

  const { data: seeding } = useQuery({
    queryKey,
    queryFn: () => api.get<string[]>(`/tournament/${encodeURIComponent(tournamentName)}/seeding`),
  });

  const load = () => queryClient.invalidateQueries({ queryKey });

  if (seeding === undefined) return null;

  return (
    <div className="flex-1 min-h-0 overflow-auto mt-2">
      {seeding.length > 0 ? (
        <FinalTableBuilder tournamentName={tournamentName} onSaved={load} />
      ) : (
        <FinalistSelection tournamentName={tournamentName} onSaved={load} />
      )}
    </div>
  );
}
