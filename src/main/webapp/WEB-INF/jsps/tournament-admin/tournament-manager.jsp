<%@ page import="net.deckserver.services.TournamentService" %>
<%@ page import="net.deckserver.storage.json.system.TournamentMetadata" %>
<%@ page import="java.util.List" %>
<%@ page import="net.deckserver.game.enums.GameStatus" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  List<TournamentMetadata> prepare = TournamentService.getTournamentsWithStatus(List.of(GameStatus.ACTIVE, GameStatus.STARTING, GameStatus.EDIT));
%>
<%-- Hidden select preserved so existing JS callbacks that read #nameOfTournament continue to work --%>
<select id="nameOfTournament" class="d-none">
  <c:forEach items="<%= prepare %>" var="tour">
    <option value="${tour.name}">${tour.name}</option>
  </c:forEach>
</select>

<div class="card shadow">
  <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
    <span class="fw-semibold" id="tourTablesTitle">Tournament Tables</span>
    <button class="btn btn-sm btn-outline-secondary" onclick="exitTourMode()">Close</button>
  </div>
  <div class="card-body">
    <div id="saveTables" class="d-none">
      <div class="d-flex gap-1 flex-wrap">
        <button onclick="saveTables()" class="btn btn-outline-secondary btn-sm">Save Tables</button>
        <button onclick="downloadCurrentTables()" class="btn btn-outline-secondary btn-sm">Download</button>
        <button onclick="showCurrentTables()" class="btn btn-outline-secondary btn-sm">Show Tables</button>
        <button data-bs-toggle="modal" data-bs-target="#importTablesModal" class="btn btn-outline-primary btn-sm">Import</button>
        <button onclick="createTournamentTables()" class="btn btn-outline-success btn-sm">Create Rounds</button>
      </div>
    </div>
    <div id="saveFinal" class="d-none">
      <div class="d-flex gap-1 flex-wrap mt-2">
        <button onclick="saveFinal()" class="btn btn-outline-secondary btn-sm">Save Final</button>
        <button onclick="startSeeding()" class="btn btn-outline-secondary btn-sm">Start Seeding</button>
        <button onclick="startFinal()" class="btn btn-outline-success btn-sm">Start Final</button>
      </div>
    </div>
    <div id="finalStartedMsg" class="d-none alert alert-info mt-2">Finals already started — see seating below.</div>
    <div id="importTablesMsg" class="d-none alert mt-2"></div>
    <div id="tourRounds" class="scrollable mhd-70 mt-2"></div>
    <div id="tourFinal" class="d-none">
      <span class="h4">Tournament Players</span>
      <ul id="finalPlayers" class="card-body p-1 grid sortableFinal"></ul>
      <div>
        <div class="card-body p-1">
          <span class="h4">Final Table</span>
          <i class="bi bi-shuffle" onclick="shuffleSeeding()"></i>
          <ul id="finalTable" class="border list-group sortableFinal" style="min-height: 38px"></ul>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="modal" id="importTablesModal" tabindex="-1" role="dialog" aria-labelledby="importTablesModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="importTablesModalLabel">Import Tables from CSV</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p class="text-muted small">Paste CSV data with columns <code>Round</code>, <code>Table</code>, <code>Player</code>. The header row is required.</p>
        <textarea id="importTablesCsv" class="form-control font-monospace" rows="14" placeholder='"Round","Table","Player"&#10;"1","1","PlayerOne"&#10;"1","1","PlayerTwo"'></textarea>
        <div id="importTablesError" class="d-none alert alert-danger mt-2"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-primary" onclick="importTables()">Import</button>
      </div>
    </div>
  </div>
</div>
