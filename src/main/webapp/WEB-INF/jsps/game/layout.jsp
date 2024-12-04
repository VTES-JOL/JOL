<h5 class="w-100 d-flex justify-content-between align-items-center">
    <span id="gameTitle" class="fs-5 user-select-all"></span>
</h5>
<div class="container-fluid my-1 g-0">
    <div class="control-grid">
        <jsp:include page="hand-card.jsp"/>
        <jsp:include page="commands.jsp"/>
        <jsp:include page="game-chat.jsp"/>
        <jsp:include page="history.jsp"/>
        <jsp:include page="notes.jsp"/>
        <jsp:include page="game-deck.jsp"/>
    </div>
    <div class="row gx-2">
        <div class="col-12 row gy-1 gx-2" id="state"></div>
    </div>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="liveToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header text-bg-secondary opacity-100">
            <strong class="me-auto">V:TES Online</strong>
            <button type="button" class="btn-close btn-outline-secondary" data-bs-dismiss="toast"
                    aria-label="Close"></button>
        </div>
        <div class="toast-body" id="gameStatusMessage"></div>
    </div>
</div>

<jsp:include page="quick-command-modal.jsp"/>
<jsp:include page="quick-chat-modal.jsp"/>
<jsp:include page="play-card-modal.jsp"/>
<jsp:include page="pick-target-modal.jsp"/>
<jsp:include page="card-modal.jsp"/>