import { memo, type MouseEvent } from 'react';
import { Flame, Lock, Minus, Plus } from 'lucide-react';
import type { CardSnapshot } from '../../api/types';
import type { MenuAnchor } from './CardContextMenu';
import { NestedCard, type QuickKind, type TableCardClick } from './Card';
import { CardHidden } from './CardHidden';
import { Clan } from './Clan';
import { Sect } from './Sect';
import { Path } from './Path';

// N2/#6 — a ready or torpor minion rendered as a bordered tile instead of a
// text-ladder row, so the board composes into a readable position. Two rows
// (identity + glyphs), a state-chip line, and the attached-card branch reused
// from Card.tsx verbatim.
//
// State-chip precedence (N4): contested > face-down > locked. The face-down
// dimming is applied to the NAME ONLY — never the chips or the tile border —
// so the more urgent state stays the loud one. A contested tile keeps
// full-strength chrome regardless.
//
// Lock is persistent tile chrome (#10), not a hover-only button; the counter
// stepper (#9) is inline with ≥44px tap targets below md.

const CHIP = 'inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[0.7rem] font-semibold';
// ≥44px hit area on touch (no visual bloat on pointer devices).
const TAP = 'inline-flex items-center justify-center p-2.5 md:p-1';

export const MinionTile = memo(function MinionTile({
  card,
  region,
  coordinate,
  compact = false,
  onAction,
  onQuick,
  onCounter,
  onCardClick,
}: {
  card: CardSnapshot;
  region: string;
  coordinate: string;
  // UNCONTROLLED — a denser tile: identity + capacity + glyphs only, no lock
  // chrome, no stepper, no state-chip row (Main.dc.html packs these to save
  // vertical space while keeping disciplines + capacity legible).
  compact?: boolean;
  onAction?: (click: TableCardClick, anchor: MenuAnchor) => void;
  onQuick?: (click: TableCardClick, kind: QuickKind) => void;
  // Inline blood +/- straight off the tile (coalesced by useCounterBump).
  onCounter?: (click: TableCardClick, step: number) => void;
  onCardClick?: () => void;
}) {
  if (!card.visible) {
    return <CardHidden card={card} region={region} coordinate={coordinate} />;
  }

  const openMenu = onAction
    ? (e: MouseEvent) => {
        e.stopPropagation();
        onAction({ coordinate, card, isChild: false }, { x: e.clientX, y: e.clientY });
      }
    : undefined;
  const tileClick = onCardClick
    ? (e: MouseEvent) => {
        e.stopPropagation();
        onCardClick();
      }
    : openMenu;
  const tileContextMenu =
    onCardClick || !openMenu
      ? undefined
      : (e: MouseEvent) => {
          e.preventDefault();
          openMenu(e);
        };

  const quick = onQuick
    ? (kind: QuickKind) => (e: MouseEvent) => {
        e.stopPropagation();
        onQuick({ coordinate, card, isChild: false }, kind);
      }
    : null;
  const counter = onCounter
    ? (step: number) => (e: MouseEvent) => {
        e.stopPropagation();
        onCounter({ coordinate, card, isChild: false }, step);
      }
    : null;

  // N4 border precedence — contested outranks face-down outranks locked.
  const border = card.contested
    ? 'border-gold bg-gold/10'
    : card.faceDown
      ? 'border-dashed border-ink-muted'
      : card.locked
        ? 'border-accent bg-accent/5'
        : 'border-line-accent';

  const hasCounterUi = (card.capacity ?? 0) > 0 || (card.counters ?? 0) > 0 || !!card.hasBlood;
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const hasVotes = !!card.votes && card.votes !== '0';

  if (compact) {
    return (
      <li
        className={`group list-none rounded border ${border} bg-hover/40 px-1.5 py-1`}
        onClick={tileClick}
        onContextMenu={tileContextMenu}
        style={tileClick ? { cursor: 'pointer' } : undefined}
      >
        <div className="flex items-baseline gap-1.5">
          <span className="shrink-0 select-all text-[0.7rem] tabular-nums text-ink-muted">{coordinate}</span>
          <a
            data-card-id={card.cardId}
            data-secured={card.playtest ? 'true' : undefined}
            className="card-name min-w-0 flex-1 truncate text-xs font-medium"
          >
            {card.name}
            {card.advanced && <i className="icon adv" />}
          </a>
          {(card.capacity ?? 0) > 0 && (
            <span className="shrink-0 rounded-full bg-blood px-1.5 text-[0.7rem] font-medium text-white tabular-nums">
              {counterText}
            </span>
          )}
        </div>
        {((card.disciplines?.length ?? 0) > 0 || card.clan) && (
          // flex-wrap: a wide crypt (6+ disciplines) overflowed the ~9rem
          // compact tile and spilled into the next one (F2). Clan flows inline
          // after the icons rather than being pinned right, so it wraps too.
          <div className="mt-0.5 flex flex-wrap items-center gap-x-1 gap-y-0.5">
            {(card.disciplines ?? []).map((disc) => (
              <span key={disc} className={`icon ${disc}`} />
            ))}
            <Clan value={card.clan} />
          </div>
        )}
      </li>
    );
  }

  return (
    <li
      className={`group list-none rounded-md border ${border} bg-hover/40 p-2`}
      onClick={tileClick}
      onContextMenu={tileContextMenu}
      style={tileClick ? { cursor: 'pointer' } : undefined}
    >
      {/* identity row */}
      <div className="flex items-start gap-1.5">
        <span className="shrink-0 select-all text-xs tabular-nums text-ink-muted">{coordinate}</span>
        <a
          data-card-id={card.cardId}
          data-secured={card.playtest ? 'true' : undefined}
          className={`card-name min-w-0 flex-1 text-wrap text-sm font-medium ${card.faceDown ? 'opacity-60' : ''}`}
        >
          {card.name}
          {card.advanced && <i className="icon adv" />}
        </a>
        {hasVotes && <span className={`${CHIP} bg-gold text-surface`}>{card.votes}</span>}
        {card.infernal && <Flame size={13} className="shrink-0 text-blood" />}

        {/* Locked = persistent filled chip (the state that matters). The
            "lock this" affordance only appears on hover/focus so an unlocked
            board isn't a field of grey buttons; touch reaches it via the
            card menu. */}
        {card.locked ? (
          quick ? (
            <button
              type="button"
              aria-label={`Unlock ${card.name ?? 'card'}`}
              title="Unlock"
              onClick={quick('unlock')}
              className={`${TAP} shrink-0 rounded bg-accent text-white hover:bg-accent-dim`}
            >
              <Lock size={12} strokeWidth={2.75} />
            </button>
          ) : (
            <span className={`${TAP} shrink-0 rounded bg-accent text-white`} title="Locked">
              <Lock size={12} strokeWidth={2.75} />
            </span>
          )
        ) : (
          quick && (
            <button
              type="button"
              aria-label={`Lock ${card.name ?? 'card'}`}
              title="Lock"
              onClick={quick('lock')}
              className={`${TAP} shrink-0 rounded border border-line-accent text-ink-muted opacity-0 transition-opacity hover:border-ink hover:text-ink focus-visible:opacity-100 group-hover:opacity-100`}
            >
              <Lock size={12} />
            </button>
          )
        )}
      </div>

      {/* glyph row */}
      {((card.disciplines?.length ?? 0) > 0 || card.clan || card.sect || card.path) && (
        <div className="mt-1 flex items-center gap-1">
          {(card.disciplines ?? []).map((disc) => (
            <span key={disc} className={`icon ${disc}`} />
          ))}
          <span className="ml-auto flex items-center gap-1">
            <Path value={card.path} />
            <Sect value={card.sect} />
            <Clan value={card.clan} />
          </span>
        </div>
      )}

      {/* counter stepper / state chips */}
      {(hasCounterUi || card.contested || card.faceDown || card.label) && (
        <div className="mt-1.5 flex flex-wrap items-center gap-1.5">
          {card.contested && <span className={`${CHIP} bg-gold text-surface`}>CONTESTED</span>}
          {card.faceDown && (
            <span
              className={`${CHIP} border border-dashed border-ink-muted text-ink-muted`}
              title="Only you can see this card"
            >
              FACE DOWN
            </span>
          )}
          {card.label && <span className={`${CHIP} border border-line bg-hover text-ink`}>{card.label}</span>}
          {hasCounterUi && (
            <span className="ml-auto flex items-center gap-1">
              {counter && (
                <button
                  type="button"
                  aria-label={`Remove blood from ${card.name ?? 'card'}`}
                  onClick={counter(-1)}
                  className={`${TAP} rounded border border-line-accent text-ink-muted hover:border-ink hover:text-ink`}
                >
                  <Minus size={12} />
                </button>
              )}
              <span className="min-w-[2.5ch] rounded-full bg-blood px-2 py-0.5 text-center text-xs font-medium text-white shadow-sm tabular-nums">
                {counterText}
              </span>
              {counter && (
                <button
                  type="button"
                  aria-label={`Add blood to ${card.name ?? 'card'}`}
                  onClick={counter(1)}
                  className={`${TAP} rounded border border-line-accent text-ink-muted hover:border-ink hover:text-ink`}
                >
                  <Plus size={12} />
                </button>
              )}
            </span>
          )}
        </div>
      )}

      {(card.cards?.length ?? 0) > 0 && (
        <ol className="mt-1.5 ml-1 list-none divide-y divide-line/20 border-l border-line pl-1.5">
          {card.cards!.map((nested, i) => (
            <NestedCard
              key={nested.id}
              card={nested}
              region={region}
              coordinate={`${coordinate}.${i + 1}`}
              onAction={onAction}
            />
          ))}
        </ol>
      )}
    </li>
  );
});
