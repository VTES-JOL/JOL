<div class="overflow-auto pb-3" style="height:78vh;">
  <table id="statsDeckGames" class="table table-bordered table-sm mb-0">
    <thead>
    <tr>
      <th class="sticky-top bg-white w-25">Deck / Player
        <input type="text" id="deckNameFilter"
               oninput="filterName('#statsDeckGames tbody tr', 'deckNameFilter', 1)">
        <i class="bi bi-filter" onclick="sortTable(0, 'statsDeckGames')"></i></th>
      <th class="sticky-top bg-white">Number of Games
        <input type="number" id="gameThresholdDeck" min="0" value="0"
               oninput="renderStats()" style="width: 45px; height: 25px;">
        <i class="bi bi-filter" onclick="sortTable(1, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">GW Total <i class="bi bi-filter"
                                                  onclick="sortTable(2, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">VP Total <i class="bi bi-filter"
                                                  onclick="sortTable(3, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">% Win Rate <i class="bi bi-filter"
                                                    onclick="sortPercentageTable(4, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">Average VP <i class="bi bi-filter"
                                                    onclick="sortTable(5, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">Highest VP <i class="bi bi-filter"
                                                    onclick="sortTable(6, 'statsDeckGames')"></i>
      </th>
      <th class="sticky-top bg-white">Highest Win Streak <i class="bi bi-filter"
                                                            onclick="sortTable(7, 'statsDeckGames')"></i>
      </th>
    </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>
