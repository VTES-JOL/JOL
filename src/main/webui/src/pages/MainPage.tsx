import { useState } from 'react';
import { GamesPanel } from './main/GamesPanel';
import { GlobalChat } from './main/GlobalChat';
import { OnlineUsers } from './main/OnlineUsers';
import { Resources } from './main/Resources';
import { SiteNotes } from './main/SiteNotes';
import './MainPage.css';

// No page-level data fetching or setup left here at all: every child below
// owns its data through a dedicated endpoint (/main/games, /main/online,
// /main/notes, /main/chat + /main/chat/history), none of which depend on
// server-side "current view" state (that's a legacy JSP-shell concept this
// page has no need for), and an unauthenticated response is already handled
// globally by the api client's 401 redirect. This page is a pure layout.
//
// Deliberately NOT Bootstrap's .row/.col-* grid: that system bakes in
// flex-wrap:wrap (for responsive column-wrapping), which breaks cross-axis
// height constraint — confirmed empirically: with three .col-lg-* stretched
// across one .row, every column rendered at its tallest child's *content*
// height instead of the row's own (correctly min-height:0-constrained)
// box height. .main-layout/.main-col-* are this page's own classes
// (MainPage.css) built for exactly this — a plain (non-wrapping) flex row
// that legacy's own main/layout.jsp already used for this same panel set.
// Below the lg breakpoint there's no room for a third column, so the
// left+center pane (Games/Chat) and the right pane (Notes/Online/Resources)
// share the same space via a two-way toggle instead of the right column
// simply disappearing — it used to be display:none below 992px, making
// Site Notes/Online Users/Resources unreachable on any tablet or narrow
// laptop. At lg+ both panes show at once (see MainPage.css) and this
// toggle is hidden.
export function MainPage() {
  const [mobilePane, setMobilePane] = useState<'main' | 'info'>('main');

  return (
    <div className="main-layout p-3">
      <div className="main-mobile-tabs d-lg-none btn-group" role="group">
        <button
          type="button"
          className={`btn btn-sm ${mobilePane === 'main' ? 'btn-secondary' : 'btn-outline-secondary'}`}
          onClick={() => setMobilePane('main')}
        >
          Games &amp; Chat
        </button>
        <button
          type="button"
          className={`btn btn-sm ${mobilePane === 'info' ? 'btn-secondary' : 'btn-outline-secondary'}`}
          onClick={() => setMobilePane('info')}
        >
          Info
        </button>
      </div>
      <div className={`main-primary ${mobilePane === 'info' ? 'main-hidden-mobile' : ''}`}>
        <div className="main-col-left">
          <GamesPanel />
        </div>
        <div className="main-col-center">
          <GlobalChat />
        </div>
      </div>
      {/*
        overflow-y-auto here (not just relying on OnlineUsers' own internal
        scroll) since Resources is static content below it — without this,
        it'd get pushed out with zero visible space whenever OnlineUsers'
        flex-fill claims all the column's height.
      */}
      <div className={`main-col-right overflow-y-auto ${mobilePane === 'main' ? 'main-hidden-mobile' : ''}`}>
        <SiteNotes />
        <OnlineUsers />
        <Resources />
      </div>
    </div>
  );
}
