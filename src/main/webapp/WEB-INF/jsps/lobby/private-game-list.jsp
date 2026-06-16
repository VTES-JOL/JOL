<div class="card shadow">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">My Games</span>
    </div>
    <ul class="list-group list-group-flush scrollable mhd-70" id="currentGames"></ul>
    <div class="card-footer">
        <label for="playerList" class="form-label">Players</label>
        <input class="form-control" id="playerList" placeholder="Start typing a player name"/>
        <label class="form-label mt-2" for="myGameList">Games</label>
        <select class="form-select" id="myGameList"></select>
        <button class="btn btn-outline-secondary btn-sm mt-2" onclick="invitePlayer()">Invite</button>
    </div>
</div>
