<div class="card shadow mt-lg-0 mt-md-2 mt-2">
    <div class="card-header bg-body-secondary">Information</div>
    <div class="card-bod p-1" id="informationPanel">
        <p class="d-inline-flex gap-1 mb-2">
            <button class="btn btn-secondary btn-sm" type="button" data-bs-toggle="collapse"
                    data-bs-target="#globalNotesPanel" aria-expanded="true"
                    aria-controls="globalNotesPanel">Global Notes
            </button>
            <button class="btn btn-secondary btn-sm player-only" type="button" data-bs-toggle="collapse"
                    data-bs-target="#privateNotesPanel" aria-expanded="false"
                    aria-controls="privateNotesPanel">Private Notes
            </button>
            <button class="btn btn-secondary btn-sm" type="button" data-bs-toggle="collapse"
                    data-bs-target="#historyPanel" aria-expanded="false" aria-controls="historyPanel">
                History
            </button>
            <button class="btn btn-secondary btn-sm player-only" type="button" data-bs-toggle="collapse"
                    data-bs-target="#gameDeckPanel" aria-expanded="false" aria-controls="gameDeckPanel"
                    onclick="doShowDeck()">Game Deck
            </button>
        </p>
        <div class="collapse multi-collapse show" id="globalNotesPanel" data-bs-parent="#informationPanel">
            <label for="globalNotes">Global Notes</label>
            <textarea id="globalNotes" class="form-control" onblur="sendGlobalNotes();"></textarea>
        </div>
        <div class="collapse multi-collapse" id="privateNotesPanel" data-bs-parent="#informationPanel">
            <label for="privateNotes">Private Notes</label>
            <textarea id="privateNotes" class="form-control" onblur="sendPrivateNotes();"></textarea>
        </div>
        <div class="collapse multi-collapse p-0" id="historyPanel" data-bs-parent="#informationPanel">
            <label for="historySelect">History:</label>
            <select id="historySelect" class="form-select form-select-sm mb-1"
                    onchange="getHistory()"></select>
            <div id="gameHistory" class="bg-white p-1"></div>
        </div>
        <div class="collapse multi-collapse " id="gameDeckPanel" data-bs-parent="#informationPanel">
            <div id="gameDeck"></div>
        </div>
    </div>
</div>