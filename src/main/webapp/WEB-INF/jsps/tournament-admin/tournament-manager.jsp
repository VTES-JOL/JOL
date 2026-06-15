<%@ page import="net.deckserver.services.TournamentService" %>
<%@ page import="net.deckserver.storage.json.system.TournamentMetadata" %>
<%@ page import="java.util.List" %>
<%@ page import="net.deckserver.game.enums.GameStatus" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  List<TournamentMetadata> prepare = TournamentService.getTournamentsWithStatus(List.of(GameStatus.ACTIVE, GameStatus.STARTING));
%>
<div class="card shadow mt-2">
  <div class="card-header bg-body-secondary">
    <h5>Tournament Tables</h5>
  </div>
  <div class="card-body">
    <label for="nameOfTournament" class="form-label">Choose Tournament:</label>
    <select name="nameOfTournament" id="nameOfTournament" class="form-select">
      <c:forEach items="<%= prepare %>" var="tour">
        <option value="${tour.name}">${tour.name}</option>
      </c:forEach>
    </select>
    <div class="d-flex justify-content-between">
      <button onclick="loadTournament()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Load Tournament</button>
    </div>
    <div id="saveTables" class="d-none">
      <div class="d-flex justify-content-between w-100">
        <button onclick="saveTables()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Save Tables</button>
        <button onclick="downloadCurrentTables()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Download Tables</button>
        <button onclick="showCurrentTables()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Show Tables</button>
        <button data-bs-toggle="modal" data-bs-target="#importTablesModal" class="btn btn-outline-primary btn-sm mt-2 w-100">Import Tables</button>
      </div>
      <div class="d-flex justify-content-between w-100">
        <button onclick="createTournamentTables()" class="btn btn-outline-success btn-sm mt-2 w-100">Create Rounds</button>
      </div>
    </div>
    <div id="saveFinal" class="d-none">
      <div class="d-flex justify-content-between w-100">
        <button onclick="saveFinal()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Save Final</button>
        <button onclick="startFinalSeeding()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Start Seeding</button>
        <button onclick="setFinalSeating()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Set Final Seating</button>
      </div>
      <div class="d-flex justify-content-between w-100">
        <button onclick="startFinal()" class="btn btn-outline-success btn-sm mt-2 w-100">Start Final</button>
      </div>
    </div>
    <div id="importTablesMsg" class="d-none alert mt-2"></div>
    <div id="tourRounds"></div>
    <div id="tourFinal" class="d-none">
      <ul id="finalPlayers" class="card-body p-1 grid sortableFinal"></ul>
      <div>
        <div class="card-body p-1">
          <span>Final Table</span>
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
