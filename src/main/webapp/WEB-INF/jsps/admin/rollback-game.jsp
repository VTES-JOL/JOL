<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary">
        <h5>Rollback Game</h5>
    </div>
    <div class="card-body">
        <label for="rollbackGamesList" class="form-label">Games</label>
        <select id="rollbackGamesList" class="form-select" onchange="rollbackChangeGame()"></select>
        <label for="rollbackTurnsList" class="form-label">Turns</label>
        <select id="rollbackTurnsList" class="form-select">
        </select>
        <button onclick="rollbackGame()" class="btn btn-outline-secondary btn-sm mt-2">Rollback Game</button>
    </div>
</div>