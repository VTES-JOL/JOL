<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Edit Deck</span>
        <span class="d-flex gap-1">
            <button class="btn btn-sm btn-outline-secondary" onclick="saveDeck()">Save <i class="bi-floppy"></i></button>
            <button class="btn btn-sm btn-outline-secondary" onclick="exitEditMode()">Cancel</button>
        </span>
    </div>
    <div class="card-body p-2 flex-fill overflow-auto px-3" style="min-height: 0">
        <div class="row mb-2">
            <label class="col-form-label col-2" for="deckName">Name</label>
            <div class="col-10">
                <input class="form-control form-control-sm w-100" type="text" id="deckName"/>
            </div>
        </div>
        <div class="row mb-2">
            <label for="validatorFormat" class="col-form-label col-2">Format</label>
            <div class="col-7">
                <select name="validatorFormat" id="validatorFormat" class="form-select form-select-sm"></select>
            </div>
            <div class="col-3">
                <button class="btn btn-outline-secondary btn-sm w-100" onclick="validate()">Validate</button>
            </div>
        </div>
        <label for="deckText" class="form-label small text-muted mb-1">Contents</label>
        <textarea id="deckText" class="form-control form-control-sm mb-2"></textarea>
        <label for="deckComment" class="form-label small text-muted mb-1">Comment</label>
        <textarea id="deckComment" class="form-control form-control-sm scrollable player-only"
                  placeholder="Deck comment..." rows="3"></textarea>
        <div id="deckErrors" class="text-danger small mt-2"></div>
    </div>
</div>
