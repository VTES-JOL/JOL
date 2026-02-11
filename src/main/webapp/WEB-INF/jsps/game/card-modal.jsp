<div class="modal" id="cardModal" tabindex="-1" role="dialog" aria-labelledby="cardModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content loading" style="height:30vh;text-align:center">
            <h2 style="position:relative;top:43%">Loading...</h2>
        </div>
        <div class="modal-content loaded" style="text-align:center">
            <div class="modal-header py-1 px-2 justify-content-between align-items-center">
                <span class="d-flex align-items-center">
                    <span class="card-clan m-2"></span>
                    <label for="clan-select" class="d-none"></label>
                    <select id="clan-select" class="form-select form-select-sm ms-2 card-clan-select d-none"
                            style="width:auto"></select>
                    <span class="card-name fs-5" id="cardModalLabel"></span>
                    <span class="votes mx-2" title="Votes"></span>
                </span>
                <span class="d-flex align-items-center">
                    <span class="card-path"></span>
                    <label for="path-select" class="d-none"></label>
                    <select id="path-select" class="form-select form-select-sm ms-2 card-path-select d-none"
                            style="width:auto"></select>
                    <span class="card-sect m-2"></span>
                    <label for="sect-select" class="d-none"></label>
                    <select id="sect-select" class="form-select form-select-sm ms-2 card-sect-select d-none"
                            style="width:auto"></select>
                    <span class="card-cost"></span>
                    <button class="btn-close" title="Close" onclick="closeModal();"></button>
                </span>
            </div>
            <div class="modal-body">
                <div id="card-image"></div>
                <div class="input-group mt-2">
                    <label for="card-label" class="input-group-text"><i class="bi bi-tag"></i></label>
                    <input type="text" class="form-control" id="card-label"
                           placeholder="Add a label for all players to see." onchange="updateNotes();">
                </div>
            </div>
            <div class="modal-footer d-flex flex-column flex-wrap justify-content-center">
                <div class="transfers-and-counters">
                    <div class="d-flex justify-content-between fs-5 rounded-pill align-items-center bg-danger-subtle gap-1">
                        <div class="counters badge rounded-pill text-bg-secondary fs-5 gap-1 d-flex align-items-center"
                             title="Counters; click right side to increase, left to decrease">
                        </div>
                        <div class="transfers transfer-btn transfer-btn-left fs-3"
                             title="Transfer one pool to this card"
                             onclick="transferToCard();">&#9668;
                        </div>
                        <div class="transfers transfer-btn transfer-btn-right fs-3"
                             title="Transfer one blood to your pool"
                             onclick="transferToPool();">&#9658;
                        </div>
                        <div class="transfers badge rounded-pill text-bg-danger fs-5 card-modal-pool">99 pool</div>
                    </div>
                </div>
                <div class="mt-2">
                    <button type="button" class="btn btn-outline-dark m-1" title="Influence"
                            data-region="inactive" data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="influence();">
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
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to ready"
                            data-region="torpor"
                            data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="moveReady();">
                        <span>Move to ready</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Lock"
                            data-lock-state="unlocked"
                            data-region="ready torpor"
                            onclick="lock();">
                        <span><i class="bi bi-lock"></i> Lock</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Unlock"
                            data-lock-state="locked"
                            data-region="ready torpor"
                            onclick="unlock();">
                        <span style="transform: rotate(-90deg);"><i class="bi bi-unlock"></i> Unlock</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Block"
                            data-region="ready" data-top-level-only
                            data-owner-only
                            data-minion-only
                            onclick="block();">
                        <span><i class="bi bi-shield"></i> Block</span>
                    </button>
                    <br/>
                    <button type="button" class="btn btn-outline-dark m-1" title="Torpor"
                            data-region="ready"
                            data-top-level-only
                            data-minion-only
                            onclick="torpor();">
                        <span>Send to Torpor</span>
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
                    <button type="button" class="btn btn-outline-dark m-1" title="Remove from game"
                            data-region="ready ashheap inactive"
                            data-owner-only
                            onclick="removeFromGame();">
                        <span><i class="bi bi-escape"></i> Remove from Game</span>
                    </button>
                    <br/>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to Predator"
                            data-region="ready"
                            onclick="movePredator();">
                        <span><i class="bi bi-arrow-left-circle"></i> Move to Predator</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark m-1" title="Move to Prey"
                            data-region="ready"
                            onclick="movePrey();">
                        <span><i class="bi bi-arrow-right-circle"></i> Move to Prey</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
