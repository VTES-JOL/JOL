var refresher = null;
var game = null;
var timeInterval = null;
var outageTime = null;
var player = null;
var currentPage = 'main';
var currentOption = "notes";

var profile = {
    email: "",
    receivePing: "",
    receiveSummary: ""
};

function errorhandler(errorString, exception) {
    if (exception.name === "dwr.engine.incompleteReply" || exception.name === 'dwr.engine.textHtmlReply') {
        document.location = "/jol/";
    }
}

$(document).ready(function () {
    DS.init({callback: init});
});

function init(data) {
    $("#loadMessage").hide();
    $("#loaded").show();
    processData(data);
}

function processData(data) {
    for (var item in data) {
        eval(item + '(data[item]);');
    }
}

function toggleVisible(s, h) {
    $("#" + h).hide();
    $("#" + s).show();
}

// Main page: global chat
function doGlobalChat() {
    var chatInput = $("#gchat");
    var chatLine = chatInput.val();
    chatInput.val('');
    if (chatLine === "") {
        return;
    }
    DS.chat(chatLine, {callback: processData, errorHandler: errorhandler});
}

// Main page: navigation buttons
function doNav(target) {
    DS.navigate(target, {callback: processData, errorHandler: errorhandler});
}

// Deck Register: deck link
function doLoadDeck(deck) {
    $("#deckName").val(deck);
    DS.getDeck(deck, {callback: processData, errorHandler: errorhandler});
}

// Utility functions
function renderButton(data) {
    var buttonsDiv = $("#buttons");
    $.each(data, function (key, value) {
        var button = $("<button/>").text(value).click(key, function () {
            DS.navigate(key, {callback: processData, errorHandler: errorhandler});
        });
        if (game === value || currentPage.toLowerCase() === key.toLowerCase()) {
            button.addClass("active-button");
        }
        buttonsDiv.append(button);
    });
}

function renderChat(id, data) {
    if (data === null) {
        return;
    }
    var chatOutputDiv = $("#" + id);
    $.each(data, function (index, chat) {
        var chatLine = $("<p/>").addClass("chat").html(chat);
        chatOutputDiv.append(chatLine);
    });
    chatOutputDiv.scrollTop(chatOutputDiv.prop("scrollHeight") - chatOutputDiv.prop("clientHeight"));
}

function renderRowWithLabel(tid, label) {
    var table = dwr.util.byId(tid);
    for (var idx = 0; idx < table.rows.length; idx++) {
        var row = table.rows[idx];
        if (row.label === label) {
            return row;
        }
    }
    var newRow = table.insertRow(table.rows.length);
    newRow.label = label;
    return newRow;
}

function renderMyGames(games) {
    if (games === null) return;
    for (var index = 0; index < games.length; index++) {
        var gameRow = renderRowWithLabel('ownGames', games[index].game);
        gameRow.removeAttribute('className');
        if (gameRow.cells.length === 0) {
            gameRow.insertCell(0);
            gameRow.insertCell(1);
        }
        if (games[index].started) {
            gameRow.cells[0].innerHTML = renderGameLink(games[index].game);
            gameRow.cells[1].innerHTML = games[index].current ? '&nbsp;' : '*';
            gameRow.className = games[index].turn === player ? "active" : 'game';
            gameRow.className += games[index].flagged ? " flagged" : "";
            gameRow.style.display = games[index].hidden ? 'none' : '';
        } else {
            gameRow.cells[0].innerHTML = '<small>' + games[index].game + '</small>';
            gameRow.cells[1].innerHTML = '<small>C' + games[index].cryptSize + ' L' + games[index].libSize + ' G' + games[index].groups + "</small>";
        }
    }
}

function renderGameLink(game) {
    return '<a onclick="doNav(' + "'g" + game + "');" + '"><small>' + game + "</small></a>";
}

function renderOnline(div, who) {
    var container = $("#" + div);
    container.empty();
    if (who === null) {
        return;
    }
    $.each(who, function (index, player) {
        var playerSpan = $("<span/>").text(player.name).addClass("label");
        if (player.superUser) {
            playerSpan.addClass("label-warning");
        } else if (player.admin) {
            playerSpan.addClass("label-dark");
        } else {
            playerSpan.addClass("label-light")
        }
        if (player.judge) {
            playerSpan.addClass("label-bold");
        }
        container.append(playerSpan);
        container.append(" ");
    });
}

