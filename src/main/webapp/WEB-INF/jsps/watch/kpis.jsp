<div id="loadingScreen" class="loading-screen d-none">
    <span class="spinner-border spinner-border-sm"></span>
    <span>Loading...</span>
</div>
<div id = "kpiContainer" class="overflow-auto pb-3" style="height:78vh;">
    <div class="row g-4 p-3">
        <!-- CURRENT MONTH -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Current Month</h5>
                    <small class="text-body-secondary" id="currentMonthLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <!-- Active Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Active Games</div>
                            <div class="fs-3 fw-semibold" id="currentMonthActiveGames"></div>
                            <div class="small text-danger" id="currentMonthActiveGamesChange">
                            </div>
                            <i class="bi bi-controller text-primary"></i>
                        </div>

                        <!-- Active Tournament Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Tournament Games</div>
                            <div class="fs-3 fw-semibold" id="currentMonthActiveTournamentGames"></div>
                            <div class="small text-danger" id="currentMonthActiveTournamentGamesChange">
                            </div>
                            <i class="bi bi-trophy-fill text-warning"></i>
                        </div>

                        <!-- Past Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Past Games</div>
                            <div class="fs-3 fw-semibold" id="currentMonthPastGames"></div>
                            <i class="bi bi-controller text-secondary"></i>
                        </div>

                        <!-- Past Tournament Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Past Tournament Games</div>
                            <div class="fs-3 fw-semibold" id="currentMonthPastTournamentGames"></div>
                            <i class="bi bi-trophy-fill text-secondary"></i>
                        </div>
                    </div>

                    <hr class="my-4">

                    <div class="row g-4">

                        <!-- TOP 3 PLAYERS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Players</h6>

                            <div class="list-group list-group-flush"
                                 id="currentMonthTop">
                            </div>
                        </div>

                        <!-- TOP 3 DEKCS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Decks</h6>

                            <div class="list-group list-group-flush"
                                 id="currentMonthTopDecks">
                            </div>
                        </div>

                        <!-- TOP 3 NATIONS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Nations</h6>

                            <div class="list-group list-group-flush"
                                 id="currentMonthTopNations">
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <!-- LAST MONTH -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Last Month</h5>
                    <small class="text-body-secondary" id="lastMonthLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <!-- Active Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Active Games</div>
                            <div class="fs-3 fw-semibold" id="lastMonthActiveGames"></div>
                            <div class="small text-danger" id="lastMonthActiveGamesChange">
                            </div>
                            <i class="bi bi-controller text-primary"></i>
                        </div>

                        <!-- Active Tournament Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Tournament Games</div>
                            <div class="fs-3 fw-semibold" id="lastMonthActiveTournamentGames"></div>
                            <div class="small text-danger" id="lastMonthActiveTournamentGamesChange">
                            </div>
                            <i class="bi bi-trophy-fill text-warning"></i>
                        </div>
                    </div>

                    <hr class="my-4">

                    <div class="row g-4">

                        <!-- TOP 3 PLAYERS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Players</h6>

                            <div class="list-group list-group-flush"
                                 id="lastMonthTop">
                            </div>
                        </div>

                        <!-- TOP 3 DECKS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Decks</h6>

                            <div class="list-group list-group-flush"
                                 id="lastMonthTopDecks">
                            </div>
                        </div>

                        <!-- TOP 3 NATIONS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Nations</h6>

                            <div class="list-group list-group-flush"
                                 id="lastMonthTopNations">
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <!-- BEFORE LAST MONTH -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Month Before Last</h5>
                    <small class="text-body-secondary" id="lastMonthLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <!-- Active Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Active Games</div>
                            <div class="fs-3 fw-semibold" id="beforeLastMonthActiveGames"></div>
                            <div class="small text-danger" id="beforeLastMonthActiveGamesChange">
                            </div>
                            <i class="bi bi-controller text-primary"></i>
                        </div>

                        <!-- Active Tournament Games -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Total Tournament Games</div>
                            <div class="fs-3 fw-semibold" id="beforeLastMonthActiveTournamentGames"></div>
                            <div class="small text-danger" id="beforeLastMonthActiveTournamentGamesChange">
                            </div>
                            <i class="bi bi-trophy-fill text-warning"></i>
                        </div>
                    </div>

                    <hr class="my-4">

                    <div class="row g-4">

                        <!-- TOP 3 PLAYERS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Players</h6>

                            <div class="list-group list-group-flush"
                                 id="beforeLastMonthTop">
                            </div>
                        </div>

                        <!-- TOP 3 DECKS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Decks</h6>

                            <div class="list-group list-group-flush"
                                 id="beforeLastMonthTopDecks">
                            </div>
                        </div>

                        <!-- TOP 3 NATIONS -->
                        <div class="col-4">
                            <h6 class="mb-3">Top 3 Nations</h6>

                            <div class="list-group list-group-flush"
                                 id="beforeLastMonthTopNations">
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row g-4 p-3">
        <!-- MOST ACTIVE GAMES -->
        <div class="col-12 col-md-3">
            <div class="card stats-card">
                <div class="card-header">
                    <div class="fs-4 text-dark">MOST ACTIVE GAMES</div>
                </div>
                <div class="card-body d-flex justify-content-between">
                    <div>
                        <div class="fs-2 text-dark" id="gamesByPlayerKpi"></div>
                        <div class="mt-3">
                            <span id="highestActiveLastMonthKpi"></span>
                            &nbsp; highest last month
                        </div>
                    </div>

                    <div class="text-primary fs-1">
                        <i class="bi bi-bar-chart-line-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <!-- TOURNAMET -->
        <div class="col-12 col-md-3">
            <div class="card stats-card">
                <div class="card-header">
                    <div class="fs-4 text-dark">MOST TOURNAMENT GAMES</div>
                </div>
                <div class="card-body d-flex justify-content-between">
                    <div>
                        <div class="fs-2 text-dark" id="tournamentsByPlayerKpi"></div>
                        <div class="mt-3">
                            <span id="highestTournamentLastMonthKpi"></span>
                            &nbsp; highest last month
                        </div>
                    </div>

                    <div class="text-primary fs-1">
                        <i class="bi bi-trophy-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <!-- OUSTED -->
        <div class="col-12 col-md-3">
            <div class="card stats-card">
                <div class="card-header">
                    <div class="fs-4 text-dark">MOST OUSTED GAMES</div>
                </div>
                <div class="card-body d-flex justify-content-between">
                    <div>
                        <div class="fs-2 text-dark" id="oustedByPlayerKpi"></div>
                    </div>

                    <div class="text-primary fs-1">
                        <i class="bi bi-0-circle-fill"></i>
                    </div>
                </div>
            </div>
        </div>
        <!-- DECK -->
        <div class="col-12 col-md-3">
            <div class="card stats-card">
                <div class="card-header">
                    <div class="fs-4 text-dark">MOST DECKS</div>
                </div>
                <div class="card-body d-flex justify-content-between">
                    <div>
                        <div class="fs-2 text-dark" id="decksByPlayerKpi"></div>
                    </div>

                    <div class="text-primary fs-1">
                        <i class="bi bi-stack"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row g-3 p-3">
        <!-- CURRENT MONTH -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Current Month</h5>
                    <small class="text-body-secondary" id="currentMonthLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <!-- Total Activity -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Total Activity</div>
                            <div class="fs-3 fw-semibold" id="currentMonthTotalActivity"></div>
                            <i class="bi bi-activity text-primary"></i>
                        </div>

                        <!-- Chat / Command -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Chat / Command</div>
                            <div class="d-flex flex-column">
                                <div>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span class="fw-semibold" id="currentMonthChat"></span>
                                </div>

                                <div>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span class="fw-semibold" id="currentMonthCommand"></span>
                                </div>
                            </div>
                        </div>

                        <!-- Unique Users -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Unique Users</div>
                            <div class="fs-3 fw-semibold" id="currentMonthUniqueUsers"></div>
                            <i class="bi bi-people-fill text-success"></i>
                        </div>

                        <!-- Most Active Day -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Day</div>
                            <div class="fs-5 fw-semibold" id="currentMonthMostActiveDay"></div>
                            <div class="small text-body-secondary">
                                <span id="currentMonthMostActiveDayEvents"></span> events
                            </div>
                            <i class="bi bi-calendar-event text-warning"></i>
                        </div>

                        <!-- Most Active Hour -->
                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Hour</div>
                            <div class="fs-5 fw-semibold" id="currentMonthMostActiveHour"></div>
                            <div class="small text-body-secondary">
                                <span id="currentMonthMostActiveHourEvents"></span> events
                            </div>
                            <i class="bi bi-clock-fill text-danger"></i>
                        </div>

                    </div>

                    <hr class="my-4">

                    <h6 class="mb-3">Top 10 Players</h6>

                    <div class="list-group list-group-flush"
                         id="currentMonthTopPlayers">
                    </div>
                </div>
            </div>
        </div>


        <!-- LAST MONTH -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Last Month</h5>
                    <small class="text-body-secondary" id="lastMonthLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <div class="col-4">
                            <div class="text-body-secondary small">Total Activity</div>
                            <div class="fs-3 fw-semibold" id="lastMonthTotalActivity"></div>
                            <i class="bi bi-activity text-primary"></i>
                        </div>

                        <!-- Chat / Command -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Chat / Command</div>
                            <div class="d-flex flex-column">
                                <div>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span class="fw-semibold" id="lastMonthChat"></span>
                                </div>

                                <div>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span class="fw-semibold" id="lastMonthCommand"></span>
                                </div>
                            </div>
                        </div>

                        <div class="col-4">
                            <div class="text-body-secondary small">Unique Users</div>
                            <div class="fs-3 fw-semibold" id="lastMonthUniqueUsers"></div>
                            <i class="bi bi-people-fill text-success"></i>
                        </div>

                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Day</div>
                            <div class="fs-5 fw-semibold" id="lastMonthMostActiveDay"></div>
                            <div class="small text-body-secondary">
                                <span id="lastMonthMostActiveDayEvents"></span>
                            </div>
                            <i class="bi bi-calendar-event text-warning"></i>
                        </div>

                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Hour</div>
                            <div class="fs-5 fw-semibold" id="lastMonthMostActiveHour"></div>
                            <div class="small text-body-secondary">
                                <span id="lastMonthMostActiveHourEvents"></span>
                            </div>
                            <i class="bi bi-clock-fill text-danger"></i>
                        </div>

                    </div>

                    <hr class="my-4">

                    <h6 class="mb-3">Top 10 Players</h6>

                    <div class="list-group list-group-flush"
                         id="lastMonthTopPlayers">
                    </div>
                </div>
            </div>
        </div>


        <!-- MONTH BEFORE LAST -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Month Before Last</h5>
                    <small class="text-body-secondary" id="monthBeforeLastLabel"></small>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <div class="col-4">
                            <div class="text-body-secondary small">Total Activity</div>
                            <div class="fs-3 fw-semibold" id="monthBeforeLastTotalActivity"></div>
                            <i class="bi bi-activity text-primary"></i>
                        </div>

                        <!-- Chat / Command -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Chat / Command</div>
                            <div class="d-flex flex-column">
                                <div>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span class="fw-semibold" id="monthBeforeLastChat"></span>
                                </div>

                                <div>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span class="fw-semibold" id="monthBeforeLastCommand"></span>
                                </div>
                            </div>
                        </div>

                        <div class="col-4">
                            <div class="text-body-secondary small">Unique Users</div>
                            <div class="fs-3 fw-semibold" id="monthBeforeLastUniqueUsers"></div>
                            <i class="bi bi-people-fill text-success"></i>
                        </div>

                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Day</div>
                            <div class="fs-5 fw-semibold" id="monthBeforeLastMostActiveDay"></div>
                            <div class="small text-body-secondary">
                                <span id="monthBeforeLastMostActiveDayEvents"></span> events
                            </div>
                            <i class="bi bi-calendar-event text-warning"></i>
                        </div>

                        <div class="col-6">
                            <div class="text-body-secondary small">Most Active Hour</div>
                            <div class="fs-5 fw-semibold" id="monthBeforeLastMostActiveHour"></div>
                            <div class="small text-body-secondary">
                                <span id="monthBeforeLastMostActiveHourEvents"></span>
                            </div>
                            <i class="bi bi-clock-fill text-danger"></i>
                        </div>

                    </div>

                    <hr class="my-4">

                    <h6 class="mb-3">Top 10 Players</h6>

                    <div class="list-group list-group-flush"
                         id="monthBeforeLastTopPlayers">
                    </div>
                </div>
            </div>
        </div>

    </div>
    <div class="row g-3 p-3">
        <!-- OVERALL -->
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Overall Overview</h5>
                </div>

                <div class="card-body">
                    <div class="row g-4">

                        <!-- Total Activity -->
                        <div class="col-4">
                            <div class="text-body-secondary small">
                                Total Activity
                            </div>
                            <div class="fs-3 fw-semibold"
                                 id="overallTotalActivity"></div>
                            <i class="bi bi-activity text-primary"></i>
                        </div>

                        <!-- Chat / Command -->
                        <div class="col-4">
                            <div class="text-body-secondary small">Chat / Command</div>
                            <div class="d-flex flex-column">
                                <div>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span class="fw-semibold" id="overallChat"></span>
                                </div>

                                <div>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span class="fw-semibold" id="overallCommand"></span>
                                </div>
                            </div>
                        </div>

                        <!-- Unique Users -->
                        <div class="col-4">
                            <div class="text-body-secondary small">
                                Unique Users
                            </div>
                            <div class="fs-3 fw-semibold"
                                 id="overallUniqueUsers"></div>
                            <i class="bi bi-people-fill text-success"></i>
                        </div>

                        <!-- Most Active Player -->
                        <div class="col-12">
                            <div class="text-body-secondary small">
                                Most Active Player
                            </div>

                            <div class="fs-5 fw-semibold"
                                 id="overallMostActivePlayer"></div>

                            <div class="small text-body-secondary">
                                <span id="overallMostActivePlayerEvents"></span>
                                events
                            </div>

                            <i class="bi bi-person-fill text-warning"></i>
                        </div>

                        <!-- Most Active Game -->
                        <div class="col-12">
                            <div class="text-body-secondary small">
                                Most Active Game
                            </div>

                            <div class="fs-5 fw-semibold"
                                 id="overallMostActiveGame"></div>

                            <div class="small text-body-secondary d-flex align-items-center gap-2">
                                <span>
                                    <i class="bi bi-activity text-primary me-1"></i>
                                    <span id="overallMostActiveGameEvents"></span> events
                                </span>
                                <span>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span id="overallMostActiveGameChat"></span>
                                </span>
                                <span>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span id="overallMostActiveGameCommand"></span>
                                </span>
                            </div>
                            <i class="bi bi-controller text-primary"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Peak Activity Days</h5>
                </div>

                <div class="card-body">
                    <div class="list-group list-group-flush"
                         id="peakActivityDays">
                    </div>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-header">
                    <h5 class="mb-0">Peak Activity Hours</h5>
                </div>

                <div class="card-body">
                    <div class="list-group list-group-flush"
                         id="peakActivityHours">
                    </div>
                </div>
            </div>
        </div>

    </div>
    <div class="row g-3 p-3">
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">

                <div class="card-header py-2">
                    <h6 class="mb-0">Player Activity</h6>
                    <small class="text-body-secondary" id="playerActivityName"></small>
                </div>

                <div class="card-body py-2">

                    <!-- Games -->
                    <div class="row g-3 mb-2">

                        <!-- Total Activity -->
                        <div class="col-3">
                            <div class="text-body-secondary small">Total Activity</div>
                            <div class="fs-3 fw-semibold" id="playerTotalActivity"></div>
                            <i class="bi bi-activity text-primary"></i>
                        </div>

                        <!-- Chat / Command -->
                        <div class="col-3">
                            <div class="text-body-secondary small">Chat / Command</div>
                            <div class="d-flex flex-column">
                                <div>
                                    <i class="bi bi-chat-fill text-success me-1"></i>
                                    <span class="fw-semibold" id="playerChat"></span>
                                </div>

                                <div>
                                    <i class="bi bi-terminal-fill text-primary me-1"></i>
                                    <span class="fw-semibold" id="playerCommand"></span>
                                </div>
                            </div>
                        </div>

                        <div class="col-3">
                            <div class="text-body-secondary small">
                                Unique Games
                            </div>
                            <div class="fs-4 fw-semibold"
                                 id="totalUniqueGames"></div>
                            <i class="bi bi-controller text-primary"></i>
                        </div>

                        <div class="col-3">
                            <div class="text-body-secondary small">
                                Avg. Games / Month
                            </div>
                            <div class="fs-4 fw-semibold"
                                 id="averageUniqueGamesPerMonth"></div>
                            <i class="bi bi-graph-up text-success"></i>
                        </div>

                    </div>

                    <hr class="my-3">

                    <div class="row">

                        <!-- Days -->
                        <div class="col-6">
                            <div class="small fw-semibold mb-2">
                                Most Active Days
                            </div>

                            <div class="list-group list-group-flush"
                                 id="playerActivityDays">
                            </div>
                        </div>

                        <!-- Hours -->
                        <div class="col-6">
                            <div class="small fw-semibold mb-2">
                                Most Active Hours
                            </div>

                            <div class="list-group list-group-flush"
                                 id="playerActivityHours">
                            </div>
                        </div>

                    </div>

                </div>
            </div>
        </div>
        <div class="col-12 col-md-4">
            <div class="card shadow-sm h-100">

                <div class="card-header py-2">
                    <h6 class="mb-0">Chat / Command Ratio</h6>
                    <small class="text-body-secondary">
                        Top 10 Players
                    </small>
                </div>

                <div class="card-body py-2">

                    <div class="list-group list-group-flush"
                         id="chatCommandRatioPlayers">
                    </div>

                </div>
            </div>
        </div>
    </div>
</div>
