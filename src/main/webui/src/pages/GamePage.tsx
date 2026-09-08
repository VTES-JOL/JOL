import { useCallback, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CardSnapshot, GameSnapshot } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { useGameSocket } from '../ws/useGameSocket';
import { showError } from '../stores/toast';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { useSubmitGuard } from '../hooks/useSubmitGuard';
import { useMediaQuery, useIsMobile } from '../hooks/useMediaQuery';
import { useNav } from '../auth/useNav';
import { useCounterBump } from './game/useCounterBump';
import { useCommandStatus } from './game/useCommandStatus';
import { submitHeaders } from './game/submitId';
import { seatOrder, relationOf, type SeatRelation } from './game/seatOrder';
import { TableHud } from './game/TableHud';
import { SeatColumn } from './game/SeatColumn';
import { YourSeatDock } from './game/YourSeatDock';
import { HandStrip } from './game/HandStrip';
import { CommandForm } from './game/CommandForm';
import { GameChatPanel } from './game/GameChatPanel';
import { ChatCompose } from './game/ChatCompose';
import { HistoryPanel } from './game/HistoryPanel';
import { NotesDeckDrawer } from './game/NotesDeckDrawer';
import { useNotesIndicator } from './game/useNotesIndicator';
import { PlayCardModal, type PendingTarget } from './game/PlayCardModal';
import { CardContextMenu, type MenuAnchor } from './game/CardContextMenu';
import { TextModeContext } from './game/textMode';
import { SeatPager } from './game/SeatPager';
import { BottomSheet } from './game/BottomSheet';
import { MobileTabBar, type MobileTab } from './game/MobileTabBar';
import { CallJudgeButton } from './game/CallJudgeButton';
import { TargetPicker } from './game/TargetPicker';
import { findCardByCoordinate, findCardByCommandCoordinate } from './game/coordinates';
import { buildPlayCommand, cardActions, type HandCardContext, type Submission, type TableCardContext } from './game/cardCommands';
import './GamePage.css';

// Optimistic (silent-reconcile, D15) pre-write for the cheap, safe-to-predict
// commands. Returns the pre-mutation snapshot so a *network* failure can roll
// it back; a server *rejection* (HTTP 200 + rejected) reconciles normally via
// applyUpdate — no rollback branch, no toast (the HUD strip carries the reason).
function optimisticDraft(prev: GameSnapshot, command: string | undefined, viewerName: string | null): GameSnapshot | null {
  if (!command) return null;
  const c = command.trim();
  const draft = structuredClone(prev);
  let changed = false;

  // `lock|unlock <playerFirst> <regionKey> <coord>` — also emitted by
  // bleed/hunt/go-anarch/leave-torpor (they lock the acting minion).
  const lock = /^(lock|unlock)\s+(\S+)\s+(\S+)\s+([\d.]+)\b/.exec(c);
  if (lock) {
    const card = findCardByCommandCoordinate(draft, lock[2], lock[3], lock[4]);
    if (card) {
      card.locked = lock[1] === 'lock';
      changed = true;
    }
  }

  // `play <regionKey> <coord> …` from the viewer's own hand/research — the card
  // is leaving that slot regardless of where it lands, so drop it now; server
  // truth fills in the destination and the replacement draw.
  const play = /^play\s+(hand|research)\s+(\d+)/.exec(c);
  if (play && viewerName) {
    const me = draft.players.find((p) => p.name === viewerName);
    const region = me?.regions.find((r) => r.commandKey === play[1]);
    const idx = Number(play[2]) - 1;
    if (region && region.cards[idx]) {
      region.cards.splice(idx, 1);
      changed = true;
    }
  }

  return changed ? draft : null;
}

