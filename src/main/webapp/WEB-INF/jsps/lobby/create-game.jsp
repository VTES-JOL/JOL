<div class="card shadow mb-2">
  <div class="card-header bg-body-secondary">
    <h5>Create Game</h5>
  </div>
  <div class="card-body">
    <label for="newGameName" class="form-label">Name</label>
    <input type="text" class="form-control" id="newGameName" name="newGameName" maxlength="60" placeholder="Game name cannot &quot; or &lsquo; characters"/>
    <div class="form-check mt-2">
      <input class="form-check-input" type="checkbox" name="publicFlag" id="publicFlag">
      <label class="form-check-label" for="publicFlag">Public Game</label>
    </div>
    <button class="btn btn-outline-secondary btn-sm" onclick="doCreateGame()">Create Game</button>
  </div>
</div>