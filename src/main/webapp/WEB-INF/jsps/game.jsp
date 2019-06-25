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
        <button type="button" class="btn btn-sm btn-outline-secondary m-1" onclick="sendChat('No block')">No block</button>
        <button type="button" class="btn btn-sm btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No pre-maneuver')">No pre-maneuver</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No pre, no maneuver</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
        <button type="button" class="btn btn-sm btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
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
        <h5 class="modal-title" id="cardModalLabel">
          <span class="card-type action" style="font-size:2em;vertical-align:sub"></span>
          <span class="card-name">Song of Serenity</span>
        </h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <p class="preamble mb-2">
          Only usable before range is chosen.
        </p>
        <div class="card-modes">
        </div>
        <div class="templates d-none">
          <button type="button" class="card-mode btn btn-block btn-outline-dark mb-2" style="white-space:normal" aria-pressed="false" data-toggle="button">
            <span class="discipline"></span>
            <p class="mode-text">The opposing minion gets -1 strength this round. A vampire may play only one Song of Serenity each combat.</span>
          </button>
        </div>
        <hr/>
        <button id="cardModalPlay" type="button"
                class="btn btn-block btn-primary mb-2" style="white-space:normal"
                onclick="playCard(event);">Play</button>
        <!--hr/>
        <button type="button" class="btn btn-outline-danger mb-1">Discard</button>
        <input type="checkbox" checked="checked">Replace</input-->
      </div>
    </div>
  </div>
</div>

<script type="text/javascript">
var DISCIPLINE_CHARS = { //TODO duplication from css bad
  ani: 'a', ANI: 'A', obe: 'b', OBE: 'B', cel: 'c', CEL: 'C',
  dom: 'd', DOM: 'D', dem: 'e', DEM: 'E', for: 'f', FOR: 'F',
  san: 'g', SAN: 'G', thn: 'h', THN: 'H', vic: 'i', VIC: 'I',
  pro: 'j', PRO: 'J', chi: 'k', CHI: 'K', val: 'l', VAL: 'L',
  mel: 'm', MEL: 'M', nec: 'n', NEC: 'N', obf: 'o', OBF: 'O',
  pot: 'p', POT: 'P', qui: 'q', QUI: 'Q', pre: 'r', PRE: 'R',
  ser: 's', SER: 'S', tha: 't', THA: 'T', aus: 'u', AUS: 'U',
  vis: 'v', VIS: 'V', abo: 'w', ABO: 'W', myt: 'x', MYT: 'X',
  dai: 'y', DAI: 'Y', spi: 'z', SPI: 'Z', obt: '*', OBT: '+',
  tem: '(', TEM: ')', str: ':', STR: ',', mal: '<', MAL: '>',
  flight: '='
};
function cardTypeCSSClass(cardType) {
  return cardType.toLowerCase().replace(' ', '_').replace('/', ' ');
}
function showCardModal(event) {
  console.log(event);
  $('#cardModal .loaded').hide();
  $('#cardModal .loading').show();
  //Show modal immediately with Loading text
  //$('#cardModal').modal('show');
  var cardId = $(event.target).data('card-id');
  var handIndex = $(event.target).data('index');
  $.get({
      //url: "rest/api/cards/au32", success: function(card) { //Guardian Vigil
      //url: "rest/api/cards/bl61", success: function(card) { //Elemental Stoicism
      url: "rest/api/cards/" + cardId, success: function(card) {
        console.log(card);
        var modal = $('#cardModal');
        modal.data('hand-index', handIndex);
        modal.data('do-not-replace', card.doNotReplace);

        $('#cardModal .card-name').text(card.displayName);
        $('#cardModal .card-type')
          .removeClass()
          .addClass('card-type ' + cardTypeCSSClass(card.type));
        $('#cardModal .preamble').text(card.preamble);

        var modeContainer = $('#cardModal .card-modes');
        modeContainer.empty();

        if (card.modes && card.modes.length > 0) {
          var modeTemplate = $('#cardModal .templates .card-mode');
          for (var i = 0; i < card.modes.length; ++i) {
            var mode = card.modes[i];
            var button = modeTemplate.clone();

            if (card.multiMode) {
              //button.data('toggle', 'button');
            }
            else button.on('click', playCard);

            button.data('disciplines', mode.disciplines);
            var disciplineStr = '';
            if (mode.disciplines != null) {
              for (var d of mode.disciplines)
                disciplineStr += DISCIPLINE_CHARS[d];
            }
            button.children('.discipline').text(disciplineStr);
            button.children('.mode-text').text(mode.text);
            button.appendTo(modeContainer);
          }
        }
        $('#cardModal .loading').hide();
        $('#cardModal .loaded').show();
        $('#cardModal').modal('show');
      }
  });
}
function playCardCommand(disciplines) {
  var modal = $('#cardModal');
  var handIndex1 = modal.data('hand-index') + 1;
  var doNotReplace = modal.data('do-not-replace');
  return 'play ' + handIndex1
         + (disciplines ? ' @ ' + disciplines.join(',') : '')
         + (doNotReplace ? '' : ' draw');
}
function playCard(event) {
  console.log(event);
  var button = $(event.target).closest('button'); //target might be inner p
  console.log(button);

  var disciplines = [];
  if (button.attr('id') == 'cardModalPlay') { //Multi-mode cards
    $('#cardModal .card-modes button.active')
      .each(function() { disciplines = disciplines.concat($(this).data('disciplines')); });
  }
  else { //Single-mode cards
    disciplines = button.data('disciplines');
  }

  var command = playCardCommand(disciplines);
  console.log(command);
  DS.submitForm(
      game, null, command, '', null, 'No',
      $("#globalNotes").val(), $("#privateNotes").val(), {
    callback: processData,
    errorHandler: errorhandler
  });
  $('#cardModal').modal('hide');
  return false;
}
</script>

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
