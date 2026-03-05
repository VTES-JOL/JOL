<div class="card shadow panel-default notes" id="notesCard">
    <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>Notes</span>
        <button class="border-0 shadow rounded-pill bg-light player-only" onclick="toggleNotes();"><i
                class="bi bi-info-lg me-2"></i>Deck
        </button>
    </div>
    <div class="card-body p-0">
        <label for="globalNotes" class="d-none"></label>
        <textarea id="globalNotes" class="form-control scrollable" onblur="sendGlobalNotes();"
                  placeholder="Global Notes"></textarea>
        <div class="border-top border-3 border-dark p-1" onclick="toggleDisplay('quickAction')">
            <a id="quickAction" class="text-decoration-none d-none" onclick="toggleDisplay('quickAction')">
                <button class="btn-sm btn-outline-secondary bg-secondary-subtle" onclick="sendCommand('unlock')"><i class="bi bi-unlock m-1"></i></button>
                <button class="btn-sm btn-outline-secondary bg-secondary-subtle" onclick="toggleDisplay('deckBody');" title="Library"><i class="bi bi-stack m-1"></i></button>
                <button class="btn-sm btn-outline-secondary bg-secondary-subtle" onclick="toggleDisplay('privateNotesDiv');" title="Privates Notes"><i class="bi bi-journal m-1"></i></button>
                <button type="button" class="btn-sm btn-outline-success bg-success-subtle" onclick="sendCommand('pool +1')">
                    +1
                </button>
                <button type="button" class="btn-sm btn-outline-danger bg-danger-subtle" onclick="sendCommand('pool -1')">
                    -1
                </button>
            </a>
        </div>
    </div>
    <div class="card-body d-flex p-0 scrollable">
        <div id="deckBody" class="d-none d-flex w-100">
            <ol id="deckBodyList" class="list-group list-group-numbered p-0 w-100"></ol>
        </div>
        <div id="privateNotesDiv" class="card-body d-flex p-0 w-100">
            <label for="privateNotes" class="d-none"></label>
            <textarea id="privateNotes" class="form-control player-only" onblur="sendPrivateNotes();"
                      placeholder="Private Notes"></textarea>
        </div>
    </div>
</div>