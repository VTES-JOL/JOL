import { useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CardSnapshot, GameSnapshot } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { useGameSocket } from '../ws/useGameSocket';
import { runRequest } from '../api/mutate';
import { PageLoading } from '../components/PageLoading';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { useSubmitGuard } from '../hooks/useSubmitGuard';
import { PlayerBoard } from './game/PlayerBoard';
import { HandStrip } from './game/HandStrip';
import { CommandForm } from './game/CommandForm';
import { GameChatPanel } from './game/GameChatPanel';
import { HistoryPanel } from './game/HistoryPanel';
import { NotesPanel } from './game/NotesPanel';
import { DeckPanel } from './game/DeckPanel';
import { PlayCardModal, type PendingTarget } from './game/PlayCardModal';
import { CardActionModal } from './game/CardActionModal';
import { TargetPicker } from './game/TargetPicker';
import { findCardByCoordinate } from './game/coordinates';
import { buildPlayCommand, type HandCardContext, type Submission, type TableCardContext } from './game/cardCommands';
import './GamePage.css';

// Handles card-modal.js's click-to-act interactions — play-card modal,
// on-table action modal, cross-card target picker. The free-text command
// form and the quick-command/quick-chat modals live in CommandForm.
export function GamePage() {
  const { gameId } = useParams<{ gameId: string }>();
  const { player: viewerName } = useAuth();
  const queryClient = useQueryClient();
  const [showHistory, setShowHistory] = useState(false);
  const [showDeck, setShowDeck] = useState(false);
  const [playModal, setPlayModal] = useState<{ ctx: HandCardContext; card: CardSnapshot } | null>(null);
  const [tableModal, setTableModal] = useState<TableCardContext | null>(null);
  const [pendingTarget, setPendingTarget] = useState<PendingTarget | null>(null);
  const boardRef = useRef<HTMLDivElement>(null);
  const { submitting, guard } = useSubmitGuard();

  const { data: game } = useQuery({
    queryKey: ['game', gameId],
    queryFn: () => api.get<GameSnapshot>(`/game/${gameId}/view`),
    enabled: !!gameId,
  });
  useGameSocket(gameId ?? null);
  useCardTooltips(boardRef, [game]);

  const applyUpdate = (updated: GameSnapshot) => queryClient.setQueryData(['game', gameId], updated);

  if (!game || !gameId) return <PageLoading />;

  const submit = (submission: Submission) => {
    guard(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/view/submit`, {
          phase: null,
          command: submission.command ?? null,
          chat: submission.chat ?? null,
          ping: null,
        }),
        'Failed to submit',
        applyUpdate,
      ),
    );
  };

  // cardOnTableClicked()'s dual role: while a target pick is pending, a
  // click on an on-table card completes that pick (pickTarget()) instead of
  // opening the action modal.
  const handleTableCardClick = (ctx: TableCardContext) => {
    if (pendingTarget) {
      const targetPlayer = ctx.controller.split(' ')[0];
      const pickedTarget = `${targetPlayer} ${ctx.regionCommandKey} ${ctx.coordinate}`;
      submit({
        command: buildPlayCommand(pendingTarget.ctx, pendingTarget.disciplines, pendingTarget.target, pickedTarget, pendingTarget.doNotReplace),
      });
      setPendingTarget(null);
      return;
    }
    setTableModal(ctx);
  };

  const handlePlayCardClick = (ctx: HandCardContext, card: CardSnapshot) => {
    setPendingTarget(null);
    setPlayModal({ ctx, card });
  };

  const liveTableCard = tableModal ? findCardByCoordinate(game, tableModal.controller, tableModal.regionType, tableModal.coordinate) : null;
  const liveControllerPool = tableModal ? game.players.find((p) => p.name === tableModal.controller)?.pool : undefined;
  const livePlayCard = playModal ? findCardByCoordinate(game, viewerName ?? '', playModal.ctx.regionType, playModal.ctx.coordinate) : null;

  return (
    <div className="flex-fill d-flex flex-column min-h-0 p-2">
      <h5 className="w-100 d-flex justify-content-between align-items-center">
        <span className="fs-5 user-select-all">{game.name}</span>
      </h5>
      <div className="container-fluid my-1 g-0 flex-grow-1 min-h-0 overflow-y-auto" ref={boardRef}>
        <div className="control-grid">
          <HandStrip game={game} viewerName={viewerName} onPlayCardClick={handlePlayCardClick} />
          <CommandForm
            gameId={gameId}
            game={game}
            viewerName={viewerName}
            onUpdated={applyUpdate}
            submitting={submitting}
            guard={guard}
          />
          {showHistory ? (
            <HistoryPanel gameId={gameId} game={game} viewerName={viewerName} onToggleChat={() => setShowHistory(false)} />
          ) : (
            <GameChatPanel gameId={gameId} game={game} viewerName={viewerName} onToggleHistory={() => setShowHistory(true)} />
          )}
          {showDeck ? (
            <DeckPanel gameId={gameId} onToggleNotes={() => setShowDeck(false)} />
          ) : (
            <NotesPanel gameId={gameId} game={game} onToggleDeck={() => setShowDeck(true)} />
          )}
        </div>
        <div className="row gx-2">
          <div className="col-12 row gy-1 gx-2">
            {game.players.map((player) => (
              <PlayerBoard
                key={player.name}
                player={player}
                game={game}
                viewerName={viewerName}
                onTableCardClick={handleTableCardClick}
                onPlayCardClick={handlePlayCardClick}
              />
            ))}
          </div>
        </div>
      </div>
      {pendingTarget && <TargetPicker cardName={pendingTarget.cardName} onCancel={() => setPendingTarget(null)} />}
      {playModal && viewerName && (
        <PlayCardModal
          ctx={playModal.ctx}
          card={livePlayCard ?? playModal.card}
          viewerName={viewerName}
          onSubmit={submit}
          onClose={() => setPlayModal(null)}
          onRequestTarget={setPendingTarget}
        />
      )}
      {tableModal && (
        <CardActionModal
          ctx={{
            ...tableModal,
            ...(liveTableCard && { card: liveTableCard }),
            ...(liveControllerPool !== undefined && { controllerPool: liveControllerPool }),
          }}
          viewerName={viewerName}
          onSubmit={submit}
          onClose={() => setTableModal(null)}
        />
      )}
    </div>
  );
}
