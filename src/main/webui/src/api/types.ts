// Hand-written mirrors of net.deckserver.rest.bean.* — kept intentionally minimal,
// only the fields the React pages actually read. Update alongside the Java bean
// when its shape changes.

export interface NavBean {
  player: string | null;
  stamp: string;
  chats: boolean;
  notificationsEnabled: boolean;
  hasSubscriptions: boolean;
  country: string | null;
  buttons: string[];
  gameButtons: Record<string, string>;
  // Count of outstanding judge requests — badge on the Judges nav item. 0 for non-judges.
  pendingJudgeRequests: number;
  // false ⇒ the game screen renders cards text-only (no image tooltips / modal art).
  // Also forced on below the md breakpoint, where there is no hover. See pages/game/textMode.
  imageTooltipPreference: boolean;
  // Server-persisted UI theme (authoritative). Mirrors the Theme union in src/theme.ts.
  theme: string;
}

export interface ChatEntry {
  timestamp: string;
  player: string;
  // Plain text with server-substituted tokens ([card:id:name], [disc:code],
  // [d], [style:text]); rendered by <MessageContent>. Not HTML.
  message: string;
}

export interface UserSummary {
  name: string;
  lastOnline: string;
  roles: string[];
  country: string | null;
}

export type GameStatus = 'Active' | 'Inviting';
export type PlayerRelationship = 'OWNER' | 'REGISTERED' | 'INVITED' | 'OPEN' | null;

export interface PlayerStatus {
  playerName: string;
  /** Stable, URL-safe id for `playerName` — path token for that player's deck endpoints. Null if unresolved. */
  playerId: string | null;
  pinged: boolean;
  current: boolean;
  /** Live in-memory game state (active games only) — for the home Games List standing display. */
  pool: number;
  vp: number;
  ousted: boolean;
}

// net.deckserver.rest.bean.RegistrationStatus.
export interface RegistrationStatus {
  player: string;
  /** Stable, URL-safe id for `player` — path token for that player's deck endpoints. Null if unresolved. */
  playerId: string | null;
  gameName: string;
  registered: boolean;
  deckName: string | null;
  deckSummary: string | null;
  valid: boolean;
}

export interface GameStatusBean {
  name: string;
  gameId: string;
  gameStatus: GameStatus;
  format: string;
  owner: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  players: Record<string, PlayerStatus>;
  registrations: RegistrationStatus[];
  activePlayer: string | null;
  predator: string | null;
  prey: string | null;
  turn: string | null;
  /** Round number (integer before the dot in `turn`); 0 when not an active game. */
  round: number;
  /** Player holding the edge, or null. */
  edge: string | null;
  /** Player names in seating order; `players` is unordered. Empty for an inviting game. */
  seating: string[];
  // Last-activity time, not creation — GameCleanUp closes a stale public lobby
  // game 5 days after this. The "closes in N days" label is derived from it.
  updated: string | null;
  playerRelationship: PlayerRelationship;
}

// GET /jol/api/main/games — net.deckserver.rest.bean.GamesSummaryBean.
export interface GamesSummary {
  games: GameStatusBean[];
  tournament: GameStatusBean[];
  ousted: GameStatusBean[];
}

// GET /jol/api/main/notes — MainResource.NotesResponse.
export interface NotesResponse {
  notes: string;
}

// GET/PUT /jol/api/profile (+ /preferences, /edge-color) — ProfileResource, net.deckserver.rest.bean.ProfileBean.
export interface Profile {
  email: string | null;
  discordID: string | null;
  veknID: string | null;
  country: string | null;
  edgeColor: string | null;
  imageTooltipPreference: boolean;
  notificationsEnabled: boolean;
  hasSubscriptions: boolean;
}

// GET /jol/api/profile/countries — ProfileResource.CountryOption.
export interface CountryOption {
  code: string;
  name: string;
}

// GET /jol/api/subscription — NotificationResource.listSubscriptions.
export interface SubscriptionStatus {
  endpoints: string[];
}

// POST /jol/api/subscription/test — NotificationResource.sendTest.
export interface TestSendResult {
  sent: number;
  failed: number;
}

