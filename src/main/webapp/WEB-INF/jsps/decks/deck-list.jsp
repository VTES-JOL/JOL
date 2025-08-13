<div class="card shadow">
    <div class="card-header bg-body-secondary">
        <h5>Decks</h5>
        <div>
            <label for="deckFilter">Filter</label>
            <select id="deckFilter" class="form-select" onchange="selectDeckFilter()">
            </select>
        </div>
    </div>
    <table class="card-body table table-sm table-hover table-bordered mb-0">
        <thead></thead>
        <tbody id="decks"></tbody>
    </table>
</div>