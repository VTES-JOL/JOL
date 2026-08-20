import {useState} from 'react';
import {Link} from 'react-router-dom';
import type {GamesSummary, GameStatusBean} from '../../api/types';
import {Card, CardHeader, CardTitle} from '../../components/Card';
import {useApiPoll} from './useApiPoll';
import {useAuth} from '../../nav/useAuth';
import {pathForGame} from '../../routes';

type TabId = 'myGames' | 'tournamentGames' | 'oustedGames';

const TABS: { id: TabId; label: string; field: keyof GamesSummary; showSeatRow: boolean }[] = [
    {id: 'myGames', label: 'Active', field: 'games', showSeatRow: true},
    {id: 'tournamentGames', label: 'Tournament', field: 'tournament', showSeatRow: true},
    {id: 'oustedGames', label: 'Ousted', field: 'ousted', showSeatRow: false},
];

function SeatRow({game}: { game: GameStatusBean }) {
    const pred = game.predator ? game.players[game.predator] : undefined;
    const active = game.activePlayer ? game.players[game.activePlayer] : undefined;
    const prey = game.prey ? game.players[game.prey] : undefined;

    return (
        <div className="d-flex align-items-center gap-1 text-muted small mt-1">
            <i className="bi bi-arrow-left-short"/>
            <span>{pred?.playerName ?? game.predator}</span>
            {pred?.pinged && <i className="bi-exclamation-triangle text-danger"/>}
            <span className="mx-1 text-body-tertiary">·</span>
            <strong>{active?.playerName ?? game.activePlayer}</strong>
            <span className="mx-1 text-body-tertiary">·</span>
            <span>{prey?.playerName ?? game.prey}</span>
            {prey?.pinged && <i className="bi-exclamation-triangle text-danger"/>}
            <i className="bi bi-arrow-right-short"/>
        </div>
    );
}

function GameRow({game, player, showSeatRow}: { game: GameStatusBean; player: string | null; showSeatRow: boolean }) {
    const self = player ? game.players[player] : undefined;
    const pinged = !!self?.pinged;
    const needsAttention = !!self && !self.current;
    const borderClass = pinged
        ? 'border-start border-3 border-danger'
        : needsAttention
            ? 'border-start border-3 border-primary-subtle'
            : '';

    return (
        <li className={`list-group-item game-entry px-2 py-2 ${borderClass}`}>
            <Link to={pathForGame(game.gameId)} className="d-block text-decoration-none text-reset">
                <div className="d-flex align-items-center justify-content-between">
          <span className="fw-bold text-break">
            {pinged && <i className="me-1 text-danger bi-exclamation-triangle"/>}
              {!pinged && needsAttention && <i className="me-1 bi-bell"/>}
              {game.name}
          </span>
                    {game.turn && <small className="text-muted ms-2 text-nowrap">{game.turn}</small>}
                </div>
                {showSeatRow && game.predator && <SeatRow game={game}/>}
            </Link>
        </li>
    );
}

const EMPTY: GamesSummary = {games: [], tournament: [], ousted: []};

export function GamesPanel() {
    const {player} = useAuth();
    const summary = useApiPoll<GamesSummary>('/main/games', EMPTY, 'main:games');
    const [activeTab, setActiveTab] = useState<TabId>('myGames');

    return (
        <Card className="flex-fill d-flex flex-column" style={{minHeight: 0}}>
            <CardHeader>
                <CardTitle>Games List</CardTitle>
            </CardHeader>
            <ul className="nav nav-tabs card-header-tabs ms-0 border-0 bg-secondary-subtle" role="tablist">
                {TABS.map((tab) => (
                    <li key={tab.id} className="nav-item" role="presentation">
                        <button
                            className={`nav-link px-3 py-2${activeTab === tab.id ? ' active' : ''}`}
                            type="button"
                            role="tab"
                            aria-selected={activeTab === tab.id}
                            onClick={() => setActiveTab(tab.id)}
                        >
                            {tab.label} <span
                            className="badge rounded-pill bg-secondary ms-1">{summary[tab.field].length}</span>
                        </button>
                    </li>
                ))}
            </ul>
            {/*
        Bootstrap's own .tab-pane{display:none} rule has no matching
        .active{display:block} override in its CSS — that toggle is normally
        applied by Bootstrap's JS (bootstrap.bundle.js), which isn't loaded
        here (see TopBar's dropdown fix for the same root cause) — so
        visibility is controlled explicitly via inline style, not just the
        active/show classes .tab-content-fill's CSS keys off of for sizing.
      */}
            <div className="tab-content tab-content-fill">
                {TABS.map((tab) => (
                    <div
                        key={tab.id}
                        className={`tab-pane${activeTab === tab.id ? ' active show' : ''}`}
                        role="tabpanel"
                        style={{display: activeTab === tab.id ? 'block' : 'none'}}
                    >
                        <ul className="list-group list-group-flush">
                            {summary[tab.field].map((game) => (
                                <GameRow key={game.name} game={game} player={player}
                                         showSeatRow={tab.showSeatRow}/>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>
        </Card>
    );
}
