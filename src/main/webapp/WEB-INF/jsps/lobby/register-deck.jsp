<div class="card shadow">
  <div class="card-header bg-body-secondary">
    <h5>Register Deck</h5>
  </div>
  <ul class="list-group list-group-flush" id="invitedGames"></ul>
  <div class="card-footer">
    <div class="mb-1">
      <label for="invitedGamesList" class="form-label mt-2">Invited Games</label>
      <select class="form-select" id="invitedGamesList"></select>
    </div>
    <div class="mb-1">
      <label for="myDeckList" class="form-label">Decks</label>
      <select class="form-select" id="myDeckList"></select>
    </div>
    <button class="btn btn-outline-secondary btn-sm mt-2" onclick="registerDeck()">Register</button>
    <div id="registerResult"></div>
  </div>
</div>