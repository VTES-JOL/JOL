<div class="card shadow commands" id="commandCard">
    <div class="card-header bg-body-secondary">Commands</div>
    <div class="card-body p-2">
        <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm">
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
                        <input type="text" class="form-control form-control-sm player-only" id="command"
                               placeholder="Enter game commands">
                    </div>
                    <label for="chat">Chat</label>
                    <div class="input-group input-group-sm mb-2">
                        <button type="button" class="btn btn-outline-secondary player-only" data-bs-toggle="modal"
                                data-bs-target="#quickChatModal" tabindex="-1">...
                        </button>
                        <input type="text" class="form-control form-control-sm can-chat" id="chat"
                               placeholder="Chat to other players">
                    </div>
                    <label for="ping" class="player-only">Ping</label>
                    <select id="ping" class="form-select form-select-sm mb-2 player-only"></select>
                    <div class="mt-2 d-flex justify-content-between">
                        <button class="btn btn-secondary btn-sm" id="gameSubmit" type="submit">
                            Submit
                        </button>
                        <button class="btn btn-warning btn-sm player-only" id="endTurn" type="button"
                                onclick="doEndTurn()">End Turn
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>