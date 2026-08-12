<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary">
        <span class="fw-semibold">Site Notes</span>
    </div>
    <div class="card-body">
        <label for="siteNotesText" class="form-label">Markdown</label>
        <textarea id="siteNotesText" class="form-control" rows="6"></textarea>
        <button onclick="adminSaveSiteNotes()" class="btn btn-outline-secondary btn-sm mt-2">Save</button>
        <button onclick="adminClearSiteNotes()" class="btn btn-outline-danger btn-sm mt-2">Clear</button>
    </div>
</div>
