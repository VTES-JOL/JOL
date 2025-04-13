"use strict";
var CLAN_CHARS = {
    abomination: 'A', ahrimane: 'B', akunanse: 'C', assamite: 'n', baali: 'E',
    blood_brother: 'F', brujah: 'o', brujah_antitribu: 'H', caitiff: 'I',
    daughter_of_cacophony: 'J', follower_of_set: 'r', gangrel: 'p',
    gangrel_antitribu: 'M', gargoyle: 'N', giovanni: 'O', guruhi: 'P',
    harbinger_of_skulls: 'Q', ishtarri: 'R', kiasyd: 'S', lasombra: 'w',
    malkavian: 'q', malkavian_antitribu: 'V', nagaraja: 'W', nosferatu: 's',
    nosferatu_antitribu: 'Y', hecata: 'y', osebo: 'Z', pander: 'a', ravnos: 'x',
    salubri: 'c', salubri_antitribu: 'd', samedi: 'e', toreador: 't',
    toreador_antitribu: 'g', tremere: 'u', tremere_antitribu: 'i',
    true_brujah: 'j', tzimisce: 'k', ventrue: 'v', ventrue_antitribu: 'm',
    avenger: '1', defender: '2', innocent: '3', judge: '4', martyr: '5',
    redeemer: '6', visionary: '7'
};

function cardTypeCSSClass(cardType) {
    return cardType.toLowerCase().replace(' ', '_').replace('/', ' ');
}

function clanToKey(clan) {
    return clan.toLowerCase().replace(/ /g, '_');
}

function showPlayCardModal(event) {
    let playCardModal = $("#playCardModal");
    playCardModal.find(".loaded").hide();
    playCardModal.find(".loading").show();
    let eventParent = $(event.target).parents(".list-group-item");
    let cardId = eventParent.data('card-id');
    let coordinates = eventParent.data('coordinates');
    let region = eventParent.closest('[data-region]').data('region');
    if (cardId) {
        $.get({
            dataType: "json",
            url: "https://static.deckserver.net/json/" + cardId, success: function (card) {
                playCardModal.data('hand-coord', coordinates);
                playCardModal.data('region', region);
                playCardModal.data('do-not-replace', region === "research" ? true : card.doNotReplace);
                playCardModal.find(".card-name").text(card.displayName);
                playCardModal.find(".card-type")
                    .removeClass()
                    .addClass('icon card-type ' + cardTypeCSSClass(card.type));
                playCardModal.find(".preamble").text(card.preamble || "");
                playCardModal.find(".burn-option").toggle(card.burnOption || "");

                let clan = playCardModal.find(".card-clan");
                clan.empty();
                if (card.clans != null) {
                    for (let c of card.clans)
                        clan.append($("<span/>").addClass("clan").addClass(clanToKey(c)));
                }

                var costText = null;
                if (card.cost != null) {
                    let value = card.cost.split(" ")[0];
                    let type = card.cost.split(" ")[1];
                    costText = "Cost: <span class='icon " + type + value + "'></span>";
                }
                playCardModal.find(".card-cost").html(costText);

                let  modeContainer = playCardModal.find(".card-modes");
                modeContainer.empty();

                if (card.modes && card.modes.length > 0) {
                    let modeTemplate = playCardModal.find(".templates .card-mode");
                    for (let i = 0; i < card.modes.length; ++i) {
                        let mode = card.modes[i];
                        let button = modeTemplate.clone();

                        button.data('disciplines', mode.disciplines);
                        button.data('target', mode.target);

                        let extendedPlayPanel = playCardModal.find(".extended-play-panel");
                        if (card.multiMode) {
                            extendedPlayPanel.show();
                            let playButton = $('#playCardModalPlayButton');
                            playButton.prop('disabled', true);
                            playButton.text('Select one or more disciplines');
                            button.on('click', multiModeButtonClicked);
                        } else {
                            extendedPlayPanel.hide();
                            button.on('click', modeClicked);
                        }

                        let disciplineSpan = button.children('.discipline');
                        disciplineSpan.empty();
                        if (mode.disciplines != null) {
                            for (let d of mode.disciplines) {
                                disciplineSpan.append($("<span/>").addClass("icon").addClass(d));
                            }
                        }

                        button.children('.mode-text').html(mode.text);
                        button.appendTo(modeContainer);
                    }
                }
                playCardModal.find(".loading").hide();
                playCardModal.find(".loaded").show();
                tippy.hideAll({duration: 0});
                playCardModal.modal('show');
            }
        });
    }
}

function modeClicked(event) {
    let button = $(event.target).closest('button');
    let target = button.data('target');
    if (target === 'MINION_YOU_CONTROL' || target === 'SELF' || target === 'SOMETHING')
        showTargetPicker(target);
    else playCard(event);
}

