<%@ page import="net.deckserver.storage.json.system.TournamentMetadata" %>
<%@ page import="net.deckserver.game.enums.GameStatus" %>
<%@ page import="net.deckserver.services.TournamentService" %>
<%@ page import="java.util.List" %><%
    List<TournamentMetadata> loadAbleTournaments = TournamentService.getTournamentsWithStatus(List.of(GameStatus.STARTING));
%>
<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary">
        <h5>Create Tournament</h5>
    </div>
    <div>
        <label for="tourNameSelect" class="form-label">Choose Tournament:</label>
        <select name="tourNameSelect" id="tourNameSelect" class="form-select">
            <c:forEach items="<%= loadAbleTournaments %>" var="loadTour">
                <option value=""></option>
                <option value="${loadTour.id}">${loadTour.name}</option>
            </c:forEach>
        </select>
        <button onclick="loadTournamentDetails()" class="btn btn-outline-secondary btn-sm mt-2 w-100">Load Tournament Details</button>
    </div>
    <div class="card-body">
        <label for="tourName" class="form-label">Tournament Name:</label>
        <input id="tourName" size="50" class="form-control"/>
        <label for="regStart" class="form-label">Registration Start:</label>
        <input type="date" id="regStart" name="regStart" class="form-control">
        <label for="regEnd" class="form-label">Registration End:</label>
        <input type="date" id="regEnd" name="regEnd" class="form-control">
        <label for="playStart" class="form-label">Play Start:</label>
        <input type="date" id="playStart" name="playStart" class="form-control">
        <label for="playEnd" class="form-label">Play End:</label>
        <input type="date" id="playEnd" name="playEnd" class="form-control">
        <label for="numOfRounds" class="form-label">Number of Rounds:</label>
        <select name="numOfRounds" id="numOfRounds" class="form-select">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
        </select>
        <label for="reqId" class="form-label">Requires VEKN Id:</label>
        <select name="reqId" id="reqId" class="form-select">
            <option value="true">Yes</option>
            <option value="false">No</option>
        </select>
        <label for="tourFormat" class="form-label">Choose a Tournament Format:</label>
        <select name="tourFormat" id="tourFormat" class="form-select">
            <option value="SINGLE_DECK">Single Deck</option>
            <option value="MULTI_DECK">Multi-Deck</option>
        </select>
        <label for="gameFormat" class="form-label">Choose a Game Format:</label>
        <select name="gameFormat" id="gameFormat" class="form-select">
            <option value="STANDARD">Standard</option>
            <option value="V5">V5</option>
            <option value="DUEL">Duel</option>
            <option value="PLAYTEST">Playtest</option>
        </select>
        <label for="ruleText" class="form-label">Tournament Rules</label>
        <input id="ruleText" size="50" class="form-control"/>
        <button onclick="addTournamentRule()" class="btn btn-outline-secondary btn-sm mt-2 form-control">Add Rule</button>
        <div id="rulesDiv"></div>
        <label for="specRulesCon" class="form-label">Special Rules</label>
        <textarea id="specRulesCon" name="specRulesCon" rows="4" cols="50" class="form-control">The following JOL rules will be enforced for the duration of the rounds with the exception of the period between <Date> and <Date>.</textarea>
        <input id="specRuleText" size="50" class="form-control"/>
        <button onclick="addSpecTournamentRule()" class="btn btn-outline-secondary btn-sm mt-2 form-control">Add Special Rule</button>
        <div id="specRulesDiv"></div>
        <button onclick="createTournament()" class="btn btn-outline-secondary btn-sm mt-2 form-control">Create Tournament</button>
    </div>
</div>
