<div class="card shadow commands" id="commandCard">
    <div class="card-header bg-body-secondary">Commands</div>
    <div class="card-body p-2">
        <div id="gameForm">
            <div class="align-items-center">
                <div class="mt-2">
                    <label for="phase" class="player-only">Phase</label>
                    <select id="phase" class="form-select form-select-sm mb-2 player-only"></select>
                </div>
                <label for="command" class="player-only">Command</label>
                <div class="input-group input-group-sm mb-2">
                    <button type="button" class="btn btn-outline-secondary player-only" data-bs-toggle="modal"
                            data-bs-target="#quickCommandModal" tabindex="-1">...
                    </button>
                    <input type="text" class="form-control form-control-sm player-only" id="command">
                </div>
                <label for="chat">Chat</label>
                <div class="input-group input-group-sm mb-2">
                    <button type="button" class="btn btn-outline-secondary player-only" data-bs-toggle="modal"
                            data-bs-target="#quickChatModal" tabindex="-1">...
                    </button>
                    <input type="text" class="form-control form-control-sm can-chat" id="chat">
                </div>
                <label for="ping" class="player-only">Ping</label>
                <select id="ping" class="form-select form-select-sm mb-2 player-only"></select>
                <div class="mt-2 d-flex justify-content-between">
                    <button class="btn btn-secondary btn-sm" id="gameSubmit" onclick="doSubmit()">Submit</button>
                    <button class="btn btn-outline-secondary btn-sm player-only" id="endTurn" onclick="endTurn()">End Turn</button>
                </div>
            </div>
        </div>
    </div>
</div>