function renderMessage(message) {
    if (!!message) {
        $("#messages").html(message).show();
    } else {
        $("#messages").hide();
    }
}

function renderActiveGames(games) {
    if (games === null) return;
    for (var index = 0; index < games.length; index++) {
        if (games[index].turn === null) continue;
        var row = renderRowWithLabel('activeGames', games[index].game);
        if (row.cells.length === 0) {
            row.insertCell(0);
            row.insertCell(1);
        }
        if (row.cells.length === 2) {
            row.cells[1].colspan = '1';
            row.insertCell(2);
            row.insertCell(3);
            row.insertCell(4);
        }
        row.cells[0].innerHTML = renderGameLink(games[index].game);
        row.cells[1].innerHTML = '<small>' + games[index].access + '</small>';
        row.cells[2].innerHTML = '<small>' + games[index].turn + '</small>';
        row.cells[3].innerHTML = '<small>' + '&nbsp ' + games[index].available.join(',') + '</small>';
        row.cells[4].innerHTML = '<small>' + games[index].admin + '</small>';
    }
}

function removeLabeledRows(table, removedGames) {
    var table = dwr.util.byId(table);
    $.each(removedGames, function (index, game) {
        $.each(table.rows, function (i, row) {
            if (row.label === game) {
                table.deleteRow(i);
                return false;
            }
        });
    });
}

// Invoked via processData()
function navigate(data) {
    ga('send', 'pageview', data.target);
    $("#" + currentPage).hide();
    $("#" + data.target).show();
    currentPage = data.target;
    game = data.game;
    $("#buttons").empty();
    renderButton({main: "Main" + (data.chats ? " *" : "")});
    if (data.player === null) {
        $("#loginInputs").show();
        $("#login").val("Log in");
        $("#gameRow").hide();
        player = null;
    } else {
        renderButton({deck: "Deck Register", profile: "Profile"});
        if (data.admin) {
            renderButton({admin: "Game Admin"});
        }
        if (data.superUser) {
            renderButton({super: "User Admin"});
        }
        renderButton(data.gameButtons);
        $("#loginInputs").hide();
        $("#login").val("Log out");
        $("#gameRow").show();
        player = data.player;
    }
    renderButton({help: "Help", _guides: "Guides"});
    $("#gamename").text(game !== null ? game : '');
}

function showDeck(data) {
    if (data.text !== null) $('#deckText').val(data.text);
    dwr.util.byId('deckcontents').innerHTML = data.format;
    dwr.util.byId('deckerrors').style.display = "none";
    if (data.errors !== null && data.errors.length !== 0) {
        var errorText = "<h3>Deck Errors</h3>" + data.errors.join('<br />');
        $('#deckerrors').html(errorText)
        dwr.util.byId('deckerrors').style.display = "block";
    }
}

function getCardDeck(game, card) {
    var divid = "dcard" + card;
    if (dwr.util.byId(divid) === null) {
        DS.getCardText('showCardDeck', card, {callback: processData, errorHandler: errorhandler});
    } else {
        dwr.util.setValue("deckcards", card);
        selectCardDeck();
    }
}

function selectCardDeck() {
    var divid = "dcard" + dwr.util.getValue("deckcards");
    var selected = dwr.util.getValue("cardSelect");
    dwr.util.setValue("cardSelect", divid);
    toggleVisible(divid, selected);
}

function showCardDeck(data) {
    var oldText = dwr.util.getValue('cardtext', {escapeHtml: false});
    var text = data.text.join("<br />");
    var newText = oldText + "<div id='dcard" + data.id + "' style='display:block;'>" + text + "</div>";
    $('#cardtext').html(newText)
    dwr.util.addOptions("deckcards", [data], "id", "name");
    dwr.util.setValue("deckcards", data.id);
    selectCardDeck();
}

function doSearch() {
    var type = dwr.util.getValue("cardtype");
    var query = dwr.util.getValue("cardquery");
    DS.cardSearch(type, query, {callback: processData, errorHandler: errorhandler});
}

