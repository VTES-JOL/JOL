import { useState } from 'react';
import { TabBar, type TabDef } from '../components/TabBar';
import { ActiveGamesTab } from './watch/ActiveGamesTab';
import { PastGamesTab } from './watch/PastGamesTab';
import { StatsTab } from './watch/StatsTab';

type MainTab = 'active' | 'past' | 'stats';

const TABS: TabDef<MainTab>[] = [
  { id: 'active', label: 'Active Games' },
  { id: 'past', label: 'Past Games' },
  { id: 'stats', label: 'Statistics' },
];

export function WatchPage() {
  const [tab, setTab] = useState<MainTab>('active');

  return (
    <div className="p-3 flex-fill d-flex flex-column min-h-0">
      <div className="card shadow flex-fill d-flex flex-column min-h-0">
        <div className="card-header bg-body-secondary p-0">
          <TabBar
            tabs={TABS}
            active={tab}
            onChange={setTab}
            className="card-header-tabs ms-0 border-0"
            tabClassName="px-3 py-2"
          />
        </div>
        <div className="tab-content-fill flex-fill d-flex flex-column min-h-0">
          {tab === 'active' && <ActiveGamesTab />}
          {tab === 'past' && <PastGamesTab />}
          {tab === 'stats' && <StatsTab />}
        </div>
      </div>
    </div>
  );
}
