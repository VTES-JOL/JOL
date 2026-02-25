"use strict";
const CLANS = ["Abomination", "Ahrimane", "Akunanse", "Baali", "Banu Haqim", "Blood Brother", "Brujah", "Brujah Antitribu",
    "Caitiff", "Daughter of Cacophony", "Gangrel", "Gangrel Antitribu", "Gargoyle", "Giovanni", "Guruhi", "Harbinger of Skulls",
    "Ishtarri", "Kiasyd", "Lasombra", "Malkavian", "Malkavian Antitribu", "Nagaraja", "Nosferatu", "Nosferatu Antitribu",
    "Hecata", "Ministry", "Osebo", "Pander", "Ravnos", "Salubri", "Salubri Antitribu", "Samedi", "Toreador", "Toreador Antitribu",
    "Tremere", "Tremere Antitribu", "True Brujah", "Tzimisce", "Ventrue", "Ventrue Antitribu", "Avenger", "Defender", "Innocent",
    "Judge", "Martyr", "Redeemer", "Visionary"];

const PATHS = ['Death and the Soul', 'Power and the Inner Voice', 'Cathari', 'Caine', 'None'];
const SECTS = ["Camarilla", "Sabbat", "Independent", "Laibon", "Anarch", "None"];

function cardTypeCSSClass(cardType) {
    return cardType.toLowerCase().replace(' ', '_').replace('/', ' ');
}

function nameToKey(name) {
    if (name.toLowerCase() === "none") {
        return "";
    }
    return name.toLowerCase().replace(/ /g, '_');
}

