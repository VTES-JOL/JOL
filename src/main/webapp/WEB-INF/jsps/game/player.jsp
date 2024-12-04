<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="net.deckserver.game.storage.state.RegionType" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    String viewer = (String) request.getAttribute("viewer");
    String player = request.getParameter("player");
    String playerIndex = request.getParameter("playerIndex");
    boolean active = game.getActivePlayer().equals(player);
    int pool = game.getPool(player);
    double vp = game.getVictoryPoints(player);
    boolean edge = player.equals(game.getEdge());
    boolean isPlayer = viewer.equals(player);
    String activeStyle = active ? "text-bg-light border-dark border-2" : (isPlayer ? "border-secondary border-2" : "");
    String activeHeaderStyle = active ? "bg-info-subtle" : "";
    String poolStyle = pool == 0 ? "text-bg-dark" : pool < 0 ? "text-bg-warning" : "text-bg-danger";
%>
<div class="col-xl col-lg-3 col-md-4 col-sm-6 g-2 player" data-player="<%= player %>" data-pool="<%= pool %>"
     data-vp="<%= vp %>" data-edge="<%= edge %>">
    <div class="card shadow-lg <%= activeStyle %>">
        <div class="card-header <%= activeHeaderStyle %> <%= activeStyle %>">
            <h6 class="d-flex justify-content-between align-items-center mb-0 lh-base">
                <span class="fw-bold">
                    <span><%= player %></span>
                    <c:if test="<%= game.isPinged(player) %>"><i class='bi-exclamation-triangle ms-2'></i></c:if>
                </span>
                <c:if test="<%= edge %>">
                    <span class="badge text-bg-light border border-secondary fw-bold align-items-center d-flex gap-1">
                        <i class="bi bi-chevron-left"></i>
                        Edge
                        <i class="bi bi-chevron-right"></i>
                    </span>
                </c:if>
                <span class="d-inline align-items-center">
                    <c:if test="<%= vp > 0%>">
                        <span class="badge rounded-pill text-bg-warning"><%= new DecimalFormat("0.#").format(vp) %> VP</span>
                    </c:if>
                    <span class="badge rounded-pill <%= poolStyle %>"><%= pool %></span>
                </span>
            </h6>
        </div>
        <div class="card-body p-2" id="<%= playerIndex %>">
            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Ready"/>
                <jsp:param name="region" value="<%= RegionType.READY %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Torpor"/>
                <jsp:param name="region" value="<%= RegionType.TORPOR %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Uncontrolled"/>
                <jsp:param name="region" value="<%= RegionType.UNCONTROLLED %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Ash heap"/>
                <jsp:param name="region" value="<%= RegionType.ASH_HEAP %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Removed from game"/>
                <jsp:param name="region" value="<%= RegionType.REMOVED_FROM_GAME %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex%>"/>
                <jsp:param name="label" value="Research"/>
                <jsp:param name="region" value="<%= RegionType.RESEARCH %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex %>"/>
                <jsp:param name="label" value="Library"/>
                <jsp:param name="region" value="<%= RegionType.LIBRARY %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex %>"/>
                <jsp:param name="label" value="Crypt"/>
                <jsp:param name="region" value="<%= RegionType.CRYPT %>"/>
            </jsp:include>

            <jsp:include page="region.jsp">
                <jsp:param name="player" value="<%= player %>"/>
                <jsp:param name="playerIndex" value="<%= playerIndex %>"/>
                <jsp:param name="label" value="Hand"/>
                <jsp:param name="region" value="<%= RegionType.HAND %>"/>
            </jsp:include>
        </div>
    </div>
</div>