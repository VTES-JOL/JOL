<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@ page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="net.deckserver.game.enums.RegionType" %>
<%@ page import="net.deckserver.JolAdmin" %>
<%@ page import="java.util.List" %>
<%@ page import="net.deckserver.storage.json.game.CardData" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    String viewer = (String) request.getAttribute("viewer");
    String player = request.getParameter("player");
    String playerIndex = request.getParameter("playerIndex");
    String label = request.getParameter("label");
    RegionType region = RegionType.valueOf(request.getParameter("region"));
    boolean simpleDisplay = RegionType.SIMPLE_REGIONS.contains(region);
    String regionId = playerIndex + "-" + region;
    boolean startCollapsed = JolAdmin.INSTANCE.getGameModel(game.getName()).getView(viewer).isCollapsed(regionId);
    boolean isVisible = game.isVisible(player, viewer, region);
    String show = startCollapsed ? "" : "show";
    String collapsed = startCollapsed ? "collapsed" : "";
    String regionStyle = region == RegionType.TORPOR ? "bg-danger-subtle" : (region == RegionType.READY ? "bg-success-subtle" : "bg-body-secondary");
    int size = game.getSize(player, region);
    List<CardData> cards = game.data().getPlayerRegion(player, region).getCards();
    String regionName = region.xmlLabel().split(" ")[0];
    request.setAttribute("visible", isVisible);
    request.setAttribute("player", player);
%>

<c:if test="<%=!cards.isEmpty() %>">
    <div class="mb-2 text-bg-light" data-region="<%= regionName %>">
        <div class="p-2 d-flex justify-content-between align-items-center <%= regionStyle %>">
            <span>
                <button class="btn btn-sm p-0 <%= collapsed %>" onclick="details(event, '<%= regionId %>');"
                        data-bs-toggle="collapse" data-bs-target="#<%= regionId %>" aria-expanded="<%= isVisible %>"
                        aria-controls="<%= regionId %>">
                    <i class="fs-6 bi bi-plus-circle <%= startCollapsed ? "" :"d-none" %>"></i>
                    <i class="fs-6 bi bi-dash-circle <%= startCollapsed ? "d-none" : "" %>"></i>
                </button>
                <span class="fw-bold"><%= label %></span>
                <span>( <%= size %> )</span>
            </span>
        </div>
        <ol id="<%= regionId %>"
            class="region list-group list-group-flush list-group-numbered <%= regionStyle %> collapse <%= show %>">
            <c:forEach items="<%= cards %>" var="card" varStatus="counter">
                <c:if test="${!player.equals(card.ownerName)}">
                    <c:set var="visible" value="true"/>
                </c:if>
                <c:choose>
                    <c:when test="${visible}">
                        <c:if test="<%= !simpleDisplay %>">
                            <jsp:include page="card.jsp">
                                <jsp:param name="player" value="<%= player %>"/>
                                <jsp:param name="region" value="<%= region %>"/>
                                <jsp:param name="id" value="${card.id}"/>
                                <jsp:param name="shadow" value="true"/>
                                <jsp:param name="index" value="${counter.count}"/>
                            </jsp:include>
                        </c:if>
                        <c:if test="<%= simpleDisplay %>">
                            <jsp:include page="card-simple.jsp">
                                <jsp:param name="player" value="<%= player %>"/>
                                <jsp:param name="region" value="<%= region %>"/>
                                <jsp:param name="id" value="${card.id}"/>
                                <jsp:param name="index" value="${counter.count}"/>
                            </jsp:include>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <jsp:include page="card-hidden.jsp">
                            <jsp:param name="player" value="<%= player%>"/>
                            <jsp:param name="region" value="<%= region %>"/>
                            <jsp:param name="id" value="${card.id}"/>
                            <jsp:param name="index" value="${counter.count}"/>
                        </jsp:include>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </ol>
    </div>
</c:if>