// GET /jol/api/admin-page/site-notes — net.deckserver.rest.AdminPageResource.SiteNotesResponse.
export interface SiteNotes {
  notes: string;
}

export interface UserRole {
  name: string;
  roles: string[];
  lastOnline: string;
}

export interface IdleGame {
  gameName: string;
  gameId: string;
  gameTimestamp: string;
  idlePlayers: Record<string, string>; // player -> last-access timestamp
}

// GET /jol/api/admin-page/games/{gameId}/state — net.deckserver.rest.bean.AdminGameStateBean.
export interface AdminGameState {
  gameId: string;
  gameName: string;
  format: string;
  turn: string;
  round: number;
  activePlayer: string | null;
  edge: string | null;
  gameTimestamp: string;
  players: AdminGamePlayerState[];
}

// net.deckserver.rest.bean.AdminGameStateBean.PlayerState — seating order.
export interface AdminGamePlayerState {
  name: string;
  seat: number;
  pool: number;
  vp: number;
  ousted: boolean;
  /** This player's last-access timestamp in this game — a replace-decision signal. */
  lastAccess: string;
}

// GET /jol/api/admin-page/games/{gameId}/rollback-preview?turn= — net.deckserver.rest.bean.RollbackPreviewBean.
export interface RollbackPreview {
  fromTurn: string;
  toTurn: string;
  turnsDiscarded: number;
  snapshotAvailable: boolean;
  activePlayerBefore: string | null;
  activePlayerAfter: string | null;
  players: RollbackPlayerDiff[];
}

// net.deckserver.rest.bean.RollbackPreviewBean.PlayerDiff.
export interface RollbackPlayerDiff {
  name: string;
  poolBefore: number;
  poolAfter: number;
  vpBefore: number;
  vpAfter: number;
  oustedBefore: boolean;
  oustedAfter: boolean;
}

// GET /jol/api/tournament/admin-list — net.deckserver.storage.json.system.TournamentMetadata.
export interface TournamentMetadata {
  id: string;
  name: string;
  deckFormat: string;
  registrationEndTime: string;
  startTime: string;
  endTime: string;
  rules: string[];
  conditions: string;
  specialRules: string[];
  registered: boolean;
  decksChosen: boolean;
  playerCount: number;
  numberOfRounds: number;
  numberOfTables: number;
  finalsSeeding: string[];
  roundsConfig: boolean;
  status: 'EDIT' | 'STARTING' | 'ACTIVE' | 'CLOSED';
}

// GET /jol/api/tournament/{name}/details — net.deckserver.storage.json.system.TournamentDetails.
export interface TournamentDetails {
  name: string;
  regStart: string;
  regEnd: string;
  playStart: string;
  playEnd: string;
  numRounds: number;
  reqId: string;
  tourFormat: string;
  gameFormat: string;
  rules: string[];
  specRulesCon: string;
  specRules: string[];
  status: string;
}

// net.deckserver.storage.json.system.TournamentRegistration.
export interface TournamentRegistration {
  player: string;
  vekn: string | null;
  deck: string | null;
}

// net.deckserver.storage.json.system.TournamentPlayer.
export interface TournamentPlayer {
  name: string;
  vp: number;
  gw: boolean;
}

// TournamentResource.PlayerRoundSummary.
export interface PlayerRoundSummary {
  name: string;
  vp: number;
  gw: boolean;
  pool: number;
}

// TournamentResource.PlayerStanding.
export interface PlayerStanding {
  player: string;
  vekn: string;
  gw: number;
  vp: number;
  rank: number;
}

// net.deckserver.storage.json.game.CardSimple (only the fields the finals seeding view reads).
export interface CardSimple {
  id: string;
  name: string;
}

// GET /jol/api/tournament/list — net.deckserver.rest.TournamentResource.TournamentListResponse.
export interface TournamentList {
  tournaments: TournamentMetadata[];
  finalsInvites: TournamentMetadata[];
}

// GET /jol/api/tournament/registered — net.deckserver.rest.TournamentResource.TournamentRegisteredResponse.
export interface TournamentRegistered {
  veknLinked: boolean;
  registeredGames: TournamentInviteStatus[];
}

