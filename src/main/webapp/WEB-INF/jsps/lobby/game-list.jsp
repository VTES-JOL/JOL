<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Games</span>
        <button class="btn btn-sm btn-outline-secondary" onclick="newLobbyGame()">
            New <i class="bi-plus-circle"></i>
        </button>
    </div>
    <div class="flex-fill min-h-0" style="overflow-y: auto; overflow-x: clip;">
        <div id="lobbyGameList" class="list-group list-group-flush"></div>
    </div>
</div>
