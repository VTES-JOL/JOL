<div class="card shadow">
  <div class="card-header bg-body-secondary">
    <h5>Replace Player</h5>
  </div>
  <div class="card-body">
    <label for="adminGameList" class="form-label">Games</label>
    <select id="adminGameList" class="form-select" onchange="adminChangeGame()"></select>
    <label for="adminReplacePlayerList" class="form-label">Player to replace:</label>
    <select id="adminReplacePlayerList" class="form-select"></select>
    <label for="adminReplacementList" class="form-label">Substitute</label>
    <select id="adminReplacementList" class="form-select"></select>
    <button onclick="replacePlayer()" class="btn btn-outline-secondary btn-sm mt-2">Replace player</button>
  </div>
</div>