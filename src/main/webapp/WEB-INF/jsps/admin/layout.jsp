<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary p-0">
        <ul class="nav nav-tabs card-header-tabs flex-wrap ms-0 border-0" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#adminPlayersTab" role="tab">Players</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#adminActionsTab" role="tab">Game Actions</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#adminIdleGamesTab" role="tab">Idle Games</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#adminIdlePlayersTab" role="tab">Idle Players</button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#adminSiteNotesTab" role="tab">Site Notes</button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="adminPlayersTab" role="tabpanel">
            <jsp:include page="player-roles.jsp"/>
        </div>
        <div class="tab-pane fade" id="adminActionsTab" role="tabpanel">
            <div class="row g-3 p-3">
                <div class="col-md-4"><jsp:include page="replace-player.jsp"/></div>
                <div class="col-md-4"><jsp:include page="end-turn.jsp"/></div>
                <div class="col-md-4"><jsp:include page="rollback-game.jsp"/></div>
            </div>
        </div>
        <div class="tab-pane fade" id="adminIdleGamesTab" role="tabpanel">
            <jsp:include page="idle-games.jsp"/>
        </div>
        <div class="tab-pane fade" id="adminIdlePlayersTab" role="tabpanel">
            <jsp:include page="idle-players.jsp"/>
        </div>
        <div class="tab-pane fade" id="adminSiteNotesTab" role="tabpanel">
            <jsp:include page="site-notes.jsp"/>
        </div>
    </div>
</div>
