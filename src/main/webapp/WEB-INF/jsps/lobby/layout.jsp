<div class="lobby-layout">
    <div class="lobby-col-left">
        <jsp:include page="game-list.jsp"/>
    </div>
    <div class="lobby-col-right">
        <div class="d-none d-flex flex-column flex-fill min-h-0" id="lobbyCreateCol">
            <jsp:include page="game-create.jsp"/>
        </div>
        <div class="d-none d-flex flex-column flex-fill min-h-0" id="lobbyDetailCol">
            <jsp:include page="game-detail.jsp"/>
        </div>
        <div class="d-flex flex-column flex-fill align-items-center justify-content-center text-muted min-h-0" id="lobbyEmptyCol">
            <i class="bi bi-controller fs-1 mb-2"></i>
            <span>Select a game or create a new one</span>
        </div>
    </div>
</div>