function showTargetPicker(target) {
    let picker = $('#targetPicker');
    $('#targetPicker .card-name').text($('#playCardModalLabel').text());
    $('#targetPicker .card-type')
        .removeClass()
        .addClass($('#playCardModal .card-type').get(0).className);
    $('#targetPicker .modal-body').text(
        target === 'SELF' ? 'Who is playing this card?' : 'Pick target.');
    picker.show();

    let usePlayerSelector = target === 'MINION_YOU_CONTROL' || target === 'SELF';
    //"player" from js/ds.js
    let playerSelector = usePlayerSelector ? '[data-player="' + player + '"]' : '';
    let playerDiv = $('#state .player' + playerSelector);
    let scrollTo =
        playerDiv.offset().top
        - picker.get(0).getBoundingClientRect().bottom;
    window.scrollTo(0, scrollTo);

    $('#playCardModal').modal('hide');
}

function cardOnTableClicked(event) {
    let picker = $('#targetPicker');
    if (picker.css('display') !== 'none')
        return pickTarget(event);
    return showCardModal(event);
}

function pickTarget(event) {
    let picker = $('#targetPicker');
    if (picker.css('display') === 'none') {
        return;
    }
    let targetAnchor = $(event.target).parents(".list-group-item");
    let player = targetAnchor.closest('[data-player]').data('player').split(' ', 1)[0];
    let region = targetAnchor.closest('[data-region]').data('region');
    let coords = targetAnchor.data('coordinates');
    let modal = $('#playCardModal');
    modal.data('target', player + ' ' + region + ' ' + coords);
    tippy.hideAll({duration: 0});

    let modesSelected = $('#playCardModal .card-modes button.active');
    playCard({target: modesSelected.eq(0)});
    closeTargetPicker();
    return false;
}

function closeTargetPicker() {
    $('#targetPicker').hide();
}

function playCardCommand(disciplines, target) {
    let modal = $('#playCardModal');
    let handIndex = modal.data('hand-coord');
    let doNotReplace = modal.data('do-not-replace');
    let region = modal.data('region');
    let getTargetFromModal = target === 'MINION_YOU_CONTROL' || target === 'SELF' || target === 'SOMETHING';
    return 'play ' + region + ' ' + handIndex
        + (disciplines ? ' @ ' + disciplines.join(',') : '')
        + (target === 'READY_REGION' ? ' ready' : '')
        + (target === 'REMOVE_FROM_GAME' ? ' rfg' : '')
        + (target === 'INACTIVE_REGION' ? ' inactive' : '')
        + (getTargetFromModal ? ' ' + modal.data('target') : '')
        + (doNotReplace ? '' : ' draw');
}

function playCard(event) {
    let button = $(event.target).closest('button'); //target might be inner p
    let disciplines = [];
    let target = null;
    if (button.attr('id') === 'playCardModalPlayButton') { //Multi-mode cards
        $('#playCardModal .card-modes button.active')
            .each(function () {
                disciplines = disciplines.concat($(this).data('disciplines'));
            });
        target = $('#playCardModal .card-modes button.active:first').data('target');
    } else { //Single-mode cards
        disciplines = button.data('disciplines');
        target = button.data('target');
    }

    let command = playCardCommand(disciplines, target);
    sendCommand(command);
    $('#playCardModal').modal('hide');
    return false;
}

function discard(replace = true) {
    let modal = $('#playCardModal');
    let handIndex = modal.data('hand-coord');
    let command = 'discard ' + handIndex + (replace ? ' draw' : '');
    sendCommand(command);
    $('#playCardModal').modal('hide');
    return false;
}

function multiModeButtonClicked(event) {
    let modes = $('#playCardModal .card-modes button.active').length;
    let playButton = $('#playCardModalPlayButton');
    playButton.prop('disabled', modes < 1);
    playButton.text(modes < 1 ? 'Select one or more disciplines' : 'Play');
}

