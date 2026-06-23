<div class="row g-2 flex-fill align-items-stretch" style="min-height: 0">
    <div class="col-md-4 col-lg-3 d-flex flex-column" style="min-height: 0">
        <jsp:include page="game-list.jsp"/>
    </div>
    <div class="col-md-8 col-lg-9 d-flex flex-column" style="min-height: 0">
        <div class="d-none d-flex flex-column flex-fill" id="lobbyCreateCol" style="min-height: 0">
            <jsp:include page="game-create.jsp"/>
        </div>
        <div class="d-none d-flex flex-column flex-fill" id="lobbyDetailCol" style="min-height: 0">
            <jsp:include page="game-detail.jsp"/>
        </div>
        <div class="d-flex flex-column flex-fill align-items-center justify-content-center text-muted" id="lobbyEmptyCol" style="min-height: 0">
            <i class="bi bi-controller fs-1 mb-2"></i>
            <span>Select a game or create a new one</span>
        </div>
    </div>
</div>
