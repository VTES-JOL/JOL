<div class="overflow-auto pb-3" style="height:78vh;">
    <table id="statsGames" class="table table-bordered table-sm mb-0">
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
                                                        onclick="sortTable(2, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                        onclick="sortTable(3, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                          onclick="sortPercentageTable(4, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                          onclick="sortTable(5, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">Highest VP <i class="bi bi-filter"
                                                          onclick="sortTable(6, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">Unique Opponents <i class="bi bi-filter"
                                                                onclick="sortTable(7, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">Most played Opponent <i class="bi bi-filter"
                                                                    onclick="sortTable(8, 'statsGames')"></i>
            </th>
            <th class="sticky-top bg-white">Highest Win Streak <i class="bi bi-filter"
                                                                  onclick="sortTable(9, 'statsGames')"></i>
            </th>
        </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>