function findName() {
    var rows = dwr.util.byId('decks').rows;
    var found = false;
    var name = '';
    var idx = 1;
    while (found === false) {
        name = 'newdeck' + idx;
        found = true;
        for (idx; idx < rows.length; idx++) {
            if (rows[idx].label === name) {
                found = false;
                break;
            }
            idx = idx + 1;
        }
    }
    return name;
}

function doEdit() {
    $("#deckEdit").show();
    $("#noedit").hide();
    $("#deckName").prop('readOnly', false);
    $("#deckText").prop('readOnly', false);
}

function doAdjust() {
    var deckName = $('#deckName').val();
    var deckText = $('#deckText').val();
    var shuffle = $('#shuffle').is(":checked")
    DS.refreshDeck(deckName, deckText, shuffle, {callback: processData, errorHandler: errorhandler});
}

function doSave() {
    toggleVisible('noedit', 'deckEdit');
    dwr.util.byId('deckName').readOnly = 'readonly';
    dwr.util.byId('deckText').readOnly = 'readonly';
    var deckName = $('#deckName').val();
    var deckText = $('#deckText').val();
    DS.submitDeck(deckName, deckText, {callback: processData, errorHandler: errorhandler});
}

function doDelete(name) {
    var confirmed = confirm("Are you use you want to delete " + name + "\nThis action is not reversible");
    if (!confirmed) return;
    DS.removeDeck(name, {callback: processData, errorHandler: errorhandler});
}

function doNewDeck() {
    doEdit();
    $('#deckName').val(findName());
    showDeck({text: '', format: '', errors: []});
}


// Callback for AdminCreator
function callbackAdmin(data) {
    var currentGames = $("#currentGames");
    var gameList = $("#gameList");
    gameList.empty();
    currentGames.empty();

    $("#endGameList").autocomplete({
        source: data.currentGames,
        change: function (event, ui) {
            if (ui.item === null) {
                $(this).val((ui.item ? ui.item.id : ""));
            }
        }
    });

    $("#playerList").autocomplete({
        source: data.players,
        change: function (event, ui) {
            if (ui.item === null) {
                $(this).val((ui.item ? ui.item.id : ""));
            }
        }
    });

    $.each(data.forming, function (index, game) {
        var headerRow = $("<tr/>");
        var gameHeader = $("<th/>").text(game.gameName);
        var startHeader = $("<th/>");
        headerRow.append(gameHeader);
        headerRow.append(startHeader);
        currentGames.append(headerRow);
        gameList.append(new Option(game.gameName, game.gameName));
        var registrationCount = 0;
        $.each(game.registrations, function (i, registration) {
            var registrationRow = $("<tr/>");
            var player = $("<td/>").text(registration.player);
            registrationRow.append(player);
            var summary = $("<td/>").text(registration.deckSummary);
            if ("invited" !== registration.deckSummary) {
                registrationCount++;
                if (!registration.valid) {
                    summary.append($('<label/>').addClass("label-invalid").text('Invalid'));
                }
            }
            registrationRow.append(summary);
            currentGames.append(registrationRow);
        });
        var registrationText = "( " + registrationCount + " registered )";
        var startButton = $("<button/>").text("Start " + registrationText).click(game, function () {
            if (confirm("Start game?")) {
                DS.startGame(game.gameName, {callback: processData, errorHandler: errorhandler});
            }
        });
        var cancelButton = $("<button/>").text("Close").click(game, function () {
            if (confirm("Cancel game?")) {
                DS.endGame(game.gameName, {callback: processData, errorHandler: errorhandler});
            }
        });
        startHeader.append(startButton);
        startHeader.append(cancelButton);
    });
}

function doCreateGame() {
    var newGameDiv = $("#newGameName");
    var gameName = newGameDiv.val();
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(gameName, {callback: processData, errorHandler: errorhandler});
    newGameDiv.val('');
}

function invitePlayer() {
    var game = $("#gameList").val();
    var player = $("#playerList").val();
    DS.invitePlayer(game, player, {callback: processData, errorHandler: errorhandler});
}

function closeGame() {
    const gameDiv = $("#endGameList");
    var selected = gameDiv.val();
    gameDiv.val("");
    if (confirm("End game?")) {
        DS.endGame(selected, {callback: processData, errorHandler: errorhandler});
    }
}

