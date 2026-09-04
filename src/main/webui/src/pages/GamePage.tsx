import { useCallback, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CardSnapshot, GameSnapshot } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { useGameSocket } from '../ws/useGameSocket';
import { runRequest } from '../api/mutate';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { useSubmitGuard } from '../hooks/useSubmitGuard';
import { PlayerBoard } from './game/PlayerBoard';
import { HandStrip } from './game/HandStrip';
import { CommandForm } from './game/CommandForm';
import { GameChatPanel } from './game/GameChatPanel';
import { HistoryPanel } from './game/HistoryPanel';
import { NotesDeckDrawer } from './game/NotesDeckDrawer';
import { useNotesIndicator } from './game/useNotesIndicator';
import { PlayCardModal, type PendingTarget } from './game/PlayCardModal';
import { CardActionModal } from './game/CardActionModal';
import { CardContextMenu, type MenuAnchor } from './game/CardContextMenu';
import { TargetPicker } from './game/TargetPicker';
import { findCardByCoordinate } from './game/coordinates';
import { buildPlayCommand, cardActions, type HandCardContext, type Submission, type TableCardContext } from './game/cardCommands';
import './GamePage.css';

// Handles card-modal.js's click-to-act interactions — play-card modal,
// on-table action modal, cross-card target picker. The free-text command
// form and the quick-command/quick-chat modals live in CommandForm.
export function GamePage() {
  const { gameId } = useParams<{ gameId: string }>();
  const navigate = useNavigate();
  const { player: viewerName } = useAuth();
  const queryClient = useQueryClient();
  const [showHistory, setShowHistory] = useState(false);
  const [notesOpen, setNotesOpen] = useState(false);
  const [playModal, setPlayModal] = useState<{ ctx: HandCardContext; card: CardSnapshot } | null>(null);
  const [cardMenu, setCardMenu] = useState<{ ctx: TableCardContext; anchor: MenuAnchor } | null>(null);
  const [tableModal, setTableModal] = useState<TableCardContext | null>(null);
  const [pendingTarget, setPendingTarget] = useState<PendingTarget | null>(null);
  const [pendingRescue, setPendingRescue] = useState<string | null>(null);
  const boardRef = useRef<HTMLDivElement>(null);
  const { submitting, guard } = useSubmitGuard();

  const { data: game, isError, refetch } = useQuery({
    queryKey: ['game', gameId],
    queryFn: () => api.get<GameSnapshot>(`/game/${gameId}/view`),
    enabled: !!gameId,
    retry: 1,
  });
  useGameSocket(gameId ?? null);
  useCardTooltips(boardRef, [game]);
  const notesIndicator = useNotesIndicator(game, notesOpen);

  const applyUpdate = useCallback(
    (updated: GameSnapshot) => queryClient.setQueryData(['game', gameId], updated),
    [queryClient, gameId],
  );

  const submit = useCallback(
    (submission: Submission) => {
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
    },
    [guard, gameId, applyUpdate],
  );

  // cardOnTableClicked()'s dual role: while a target pick is pending, a
  // click on an on-table card completes that pick (pickTarget()) instead of
  // opening the action menu.
  const handleTableCardClick = useCallback(
    (ctx: TableCardContext, anchor: MenuAnchor) => {
      if (pendingTarget) {
        const targetPlayer = ctx.controller.split(' ')[0];
        const pickedTarget = `${targetPlayer} ${ctx.regionCommandKey} ${ctx.coordinate}`;
        submit({
          command: buildPlayCommand(pendingTarget.ctx, pendingTarget.disciplines, pendingTarget.target, pickedTarget, pendingTarget.doNotReplace),
        });
        setPendingTarget(null);
        return;
      }
      if (pendingRescue) {
        submit(cardActions.rescue(pendingRescue, ctx.card.name ?? 'a vampire'));
        setPendingRescue(null);
        return;
      }
      setCardMenu({ ctx, anchor });
    },
    [pendingTarget, pendingRescue, submit],
  );

  // Rescue is the one registry action that starts a cross-card pick instead of
  // submitting straight away (see cardActionRegistry's requestTarget).
  const handleRequestTarget = useCallback((actionId: string, rescuerName: string) => {
    if (actionId === 'rescue') {
      setCardMenu(null);
      setTableModal(null);
      setPendingTarget(null);
      setPendingRescue(rescuerName);
    }
  }, []);

  const handlePlayCardClick = useCallback((ctx: HandCardContext, card: CardSnapshot) => {
    setPendingTarget(null);
    setPlayModal({ ctx, card });
  }, []);

  if (!gameId || (isError && !game)) {
    return (
      <div className="flex flex-1 min-h-0 flex-col items-center justify-center gap-3 bg-base p-8 text-center">
        <p className="text-sm text-ink">This game couldn’t be loaded.</p>
        <p className="text-xs text-ink-muted">
          It may have been closed, or you don’t have access to it.
        </p>
        <div className="mt-1 flex gap-2">
          {gameId && (
            <Button variant="secondary" size="sm" onClick={() => refetch()}>
              Try again
            </Button>
          )}
          <Button variant="primary" size="sm" onClick={() => navigate('/jol/')}>
            Back to lobby
          </Button>
        </div>
      </div>
    );
  }

  if (!game) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <Spinner />
      </div>
    );
  }

  const liveTableCard = tableModal ? findCardByCoordinate(game, tableModal.controller, tableModal.regionType, tableModal.coordinate) : null;
  const liveControllerPool = tableModal ? game.players.find((p) => p.name === tableModal.controller)?.pool : undefined;
  const livePlayCard = playModal ? findCardByCoordinate(game, viewerName ?? '', playModal.ctx.regionType, playModal.ctx.coordinate) : null;
  // Keep the open menu's card/pool live so its counter stepper reflects each bump.
  const liveMenuCard = cardMenu ? findCardByCoordinate(game, cardMenu.ctx.controller, cardMenu.ctx.regionType, cardMenu.ctx.coordinate) : null;
  const liveMenuPool = cardMenu ? game.players.find((p) => p.name === cardMenu.ctx.controller)?.pool : undefined;

  const handRegion = viewerName
    ? game.players.find((p) => p.name === viewerName)?.regions.find((r) => r.type === 'HAND')
    : undefined;

  return (
    <div className="flex flex-col flex-1 min-h-0 p-2 bg-base text-ink">
      <div className="flex items-baseline gap-x-3 gap-y-0.5 flex-wrap px-1 pb-1">
        <h1 className="text-lg font-semibold text-ink select-all">{game.name}</h1>
        <span className="flex items-center gap-1.5 text-sm text-ink-secondary">
          <span>{game.turnLabel}</span>
          <span className="text-ink-muted">·</span>
          <span>{game.phase}</span>
        </span>
        {viewerName === game.currentPlayer && (
          <span className="inline-flex items-center rounded-full border border-accent/40 bg-accent/15 px-2 py-0.5 text-xs font-medium text-accent">
            Your turn
          </span>
        )}
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto my-1" ref={boardRef}>
        <div className="control-grid">
          <HandStrip handRegion={handRegion} show={game.player && !!viewerName} onPlayCardClick={handlePlayCardClick} />
          <CommandForm
            gameId={gameId}
            game={game}
            viewerName={viewerName}
            onUpdated={applyUpdate}
            submitting={submitting}
            guard={guard}
          />
          {showHistory ? (
            <HistoryPanel
              gameId={gameId}
              game={game}
              viewerName={viewerName}
              onToggleChat={() => setShowHistory(false)}
              notesIndicator={notesIndicator}
              onOpenNotes={() => setNotesOpen(true)}
            />
          ) : (
            <GameChatPanel
              gameId={gameId}
              game={game}
              viewerName={viewerName}
              onToggleHistory={() => setShowHistory(true)}
              notesIndicator={notesIndicator}
              onOpenNotes={() => setNotesOpen(true)}
            />
          )}
        </div>
        <div className="game-board grid gap-2 mt-2 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
          {game.players.map((player) => (
            <PlayerBoard
              key={player.name}
              player={player}
              edgeColor={game.edgeColor}
              edgeTextColor={game.edgeTextColor}
              isSeatedPlayer={game.player}
              viewerName={viewerName}
              onTableCardClick={handleTableCardClick}
              onQuickCommand={submit}
              onPlayCardClick={handlePlayCardClick}
            />
          ))}
        </div>
      </div>
      <NotesDeckDrawer gameId={gameId} game={game} open={notesOpen} onClose={() => setNotesOpen(false)} />
      {cardMenu && (
        <CardContextMenu
          ctx={{
            ...cardMenu.ctx,
            ...(liveMenuCard && { card: liveMenuCard }),
            ...(liveMenuPool !== undefined && { controllerPool: liveMenuPool }),
          }}
          anchor={cardMenu.anchor}
          phase={game.phase}
          viewerName={viewerName}
          onSubmit={submit}
          onRequestTarget={handleRequestTarget}
          onOpenPanel={() => {
            setTableModal(cardMenu.ctx);
            setCardMenu(null);
          }}
          onClose={() => setCardMenu(null)}
        />
      )}
      {pendingTarget && <TargetPicker cardName={pendingTarget.cardName} onCancel={() => setPendingTarget(null)} />}
      {pendingRescue && (
        <TargetPicker
          cardName={`Rescue — ${pendingRescue}`}
          prompt="Click the vampire in torpor to rescue."
          onCancel={() => setPendingRescue(null)}
        />
      )}
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
          phase={game.phase}
          onSubmit={submit}
          onRequestTarget={handleRequestTarget}
          onClose={() => setTableModal(null)}
        />
      )}
    </div>
  );
}
