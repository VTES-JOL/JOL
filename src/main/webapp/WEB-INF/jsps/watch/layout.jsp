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
                        <button class="btn btn-outline-secondary btn-sm" title="Reset all filter" onclick="resetStats()"><i class="bi-trash"></i></button>
                    </div>
                    <div class="form-check form-switch col-auto m-2 pt-1">
                        <input class="form-check-input" type="checkbox" role="switch" id="onlyTournaments" switch="" onclick="renderStats()">
                        <label class="form-check-label" for="onlyTournaments">Only Tournaments</label>
                    </div>
                </div>
            </div>
            <ul class="nav nav-tabs mt-3">
                <li class="nav-item">
                    <button id="playerStatsTab"
                            class="nav-link active"
                            data-bs-toggle="tab"
                            data-bs-target="#playerStatsPane">
                        Player Statistics
                    </button>
                </li>

                <li class="nav-item">
                    <button id="deckStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#deckStatsPane">
                        Deck Statistics
                    </button>
                </li>
                <li class="nav-item">
                    <button id="nationStatsTab"
                            class="nav-link"
                            onclick="renderStats()"
                            data-bs-toggle="tab"
                            data-bs-target="#nationStatsPane">
                        Nation Statistics
                    </button>
                </li>
            </ul>

            <div class="tab-content mt-3">

                <!-- Player Stats -->

                <div class="tab-pane fade show active"
                     id="playerStatsPane">

                    <div class="overflow-auto" style="max-height:100vh;">
                        <table id="statsGames" class="table table-sm mb-0">
                            <thead>
                            <tr>
                                <th class="sticky-top bg-white">Player
                                    <input type="text" id="playerNameFilter"
                                           oninput="filterName('#statsGames tbody tr', 'playerNameFilter', 1)">
                                    <i class="bi bi-filter" onclick="sortTable(0, 'statsGames')"></i></th>
                                <th class="sticky-top bg-white">Number of Games
                                    <input type="number" id="gameThreshold" min="0" value="0"
                                           oninput="renderStats()" style="width: 45px; height: 25px;">
                                    <i class="bi bi-filter" onclick="sortTable(1, 'statsGames')"></i>
                                </th>
                                <th class="sticky-top bg-white">GW Total <i class="bi bi-filter"
                                                                            onclick="sortTable(2, 'statsGames')"></i></th>
                                <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                                            onclick="sortTable(3, 'statsGames')"></i></th>
                                <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                                              onclick="sortPercentageTable(4, 'statsGames')"></i></th>
                                <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                                              onclick="sortTable(5), 'statsGames'"></i></th>
                            </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>

                </div>

                <!-- Deck Stats -->

                <div class="tab-pane fade"
                     id="deckStatsPane">

                    <div class="overflow-auto" style="max-height:100vh;">
                        <table id="statsDeckGames" class="table table-sm mb-0">
                            <thead>
                            <tr>
                                <th class="sticky-top bg-white">Deck / Player
                                    <input type="text" id="deckNameFilter"
                                           oninput="filterName('#statsDeckGames tbody tr', 'deckNameFilter', 1)">
                                    <i class="bi bi-filter" onclick="sortTable(0, 'statsDeckGames')"></i></th>
                                <th class="sticky-top bg-white">Number of Games
                                    <input type="number" id="gameThresholdDeck" min="0" value="0"
                                           oninput="renderStats()" style="width: 45px; height: 25px;">
                                    <i class="bi bi-filter" onclick="sortTable(1, 'statsDeckGames')"></i>
                                </th>
                                <th class="sticky-top bg-white">GW Total <i class="bi bi-filter"
                                                                            onclick="sortTable(2, 'statsDeckGames')"></i></th>
                                <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                                            onclick="sortTable(3, 'statsDeckGames')"></i></th>
                                <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                                              onclick="sortPercentageTable(4, 'statsDeckGames')"></i></th>
                                <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                                              onclick="sortTable(5, 'statsDeckGames')"></i></th>
                            </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>

                </div>

                <!-- Nation Stats -->

                <div class="tab-pane fade"
                     id="nationStatsPane">

                    <div class="overflow-auto" style="max-height:100vh;">
                        <table id="statsNationGames" class="table table-sm mb-0">
                            <thead>
                            <tr>
                                <th class="sticky-top bg-white">Nation
                                    <input type="text" id="nationNameFilter"
                                           oninput="filterName('#statsNationGames tbody tr', 'nationNameFilter', 1)">
                                    <i class="bi bi-filter" onclick="sortTable(0, 'statsNationGames')"></i></th>
                                <th class="sticky-top bg-white">Number of Games
                                    <input type="number" id="gameThresholdNation" min="0" value="0"
                                           oninput="renderStats()" style="width: 45px; height: 25px;">
                                    <i class="bi bi-filter" onclick="sortTable(1, 'statsNationGames')"></i>
                                </th>
                                <th class="sticky-top bg-white">GW Total <i class="bi bi-filter"
                                                                            onclick="sortTable(2, 'statsNationGames')"></i></th>
                                <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                                            onclick="sortTable(3, 'statsNationGames')"></i></th>
                                <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                                              onclick="sortPercentageTable(4, 'statsNationGames')"></i></th>
                                <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                                              onclick="sortTable(5, 'statsNationGames')"></i></th>
                            </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>

                </div>

            </div>

        </div>
    </div>
</div>
</div>
