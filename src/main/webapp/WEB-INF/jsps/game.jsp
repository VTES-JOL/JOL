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
          <button type="button" class="card-minion btn btn-block btn-outline-dark mb-2" aria-pressed="false" data-toggle="button">
            <span class="minion-name">Suzy</span>
            <small class="minion-life counter blood">3 / 7</small>
            <span class="minion-locked label-dark" style="border: 1px solid black; padding: 0 .5rem">LOCKED</span>
            <span class="minion-label label-light">Sammy</span>
            <span class="minion-index">(#1)</span>
          </button>
        </div>
        <div class="who-is-playing-panel" style="display:none">
          <hr/>
          <p>Who is playing this?</p>
          <div class="card-minions"></div>
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

<script type="text/javascript">
var CLAN_CHARS = {
  abomination: 'A', arihmane: 'B', akunanse: 'C', assamite: 'D', baali: 'E',
  blood_brothers: 'F', brujah: 'G', brujah_antitribu: 'H', caitiff: 'I',
  daughters_of_cacophony: 'J', followers_of_set: 'K', gangrel: 'L',
  gangrel_antitribu: 'M', gargoyle: 'N', giovanni: 'O', guruhi: 'P',
  harbingers_of_skulls: 'Q', ishtarri: 'R', kiasyd: 'S', lasombra: 'T',
  malkavian: 'U', malkavian_antitribu: 'V', nagaraja: 'W', nosferatu: 'X',
  nosferatu_antitribu: 'Y', osebo: 'Z', pander: '[', ravnos: '\\',
  salubri: ']', salubri_antitribu: '^', samedi: '_', toreador: '`',
  toreador_antitribu: 'a', tremere: 'b', tremere_antitrubu: 'c',
  true_brujah: 'd', tzimisce: 'e', ventrue: 'f', ventrue_antitribu: 'g',
  avenger: 'h', defender: 'i', innocent: 'j', judge: 'k', martyr: 'l',
  redeemer: 'm', visionary: 'n'
};
var DISCIPLINE_CHARS = {
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
function clanToKey(clan) {
  return clan.toLowerCase().replace(' ', '_');
}
function showCardModal(event) {
  $('#cardModal .loaded').hide();
  $('#cardModal .loading').show();
  //Show modal immediately with Loading text
  //$('#cardModal').modal('show');
  var cardId = $(event.target).data('card-id');
  var handIndex = $(event.target).data('index');
  $.get({
      //url: "rest/api/cards/lo7", success: function(card) { //Akhunanse Kholo
      //url: "rest/api/cards/km98", success: function(card) { //Pack Alpha
      //url: "rest/api/cards/jy322", success: function(card) { //Raven Spy
      //url: "rest/api/cards/tr14", success: function(card) { //CrimethInc
      //url: "rest/api/cards/lo170", success: function(card) { //Reanimated Corpse - an ally
      //url: "rest/api/cards/lo22", success: function(card) { //Base Hunting Ground - pool cost and clan requirement
      //url: "rest/api/cards/una21", success: function(card) { //Ennoia's Theater - multiple clans
      //url: "rest/api/cards/au36", success: function(card) { //Make the Misere - multiple lines in preamble
      //url: "rest/api/cards/au32", success: function(card) { //Guardian Vigil - multi-mode
      //url: "rest/api/cards/bl61", success: function(card) { //Elemental Stoicism - modes require multiple disciplines
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

        var clanText = '';
        if (card.clans != null) {
          for (var c of card.clans)
            clanText += CLAN_CHARS[clanToKey(c)];
        }
        $('#cardModal .card-clan').text(clanText);

        var costText = null;
        if (card.cost != null) costText = 'Costs ' + card.cost + '.';
        $('#cardModal .card-cost').text(costText);

        var modeContainer = $('#cardModal .card-modes');
        modeContainer.empty();

        var minionsConfigured = false;
        var allModesTargetActingMinion = true;

        if (card.modes && card.modes.length > 0) {
          var modeTemplate = $('#cardModal .templates .card-mode');
          for (var i = 0; i < card.modes.length; ++i) {
            var mode = card.modes[i];
            var button = modeTemplate.clone();

            var extendedPlayPanel = $('#cardModal .extended-play-panel');
            if (card.multiMode) {
              extendedPlayPanel.show();
              var playButton = $('#cardModalPlayButton');
              playButton.prop('disabled', true);
              playButton.text('Select one or more disciplines');
              button.on('click', multiModeButtonClicked);
            }
            else {
              extendedPlayPanel.hide();
              button.on('click', modeClicked);
            }

            button.data('disciplines', mode.disciplines);
            button.data('target', mode.target);
            var disciplineStr = '';
            if (mode.disciplines != null) {
              for (var d of mode.disciplines)
                disciplineStr += DISCIPLINE_CHARS[d];
            }
            button.children('.discipline').text(disciplineStr);
            button.children('.mode-text').text(mode.text);
            button.appendTo(modeContainer);

            if (mode.target == 'SELF') {
              if (!minionsConfigured) {
                configureMinionButtons();
                minionsConfigured = true;
              }
            }
            else {
              allModesTargetActingMinion = false;
            }
          }
          $('#cardModal .who-is-playing-panel').toggle(allModesTargetActingMinion);
        }
        if (!minionsConfigured) $('who-is-playing-panel').hide();
        $('#cardModal .loading').hide();
        $('#cardModal .loaded').show();
        tippy.hideAll({ duration: 0 });
        $('#cardModal').modal('show');
      }
  });
}
function configureMinionButtons() {
  var minionContainer = $('#cardModal .card-minions');
  minionContainer.empty();

  var minionTemplate = $('#cardModal .templates .card-minion');
  //TODO fetch from forthcoming minion API
  var minions = [
    {index: 1, name: 'Suzy', type: 'vampire', blood: 3, capacity: 7, locked: true},
    {index: 3, name: 'Ambrosio Luis Monçada, Plenipotentiary', type: 'vampire', blood: 10, capacity: 10, locked: false},
    {index: 10, name: 'Reanimated Corpse', type: 'ally', life: 2, locked: false, label: 'Sammy'}
  ];
  for (var i = 0; i < minions.length; ++i) {
    var minion = minions[i];
    console.log(minion);
    var button = minionTemplate.clone();
    button.data('region-index', minion.index);
    button.children('.minion-name').text(minion.name);
    var life = button.children('.minion-life');
    switch (minion.type) {
      case 'vampire':
        life.text(minion.blood + ' / ' + minion.capacity);
        life.removeClass('life').addClass('blood');
        break;
      case 'ally':
        life.text(minion.life);
        life.removeClass('blood').addClass('life');
        break;
      default:
        console.log('Card Modal: unknown minion type: ' + minion.type);
        break;
    }
    button.children('.minion-locked').toggle(minion.locked);
    var label = button.children('.minion-label');
    label.text(minion.label);
    label.toggle(minion.label != null);
    button.children('.minion-index').text('(#' + minion.index + ')');
    button.on('click', minionClicked);
    button.appendTo(minionContainer);
  }
}
function minionClicked(event) {
  var button = $(event.target).closest('button');
  var minions = $('#cardModal .card-minions');
  minions.children().removeClass('active');

  var extendedPlayPanel = $('#cardModal .extended-play-panel');
  var multiMode = extendedPlayPanel.css('display') != 'none';
  var modesSelected = $('#cardModal .card-modes button.active');
  if (!multiMode && modesSelected.length > 0) {
    //Not needed for visual effects or state change.  Only needed in case the
    //card play is happening now, before the state change takes effect normally.
    button.addClass('active');
    playCard({target: modesSelected.eq(0)});
  }
}
function modeClicked(event) {
  var button = $(event.target).closest('button');
  var minionPanel = $('#cardModal .who-is-playing-panel');
  minionPanel.toggle(button.data('target') == 'SELF');

  if (minionPanel.css('display') == 'none'
      || $('#cardModal .card-minions button.active').length > 0) {
    playCard(event);
  }
  else {
    $('#cardModal .card-modes button').removeClass('active');
  }
}
function playCardCommand(disciplines, target) {
  var modal = $('#cardModal');
  var handIndex1 = modal.data('hand-index') + 1;
  var doNotReplace = modal.data('do-not-replace');
  return 'play ' + handIndex1
         + (disciplines ? ' @ ' + disciplines.join(',') : '')
         + (target == 'READY_REGION' ? ' ready' : '')
         + (target == 'SELF' ? ' re ' + $('#cardModal .card-minions button.active').data('region-index') : '')
         + (doNotReplace ? '' : ' draw');
}
function playCard(event) {
  var button = $(event.target).closest('button'); //target might be inner p
  var disciplines = [];
  var target = null;
  if (button.attr('id') == 'cardModalPlayButton') { //Multi-mode cards
    $('#cardModal .card-modes button.active')
      .each(function() { disciplines = disciplines.concat($(this).data('disciplines')); });
    target = $('#cardModal .card-modes button.active:first').data('target');
  }
  else { //Single-mode cards
    disciplines = button.data('disciplines');
    target = button.data('target');
  }

  var command = playCardCommand(disciplines, target);
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
function multiModeButtonClicked(event) {
  var button = $(event.target).closest('button');
  var delta = button.hasClass('active') ? -1 : 1;
  var modes = $('#cardModal .card-modes button.active').length + delta;
  var playButton = $('#cardModalPlayButton');
  playButton.prop('disabled', modes < 1);
  playButton.text(modes < 1 ? 'Select one or more disciplines' : 'Play');
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
