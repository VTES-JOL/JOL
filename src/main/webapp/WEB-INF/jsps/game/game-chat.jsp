<div class="card shadow chat" id="gameChatCard">
    <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Game Chat</span>
        <button class="border-0 shadow rounded-pill bg-light" onclick="toggleChat();"><i
                class="bi bi-clock-history me-2"></i>History</button>
    </div>
    <div class="card-body p-0 game-chat d-flex flex-column">
        <div id="gameChatOutput" class="p-1 flex-fill min-h-0 overflow-y-auto"></div>
        <div class="p-1 border-top">
            <form onsubmit="return doSubmit()" autocomplete="off">
                <div class="input-group input-group-sm">
                    <button type="button" class="btn btn-outline-secondary player-only" data-bs-toggle="modal"
                            data-bs-target="#quickChatModal" tabindex="-1">...
                    </button>
                    <input type="text" class="form-control form-control-sm can-chat" id="chat"
                           placeholder="Chat to other players">
                </div>
            </form>
        </div>
    </div>
</div>
