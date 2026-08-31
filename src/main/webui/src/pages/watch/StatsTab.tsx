import { useState } from 'react';
import { Trash2 } from 'lucide-react';
import { TabBar, type TabDef } from '../../components/TabBar';
import { Button } from '../../components/ui/Button';
import { Switch } from '../../components/ui/Switch';
import { useAuth } from '../../auth/useAuth';
import { PlayerStats } from './stats/PlayerStats';
import { DeckStats } from './stats/DeckStats';
import { NationStats } from './stats/NationStats';
import { PersonalStats } from './stats/PersonalStats';
import { GameStats } from './stats/GameStats';
import { JolStats } from './stats/JolStats';

type StatsSubTab = 'player' | 'deck' | 'nation' | 'personal' | 'game' | 'jol';

const TABS: TabDef<StatsSubTab>[] = [
  { id: 'player', label: 'Players' },
  { id: 'deck', label: 'Decks' },
  { id: 'nation', label: 'Nations' },
  { id: 'personal', label: 'Personal' },
  { id: 'game', label: 'Games' },
  { id: 'jol', label: 'Jol' },
];

const DATE_INPUT =
  'rounded border border-line bg-surface/70 px-2 py-1 text-sm text-ink outline-none focus:border-accent/60';

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
    <div className="flex flex-col min-h-0 overflow-hidden">
      <div className="flex flex-wrap items-center gap-2 mt-3 px-1">
        <label htmlFor="statsFrom" className="text-xs text-ink-muted">
          From
        </label>
        <input
          id="statsFrom"
          type="date"
          className={DATE_INPUT}
          value={fromDate}
          onChange={(e) => setFromDate(e.target.value)}
        />
        <label htmlFor="statsTo" className="text-xs text-ink-muted">
          To
        </label>
        <input id="statsTo" type="date" className={DATE_INPUT} value={toDate} onChange={(e) => setToDate(e.target.value)} />
        <Button
          variant="secondary"
          size="sm"
          onClick={() => setRange(`${currentYear() - 1}-01-01`, `${currentYear() - 1}-12-31`)}
        >
          Last Year
        </Button>
        <Button
          variant="secondary"
          size="sm"
          onClick={() => setRange(`${currentYear()}-01-01`, `${currentYear()}-12-31`)}
        >
          Current Year
        </Button>
        <Button variant="ghost" size="sm" title="Reset all filters" onClick={reset}>
          <Trash2 size={14} />
        </Button>
        <Switch
          id="onlyTournaments"
          label="Only Tournaments"
          checked={isTourney}
          onChange={(e) => setIsTourney(e.target.checked)}
        />
      </div>

      <TabBar tabs={TABS} active={subTab} onChange={setSubTab} className="mt-3" />

      <div className="flex-1 min-h-0 mt-3">
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
