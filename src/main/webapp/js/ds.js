var refresher = null;
var game = null;
var timeInterval = null;
var outageTime = null;
var player = null;

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

function loadTypes(data) {
    dwr.util.addOptions('cardtype', data);
}

$(document).ready(function () {
    DS.getTypes({callback: loadTypes});
    DS.init({callback: playerMap});
});

function playerMap(data) {
    for (var item in data) {
        eval(item + '(data[item]);');
    }
}

function globalChat() {
    var chatLine = dwr.util.getValue('gchat');
    dwr.util.setValue('gchat', '');
    if (chatLine === "") {
        return;
    }
    DS.chat(chatLine, {callback: playerMap});
}

function toggleVisible(s, h) {
    dwr.util.byId(h).style.display = 'none';
    dwr.util.byId(s).style.display = '';
}

function doNav(target) {
    DS.navigate(target, {callback: playerMap});
}

function doButtons(data) {
    var buttons = dwr.util.getValue('buttons', {escapeHtml: false});
    for (var prop in data) {
        if (data.hasOwnProperty(prop)) {
            buttons += '<button onclick="doNav(' + "'" + prop + "'" + ');">' + data[prop] + "</button>";
        }
    }
    dwr.util.setValue('buttons', buttons, {escapeHtml: false});
}

function renderChat(did, id, data) {
    if (data === null) {
        return;
    }
    var curScroll = dwr.util.byId(did).scrollTop;
    dwr.util.byId(did).scrollTop = 1000000;
    if (dwr.util.byId(did).scrollTop === curScroll) {
        curScroll = 1000000;
    }
    var table = $("#" + id);
    $.each(data, function (index, chat) {
        var chatLine = $("<p/>").addClass("chat").html(chat);
        table.append(chatLine);
    });
    dwr.util.byId(did).scrollTop = curScroll;
}

function navigate(data) {
    toggleVisible('loaded', 'loadmsg');
    var selected = dwr.util.getValue("contentselect");
    dwr.util.setValue("contentselect", data.target);
    toggleVisible(data.target, selected);
    dwr.util.setValue('buttons', '');
    doButtons({main: "Main" + (data.chats ? " *" : "")});
    if (data.player === null) {
        toggleVisible('logininputs', 'loggedin');
        dwr.util.setValue('login', 'Log in');
        dwr.util.byId('gameRow').style.display = "none";
        player = null;
    } else {
        doButtons({deck: "Deck Register", profile: "Profile"});
        if (data.admin) {
            doButtons({admin: "Game Admin"})
        }
        doButtons(data.gameButtons);
        toggleVisible('loggedin', 'logininputs');
        dwr.util.setValue('login', 'Log out');
        dwr.util.byId('gameRow').style.display = "";
        player = data.player;
    }
    doButtons({help: "Help"});
    doButtons({_guides: "Guides"});
    game = data.game;
    dwr.util.setValue('gamename', '');
    if (game !== null) {
        dwr.util.setValue('gamename', game);
    }
}

function addGameRow(tid, label) {
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
        var gameRow = addGameRow('owngames', games[index].game);
        gameRow.removeAttribute('className')
        if (gameRow.cells.length === 0) {
            gameRow.insertCell(0);
            gameRow.insertCell(1);
        }
        if (games[index].started) {
            gameRow.cells[0].innerHTML = makeGameLink(games[index].game);
            gameRow.cells[1].innerHTML = games[index].current ? '&nbsp;' : '*';
            if (games[index].turn === player) {
                gameRow.className = "active";
            }
        } else {
            gameRow.cells[0].innerHTML = '<small>' + games[index].game + '</small>';
            gameRow.cells[1].innerHTML = '<small>C' + games[index].cryptSize + ' L' + games[index].libSize + ' G' + games[index].groups + "</small>";
        }
    }
}

function makeGameLink(game) {
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
        container.append(playerSpan);
        container.append(" ");
    });
}

function renderMessage(message) {
    if (message !== null && message !== "") {
        dwr.util.byId('messages').style.display = "";
        dwr.util.setValue('messages', message, {escapeHtml: false});
    } else {
        dwr.util.byId('messages').style.display = "none";
    }
}

