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
                id="clansTab"
                data-bs-toggle="tab"
                data-bs-target="#jolClansPane"
                type="button">
            Clans
        </button>
    </li>
</ul>

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

                <div id="jolChartCollapse" class="accordion-collapse collapse show" aria-labelledby="jolChartHeading">
                    <div class="accordion-body">
                        <div style="height: 350px;">
                            <canvas id="myChart"></canvas>
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
                <div class="tab-pane fade" id="jolClansPane">
                    <div class="overflow-auto pb-3" style="height:78vh;">
                        <table id="statsJolClans" class="table table-bordered table-sm mb-0">
                            <thead>
                            <tr>
                                <th class="sticky-top bg-white">Clan
                                    <input type="text" id="clanFilter"
                                           oninput="filterName('#statsJolClans tbody tr', 'clanFilter', 1)">
                                    <i class="bi bi-filter" onclick="sortTable(0, 'statsJolClans')"></i></th>
                                <th class="sticky-top bg-white">Count
                                    <i class="bi bi-filter" onclick="sortTable(1, 'statsJolClans')"></i></th>
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