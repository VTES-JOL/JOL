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
    <div className="flex flex-col flex-1 min-h-0 gap-3 p-4 bg-base text-ink">
      <div className="shrink-0 rounded-lg border border-line-accent bg-surface/85 px-2 backdrop-blur-md">
        <TabBar tabs={TABS} active={tab} onChange={setTab} />
      </div>
      <div className="flex-1 min-h-0 flex flex-col">
        {tab === 'active' && <ActiveGamesTab />}
        {tab === 'past' && <PastGamesTab />}
        {tab === 'stats' && <StatsTab />}
      </div>
    </div>
  );
}
