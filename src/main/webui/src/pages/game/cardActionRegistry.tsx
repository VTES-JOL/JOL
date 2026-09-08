import type { ReactNode } from 'react';
import {
  ArrowLeftCircle,
  ArrowRightCircle,
  Eye,
  EyeOff,
  Flame,
  LifeBuoy,
  Lock,
  LogOut,
  Shield,
  Unlock,
} from 'lucide-react';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';

// Single source of truth for the on-table card actions — consumed by
// CardActionModal now, and by the card context menu (phase 2). Mirrors the
// legacy card-modal.jsp button list, re-grouped and made phase-aware.
//
// Gating vocabulary:
//   controllerOnly — the card sits on the viewer's board
//   ownerOnly      — the viewer is the card's start-of-game owner (only used
//                    for actions that pull a card into the owner's own private
//                    zones, or influence their own uncontrolled region)
//   phase          — only offered during this turn phase
//   childSafe      — also valid for an attached card (retainer / equipment);
//                    everything else is hidden when the target is attached

export type ActionGroup = 'state' | 'declare' | 'move' | 'remove';

export const GROUP_ORDER: ActionGroup[] = ['state', 'declare', 'move', 'remove'];
export const GROUP_LABEL: Record<ActionGroup, string> = {
  state: 'State',
  declare: 'Declare',
  move: 'Move',
  remove: 'Remove',
};

export interface ActionEnv {
  region: string; // regionCommandKey: ready | torpor | inactive | ashheap
  phase: string; // GameSnapshot.phase description: Unlock | Master | Minion | Influence | Discard
  controlledByViewer: boolean;
  isCardOwner: boolean;
  isChild: boolean;
  minion: boolean;
  locked: boolean;
  contested: boolean;
  faceDown: boolean;
}

export interface CardAction {
  id: string;
  label: ReactNode;
  title: string;
  group: ActionGroup;
  regions: string[];
  phase?: string;
  lockState?: 'locked' | 'unlocked';
  contested?: boolean;
  controllerOnly?: boolean;
  ownerOnly?: boolean;
  topLevelOnly?: boolean;
  minionOnly?: boolean;
  nonMinionOnly?: boolean;
  faceDown?: boolean;
  childSafe?: boolean;
  // Instead of running build(), the surface asks the page to start a
  // cross-card target pick (currently only Rescue → pick a torpor vampire).
  requestTarget?: boolean;
  // Higher = more likely the thing you tapped the card to do right now. Feeds
  // the mobile action sheet's "LIKELY NOW" lead list (ActSheet.dc.html); 0 /
  // unset actions only ever appear in the grouped tail.
  priority?: number;
  build: (ctx: TableCardContext) => Submission;
}