// net.deckserver.storage.json.system.TournamentInviteStatus.
export interface TournamentInviteStatus {
  name: string;
  deck: Deck | null;
  /** Per-card display detail for `deck`, keyed by card id — feeds the shared deck view's icons. */
  details: Record<string, CardDetail>;
  format: string;
}

// net.deckserver.rest.bean.DeckInfoBean.
export interface DeckInfoBean {
  name: string;
  deckId: string;
  deckFormat: string;
  gameFormats: string[];
  comments: string;
}

// net.deckserver.storage.json.deck.DeckValidity — one game format's validation
// outcome. `format` is the format label ("Standard" | "Duel" | "V5");
// `computedAt` is an ISO-8601 instant.
export interface DeckValidity {
  format: string;
  valid: boolean;
  errors: string[];
  computedAt: string;
}

// net.deckserver.rest.bean.ImportPreviewBean — POST /jol/api/cards/preview.
export interface ImportPreview {
  format: 'krcg' | 'jol';
  deckName: string | null;
  deckDescription: string | null;
  resolved: Array<{ count: number; card: CardDetail }>;
  errors: Array<{ line: string; reason: string }>;
}

// net.deckserver.rest.bean.CardDetailBean — GET /jol/api/cards/{autocomplete,details}.
// One shape for autocomplete suggestions, deck-entry enrichment and icons.
// Crypt: types = ["Vampire"] | ["Imbued"], group = "1"–"7" | "ANY".
// Library: types = the card's type list, group = null.
export interface CardDetail {
  id: string;
  name: string;
  crypt: boolean;
  types: string[];
  group: string | null;
  banned: boolean;
  advanced: boolean;
  sets: string[];
  clan: string | null;
  path: string | null;
  capacity: number | null;
  disciplines: string[];
  andDisciplines: string[];
  orDisciplines: string[];
  requirementClans: string[];
  requirementPath: string | null;
  poolCost: number | null;
  bloodCost: number | null;
}

// net.deckserver.rest.bean.CardBean — GET /jol/api/cards/{id} and
// GET /jol/api/cards?ids=a,b,c. Full projection of Card/CryptCard/LibraryCard;
// long-cache immutable. `crypt` is the discriminator: crypt-only fields are
// null on a library card and vice versa, EXCEPT the library list fields
// (requirementClans / andDisciplines / orDisciplines) which are `null` on a
// crypt card but `[]` (never null) on a library card.
export interface CardBean {
  id: string;
  name: string;
  displayName: string; // crypt: name + " (G# ADV)" qualifier
  crypt: boolean;
  aka: string[]; // never null
  sets: string[]; // never null
  cardText: string; // plain text, \n line breaks
  artist: string;
  banned: boolean;
  playtest: boolean;
  unique: boolean;
  typeLine: string; // raw "/"-joined, e.g. "Action/Combat" | "Vampire"
  types: string[]; // split list

  // crypt-only — null on a library card
  clan: string | null;
  sect: string | null;
  path: string | null;
  group: string | null; // "1".."7" | "ANY"
  advanced: boolean | null;
  infernal: boolean | null;
  capacity: number | null;
  disciplines: string[] | null; // case-encoded (UPPER=superior, lower=inferior)
  title: string | null;
  votes: string | null; // "1".."4" | "P" | ""

  // library-only — null on a crypt card; list fields come back [] on a library card
  flavorText: string | null;
  requirementClans: string[] | null;
  requirementPath: string | null;
  andDisciplines: string[] | null;
  orDisciplines: string[] | null;
  poolCost: number | null; // -1 = variable (X)
  bloodCost: number | null; // -1 = X
  convictionCost: number | null; // -1 = X
  burnOption: boolean | null;
  preamble: string | null; // leading restriction line(s)
  doNotReplace: boolean | null;
}

