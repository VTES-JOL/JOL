// Hand-written mirrors of net.deckserver.dwr.bean.* — kept intentionally minimal,
// only the fields the React pages actually read. Update alongside the Java bean
// when its shape changes.

export interface NavBean {
  player: string | null;
  target: string;
  stamp: string;
  chats: boolean;
  game: string | null;
  notificationsEnabled: boolean;
  hasSubscriptions: boolean;
  country: string | null;
  buttons: string[];
  gameButtons: Record<string, string>;
}

export interface ChatEntry {
  timestamp: string;
  player: string;
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
  pinged: boolean;
  current: boolean;
}

export interface GameStatusBean {
  name: string;
  gameId: string;
  gameStatus: GameStatus;
  format: string;
  owner: string;
  visibility: 'PUBLIC' | 'PRIVATE';
  players: Record<string, PlayerStatus>;
  activePlayer: string | null;
  predator: string | null;
  prey: string | null;
  turn: string | null;
  created: string | null;
  playerRelationship: PlayerRelationship;
}

// GET /jol/api/main/games — net.deckserver.dwr.bean.GamesSummaryBean.
export interface GamesSummary {
  games: GameStatusBean[];
  tournament: GameStatusBean[];
  ousted: GameStatusBean[];
}

// GET /jol/api/main/notes — MainResource.NotesResponse.
export interface NotesResponse {
  notes: string;
}

// GET/PUT /jol/api/profile (+ /preferences, /edge-color) — ProfileResource, net.deckserver.dwr.bean.ProfileBean.
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

// GET /jol/api/admin-page — net.deckserver.dwr.bean.AdminPageBean. `players`
// (dead idle-players.jsp, never included by admin/layout.jsp) is omitted —
// not ported, see AdminPage.tsx.
export interface AdminPage {
  userRoles: UserRole[];
  substitutes: string[];
  games: Record<string, string>; // gameId -> gameName
  idleGames: IdleGame[];
  siteNotes: string;
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

// GET /jol/api/tournament/player-list — net.deckserver.dwr.bean.TournamentBean.
export interface TournamentBean {
  veknLinked: boolean;
  tournaments: TournamentMetadata[];
  registeredGames: TournamentInviteStatus[];
  finalsInvites: TournamentMetadata[];
  decks: DeckInfoBean[];
}

// net.deckserver.storage.json.system.TournamentInviteStatus.
export interface TournamentInviteStatus {
  name: string;
  deck: Deck | null;
  format: string;
}

// net.deckserver.dwr.bean.DeckInfoBean.
export interface DeckInfoBean {
  name: string;
  deckFormat: string;
  gameFormats: string[];
  comments: string;
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

// GET /jol/api/watch — net.deckserver.dwr.bean.AllGamesBean.
export interface AllGames {
  games: GameSummary[];
  history: GameHistory[];
}

// net.deckserver.dwr.bean.GameSummaryBean.
export interface GameSummary {
  gameName: string;
  gameId: string;
  turn: string;
  timestamp: string;
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
  treshold: number;
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
