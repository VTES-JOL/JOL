<div class="card shadow mb-2">
    <div class="card-header bg-body-secondary">
        <h5>Create Game</h5>
    </div>
    <div class="card-body">
        <div class="mb-1">
            <label for="newGameName" class="form-label">Name</label>
            <input type="text" class="form-control" id="newGameName" name="newGameName" maxlength="60"
                   placeholder="Game name cannot &quot; or &lsquo; characters"/>
        </div>
        <div class="mb-1">
            <label class="form-label" for="publicFlag">Visibility</label>
            <select name="publicFlag" class="form-select" id="publicFlag">
                <option value="PRIVATE">Private</option>
                <option value="PUBLIC">Public</option>
            </select>
        </div>
        <div class="mb-1">
            <label for="gameFormat" class="form-label">Format</label>
            <select name="gameFormat" id="gameFormat" class="form-select">
                <option value="STANDARD">Standard</option>
                <option value="V5">V5</option>
                <option value="JYHAD">Jyhad</option>
                <option value="DUEL">Duel</option>
            </select>
        </div>
        <button class="btn btn-outline-secondary btn-sm mt-2" onclick="doCreateGame()">Create Game</button>
    </div>
</div>