// Body of every mapped error response (all 4xx + mapped 5xx) —
// net.deckserver.rest.ApiExceptionMappers.ApiError. `code` is a stable token
// (bad_request / unauthorized / forbidden / not_found / method_not_allowed /
// conflict / unsupported_media_type / unprocessable / server_error / error);
// prefer it over matching `message`. Unmapped 500s have no guaranteed body.
export interface ApiErrorBody {
  code: string;
  message: string;
}

// net.deckserver.storage.json.deck.{Deck,Crypt,Library,LibraryCard,CardCount}.
export interface Deck {
  id: string;
  name: string;
  crypt: { count: number; cards: CardCount[] };
  library: { count: number; cards: LibraryCard[] };
  comments: string;
  player: string;
  author: string;
}

export interface CardCount {
  id: number;
  name: string;
  count: number;
  comments: string;
}

export interface LibraryCard {
  type: string;
  count: number;
  cards: CardCount[];
}

// net.deckserver.rest.bean.EnrichedDeck — the shared read-only deck-view shape.
// Returned by the game / lobby / tournament deck endpoints and mirrored inside
// DeckPageBean, so one <DeckView> renders every deck preview with icons.
export interface EnrichedDeck {
  deck: Deck;
  /** card id (as a string) -> display detail. Covers every distinct card in `deck`. */
  details: Record<string, CardDetail>;
}

// net.deckserver.rest.bean.GameSummaryBean — one active game on Watch → Active
// Games. Everything below `timestamp` is live in-memory game state, fresh on
// each poll.
export interface GameSummary {
  gameName: string;
  gameId: string;
  /** Turn label, e.g. "Ludwig 5.2". */
  turn: string;
  /** Round number (integer before the dot), for "furthest along" sorting. */
  round: number;
  timestamp: string;
  format: string;
  activePlayer: string | null;
  edge: string | null;
  players: ActiveGamePlayer[];
}

// net.deckserver.rest.bean.GameSummaryBean.PlayerSummary — seating order.
export interface ActiveGamePlayer {
  name: string;
  pool: number;
  vp: number;
  ousted: boolean;
}

// net.deckserver.storage.json.system.GameHistory.
export interface GameHistory {
  name: string;
  started: string;
  ended: string;
  results: PlayerResult[];
}

// net.deckserver.storage.json.system.PlayerResult.
export interface PlayerResult {
  playerName: string;
  deckName: string;
  victoryPoints: number;
  gameWin: boolean;
}

// StatisticsResource.StatsRequest.
export interface StatsRequest {
  threshold: number;
  fromDate: string;
  toDate: string;
  isTourney: boolean;
}

// StatisticsResource.StatsDto — response of /stats/players, /stats/decks, /stats/nations.
export interface StatsDto {
  allGames: string;
  gwCount: string;
  vpCount: string;
  winRate: string;
  avgVp: string;
  highestVp: string;
  uniqueOpponents: string;
  mostPlayedOpponent: string;
  winStreak: string;
}

// StatisticsResource.OpponentStats — response of /stats/performance/{player}/players.
export interface OpponentStats {
  opponent: string;
  games: number;
  wins: number;
  winRate: string;
  winOpponent: number;
  winRateOpponent: string;
  winOther: number;
  losses: number;
}

// StatisticsResource.DeckMatchup — response of /stats/performance/{player}/decks.
export interface DeckMatchup {
  deckName: string;
  gameNames: string;
  opponentDeckName: string;
  games: number;
  totalWins: number;
  totalVP: string;
  averageVP: string;
  opponentTotalVP: string;
  opponentAverageVP: string;
  vpDifference: string;
}

// StatisticsResource.GameDuration — response of /stats/games.
export interface GameDuration {
  gameName: string;
  players: string;
  duration: string;
  hasGw: boolean;
  vps: number;
}

// StatisticsResource.JolStats — response of /stats/jol (keyed by YearMonth string, e.g. "2026-08").
export interface JolStats {
  gamesStartedPerMonth: number;
  gamesEndedPerMonth: number;
  winsPerMonth: number;
  winRate: string;
  vpPerMonth: number;
  avgVp: string;
  avgDuration: string;
  bestPlayer: string;
  bestDeck: string;
  bestNation: string;
}

