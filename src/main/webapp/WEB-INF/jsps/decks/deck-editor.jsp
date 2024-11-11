<div class="card shadow">
  <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
    <h5 class="d-inline">Deck Editor</h5>
    <button class="btn btn-outline-secondary border btn-sm float-right" onclick="newDeck()">New <i class="bi-bookmark-plus"></i></button>
  </div>
  <div class="card-body">
    <div class="row align-items-center">
      <div class="col-3">
        <label class="form-label" for="deckName">Deck Name:</label>
      </div>
      <div class="col-6">
        <input class="form-control w-100" type="text" id="deckName"/>
      </div>
      <div class="col-3 d-grid gap-1 d-md-block">
        <button class="btn btn-outline-secondary border btn-sm mb-1" onclick="saveDeck()">Save</button>
        <button class="btn btn-outline-secondary border btn-sm mb-1" onclick="parseDeck()">Check</button>
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