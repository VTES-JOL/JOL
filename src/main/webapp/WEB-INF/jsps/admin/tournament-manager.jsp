<%@ page import="net.deckserver.services.TournamentService" %>
<%@ page import="net.deckserver.storage.json.system.TournamentMetadata" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  List<TournamentMetadata> prepare = TournamentService.getTournamentsReadyToPrepare();
%>
    <div class="card shadow mt-2">
  <div class="card-header bg-body-secondary">
    <h5>Tournament Tables</h5>
  </div>
  <div class="card-body">
    <label for="nameOfTournament" class="form-label">Choose Tournament:</label>
    <select name="nameOfTournament" id="nameOfTournament" class="form-select">
      <c:forEach items="<%= prepare %>" var="tour">
        <option value="${tour.id}">${tour.name}</option>
      </c:forEach>
    </select>
    <div class="d-flex justify-content-between">
      <button onclick="loadTournament()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Load Tournament</button>
      <button onclick="saveTables()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Save Tables</button>
      <button onclick="startTournament()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Start Tournament</button>
    </div>
    <div id="tourRounds"></div>
  </div>
</div>

