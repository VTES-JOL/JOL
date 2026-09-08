import { memo, useCallback, useEffect, useRef, useState, type MouseEvent } from 'react';
import { MinusCircle, PlusCircle } from 'lucide-react';
import type { CardSnapshot, RegionSnapshot } from '../../api/types';
import { Card, RegionLabelBadges, type QuickKind, type TableCardClick } from './Card';
import { CardSimple } from './CardSimple';
import { MinionTile } from './MinionTile';
import { PermanentChip } from './PermanentChip';
import type { MenuAnchor } from './CardContextMenu';
import { cardActions, type HandCardContext, type Submission, type TableCardContext } from './cardCommands';

// READY/TORPOR/UNCONTROLLED render as tiles (MinionTile / PermanentChip) in a
// wrapping grid; CRYPT and the expanded piles stay on <Card>. UNCONTROLLED
// uses MinionTile's compact variant (no lock, no stepper).
const TILE_REGIONS = new Set(['READY', 'TORPOR', 'UNCONTROLLED']);

// READY/TORPOR/UNCONTROLLED are live board state — prominent header, coloured
// left edge. Everything else (ash heap, RFG, library, crypt, hand, research)
// is a reference pile: quiet, smaller header, no edge.
const PRIMARY_REGIONS = new Set(['READY', 'TORPOR', 'UNCONTROLLED']);

const REGION_ACCENT: Record<string, string> = {
  READY: 'border-l-online',
  TORPOR: 'border-l-blood',
  UNCONTROLLED: 'border-l-gold',
};

// card.jsp/card-simple.jsp/card-hidden.jsp's click routing, replicated
// exactly: READY/TORPOR/UNCONTROLLED (full card.jsp) and ASH_HEAP always
// route to the on-table action modal for any seated player (no owner check
// — you bleed/block/contest OPPONENTS' cards); HAND/RESEARCH open the
// play-card modal, but only for the viewer's own region. Every other
// region/viewer combination (including any non-seated viewer — spectators,
// judges, admins) is inert, matching `showAction`'s outer
// `game.getPlayers().contains(viewer)` gate.
type ClickMode = 'action' | 'play' | null;

function clickMode(regionType: string, isOwnRegion: boolean, isSeatedPlayer: boolean): ClickMode {
  if (!isSeatedPlayer) return null;
  if (regionType === 'READY' || regionType === 'TORPOR' || regionType === 'UNCONTROLLED' || regionType === 'ASH_HEAP') return 'action';
  if ((regionType === 'HAND' || regionType === 'RESEARCH') && isOwnRegion) return 'play';
  return null;
}

// A face-down card stays playable from wherever it sits — the server enriches
// it with play modes for its controller (GameSnapshotFactory), so the
// controller's click opens the play-card modal instead of the action modal,
// overriding the region-level clickMode.
function isFaceDownPlayable(card: CardSnapshot, isOwnRegion: boolean, isSeatedPlayer: boolean): boolean {
  return isSeatedPlayer && isOwnRegion && !!card.faceDown && (card.modes?.length ?? 0) > 0;
}