export const CARD_ACTIONS: CardAction[] = [
  // ── State ──────────────────────────────────────────────────────────────
  { id: 'lock', group: 'state', label: <><Lock size={13} /> Lock</>, title: 'Lock', regions: ['ready', 'torpor'], lockState: 'unlocked', childSafe: true, priority: 80, build: cardActions.lock },
  { id: 'unlock', group: 'state', label: <><Unlock size={13} /> Unlock</>, title: 'Unlock', regions: ['ready', 'torpor'], lockState: 'locked', childSafe: true, priority: 78, build: cardActions.unlock },
  { id: 'contest', group: 'state', label: 'Contest', title: 'Contest', regions: ['ready', 'torpor'], contested: false, build: (ctx) => cardActions.contest(ctx, false) },
  { id: 'clear-contest', group: 'state', label: 'Clear Contest', title: 'Clear Contest', regions: ['ready', 'torpor'], contested: true, build: (ctx) => cardActions.contest(ctx, true) },
  { id: 'hide', group: 'state', label: <><EyeOff size={13} /> Turn Face Down</>, title: 'Turn this card face down — only you will see it', regions: ['ready', 'torpor', 'inactive'], controllerOnly: true, faceDown: false, build: cardActions.hide },
  { id: 'reveal', group: 'state', label: <><Eye size={13} /> Reveal</>, title: 'Turn this card face up for everyone', regions: ['ready', 'torpor', 'inactive'], controllerOnly: true, faceDown: true, build: cardActions.reveal },

  // ── Declare (minion phase — otherwise use chat) ────────────────────────
  { id: 'bleed', group: 'declare', label: 'Bleed', title: 'Bleed', regions: ['ready'], phase: 'Minion', lockState: 'unlocked', topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 100, build: cardActions.bleed },
  { id: 'hunt', group: 'declare', label: 'Hunt', title: 'Hunt', regions: ['ready'], phase: 'Minion', lockState: 'unlocked', topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 70, build: cardActions.hunt },
  { id: 'go-anarch', group: 'declare', label: 'Go Anarch', title: 'Go Anarch', regions: ['ready'], phase: 'Minion', lockState: 'unlocked', topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 40, build: cardActions.goAnarch },
  { id: 'block', group: 'declare', label: <><Shield size={13} /> Block</>, title: 'Block', regions: ['ready'], phase: 'Minion', topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 95, build: (ctx) => cardActions.block(ctx.card.name ?? '') },
  { id: 'rescue', group: 'declare', label: <><LifeBuoy size={13} /> Rescue…</>, title: 'Attempt to rescue a vampire from torpor', regions: ['ready'], phase: 'Minion', topLevelOnly: true, controllerOnly: true, minionOnly: true, requestTarget: true, priority: 55, build: () => ({}) },
  { id: 'leave-torpor', group: 'declare', label: 'Leave Torpor', title: 'Leave Torpor', regions: ['torpor'], phase: 'Minion', lockState: 'unlocked', topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 65, build: cardActions.leaveTorpor },

  // ── Move ──────────────────────────────────────────────────────────────
  { id: 'influence', group: 'move', label: 'Influence out', title: 'Influence out — move to your ready region', regions: ['inactive'], phase: 'Influence', topLevelOnly: true, ownerOnly: true, minionOnly: true, priority: 90, build: cardActions.influence },
  { id: 'move-ready', group: 'move', label: 'Move to ready', title: 'Move to ready', regions: ['torpor'], topLevelOnly: true, controllerOnly: true, minionOnly: true, priority: 60, build: cardActions.moveReady },
  { id: 'torpor', group: 'move', label: 'Send to Torpor', title: 'Send to Torpor', regions: ['ready'], topLevelOnly: true, minionOnly: true, build: cardActions.torpor },
  { id: 'banish', group: 'move', label: 'Banish', title: 'Banish to the uncontrolled region', regions: ['ready'], build: cardActions.banish },
  { id: 'move-predator', group: 'move', label: <><ArrowLeftCircle size={13} /> Move to Predator</>, title: 'Move to Predator', regions: ['ready'], build: cardActions.movePredator },
  { id: 'move-prey', group: 'move', label: <><ArrowRightCircle size={13} /> Move to Prey</>, title: 'Move to Prey', regions: ['ready'], build: cardActions.movePrey },
  { id: 'move-hand', group: 'move', label: 'Move to Hand', title: 'Move to Hand', regions: ['ashheap'], ownerOnly: true, build: cardActions.moveHand },
  { id: 'move-library', group: 'move', label: 'Move to Library', title: 'Move to bottom of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, build: (ctx) => cardActions.moveLibrary(ctx, false) },
  { id: 'move-library-top', group: 'move', label: 'Move to Library (Top)', title: 'Move to top of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, build: (ctx) => cardActions.moveLibrary(ctx, true) },
  { id: 'move-uncontrolled', group: 'move', label: 'Move to Uncontrolled', title: 'Move to uncontrolled', regions: ['ashheap'], ownerOnly: true, minionOnly: true, build: cardActions.moveUncontrolled },

  // ── Remove ────────────────────────────────────────────────────────────
  { id: 'burn', group: 'remove', label: <><Flame size={13} /> Burn</>, title: 'Burn', regions: ['ready', 'torpor', 'inactive'], childSafe: true, build: cardActions.burn },
  { id: 'rfg', group: 'remove', label: <><LogOut size={13} /> Remove from Game</>, title: 'Remove from game', regions: ['ready', 'torpor', 'inactive', 'ashheap'], controllerOnly: true, build: cardActions.removeFromGame },
];

// The top few actions worth leading with for a card in this state — the mobile
// action sheet's "LIKELY NOW" section (ActSheet.dc.html). Only priority-weighted
// actions qualify; returns [] when fewer than two do (the sheet then skips the
// lead section and shows the grouped list straight away).
export function likelyNow(env: ActionEnv, limit = 4): CardAction[] {
  const ranked = CARD_ACTIONS.filter((a) => (a.priority ?? 0) > 0 && actionAvailable(a, env)).sort(
    (a, b) => (b.priority ?? 0) - (a.priority ?? 0),
  );
  return ranked.length >= 2 ? ranked.slice(0, limit) : [];
}

export function actionAvailable(a: CardAction, env: ActionEnv): boolean {
  if (!a.regions.includes(env.region)) return false;
  if (env.isChild && !a.childSafe) return false;
  if (a.phase && a.phase !== env.phase) return false;
  if (a.lockState && a.lockState !== (env.locked ? 'locked' : 'unlocked')) return false;
  if (a.contested !== undefined && a.contested !== env.contested) return false;
  if (a.controllerOnly && !env.controlledByViewer) return false;
  if (a.ownerOnly && !env.isCardOwner) return false;
  if (a.topLevelOnly && env.isChild) return false;
  if (a.minionOnly && !env.minion) return false;
  if (a.nonMinionOnly && env.minion) return false;
  if (a.faceDown !== undefined && a.faceDown !== env.faceDown) return false;
  return true;
}
