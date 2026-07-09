<div id="historyCard" class="game-chat-inner d-none">
    <div class="game-chat-subheader">
        <span class="game-chat-subheader-title">History</span>
        <button class="btn btn-sm btn-link text-body p-0" onclick="toggleChat()">
            <i class="bi bi-chat me-1"></i>Chat
        </button>
    </div>
    <div class="p-2 d-flex flex-column overflow-hidden flex-grow-1">
        <select id="historySelect" class="form-select form-select-sm mb-1" onchange="getHistory()"></select>
        <div id="gameHistory" class="p-1 scrollable overflow-y-auto flex-grow-1"></div>
    </div>
</div>
