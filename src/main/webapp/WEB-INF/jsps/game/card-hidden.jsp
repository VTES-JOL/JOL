<%@ page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="net.deckserver.game.enums.RegionType" %>
<%@ page import="net.deckserver.game.ui.CardDetail" %>
<%@ page import="net.deckserver.storage.json.game.CardData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    String viewer = (String) request.getAttribute("viewer");
    String player = request.getParameter("player");
    String id = request.getParameter("id");
    String index = request.getParameter("index");
    RegionType region = RegionType.valueOf(request.getParameter("region"));
    CardData card = game.getCard(id);
    CardDetail cardDetail = new CardDetail(card);
    String regionStyle = region == RegionType.REMOVED_FROM_GAME ? "opacity-50" : "";
    String attributes = cardDetail.buildAttributes(region, index, false);
    String action = RegionType.PLAYABLE_REGIONS.contains(region) && player.equals(viewer) ? "showPlayCardModal(event);" : (region == RegionType.ASH_HEAP ? "cardOnTableClicked(event);" : "");
    String showAction = game.getPlayers().contains(viewer) ? action : "";
%>
<li <%= attributes %> onclick="<%= showAction %>"
                      class="flex-grow-1 list-group-item d-flex justify-content-between align-items-center p-1 shadow <%= regionStyle %>">
    <div class="mx-1 me-auto w-100 align-items-center">
        <div class="d-flex justify-content-between align-items-center w-100">
            <span>*********</span>
        </div>
    </div>
</li>