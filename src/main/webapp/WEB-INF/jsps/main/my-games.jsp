<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary p-0">
        <ul class="nav nav-tabs card-header-tabs ms-0 border-0" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active px-3 py-2" id="activeTab" data-bs-toggle="tab"
                        data-bs-target="#myGamesPane" type="button" role="tab">
                    Active <span class="badge rounded-pill bg-secondary ms-1" id="myGames-count">0</span>
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link px-3 py-2" id="oustedTab" data-bs-toggle="tab"
                        data-bs-target="#oustedGamesPane" type="button" role="tab">
                    Ousted <span class="badge rounded-pill bg-secondary ms-1" id="oustedGames-count">0</span>
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content tab-content-fill">
        <div class="tab-pane fade show active" id="myGamesPane" role="tabpanel">
            <ul class="list-group list-group-flush" id="myGames"></ul>
        </div>
        <div class="tab-pane fade" id="oustedGamesPane" role="tabpanel">
            <ul class="list-group list-group-flush" id="oustedGames"></ul>
        </div>
    </div>
</div>
