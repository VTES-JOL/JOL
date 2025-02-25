<div class="modal" id="playCardModal" tabindex="-1" role="dialog" aria-labelledby="playCardModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content loading" style="height:30vh;text-align:center">
            <h2 style="position:relative;top:43%">Loading...</h2>
        </div>
        <div class="modal-content loaded" style="text-align:center">
            <div class="modal-header">
                <h5 class="modal-title">
                    <span class="card-type"></span>
                    <span class="card-name" id="playCardModalLabel"></span>
                </h5>
            </div>
            <div class="modal-body">
                <div class="requirements">
                    <span class="card-clan"></span>
                    <span class="card-cost"></span>
                </div>
                <p class="mb-2">
                    <span class="preamble"></span>
                </p>
                <div class="card-modes d-grid gap-2"></div>
                <div class="templates d-none">
                    <button type="button" class="card-mode btn btn-outline-dark mb-2" aria-pressed="false"
                            data-bs-toggle="button">
                        <span class="discipline"></span>
                        <span class="mode-text"></span>
                    </button>
                </div>
                <div class="extended-play-panel" style="display:none">
                    <hr/>
                    <button id="playCardModalPlayButton" type="button"
                            class="btn btn-outline-secondary mb-2" style="white-space:normal"
                            onclick="playCard()">Play
                    </button>
                </div>
                <div class="d-flex justify-content-center">
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard"
                            onclick="discard(false);">
                        <span><i class="bi bi-trash"></i> Discard</span>
                    </button>
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard and replace"
                            onclick="discard();">
                        <span><i class="bi bi-recycle"></i> Discard + Draw</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
