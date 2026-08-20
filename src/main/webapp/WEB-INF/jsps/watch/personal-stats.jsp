<!-- Personal Statistics Sub-Tabs -->
<ul class="nav nav-tabs mt-3" id="personalStatsSubTabs">
    <li class="nav-item">
        <button class="nav-link active"
                id="personalOverviewTab"
                data-bs-toggle="tab"
                data-bs-target="#personalOverviewPane"
                type="button">
            Opponent Performance
        </button>
    </li>
    <li class="nav-item">
        <button class="nav-link"
                id="personalOpponentTab"
                data-bs-toggle="tab"
                data-bs-target="#personalOpponentPane"
                type="button">
            Deck Performance
        </button>
    </li>
</ul>

<!-- Personal Statistics Sub-Tab Content -->
<div class="tab-content mt-3">
    <!-- Opponent -->
    <div class="tab-pane fade show active" id="personalOverviewPane">
        <div class="overflow-auto pb-3" style="height:73vh;">
            <table id="statsPersonalGames" class="table table-bordered table-sm mb-0">
                <thead>
                <tr>
                    <th class="sticky-top bg-white">Opponent
                        <input type="text" id="personalNameFilter"
                               oninput="filterName('#statsPersonalGames tbody tr', 'personalNameFilter', 0)">
                        <i class="bi bi-filter" onclick="sortTable(0, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Number of Games
                        <i class="bi bi-filter" onclick="sortTable(1, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Wins
                        <i class="bi bi-filter" onclick="sortTable(2, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Win Rate
                        <i class="bi bi-filter"
                           onclick="sortPercentageTable(3, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent Won
                        <i class="bi bi-filter" onclick="sortTable(4, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Win Rate Against Opponent
                        <i class="bi bi-filter"
                           onclick="sortPercentageTable(5, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Other player won
                        <i class="bi bi-filter" onclick="sortTable(6, 'statsPersonalGames')"></i>
                    </th>
                    <th class="sticky-top bg-white">Losses
                        <i class="bi bi-filter" onclick="sortTable(7, 'statsPersonalGames')"></i>
                    </th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>

    <!-- Deck Performance -->
    <div class="tab-pane fade" id="personalOpponentPane">
        <div class="overflow-auto pb-3" style="height:73vh;">
            <table id="statsPersonalDecks" class="table table-bordered table-sm mb-0">
                <thead>
                <tr>
                    <th class="sticky-top bg-white">Deck
                        <input type="text" id="personalDeckNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalDeckNameFilter', 1)">
                        <i class="bi bi-filter" onclick="sortTable(0, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent Deck
                        <input type="text" id="personalOpponentNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalOpponentNameFilter', 2)">
                        <i class="bi bi-filter" onclick="sortTable(1, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Game Names
                        <input type="text" id="personalGamesNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalGamesNameFilter', 3)">
                        <i class="bi bi-filter" onclick="sortTable(2, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Games
                        <i class="bi bi-filter" onclick="sortTable(3, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Wins
                        <i class="bi bi-filter" onclick="sortTable(4, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">VP
                        <i class="bi bi-filter" onclick="sortTable(5, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Avg VP
                        <i class="bi bi-filter" onclick="sortTable(6, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent VP
                        <i class="bi bi-filter" onclick="sortTable(7, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent Avg VP
                        <i class="bi bi-filter" onclick="sortTable(8, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">VP Difference
                        <i class="bi bi-filter" onclick="sortTable(9, 'statsPersonalDecks')"></i>
                    </th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</div>