<div class="card shadow commands" id="commandCard">
    <div class="card-header bg-body-secondary">Commands</div>
    <div class="card-body p-2">
        <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm">
            <div class="align-items-center">
                <label for="phase" class="player-only">Phase</label>
                <select id="phase" class="form-select form-select-sm mb-2 player-only"></select>
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
                <label for="endTurn" class="player-only">End</label>
                <select id="endTurn" class="form-select form-select-sm mb-2 player-only">
                    <option value="No">No</option>
                    <option value="Yes">Yes</option>
                </select>
                <button class="btn btn-secondary btn-sm" id="gameSubmit">Submit</button>
            </div>
        </form>
    </div>
</div>