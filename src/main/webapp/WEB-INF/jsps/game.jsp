<div class="modal" id="quickChatModal" tabindex="-1" role="dialog" aria-labelledby="quickChatModalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="quickChatModalLabel">Quick Chat</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <button type="button" class="btn btn-sm btn-outline-secondary m-1" onclick="sendChat('Block?')">Block?</button>
        <button type="button" class="btn btn-sm btn-outline-secondary m-1" onclick="sendChat('No block')">No block</button>
        <button type="button" class="btn btn-sm btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No pre-range')">No pre-range</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No pre, no maneuver</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Wave')">Wave</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No press')">No press</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Combat ends')">Combat ends</button>
        <button type="button" class="btn btn-sm btn-outline-success m-1" onclick="sendChat('No sudden/wash')">No sudden/wash</button>
      </div>
    </div>
  </div>
</div>

<div class="modal" id="cardModal" tabindex="-1" role="dialog" aria-labelledby="cardModalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content loading" style="height:50vh;text-align:center">
      <h2 style="position:relative;top:43%">Loading...</h2>
    </div>
    <div class="modal-content loaded" style="text-align:center">
      <div class="modal-header">
        <h5 class="modal-title">
          <span class="card-type action"></span>
          <span class="card-name" id="cardModalLabel">Song of Serenity</span>
        </h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <div class="requirements">
          <span class="card-clan"></span>
          <span class="card-cost">Costs 1 blood.</span>
        </div>
        <p class="preamble mb-2">Only usable before range is chosen.</p>
        <div class="card-modes"></div>
        <div class="templates d-none">
          <button type="button" class="card-mode btn btn-block btn-outline-dark mb-2" aria-pressed="false" data-toggle="button">
            <span class="discipline">a</span>
            <p class="mode-text">The opposing minion gets -1 strength this round. A vampire may play only one Song of Serenity each combat.</p>
          </button>
        </div>
        <div class="extended-play-panel" style="display:none">
          <hr/>
          <button id="cardModalPlayButton" type="button"
                  class="btn btn-block btn-primary mb-2" style="white-space:normal"
                  onclick="playCard(event);">Play</button>
          <!--hr/>
          <button type="button" class="btn btn-outline-danger mb-1">Discard</button>
          <input type="checkbox" checked="checked">Replace</input-->
        </div>
      </div>
    </div>
  </div>
</div>

<div id="targetPicker" class="fixed-top" style="display:none">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">
          <span class="card-type action"></span>
          <span class="card-name">Song of Serenity</span>
        </h5>
        <button type="button" class="close" onclick="closeTargetPicker()">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">Pick target.</div>
    </div>
  </div>
</div>

<script type="text/javascript" src="js/card-modal.js"></script>

<div id="game-info" class="border-bottom row no-gutters">
    <div class="col-sm-7">
        <div class="row no-gutters">
            <div id="playerHand" class="col-sm-6 player-only grey border-right">
                <div class="game-header">
                    <h5>Hand</h5>
                </div>
                <div id="hand" class="scrollable"></div>
            </div>
            <div id="playerCommands" class="player-only col-sm-6 grey">
                <div class="game-header">
                    <h5>Commands</h5>
                </div>
                <form onsubmit="return doSubmit()" autocomplete="off" class="padded">

                    <div id="phaseCommand" class="form-group form-row mb-1">
                        <label for="phase" class="col-4 col-form-label col-form-label-sm">Phase:</label>
                        <div class="col-8">
                            <select id="phase" name="phase" class="form-control form-control-sm"></select>
                        </div>
                    </div>
                    <div class="form-group form-row mb-1">
                        <label for="command" class="col-4 col-form-label col-form-label-sm">Command:</label>
                        <div class="col-8">
                            <input name="command" type="text" id="command" class="form-control form-control-sm"
                                   maxlength="100"/>
                        </div>
                    </div>
                    <div class="form-group form-row mb-1">
                        <label for="chat" class="col-2 col-form-label col-form-label-sm">Chat:</label>
                        <div class="col-2" style="text-align:right;padding-right:0">
                            <button id="quickChatButton" type="button" class="btn btn-sm btn-outline-secondary" data-toggle="modal" data-target="#quickChatModal" tabindex="-1">...</button>
                        </div>
                        <div class="col-8">
                            <input name="chat" type="text" id="chat" class="form-control form-control-sm" maxlength="100"/>
                        </div>
                    </div>
                    <div class="form-group form-row mb-1">
                        <label for="ping" class="col-4 col-form-label col-form-label-sm">Ping:</label>
                        <div class="col-8">
                            <select id="ping" name="ping" class="form-control form-control-sm"></select>
                        </div>
                    </div>
                    <div class="form-group form-row mb-1">
                        <div id="endCommand" class="col-8">
                            <div class="form-row">
                                <label for="endturn" class="col-6 col-form-label col-form-label-sm">End turn?</label>
                                <div class="col-6">
                                    <select id="endTurn" name="endturn" class="form-control form-control-sm">
                                        <option value="No">No</option>
                                        <option value="Yes">Yes</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-4">
                            <button name="Submit" type="submit" value="Submit" class="btn btn-sm btn-primary w-100">Submit</button>
                        </div>
                    </div>
                    <span id="status"></span>

                </form>
            </div>
        </div>
        <div class="row">
            <div id="gameChatContainer" class="col">
                <div class="game-header">
                    <h5>Game Chat</h5>
                    <span id="gameTitle"></span>
                    <span id="gameLabel"></span>
                </div>
                <div id="gameChatOutput" class="scrollable side-padded half-height-content light border-top"></div>
            </div>
        </div>
    </div>
    <div id="other" class="col-sm-5 grey border-left">
        <div class="game-header">
            <nav class="nav">
                <a class="nav-link" href="#" onclick="otherClicked(event)" data-target="notes">Notes</a>
                <a class="nav-link" href="#" onclick="otherClicked(event)" data-target="history">History</a>
                <a class="nav-link player-only" href="#" onclick="otherClicked(event)" data-target="deck">Deck</a>
            </nav>
        </div>
        <div id="history" class="row no-gutters">
            <div class="col reactive-height-content">
                <h5 class="notes-header">History
                    <select id="turns" onchange="getHistory()"></select>
                </h5>
                <div id="historyOutput" class="scrollable side-padded reactive-height-content-header"></div>
            </div>
        </div>
        <div id="notes" class="row no-gutters">
            <div class="col-sm-6 border-right reactive-height-content">
                <h5 class="notes-header">Global Notes</h5>
                <textarea id="globalNotes" class="game-notes reactive-height-content-header side-padded"></textarea>
            </div>
            <div class="col-sm-6 player-only reactive-height-content">
                <h5 class="notes-header">Private Notes</h5>
                <textarea id="privateNotes" class="game-notes reactive-height-content-header side-padded"></textarea>
            </div>
        </div>
        <div id="gameDeck" class="row no-gutters">
            <div class="col reactive-height-content">
                <h5 class="notes-header">Registered Deck</h5>
                <div id="gameDeckOutput" class="scrollable side-padded reactive-height-content-header"></div>
            </div>
        </div>
    </div>
</div>
<div id="state" class="game-table row no-gutters"></div>
