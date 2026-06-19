<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Decks</span>
        <button class="btn btn-sm btn-outline-secondary" onclick="newDeck()">New <i class="bi-bookmark-plus"></i></button>
    </div>
    <div class="card-body p-2">
        <input id="deckTextFilter" type="text" class="form-control form-control-sm rounded-pill mb-1"
               placeholder="Filter by name or comment..." oninput="filterDeckList()">
        <select id="deckFilter" class="form-select form-select-sm" onchange="selectDeckFilter()"></select>
    </div>
    <div class="flex-fill overflow-auto" style="min-height: 0; overflow-x: clip;">
        <table class="table table-sm table-hover mb-0">
            <tbody id="decks"></tbody>
        </table>
    </div>
</div>
