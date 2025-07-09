<%@ page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="net.deckserver.game.storage.state.RegionType" %>
<%@ page import="net.deckserver.game.ui.state.CardDetail" %>
<%@ page import="net.deckserver.storage.json.cards.CardSummary" %>
<%@ page import="net.deckserver.game.storage.cards.CardSearch" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.common.base.Strings" %>
<%@ page import="net.deckserver.game.interfaces.state.Card" %>
<%@ page import="net.deckserver.game.storage.cards.Sect" %>
<%@ page import="net.deckserver.game.storage.cards.Clan" %>
<%@ page import="net.deckserver.game.storage.cards.Path" %>
<%@ page import="java.util.Optional" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    String viewer = (String) request.getAttribute("viewer");
    String player = request.getParameter("player");
    String id = request.getParameter("id");
    String index = request.getParameter("index");
    boolean topLevel = index.split("\\.").length == 1;
    pageContext.setAttribute("currentIndex", index);
    boolean shadow = Boolean.parseBoolean(request.getParameter("shadow"));
    RegionType region = RegionType.valueOf(request.getParameter("region"));
    Card card = game.getCard(id);
    Card[] cards = card.getCards();
    CardDetail cardDetail = game.getDetail(card);
    CardSummary cardSummary = CardSearch.INSTANCE.get(cardDetail.getCardId());
    List<String> defaultDisciplines = cardSummary.getDisciplines();
    String defaultVotes = cardSummary.getVotes();
    String defaultClan = cardSummary.getSingleClanClass();
    Path path = Optional.ofNullable(cardDetail.getPath()).map(Path::valueOf).orElse(null);
    Integer defaultCapacity = cardSummary.getCapacity();
    String defaultSect = cardSummary.getSect();
    Sect sect = Sect.of(cardDetail.getSect());
    Clan clan = Clan.of(cardDetail.getClan());
    List<String> disciplines = cardDetail.getDisciplines();
    if (disciplines.isEmpty()) { disciplines = defaultDisciplines; }
    String label = cardDetail.getLabel();
    int counters = cardDetail.getCounters();
    int capacity = cardDetail.getCapacity();
    boolean hasCapacity = capacity > 0;
    if (capacity == 0 && defaultCapacity != null && topLevel) {
        capacity = defaultCapacity;
    }
    String votes = cardDetail.getVotes();
    if (Strings.isNullOrEmpty(votes)) { votes = defaultVotes; }
    boolean hasVotes = !Strings.isNullOrEmpty(votes) && !votes.equals("0");
    boolean contested = cardDetail.isContested();
    boolean locked = cardDetail.isLocked();
    boolean infernal = cardDetail.isInfernal();
    String shadowStyle = shadow ? "shadow" : "";
    if (hasCapacity && cardSummary.hasBlood()) {
        if (sect == null) {
            sect = Sect.of(defaultSect);
        }
        if (clan == null) {
            clan = Clan.of(defaultClan);
        }
    }
    // Counter Style Logic
    // Green: hasLife && OTHER_VISIBLE_REGION
    // Red: hasBlood || hasCapacity
    String counterStyle = (cardSummary.hasLife() && RegionType.OTHER_VISIBLE_REGIONS.contains(region) ) ? "text-bg-success" : ((cardSummary.hasBlood() || hasCapacity) ? "text-bg-danger" : "text-bg-secondary");
    String regionStyle = region == RegionType.TORPOR ? "opacity-75" : "";
    String contestedStyle = contested ? "bg-warning-subtle" : "";
    String counterText = counters + (capacity > 0 ? " / " + capacity : "");
    String attributes = cardDetail.buildAttributes(region, index, true);
    String action = CardDetail.FULL_ATTRIBUTE_REGIONS.contains(region) ? "cardOnTableClicked(event);" : "pickCard(event);";
    String showAction = game.getPlayers().contains(viewer) ? action : "";
    request.setAttribute("visible", true);
    request.setAttribute("player", player);
    request.setAttribute("cards", cards);
