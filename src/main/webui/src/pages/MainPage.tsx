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
      className={`jt:px-3 jt:py-1 jt:text-sm jt:rounded jt:border jt:border-line-accent ${
        mobilePane === pane ? 'jt:bg-accent jt:text-surface' : 'jt:text-ink-secondary jt:hover:bg-hover'
      }`}
    >
      {label}
    </button>
  );

  return (
    <div className="jt-scope jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:gap-2 jt:p-3 jt:bg-base jt:text-ink jt:lg:flex-row">
      <div className="jt:lg:hidden jt:flex jt:gap-1">
        {toggleBtn('main', 'Games & Chat')}
        {toggleBtn('info', 'Info')}
      </div>

      <div
        className={`jt:flex-col jt:md:flex-row jt:flex-1 jt:min-h-0 jt:gap-2 jt:lg:flex ${
          mobilePane === 'info' ? 'jt:hidden' : 'jt:flex'
        }`}
      >
        <div className="jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:min-w-0 jt:md:flex-none jt:md:w-1/3 jt:lg:w-1/4">
          <GamesPanel />
        </div>
        <div className="jt:flex jt:flex-col jt:flex-1 jt:min-h-0 jt:min-w-0">
          <GlobalChat />
        </div>
      </div>

      <div
        className={`jt:flex-col jt:flex-1 jt:min-h-0 jt:gap-2 jt:overflow-y-auto jt:lg:flex jt:lg:flex-none jt:lg:w-1/4 ${
          mobilePane === 'main' ? 'jt:hidden' : 'jt:flex'
        }`}
      >
        <SiteNotes />
        <OnlineUsers />
        <Resources />
      </div>
    </div>
  );
}
