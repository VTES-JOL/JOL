import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CardSnapshot, GameSnapshot, PlayerSnapshot } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { useGameSocket } from '../ws/useGameSocket';
import { showError } from '../stores/toast';
import { Spinner } from '../components/ui/Spinner';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { useSubmitGuard } from '../hooks/useSubmitGuard';
import { useMediaQuery, useIsMobile } from '../hooks/useMediaQuery';
import { useResizableSplit } from '../hooks/useResizableSplit';
import { useNav } from '../auth/useNav';
import { useCounterBump } from './game/useCounterBump';
import { useCommandStatus } from './game/useCommandStatus';
import { submitHeaders } from './game/submitId';
import { seatOrder, relationOf, type SeatRelation } from './game/seatOrder';
import { TableHud } from './game/TableHud';
import { SeatColumn } from './game/SeatColumn';
import { SeatGrid } from './game/SeatGrid';
import { GameLoadError } from './game/GameLoadError';
import { BoardDensityToggle } from './game/BoardDensityToggle';
import { YourSeatDock } from './game/YourSeatDock';
import { HandStrip } from './game/HandStrip';
import { HandDock } from './game/HandDock';
import { CommandForm } from './game/CommandForm';
import { DockCommandStack } from './game/DockCommandStack';
import { GameChatPanel } from './game/GameChatPanel';
import { ChatCompose } from './game/ChatCompose';
import { HistoryPanel } from './game/HistoryPanel';
import { NotesDeckDrawer } from './game/NotesDeckDrawer';
import { useNotesIndicator } from './game/useNotesIndicator';
import { PlayCardModal, type PendingTarget } from './game/PlayCardModal';
import { CardContextMenu, type MenuAnchor } from './game/CardContextMenu';
import { TextModeContext } from './game/textMode';
import { BoardDensityContext, useBoardDensityState } from './game/boardDensity';
import { SeatPager } from './game/SeatPager';
import { BottomSheet } from './game/BottomSheet';
import { MobileTabBar, type MobileTab } from './game/MobileTabBar';
import { TargetPicker } from './game/TargetPicker';
import { findCardByCoordinate, findCardByCommandCoordinate } from './game/coordinates';
import { MessageSquare } from 'lucide-react';
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
  // F2: the 768–1023 band (mid, not mobile) also moves chat out of the
  // scrolling column and into a sheet — three fighting scrollers (opponents /
  // chat / dock) all lost at this height. Same BottomSheet the mobile Log tab
  // uses, own open flag since there's no tab bar at this width.
  const [midChatOpen, setMidChatOpen] = useState(false);
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
  // Genuinely wide screens — the talk rail can afford Game Chat and History
  // side by side (~1/3 of the width); the HUD History toggle collapses it back.
  // The narrow end of the wide range — 4 opponents fold to a 2×2 grid here
  // rather than four cramped columns.
  const midWide = useMediaQuery('(min-width: 1024px) and (max-width: 1399px)');
  const isMobile = useIsMobile();
  // Draggable opponents / dock split (wide layout only), remembered per game.
  // F4: default the opponents pane taller (62%, was 50%) — at the old default
  // a 4-opponent board clipped its last minion row before the dock even
  // competed for space.
  const { topPercent, containerRef, dividerProps } = useResizableSplit(`jol-split:${gameId ?? 'none'}`, { initial: 62 });
  // §6c — image-free card mode. On when the player turned image tooltips off,
  // or always below md (no hover on touch). Provided to the whole board tree.
  const nav = useNav();
  const textMode = useIsMobile() || nav?.imageTooltipPreference === false;
  // Board density (2-up tiles vs text rows), remembered per game; forced to
  // text when textMode is on, single-column tiles on mobile widths.
  const boardDensity = useBoardDensityState(gameId, textMode, useIsMobile());

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

  // Deep-link: when a pending action names a board card, bring it into view and
  // pulse it once so "respond to the bleed on X" points at X. Unconditional
  // hook (guards on `game` inside) — it sits above GamePage's early returns.
  const pendingTargetId = game?.pendingAction?.targetCardId ?? null;
  const pendingKey = game?.pendingAction?.id ?? null;
  useEffect(() => {
    if (!pendingTargetId) return;
    const el = boardRef.current?.querySelector(
      `[data-card-instance="${pendingTargetId}"], [data-card-id="${pendingTargetId}"]`,
    );
    if (!el) return;
    el.scrollIntoView({ block: 'center', behavior: 'smooth' });
    el.classList.add('card-pulse');
    const t = setTimeout(() => el.classList.remove('card-pulse'), 1600);
    return () => clearTimeout(t);
  }, [pendingTargetId, pendingKey]);

  // Drives the Log tab/toggle's unread dot (mobile tab bar and the mid-band
  // sheet toggle both use it). `seenChatLen` is set to the line count when
  // either sheet opens; a new turn shrinks game.chat, so clamp before
  // comparing — no effect needed.
  const chatLen = game?.chat.length ?? 0;
  const logSheetOpen = mobileTab === 'log' || midChatOpen;
  const logUnread = !logSheetOpen && chatLen > Math.min(seenChatLen, chatLen);

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
    return <GameLoadError canRetry={!!gameId} onRetry={() => refetch()} onBack={() => navigate('/jol/')} />;
  }

  if (!game) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center">
        <Spinner />
      </div>
    );
  }

  const livePlayCard = playModal ? findCardByCoordinate(game, viewerName ?? '', playModal.ctx.regionType, playModal.ctx.coordinate) : null;
  // Keep the open menu's card/pool live so its counter stepper reflects each bump.
  const liveMenuCard = cardMenu ? findCardByCoordinate(game, cardMenu.ctx.controller, cardMenu.ctx.regionType, cardMenu.ctx.coordinate) : null;
  const liveMenuPool = cardMenu ? game.players.find((p) => p.name === cardMenu.ctx.controller)?.pool : undefined;

  const isMyTurn = !!viewerName && viewerName === game.currentPlayer;
  const influencePriority = isMyTurn && game.phase === 'Influence';
  // A pending response window that needs THIS viewer to act (owes a response,
  // or is the actor who can resolve). The informational face stays HUD-only.
  const pa = game.pendingAction;
  const pendingActionable =
    !!pa && !!viewerName && (pa.actor === viewerName || pa.awaiting.includes(viewerName));
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

  // One place for the per-seat prop wiring, shared by the wide grid, the
  // 768–1023 auto-fill grid and the mobile SeatPager (was duplicated 3×).
  const renderSeat = (seat: PlayerSnapshot) => (
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
  );

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

  // The wide-layout opponent grid — SeatGrid owns the real-table ordering and
  // the player-count-/width-aware column count (2×2 with prey/predator flanking
  // your dock at the narrow end of the wide range).
  const opponentSeats = (
    <SeatGrid seats={others} variant="counted" midWide={midWide} renderSeat={renderSeat} />
  );

  // Wide-layout dock: an L — your board over the command band on the left, your
  // hand a full-height column on the right (ui review: hand as a first-class
  // zone, command band directly under the board you read from).
  const dockGrid = (
    <div className="grid min-h-0 flex-1 gap-2 [grid-template-rows:minmax(0,1fr)_auto] [grid-template-columns:minmax(0,1fr)_minmax(0,1.15fr)]">
      <div className="flex min-h-0 flex-col overflow-hidden [grid-column:1] [grid-row:1]">
        <div className="flex shrink-0 items-center justify-end px-1 pb-1">
          <BoardDensityToggle
            density={boardDensity.density}
            onToggle={() => boardDensity.setDensity(boardDensity.density === 'text' ? 'tiles' : 'text')}
          />
        </div>
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
            influencePriority={influencePriority}
          />
        )}
      </div>
      <HandDock
        className="[grid-column:2] [grid-row:1/3]"
        handRegion={handRegion}
        show={showHand}
        onPlayCardClick={handlePlayCardClick}
      />
      <DockCommandStack
        variant="dock"
        className="flex flex-col gap-1.5 border-t border-line pt-1.5 [grid-column:1] [grid-row:2]"
        game={game}
        gameId={gameId}
        viewerName={viewerName}
        me={me}
        isMyTurn={isMyTurn}
        pendingActionable={pendingActionable}
        onCommand={(command) => submit({ command })}
        onUpdated={applyUpdate}
        captureStatus={captureStatus}
        submitting={submitting}
        guard={guard}
        onRespondFocus={() => document.getElementById('command')?.focus()}
      />
    </div>
  );

  // Talk rail: always the single combined panel with the HUD History toggle
  // switching Chat / History — for every persona, at every width. A prior
  // pass tried a side-by-side Chat+History split for judge/spectator (always)
  // and for a seated player past a width threshold (F11); removed by design
  // decision — a toggle reads more clearly than two half-width panels even
  // when there's room to spare.
  const railContent = chatPanel;

  // F6: a judge isn't seated (`me` is null), so gets none of the seated dock's
  // command band — despite the backend's submit already accepting a judge's
  // commands (`canJudge`). Give an unseated judge a standalone command input
  // in the rail, wide and mid bands (the mobile Act sheet already covers this
  // via DockCommandStack, which now also gates on `game.judge`).
  const judgeCommandBar = game.judge && !me && (
    <div className="mt-2 shrink-0 border-t border-line pt-2">
      {/* NF4: a bare command band under the rail read as more chat/history
          chrome — nothing marked it as a distinct control that issues a live
          command to the game. */}
      <span className="mb-1 block text-[0.65rem] font-bold uppercase tracking-wide text-ink-muted">
        Judge commands
      </span>
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
  );

  return (
    <TextModeContext.Provider value={textMode}>
    <BoardDensityContext.Provider value={boardDensity}>
    <div className="flex flex-col flex-1 min-h-0 text-ink">
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

      {/* F1: the table is not an "atmosphere" page — it's mostly bare gutter
          (dock foot, board gaps, the space between seat rows) with no plate
          over it, so RouteBackground's photo dominates instead of settling
          into the corners. Opaque ground here opts the whole play area out.
          F8: past ~1800px nothing keyed off the extra width — seats just grew
          wider and the rail stayed a fixed size, so it was pure gutter, not
          information. Cap + centre instead of pretending to scale further. */}
      {/* NF1: bg-base lives on this full-width, uncapped wrapper so it always
          covers the whole route — a prior version put both the cap and the
          background on the same element, so past 1800px the side margins
          added by centering fell back to transparent and RouteBackground's
          photo bled through again (F1, reopened). The cap/centering below is
          purely an inner layout concern now. */}
      <div id="table-row" className="flex flex-1 min-h-0 min-w-0 bg-base">
        <div className="mx-auto flex w-full max-w-[1800px] min-w-0 min-h-0 flex-1">
        <div id="table-col" className="flex flex-col flex-1 min-h-0 min-w-0 p-2" ref={boardRef}>
          {isMobile ? (
            <>
              <SeatPager seats={me ? [me, ...others] : others} renderSeat={renderSeat} />

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
          ) : wideLayout ? (
            me ? (
              <div ref={containerRef} className="flex flex-1 min-h-0 flex-col">
                <div
                  id="opponents"
                  className="game-board shrink-0 overflow-y-auto"
                  style={{ flexBasis: `${topPercent}%`, minHeight: '6rem' }}
                >
                  {opponentSeats}
                </div>
                <div
                  {...dividerProps}
                  className="group flex h-3 shrink-0 cursor-row-resize touch-none items-center justify-center gap-2 text-[0.55rem] font-semibold uppercase tracking-[0.2em] text-ink-muted/50 transition-colors hover:text-ink-muted"
                  title="Drag to resize · double-click to reset"
                >
                  <span className="h-px flex-1 bg-line" />
                  <span aria-hidden>⋯ drag ⋯</span>
                  <span className="h-px flex-1 bg-line" />
                </div>
                <div className="flex flex-1 min-h-0 flex-col overflow-hidden border-t border-line-accent pt-1.5">
                  {dockGrid}
                </div>
              </div>
            ) : (
              <div id="opponents" className="game-board flex-1 min-h-0 overflow-y-auto">
                {opponentSeats}
              </div>
            )
          ) : (
            <>
              {/* Board state fills the top; your seat + hand + commands dock at
                  the foot of the column (Main.dc.html), so the state above no
                  longer scrolls past a fixed top control band. F2: chat moved
                  out of this column into a sheet (below) — at 768–1023 it was
                  a third scroller fighting #opponents and the dock for a share
                  of a short viewport, so opponents now gets the whole column
                  above the dock instead of a 3:2 split. */}
              <div id="opponents" className="game-board flex-1 min-h-[7rem] overflow-y-auto">
                {/* NF5 (D32): auto-fill instead of fixed column counts, so a
                    4-opponent game at ≥1280px doesn't leave a dead 5th column
                    (finding #6's own sketch). Cards size to a ~17rem min and
                    grow to fill the row. */}
                <SeatGrid seats={others} variant="autofill" renderSeat={renderSeat} />
              </div>

              {!wideLayout && (
                <button
                  type="button"
                  onClick={() => {
                    setSeenChatLen(chatLen);
                    setMidChatOpen(true);
                  }}
                  className="relative mt-2 flex shrink-0 items-center gap-1.5 self-start rounded-md border border-line bg-surface/40 px-3 py-1.5 text-xs font-semibold uppercase tracking-wide text-ink-muted hover:bg-hover hover:text-ink"
                >
                  <MessageSquare size={14} />
                  Table talk
                  {logUnread && (
                    <span className="absolute -right-1 -top-1 h-2 w-2 rounded-full bg-accent" aria-hidden />
                  )}
                </button>
              )}

              {/* NF6 (D32): the dock sizes to its content (your board + hand +
                  command band) with a 55vh cap instead of flex-grow:2 — a short
                  own-board no longer leaves ~120px dead space above the hand
                  band; the slack goes back to #opponents (flex-[3]). */}
              <div className="mt-2 flex shrink-0 min-h-[13rem] max-h-[55vh] flex-col overflow-hidden border-t-2 border-line-accent pt-2">
                {judgeCommandBar}
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
          <div id="talk-rail" className="flex w-[30rem] shrink-0 flex-col min-h-0 p-2 pl-0">
            {railContent}
            {judgeCommandBar}
          </div>
        )}
        </div>
      </div>

      {!isMobile && !wideLayout && (
        <BottomSheet
          open={midChatOpen}
          onClose={() => setMidChatOpen(false)}
          label="Table talk"
          maxHeightClass="max-h-[80vh]"
        >
          <div className="flex min-h-0 flex-1 flex-col px-2 pb-2">{chatPanel}</div>
        </BottomSheet>
      )}

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
                  <span className="rounded-full bg-accent px-2 py-0.5 text-xs text-white">
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
              <DockCommandStack
                variant="sheet"
                className="flex flex-col gap-3 px-4 pb-6 pt-1"
                game={game}
                gameId={gameId}
                viewerName={viewerName}
                me={me}
                isMyTurn={isMyTurn}
                pendingActionable={false}
                onCommand={(command) => submit({ command })}
                onUpdated={applyUpdate}
                captureStatus={captureStatus}
                submitting={submitting}
                guard={guard}
              />
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
    </BoardDensityContext.Provider>
    </TextModeContext.Provider>
  );
}
