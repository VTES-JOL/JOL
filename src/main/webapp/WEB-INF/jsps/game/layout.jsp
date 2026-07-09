<div class="game-topbar">
    <button class="btn btn-sm btn-outline-secondary d-md-none me-1" type="button"
            data-bs-toggle="offcanvas" data-bs-target="#gameSidebarOffcanvas" aria-controls="gameSidebarOffcanvas">
        <i class="bi bi-layout-sidebar"></i>
    </button>
    <button class="btn btn-sm btn-outline-secondary d-none d-md-inline-flex me-1" type="button"
            onclick="toggleHandSidebar()" title="Toggle Hand">
        <i class="bi bi-layout-sidebar"></i>
    </button>
    <span id="gameTitle" class="fs-5 user-select-all text-truncate flex-grow-1"></span>
    <span id="gameLabel" class="badge bg-secondary ms-2 flex-shrink-0"></span>
    <button class="btn btn-sm btn-outline-secondary ms-2 flex-shrink-0 d-none d-md-inline-flex"
            id="chatToggleBtn" onclick="toggleGameChat()" title="Toggle Chat">
        <i class="bi bi-chat-dots"></i>
    </button>
    <button class="btn btn-sm btn-outline-secondary ms-1 flex-shrink-0 d-none d-md-inline-flex" type="button"
            data-bs-toggle="modal" data-bs-target="#notesModal" title="Notes">
        <i class="bi bi-journal-text me-1"></i>Notes
    </button>
    <span class="d-none d-md-inline-flex ms-1">
        <button class="btn btn-sm btn-outline-secondary flex-shrink-0 player-only" type="button"
                data-bs-toggle="modal" data-bs-target="#deckModal" onclick="doShowDeck()" title="Deck">
            <i class="bi bi-layers me-1"></i>Deck
        </button>
    </span>
</div>

<div class="game-layout">
    <div class="offcanvas offcanvas-start game-sidebar" tabindex="-1" id="gameSidebarOffcanvas"
         aria-labelledby="gameSidebarLabel">
        <div class="offcanvas-header">
            <h6 class="offcanvas-title" id="gameSidebarLabel">Game Controls</h6>
            <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>
        <div class="offcanvas-body p-0">
            <jsp:include page="hand-card.jsp"/>
        </div>
    </div>

    <div class="game-state-area">
        <div class="row gy-1 gx-2" id="state"></div>
    </div>
</div>

<%-- Phase + command bar: desktop only, players only (hidden for observers via player-only) --%>
<div class="game-phasecmd-bar player-only" id="gamePhaseCmdBar">
    <div id="phaseIndicator" class="phase-steps"></div>
    <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm" class="phase-cmd-form">
        <div class="input-group input-group-sm">
            <button type="button" class="btn btn-outline-secondary player-only"
                    data-bs-toggle="modal" data-bs-target="#quickCommandModal" tabindex="-1">...</button>
            <input type="text" class="form-control player-only" id="command"
                   placeholder="Enter game command">
        </div>
    </form>
    <button class="btn btn-sm btn-warning player-only ms-2 flex-shrink-0" id="endTurn" type="button"
            onclick="doEndTurn()">End Turn</button>
</div>

<%-- Chat panel: desktop bottom, visible by default, toggled by topbar button --%>
<div class="game-chat-area d-none d-md-block" id="gameChatArea">
    <jsp:include page="game-chat.jsp"/>
    <jsp:include page="history.jsp"/>
</div>

<div class="game-mobile-bar d-flex d-md-none border-top">
    <button class="flex-fill btn btn-link text-body py-2" type="button"
            data-bs-toggle="modal" data-bs-target="#quickChatModal">
        <i class="bi bi-chat-dots fs-5 d-block"></i>
        <small>Chat</small>
    </button>
    <button class="flex-fill btn btn-link text-body py-2 player-only" type="button"
            data-bs-toggle="modal" data-bs-target="#quickCommandModal">
        <i class="bi bi-lightning-charge fs-5 d-block"></i>
        <small>Command</small>
    </button>
    <button class="flex-fill btn btn-link text-body py-2 player-only" type="button"
            data-bs-toggle="modal" data-bs-target="#deckModal" onclick="doShowDeck()">
        <i class="bi bi-layers fs-5 d-block"></i>
        <small>Deck</small>
    </button>
    <button class="flex-fill btn btn-link text-body py-2" type="button"
            data-bs-toggle="modal" data-bs-target="#notesModal">
        <i class="bi bi-journal-text fs-5 d-block"></i>
        <small>Notes</small>
    </button>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="liveToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header text-bg-secondary opacity-100">
            <strong class="me-auto">V:TES Online</strong>
            <button type="button" class="btn-close btn-outline-secondary" data-bs-dismiss="toast"
                    aria-label="Close"></button>
        </div>
        <div class="toast-body" id="gameStatusMessage"></div>
    </div>
</div>

<jsp:include page="quick-command-modal.jsp"/>
<jsp:include page="quick-chat-modal.jsp"/>
<jsp:include page="play-card-modal.jsp"/>
<jsp:include page="pick-target-modal.jsp"/>
<jsp:include page="card-modal.jsp"/>
<jsp:include page="notes-modal.jsp"/>
<jsp:include page="deck-modal.jsp"/>
<jsp:include page="region-modal.jsp"/>
