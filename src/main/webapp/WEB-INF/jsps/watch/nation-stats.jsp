<div class="overflow-auto pb-3" style="height:78vh;">
    <!-- Chart Accordion -->
    <div class="accordion mb-3" id="nationChartAccordion">
        <div class="accordion-item">
            <h2 class="accordion-header" id="nationChartHeading">
                <button class="accordion-button"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#nationChartCollapse"
                        aria-expanded="true"
                        aria-controls="nationChartCollapse">
                    Top 10
                </button>
            </h2>
            <div id="nationChartCollapse" class="accordion-collapse collapse show"
                 aria-labelledby="nationChartHeading">
                <div class="accordion-body">
                    <div class="row g-4">
                        <div class="col-12 col-lg-3">
                            <div style="height: 350px;">
                                <canvas id="nationChartGames"></canvas>
                            </div>
                        </div>
                        <div class="col-12 col-lg-3">
                            <div style="height: 350px;">
                                <canvas id="nationChartWins"></canvas>
                            </div>
                        </div>
                        <div class="col-12 col-lg-3">
                            <div style="height: 350px;">
                                <canvas id="nationChartVp"></canvas>
                            </div>
                        </div>
                        <div class="col-12 col-lg-3">
                            <div style="height: 350px;">
                                <canvas id="nationChartPlayers"></canvas>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="accordion" id="nationTableAccordion">
        <div class="accordion-item">
            <h2 class="accordion-header" id="nationTableHeading">
                <button class="accordion-button"
                        type="button"
                        data-bs-toggle="collapse"
                        data-bs-target="#nationTableCollapse"
                        aria-expanded="true"
                        aria-controls="nationTableCollapse">
                    Table
                </button>
            </h2>
            <div id="nationTableCollapse" class="accordion-collapse collapse show"
                 aria-labelledby="nationTableHeading">
                <div class="accordion-body p-0">
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
                                                                        onclick="sortTable(2, 'statsNationGames')"></i>
                            </th>
                            <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                                        onclick="sortTable(3, 'statsNationGames')"></i>
                            </th>
                            <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                                          onclick="sortPercentageTable(4, 'statsNationGames')"></i>
                            </th>
                            <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                                          onclick="sortTable(5, 'statsNationGames')"></i>
                            </th>
                            <th class="sticky-top bg-white">Highest VP <i class="bi bi-filter"
                                                                          onclick="sortTable(6, 'statsNationGames')"></i>
                            </th>
                            <th class="sticky-top bg-white">Highest Win Streak <i class="bi bi-filter"
                                                                                  onclick="sortTable(7, 'statsNationGames')"></i>
                            </th><th class="sticky-top bg-white">Players Count <i class="bi bi-filter"
                                                                                  onclick="sortTable(8, 'statsNationGames')"></i>
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