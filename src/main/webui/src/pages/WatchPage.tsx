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
    <div className="jt-scope jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:p-4 jt:bg-base jt:text-ink">
      <TabBar tabs={TABS} active={tab} onChange={setTab} />
      <div className="jt:flex-1 jt:min-h-0 jt:mt-3 jt:flex jt:flex-col">
        {tab === 'active' && <ActiveGamesTab />}
        {tab === 'past' && <PastGamesTab />}
        {tab === 'stats' && <StatsTab />}
      </div>
    </div>
  );
}