function doRegister() {
    var regGame = $("#reggames").val();
    var regDeck = $("#regdecks").val();
    DS.registerDeck(regGame, regDeck, {callback: processData, errorHandler: errorhandler});
}

function refreshState(force) {
    DS.getState(game, force, {callback: processData, errorHandler: errorhandler});
}

function doToggle(thistag) {
    var region = $("#region" + thistag);
    var regionToggle = region.find("i.toggle");
    if (region.css("display") === 'none') {
        region.show();
        regionToggle.text("-");
    }
    else {
        region.hide();
        regionToggle.text("+");
    }
}

function doGameChat() {
    const chatDiv = $("#judgeChat");
    var chat = chatDiv.val();
    chatDiv.val("");
    DS.gameChat(game, chat, {callback: processData, errorHandler: errorhandler});
    return false;
}

function doShowDeck() {
    DS.gameDeck(game, {callback: callbackShowGameDeck, errorHandler: errorhandler});
}

function doSubmit() {
    var phaseSelect = $("#phase");
    var commandInput = $("#command");
    var chatInput = $("#chat");
    var pingSelect = $("#ping");
    var endTurnSelect = $("#endTurn");
    var globalNotes = $("#globalNotes");
    var privateNotes = $("#privateNotes");

    var phase = phaseSelect.val();
    var ping = pingSelect.val();
    var command = commandInput.val();
    var chat = chatInput.val();
    var endTurn = endTurnSelect.val();
    var global = globalNotes.val();
    var text = privateNotes.val();
    phase = phase === "" ? null : phase;
    ping = ping === "" ? null : ping;
    commandInput.val("");
    chatInput.val("");
    pingSelect.val("");
    endTurnSelect.val("No");
    if (endTurn === "Yes") {
        phaseSelect.val("Unlock");
    }
    DS.submitForm(game, phase, command, chat, ping, endTurn, global, text, {
        callback: processData,
        errorHandler: errorhandler
    });
    return false;
}

function updateOther() {
    currentOption = $("#otherSelect").val();
    switch (currentOption) {
        case "notes":
            showNotes();
            break;
        case "history":
            showHistory();
            break;
        case "deck":
            showGameDeck();
            break;
    }
}

function showGameDeck() {
    $("#gameDeck").show();
    $("#notes").hide();
    $("#history").hide();
    doShowDeck();
}

function showNotes() {
    $("#notes").show();
    $("#history").hide();
    $("#gameDeck").hide();
}

function showHistory() {
    $("#history").show();
    $("#notes").hide();
    $("#gameDeck").hide();
    getHistory();
}

function loadGame(data) {
    if (!data.player) {
        $(".player-only").hide();
    } else {
        $(".player-only").show();
    }
    if (data.resetChat) {
        $("#gameChatOutput").empty();
        $("#historyOutput").empty();
        $("#gameDeckOutput").empty();
        $("#globalNotes").empty();
        $("#privateNotes").empty();
        currentOption = "notes";
    }
    if (!data.player && !data.judge) {
        $(".reactive-height").addClass("half-height").removeClass("full-height");
        $(".reactive-height-content").addClass("half-height-content").removeClass("full-height-content");
        $(".reactive-height-content-header").addClass("half-height-content-header").removeClass("full-height-content-header");
    } else {
        $(".reactive-height").addClass("full-height").removeClass("half-height");
        $(".reactive-height-content").addClass("full-height-content").removeClass("half-height-content");
        $(".reactive-height-content-header").addClass("full-height-content-header").removeClass("half-height-content-header");
    }
    $("#otherSelect").val(currentOption);
    updateOther();
    if (data.hand !== null) {
        $("#hand").html(data.hand);
    }
    if (data.state !== null) {
        $("#state").html(data.state);
    }
    if (data.global !== null) {
        $("#globalNotes").val(data.global);
    }
    if (data.text !== null) {
        $("#privateNotes").val(data.text);
    }
    if (data.label !== null) {
        $("#gameLabel").text(data.label);
    }
    if (data.refresh > 0) {
        if (refresher !== null) clearTimeout(refresher);
        refresher = setTimeout("refreshState(false)", data.refresh);
    }
    if (data.ping !== null) {
        var pingSelect = $("#ping");
        pingSelect.empty();
        pingSelect.append(new Option("",""));
        $.each(data.ping, function(index,value) {
           pingSelect.append(new Option(value, value));
        });
    }
    if (data.turn !== null) {
        renderChat('gameChatOutput', data.turn);
    }
    if (data.turns !== null) {
        var turnSelect = $("#turns");
        turnSelect.empty();
        data.turns.shift();
        $.each(data.turns, function (index, turn) {
            turnSelect.append($(new Option(turn, turn)));
        });
    }
    if (data.phases !== null) {
        var phaseSelect = $("#phase");
        var currentPhase = phaseSelect.val();
        $("#phaseCommand").show();
        $("#endCommand").show();
        phaseSelect.empty();
        $.each(data.phases, function (index, value) {
            phase.append(new Option(value, value));
        });
        if (data.turnChanged) {
            currentPhase = "Unlock";
        }
        phaseSelect.val(currentPhase);
    } else {
        $("#phaseCommand").hide();
        $("#endCommand").hide();
    }
    if (data.collapsed !== null) {
        for (var c in data.collapsed) {
            doToggle(data.collapsed[c]);
        }
    }
    generateCardData("#game");
}

