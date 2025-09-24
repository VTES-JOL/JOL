<div class="card shadow panel-secondary d-none" id="historyCard">
    <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>History</span>
        <button class="border-0 shadow rounded-pill bg-light" onclick="toggleChat();"><i class="bi bi-chat me-2"></i>Game
            Chat
        </button>
    </div>
    <div class="card-body p-2 overflow-hidden">
        <label for="historySelect">History:</label>
        <select id="historySelect" class="form-select form-select-sm mb-1" onchange="getHistory()"></select>
        <div id="gameHistory" class="bg-white p-1 scrollable"></div>
    </div>
</div>