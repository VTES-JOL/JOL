<div id="gameChatCard" class="game-chat-inner">
    <div class="game-chat-subheader">
        <span class="game-chat-subheader-title">Chat</span>
        <button class="btn btn-sm btn-link text-body p-0" onclick="toggleChat()">
            <i class="bi bi-clock-history me-1"></i>History
        </button>
    </div>
    <div id="gameChatOutput" class="game-chat-output p-1 overflow-y-auto"></div>
    <div class="p-1 border-top flex-shrink-0">
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