function renderActiveGames(games) {
    if (games === null) return;
    for (var index = 0; index < games.length; index++) {
        if (games[index].turn === null) continue;
        var row = addGameRow('activegames', games[index].game);
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
        row.cells[0].innerHTML = makeGameLink(games[index].game);
        row.cells[1].innerHTML = '<small>' + games[index].access + '</small>';
        row.cells[2].innerHTML = '<small>' + games[index].turn + '</small>';
        row.cells[3].innerHTML = '<small>' + '&nbsp ' + games[index].available.join(',') + '</small>';
        row.cells[4].innerHTML = '<small>' + games[index].admin + '</small>';
    }
}

function removeOwnGames(removedGames) {
    var table = dwr.util.byId('owngames');
    $.each(removedGames, function (index, game) {
        $.each(table.rows, function (i, row) {
            if (row.label === game) {
                table.deleteRow(i);
                return false;
            }
        });
    });
}

function removeActiveGames(removedGames) {
    var table = dwr.util.byId('activegames');
    $.each(removedGames, function (index, game) {
        $.each(table.rows, function (i, row) {
            if (row.label === game) {
                table.deleteRow(i);
                return false;
            }
        });
    });
}

function loadDeck(deck) {
    dwr.util.setValue('deckname', deck);
    DS.getDeck(deck, {callback: playerMap});
}

function showDeck(data) {
    if (data.text !== null) dwr.util.setValue('decktext', data.text);
    dwr.util.byId('deckcontents').innerHTML = data.format;
    dwr.util.byId('deckerrors').style.display = "none";
    if (data.errors !== null && data.errors.length !== 0) {
        var errorText = "<h3>Deck Errors</h3>" + data.errors.join('<br />');
        dwr.util.setValue('deckerrors', errorText, {escapeHtml: false});
        dwr.util.byId('deckerrors').style.display = "block";
    }
}

function getCardDeck(e, game, card) {
    e.preventDefault();
    var divid = "dcard" + card;
    if (dwr.util.byId(divid) === null) {
        DS.getCardText('showCardDeck', card, {callback: playerMap});
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
    dwr.util.setValue('cardtext', newText, {escapeHtml: false});
    dwr.util.addOptions("deckcards", [data], "id", "name");
    dwr.util.setValue("deckcards", data.id);
    selectCardDeck();
}

function doSearch() {
    DS.cardSearch(dwr.util.getValue("cardtype"), dwr.util.getValue("cardquery"), {callback: playerMap});
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
    toggleVisible('deckedit', 'noedit');
    dwr.util.byId('deckname').readOnly = null;
    dwr.util.byId('decktext').readOnly = null;
}

function doAdjust() {
    DS.refreshDeck(dwr.util.getValue('deckname'), dwr.util.getValue('decktext'), dwr.util.getValue('shuffle'), {callback: playerMap});
}

function doSave() {
    toggleVisible('noedit', 'deckedit');
    dwr.util.byId('deckname').readOnly = 'readonly';
    dwr.util.byId('decktext').readOnly = 'readonly';
    DS.submitDeck(dwr.util.getValue('deckname'), dwr.util.getValue('decktext'), {callback: playerMap});
}

function doDelete(name) {
    var confirmed = confirm("Are you use you want to delete " + name + "\nThis action is not reversible");
    if (!confirmed) return;
    DS.removeDeck(name, {callback: playerMap});
}

function doNewDeck() {
    doEdit();
    dwr.util.setValue('deckname', findName());
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
                DS.startGame(game.gameName, {callback: playerMap});
            }
        });
        var cancelButton = $("<button/>").text("Close").click(game, function () {
            if (confirm("Cancel game?")) {
                DS.endGame(game.gameName, {callback: playerMap});
            }
        });
        startHeader.append(startButton);
        startHeader.append(cancelButton);
    });
}

function createGame() {
    var gameName = dwr.util.getValue("newGameName");
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(dwr.util.getValue("newGameName"), {callback: playerMap});
    dwr.util.setValue("newGameName", '');
}

function invitePlayer() {
    var game = $("#gameList").val();
    var player = $("#playerList").val();
    DS.invitePlayer(game, player, {callback: playerMap});
}

function closeGame() {
    if (confirm("End game?")) {
        DS.endGame(dwr.util.getValue("endGameSelector"), {callback: playerMap});
    }
}

