<div class="modal fade" id="notesModal" tabindex="-1" aria-labelledby="notesModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="notesModalLabel">Notes</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <label for="globalNotes" class="form-label fw-semibold">Global Notes</label>
                <textarea id="globalNotes" class="form-control mb-3" rows="5" onblur="sendGlobalNotes();"
                          placeholder="Visible to all players in this game"></textarea>
                <label for="privateNotes" class="form-label fw-semibold player-only">Private Notes</label>
                <textarea id="privateNotes" class="form-control player-only" rows="5" onblur="sendPrivateNotes();"
                          placeholder="Only visible to you"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
