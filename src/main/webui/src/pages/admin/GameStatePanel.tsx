import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Droplet, ExternalLink, Swords } from 'lucide-react';
import { Card, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { api } from '../../api/client';
import type { AdminGameState } from '../../api/types';
import { pathForGame } from '../../routes';
import { relativeTime } from '../../utils/relativeTime';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';
import { adminTimestamp } from './adminFormatting';
import { resolvePlayerName } from './adminControls';
import { RollbackPreviewModal } from './RollbackPreviewModal';

function formatVp(vp: number): string {
  return Number.isInteger(vp) ? String(vp) : vp.toFixed(1);
}

/**
 * The Games tab's single game context: pick a game above, and this shows its
 * live table state — seating, current player, edge, pool/VP, and each
 * player's last-access time — with the admin actions (end turn, roll back,
 * replace a player) acting on that one game.
 */
export function GameStatePanel({ gameId, gameLabel }: { gameId: string; gameLabel: string }) {
  const queryClient = useQueryClient();
  const { data: state, isPending } = useQuery({
    queryKey: ['admin-page', 'game-state', gameId],
    queryFn: () => api.get<AdminGameState>(`/admin-page/games/${encodeURIComponent(gameId)}/state`),
  });
  const { data: turns = [] } = useQuery({
    queryKey: ['admin-page', 'game-turns', gameId],
    queryFn: () => api.get<string[]>(`/game/${encodeURIComponent(gameId)}/turns`),
  });
  // `substitutes` = who's around right now (seeds the field with a sensible
  // default); `allPlayers` = the full list the autocomplete resolves against,
  // so an admin can hand a seat to someone specific who isn't recently active.
  const { data: substitutes = [] } = useQuery({
    queryKey: ['admin-page', 'substitutes'],
    queryFn: () => api.get<string[]>('/admin-page/substitutes'),
  });
  const { data: allPlayers = [] } = useQuery({
    queryKey: ['admin-page', 'players'],
    queryFn: () => api.get<string[]>('/admin-page/players'),
  });

  const [rollbackTurn, setRollbackTurn] = useState('');
  const [rollbackOpen, setRollbackOpen] = useState(false);
  const [replacingSeat, setReplacingSeat] = useState<number | null>(null);
  const [substitute, setSubstitute] = useState('');

  const gameName = state?.gameName ?? gameLabel;
  const turn = rollbackTurn || turns[0] || '';

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-page', 'game-state', gameId] });
    queryClient.invalidateQueries({ queryKey: ['admin-page', 'game-turns', gameId] });
    queryClient.invalidateQueries({ queryKey: ['admin-page', 'idle-games'] });
  };

  const endTurn = async () => {
    if (
      !(await confirmDialog(`This forces the current turn to end in ${gameName}.`, {
        title: 'End the current turn?',
        confirmLabel: 'End turn',
      }))
    )
      return;
    runRequest(api.post(`/admin-page/games/${encodeURIComponent(gameId)}/end-turn`), 'Failed to end turn', invalidate);
  };

  const startReplace = (seat: number) => {
    setReplacingSeat(seat);
    setSubstitute(substitutes[0] ?? '');
  };

  // The substitute must be a real player, not already seated in this game, and
  // not the player being replaced — the backend would otherwise seat a name
  // with no PlayerInfo and corrupt game state.
  const seated = new Set(state?.players.map((p) => p.name) ?? []);
  const resolvedSub = resolvePlayerName(substitute, allPlayers);
  const subError =
    !substitute.trim() || !resolvedSub
      ? substitute.trim() && !resolvedSub
        ? 'No player by that name'
        : undefined
      : seated.has(resolvedSub)
        ? 'Already in this game'
        : undefined;
  const subValid = !!resolvedSub && !seated.has(resolvedSub);

  const confirmReplace = async (existingPlayer: string) => {
    if (!resolvedSub || !subValid || resolvedSub === existingPlayer) return;
    if (
      !(await confirmDialog(`${existingPlayer} is swapped out of ${gameName} and ${resolvedSub} takes over their seat, deck and hand.`, {
        title: 'Replace this player?',
        confirmLabel: 'Replace player',
        danger: true,
      }))
    )
      return;
    setReplacingSeat(null);
    runRequest(
      api.put(`/admin-page/games/${encodeURIComponent(gameId)}/replace-player`, { existingPlayer, newPlayer: resolvedSub }),
      'Failed to replace player',
      invalidate,
    );
  };

  const th = 'px-3 py-1.5 text-left font-semibold text-ink-muted border-b border-line whitespace-nowrap';
  const td = 'px-3 py-1.5 border-b border-line/50 align-middle';

  return (
    <Card>
      <CardHeader className="flex flex-wrap items-center justify-between gap-2">
        <CardTitle>
          {gameName}
          {state && (
            <span className="ml-2 inline-flex items-center gap-2 align-middle text-xs font-normal text-ink-muted">
              <Badge variant="format">{state.format}</Badge>
              <span>Turn {state.turn}</span>
              <span title={adminTimestamp(state.gameTimestamp)}>· active {relativeTime(state.gameTimestamp)}</span>
            </span>
          )}
        </CardTitle>
        <Link to={pathForGame(gameId)} className="inline-flex items-center gap-1 text-xs text-accent hover:underline">
          Open game <ExternalLink size={12} />
        </Link>
      </CardHeader>

      <div className="flex flex-wrap items-end gap-3 border-b border-line bg-panel/30 px-3 py-2">
        <Button variant="secondary" size="sm" className="whitespace-nowrap" onClick={endTurn} disabled={!state}>
          End turn
        </Button>
        <div className="flex items-end gap-2">
          <Select
            id="adminRollbackTurn"
            size="sm"
            label="Roll back to"
            value={turn}
            onChange={(e) => setRollbackTurn(e.target.value)}
          >
            {turns.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </Select>
          <Button
            variant="danger"
            size="sm"
            className="whitespace-nowrap"
            onClick={() => setRollbackOpen(true)}
            disabled={!turn}
          >
            Roll back
          </Button>
        </div>
      </div>

      {rollbackOpen && turn && (
        <RollbackPreviewModal
          gameId={gameId}
          gameName={gameName}
          turn={turn}
          onClose={() => setRollbackOpen(false)}
          onRolledBack={invalidate}
        />
      )}

      <div className="overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr>
              <th className={`${th} w-8 text-center`}>#</th>
              <th className={th}>Player</th>
              <th className={`${th} text-right`}>Pool</th>
              <th className={`${th} text-right`}>VP</th>
              <th className={th}>Last seen</th>
              <th className={`${th} text-right`}>Action</th>
            </tr>
          </thead>
          <tbody>
            {isPending && (
              <tr>
                <td className={`${td} text-ink-muted`} colSpan={6}>
                  Loading game state…
                </td>
              </tr>
            )}
            {state?.players.map((p) => {
              const active = p.name === state.activePlayer;
              const hasEdge = !!state.edge && p.name === state.edge;
              return (
                <tr key={p.name} className={active ? 'bg-arcane/10' : p.ousted ? 'text-ink-muted' : 'hover:bg-hover'}>
                  <td className={`${td} text-center tabular-nums text-ink-muted`}>{p.seat}</td>
                  <td className={td}>
                    <span className={`inline-flex items-center gap-1 ${active ? 'font-semibold text-arcane-soft' : 'text-ink'}`}>
                      {p.name}
                      {hasEdge && <Swords size={12} className="text-gold-soft" aria-label="has the edge" />}
                      {p.ousted && (
                        <span className="rounded bg-blood/15 px-1 py-0.5 text-[10px] font-bold text-blood-soft">OUT</span>
                      )}
                    </span>
                  </td>
                  <td className={`${td} text-right tabular-nums`}>
                    <span className="inline-flex items-center justify-end gap-0.5">
                      <Droplet size={11} className="fill-current text-blood" />
                      {p.pool}
                    </span>
                  </td>
                  <td className={`${td} text-right tabular-nums`}>
                    {p.vp > 0 ? <span className="font-semibold text-gold-soft">{formatVp(p.vp)}</span> : <span className="text-ink-muted">–</span>}
                  </td>
                  <td className={`${td} whitespace-nowrap text-ink-secondary`} title={adminTimestamp(p.lastAccess)}>
                    {relativeTime(p.lastAccess)}
                  </td>
                  <td className={`${td} text-right`}>
                    {replacingSeat === p.seat ? (
                      <span className="inline-flex items-start gap-1">
                        <span className="w-44 text-left">
                          <Input
                            id="adminReplaceSubstitute"
                            size="sm"
                            srLabel="Substitute"
                            list="admin-replace-players"
                            placeholder="Type a player name…"
                            autoComplete="off"
                            value={substitute}
                            onChange={(e) => setSubstitute(e.target.value)}
                            error={resolvedSub === p.name ? 'Same player' : subError}
                          />
                        </span>
                        <Button
                          variant="danger"
                          size="sm"
                          className="whitespace-nowrap"
                          onClick={() => confirmReplace(p.name)}
                          disabled={!subValid || resolvedSub === p.name}
                        >
                          Confirm
                        </Button>
                        <Button variant="secondary" size="sm" className="whitespace-nowrap" onClick={() => setReplacingSeat(null)}>
                          Cancel
                        </Button>
                      </span>
                    ) : (
                      <Button variant="secondary" size="sm" onClick={() => startReplace(p.seat)}>
                        Replace
                      </Button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        <datalist id="admin-replace-players">
          {allPlayers.map((s) => (
            <option key={s} value={s} />
          ))}
        </datalist>
        {replacingSeat !== null && substitutes.length === 0 && (
          <p className="px-3 py-2 text-xs italic text-ink-muted">
            No recently-active players to suggest — type any player’s name.
          </p>
        )}
      </div>
    </Card>
  );
}
