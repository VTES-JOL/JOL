<div class="card shadow flex-fill d-flex flex-column min-h-0">
    <div class="card-header bg-body-secondary p-0">
        <ul class="nav nav-tabs card-header-tabs ms-0 border-0" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active px-3 py-2" data-bs-toggle="tab"
                        data-bs-target="#activeGamesPane" type="button" role="tab">
                    Active Games
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link px-3 py-2" data-bs-toggle="tab"
                        data-bs-target="#pastGamesPane" type="button" role="tab">
                    Past Games
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link px-3 py-2" data-bs-toggle="tab"
                        onclick="renderStats()"
                        data-bs-target="#statGamesPane" type="button" role="tab">
                    Statistics
                </button>
            </li>
            <li class="ms-auto d-flex align-items-center pe-2">
                <button class="btn btn-outline-secondary btn-sm d-none" id="exportCsvBtn"
                        onclick="exportCsv()">Export CSV <i class="bi-download"></i></button>
            </li>
        </ul>
    </div>
    <div class="tab-content tab-content-fill">
        <div class="tab-pane fade show active" id="activeGamesPane" role="tabpanel">
            <table id="activeGames" class="table table-sm table-hover mb-0">
                <thead>
                <tr>
                    <th>Game</th>
                    <th>Current Turn</th>
                    <th>Updated</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
        <div class="tab-pane fade" id="pastGamesPane" role="tabpanel">
            <table id="pastGames" class="table table-sm table-hover mb-0">
                <thead>
                <tr>
                    <th>Game</th>
                    <th>Started</th>
                    <th>Ended</th>
                    <th colspan="3">Results</th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
        <div class="tab-pane fade overflow-hidden" id="statGamesPane" role="tabpanel">
            <div class="container mt-3">
                <div class="row align-items-center g-2">
                    <div class="col-auto">
                        <label for="fromDate" class="form-label mb-0">From</label>
                    </div>
                    <div class="col-auto">
                        <input type="date" class="form-control" id="fromDate">
                    </div>

                    <div class="col-auto">
                        <label for="toDate" class="form-label mb-0">To</label>
                    </div>
                    <div class="col-auto">
                        <input type="date" class="form-control" id="toDate">
                    </div>

                    <div class="col-auto">
                        <button onclick="renderStats()" type="button" class="btn btn-outline-secondary btn-sm">Search
                        </button>
                    </div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-outline-secondary btn-sm"
                                onclick="renderStatsFor(new Date().getFullYear()-1 + '-01-01', new Date().getFullYear()-1 + '-12-31')">
                            Last Year
                        </button>
                    </div>
                    <div class="col-auto">
                        <button type="button" class="btn btn-outline-secondary btn-sm"
                                onclick="renderStatsFor(new Date().getFullYear() + '-01-01', new Date().getFullYear() + '-12-31')">
                            Current Year
                        </button>
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-outline-secondary btn-sm" title="Reset all filter"
                                onclick="resetStats()"><i class="bi-trash"></i></button>
                    </div>
                    <div class="form-check form-switch col-auto m-2 pt-1">
                        <input class="form-check-input" type="checkbox" role="switch" id="onlyTournaments" switch=""
                               onclick="renderStats()">
                        <label class="form-check-label" for="onlyTournaments">Only Tournaments</label>
                    </div>
                </div>
            </div>
            <ul class="nav nav-tabs mt-3">
                <li class="nav-item">
                    <button id="kpiStatsTab"
                            class="nav-link active"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#kpiStatsPane">
                        KPI's
                    </button>
                </li>
                <li class="nav-item">
                    <button id="playerStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#playerStatsPane">
                        Players
                    </button>
                </li>
                <li class="nav-item">
                    <button id="deckStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#deckStatsPane">
                        Decks
                    </button>
                </li>
                <li class="nav-item">
                    <button id="nationStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#nationStatsPane">
                        Nations
                    </button>
                </li>
                <li class="nav-item">
                    <button id="personalStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#personalStatsPane">
                        Personal
                    </button>
                </li>
                <li class="nav-item">
                    <button id="gameStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#gameStatsPane">
                        Games
                    </button>
                </li>
                <li class="nav-item">
                    <button id="jolStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#jolStatsPane">
                        Jol
                    </button>
                </li>
            </ul>

            <div class="tab-content mt-3">
                <!-- KPIs -->
                <div class="tab-pane fade show active" id="kpiStatsPane">
                    <jsp:include page="kpis.jsp"/>
                </div>
                <!-- Player Stats -->
                <div class="tab-pane fade" id="playerStatsPane">
                    <jsp:include page="player-stats.jsp"/>
                </div>
                <!-- Deck Stats -->
                <div class="tab-pane fade" id="deckStatsPane">
                    <jsp:include page="deck-stats.jsp"/>
                </div>
                <!-- Nation Stats -->
                <div class="tab-pane fade" id="nationStatsPane">
                    <jsp:include page="nation-stats.jsp"/>
                </div>
                <!-- Game Stats -->
                <div class="tab-pane fade" id="gameStatsPane">
                   <jsp:include page="game-stats.jsp"/>
                </div>
                <!-- Jol Stats -->
                <div class="tab-pane fade" id="jolStatsPane">
                    <jsp:include page="jol-stats.jsp"/>
                </div>
                <!-- Personal Stats -->
                <div class="tab-pane fade" id="personalStatsPane">
                    <jsp:include page="personal-stats.jsp"/>
                </div>
            </div>
        </div>
    </div>
</div>