// net.deckserver.storage.json.deck.DeckStats.
export interface DeckStats {
  cryptSize: number;
  librarySize: number;
  groups: string[];
  bannedCards: boolean;
  summary: string;
}

// net.deckserver.storage.json.deck.ExtendedDeck.
export interface ExtendedDeck {
  deck: Deck;
  stats: DeckStats;
  errors: string[];
}

// GET /jol/api/decks/{deckId} and POST /jol/api/decks/player/{load,…} —
// net.deckserver.rest.bean.DeckPageBean.
export interface DeckPageBean {
  selectedDeck: ExtendedDeck | null;
  contents: string | null;
  tags: string[];
  /** The selected deck's stable id, or null when nothing is loaded. */
  deckId: string | null;
  /** Per-format validation outcome, keyed by format name ("STANDARD" | "DUEL" | "V5"). */
  formatValidity: Record<string, DeckValidity>;
  /**
   * Per-card display detail for `selectedDeck`, keyed by card id (as a string) —
   * lets the editor paint icons on first render instead of firing `/cards/details`.
   * Empty when no deck is loaded.
   */
  details: Record<string, CardDetail>;
}

// net.deckserver.rest.bean.CardSnapshot — recursive. Each node is gated
// independently by CardVisibility (a face-down card may sit under a visible
// parent and vice versa). When `visible` is false every field below
// `faceDown` is absent. `faceDown` is present on both forms: on the visible
// card it's the controller's own view (render it distinctly); on the withheld
// placeholder it means "render a card back", not the hidden-hand asterisks.
export interface CardSnapshot {
  id: string;
  visible: boolean;
  counters: number;
  faceDown?: boolean;
  cardId?: string;
  name?: string;
  advanced?: boolean;
  disciplines?: string[];
  capacity?: number;
  votes?: string | null;
  contested?: boolean;
  locked?: boolean;
  infernal?: boolean;
  playtest?: boolean;
  clan?: string;
  sect?: string;
  path?: string;
  label?: string;
  /** Start-of-game owner — distinct from the board the card currently sits on. */
  owner?: string;
  minion?: boolean;
  typeClass?: string;
  clanClasses?: string[];
  hasBlood?: boolean;
  hasLife?: boolean;
  cards?: CardSnapshot[];
  // Play-card-modal fields — present only for cards in the viewer's own HAND /
  // RESEARCH region (GameSnapshotFactory scopes the enrichment), absent
  // everywhere else.
  modes?: CardMode[];
  multiMode?: boolean;
  doNotReplace?: boolean;
  preamble?: string;
  cost?: string;
}

// net.deckserver.rest.bean.RegionSnapshot.
export interface RegionSnapshot {
  type: string; // RegionType name, e.g. "READY"
  commandKey: string; // short key used in commands, e.g. "ready"/"inactive"/"ashheap"/"rfg"
  label: string;
  simple: boolean;
  openHand: boolean;
  hiddenHand: boolean;
  cards: CardSnapshot[];
}

// net.deckserver.rest.bean.PlayerSnapshot.
export interface PlayerSnapshot {
  name: string;
  pool: number;
  victoryPoints: number;
  active: boolean;
  edge: boolean;
  pinged: boolean;
  regions: RegionSnapshot[];
  // Egocentric-seating support (backend Cycle 3, D8). Server-derived: the
  // live seat before / after this one, ousted seats skipped, recomputed on
  // withdrawal / oust. Absent on older responses — the client falls back to
  // seatOrder.relationOf() when so.
  predator?: string | null;
  prey?: string | null;
  // ISO timestamp of this seat's last board action — drives the HUD "waiting
  // Xd Yh" chip. Absent on older responses (HUD omits the chip).
  lastActionAt?: string | null;
  // How this seat left the game, from a persisted per-exit record (backend C5).
  // exitVpRecipient is the player credited the VP: the predator-at-oust for an
  // OUST, the seat itself for a WITHDRAW. Absent on games predating the record
  // (and on timeout exits) — the ousted strip then just reads "— out".
  exitKind?: 'OUST' | 'WITHDRAW' | null;
  exitVpRecipient?: string | null;
}

