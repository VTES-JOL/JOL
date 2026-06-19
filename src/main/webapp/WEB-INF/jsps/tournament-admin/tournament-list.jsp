<%@ page import="net.deckserver.services.TournamentService" %>
<%@ page import="net.deckserver.storage.json.system.TournamentMetadata" %>
<%@ page import="java.util.List" %>
<%@ page import="net.deckserver.game.enums.GameStatus" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    List<TournamentMetadata> editTours = TournamentService.getTournamentsWithStatus(List.of(GameStatus.EDIT));
    List<TournamentMetadata> startingTours = TournamentService.getTournamentsWithStatus(List.of(GameStatus.STARTING));
    List<TournamentMetadata> activeTours = TournamentService.getTournamentsWithStatus(List.of(GameStatus.ACTIVE));
    pageContext.setAttribute("editTours", editTours);
    pageContext.setAttribute("startingTours", startingTours);
    pageContext.setAttribute("activeTours", activeTours);
%>
<div class="card shadow">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Tournaments</span>
        <button class="btn btn-sm btn-outline-secondary" onclick="newTournament()">New <i class="bi-plus"></i></button>
    </div>
    <div class="scrollable mhd-70">
        <ul class="list-group list-group-flush" id="tournamentAdminList">
            <c:forEach items="${activeTours}" var="t">
                <li class="list-group-item d-flex justify-content-between align-items-center tournament-admin-entry"
                    data-name="${t.name}" data-status="ACTIVE"
                    data-reg-end="${t.registrationEndTime}" data-play-start="${t.startTime}" data-play-end="${t.endTime}"
                    onclick="tournamentAdminClick(this)" style="cursor:pointer">
                    <span>${t.name}</span>
                    <span class="badge text-bg-success">Active</span>
                </li>
            </c:forEach>
            <c:forEach items="${startingTours}" var="t">
                <li class="list-group-item d-flex justify-content-between align-items-center tournament-admin-entry"
                    data-name="${t.name}" data-status="STARTING"
                    data-reg-end="${t.registrationEndTime}" data-play-start="${t.startTime}" data-play-end="${t.endTime}"
                    onclick="tournamentAdminClick(this)" style="cursor:pointer">
                    <span>${t.name}</span>
                    <span class="badge text-bg-secondary">Starting</span>
                </li>
            </c:forEach>
            <c:forEach items="${editTours}" var="t">
                <li class="list-group-item d-flex justify-content-between align-items-center tournament-admin-entry"
                    data-name="${t.name}" data-status="EDIT"
                    data-reg-end="${t.registrationEndTime}" data-play-start="${t.startTime}" data-play-end="${t.endTime}"
                    onclick="tournamentAdminClick(this)" style="cursor:pointer">
                    <span>${t.name}</span>
                    <span class="badge text-bg-warning">Draft</span>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>
