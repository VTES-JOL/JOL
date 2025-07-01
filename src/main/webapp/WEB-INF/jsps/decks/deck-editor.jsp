<div class="card shadow">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <h5 class="d-inline">Deck Editor</h5>
        <button class="btn btn-outline-secondary border btn-sm float-right" onclick="newDeck()">New <i
                class="bi-bookmark-plus"></i></button>
    </div>
    <div class="card-body">
        <div class="row">
            <label class="col-form-label col-2" for="deckName">Name</label>
            <div class="col-8">
                <input class="form-control w-100" type="text" id="deckName"/>
            </div>
            <div class="col-2">
                <button class="btn btn-outline-secondary border btn-sm mb-1" onclick="saveDeck()">Save</button>
            </div>
        </div>
        <div class="row">
            <label for="validatorFormat" class="col-form-label col-2">Format</label>
            <div class="col-8">
                <select name="validatorFormat" id="validatorFormat" class="form-select">
                    <option value="STANDARD">Standard</option>
                    <option value="V5">V5</option>
                    <option value="V5 Strict">V5 Strict</option>
                    <option value="DUEL">Duel</option>
                </select>
            </div>
            <div class="col-2">
                <button class="btn btn-outline-secondary border btn-sm mb-1" onclick="validate()">Validate</button>
            </div>
        </div>
        <label for="deckText" class="form-label">Contents</label>
        <textarea id="deckText" class="form-control"></textarea>
    </div>
</div>
<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary">
        <h5>Errors</h5>
    </div>
    <div id="deckErrors" class="card-body"></div>
</div>