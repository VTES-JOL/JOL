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
            <div class="card shadow" data-region="hand">
                <div class="card-header bg-body-secondary">Hand</div>
                <ol class="card-body list-group list-group-numbered p-0" id="hand"></ol>
            </div>
        </div>
        <div class="col-xl-6 col-lg-5 col-md-6">
            <div class="card shadow mt-2 mt-md-0">
                <div class="card-header bg-body-secondary">Commands</div>
                <div class="card-body">
                    <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm">
                        <div class="row gy-1 align-items-center">
                            <div class="col-md-2">
                                <label for="phase">Phase</label>
                            </div>
                            <div class="col-md-10">
                                <select id="phase" class="form-select form-select-sm"></select>
                            </div>
                            <div class="col-md-2">
                                <label for="command">Command</label>
                            </div>
                            <div class="col-md-10">
                                <div class="input-group input-group-sm">
                                    <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                                            data-bs-target="#quickCommandModal" tabindex="-1">...
                                    </button>
                                    <input type="text" class="form-control form-control-sm" id="command">
                                </div>
                            </div>
                            <div class="col-md-2">
                                <label for="chat">Chat</label>
                            </div>
                            <div class="col-md-10">
                                <div class="input-group input-group-sm">
                                    <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                                            data-bs-target="#quickChatModal" tabindex="-1">...
                                    </button>
                                    <input type="text" class="form-control form-control-sm" id="chat">
                                </div>
                            </div>
                            <div class="col-md-2 col-1">
                                <label for="ping">Ping</label>
                            </div>
                            <div class="col-md-4 col-5">
                                <select id="ping" class="form-select form-select-sm"></select>
                            </div>
                            <div class="col-md-1 col-1">
                                <label for="endTurn">End</label>
                            </div>
                            <div class="col-md-3 col-5">
                                <select id="endTurn" class="form-select form-select-sm">
                                    <option value="No">No</option>
                                    <option value="Yes">Yes</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button class="btn btn-secondary btn-sm" id="gameSubmit">Submit</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <div class="card shadow mt-2">
                <div class="card-header bg-body-secondary justify-content-between d-flex align-items-center">
                    <span>Game Chat</span>
                    <span id="gameLabel" class="px-2"></span>
                </div>
                <div class="card-body p-0">
                    <div id="gameChatOutput" class="scrollable bg-white p-1"></div>
                </div>
            </div>
        </div>
        <div class="col-lg-4 col-md-12">
            <div class="card shadow mt-lg-0 mt-md-2 mt-2">
                <div class="card-header bg-body-secondary">Information</div>
                <div class="card-bod p-1" id="informationPanel">
                    <p class="d-inline-flex gap-1 mb-2">
                        <button class="btn btn-secondary btn-sm" type="button" data-bs-toggle="collapse"
                                data-bs-target="#globalNotesPanel" aria-expanded="true"
                                aria-controls="globalNotesPanel">Global Notes
                        </button>
                        <button class="btn btn-secondary btn-sm player-only" type="button" data-bs-toggle="collapse"
                                data-bs-target="#privateNotesPanel" aria-expanded="false"
                                aria-controls="privateNotesPanel">Private Notes
                        </button>
                        <button class="btn btn-secondary btn-sm" type="button" data-bs-toggle="collapse"
                                data-bs-target="#historyPanel" aria-expanded="false" aria-controls="historyPanel">
                            History
                        </button>
                        <button class="btn btn-secondary btn-sm player-only" type="button" data-bs-toggle="collapse"
                                data-bs-target="#gameDeckPanel" aria-expanded="false" aria-controls="gameDeckPanel"
                                onclick="doShowDeck()">Game Deck
                        </button>
                    </p>
                    <div class="collapse multi-collapse show" id="globalNotesPanel" data-bs-parent="#informationPanel">
                        <label for="globalNotes">Global Notes</label>
                        <textarea id="globalNotes" class="form-control" onblur="sendGlobalNotes();"></textarea>
                    </div>
                    <div class="collapse multi-collapse" id="privateNotesPanel" data-bs-parent="#informationPanel">
                        <label for="privateNotes">Private Notes</label>
                        <textarea id="privateNotes" class="form-control" onblur="sendPrivateNotes();"></textarea>
                    </div>
                    <div class="collapse multi-collapse p-0" id="historyPanel" data-bs-parent="#informationPanel">
                        <label for="historySelect">History:</label>
                        <select id="historySelect" class="form-select form-select-sm mb-1"
                                onchange="getHistory()"></select>
                        <div id="gameHistory" class="bg-white p-1"></div>
                    </div>
                    <div class="collapse multi-collapse " id="gameDeckPanel" data-bs-parent="#informationPanel">
                        <div id="gameDeck"></div>
                    </div>
                </div>
            </div>
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

<div class="modal" id="quickCommandModal" tabindex="-1" role="dialog" aria-labelledby="quickCommandModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickCommandModalLabel">
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Command</span>
                </h5>
            </div>
            <div class="modal-body">
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn
                    edge
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw
                    crypt
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">
                    Discard random
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">
                    Shuffle crypt
                </button>

                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +1')">Pool +1
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +2')">+2</button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +3')">+3</button>

                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -1')">Pool -1
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -2')">-2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -3')">-3</button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="quickChatModal" tabindex="-1" role="dialog" aria-labelledby="quickChatModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickChatModalLabel">
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Chat</span>
                </h5>
            </div>
            <div class="modal-body">
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Bleed')">Bleed</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Hunt')">Hunt</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Block?')">Block?</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No block')">No block
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Yes')">Yes</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No')">No</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Wait')">Wait</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('1')">1</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('2')">2</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('3')">3</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('4')">4</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre-range')">No
                    pre-range
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No
                    pre, no maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Long')">Long</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No grapple')">No grapple
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Wave')">Wave</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No additional strikes')">No
                    additional strikes
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No press')">No press
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Combat ends')">Combat ends
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendChat('No sudden/wash')">No
                    sudden/wash
                </button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="playCardModal" tabindex="-1" role="dialog" aria-labelledby="playCardModalLabel" aria-hidden="true">
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
                            onclick="playCard(event);">Play
                    </button>
                </div>
                <div class="d-flex justify-content-center">
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard and replace"
                            onclick="discard();">
                        <span>&#10607;</span>
                    </button>
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard"
                            onclick="discard(false);">
                        <span>&#10585;</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fixed-top custom-modal" id="targetPicker" data-bs-backdrop="false" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">
                    <span class="card-type icon"></span>
                    <span class="card-name"></span>
                </h5>
            </div>
            <div class="modal-body">Pick target.</div>
        </div>
    </div>
</div>

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
                        <span>Play</span>
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
                            data-region="ready torpor"
                            onclick="burn();">
                        <span>Burn</span>
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
