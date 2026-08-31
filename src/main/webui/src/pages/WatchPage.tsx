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
    <div className="flex flex-col flex-1 min-h-0 p-4 bg-base text-ink">
      <TabBar tabs={TABS} active={tab} onChange={setTab} />
      <div className="flex-1 min-h-0 mt-3 flex flex-col">
        {tab === 'active' && <ActiveGamesTab />}
        {tab === 'past' && <PastGamesTab />}
        {tab === 'stats' && <StatsTab />}
      </div>
    </div>
  );
}
