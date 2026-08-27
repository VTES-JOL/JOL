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
                id="personalDeckTab"
                data-bs-toggle="tab"
                data-bs-target="#personalDeckPane"
                type="button">
            Deck Performance
        </button>
    </li>
    <li class="nav-item">
        <button class="nav-link"
                id="personalReactionTab"
                data-bs-toggle="tab"
                data-bs-target="#personalReactionPane"
                type="button">
            Reaction Performance
        </button>
    </li>
</ul>

<!-- Personal Statistics Sub-Tab Content -->
<div class="tab-content mt-3">
    <!-- Opponent -->
    <div class="tab-pane fade show active" id="personalOverviewPane">
        <div class="overflow-auto pb-3" style="height:73vh;">
            <!-- Chart Accordion -->
            <div class="accordion mb-3" id="personalChartAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="personalChartHeading">
                        <button class="accordion-button"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#personalChartCollapse"
                                aria-expanded="true"
                                aria-controls="personalChartCollapse">
                            Top 10
                        </button>
                    </h2>
                    <div id="personalChartCollapse" class="accordion-collapse collapse show"
                         aria-labelledby="personalChartHeading">
                        <div class="accordion-body">
                            <div style="height: 350px;">
                                <canvas id="personalChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="accordion" id="personalTableAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="personalTableHeading">
                        <button class="accordion-button"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#personalTableCollapse"
                                aria-expanded="true"
                                aria-controls="personalTableCollapse">
                            Table
                        </button>
                    </h2>
                    <div id="personalTableCollapse" class="accordion-collapse collapse show"
                         aria-labelledby="personalTableHeading">
                        <div class="accordion-body p-0">
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
                                    <th class="sticky-top bg-white">Win Rate Overall
                                        <i class="bi bi-filter"
                                           onclick="sortPercentageTable(3, 'statsPersonalGames')"></i>
                                    </th>
                                    <th class="sticky-top bg-white">Win Rate Against Opponent
                                        <i class="bi bi-filter"
                                           onclick="sortPercentageTable(4, 'statsPersonalGames')"></i>
                                    </th>
                                    <th class="sticky-top bg-white">Opponent Won
                                        <i class="bi bi-filter" onclick="sortTable(5, 'statsPersonalGames')"></i>
                                    </th>
                                    <th class="sticky-top bg-white">Win Rate Opponent Against You
                                        <i class="bi bi-filter"
                                           onclick="sortPercentageTable(6, 'statsPersonalGames')"></i>
                                    </th>
                                    <th class="sticky-top bg-white">Other player won
                                        <i class="bi bi-filter" onclick="sortTable(7, 'statsPersonalGames')"></i>
                                    </th>
                                    <th class="sticky-top bg-white">Losses
                                        <i class="bi bi-filter" onclick="sortTable(8, 'statsPersonalGames')"></i>
                                    </th>
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
    <!-- Deck -->
    <div class="tab-pane fade" id="personalDeckPane">
        <div class="overflow-auto pb-3" style="height:73vh;">
            <table id="statsPersonalDecks" class="table table-bordered table-sm mb-0">
                <thead>
                <tr>
                    <th class="sticky-top bg-white">Deck
                        <input type="text" id="personalDeckNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalDeckNameFilter', 1)">
                        <i class="bi bi-filter"
                           onclick="sortTable(0, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent Deck
                        <input type="text" id="personalOpponentNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalOpponentNameFilter', 2)">
                        <i class="bi bi-filter"
                           onclick="sortTable(1, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Game Names
                        <input type="text" id="personalGamesNameFilter"
                               oninput="filterName('#statsPersonalDecks tbody tr', 'personalGamesNameFilter', 3)">
                        <i class="bi bi-filter"
                           onclick="sortTable(2, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Games
                        <i class="bi bi-filter"
                           onclick="sortTable(3, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Wins
                        <i class="bi bi-filter"
                           onclick="sortTable(4, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">VP
                        <i class="bi bi-filter"
                           onclick="sortTable(5, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Avg VP
                        <i class="bi bi-filter"
                           onclick="sortTable(6, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent VP
                        <i class="bi bi-filter"
                           onclick="sortTable(7, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Opponent Avg VP
                        <i class="bi bi-filter"
                           onclick="sortTable(8, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">VP Difference
                        <i class="bi bi-filter"
                           onclick="sortTable(9, 'statsPersonalDecks')"></i>
                    </th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
    <!-- Reaction -->
    <div class="tab-pane fade" id="personalReactionPane">
        <div class="overflow-auto pb-3" style="height:73vh;">
            <table id="statsPersonalReaction" class="table table-bordered table-sm mb-0">
                <thead>
                <tr>
                    <th class="sticky-top bg-white">Game
                        <input type="text" id="personalGameFilter"
                               oninput="filterName('#statsPersonalReaction tbody tr', 'personalGameFilter', 1)">
                        <i class="bi bi-filter"
                           onclick="sortTable(0, 'statsPersonalReaction')"></i>
                    </th>
                    <th class="sticky-top bg-white">From
                        <i class="bi bi-filter"
                           onclick="sortTable(1, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Command
                        <i class="bi bi-filter"
                           onclick="sortTable(2, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Response
                        <i class="bi bi-filter"
                           onclick="sortTable(3, 'statsPersonalDecks')"></i>
                    </th>
                    <th class="sticky-top bg-white">Reaction
                        <i class="bi bi-filter"
                           onclick="sortTable(4, 'statsPersonalDecks')"></i>
                    </th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</div>