function generateCardData(parent) {
    tippy(parent + ' a.card-name', {
        placement: 'right',
        arrow: true,
        onShow: function (instance) {
            const content = this.querySelector('.tippy-content');
            content.innerHTML = "Loading...";
            var cardId = instance.title;
            $.get({
                url: "rest/card/" + cardId, success: function (data) {
                    content.innerHTML = data;
                }
            });
        }
    });
}

function details(tag) {
    DS.doToggle(game, tag, {callback: processData, errorHandler: errorhandler});
    doToggle(tag);
}

function showStatus(data) {
    $('#status').html(data)
}

function selectCard() {
    var divid = dwr.util.getValue("cards");
    var selected = dwr.util.getValue("extraSelect");
    dwr.util.setValue("extraSelect", divid);
    toggleVisible(divid, selected);
}

function getHistory() {
    var turns = $('#turns').val();
    DS.getHistory(game, turns, {callback: loadHistory});
}

function loadHistory(data) {
    var historyDiv = $("#historyOutput");
    historyDiv.empty();
    $.each(data, function (index, content) {
        var turnContent = $("<p/>").addClass("chat").html(content);
        historyDiv.append(turnContent);
    });
    generateCardData("#historyOutput");
}

function callbackSuper(data) {

}

function callbackProfile(data) {
    if (profile.email !== data.email) {
        dwr.util.setValue("profileEmail", data.email);
    }

    if (profile.receivePing !== data.receivePing) {
        dwr.util.setValue("profilePing", data.receivePing);
    }

    if (profile.receiveSummary !== data.receiveSummary) {
        dwr.util.setValue("profileTurnSummary", data.receiveSummary);
    }

    profile = data;

    dwr.util.setValue("profilePasswordError", "");
    dwr.util.setValue("profileNewPassword", "");
    dwr.util.setValue("profileConfirmPassword", "");
}

function updateProfile() {
    var newEmail = dwr.util.getValue("profileEmail");
    var newPing = dwr.util.getValue("profilePing");
    var newSummary = dwr.util.getValue("profileTurnSummary");
    DS.updateProfile(newEmail, newPing, newSummary, {callback: processData, errorHandler: errorhandler});
}

function updatePassword() {
    var profileNewPassword = dwr.util.getValue("profileNewPassword");
    var profileConfirmPassword = dwr.util.getValue("profileConfirmPassword");
    if (!profileNewPassword && !profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Please enter a new password to proceed.");
    } else if (profileNewPassword !== profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Password chosen does not match.");
    } else {
        DS.changePassword(profileNewPassword, {callback: processData, errorHandler: errorhandler});
        dwr.util.setValue("profilePasswordError", "Password updated");
    }
}

function loadTypes(data) {
    dwr.util.addOptions('cardtype', data);
}

