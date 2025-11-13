<div class="row mt-2">
    <div class="col-sm-4">
        <jsp:include page="site-admin.jsp"/>
        <jsp:include page="add-role.jsp"/>
        <jsp:include page="player-roles.jsp"/>
    </div>
    <div class="col-sm-4">
        <jsp:include page="replace-player.jsp"/>
        <jsp:include page="end-turn.jsp"/>
        <jsp:include page="rollback-game.jsp"/>
    </div>
    <div class="col-sm-4">
        <jsp:include page="idle-games.jsp"/>
        <jsp:include page="idle-players.jsp"/>
    </div>
</div>