// Mirrors region.jsp — collapse/expand is purely local UI state here (see
// GameSnapshotFactory's javadoc). A region that gains a card auto-expands so
// the change is visible, even if a viewer had collapsed it.
//
// React.memo: `region` keeps the same reference across an unrelated ['game',
// id] refetch (TanStack structural sharing), and every other prop is a
// primitive or a stable callback, so an opponent's action skips this whole
// card list. `onAction` is useCallback'd for the same reason — otherwise a
// fresh closure each render would defeat Card's own memo.
export const Region = memo(function Region({
  region,
  defaultCollapsed,
  controller,
  controllerPool,
  isOwnRegion,
  isSeatedPlayer,
  onTableCardClick,
  onQuickCommand,
  onCounterBump,
  onPlayCardClick,
}: {
  region: RegionSnapshot;
  defaultCollapsed: boolean;
  controller: string;
  controllerPool: number;
  isOwnRegion: boolean;
  isSeatedPlayer: boolean;
  onTableCardClick: (ctx: TableCardContext, anchor: MenuAnchor) => void;
  onQuickCommand: (submission: Submission) => void;
  // Inline blood +/- off a minion tile (coalesced by useCounterBump). Absent
  // in the archival-pile context (PilesFooter), where no minion tiles render.
  onCounterBump?: (ctx: TableCardContext, kind: 'blood', step: number) => void;
  onPlayCardClick: (ctx: HandCardContext, card: CardSnapshot) => void;
}) {
  const [collapsed, setCollapsed] = useState(defaultCollapsed);
  const prevCardCount = useRef(region.cards.length);

  useEffect(() => {
    if (region.cards.length > prevCardCount.current) {
      setCollapsed(false);
    }
    prevCardCount.current = region.cards.length;
  }, [region.cards.length]);

  const onAction = useCallback(
    ({ coordinate, card, isChild }: TableCardClick, anchor: MenuAnchor) =>
      onTableCardClick(
        {
          controller,
          controllerPool,
          regionType: region.type,
          regionCommandKey: region.commandKey,
          coordinate,
          card,
          isChild,
          controlledByViewer: isOwnRegion,
        },
        anchor,
      ),
    [onTableCardClick, controller, controllerPool, region.type, region.commandKey, isOwnRegion],
  );

  const rowCtx = useCallback(
    ({ coordinate, card, isChild }: TableCardClick): TableCardContext => ({
      controller,
      controllerPool,
      regionType: region.type,
      regionCommandKey: region.commandKey,
      coordinate,
      card,
      isChild,
      controlledByViewer: isOwnRegion,
    }),
    [controller, controllerPool, region.type, region.commandKey, isOwnRegion],
  );

  const onQuick = useCallback(
    (click: TableCardClick, kind: QuickKind) => {
      onQuickCommand((kind === 'lock' ? cardActions.lock : cardActions.unlock)(rowCtx(click)));
    },
    [onQuickCommand, rowCtx],
  );

  const onCounter = useCallback(
    (click: TableCardClick, step: number) => {
      onCounterBump?.(rowCtx(click), 'blood', step);
    },
    [onCounterBump, rowCtx],
  );

  const primary = PRIMARY_REGIONS.has(region.type);
  if (region.cards.length === 0) {
    // A non-primary empty region is dropped (it lives in PilesFooter's count
    // row). An empty primary region renders a quiet "TORPOR 0" label instead
    // of vanishing — "nobody in torpor" is information (finding #7).
    if (!primary) return null;
    return (
      <div className="mb-1 flex items-center gap-2 px-2 py-1 text-[0.7rem] font-semibold uppercase tracking-wide text-ink-muted">
        <span className="truncate">{region.label}</span>
        <span className="tabular-nums text-line-accent">0</span>
      </div>
    );
  }

  const accent = REGION_ACCENT[region.type] ?? 'border-l-line-accent';
  const mode = clickMode(region.type, isOwnRegion, isSeatedPlayer);

  // `i` is the card's index in the backend's region list — that IS its
  // coordinate (`lock … ready <i+1>`), so it must travel with the card, not
  // be re-derived from render position. The backend now stable-sorts READY
  // minions-first on every submit / load (GameData.normalizeReadyOrder, D35b),
  // so `i` and render order already agree and the numbers read contiguously;
  // the client-side partition below is kept as a belt-and-braces guard for the
  // brief window right after a rollback before the next submit re-normalises.
  const indexed = region.cards.map((card, i) => ({ card, i }));
  // NF2 (D32): an opponent's UNCONTROLLED is almost always a stack of
  // identity-less `*********` rows — inert (you can't act on a hidden card you
  // don't control) and ~100px of noise per seat. Collapse those into one dense
  // "N hidden" chip row; visible influence-in-progress and deliberately
  // face-down cards still render as tiles.
  const hiddenUncontrolled =
    region.type === 'UNCONTROLLED' ? indexed.filter((e) => !e.card.visible && !e.card.faceDown).length : 0;
  const visibleIndexed =
    region.type === 'UNCONTROLLED' ? indexed.filter((e) => e.card.visible || e.card.faceDown) : indexed;
  const orderedCards =
    region.type === 'READY'
      ? [...visibleIndexed.filter((e) => e.card.minion !== false), ...visibleIndexed.filter((e) => e.card.minion === false)]
      : visibleIndexed;

  const renderCard = (card: CardSnapshot, i: number) => {
    const coordinate = String(i + 1);
    const playClick = () =>
      onPlayCardClick({ regionType: region.type, regionCommandKey: region.commandKey, coordinate }, card);
    const faceDownPlay = isFaceDownPlayable(card, isOwnRegion, isSeatedPlayer);
    if (region.simple) {
      const actionClick = (e: MouseEvent) =>
        onAction({ coordinate, card, isChild: false }, { x: e.clientX, y: e.clientY });
      const actionContextMenu = (e: MouseEvent) => {
        e.preventDefault(); // suppress Chrome's own menu
        actionClick(e);
      };
      const onClick = faceDownPlay
        ? playClick
        : mode === 'action'
          ? actionClick
          : mode === 'play'
            ? playClick
            : undefined;
      return (
        <CardSimple
          key={card.id}
          card={card}
          region={region.type}
          coordinate={coordinate}
          onClick={onClick}
          onContextMenu={!faceDownPlay && mode === 'action' ? actionContextMenu : undefined}
        />
      );
    }
    const actionHandler = mode === 'action' ? onAction : undefined;
    const cardClick = faceDownPlay ? playClick : undefined;
    if (TILE_REGIONS.has(region.type)) {
      const uncontrolled = region.type === 'UNCONTROLLED';
      // Uncontrolled minions can't be locked and don't take blood off the tile.
      const quickHandler = mode === 'action' && !uncontrolled ? onQuick : undefined;
      // A non-minion in READY (master / location / powerbase) — compact chip, no ring.
      if (region.type === 'READY' && card.minion === false) {
        return (
          <PermanentChip
            key={card.id}
            card={card}
            region={region.type}
            coordinate={coordinate}
            onAction={actionHandler}
            onQuick={quickHandler}
            onCardClick={cardClick}
          />
        );
      }
      return (
        <MinionTile
          key={card.id}
          card={card}
          region={region.type}
          coordinate={coordinate}
          compact={uncontrolled}
          onAction={actionHandler}
          onQuick={quickHandler}
          // NF4 (D32): steppers only when the caller actually supplied a bump
          // handler (controller / judge) — otherwise the tile shows a static
          // count pill.
          onCounter={quickHandler && onCounterBump ? onCounter : undefined}
          onCardClick={cardClick}
        />
      );
    }
    return (
      <Card
        key={card.id}
        card={card}
        region={region.type}
        coordinate={coordinate}
        onAction={actionHandler}
        onQuick={mode === 'action' && (region.type === 'READY' || region.type === 'TORPOR') ? onQuick : undefined}
        onCardClick={cardClick}
      />
    );
  };

  return (
    <div className={`mb-2 ${primary ? `border-l-2 ${accent}` : ''}`}>
      <div
        className={`px-2 py-1.5 flex justify-between items-center ${
          primary ? 'bg-panel border-b border-line-accent' : 'bg-panel/40'
        }`}
      >
        <span className="flex items-center gap-1.5 min-w-0">
          <button
            type="button"
            className="text-ink-muted hover:text-ink shrink-0"
            onClick={() => setCollapsed((prev) => !prev)}
            aria-label={collapsed ? `Expand ${region.label}` : `Collapse ${region.label}`}
          >
            {collapsed ? <PlusCircle size={15} /> : <MinusCircle size={15} />}
          </button>
          <span
            className={`uppercase tracking-wide truncate ${
              primary ? 'font-bold text-xs text-ink' : 'font-semibold text-[0.7rem] text-ink-muted'
            }`}
          >
            {region.label}
          </span>
          <RegionLabelBadges region={region} />
        </span>
        <span className={`text-xs tabular-nums shrink-0 ${primary ? 'text-ink-secondary' : 'text-ink-muted'}`}>
          {region.cards.length}
        </span>
      </div>
      {!collapsed && (
        <ol
          className={
            region.type === 'UNCONTROLLED'
              ? 'region list-none grid gap-1.5 p-1.5 [grid-template-columns:repeat(auto-fill,minmax(min(9rem,100%),1fr))]'
              : TILE_REGIONS.has(region.type)
                ? 'region list-none grid gap-1.5 p-1.5 [grid-template-columns:repeat(auto-fill,minmax(min(15rem,100%),1fr))]'
                : 'region list-none divide-y divide-line/40'
          }
        >
          {orderedCards.map(({ card, i }) => renderCard(card, i))}
          {hiddenUncontrolled > 0 && (
            <li
              className="col-span-full flex list-none items-center gap-1.5 rounded border border-line-accent border-dashed bg-hover/30 px-2 py-1 text-[0.7rem] text-ink-muted"
              style={{ gridColumn: '1 / -1' }}
              title={`${hiddenUncontrolled} card${hiddenUncontrolled === 1 ? '' : 's'} being influenced — identity hidden`}
            >
              {Array.from({ length: Math.min(hiddenUncontrolled, 8) }).map((_, k) => (
                <span key={k} className="inline-block h-2.5 w-2.5 rounded-[2px] border border-ink-muted/60" aria-hidden />
              ))}
              <span className="ml-auto tabular-nums font-semibold">{hiddenUncontrolled} hidden</span>
            </li>
          )}
        </ol>
      )}
    </div>
  );
});