function showCardModal(event) {
    $('#cardModal .loaded').hide();
    $('#cardModal .loading').show();
    let target = $(event.target).closest('[data-coordinates]');
    let controller = target.closest('[data-player]').data('player');
    let controllerPool = target.closest('[data-pool]').data('pool');
    let region = target.closest('[data-region]').data('region');
    let isChild = !target.closest('ol')[0].className.includes('region');
    let coordinates = target.data('coordinates');
    let cardId = target.data('card-id');
    let capacity = target.data('capacity');
    let counters = target.data('counters');
    let label = target.data('label');
    let locked = target.data('locked');
    let votes = target.data('votes');
    let contested = target.data('contested');
    let minion = target.data("minion");
    let owner = controller === player;
    if (cardId) {
        $.get({
            dataType: "json",
            url: "https://static.deckserver.net/json/" + cardId, success: function (card) {
                var modal = $('#cardModal');
                modal.data('controller', controller);
                modal.data('region', region);
                modal.data('coordinates', coordinates);

                $('#cardModal .card-name').text(card.displayName);
                $('#cardModal .card-label').text(label).toggle(label.length > 0);
                $('#cardModal .card-text').html(card.originalText);
                $('#cardModal .votes').text(votes).addClass("badge rounded-pill text-bg-warning").toggle(votes > 0 || votes === 'P');

                let clan = $("#cardModal .card-clan");
                clan.empty();
                if (card.clans != null) {
                    for (let c of card.clans)
                        clan.append($("<span/>").addClass("clan").addClass(clanToKey(c)));
                }

                let disciplineSpan = $('#cardModal .discipline');
                disciplineSpan.empty();
                if (card.disciplines != null) {
                    for (let d of card.disciplines) {
                        disciplineSpan.append($("<span/>").addClass("icon").addClass(d));
                    }
                }

                //If this is our inactive region, show capacity required to influence out.
                //player is a global from ds.js - the logged-in player
                if (controller === player && capacity === -1 && card.capacity != null)
                    capacity = card.capacity;
                setCounters(counters, capacity, card.type);
                setPool(controllerPool);

                $('#cardModal .transfers').removeClass("d-none");
                $('#cardModal .counters').removeClass("d-none");
                $('#cardModal button').show();
                $(`#cardModal button[data-region]`).each(function () {
                    let showThis = $(this).data("region").split(" ").includes(region);
                    if (!showThis) {
                        $(this).hide();
                    }
                })

                $(`#cardModal button[data-lock-state][data-lock-state!="${locked ? "locked" : "unlocked"}"]`).hide();
                $(`#cardModal button[data-contested][data-contested!="${contested}"]`).hide();

                if (!owner) {
                    $('#cardModal .transfers').addClass("d-none");
                    $("#cardModal button[data-owner-only]").hide();
                }

                if (isChild) {
                    $(`#cardModal button[data-top-level-only]`).hide();
                }

                if (minion) {
                    $(`#cardModal button[data-non-minion-only]`).hide();
                } else {
                    $('#cardModal .transfers').addClass("d-none");
                    $(`#cardModal button[data-minion-only]`).hide();
                }

                if (region === "ashheap") {
                    $('#cardModal .counters').addClass("d-none");
                }

                $('#cardModal .loading').hide();
                $('#cardModal .loaded').show();
                tippy.hideAll({duration: 0});
                $('#cardModal').modal('show');
            }
        });
    }
}

function setCounters(current, capacity, cardType = null) {
    let counterBar = $('#cardModal .counters');
    counterBar.empty();
    if (cardType != null) {
        counterBar.removeClass('text-bg-danger text-bg-success text-bg-secondary');
        let class_ = null;
        switch (cardType.toUpperCase()) {
            case 'VAMPIRE':
                class_ = 'text-bg-danger';
                break;
            case 'RETAINER':
            case 'ALLY':
            case 'IMBUED':
                class_ = 'text-bg-success';
                break;
            default:
                class_ = 'text-bg-secondary';
                break;
        }
        counterBar.addClass(class_);
    }

    let negativeCounter = $("<i/>").addClass("bi bi-dash-lg").on('click', removeCounter);
    let plusCounter = $("<i/>").addClass("bi bi-plus-lg").on('click', addCounter);
    let text = capacity > 0 ? `${current} / ${capacity}` : `${current}`;
    counterBar.append(negativeCounter, text, plusCounter);

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
        modal.data('region').split(' ')[0], //ready-region > ready
        modal.data('coordinates'),
        commandTail);
    var command = parts.join(' ');
    sendCommand(command.trim(), message);
    if (closeModal) $('#cardModal').modal('hide');
    return false;
}

function lock(message = '') {
    return doCardCommand('lock', message);
}

function unlock(message = '') {
    return doCardCommand('unlock', message);
}

function contest(flag) {
    return doCardCommand("contest", "", flag ? "" : "clear");
}

function bleed() {
    return lock('Bleed');
}

function hunt() {
    return lock('Hunt');
}

function torpor() {
    var controller = $('#cardModal').data('controller').split(' ', 2)[0];
    return doCardCommand("move", "", controller + " torpor")
}

function goAnarch() {
    return lock('Go anarch');
}

function leaveTorpor() {
    return lock('Leave Torpor');
}

function burn() {
    return doCardCommand('burn');
}

function playVamp() {
    var modal = $('#cardModal');
    var command = `influence ${modal.data('coordinates')}`;
    sendCommand(command);
    modal.modal('hide');
    return false;
}

function block() {
    var name = $('#cardModal .card-name').text();
    var message = name + ' blocks';
    sendChat(message);
    $('#cardModal').modal('hide');
    return false;
}

function moveHand() {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    let command = `move ${region} ${modal.data('coordinates')} hand`;
    sendCommand(command);
    modal.modal('hide');
    return false;
}

function moveLibrary(top) {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    let command = `move ${region} ${modal.data('coordinates')} library`;
    if (top) {
        command += " top";
    }
    sendCommand(command);
    modal.modal('hide');
    return false;
}

function moveUncontrolled() {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    let command = `move ${region} ${modal.data('coordinates')} inactive`;
    sendCommand(command);
    modal.modal('hide');
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
