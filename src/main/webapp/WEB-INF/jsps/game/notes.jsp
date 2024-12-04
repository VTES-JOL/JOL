<div class="card shadow notes">
  <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
    <span>Notes</span>
    <button class="border-0 shadow rounded-pill bg-light" onclick="toggleNotes();"><i class="bi bi-info-lg me-2"></i>Deck</button>
  </div>
  <div class="card-body p-0">
    <textarea id="globalNotes" class="form-control scrollable" onblur="sendGlobalNotes();" placeholder="Global Notes"></textarea>
    <textarea id="privateNotes" class="form-control scrollable" onblur="sendPrivateNotes();" placeholder="Private Notes"></textarea>
  </div>
</div>