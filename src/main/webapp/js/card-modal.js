"use strict";
var CLAN_CHARS = {
  abomination: 'A', ahrimane: 'B', akunanse: 'C', assamite: 'n', baali: 'E',
  blood_brother: 'F', brujah: 'o', brujah_antitribu: 'H', caitiff: 'I',
  daughter_of_cacophony: 'J', follower_of_set: 'r', gangrel: 'p',
  gangrel_antitribu: 'M', gargoyle: 'N', giovanni: 'O', guruhi: 'P',
  harbinger_of_skulls: 'Q', ishtarri: 'R', kiasyd: 'S', lasombra: 'w',
  malkavian: 'q', malkavian_antitribu: 'V', nagaraja: 'W', nosferatu: 's',
  nosferatu_antitribu: 'Y', osebo: 'Z', pander: 'a', ravnos: 'x',
  salubri: 'c', salubri_antitribu: 'd', samedi: 'e', toreador: 't',
  toreador_antitribu: 'g', tremere: 'u', tremere_antitribu: 'i',
  true_brujah: 'j', tzimisce: 'k', ventrue: 'v', ventrue_antitribu: 'm',
  avenger: '1', defender: '2', innocent: '3', judge: '4', martyr: '5',
  redeemer: '6', visionary: '7'
};
var DISCIPLINE_CHARS = {
  aus: 'a', AUS: 'A', obe: 'b', OBE: 'B', cel: 'c', CEL: 'C',
  dom: 'd', DOM: 'D', dem: 'e', DEM: 'E', for: 'f', FOR: 'F',
  san: 'g', SAN: 'G', thn: 'h', THN: 'H', ani: 'i', ANI: 'I',
  pro: 'j', PRO: 'J', chi: 'k', CHI: 'K', val: 'l', VAL: 'L',
  mel: 'm', MEL: 'M', nec: 'n', NEC: 'N', obf: 'o', OBF: 'O',
  pot: 'p', POT: 'P', qui: 'q', QUI: 'Q', pre: 'r', PRE: 'R',
  ser: 's', SER: 'S', tha: 't', THA: 'T', vic: 'v', VIC: 'V',
  vis: 'u', VIS: 'U', abo: 'w', ABO: 'W', myt: 'x', MYT: 'X',
  dai: 'y', DAI: 'Y', spi: 'z', SPI: 'Z', obt: '$', OBT: '£',
  tem: '?', TEM: '!', str: 'à', STR: 'á', mal: 'â', MAL: 'ã',
  obl: 'ø', OBL: 'Ø', FLIGHT: '^',
  inn: '#', jud: "%", viz: ")", ven: "(", def: "@", mar: "&", red: "*"
};
function cardTypeCSSClass(cardType) {
  return cardType.toLowerCase().replace(' ', '_').replace('/', ' ');
}
function clanToKey(clan) {
  return clan.toLowerCase().replace(/ /g, '_');
}
function showPlayCardModal(event) {
  $('#playCardModal .loaded').hide();
  $('#playCardModal .loading').show();
  //Show modal immediately with Loading text
  //$('#playCardModal').modal('show');
  var cardId = $(event.target).data('card-id');
  var handCoord = $(event.target).data('coordinates');
  $.get({
      dataType: "json",
      url: "rest/api/cards/" + cardId, success: function(card) {
        var modal = $('#playCardModal');
        modal.data('hand-coord', handCoord);
        modal.data('do-not-replace', card.doNotReplace);

        $('#playCardModal .card-name').text(card.displayName);
        $('#playCardModal .card-type')
          .removeClass()
          .addClass('card-type ' + cardTypeCSSClass(card.type));
        $('#playCardModal .preamble').text(card.preamble);
        $('#playCardModal .burn-option').toggle(card.burnOption);

        var clanText = '';
        if (card.clans != null) {
          for (var c of card.clans)
            clanText += CLAN_CHARS[clanToKey(c)];
        }
        $('#playCardModal .card-clan').text(clanText);

        var costText = null;
        if (card.cost != null){
          let value = card.cost.split(" ")[0];
          let type = card.cost.split(" ")[1];
          costText = "Cost: <span class='discipline cost " + type + value + "'></span>";
        }
        $('#playCardModal .card-cost').html(costText);

        var modeContainer = $('#playCardModal .card-modes');
        modeContainer.empty();

        if (card.modes && card.modes.length > 0) {
          var modeTemplate = $('#playCardModal .templates .card-mode');
          for (var i = 0; i < card.modes.length; ++i) {
            var mode = card.modes[i];
            var button = modeTemplate.clone();

            var extendedPlayPanel = $('#playCardModal .extended-play-panel');
            if (card.multiMode) {
              extendedPlayPanel.show();
              var playButton = $('#playCardModalPlayButton');
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
            var hackDisciplineMargin = false;
            if (mode.disciplines != null) {
              for (var d of mode.disciplines) {
                disciplineStr += DISCIPLINE_CHARS[d];
              }
            }
            var disciplineSpan = button.children('.discipline');
            disciplineSpan.text(disciplineStr);
            disciplineSpan.css('margin-right', hackDisciplineMargin ? '15px' : '');
            button.children('.mode-text').html(mode.text);
            button.appendTo(modeContainer);
          }
        }
        $('#playCardModal .loading').hide();
        $('#playCardModal .loaded').show();
        tippy.hideAll({ duration: 0 });
        $('#playCardModal').modal('show');
      }
  });
}
function modeClicked(event) {
  var button = $(event.target).closest('button');
  var target = button.data('target');
  if (target == 'MINION_YOU_CONTROL' || target == 'SELF' || target == 'SOMETHING')
    showTargetPicker(target);
  else playCard(event);
}
function showTargetPicker(target) {
  var picker = $('#targetPicker');
  $('#targetPicker .card-name').text($('#playCardModalLabel').text());
  $('#targetPicker .card-type')
    .removeClass()
    .addClass($('#playCardModal .card-type').get(0).className);
  $('#targetPicker .modal-body').text(
    target == 'SELF' ? 'Who is playing this?' : 'Pick target.');
  picker.show();

  var usePlayerSelector = target == 'MINION_YOU_CONTROL' || target == 'SELF';
  //"player" from js/ds.js
  var playerSelector = usePlayerSelector ? '[data-player="' + player +'"]' : '';
  var playerDiv = $('#state .player' + playerSelector);
  var scrollTo =
    playerDiv.offset().top
    - picker.get(0).getBoundingClientRect().bottom;
  window.scrollTo(0, scrollTo);

  $('#playCardModal').modal('hide');
}
function cardOnTableClicked(event) {
  var picker = $('#targetPicker');
  if (picker.css('display') != 'none')
    return pickTarget(event);
  return showCardModal(event);
}
function pickTarget(event) {
  var picker = $('#targetPicker');
  if (picker.css('display') == 'none') {
    return;
  }
  var targetAnchor = $(event.target).closest('a');
  var player = targetAnchor.closest('[data-player]').data('player').split(' ', 1)[0];
  var region = targetAnchor.closest('[data-region]').data('region').split('-', 1)[0];
  var coords = targetAnchor.data('coordinates');
  var modal = $('#playCardModal');
  modal.data('target', player + ' ' + region + ' ' + coords);
  tippy.hideAll({ duration: 0 });

  var modesSelected = $('#playCardModal .card-modes button.active');
  playCard({target: modesSelected.eq(0)});
  closeTargetPicker();
  return false;
}
function closeTargetPicker() {
  $('#targetPicker').hide();
}
function playCardCommand(disciplines, target) {
  var modal = $('#playCardModal');
  var handIndex = modal.data('hand-coord');
  var doNotReplace = modal.data('do-not-replace');
  var getTargetFromModal = target == 'MINION_YOU_CONTROL' || target == 'SELF' || target == 'SOMETHING';
  return 'play ' + handIndex
         + (disciplines ? ' @ ' + disciplines.join(',') : '')
         + (target == 'READY_REGION' ? ' ready' : '')
         + (target == 'REMOVE_FROM_GAME' ? ' rfg' : '')
         + (target == 'INACTIVE_REGION' ? ' inactive' : '')
         + (getTargetFromModal ? ' ' + modal.data('target') : '')
         + (doNotReplace ? '' : ' draw');
}
function playCard(event) {
  var button = $(event.target).closest('button'); //target might be inner p
  var disciplines = [];
  var target = null;
  if (button.attr('id') == 'playCardModalPlayButton') { //Multi-mode cards
    $('#playCardModal .card-modes button.active')
      .each(function() { disciplines = disciplines.concat($(this).data('disciplines')); });
    target = $('#playCardModal .card-modes button.active:first').data('target');
  }
  else { //Single-mode cards
    disciplines = button.data('disciplines');
    target = button.data('target');
  }

  var command = playCardCommand(disciplines, target);
  sendCommand(command);
  $('#playCardModal').modal('hide');
  return false;
}
function discard(replace = true) {
  var modal = $('#playCardModal');
  var handIndex = modal.data('hand-coord');
  var command = 'discard ' + handIndex + (replace ? ' draw' : '');
  sendCommand(command);
  $('#playCardModal').modal('hide');
  return false;
}
function nextCard() {
  var modal = $('#playCardModal');
  var nextIndex = modal.data('hand-coord') + 1;
  var nextCard = $('#hand .card-list a[data-coordinates=' + nextIndex + ']')
  if (nextCard.length == 0) nextCard = $('#hand .card-list a:first')
  nextCard.click();
  return false;
}
function previousCard() {
  var modal = $('#playCardModal');
  var nextIndex = modal.data('hand-coord') - 1;
  var nextCard = null;
  if (nextIndex < 1) nextCard = $('#hand .card-list a:last');
  else nextCard = $('#hand .card-list a[data-coordinates=' + nextIndex + ']')
  nextCard.click();
  return false;
}
function multiModeButtonClicked(event) {
  var button = $(event.target).closest('button');
  var delta = button.hasClass('active') ? -1 : 1;
  var modes = $('#playCardModal .card-modes button.active').length + delta;
  var playButton = $('#playCardModalPlayButton');
  playButton.prop('disabled', modes < 1);
  playButton.text(modes < 1 ? 'Select one or more disciplines' : 'Play');
}
function showCardModal(event) {
  $('#cardModal .loaded').hide();
  $('#cardModal .loading').show();
  var target = $(event.target);
  var controller = target.closest('[data-player]').data('player');
  var controllerPool = target.closest('[data-pool]').data('pool');
  var region = target.closest('[data-region]').data('region');
  var isChild = !target.closest('ol')[0].id.startsWith('region');
  var coordinates = target.data('coordinates');
  var cardId = target.data('card-id');
  var capacity = target.data('capacity');
  var counters = target.data('counters');
  var label = target.data('label');
  var locked = target.data('locked');
  var votes = target.data('votes');
  var contested = target.data('contested');
  $.get({
      dataType: "json",
      url: "rest/api/cards/" + cardId, success: function(card) {
        var modal = $('#cardModal');
        modal.data('controller', controller);
        modal.data('region', region);
        modal.data('coordinates', coordinates);

        $('#cardModal .card-name').text(card.displayName);
        $('#cardModal .card-label').text(label).toggle(label.length > 0);
        $('#cardModal .card-text').text(card.originalText);
        $('#cardModal .votes').text(votes).toggle(votes > 0 || votes === 'P');

        var clanText = '';
        var clanHoverText = '';
        if (card.clans != null && card.clans.length > 0) {
          var c = card.clans[0];
          clanText = CLAN_CHARS[clanToKey(c)];
          clanHoverText = c;
        }
        $('#cardModal .card-clan').text(clanText).attr('title', clanHoverText);

        var disciplineStr = '';
        if (card.disciplines != null) {
          for (var d of card.disciplines) {
            disciplineStr += DISCIPLINE_CHARS[d];
          }
        }
        var disciplineSpan = $('#cardModal .discipline');
        disciplineSpan.text(disciplineStr);

        //If this is our inactive region, show capacity required to influence out.
        //player is a global from ds.js - the logged-in player
        if (controller == player && capacity == -1 && card.capacity != null)
          capacity = card.capacity;
        setCounters(counters, capacity, card.type);

        if (controller == player) { //player is a global from ds.js - the logged-in player
          setPool(controllerPool);
          $('#cardModal .transfers').show();
          $('#cardModal .counters').css('position', 'absolute');
        } else {
          $('#cardModal .transfers').hide();
          //Put the counter vial back in normal flow
          $('#cardModal .counters').css('position', 'relative');
        }

        $('#cardModal button').show();
        // $(`#cardModal button[data-region][data-region!="${region}"]`).hide();
        $(`#cardModal button[data-region]`).each(function() {
          let showThis = $(this).data("region").split(" ").includes(region);
          if (!showThis) { $(this).hide(); }
        })
        $(`#cardModal button[data-lock-state][data-lock-state!="${locked ? "locked" : "unlocked"}"]`).hide();
        $(`#cardModal button[data-contested][data-contested!="${contested}"]`).hide();
        //This will be a good enhancement once we can identify non-crypt cards that become minions,
        //like Embrace, Call the Great Beast, and Jake Washington.
        //$(`#cardModal button[data-type][data-type!="${card.type.toUpperCase()}"]`).hide();

        if (isChild) {
          $(`#cardModal button[data-top-level-only]`).hide();
        }

        $('#cardModal .loading').hide();
        $('#cardModal .loaded').show();
        tippy.hideAll({ duration: 0 });
        $('#cardModal').modal('show');
      }
  });
}
function setCounters(current, capacity, cardType = null) {
  var counterBar = $('#cardModal .counters');
  if (cardType != null) {
    counterBar.removeClass('blood life');
    var class_ = null;
    switch (cardType.toUpperCase()) {
        case 'VAMPIRE': class_ = 'blood'; break;
        case 'RETAINER':
        case 'ALLY':
        case 'IMBUED': class_ = 'life'; break;
    }
    if (class_ != null) counterBar.addClass(class_);
  }

  var text = capacity > 0 ? `- ${current} / ${capacity} +` : `- ${current} +`;
  counterBar.text(text);

  var modal = $('#cardModal');
  modal.data('counters', current);
  modal.data('capacity', capacity);
}
function setPool(pool) {
  $('#cardModal .card-modal-pool').text(`${pool} pool`);
  $('#cardModal').data('pool', pool);
}
function doCardCommand(commandKeyword, message = '', commandTail = '', closeModal = true, omitPlayer = false) {
  var modal = $('#cardModal');
  var parts = new Array(5);
  parts.push(commandKeyword);
  if (!omitPlayer) {
    var player = modal.data('controller').split(' ', 2)[0]; //names with spaces do not work
    parts.push(player);
  }
  parts.push(
    modal.data('region').split('-')[0], //ready-region > ready
    modal.data('coordinates'),
    commandTail);
  var command = parts.join(' ');
  sendCommand(command, message);
  if (closeModal) $('#cardModal').modal('hide');
  return false;
}
function lock(message = '') { return doCardCommand('lock', message); }
function unlock(message = '') { return doCardCommand('unlock', message); }

function contest(flag) {
  return doCardCommand("contest", "", flag ? "" : "clear");
}
function bleed() { return lock('Bleed'); }
function hunt() { return lock('Hunt'); }

function torpor() {
  var controller = $('#cardModal').data('controller').split(' ', 2)[0];
  return doCardCommand("move", "", controller + " torpor")}
function goAnarch() { return lock('Go anarch'); }
function leaveTorpor() { return lock('Leave torpor'); }
function burn() { return doCardCommand('burn'); }
function playVamp() {
  var modal = $('#cardModal');
  var command = `play vamp ${modal.data('coordinates')}`;
  sendCommand(command);
  $('#cardModal').modal('hide');
  return false;
}
function block() {
  var name = $('#cardModal .card-name').text();
  var message = name + ' blocks';
  sendChat(message);
  $('#cardModal').modal('hide');
  return false;
}
function removeCounter(doCommand = true) {
  var modal = $('#cardModal');
  var counters = modal.data('counters');
  if (counters > 0) {
    var capacity = modal.data('capacity');
    if (doCommand) doCardCommand('blood', '', '-1', false);
    setCounters(counters - 1, capacity);
  }
  return false;
}
function addCounter(doCommand = true) {
  var modal = $('#cardModal');
  var counters = modal.data('counters');
  var capacity = modal.data('capacity');
  if (doCommand) doCardCommand('blood', '', '+1', false);
  setCounters(counters + 1, capacity);
  return false;
}
var countersLastClicked = null;
function vialClicked(event, upClicked, downClicked) {
    //These events were firing 3 times when double-clicked.
    //Ignore the duplicate click event that comes through with mozInputSource = MOZ_SOURCE_TOUCH
    //on the Mac mouse when double-clicking.
    if (event.detail > 1 && countersLastClicked != null) {
        countersLastClicked = null;
        return false;
    }
    var bounds = event.target.getBoundingClientRect();
    var x = event.clientX - bounds.left;
    if (x >= event.target.clientWidth / 2)
        upClicked();
    else downClicked();
    countersLastClicked = event.timeStamp;
    return false;
}
function countersClicked(event) {
    return vialClicked(event, addCounter, removeCounter);
}
function transferToCard() {
  var modal = $('#cardModal');
  var pool = modal.data('pool');
  doCardCommand('transfer', '', '+1', false, true);
  setPool(pool - 1);
  addCounter(false);
  return false;
}
function transferToPool() {
  var modal = $('#cardModal');
  var counters = modal.data('counters');
  if (counters > 0) {
    var pool = modal.data('pool');
    doCardCommand('transfer', '', '-1', false, true);
    setPool(pool + 1);
    removeCounter(false);
  }
  return false;
}
function poolClicked(event) {
    return vialClicked(event, transferToPool, transferToCard);
}
