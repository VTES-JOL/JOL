<div class="card shadow flex-fill d-flex flex-column" style="min-height: 0">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="d-flex align-items-center gap-2">
            <span class="fw-semibold" id="lobbyDetailName"></span>
            <span id="lobbyDetailFormatBadge" class="badge bg-secondary"></span>
            <span id="lobbyDetailVisibilityBadge" class="badge"></span>
        </span>
        <span class="d-flex gap-1">
            <button id="lobbyDetailStartBtn" class="btn btn-sm btn-outline-secondary d-none" onclick="startLobbyGame()">
                Start <i class="bi-play-circle"></i>
            </button>
            <button id="lobbyDetailCloseBtn" class="btn btn-sm btn-outline-secondary d-none" onclick="closeLobbyGame()">
                Close <i class="bi-x-circle"></i>
            </button>
            <button id="lobbyDetailJoinBtn" class="btn btn-sm btn-outline-secondary d-none" onclick="joinLobbyGame()">
                Join <i class="bi-box-arrow-in-right"></i>
            </button>
            <button id="lobbyDetailLeaveBtn" class="btn btn-sm btn-outline-secondary d-none" onclick="leaveLobbyGame()">
                Leave <i class="bi-box-arrow-left"></i>
            </button>
            <button class="btn btn-sm btn-outline-secondary" onclick="exitLobbyDetail()">
                <i class="bi-x"></i>
            </button>
        </span>
    </div>
    <div class="card-body p-0 d-flex flex-column overflow-auto" style="min-height: 0">
        <!-- Players / registrations -->
        <div class="p-3 border-bottom">
            <div class="fw-semibold small text-muted mb-2">Players</div>
            <table class="table table-sm table-hover mb-0" id="lobbyDetailPlayerTable">
                <tbody id="lobbyDetailPlayerBody"></tbody>
            </table>
        </div>

        <!-- Invite section (owner only) -->
        <div id="lobbyDetailInviteSection" class="p-3 border-bottom d-none">
            <div class="fw-semibold small text-muted mb-2">Invite Player</div>
            <div class="d-flex gap-2">
                <input class="form-control form-control-sm" id="lobbyDetailInviteInput" placeholder="Start typing a player name"/>
                <button class="btn btn-sm btn-outline-secondary text-nowrap" onclick="inviteLobbyPlayer()">
                    Invite <i class="bi-person-plus"></i>
                </button>
            </div>
        </div>

        <!-- Deck registration (invited/registered player) -->
        <div id="lobbyDetailDeckSection" class="p-3 border-bottom d-none">
            <div class="fw-semibold small text-muted mb-2">Register Deck</div>
            <div class="d-flex align-items-center gap-2">
                <div class="dropdown">
                    <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                            data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
                        Choose Deck
                    </button>
                    <div>
                        <ul class="dropdown-menu" id="lobbyDeckDropdown">
                            <li>
                                <input class="form-control form-control-sm mx-2" id="lobbyDeckSearch"
                                       style="width: calc(100% - 1rem)" type="text"
                                       placeholder="Search..." oninput="filterLobbyDeckList()">
                            </li>
                        </ul>
                    </div>
                </div>
                <span id="lobbyRegisteredDeckName" class="text-muted small"></span>
            </div>
        </div>

        <!-- Deck preview -->
        <div id="lobbyDeckPreviewSection" class="p-3 d-none flex-fill overflow-auto" style="min-height: 0">
            <div class="fw-semibold small text-muted mb-2">Registered Deck</div>
            <div id="lobbyDeckPreview"></div>
        </div>

        <div id="lobbyDetailMsg" class="p-3 small text-success d-none"></div>
    </div>
</div>