function doRegister() {
    DS.registerDeck(dwr.util.getValue('reggames'), dwr.util.getValue('regdecks'), {callback: playerMap});
}

function refreshState(force) {
    DS.getState(game, force, {callback: playerMap});
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

function doSubmit() {
    var phase, ping = null;
    var command = dwr.util.getValue('command');
    var chat = dwr.util.getValue('chat');
    var endTurn = dwr.util.getValue('endturn');
    if (dwr.util.byId('phase') !== null) {
        phase = dwr.util.getValue('phase');
    }
    if (dwr.util.byId('ping').selectedIndex > 0) {
        ping = dwr.util.getValue('ping');
    }

    dwr.util.setValue('command', "");
    dwr.util.setValue('chat', "");
    dwr.util.setValue('ping', 'NNNPPPP');
    if (endTurn === "Yes") dwr.util.setValue('phase', "Unlock");
    dwr.util.byId('endturn').selectedIndex = 0;
    var global = dwr.util.getValue('global');
    var text = dwr.util.getValue('notes');
    DS.submitForm(game, phase, command, chat, ping, endTurn, global, text, {callback: playerMap});
    return false;
}


function loadGame(data) {
    if (!data.player) {
        dwr.util.byId('hand').style.display = 'none';
        dwr.util.byId('playerPad').style.display = 'none';
        dwr.util.byId('dsForm').style.display = 'none';
    } else {
        dwr.util.byId('hand').style.display = '';
        dwr.util.byId('playerPad').style.display = '';
        dwr.util.byId('dsForm').style.display = '';
    }
    if (data.hand !== null)
        dwr.util.setValue('hand', data.hand, {escapeHtml: false});
    if (data.state !== null)
        dwr.util.setValue('state', data.state, {escapeHtml: false});
    if (data.global !== null)
        dwr.util.setValue('global', data.global);
    if (data.text !== null)
        dwr.util.setValue('notes', data.text);
    if (data.label !== null) {
        dwr.util.setValue('turnlabel', data.label);
    }
    if (data.refresh > 0) {
        if (refresher !== null) clearTimeout(refresher);
        refresher = setTimeout("refreshState(false)", data.refresh);
    }
    if (data.pingkeys !== null) {
        var pingarr = [];
        for (var i = 0; i < data.pingkeys.length; i++) {
            pingarr[i] = {};
            pingarr[i].key = data.pingkeys[i];
            pingarr[i].value = data.pingvalues[i];
        }
        var pingSelection = dwr.util.getValue('ping');
        dwr.util.removeAllOptions('ping');
        dwr.util.addOptions('ping', {"": "No ping"});
        dwr.util.addOptions('ping', pingarr, 'value', 'key');
        dwr.util.setValue('ping', pingSelection);
    }
    if (data.turns !== null) {
        var sel = dwr.util.getValue('turns');
        var num = dwr.util.byId('turns').options.length;
        dwr.util.removeAllOptions('turns');
        dwr.util.addOptions('turns', data.turns);
        if (num !== data.turns.length && (data.turns.length === 1 || sel === data.turns[1])) {
            dwr.util.setValue('turns', data.turns[0]);
        } else {
            dwr.util.setValue('turns', sel);
        }
    }
    if (data.turn !== null) {
        if (data.resetChat) {
            var table = dwr.util.byId('curturntable');
            while (table.rows.length > 0) table.deleteRow(0);
        }
        renderChat('curturn', 'curturntable', data.turn);
        loadHistory(data.turn);
    }
    if (data.phases !== null) {
        var phasev = dwr.util.getValue('phase');
        dwr.util.byId('phasecommand').style.display = '';
        dwr.util.byId('endcommand').style.display = '';
        dwr.util.removeAllOptions('phase');
        dwr.util.addOptions('phase', data.phases);
        if (data.turnChanged) {
            phasev = 'Unlock';
        }
        dwr.util.setValue('phase', phasev);
    } else {
        dwr.util.byId('phasecommand').style.display = 'none';
        dwr.util.byId('endcommand').style.display = 'none';
    }
    if (data.collapsed !== null) {
        for (var c in data.collapsed) {
            doToggle(data.collapsed[c]);
        }
    }
}

function details(e, tag) {
    e.preventDefault();
    DS.doToggle(game, tag, {callback: playerMap});
    doToggle(tag);
}

function showStatus(data) {
    dwr.util.setValue('status', data, {escapeHtml: false});
}

function getCard(e, card) // Open card text in separate window (always on top)
{
    e.preventDefault();
    var divid = "card" + card;
    if (dwr.util.byId(divid) === null) {
        DS.getCardText('showCard', card, {callback: playerMap});
    } else {
        dwr.util.setValue("cards", card);
        selectCard();
    }
}

function showCard(data) {
    var old = dwr.util.getValue('extra', {escapeHtml: false});
    var text = data.text.join("<br />");
    dwr.util.setValue('extra', old + "<div class='padded' id='card" + data.id + "'>" + text + "</div>", {escapeHtml: false});
    dwr.util.addOptions("cards", [data], "id", "name");
    dwr.util.setValue("cards", data.id);
    selectCard();
}

function selectCard() {
    if (dwr.util.getValue("cards") === "NOCARD") {
        selectHistory();
    } else {
        var divid = "card" + dwr.util.getValue("cards");
        var selected = dwr.util.getValue("extraSelect");
        dwr.util.setValue("extraSelect", divid);
        toggleVisible(divid, selected);
    }
}

function selectHistory() {
    var id = dwr.util.getValue("extraSelect");
    dwr.util.byId('cards').selectedIndex = 0;
    dwr.util.setValue("extraSelect", "history");
    toggleVisible('history', id);
}

function retrieveHistory() {
    DS.getHistory(game, dwr.util.getValue('turns'), {callback: loadHistory});
}

function getHistory() {
    selectHistory();
    retrieveHistory();
}

function loadHistory(data) {
    var historyDiv = $("#history");
    historyDiv.empty();
    $.each(data, function (index, content) {
        var turnContent = $("<p/>").addClass("chat").html(content);
        historyDiv.append(turnContent);
    });
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
    DS.updateProfile(newEmail, newPing, newSummary, {callback: playerMap});
}

function updatePassword() {
    var profileNewPassword = dwr.util.getValue("profileNewPassword");
    var profileConfirmPassword = dwr.util.getValue("profileConfirmPassword");
    if (!profileNewPassword && !profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Please enter a new password to proceed.");
    } else if (profileNewPassword !== profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Password chosen does not match.");
    } else {
        DS.changePassword(profileNewPassword, {callback: playerMap});
        dwr.util.setValue("profilePasswordError", "Password updated");
    }
}

function callbackShowDecks(data) {
    // Deck List
    dwr.util.removeAllRows('decks');
    for (var dIdx = 0; dIdx < data.decks.length; dIdx++) {
        var dRow = addGameRow('decks', data.decks[dIdx]);
        if (dRow.cells.length === 0) {
            dRow.insertCell(0).innerHTML = '<a onclick="loadDeck(' + "'" + data.decks[dIdx] + "');" + '">' + data.decks[dIdx] + '</a>';
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
            var gRow = addGameRow('opengames', data.games[gIdx].game);
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

function callbackShowCards(data) {
    var len = dwr.util.byId('showcards').rows.length;
    for (i = 0; i < len; i++) {
        dwr.util.byId('showcards').deleteRow(0);
    }
    for (var i = 0; i < data.length; i++) {
        dwr.util.byId('showcards').insertRow(0).insertCell(0).innerHTML = '<a onclick="getCardDeck(event, null,' + "'" + data[i].id + "'" + ');">' + data[i].name + '</a>';
    }
}

function callbackUpdateDeck(data) {
    dwr.util.setValue('deckname', data);
}

// Callback for MainCreator
function callbackMain(data) {
    dwr.util.setValue('chatstamp', data.stamp);
    if (data.loggedIn) {
        toggleVisible('player', 'register');
        toggleVisible('globalchat', 'welcome');
        renderChat('gchatwin', 'gchattable', data.chat);
        renderOnline('whoson', data.who);
        renderMyGames(data.myGames);
        renderActiveGames(data.games);
        removeOwnGames(data.removedGames);
        removeActiveGames(data.removedGames);
        renderMessage(data.message);
        if (data.refresh > 0) {
            refresher = setTimeout("DS.doPoll({callback: playerMap, errorHandler: errorhandler})", data.refresh);
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
            clockDiv.innerHTML = '<button class="btn-vtes-info">System restart in ' + t.days + 'd ' +
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