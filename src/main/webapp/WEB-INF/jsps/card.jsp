<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.deckserver.dwr.jsp.CardParams" %>
<%@ page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="net.deckserver.game.interfaces.state.Card" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="net.deckserver.storage.json.cards.CardSummary" %>
<%
    CardParams p = (CardParams) request.getAttribute("cparams");
    JolGame game = (JolGame) request.getAttribute("game");
    String region = (String) request.getAttribute("region");
    String coordinates = (String) request.getAttribute("coordinates");
    Card c = p.getCard();
    int capacity = game.getCapacity(c.getId());
    int counters = game.getCounters(c.getId());
    boolean locked = game.isTapped(c.getId());
    boolean contested = game.getContested(c.getId());
    String label = StringEscapeUtils.escapeHtml4(game.getText(c.getId()));
    String votes = game.getVotes(c.getId());
    boolean nested = p.doNesting();
    Card[] cards = c.getCards();
    boolean hasCards = cards != null && cards.length > 0;
    CardSummary summary = p.getSummary();
    boolean isCrypt = summary.isCrypt();
    boolean hasLife = summary.hasLife();
    boolean hasBlood = summary.hasBlood();
    boolean advanced = summary.isAdvanced();
    String typeClass = summary.getTypeClass();
    String[] disciplines = game.getDisciplines(c.getId());
    boolean hasDisciplines = disciplines.length > 0;
    request.setAttribute("typeClass", typeClass);
    request.setAttribute("isCrypt", isCrypt);
    request.setAttribute("hasLife", hasLife);
    request.setAttribute("hasBlood", hasBlood);
    request.setAttribute("p", p);
    request.setAttribute("c", c);
    request.setAttribute("game", game);
    request.setAttribute("capacity", capacity);
    request.setAttribute("counters", counters);
    request.setAttribute("advanced",advanced);
    request.setAttribute("votes", votes);
    request.setAttribute("locked", locked);
    request.setAttribute("label", label);
    request.setAttribute("contested", contested);
    request.setAttribute("nested", nested);
    request.setAttribute("cards", cards);
    request.setAttribute("hasCards", hasCards);
    request.setAttribute("cardEntry", summary);
    request.setAttribute("disciplines", disciplines);
    request.setAttribute("hasDisciplines", hasDisciplines);
    request.setAttribute("region", region);
%>

<li class="<%= contested ? "contested" : "" %>">

    <c:if test="${p.hidden}">
        <a data-coordinates="<%= coordinates %>" onclick="pickTarget(event)">XXXXXX</a>
    </c:if>
    <c:if test="${!p.hidden}">
        <a class="card-name <%= typeClass %>" data-card-id="<%= p.getId() %>" data-coordinates="<%= coordinates %>"
                <c:choose>
                    <c:when test="${region == 'hand'}">
                        onclick="showPlayCardModal(event)"
                    </c:when>
                    <c:when test="${region == 'ready-region' || region == 'torpor' || region == 'inactive-region'}">
                        data-contested="<%= contested %>"
                        data-label="<%= label %>" data-locked="<%= locked %>" data-counters="<%= counters %>" data-votes="<%= votes %>" data-capacity="<%= capacity %>"
                        onclick="cardOnTableClicked(event)"
                    </c:when>
                    <c:otherwise>
                        onclick="pickTarget(event)"
                    </c:otherwise>
                </c:choose>
        ><%= p.getName() %>
        </a>
    </c:if>
    <c:if test="${game != null}">
        <c:if test="${capacity > 0 && !p.hidden}">
            <small class="counter blood"><%= counters %> / <%= capacity %>
            </small>
        </c:if>
        <c:if test="${hasLife && counters > 0}">
            <small class="counter life"><%= counters %>
            </small>
        </c:if>
        <c:if test="${counters > 0 && !(capacity > 0 && !p.hidden) && !(hasLife)}">
            <small class="counter"><%= counters %>
            </small>
        </c:if>

        <c:if test="${votes != '0'}">
            <small class="counter votes" title="<%= votes %> votes"><%= votes %>
            </small>
        </c:if>

        <c:if test="${locked}">
            <small class="label label-dark">LOCKED</small>
        </c:if>
        <c:if test="${contested}">
            <small class="label">CONTEST</small>
        </c:if>
        <c:if test="${label.length() > 0}">
            <small class="label label-light"><%= label %>
            </small>
        </c:if>
        <c:if test="${hasDisciplines && (region == 'ready-region' || region == 'torpor')}">
            <p class="discipline-display">
                <c:forEach items="${disciplines}" var="disc">
                    <span class="discipline ${disc}"></span>
                </c:forEach>
            </p>
        </c:if>
        <c:if test="${nested && hasCards}">
            <ol>
                <%
                    int childCoord = 1;
                %>
                <c:forEach items="${cards}" var="child">
                    <%
                        Card child = (Card) pageContext.findAttribute("child");
                        request.setAttribute("cparams", new CardParams(child));
                        request.setAttribute("coordinates", String.format("%s %s", coordinates, childCoord));
                        ++childCoord;
                    %>
                    <jsp:include page="card.jsp"/>
                </c:forEach>
            </ol>
        </c:if>
    </c:if>
    <c:if test="${game == null && isCrypt}">
        ( <%= summary.getGroup() %>)
    </c:if>
</li>