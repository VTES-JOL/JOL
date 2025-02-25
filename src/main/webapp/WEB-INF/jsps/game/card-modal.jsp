<div class="modal" id="cardModal" tabindex="-1" role="dialog" aria-labelledby="cardModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content loading" style="height:30vh;text-align:center">
            <h2 style="position:relative;top:43%">Loading...</h2>
        </div>
        <div class="modal-content loaded" style="text-align:center">
            <div class="modal-header">
                <h5 class="modal-title d-flex justify-content-between align-items-center w-100">
                    <span class="align-items-center">
                        <span class="card-clan"></span>
                        <span class="card-name" id="cardModalLabel">Maris Streck</span>
                        <span class="votes mx-2" title="Votes">3</span>
                    </span>
                    <span class="card-label badge bg-light text-black shadow fs-6"></span>
                </h5>
            </div>
            <div class="modal-body">
                <div class="d-flex align-items-center">
                    <span class="discipline">a</span>
                </div>
                <div class="card-text text-start"></div>
            </div>
            <div class="modal-footer d-flex flex-wrap justify-content-center">
                <div class="transfers-and-counters">
                    <div class="d-flex justify-content-between fs-5 rounded-pill align-items-center bg-danger-subtle gap-1">
                        <div class="counters badge rounded-pill text-bg-secondary fs-5 gap-1 d-flex align-items-center"
                             title="Counters; click right side to increase, left to decrease">
                        </div>
                        <div class="transfers transfer-btn transfer-btn-left fs-3" title="Transfer one pool to this card"
                             onclick="transferToCard();">&#9668;
                        </div>
                        <div class="transfers transfer-btn transfer-btn-right fs-3" title="Transfer one blood to your pool"
                             onclick="transferToPool();">&#9658;
                        </div>
                        <div class="transfers badge rounded-pill text-bg-danger fs-5 card-modal-pool">99 pool</div>
                    </div>
                </div>
                <div class="mt-2">
                    <button type="button" class="btn btn-outline-dark m-1" title="Play"
                            data-region="inactive" data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="playVamp();">
                        <span>Influence</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Bleed"
                            data-region="ready" data-lock-state="unlocked"
                            data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="bleed();">
                        <span>Bleed</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Contest"
                            data-region="ready torpor" data-contested="false"
                            onclick="contest(true);">
                        <span>Contest</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Clear Contest"
                            data-region="ready torpor" data-contested="true"
                            onclick="contest(false);">
                        <span>Clear Contest</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Hunt"
                            data-region="ready" data-lock-state="unlocked"
                            data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="hunt();">
                        <span>Hunt</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Torpor"
                            data-region="ready"
                            data-top-level-only
                            data-minion-only
                            onclick="torpor();">
                        <span>Send to Torpor</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Go Anarch"
                            data-region="ready" data-lock-state="unlocked"
                            data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="goAnarch();">
                        <span>Go Anarch</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Leave Torpor"
                            data-region="torpor" data-lock-state="unlocked"
                            data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="leaveTorpor();">
                        <span>Leave Torpor</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Lock"
                            data-lock-state="unlocked"
                            data-region="ready torpor"
                            onclick="lock();">
                        <span>&cudarrr;</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Unlock"
                            data-lock-state="locked"
                            data-region="ready torpor"
                            onclick="unlock();">
                        <span style="transform: rotate(-90deg);">&#10548;</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Block"
                            data-region="ready" data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="block();">
                        <span>Block</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Burn"
                            data-region="ready torpor inactive"
                            onclick="burn();">
                        <span><i class="bi bi-fire"></i> Burn</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to Hand"
                            data-region="ashheap"
                            data-owner-only
                            onclick="moveHand();">
                        <span>Move to Hand</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to bottom of Library"
                            data-region="ashheap"
                            data-owner-only
                            data-non-minion-only
                            onclick="moveLibrary(false);">
                        <span>Move to Library</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to top of Library"
                            data-region="ashheap"
                            data-owner-only
                            data-non-minion-only
                            onclick="moveLibrary(true);">
                        <span>Move to Library (Top)</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to uncontrolled"
                            data-region="ashheap"
                            data-owner-only
                            data-minion-only
                            onclick="moveUncontrolled();">
                        <span>Move to Uncontrolled</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