// Helpers to convert between display and keys, and to sentence-case tooltips
function sentenceCase(upperOrMixed) {
    if (!upperOrMixed) return "";
    const s = String(upperOrMixed).toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function keyToDisplay(key) {
    if (!key) return "None";
    return key.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
}

function buildIconSpan(kind, display) {
    const key = nameToKey(display);
    const $span = $("<span/>").addClass(kind).addClass(key);
    if (kind === 'clan') {
        // Tooltip should be sentence case of original DESCRIPTION (display)
        $span.attr('title', sentenceCase(display));
    }
    if (kind === 'sect') {
        $span.addClass('sect'); // ensure class hook
        $span.attr('title', sentenceCase(display));
    }
    if (kind === 'path') {
        $span.addClass('path');
        $span.attr('title', sentenceCase(display));
    }
    return $span;
}

function fillSelect($select, options) {
    $select.empty();
    for (const opt of options) {
        const val = nameToKey(opt);
        const text = opt; // keep human-friendly
        $select.append($("<option/>").attr('value', val).text(text));
    }
}

// Toggle between icon and select for a field
function enableInlinePicker(config) {
    const {containerSel, selectSel, values, currentDisplay, minion, onChanged} = config;
    const $container = $(containerSel);
    const $select = $(selectSel);

    // Small helpers to DRY logic and reduce DOM churn
    function createNonePlaceholder() {
        return $('<span/>')
            .addClass('text-muted')
            .css({cursor: 'pointer', 'border-radius': '4px', padding: '0 2px', margin: '0 2px'})
            .attr('title', 'None')
            .html('<i class="bi bi-ban"></i>')
            .on('mouseenter.inlinePicker', function () {
                $(this).css('box-shadow', '0 0 0.25rem 0.15rem rgba(108, 117, 125, 0.6)');
            })
            .on('mouseleave.inlinePicker', function () {
                $(this).css('box-shadow', 'none');
            });
    }

    function renderContainer(kind, display) {
        const hasValue = display && display.toLowerCase() !== 'none';
        if (hasValue) {
            $container.html(buildIconSpan(kind, display));
        } else {
            $container.html(createNonePlaceholder());
        }
    }

    function ensureOptions($sel, opts) {
        // Only (re)build if counts differ or first/last differ (cheap check)
        const existing = $sel[0].options;
        const needsBuild =
            existing.length !== opts.length ||
            (opts.length > 0 &&
                (existing[0]?.value !== nameToKey(opts[0]) ||
                    existing[existing.length - 1]?.value !== nameToKey(opts[opts.length - 1])));
        if (needsBuild) {
            fillSelect($sel, opts);
        }
    }

    function setSelectValue($sel, key) {
        if ($sel.val() !== key) {
            $sel.val(key);
        }
    }

    // Determine kind based on container
    const kind = $container.hasClass('card-clan') ? 'clan'
        : ($container.hasClass('card-path') ? 'path' : 'sect');

    // Early bail if not a minion
    if (!minion) {
        $container.addClass('d-none').empty();
        $select.addClass('d-none');
        return;
    }
    $container.removeClass('d-none');

    // Common hover effect for container (Bootstrap secondary-like color)
    $container
        .css('border-radius', '4px')
        .off('mouseenter.inlinePicker mouseleave.inlinePicker')
        .on('mouseenter.inlinePicker', function () {
            $(this).css('box-shadow', '0 0 0.25rem 0.15rem rgba(108, 117, 125, 0.6)');
        })
        .on('mouseleave.inlinePicker', function () {
            $(this).css('box-shadow', 'none');
        });

    // Prepare select once (only if needed), and set current value
    ensureOptions($select, values);
    const currentKey = nameToKey(currentDisplay || "None");
    setSelectValue($select, currentKey);

    // Initial render
    renderContainer(kind, currentDisplay);

    // Click to switch to select
    $container.off('click.inlinePicker').on('click.inlinePicker', function () {
        $container.addClass('d-none');
        $select.removeClass('d-none').focus();
    });

    // Change handler -> send update and re-render
    $select.off('change.inlinePicker').on('change.inlinePicker', function () {
        const newKey = $(this).val();
        const newDisplay = keyToDisplay(newKey);
        const trimmedKey = (newKey || '').split('_', 1)[0];

        if (newDisplay !== currentDisplay) {
            onChanged(newDisplay, trimmedKey);
        }

        // Switch back to icon reflecting new value (or None placeholder)
        $select.addClass('d-none');
        $container.removeClass('d-none');
        renderContainer(kind, newDisplay);
    });

    // Blur -> revert without change
    $select.off('blur.inlinePicker').on('blur.inlinePicker', function () {
        $select.addClass('d-none');
        $container.removeClass('d-none');
    });
}

function showPlayCardModal(event) {
    let playCardModal = $("#playCardModal");
    playCardModal.find(".loaded").hide();
    playCardModal.find(".loading").show();
    let eventParent = $(event.target).parents(".list-group-item");
    let cardId = eventParent.data('card-id');
    let secured = eventParent.data('secured') || false ? "secured/" : "";
    let coordinates = eventParent.data('coordinates');
    let region = eventParent.closest('[data-region]').data('region');
    if (cardId) {
        $.get({
            dataType: "json",
            url: `${BASE_URL}/${secured}json/${cardId}`, success: function (card) {
                playCardModal.data('hand-coord', coordinates);
                playCardModal.data('region', region);
                playCardModal.data('do-not-replace', region === "research" ? true : card.doNotReplace);
                playCardModal.find(".card-name").text(card.displayName);
                playCardModal.find(".card-type")
                    .removeClass()
                    .addClass('icon card-type ' + cardTypeCSSClass(card.type));
                playCardModal.find(".preamble").text(card.preamble || "");
                playCardModal.find(".burn-option").toggle(card.burnOption || "");
                playCardModal.find(".card-sect").text(card.sect || "");
                let path = playCardModal.find(".card-path");
                path.empty();
                if (card.path != null) {
                    path.append($("<span/>").addClass("path").addClass(nameToKey(card.path)));
                }

                let clan = playCardModal.find(".card-clan");
                clan.empty();
                if (card.clans != null) {
                    for (let c of card.clans)
                        clan.append($("<span/>").addClass("clan").addClass(nameToKey(c)));
                }

                var costText = null;
                if (card.cost != null) {
                    let value = card.cost.split(" ")[0];
                    let type = card.cost.split(" ")[1];
                    costText = "Cost: <span class='icon " + type + value + "'></span>";
                }
                playCardModal.find(".card-cost").html(costText);

                let modeContainer = playCardModal.find(".card-modes");
                modeContainer.empty();

                if (card.modes && card.modes.length > 0) {
                    let modeTemplate = playCardModal.find(".templates .card-mode");
                    for (let i = 0; i < card.modes.length; ++i) {
                        let mode = card.modes[i];
                        let button = modeTemplate.clone();

                        if (mode.disciplines && mode.disciplines.length > 0) {
                            button.data('disciplines', mode.disciplines);
                        }
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

    // Close when clicking outside of the picker
    $(document).off('mousedown.targetPicker').on('mousedown.targetPicker', function (e) {
        const $target = $(e.target);
        const clickedInsidePicker = $target.closest('#targetPicker').length > 0;
        const clickedCardOnTable = $target.closest('.list-group-item,[data-coordinates]').length > 0;
        if (!clickedInsidePicker && !clickedCardOnTable) {
            closeTargetPicker();
        }
    });

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
    let secured = target.data('secured') || false ? "secured/" : "";
    let minion = target.data("minion");
    let sect = target.data("sect");
    let path = target.data("path");
    let clan = target.data("clan");
    let owner = controller === player;
    if (cardId) {
        if (profile.imageTooltipPreference) {
            let content = `<img width="350" height="500" src="${BASE_URL}/${secured}images/${cardId}" alt="Loading..."/>`;
            $("#card-image").html(content);
        } else {
            $.get({
                dataType: "html",
                url: `${BASE_URL}/${secured}html/${cardId}`, success: function (data) {
                    let content = `<div class="p-2">${data}</div>`;
                    $("#card-image").html(content);
                }
            });
        }
        $.get({
            dataType: "json",
            url: `${BASE_URL}/${secured}json/${cardId}`, success: function (card) {

                const modal = $('#cardModal');
                // Update fields used for commands
                modal.data('controller', controller);
                modal.data('region', region);
                modal.data('coordinates', coordinates);

                // Set Modal name to card name
                $('#cardModal .card-name').text(card.displayName);
                // Display label
                $('#card-label').val(label);
                // Votes
                $('#cardModal .votes').text(votes).addClass("badge rounded-pill text-bg-warning").toggle(votes > 0 || votes === 'P');

                // Cost
                var costText = null;
                if (card.cost != null) {
                    let value = card.cost.split(" ")[0];
                    let type = card.cost.split(" ")[1];
                    costText = "<span class='icon " + type + value + "'></span>";
                }
                $('#cardModal .card-cost').html(costText);

                // Set counters on card, capacity, and player pool
                if (controller === player && capacity === 0 && card.capacity != null)
                    capacity = card.capacity;
                setCounters(counters, capacity, card.type);
                setPool(controllerPool);

                // Reset buttons to default state
                $('#cardModal .transfers').removeClass("d-none");
                $('#cardModal .counters').removeClass("d-none");
                $('#cardModal button').show();

                //
                $(`#cardModal button[data-region]`).each(function () {
                    let showThis = $(this).data("region").split(" ").includes(region);
                    if (!showThis) {
                        $(this).hide();
                    }
                })
                // Hide buttons not valid for lock state
                $(`#cardModal button[data-lock-state][data-lock-state!="${locked ? "locked" : "unlocked"}"]`).hide();

                // Hide buttons not valid for contested state
                $(`#cardModal button[data-contested][data-contested!="${contested}"]`).hide();

                // Hide buttons intended for owner only
                if (!owner) {
                    $('#cardModal .transfers').addClass("d-none");
                    $("#cardModal button[data-owner-only]").hide();
                }

                // Hide buttons intended for use by top level cards
                if (isChild) {
                    $(`#cardModal button[data-top-level-only]`).hide();
                }

                // Hide buttons intended for use by non-minion cards
                if (minion) {
                    $(`#cardModal button[data-non-minion-only]`).hide();
                } else {
                    $('#cardModal .transfers').addClass("d-none");
                    $(`#cardModal button[data-minion-only]`).hide();
                }

                // Hide counter-buttons if the card is in ashheap
                if (region === "ashheap") {
                    $('#cardModal .counters').addClass("d-none");
                }

                // Render and wire up clan/path/sect inline pickers
                const currentClan = clan && clan !== 'NONE' ? clan : 'None';
                const currentSect = sect && sect !== 'NONE' ? sect : 'None';
                const currentPath = path && path !== 'NONE' ? path : 'None';

                enableInlinePicker({
                    containerSel: '#cardModal .card-clan',
                    selectSel: '#clan-select',
                    values: CLANS,
                    currentDisplay: currentClan,
                    minion: minion,
                    onChanged: function (newDisplay, newKey) {
                        // Update data-* for subsequent opens or actions if needed
                        target.data('clan', newDisplay);
                        // Send update to server
                        sendCommand(['clan', modal.data('controller').split(' ', 2)[0], modal.data('region').split(' ')[0], modal.data('coordinates'), newKey.split('_')[0]].join(' ').trim());
                    }
                });

                enableInlinePicker({
                    containerSel: '#cardModal .card-path',
                    selectSel: '#path-select',
                    values: PATHS,
                    currentDisplay: currentPath,
                    minion: minion,
                    onChanged: function (newDisplay, newKey) {
                        target.data('path', newDisplay);
                        sendCommand(['path', modal.data('controller').split(' ', 2)[0], modal.data('region').split(' ')[0], modal.data('coordinates'), newKey.split('_')[0]].join(' ').trim());
                    }
                });

                enableInlinePicker({
                    containerSel: '#cardModal .card-sect',
                    selectSel: '#sect-select',
                    values: SECTS,
                    currentDisplay: currentSect,
                    minion: minion,
                    onChanged: function (newDisplay, newKey) {
                        target.data('sect', newDisplay);
                        sendCommand(['sect', modal.data('controller').split(' ', 2)[0], modal.data('region').split(' ')[0], modal.data('coordinates'), newKey].join(' ').trim());
                    }
                });

                $('#cardModal .loading').hide();
                $('#cardModal .loaded').show();
                tippy.hideAll({duration: 0});
                modal.modal('show');
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

function influence() {
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

function moveReady() {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    let command = `move ${region} ${modal.data('coordinates')} ready`;
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

function removeFromGame() {
    return doCardCommand('rfg');
}

function movePredator() {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    var player = modal.data('controller').split(' ', 2)[0]; //names with spaces do not work
    let command = `move ${player} ${region} ${modal.data('coordinates')} predator`;
    sendCommand(command);
    modal.modal('hide');
    return false;
}

function movePrey() {
    let modal = $('#cardModal');
    let region = $("#cardModal").data('region');
    var player = modal.data('controller').split(' ', 2)[0]; //names with spaces do not work
    let command = `move ${player} ${region} ${modal.data('coordinates')} prey`;
    sendCommand(command);
    modal.modal('hide');
    return false;
}

function closeModal() {
    $('#cardModal').modal('hide');
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

function updateNotes() {
    var cardLabel = $("#card-label").val();
    var modal = $('#cardModal');
    var parts = new Array(5);
    parts.push('label',
        modal.data('controller').split(' ', 2)[0],
        modal.data('region').split(' ')[0], //ready-region > ready
        modal.data('coordinates'),
        cardLabel);
    var command = parts.join(' ');
    sendCommand(command.trim());
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
