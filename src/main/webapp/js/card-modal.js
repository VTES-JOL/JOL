"use strict";
var CLAN_CHARS = {
  abomination: 'A', arihmane: 'B', akunanse: 'C', assamite: 'D', baali: 'E',
  blood_brothers: 'F', brujah: 'G', brujah_antitribu: 'H', caitiff: 'I',
  daughter_of_cacophony: 'J', follower_of_set: 'K', gangrel: 'L',
  gangrel_antitribu: 'M', gargoyle: 'N', giovanni: 'O', guruhi: 'P',
  harbinger_of_skulls: 'Q', ishtarri: 'R', kiasyd: 'S', lasombra: 'T',
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
  san: 'g', SAN: 'G', thn: 'h', THN: 'H', vic: 'v', VIC: 'V',
  pro: 'j', PRO: 'J', chi: 'k', CHI: 'K', val: 'l', VAL: 'L',
  mel: 'm', MEL: 'M', nec: 'n', NEC: 'N', obf: 'o', OBF: 'O',
  pot: 'p', POT: 'P', qui: 'q', QUI: 'Q', pre: 'r', PRE: 'R',
  ser: 's', SER: 'S', tha: 't', THA: 'T', aus: 'u', AUS: 'U',
  vis: 'i', VIS: 'I', abo: 'w', ABO: 'W', myt: 'x', MYT: 'X',
  dai: 'y', DAI: 'Y', spi: 'z', SPI: 'Z', obt: '*', OBT: '+',
  tem: '(', TEM: ')', str: ':', STR: ';', mal: '<', MAL: '>',
  FLIGHT: '='
};
var DISCIPLINES_NEEDING_SPACING_HACK = ['str', 'STR', 'mal', 'MAL'];
function cardTypeCSSClass(cardType) {
  return cardType.toLowerCase().replace(' ', '_').replace('/', ' ');
}
function clanToKey(clan) {
  return clan.toLowerCase().replace(/ /g, '_');
}
function showCardModal(event) {
  $('#cardModal .loaded').hide();
  $('#cardModal .loading').show();
  //Show modal immediately with Loading text
  //$('#cardModal').modal('show');
  var cardId = $(event.target).data('card-id');
  var handCoord = $(event.target).data('coordinates');
  $.get({
      url: "rest/api/cards/" + cardId, success: function(card) {
        var modal = $('#cardModal');
        modal.data('hand-coord', handCoord);
        modal.data('do-not-replace', card.doNotReplace);

        $('#cardModal .card-name').text(card.displayName);
        $('#cardModal .card-type')
          .removeClass()
          .addClass('card-type ' + cardTypeCSSClass(card.type));
        $('#cardModal .preamble').text(card.preamble);
        $('#cardModal .burn-option').toggle(card.burnOption);

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
            var hackDisciplineMargin = false;
            if (mode.disciplines != null) {
              for (var d of mode.disciplines) {
                disciplineStr += DISCIPLINE_CHARS[d];
                if (DISCIPLINES_NEEDING_SPACING_HACK.includes(d))
                  hackDisciplineMargin = true;
              }
            }
            var disciplineSpan = button.children('.discipline');
            disciplineSpan.text(disciplineStr);
            disciplineSpan.css('margin-right', hackDisciplineMargin ? '15px' : '');
            button.children('.mode-text').text(mode.text);
            button.appendTo(modeContainer);
          }
        }
        $('#cardModal .loading').hide();
        $('#cardModal .loaded').show();
        tippy.hideAll({ duration: 0 });
        $('#cardModal').modal('show');
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
  $('#targetPicker .card-name').text($('#cardModalLabel').text());
  $('#targetPicker .card-type')
    .removeClass()
    .addClass($('#cardModal .card-type').get(0).className);
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

  $('#cardModal').modal('hide');
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
  var modal = $('#cardModal');
  modal.data('target', player + ' ' + region + ' ' + coords);
  tippy.hideAll({ duration: 0 });

  var modesSelected = $('#cardModal .card-modes button.active');
  playCard({target: modesSelected.eq(0)});
  closeTargetPicker();
  return false;
}
function closeTargetPicker() {
  $('#targetPicker').hide();
}
function playCardCommand(disciplines, target) {
  var modal = $('#cardModal');
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
function discard(replace = true) {
  var modal = $('#cardModal');
  var handIndex = modal.data('hand-coord');
  var command = 'discard ' + handIndex + (replace ? ' draw' : '');
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
function nextCard() {
  var modal = $('#cardModal');
  var nextIndex = modal.data('hand-coord') + 1;
  var nextCard = $('#hand .card-list a[data-coordinates=' + nextIndex + ']')
  if (nextCard.length == 0) nextCard = $('#hand .card-list a:first')
  nextCard.click();
  return false;
}
function previousCard() {
  var modal = $('#cardModal');
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
  var modes = $('#cardModal .card-modes button.active').length + delta;
  var playButton = $('#cardModalPlayButton');
  playButton.prop('disabled', modes < 1);
  playButton.text(modes < 1 ? 'Select one or more disciplines' : 'Play');
}