// GET /jol/api/game/{id}/history?turn=X — net.deckserver.storage.json.game.ChatData.
export interface ChatData {
  timestamp: string;
  // Full-precision ISO insert time; `timestamp` is only minute-granularity.
  // Used to interleave failed command attempts at their true position.
  postedAt?: string;
  // Plain text with server-substituted tokens ([card:id:name], [disc:code],
  // [d], [style:text]); rendered by <MessageContent>. Not HTML.
  message: string;
  source: string;
  // Coarse line category, derived server-side (leak-free — no card id): 'talk'
  // (a player/judge spoke to the table), 'move' (a command's log line), 'phase'
  // (a "Start of X phase." marker), 'system' (turn / oust / timeout / contest…).
  // The chat log's All / Talk toggle keeps 'talk' + 'system'.
  kind?: 'talk' | 'move' | 'phase' | 'system';
  // Structured `verb arg…` form of the action. Judge-only — it carries the
  // real card id even for a face-down play/move, so the server strips it for
  // seated players / spectators. The client does not render it.
  command?: string;
  // Raw command a player submitted, plus who submitted it. Server only sends
  // these to a judge watching a game they are not seated in; stripped otherwise.
  invocation?: string;
  invocationBy?: string;
  // Monotonic id shared by every line one command submission produced; distinct
  // for the next submission even when `invocation` is identical. Used to show the
  // "» command" header once per submission. Judge-only (stripped otherwise).
  invocationSeq?: number;
}

// GET /jol/api/game/{id}/command-errors?turn=X — judge-only. A command a player
// mistyped: it produced no chat, but a judge investigating a misplay can see the
// attempt. net.deckserver.storage.json.game.CommandErrorData.
export interface CommandError {
  timestamp: string;
  // Full-precision ISO attempt time, paralleling ChatData.postedAt.
  occurredAt?: string;
  player: string;
  command: string;
  error?: string;
}

// GET /jol/api/game/{id}/view (+ POST view/submit, POST view/end-turn) —
// net.deckserver.rest.bean.GameSnapshot.
export interface GameSnapshot {
  id: string;
  name: string;
  players: PlayerSnapshot[];
  /** Player names in seating order; drives the per-actor accent colour in the chat log. */
  seating: string[];
  currentPlayer: string;
  edgePlayer: string;
  turn: string;
  turnLabel: string;
  phase: string;
  phases: string[];
  turns: string[];
  pingOptions: string[];
  player: boolean;
  admin: boolean;
  judge: boolean;
  globalNotes: string | null;
  privateNotes: string | null;
  edgeColor: string;
  edgeTextColor: 'white' | 'black';
  status: string | null;
  // Monotonic game-state version (backend Cycle 3, D8 — from GameStateEntity
  // @Version). The WS game-update frame carries the same value; the client
  // skips a WS-triggered refetch when the frame's stamp <= the cached
  // snapshot's. Older backends sent a timestamp string here — read it through
  // Number() and only trust a finite value (a NaN never wins the <= compare,
  // so a mismatched build just always refetches, which is safe).
  stamp: number;
  // Present only on POST view/submit and view/end-turn responses (never on
  // GET /view): true when the engine rejected the command and nothing changed.
  // `status` still carries the human message. Absent ⇒ treat as not rejected.
  rejected?: boolean;
  // The single OPEN "call a judge" request for this game, or null. Viewer-aware:
  // rawDetails and the can* flags depend on who is asking. net.deckserver.rest.bean.JudgeRequestBean.
  judgeRequest: JudgeRequestSnapshot | null;
  // The one open action / response window (rules R1 / D9), or null. Not viewer-
  // scoped — the client decides per-viewer whether to show Respond/Pass, Resolve,
  // or an informational line. net.deckserver.rest.bean.PendingActionBean.
  pendingAction?: PendingAction | null;
  // Current-turn game chat, carried inline so the log updates in the same round
  // trip as the board (same data as GET history?turn=<turnLabel>). Judge-only
  // fields are stripped for seated players / spectators.
  chat: ChatData[];
  // Failed command attempts for the current turn — non-empty only for a judge
  // watching a game they are not seated in. Mirrors GET command-errors.
  commandErrors: CommandError[];
}

