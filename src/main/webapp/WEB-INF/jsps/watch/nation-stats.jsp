<div class="overflow-auto pb-3" style="height:78vh;">
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
      </th>
    </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>