import { useState } from 'react';
import { GamesPanel } from './main/GamesPanel';
import { GlobalChat } from './main/GlobalChat';
import { OnlineUsers } from './main/OnlineUsers';
import { Resources } from './main/Resources';
import { SiteNotes } from './main/SiteNotes';

// Pure layout — every child owns its data through a dedicated endpoint
// (/main/games, /main/online, /main/notes, /main/chat + /main/chat/history).
//
// A plain (non-wrapping) flex row rather than a Bootstrap grid: .row bakes in
// flex-wrap:wrap for responsive column-wrapping, which breaks cross-axis
// height constraint — every column renders at its tallest child's content
// height instead of the row's (min-height:0-constrained) box height.
//
// Below lg there's no room for a third column, so the left+center pane
// (Games/Chat) and the right pane (Notes/Online/Resources) share the space
// via a two-way toggle rather than the right column simply disappearing. At
// lg+ both panes show at once and the toggle is hidden.
export function MainPage() {
  const [mobilePane, setMobilePane] = useState<'main' | 'info'>('main');

  const toggleBtn = (pane: 'main' | 'info', label: string) => (
    <button
      type="button"
      onClick={() => setMobilePane(pane)}
      className={`px-3 py-1 text-sm rounded border border-line-accent ${
        mobilePane === pane ? 'bg-accent text-surface' : 'text-ink-secondary hover:bg-hover'
      }`}
    >
      {label}
    </button>
  );

  return (
    <div className="flex flex-col flex-1 min-h-0 gap-2 p-3 bg-base text-ink lg:flex-row">
      <div className="lg:hidden flex gap-1">
        {toggleBtn('main', 'Games & Chat')}
        {toggleBtn('info', 'Info')}
      </div>

      <div
        className={`flex-col md:flex-row flex-1 min-h-0 gap-2 lg:flex ${
          mobilePane === 'info' ? 'hidden' : 'flex'
        }`}
      >
        <div className="flex flex-col flex-1 min-h-0 min-w-0 md:flex-none md:w-1/3 lg:w-1/4">
          <GamesPanel />
        </div>
        <div className="flex flex-col flex-1 min-h-0 min-w-0">
          <GlobalChat />
        </div>
      </div>

      <div
        className={`flex-col flex-1 min-h-0 gap-2 overflow-y-auto lg:flex lg:flex-none lg:w-1/4 ${
          mobilePane === 'main' ? 'hidden' : 'flex'
        }`}
      >
        <SiteNotes />
        <OnlineUsers />
        <Resources />
      </div>
    </div>
  );
}
