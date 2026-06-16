<div class="card shadow">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Past Games</span>
        <button class="btn btn-outline-secondary btn-sm" onclick="exportCsv()">Export CSV <i class="bi-download"></i></button>
    </div>
    <div class="scrollable mhd-85">
        <table id="pastGames" class="table table-sm table-hover mb-0">
            <thead>
            <tr>
                <th>Game</th>
                <th>Started</th>
                <th>Ended</th>
                <th colspan="3">Results</th>
            </tr>
            </thead>
            <tbody></tbody>
        </table>
    </div>
</div>
