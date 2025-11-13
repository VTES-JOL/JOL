<div class="card shadow">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-baseline">
        <h5>Active games:</h5>
        <div class="form-check form-switch">
            <input class="form-check-input" type="checkbox" role="switch" id="myGamesDetailedMode"
                   onchange="toggleDetailedMode(this);">
            <label class="form-check-label" for="myGamesDetailedMode">Details</label>
        </div>
    </div>
    <ul class="list-group list-group-flush" id="myGames">
    </ul>
</div>