// Handles card-modal.js's click-to-act interactions — play-card modal,
// on-table action modal, cross-card target picker. The free-text command
// form and the quick-command/quick-chat modals live in CommandForm; the
// persistent turn/phase readout lives in TableHud.
export function GamePage() {
  const { gameId } = useParams<{ gameId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { player: viewerName } = useAuth();
  const [showHistory, setShowHistory] = useState(false);
  const [notesOpen, setNotesOpen] = useState(false);
  // <md shell (C5 #3 full): a bottom tab bar; Hand / Log / Act each open a
  // bottom sheet, 'table' = no sheet. The chat sheet stays mounted (its scroll
  // survives close-reopen); hand/act are cheap to remount.
  const [mobileTab, setMobileTab] = useState<MobileTab>('table');
  const [seenChatLen, setSeenChatLen] = useState(0);
  const [playModal, setPlayModal] = useState<{ ctx: HandCardContext; card: CardSnapshot } | null>(null);
  const [cardMenu, setCardMenu] = useState<{ ctx: TableCardContext; anchor: MenuAnchor } | null>(null);
  const [pendingTarget, setPendingTarget] = useState<PendingTarget | null>(null);
  const [pendingRescue, setPendingRescue] = useState<string | null>(null);
  // The non-scrolling column that holds #opponents + the dock — tooltip scope
  // (ui-design "Scroll contract": boardRef is #table-col, NOT the scroller).
  const boardRef = useRef<HTMLDivElement>(null);
  const { submitting, guard } = useSubmitGuard();
  // lg+ pulls Game Chat / History into a full-height right rail; md–lg stacks
  // it under the control band; <md (isMobile) opens it as a bottom sheet.
  const wideLayout = useMediaQuery('(min-width: 1024px)');
  const isMobile = useIsMobile();
  // §6c — image-free card mode. On when the player turned image tooltips off,
  // or always below md (no hover on touch). Provided to the whole board tree.
  const nav = useNav();
  const textMode = useIsMobile() || nav?.imageTooltipPreference === false;

  const { data: game, isError, refetch } = useQuery({
    queryKey: ['game', gameId],
    queryFn: () => api.get<GameSnapshot>(`/game/${gameId}/view`),
    enabled: !!gameId,
    retry: 1,
  });
  useGameSocket(gameId ?? null);
  useCardTooltips(boardRef, [game], !textMode);
  const notesIndicator = useNotesIndicator(game, notesOpen);

  // applyUpdate writes a fresh snapshot to the query cache; it also re-applies
  // any counter taps still queued by useCounterBump so an in-progress stepper
  // burst isn't visually reverted by an unrelated command's response.
  const { bump: counterBump, applyUpdate } = useCounterBump(gameId);

  // The single rejected-command surface — CommandForm's typed/quick paths and
  // the card-click act paths both feed this; TableHud renders it once. See
  // useCommandStatus for why it can't be read off `game`.
  const { status: commandStatus, captureStatus, clearStatus } = useCommandStatus();

  // Drives the Log tab's unread dot. `seenChatLen` is set (in the tab handler)
  // to the line count when the Log sheet is opened; a new turn shrinks
  // game.chat, so clamp before comparing — no effect needed.
  const chatLen = game?.chat.length ?? 0;
  const logUnread = mobileTab !== 'log' && chatLen > Math.min(seenChatLen, chatLen);

  // Shared optimistic POST wrapper (D15 silent reconcile): pre-write the cache,
  // fire the request, overwrite with server truth on return, roll back only on
  // a network failure.
  const optimisticPost = useCallback(
    (body: Record<string, unknown>, prewrite: (prev: GameSnapshot) => GameSnapshot | null) => {
      const key = ['game', gameId] as const;
      const prev = queryClient.getQueryData<GameSnapshot>(key);
      const draft = prev ? prewrite(prev) : null;
      if (draft) queryClient.setQueryData(key, draft);
      guard(async () => {
        try {
          const updated = await api.post<GameSnapshot>(`/game/${gameId}/view/submit`, body, submitHeaders());
          captureStatus(updated);
          applyUpdate(updated);
        } catch (err) {
          console.error('Failed to submit', err);
          showError('Failed to submit.');
          if (draft && prev) queryClient.setQueryData(key, prev);
        }
      });
    },
    [gameId, queryClient, guard, captureStatus, applyUpdate],
  );

  const submit = useCallback(
    (submission: Submission) => {
      optimisticPost(
        {
          phase: null,
          command: submission.command ?? null,
          chat: submission.chat ?? null,
          ping: submission.ping ?? null,
        },
        (prev) => optimisticDraft(prev, submission.command ?? undefined, viewerName),
      );
    },
    [optimisticPost, viewerName],
  );

  const sendChat = useCallback((text: string) => submit({ chat: text }), [submit]);

  const submitPhase = useCallback(
    (phase: string) => {
      optimisticPost({ phase, command: null, chat: null, ping: null }, (prev) => {
        const draft = structuredClone(prev);
        draft.phase = phase;
        return draft;
      });
    },
    [optimisticPost],
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

  const livePlayCard = playModal ? findCardByCoordinate(game, viewerName ?? '', playModal.ctx.regionType, playModal.ctx.coordinate) : null;
  // Keep the open menu's card/pool live so its counter stepper reflects each bump.
  const liveMenuCard = cardMenu ? findCardByCoordinate(game, cardMenu.ctx.controller, cardMenu.ctx.regionType, cardMenu.ctx.coordinate) : null;
  const liveMenuPool = cardMenu ? game.players.find((p) => p.name === cardMenu.ctx.controller)?.pool : undefined;

  const isMyTurn = !!viewerName && viewerName === game.currentPlayer;
  const showHand = game.player && !!viewerName;
  const canChat = game.player || game.judge;

  // NF4 (D32): the inline blood +/- stepper is a controller-only affordance
  // (plus judges, who legitimately adjust any board). On an opponent's minion
  // tile it's wrong-affordance noise — pass no bump handler there so the tile
  // shows a static count pill instead.
  const counterBumpFor = (seatName: string) =>
    game.judge || seatName === viewerName ? counterBump : undefined;

  const { me, others } = seatOrder(game.players, game.seating, viewerName);
  // Prefer the server-derived relations (ousted-skipped, reassigned on
  // withdrawal); fall back to the pure client derivation only while the D8
  // fields haven't rolled out.
  const serverRelations = !!me && (me.prey !== undefined || me.predator !== undefined);
  const oustedNames = new Set(game.players.filter((p) => p.pool < 1).map((p) => p.name));
  const relationFor = (name: string): SeatRelation => {
    if (name === viewerName) return null; // own seat — labelled separately
    const direct = serverRelations
      ? me?.prey === name
        ? 'prey'
        : me?.predator === name
          ? 'predator'
          : null
      : relationOf(name, game.seating, viewerName, (n) => oustedNames.has(n));
    // A live opponent who is neither prey nor predator still gets a quiet
    // "across the table" tag — keeps every seat's name row vertically aligned
    // and matches Main.dc.html.
    if (direct) return direct;
    return me && !oustedNames.has(name) ? 'table' : null;
  };

  const handRegion = viewerName
    ? game.players.find((p) => p.name === viewerName)?.regions.find((r) => r.type === 'HAND')
    : undefined;

  // Both panels stay mounted, one hidden — flipping to History no longer
  // unmounts the live chat (its scroll position, and any turn HistoryPanel had
  // loaded, survive the toggle). ui-design C4 #6.
  const chatPanel = (
    <>
      <div className={showHistory ? 'hidden' : 'flex flex-1 min-h-0 flex-col'}>
        <GameChatPanel game={game} gameId={gameId} viewerName={viewerName} />
      </div>
      <div className={showHistory ? 'flex flex-1 min-h-0 flex-col' : 'hidden'}>
        <HistoryPanel gameId={gameId} game={game} viewerName={viewerName} />
      </div>
      {canChat && <ChatCompose onSend={sendChat} disabled={submitting} />}
    </>
  );

  return (
    <TextModeContext.Provider value={textMode}>
    <div className="flex flex-col flex-1 min-h-0 bg-base text-ink">
      <TableHud
        game={game}
        gameId={gameId}
        viewerName={viewerName}
        onSubmitPhase={submitPhase}
        canSubmitPhase={isMyTurn && !submitting}
        commandStatus={commandStatus}
        onClearStatus={clearStatus}
        onPendingCommand={(command) => submit({ command })}
        onPendingRespond={() => {
          // <md the command input lives in the Act sheet — open it first, then
          // focus once it has mounted.
          if (isMobile) setMobileTab('act');
          setTimeout(() => {
            document.getElementById('commandForm')?.scrollIntoView({ block: 'center', behavior: 'smooth' });
            document.getElementById('command')?.focus();
          }, isMobile ? 60 : 0);
        }}
        onUpdated={applyUpdate}
        submitting={submitting}
        guard={guard}
        onOpenNotes={() => setNotesOpen(true)}
        notesIndicator={notesIndicator}
        showHistory={showHistory}
        onToggleHistory={() => setShowHistory((v) => !v)}
      />

      <div id="table-row" className="flex flex-1 min-h-0 min-w-0">
        <div id="table-col" className="flex flex-col flex-1 min-h-0 min-w-0 p-2" ref={boardRef}>
          {isMobile ? (
            <>
              <SeatPager
                seats={me ? [me, ...others] : others}
                renderSeat={(seat) => (
                  <SeatColumn
                    player={seat}
                    gameId={gameId}
                    edgeColor={game.edgeColor}
                    edgeTextColor={game.edgeTextColor}
                    isSeatedPlayer={game.player}
                    viewerName={viewerName}
                    relation={relationFor(seat.name)}
                    pingable={game.player && game.pingOptions.includes(seat.name)}
                    onTableCardClick={handleTableCardClick}
                    onQuickCommand={submit}
                    onCounterBump={counterBumpFor(seat.name)}
                    onPlayCardClick={handlePlayCardClick}
                  />
                )}
              />

              <MobileTabBar
                active={mobileTab}
                onSelect={(tab) => {
                  const next = mobileTab === tab ? 'table' : tab;
                  if (next === 'log') setSeenChatLen(chatLen);
                  setMobileTab(next);
                }}
                showHand={showHand}
                showAct={game.player || game.judge}
                handCount={handRegion?.cards.length ?? 0}
                logUnread={logUnread}
              />
            </>
          ) : (
            <>
              {/* Board state fills the top; your seat + hand + commands dock at
                  the foot of the column (Main.dc.html), so the state above no
                  longer scrolls past a fixed top control band. Opponents and the
                  dock share the column height 3:2 (was a fixed 44vh cap on the
                  dock — cramped below ~1300px, D25); each has a rem floor and
                  scrolls internally, so the command band is reachable at any
                  window height. */}
              <div id="opponents" className="game-board flex-[3_1_0%] min-h-[7rem] overflow-y-auto">
                {/* NF5 (D32): auto-fill instead of fixed column counts, so a
                    4-opponent game at ≥1280px doesn't leave a dead 5th column
                    (finding #6's own sketch). Cards size to a ~17rem min and
                    grow to fill the row. */}
                <div className="grid gap-2 items-start [grid-template-columns:repeat(auto-fill,minmax(min(17rem,100%),1fr))]">
                  {others.map((player) => (
                    <SeatColumn
                      key={player.name}
                      player={player}
                      gameId={gameId}
                      edgeColor={game.edgeColor}
                      edgeTextColor={game.edgeTextColor}
                      isSeatedPlayer={game.player}
                      viewerName={viewerName}
                      relation={relationFor(player.name)}
                      pingable={game.player && game.pingOptions.includes(player.name)}
                      onTableCardClick={handleTableCardClick}
                      onQuickCommand={submit}
                      onCounterBump={counterBumpFor(player.name)}
                      onPlayCardClick={handlePlayCardClick}
                    />
                  ))}
                </div>
              </div>

              {!wideLayout && (
                // NF1 (D32): a real flex participant, not `shrink-0 max-h-[45vh]`
                // — in the 768–1023 band that fixed block unconditionally took
                // ~45vh and starved #opponents to its rem floor. Now it shares
                // the leftover column height with #opponents 1 : 3 (the dock
                // below is content-sized — NF6), with its own 7rem floor and a
                // 35vh ceiling so it never balloons.
                <div className="mt-2 flex flex-[1_1_0%] min-h-[7rem] max-h-[35vh] flex-col overflow-hidden">
                  {chatPanel}
                </div>
              )}

              {/* NF6 (D32): the dock sizes to its content (your board + hand +
                  command band) with a 55vh cap instead of flex-grow:2 — a short
                  own-board no longer leaves ~120px dead space above the hand
                  band; the slack goes back to #opponents (flex-[3]). */}
              <div className="mt-2 flex shrink-0 min-h-[13rem] max-h-[55vh] flex-col overflow-hidden border-t-2 border-line-accent pt-2">
                {me && (
                  <YourSeatDock
                    player={me}
                    gameId={gameId}
                    edgeColor={game.edgeColor}
                    edgeTextColor={game.edgeTextColor}
                    viewerName={viewerName}
                    onTableCardClick={handleTableCardClick}
                    onQuickCommand={submit}
                    onCounterBump={counterBump}
                    onPlayCardClick={handlePlayCardClick}
                  />
                )}
                {(showHand || game.player) && (
                  <div className="mt-2 flex shrink-0 items-center gap-3 border-t border-line pt-2">
                    {showHand && (
                      <>
                        <span className="shrink-0 text-[0.65rem] font-bold uppercase tracking-wide text-ink-muted">
                          Hand {handRegion?.cards.length ?? 0}
                        </span>
                        <div className="min-w-0 flex-1">
                          <HandStrip handRegion={handRegion} show onPlayCardClick={handlePlayCardClick} />
                        </div>
                      </>
                    )}
                    <div className={showHand ? 'shrink-0' : 'min-w-0 flex-1'}>
                      <CommandForm
                        gameId={gameId}
                        game={game}
                        viewerName={viewerName}
                        onUpdated={applyUpdate}
                        captureStatus={captureStatus}
                        submitting={submitting}
                        guard={guard}
                      />
                    </div>
                  </div>
                )}
              </div>
            </>
          )}
        </div>

        {wideLayout && (
          <div id="talk-rail" className="flex w-[24rem] shrink-0 flex-col min-h-0 p-2 pl-0">
            {chatPanel}
          </div>
        )}
      </div>

      {isMobile && (
        <>
          {/* Kept mounted (open toggles visibility) so the log's scroll
              position and any loaded history turn survive a close-reopen. */}
          <BottomSheet
            open={mobileTab === 'log'}
            onClose={() => setMobileTab('table')}
            label="Table talk"
            maxHeightClass="max-h-[80vh]"
          >
            <div className="flex min-h-0 flex-1 flex-col px-2 pb-2">{chatPanel}</div>
          </BottomSheet>

          {showHand && (
            <BottomSheet
              open={mobileTab === 'hand'}
              onClose={() => setMobileTab('table')}
              label="Your hand"
              header={
                <>
                  <span className="font-semibold">Your hand</span>
                  <span className="rounded-full bg-accent px-2 py-0.5 text-xs text-surface">
                    {handRegion?.cards.length ?? 0}
                  </span>
                </>
              }
            >
              <div className="min-h-0 flex-1 overflow-y-auto px-3 pb-4">
                <HandStrip
                  handRegion={handRegion}
                  show
                  layout="list"
                  onPlayCardClick={(ctx, card) => {
                    setMobileTab('table');
                    handlePlayCardClick(ctx, card);
                  }}
                />
              </div>
            </BottomSheet>
          )}

          {(game.player || game.judge) && (
            <BottomSheet
              open={mobileTab === 'act'}
              onClose={() => setMobileTab('table')}
              label="Act"
              header={<span className="font-semibold">Act</span>}
              maxHeightClass="max-h-[60vh]"
            >
              <div className="flex flex-col gap-3 px-4 pb-6 pt-1">
                <CommandForm
                  gameId={gameId}
                  game={game}
                  viewerName={viewerName}
                  onUpdated={applyUpdate}
                  captureStatus={captureStatus}
                  submitting={submitting}
                  guard={guard}
                />
                <CallJudgeButton
                  gameId={gameId}
                  game={game}
                  onUpdated={applyUpdate}
                  submitting={submitting}
                  guard={guard}
                />
              </div>
            </BottomSheet>
          )}
        </>
      )}

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
          onCounterBump={counterBump}
          onRequestTarget={handleRequestTarget}
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
    </div>
    </TextModeContext.Provider>
  );
}