function callbackShowDecks(data) {
    DS.getTypes({callback: loadTypes});
    // Deck List
    dwr.util.removeAllRows('decks');
    for (var dIdx = 0; dIdx < data.decks.length; dIdx++) {
        var dRow = renderRowWithLabel('decks', data.decks[dIdx]);
        if (dRow.cells.length === 0) {
            dRow.insertCell(0).innerHTML = '<a onclick="doLoadDeck(' + "'" + data.decks[dIdx] + "');" + '">' + data.decks[dIdx] + '</a>';
            dRow.insertCell(1);
        }
        dRow.cells[1].innerHTML = "<a onclick='doDelete(\"" + data.decks[dIdx] + "\");'>&#x2717;</a>";
        dRow.cells[1].className = 'delete';
    }
    if (data.games.length === 0) {
        dwr.util.byId('gameRegistration').style.display = 'none';
    } else {
        dwr.util.byId('gameRegistration').style.display = 'block';
        // Register Decks for Games
        for (var gIdx = 0; gIdx < data.games.length; gIdx++) {
            var gRow = renderRowWithLabel('opengames', data.games[gIdx].game);
            if (gRow.cells.length === 0) {
                gRow.insertCell(0).innerHTML = data.games[gIdx].game;
                gRow.insertCell(1);
                gRow.insertCell(2);
            }
            gRow.cells[1].innerHTML = data.games[gIdx].name;
            gRow.cells[2].innerHTML = '<small>L' + data.games[gIdx].lib + ' C' + data.games[gIdx].crypt + ' G ' + data.games[gIdx].groups + "</small>";
        }
        dwr.util.removeAllOptions('reggames');
        dwr.util.removeAllOptions('regdecks');
        dwr.util.addOptions('reggames', data.games, 'game', 'game');
        dwr.util.addOptions('regdecks', data.decks);
    }
}

function callbackShowGameDeck(data) {
    var deckContentsDiv = $("#gameDeckOutput");
    if (deckContentsDiv.html() === "") {
        var contents = data.split("\n").join("<br />");
        deckContentsDiv.html(contents);
    }
}

function callbackShowCards(data) {
    var len = dwr.util.byId('showcards').rows.length;
    for (i = 0; i < len; i++) {
        dwr.util.byId('showcards').deleteRow(0);
    }
    for (var i = 0; i < data.length; i++) {
        dwr.util.byId('showcards').insertRow(0).insertCell(0).innerHTML = '<a href="javascript:getCardDeck(null,' + "'" + data[i].id + "'" + ');">' + data[i].name + '</a>';
    }
}

function callbackUpdateDeck(data) {
    $('#deckName').val(data);
}

// Callback for MainCreator
function callbackMain(data) {
    $('#chatstamp').text(data.stamp);
    if (data.loggedIn) {
        toggleVisible('player', 'register');
        toggleVisible('globalchat', 'welcome');
        renderChat('globalChatOutput', data.chat);
        renderOnline('whoson', data.who);
        renderMyGames(data.myGames);
        renderActiveGames(data.games);
        removeLabeledRows('ownGames', data.removedGames);
        removeLabeledRows('activeGames', data.removedGames);
        renderMessage(data.message);
        if (data.refresh > 0) {
            refresher = setTimeout("DS.doPoll({callback: processData, errorHandler: errorhandler})", data.refresh);
        }
    }
}

function callbackStatus(data) {
    var clockDiv = dwr.util.byId('clockdiv');
    if (data === "outage past" || data === "not yet") {
        clockDiv.style.display = 'none';
        clearInterval(timeInterval);
    } else {
        if (data === outageTime) {
            return;
        }
        clearInterval(timeInterval);
        timeInterval = setInterval(function () {
            var t = getTimeRemaining(data);
            clockDiv.innerHTML = '<button class="">System restart in ' + t.days + 'd ' +
                t.hours + 'h ' +
                t.minutes + 'm ' +
                t.seconds + 's' + "</button>";
            if (t.total <= 0) {
                clearInterval(timeInterval);
                clockDiv.style.display = 'none';
            }
            clockDiv.style.display = '';
        }, 1000);
    }
}

function getTimeRemaining(endTime) {
    var t = Date.parse(endTime) - Date.parse(new Date());
    var seconds = Math.floor((t / 1000) % 60);
    var minutes = Math.floor((t / 1000 / 60) % 60);
    var hours = Math.floor((t / (1000 * 60 * 60)) % 24);
    var days = Math.floor(t / (1000 * 60 * 60 * 24));
    return {
        'total': t,
        'days': days,
        'hours': hours,
        'minutes': minutes,
        'seconds': seconds
    };
}