// net.deckserver.rest.bean.PendingActionBean — the open response window.
export type PendingActionType =
  | 'BLEED'
  | 'HUNT'
  | 'RUSH'
  | 'POLITICAL'
  | 'RESCUE'
  | 'DIABLERISE'
  | 'LEAVE_TORPOR'
  | 'GO_ANARCH'
  | 'ACTION_CARD'
  | 'OTHER';

export interface PendingAction {
  id: string;
  actor: string;
  actingCardId?: string | null;
  type: PendingActionType;
  label: string; // short human label ("bleed", "hunt", …)
  targetPlayer?: string | null;
  targetCardId?: string | null;
  amount: number; // 0 when unspecified
  note?: string | null;
  declaredAt: string;
  awaiting: string[]; // seats that still owe a response
  passed: string[]; // seats that explicitly passed this window
}

export type JudgeRequestCategory = 'INCORRECT_PLAY' | 'CARD_RULING' | 'OTHER';

export interface JudgeRequestSnapshot {
  id: number;
  requester: string;
  category: JudgeRequestCategory;
  createdAt: string;
  updatedAt: string;
  // Parsed token form ([card:id:name] …) — render with <MessageContent>.
  details: string;
  // Verbatim text the requester typed; only present for the requester (edit pre-fill).
  rawDetails: string | null;
  status: 'OPEN';
  canEdit: boolean;
  canRetract: boolean;
  canResolve: boolean;
}

// GET /jol/api/judge/requests — net.deckserver.rest.bean.JudgeQueueBean.
export interface JudgeQueue {
  open: JudgeQueueEntry[];
  history: JudgeQueueEntry[];
}

export interface JudgeQueueEntry {
  id: number;
  gameId: string | null;
  gameName: string;
  tournamentName: string | null;
  tournament: boolean;
  requester: string;
  category: JudgeRequestCategory;
  status: 'OPEN' | 'RETRACTED' | 'RESOLVED';
  createdAt: string;
  // Parsed token form — render with <MessageContent>.
  details: string;
  canRule: boolean;
  resolvedBy: string | null;
  resolvedAt: string | null;
  resolution: string | null;
}

// net.deckserver.rest.bean.PlayModeBean — one play option for a hand/research
// card, carried on CardSnapshot.modes. `target` gates the client-side
// target-picker flow (needsTargetPicker in cardCommands.ts).
export type CardModeTarget = 'READY_REGION' | 'SELF' | 'SOMETHING' | 'REMOVE_FROM_GAME' | 'INACTIVE_REGION' | 'MINION_YOU_CONTROL';

export interface CardMode {
  disciplines: string[] | null;
  text: string;
  target: CardModeTarget | null;
}

// ── Metrics (MetricsResource — /metrics/*, public) ──────────────────────────
// Mirrors of net.deckserver.rest.bean.Metric* records.

export type MetricGrain = 'hour' | 'day' | 'month' | 'year';

// MetricBucket — one time bucket. `bucket` is a local-time ISO string (no
// offset) at the interval start, in the `tz` the query ran with.
export interface MetricBucket {
  bucket: string;
  submits: number;
  commands: number;
  chats: number;
  activePlayers: number;
  activeGames: number;
}

// MetricSeries — response element of /metrics/by-player and /metrics/by-game.
// `key` is the player or game name; series come most-active first.
export interface MetricSeries {
  key: string;
  submits: number;
  buckets: MetricBucket[];
}

// HeatmapCell — response element of /metrics/heatmap. dayOfWeek is ISO
// (1 = Monday … 7 = Sunday); empty cells are omitted.
export interface HeatmapCell {
  dayOfWeek: number;
  hourOfDay: number;
  submits: number;
}

// MetricTotals — response of /metrics/totals. first/lastEvent are ISO offset
// (UTC) strings, or null when the window is empty.
export interface MetricTotals {
  submits: number;
  commands: number;
  chats: number;
  activePlayers: number;
  activeGames: number;
  activeDays: number;
  firstEvent: string | null;
  lastEvent: string | null;
}
