<!-- Jol Statistics Sub-Tabs -->
<ul class="nav nav-tabs mt-3" id="jolMonthlyStatsSubTabs">
    <li class="nav-item">
        <button class="nav-link active"
                id="jolMonthlyTab"
                data-bs-toggle="tab"
                data-bs-target="#jolMonthlyPane"
                type="button">
            Monthly
        </button>
    </li>
    <li class="nav-item">
        <button class="nav-link"
                id="jolMetricsTab"
                onclick="renderStats()"
                data-bs-toggle="tab"
                data-bs-target="#jolMetricsPane"
                type="button">
            Metrics & Command
        </button>
    </li>
    <li class="nav-item">
        <button class="nav-link"
                id="jolReactionTab"
                onclick="renderStats()"
                data-bs-toggle="tab"
                data-bs-target="#jolReactionPane"
                type="button">
            Reaction
        </button>
    </li>
</ul>

<div class="tab-content">
    <div class="tab-pane fade show active" id="jolMonthlyPane">
        <div class="overflow-auto pb-3" style="height:78vh;">
            <!-- Chart Accordion -->
            <div class="accordion mb-3" id="jolChartAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolChartHeading">
                        <button class="accordion-button"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#jolChartCollapse"
                                aria-expanded="true"
                                aria-controls="jolChartCollapse">
                            Chart
                        </button>
                    </h2>
                    <div id="jolChartCollapse" class="accordion-collapse collapse show"
                         aria-labelledby="jolChartHeading">
                        <div class="accordion-body">
                            <div style="height: 350px;">
                                <canvas id="jolChart"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="accordion" id="jolTableAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolTableHeading">
                        <button class="accordion-button"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#jolTableCollapse"
                                aria-expanded="true"
                                aria-controls="jolTableCollapse">
                            Table
                        </button>
                    </h2>
                    <div id="jolTableCollapse"
                         class="accordion-collapse collapse show"
                         aria-labelledby="jolTableHeading">

                        <div class="accordion-body p-0">
                            <table id="statsJolGames" class="table table-bordered table-sm mb-0">
                                <thead>
                                <tr>
                                    <th class="sticky-top bg-white">Month
                                        <input type="text" id="monthFilter"
                                               oninput="filterName('#statsJolGames tbody tr', 'monthFilter', 1)">
                                        <i class="bi bi-filter" onclick="sortTable(0, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Games Started
                                        <i class="bi bi-filter" onclick="sortTable(1, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Games Ended
                                        <i class="bi bi-filter" onclick="sortTable(2, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Net
                                        <i class="bi bi-filter" onclick="sortTable(3, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Wins
                                        <i class="bi bi-filter" onclick="sortTable(4, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Win Rate
                                        <i class="bi bi-filter" onclick="sortTable(5, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Vp
                                        <i class="bi bi-filter" onclick="sortTable(6, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Avg Vp
                                        <i class="bi bi-filter" onclick="sortTable(7, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Avg Duration
                                        <i class="bi bi-filter" onclick="sortTable(8, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Player of the Month
                                        <i class="bi bi-filter" onclick="sortTable(9, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white w-25">Deck of the Month
                                        <i class="bi bi-filter" onclick="sortTable(10, 'statsJolGames')"></i></th>
                                    <th class="sticky-top bg-white">Nation of the Month
                                        <i class="bi bi-filter" onclick="sortTable(11, 'statsJolGames')"></i></th>
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

    <div class="tab-pane fade" id="jolMetricsPane">
        <div class="overflow-auto pb-3" style="height:78vh;">
            <!-- Player Metrics Accordion -->
            <div class="accordion mb-3" id="jolPlayerMetricsAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolPlayerMetricsHeading">
                        <button class="accordion-button collapsed"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#jolPlayerMetricsCollapse"
                                aria-expanded="false"
                                aria-controls="jolPlayerMetricsCollapse">
                            Player Metrics
                        </button>
                    </h2>
                    <div id="jolPlayerMetricsCollapse" class="accordion-collapse collapse"
                         data-bs-parent="#jolMetricsPane"
                         aria-labelledby="jolPlayerMetricsHeading">
                        <div class="accordion-body p-0">
                            <div class="overflow-auto" style="max-height:100vh;">
                                <table id="playerMetrics" class="table table-sm mb-0">
                                    <thead>
                                    <tr>
                                        <th class="sticky-top bg-white">Player
                                            <input type="text" id="playerMetricsFilter"
                                                   oninput="filterName('#playerMetrics tbody tr', 'playerMetricsFilter', 1)">
                                            <i class="bi bi-filter" onclick="sortTable(0, 'playerMetrics')"></i></th>
                                        <th class="sticky-top bg-white">All
                                            <i class="bi bi-filter" onclick="sortTable(1, 'playerMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Chat
                                            <i class="bi bi-filter" onclick="sortTable(2, 'playerMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Command
                                            <i class="bi bi-filter" onclick="sortTable(3, 'playerMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Chat & Command
                                            <i class="bi bi-filter" onclick="sortTable(4, 'playerMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Ping
                                            <i class="bi bi-filter" onclick="sortTable(5, 'playerMetrics')"></i>
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
            <!-- Games Metrics -->
            <div class="accordion mb-3" id="jolGamesMetricsAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolGamesMetricsHeading">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                data-bs-target="#jolGamesMetricsCollapse" aria-expanded="false"
                                aria-controls="jolGamesMetricsCollapse"> Games Metrics
                        </button>
                    </h2>
                    <div id="jolGamesMetricsCollapse" class="accordion-collapse collapse"
                         data-bs-parent="#jolMetricsPane"
                         aria-labelledby="jolGamesMetricsHeading">
                        <div class="accordion-body p-0">
                            <div class="overflow-auto" style="max-height:100vh;">
                                <table id="gamesMetrics" class="table table-sm mb-0">
                                    <thead>
                                    <tr>
                                        <th class="sticky-top bg-white">Game
                                            <input type="text" id="gameMetricsFilter"
                                                   oninput="filterName('#gamesMetrics tbody tr', 'gameMetricsFilter', 1)">
                                            <i class="bi bi-filter" onclick="sortTable(0, 'gamesMetrics')"></i></th>
                                        <th class="sticky-top bg-white">All
                                            <i class="bi bi-filter" onclick="sortTable(1, 'gamesMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Chat
                                            <i class="bi bi-filter" onclick="sortTable(2, 'gamesMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Command
                                            <i class="bi bi-filter" onclick="sortTable(3, 'gamesMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Chat & Command
                                            <i class="bi bi-filter" onclick="sortTable(4, 'gamesMetrics')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Ping
                                            <i class="bi bi-filter" onclick="sortTable(5, 'playerMetrics')"></i>
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

            <!-- Player Commands -->
            <div class="accordion mb-3" id="jolPlayerCommandsAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolPlayerCommandsHeading">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                data-bs-target="#jolPlayerCommandsCollapse" aria-expanded="false"
                                aria-controls="jolPlayerCommandsCollapse"> Player Commands
                        </button>
                    </h2>
                    <div id="jolPlayerCommandsCollapse" class="accordion-collapse collapse"
                         data-bs-parent="#jolMetricsPane"
                         aria-labelledby="jolPlayerCommandsHeading">
                        <div class="accordion-body p-0">
                            <div class="overflow-auto" style="max-height:100vh;">
                                <table id="playerCommands" class="table table-sm mb-0">
                                    <thead>
                                    <tr>
                                        <th class="sticky-top bg-white w-25">Player
                                            <input type="text" id="playerCommandFilter"
                                                   oninput="filterName('#playerCommands tbody tr', 'playerCommandFilter', 1)">
                                            <i class="bi bi-filter" onclick="sortTable(0, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Timeout
                                            <i class="bi bi-filter" onclick="sortTable(1, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">VP
                                            <i class="bi bi-filter" onclick="sortTable(2, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Choose
                                            <i class="bi bi-filter" onclick="sortTable(3, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Reveal
                                            <i class="bi bi-filter" onclick="sortTable(4, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Label
                                            <i class="bi bi-filter" onclick="sortTable(5, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Votes
                                            <i class="bi bi-filter" onclick="sortTable(6, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Random
                                            <i class="bi bi-filter" onclick="sortTable(7, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Flip
                                            <i class="bi bi-filter" onclick="sortTable(8, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Discard
                                            <i class="bi bi-filter" onclick="sortTable(9, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Draw
                                            <i class="bi bi-filter" onclick="sortTable(10, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Edge
                                            <i class="bi bi-filter" onclick="sortTable(11, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Play
                                            <i class="bi bi-filter" onclick="sortTable(12, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Influence
                                            <i class="bi bi-filter" onclick="sortTable(13, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Move
                                            <i class="bi bi-filter" onclick="sortTable(14, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Burn
                                            <i class="bi bi-filter" onclick="sortTable(15, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Pool
                                            <i class="bi bi-filter" onclick="sortTable(16, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Blood
                                            <i class="bi bi-filter" onclick="sortTable(17, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Contest
                                            <i class="bi bi-filter" onclick="sortTable(18, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Disc
                                            <i class="bi bi-filter" onclick="sortTable(19, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Capacity
                                            <i class="bi bi-filter" onclick="sortTable(20, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Unlock
                                            <i class="bi bi-filter" onclick="sortTable(21, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Lock
                                            <i class="bi bi-filter" onclick="sortTable(22, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Order
                                            <i class="bi bi-filter" onclick="sortTable(23, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Show
                                            <i class="bi bi-filter" onclick="sortTable(24, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Shuffle
                                            <i class="bi bi-filter" onclick="sortTable(25, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Transfer
                                            <i class="bi bi-filter" onclick="sortTable(26, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Rfg
                                            <i class="bi bi-filter" onclick="sortTable(27, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Path
                                            <i class="bi bi-filter" onclick="sortTable(28, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Sect
                                            <i class="bi bi-filter" onclick="sortTable(29, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Clan
                                            <i class="bi bi-filter" onclick="sortTable(30, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Open
                                            <i class="bi bi-filter" onclick="sortTable(31, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Ping
                                            <i class="bi bi-filter" onclick="sortTable(32, 'playerCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">All
                                            <i class="bi bi-filter" onclick="sortTable(33, 'playerCommands')"></i>
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

            <!-- Game Commands -->
            <div class="accordion" id="jolGameCommandsAccordion">
                <div class="accordion-item">
                    <h2 class="accordion-header" id="jolGameCommandsHeading">
                        <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse"
                                data-bs-target="#jolGameCommandsCollapse" aria-expanded="false"
                                aria-controls="jolGameCommandsCollapse"> Game Commands
                        </button>
                    </h2>
                    <div id="jolGameCommandsCollapse" class="accordion-collapse collapse"
                         data-bs-parent="#jolMetricsPane"
                         aria-labelledby="jolGameCommandsHeading">
                        <div class="accordion-body p-0">
                            <div class="overflow-auto" style="max-height:100vh;">
                                <table id="gameCommands" class="table table-sm mb-0">
                                    <thead>
                                    <tr>
                                        <th class="sticky-top bg-white">Game
                                            <input type="text" id="gameCommandFilter"
                                                   oninput="filterName('#gameCommands tbody tr', 'gameCommandFilter', 1)">
                                            <i class="bi bi-filter" onclick="sortTable(0, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Timeout
                                            <i class="bi bi-filter" onclick="sortTable(1, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">VP
                                            <i class="bi bi-filter" onclick="sortTable(2, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Choose
                                            <i class="bi bi-filter" onclick="sortTable(3, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Reveal
                                            <i class="bi bi-filter" onclick="sortTable(4, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Label
                                            <i class="bi bi-filter" onclick="sortTable(5, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Votes
                                            <i class="bi bi-filter" onclick="sortTable(6, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Random
                                            <i class="bi bi-filter" onclick="sortTable(7, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Flip
                                            <i class="bi bi-filter" onclick="sortTable(8, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Discard
                                            <i class="bi bi-filter" onclick="sortTable(9, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Draw
                                            <i class="bi bi-filter" onclick="sortTable(10, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Edge
                                            <i class="bi bi-filter" onclick="sortTable(11, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Play
                                            <i class="bi bi-filter" onclick="sortTable(12, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Influence
                                            <i class="bi bi-filter" onclick="sortTable(13, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Move
                                            <i class="bi bi-filter" onclick="sortTable(14, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Burn
                                            <i class="bi bi-filter" onclick="sortTable(15, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Pool
                                            <i class="bi bi-filter" onclick="sortTable(16, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Blood
                                            <i class="bi bi-filter" onclick="sortTable(17, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Contest
                                            <i class="bi bi-filter" onclick="sortTable(18, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Disc
                                            <i class="bi bi-filter" onclick="sortTable(19, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Capacity
                                            <i class="bi bi-filter" onclick="sortTable(20, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Unlock
                                            <i class="bi bi-filter" onclick="sortTable(21, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Lock
                                            <i class="bi bi-filter" onclick="sortTable(22, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Order
                                            <i class="bi bi-filter" onclick="sortTable(23, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Show
                                            <i class="bi bi-filter" onclick="sortTable(24, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Shuffle
                                            <i class="bi bi-filter" onclick="sortTable(25, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Transfer
                                            <i class="bi bi-filter" onclick="sortTable(26, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Rfg
                                            <i class="bi bi-filter" onclick="sortTable(27, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Path
                                            <i class="bi bi-filter" onclick="sortTable(28, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Sect
                                            <i class="bi bi-filter" onclick="sortTable(29, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Clan
                                            <i class="bi bi-filter" onclick="sortTable(30, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Open
                                            <i class="bi bi-filter" onclick="sortTable(31, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">Ping
                                            <i class="bi bi-filter" onclick="sortTable(32, 'gameCommands')"></i>
                                        </th>
                                        <th class="sticky-top bg-white">All
                                            <i class="bi bi-filter" onclick="sortTable(33, 'gameCommands')"></i>
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
    </div>

    <div class="tab-pane fade" id="jolReactionPane">
        <div class="overflow-auto pb-3" style="height:78vh;">
            <table id="statsJolReaction" class="table table-bordered table-sm mb-0">
                <thead>
                <tr>
                    <th class="sticky-top bg-white">Player
                        <input type="text" id="playerReactionFilter"
                               oninput="filterName('#statsJolReaction tbody tr', 'playerReactionFilter', 1)">
                        <i class="bi bi-filter" onclick="sortTable(0, 'statsJolReaction')"></i></th>
                    <th class="sticky-top bg-white">Avg Reaction Time
                        <i class="bi bi-filter" onclick="sortTableByDuration(1,'#statsJolReaction tbody')"></i></th>
                </tr>
                </thead>
                <tbody></tbody>
            </table>
        </div>
    </div>
</div>

