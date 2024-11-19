<div class="accordion accordion-flush mt-2 shadow" id="gameHeader">
    <div class="accordion-item">
        <h5 class="accordion-header">
            <button class="accordion-button text-bg-light p-2" type="button" data-bs-toggle="collapse"
                    data-bs-target="#gameControls" aria-expanded="true" aria-controls="gameControls">
                <span class="w-100 d-flex justify-content-between align-items-center">
                    <span id="gameTitle" class="fs-5"></span>
                </span>
            </button>
        </h5>
    </div>
</div>
<div class="container-fluid my-1 g-0">
    <div class="row gx-2 accordion-collapse collapse show" id="gameControls">
        <div class="col-xl-2 col-lg-3 col-md-6 player-only">
            <jsp:include page="hand-card.jsp"/>
        </div>
        <div class="col-xl-6 col-lg-5 col-md-6">
            <jsp:include page="commands.jsp"/>
            <jsp:include page="game-chat.jsp"/>
        </div>
        <div class="col-lg-4 col-md-12">
            <jsp:include page="information.jsp"/>
        </div>
    </div>
    <div class="row gx-2">
        <div class="col-12 row gy-1 gx-2" id="state"></div>
    </div>
</div>

<div class="toast-container position-fixed top-0 end-0 p-3">
    <div id="liveToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header text-bg-secondary opacity-100">
            <strong class="me-auto">V:TES Online</strong>
            <button type="button" class="btn-close btn-outline-secondary" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body" id="gameStatusMessage"></div>
    </div>
</div>

<jsp:include page="quick-command-modal.jsp"/>
<jsp:include page="quick-chat-modal.jsp"/>
<jsp:include page="play-card-modal.jsp"/>
<jsp:include page="pick-target-modal.jsp"/>
<jsp:include page="card-modal.jsp"/>