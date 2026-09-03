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
  stamp: string;
  // The single OPEN "call a judge" request for this game, or null. Viewer-aware:
  // rawDetails and the can* flags depend on who is asking. net.deckserver.rest.bean.JudgeRequestBean.
  judgeRequest: JudgeRequestSnapshot | null;
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
