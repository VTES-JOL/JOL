<div class="card shadow flex-fill d-flex flex-column min-h-0">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold" id="deckPreviewTitle">Preview</span>
        <span class="d-flex align-items-center gap-2">
            <span id="deckSummary" class="text-muted small"></span>
            <button class="btn btn-sm btn-outline-secondary" onclick="enterEditMode()">Edit <i class="bi-pencil"></i></button>
        </span>
    </div>
    <div class="card-body p-2 flex-fill d-flex flex-column overflow-auto min-h-0">
        <div id="deckPreview" class="flex-fill px-1 min-h-0"></div>
        <div id="deckValidation" class="mt-2 d-none">
            <div id="deckValidationBadge"></div>
            <div id="deckValidationErrors" class="small text-danger mt-1"></div>
        </div>
    </div>
</div>
