import { useQuery } from '@tanstack/react-query';
import { AlertTriangle } from 'lucide-react';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Spinner } from '../../components/ui/Spinner';
import { api } from '../../api/client';
import type { RollbackPreview, RollbackPlayerDiff } from '../../api/types';
import { runRequest } from '../../api/mutate';

function fmtVp(vp: number): string {
  return Number.isInteger(vp) ? String(vp) : vp.toFixed(1);
}

function rowChanged(d: RollbackPlayerDiff): boolean {
  return d.poolBefore !== d.poolAfter || d.vpBefore !== d.vpAfter || d.oustedBefore !== d.oustedAfter;
}

/** `12` / `12 → 9` — the "after" half only when it differs. */
function Delta({ before, after, kind }: { before: string; after: string; kind?: 'vp' | 'ousted' }) {
  if (before === after) return <span className="text-ink-muted">{before}</span>;
  const afterClass = kind === 'vp' ? 'text-gold-soft' : kind === 'ousted' ? 'text-blood-soft' : 'text-ink';
  return (
    <span>
      <span className="text-ink-muted line-through">{before}</span>
      <span className="mx-1 text-ink-muted">→</span>
      <span className={`font-semibold ${afterClass}`}>{after}</span>
    </span>
  );
}

export function RollbackPreviewModal({
  gameId,
  gameName,
  turn,
  onClose,
  onRolledBack,
}: {
  gameId: string;
  gameName: string;
  turn: string;
  onClose: () => void;
  onRolledBack: () => void;
}) {
  const { data, isPending } = useQuery({
    queryKey: ['admin-page', 'rollback-preview', gameId, turn],
    queryFn: () =>
      api.get<RollbackPreview>(
        `/admin-page/games/${encodeURIComponent(gameId)}/rollback-preview?turn=${encodeURIComponent(turn)}`,
      ),
  });

  const doRollback = () => {
    runRequest(
      api.post(`/admin-page/games/${encodeURIComponent(gameId)}/rollback`, { turn }),
      'Failed to rollback game',
      () => {
        onRolledBack();
        onClose();
      },
    );
  };

  const canConfirm = !!data?.snapshotAvailable;
  const activePlayerChanges = !!data && data.activePlayerBefore !== data.activePlayerAfter;

  return (
    <Modal
      onClose={onClose}
      title={`Roll back ${gameName}?`}
      size="lg"
      footer={
        <div className="flex justify-end gap-2 px-4 py-3">
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" onClick={doRollback} disabled={!canConfirm}>
            Roll back
          </Button>
        </div>
      }
    >
      {isPending ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : !data ? (
        <p className="text-sm text-blood-soft">Couldn’t load the rollback preview.</p>
      ) : !data.snapshotAvailable ? (
        <div className="flex items-start gap-2 rounded border border-blood/40 bg-blood/10 p-3 text-sm text-blood-soft">
          <AlertTriangle size={16} className="mt-0.5 shrink-0" />
          <span>
            No saved snapshot for turn <strong>{data.toTurn}</strong>. A rollback to this turn would fail — pick a
            different turn.
          </span>
        </div>
      ) : (
        <>
          <p className="text-sm text-ink">
            Discards <strong>{data.turnsDiscarded}</strong> {data.turnsDiscarded === 1 ? 'turn' : 'turns'} of play —
            every action after this point is lost for all players.
          </p>
          <div className="flex flex-wrap gap-x-6 gap-y-1 text-xs text-ink-secondary">
            <span>
              Turn <span className="text-ink-muted line-through">{data.fromTurn}</span>
              <span className="mx-1">→</span>
              <span className="font-semibold text-ink">{data.toTurn}</span>
            </span>
            {activePlayerChanges && (
              <span>
                Active player{' '}
                <Delta before={data.activePlayerBefore ?? '—'} after={data.activePlayerAfter ?? '—'} />
              </span>
            )}
          </div>

          <div className="overflow-auto rounded border border-line/60">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-ink-muted">
                  <th className="px-3 py-1.5 font-semibold border-b border-line">Player</th>
                  <th className="px-3 py-1.5 font-semibold border-b border-line">Pool</th>
                  <th className="px-3 py-1.5 font-semibold border-b border-line">VP</th>
                  <th className="px-3 py-1.5 font-semibold border-b border-line">Status</th>
                </tr>
              </thead>
              <tbody>
                {data.players.map((d) => {
                  const changed = rowChanged(d);
                  return (
                    <tr key={d.name} className={changed ? '' : 'text-ink-muted'}>
                      <td className="px-3 py-1.5 border-b border-line/50">{d.name}</td>
                      <td className="px-3 py-1.5 border-b border-line/50 tabular-nums">
                        <Delta before={String(d.poolBefore)} after={String(d.poolAfter)} />
                      </td>
                      <td className="px-3 py-1.5 border-b border-line/50 tabular-nums">
                        <Delta before={fmtVp(d.vpBefore)} after={fmtVp(d.vpAfter)} kind="vp" />
                      </td>
                      <td className="px-3 py-1.5 border-b border-line/50">
                        {d.oustedBefore === d.oustedAfter ? (
                          <span className="text-ink-muted">{d.oustedAfter ? 'Ousted' : 'In'}</span>
                        ) : (
                          <Delta
                            before={d.oustedBefore ? 'Ousted' : 'In'}
                            after={d.oustedAfter ? 'Ousted' : 'In'}
                            kind="ousted"
                          />
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </Modal>
  );
}
