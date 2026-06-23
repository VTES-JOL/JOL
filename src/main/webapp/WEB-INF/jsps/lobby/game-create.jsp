<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">New Game</span>
        <button class="btn btn-sm btn-outline-secondary" onclick="exitLobbyDetail()">Cancel</button>
    </div>
    <div class="card-body p-3 overflow-auto" style="min-height: 0">
        <div class="mb-3">
            <label for="lobbyGameName" class="form-label">Name</label>
            <input type="text" class="form-control" id="lobbyGameName" maxlength="60"
                   placeholder="Game name (no ' or &quot; characters)"/>
        </div>
        <div class="mb-3">
            <label class="form-label" for="lobbyPublicFlag">Visibility</label>
            <select name="lobbyPublicFlag" class="form-select" id="lobbyPublicFlag" onchange="toggleLobbyInviteSection()">
                <option value="PRIVATE">Private</option>
                <option value="PUBLIC">Public</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="lobbyGameFormat" class="form-label">Format</label>
            <select name="lobbyGameFormat" id="lobbyGameFormat" class="form-select"></select>
        </div>
        <div id="lobbyInviteSection" class="mb-3">
            <label class="form-label">Invite Players</label>
            <div class="d-flex gap-2 mb-2">
                <input class="form-control" id="lobbyInviteInput" placeholder="Start typing a player name"/>
                <button class="btn btn-outline-secondary btn-sm text-nowrap" onclick="addLobbyPendingInvite()">
                    Add <i class="bi-person-plus"></i>
                </button>
            </div>
            <ul id="lobbyPendingInvites" class="list-group list-group-flush small"></ul>
        </div>
        <button class="btn btn-outline-secondary" onclick="doCreateLobbyGame()">
            Create Game <i class="bi-plus-circle"></i>
        </button>
        <span id="lobbyCreateError" class="text-danger small ms-2"></span>
    </div>
</div>
