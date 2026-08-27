<div class="overflow-auto pb-3" style="height:78vh;">
  <table id="statsGameGames" class="table table-bordered table-sm mb-0">
    <thead>
    <tr>
      <th class="sticky-top bg-white">Game
        <input type="text" id="gameNameFilter"
               oninput="filterName('#statsGameGames tbody tr', 'gameNameFilter', 1)">
        <i class="bi bi-filter" onclick="sortTable(0, 'statsGameGames')"></i></th>
      <th class="sticky-top bg-white">Players
        <input type="text" id="gamePlayerFilter"
               oninput="filterName('#statsGameGames tbody tr', 'gamePlayerFilter', 2)">
        <i class="bi bi-filter" onclick="sortTable(1, 'statsGameGames')"></i></th>

      </th>
      <th class="sticky-top bg-white">Duration
        <i class="bi bi-filter" onclick="sortTableByDuration(2, '#statsGameGames tbody')"></i>
      </th>
      </th>
      <th class="sticky-top bg-white">GW?
        <i class="bi bi-filter" onclick="sortTableByBoolean(3)"></i>
      </th>
      </th>
      <th class="sticky-top bg-white">VPs
        <i class="bi bi-filter" onclick="sortTable(4, 'statsGameGames')"></i>
      </th>
    </tr>
    </thead>
    <tbody></tbody>
  </table>
</div>