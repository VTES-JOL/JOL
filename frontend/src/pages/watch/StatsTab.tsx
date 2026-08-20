import { useState } from 'react';
import { useAuth } from '../../nav/useAuth';
import { PlayerStats } from './stats/PlayerStats';
import { DeckStats } from './stats/DeckStats';
import { NationStats } from './stats/NationStats';
import { PersonalStats } from './stats/PersonalStats';
import { GameStats } from './stats/GameStats';
import { JolStats } from './stats/JolStats';

type StatsSubTab = 'player' | 'deck' | 'nation' | 'personal' | 'game' | 'jol';

const TABS: { id: StatsSubTab; label: string }[] = [
  { id: 'player', label: 'Players' },
  { id: 'deck', label: 'Decks' },
  { id: 'nation', label: 'Nations' },
  { id: 'personal', label: 'Personal' },
  { id: 'game', label: 'Games' },
  { id: 'jol', label: 'Jol' },
];

function currentYear() {
  return new Date().getFullYear();
}

export function StatsTab() {
  const { player } = useAuth();
  const [subTab, setSubTab] = useState<StatsSubTab>('player');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [isTourney, setIsTourney] = useState(false);

  const setRange = (from: string, to: string) => {
    setFromDate(from);
    setToDate(to);
  };

  const reset = () => {
    setFromDate('');
    setToDate('');
    setIsTourney(false);
  };

  return (
    <div className="tab-pane fade show active overflow-hidden">
      <div className="container mt-3">
        <div className="row align-items-center g-2">
          <div className="col-auto">
            <label className="form-label mb-0">From</label>
          </div>
          <div className="col-auto">
            <input type="date" className="form-control" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
          </div>
          <div className="col-auto">
            <label className="form-label mb-0">To</label>
          </div>
          <div className="col-auto">
            <input type="date" className="form-control" value={toDate} onChange={(e) => setToDate(e.target.value)} />
          </div>
          <div className="col-auto">
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm"
              onClick={() => setRange(`${currentYear() - 1}-01-01`, `${currentYear() - 1}-12-31`)}
            >
              Last Year
            </button>
          </div>
          <div className="col-auto">
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm"
              onClick={() => setRange(`${currentYear()}-01-01`, `${currentYear()}-12-31`)}
            >
              Current Year
            </button>
          </div>
          <div className="col-auto">
            <button className="btn btn-outline-secondary btn-sm" title="Reset all filter" onClick={reset}>
              <i className="bi-trash" />
            </button>
          </div>
          <div className="form-check form-switch col-auto m-2 pt-1">
            <input
              className="form-check-input"
              type="checkbox"
              role="switch"
              id="onlyTournaments"
              checked={isTourney}
              onChange={(e) => setIsTourney(e.target.checked)}
            />
            <label className="form-check-label" htmlFor="onlyTournaments">
              Only Tournaments
            </label>
          </div>
        </div>
      </div>
      <ul className="nav nav-tabs mt-3">
        {TABS.map((t) => (
          <li className="nav-item" key={t.id}>
            <button className={`nav-link ${subTab === t.id ? 'active' : ''}`} onClick={() => setSubTab(t.id)}>
              {t.label}
            </button>
          </li>
        ))}
      </ul>
      <div className="tab-content mt-3">
        {subTab === 'player' && <PlayerStats fromDate={fromDate} toDate={toDate} isTourney={isTourney} />}
        {subTab === 'deck' && <DeckStats fromDate={fromDate} toDate={toDate} isTourney={isTourney} />}
        {subTab === 'nation' && <NationStats fromDate={fromDate} toDate={toDate} isTourney={isTourney} />}
        {subTab === 'personal' && player && (
          <PersonalStats player={player} fromDate={fromDate} toDate={toDate} isTourney={isTourney} />
        )}
        {subTab === 'game' && <GameStats fromDate={fromDate} toDate={toDate} isTourney={isTourney} />}
        {subTab === 'jol' && <JolStats fromDate={fromDate} toDate={toDate} isTourney={isTourney} />}
      </div>
    </div>
  );
}
