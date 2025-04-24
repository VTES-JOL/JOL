<div class="card shadow panel-default chat" id="gameChatCard">
    <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Game Chat</span>
        <span>
            <span id="gameLabel" class="px-2"></span>
            <button class="border-0 shadow rounded-pill bg-light" onclick="toggleChat();"><i class="bi bi-clock-history me-2"></i>History</button>
        </span>
    </div>
    <div class="card-body p-0 game-chat">
        <div id="gameChatOutput" class="bg-white p-1 scrollable"></div>
    </div>
</div>