%>
<li <%= attributes %> onclick="<%= showAction %>" class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1 <%= regionStyle%> <%= shadowStyle %> <%= contestedStyle %>">
    <div class="mx-1 me-auto w-100">
        <div class="d-flex justify-content-between">
            <div class="d-flex flex-column">
                <div class="d-flex align-items-center gap-1">
                    <a data-card-id="<%= cardDetail.getCardId() %>" class="card-name text-wrap">
                        <%= cardSummary.getDisplayName() %>
                        <c:if test="<%= cardSummary.isAdvanced() %>"><i class='icon adv'></i></c:if>
                    </a>
                    <c:if test="<%= hasVotes %>"><span class="badge rounded-pill text-bg-warning "><%= votes %></span></c:if>
                    <c:if test="<%= contested %>"><span class="badge text-bg-warning p-1 px-2" style="font-size: 0.6rem;">CONTESTED</span></c:if>
                </div>
                <div class="d-flex align-items-center gap-1">
                    <span>
                        <c:forEach items="<%= disciplines %>" var="disc">
                            <span class="icon ${disc}"></span>
                        </c:forEach>
                    </span>
                    <span class="badge bg-light text-black shadow border border-secondary-subtle"><%= label %></span>
                </div>
            </div>
            <div class="d-flex flex-column">
                <div class="d-flex justify-content-end align-items-center gap-1">
                    <c:if test="<%= infernal %>"><i class="bi bi-fire text-danger fs-6"></i></c:if>
                    <c:if test="<%= locked %>"><span class="badge text-bg-dark p-1 px-2" style="font-size: 0.6rem;">LOCKED</span></c:if>
                    <c:if test="<%= counters > 0 || capacity > 0%>"><span class="badge rounded-pill shadow <%= counterStyle%>"><%= counterText%></span></c:if>
                </div>
                <div class="d-flex justify-content-end align-items-center gap-1">
                    <c:if test="<%= cardSummary.hasBlood() %>">
                        <c:if test="<%= path != null %>">
                            <% assert path != null; %>
                            <span class="path <%= path.toString().toLowerCase() %>"></span>
                        </c:if>
                        <c:if test="<%= sect != null %>">
                            <% assert sect != null; %>
                            <small class="sect" title="<%= sect.getDescription()%>"><%= sect.getDescription() %></small>
                        </c:if>
                        <c:if test="<%= clan != null %>">
                            <% assert clan != null; %>
                            <span class="clan <%= clan.toString().toLowerCase() %>" title="<%= clan.getDescription() %>"></span>
                        </c:if>
                    </c:if>
                </div>
        </div>
    </div>
        <ol class="list-group list-group-numbered ms-n3">
            <c:forEach items="<%= cards %>" var="card" varStatus="counter">
                <c:if test="${!player.equals(card.owner)}">
                    <c:set var="visible" value="true"/>
                </c:if>
                <c:choose>
                    <c:when test="${visible}">
                        <jsp:include page="card.jsp">
                            <jsp:param name="player" value="<%= player%>"/>
                            <jsp:param name="region" value="<%= region %>"/>
                            <jsp:param name="id" value="${card.id}"/>
                            <jsp:param name="shadow" value="false"/>
                            <jsp:param name="visible" value="true"/>
                            <jsp:param name="index" value="${currentIndex}.${counter.count}"/>
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <jsp:include page="card-hidden.jsp">
                            <jsp:param name="player" value="<%= player%>"/>
                            <jsp:param name="region" value="<%= region %>"/>
                            <jsp:param name="id" value="${card.id}"/>
                            <jsp:param name="index" value="${currentIndex}.${counter.count}"/>
                        </jsp:include>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ol>

   <%-- <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
            <span>
                <a data-card-id="<%= cardDetail.getCardId() %>" class="card-name text-wrap">
                    <%= cardSummary.getDisplayName() %>
                    <c:if test="<%= cardSummary.isAdvanced() %>">
                        <i class='icon adv'></i>
                    </c:if>
                </a>
                <c:if test="<%= hasVotes %>">
                    <span class="badge rounded-pill text-bg-warning "><%= votes %></span>
                </c:if>
            </span>
        <span class="d-flex gap-1 align-items-center">
                <c:if test="<%= locked %>"><span class="badge text-bg-dark p-1 px-2" style="font-size: 0.6rem;">LOCKED</span></c:if>
                <c:if test="<%= contested %>"><span class="badge text-bg-warning p-1 px-2" style="font-size: 0.6rem;">CONTESTED</span></c:if>
                <c:if test="<%= counters > 0 || capacity > 0%>">
                    <span class="badge rounded-pill shadow <%= counterStyle%>"><%= counterText%></span>
                </c:if>
            </span>
    </div>
    <div class="d-flex justify-content-between align-items-center w-100 pb-1">
            <span>
                <c:forEach items="<%= disciplines %>" var="disc">
                    <span class="icon ${disc}"></span>
                </c:forEach>
            </span>
        <span class="d-flex align-items-center gap-1">
                <span class="badge bg-light text-black shadow border border-secondary-subtle"><%= label %></span>
                <c:if test="<%= cardSummary.hasBlood() %>">
                    <c:if test="<%= !Strings.isNullOrEmpty(path) %>">
                        <span class="path <%= path %>"></span>
                    </c:if>
                    <c:if test="<%= sect != null %>">
                        <% assert sect != null; %>
                        <span class="sect" title="<%= sect.getDescription()%>"><%= sect.getDescription() %></span>
                    </c:if>
                    <c:if test="<%= clan != null %>">
                        <% assert clan != null; %>
                        <span class="clan <%= clan.toString().toLowerCase() %>" title="<%= clan.getDescription() %>"></span>
                    </c:if>

                </c:if>
            </span>
    </div>--%>
</li>