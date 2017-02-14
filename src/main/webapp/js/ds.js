var refresher = null;
var game = null;

function errorhandler(errorString, exception) {
    console.log(exception);
    if (exception.name == "dwr.engine.textHtmlReply") {
        window.alert("Your session has expired, please login again.");
        document.location = '/jol/';
    } else if (exception.name == "dwr.engine.incompleteReply") {
        window.alert("Lost connection with the server, reloading..");
        document.location = "/jol/";
    }
}

function loadTypes(data) {
    dwr.util.addOptions('cardtype', data);
}

function init() {
    DS.getTypes({callback: loadTypes});
    DS.init({callback: playerMap});
}

function playerMap(data) {
    for (var item in data) {
        eval(item + '(data[item]);');
    }
}

function globalChat() {
    DS.chat(dwr.util.getValue('gchat'), {callback: playerMap});
    dwr.util.setValue('gchat', '');
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
    if (data == null) {
        return;
    }
    var curScroll = dwr.util.byId(did).scrollTop;
    dwr.util.byId(did).scrollTop = 1000000;
    if (dwr.util.byId(did).scrollTop == curScroll) {
        curScroll = 1000000;
    }
    var table = dwr.util.byId(id);
    for (var idx = 0; idx < data.length; idx++) {
        table.insertRow(table.rows.length).insertCell(0).innerHTML = data[idx];
    }
    dwr.util.byId(did).scrollTop = curScroll;
}

function navigate(data) {
    toggleVisible('loaded', 'loadmsg');
    var selected = dwr.util.getValue("contentselect");
    dwr.util.setValue("contentselect", data.target);
    toggleVisible(data.target, selected);
    dwr.util.setValue('buttons', '');
    doButtons(data.playerButtons);
    doButtons(data.adminButtons);
    doButtons(data.gameButtons);
    doButtons({help: "Help"});
    doButtons({_guides: "Guides"})
    if (data.player == null) {
        toggleVisible('logininputs', 'loggedin');
        dwr.util.setValue('login', 'Log in');
        dwr.util.byId('gameRow').style.display = "none";
    } else {
        dwr.util.setValue('loggedin', data.player + ' is logged in');
        toggleVisible('loggedin', 'logininputs');
        dwr.util.setValue('login', 'Log out');
        dwr.util.byId('gameRow').style.display = "";
    }
    game = data.game;
    dwr.util.setValue('gamename', '');
    if (game != null) {
        dwr.util.setValue('gamename', game);
    }
}

function addGameRow(tid, label) {
    var table = dwr.util.byId(tid);
    for (var idx = 0; idx < table.rows.length; idx++) {
        var row = table.rows[idx];
        if (row.label == label) {
            return row;
        }
    }
    var newRow = table.insertRow(0);
    newRow.label = label;
    return newRow;
}

function renderMyGames(games) {
    if (games == null) return;
    for (var index = 0; index < games.length; index++) {
        var gameRow = addGameRow('owngames', games[index].game);
        if (gameRow.cells.length == 0) {
            gameRow.insertCell(0);
            gameRow.insertCell(1);
        }
        if (games[index].started) {
            gameRow.cells[0].innerHTML = makeGameLink(games[index].game);
            gameRow.cells[1].innerHTML = games[index].current ? '&nbsp;' : '*';
        } else {
            gameRow.cells[0].innerHTML = games[index].game;
            gameRow.cells[1].innerHTML = 'C' + games[index].cryptSize + ' L' + games[index].libSize;
        }
    }
}

function makeGameLink(game) {
    return '<a class="gamelink" onclick="doNav(' + "'g" + game + "');" + '">' + game + "</a>";
}

function renderOnline(div, who) {
    if (who == null) {
        return;
    }
    dwr.util.setValue(div, who.join(', '));
}

function renderActiveGames(games) {
    if (games == null) return;
    for (var index = 0; index < games.length; index++) {
        if (games[index].turn == null) continue;
        var row = addGameRow('activegames', games[index].game);
        if (row.cells.length == 0) {
            row.insertCell(0);
            row.insertCell(1);
        }
        if (row.cells.length == 2) {
            row.cells[1].colspan = '1';
            row.insertCell(2);
            row.insertCell(3);
            row.insertCell(4);
        }
        row.cells[0].innerHTML = makeGameLink(games[index].game);
        row.cells[1].innerHTML = games[index].access;
        row.cells[2].innerHTML = games[index].turn;
        row.cells[3].innerHTML = '&nbsp ' + games[index].available.join(',');
        row.cells[4].innerHTML = games[index].admin;
    }
}

function loadDeck(deck) {
    dwr.util.setValue('deckname', deck);
    DS.getDeck(deck, {callback: playerMap});
}

function showDeck(data) {
    if (data.text != null) dwr.util.setValue('decktext', data.text);
    dwr.util.byId('deckcontents').innerHTML = data.format;
    dwr.util.byId('deckerrors').style.display = "none";
    if (data.errors != null && data.errors.length != 0) {
        var errorText = "<h3>Deck Errors</h3>" + data.errors.join('<br />');
        dwr.util.setValue('deckerrors', errorText, {escapeHtml: false});
        dwr.util.byId('deckerrors').style.display = "block";
    }
}

function getCardDeck(game, card) {
    var divid = "dcard" + card;
    if (dwr.util.byId(divid) == null) {
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
    while (found == false) {
        name = 'newdeck' + idx;
        found = true;
        for (idx; idx < rows.length; idx++) {
            if (rows[idx].label == name) {
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

function createGame() {
    var gameName = dwr.util.getValue("creategame");
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(dwr.util.getValue("creategame"), {callback: playerMap});
    dwr.util.setValue("creategame", '');
}

function invitePlayer(game) {
    DS.invitePlayer(game, dwr.util.getValue('cgs' + game), {callback: playerMap});
}

function startGame(game) {
    if (confirm("Start game?")) {
        DS.startGame(game, {callback: playerMap});
    }
}

function doRegister() {
    DS.registerDeck(dwr.util.getValue('reggames'), dwr.util.getValue('regdecks'), {callback: playerMap});
}

function closeGame() {
    DS.endGame(dwr.util.getValue("endgameselector"), {callback: playerMap});
}

function refreshState(force) {
    DS.getState(game, force, {callback: playerMap});
}

function doToggle(thistag) {
    var region = document.getElementById("region" + thistag);
    if (region.style.display == 'none') {
        region.style.display = ''; // Show details
        document.getElementById(thistag).innerHTML = "-";
    }
    else {
        region.style.display = 'none'; // Hide details
        document.getElementById(thistag).innerHTML = "+";
    }
}

function doSubmit() {
    var phase, ping = null;
    var command = dwr.util.getValue('command');
    var chat = dwr.util.getValue('chat');
    var endTurn = dwr.util.getValue('endturn');
    if (dwr.util.byId('phase') != null) {
        phase = dwr.util.getValue('phase');
    }
    if (dwr.util.byId('ping').selectedIndex > 0) {
        ping = dwr.util.getValue('ping');
    }

    dwr.util.setValue('command', "");
    dwr.util.setValue('chat', "");
    dwr.util.setValue('ping', 'NNNPPPP');
    if (endTurn == "Yes") dwr.util.setValue('phase', "Untap");
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
        if (!data.admin) {
            dwr.util.byId('dsForm').style.display = 'none';
        }
    } else {
        dwr.util.byId('hand').style.display = '';
        dwr.util.byId('playerPad').style.display = '';
    }
    if (data.hand != null)
        dwr.util.setValue('hand', data.hand, {escapeHtml: false});
    if (data.state != null)
        dwr.util.setValue('state', data.state, {escapeHtml: false});
    if (data.global !== null)
        dwr.util.setValue('global', data.global);
    if (data.text != null)
        dwr.util.setValue('notes', data.text);
    if (data.label != null) {
        dwr.util.setValue('turnlabel', data.label);
    }
    if (data.refresh > 0) {
        if (refresher != null) clearTimeout(refresher);
        refresher = setTimeout("refreshState(false)", data.refresh);
    }
    if (data.pingkeys != null) {
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
    if (data.turns != null) {
        var sel = dwr.util.getValue('turns');
        var num = dwr.util.byId('turns').options.length;
        dwr.util.removeAllOptions('turns');
        dwr.util.addOptions('turns', data.turns);
        if (num != data.turns.length && (data.turns.length == 1 || sel == data.turns[1])) {
            dwr.util.setValue('turns', data.turns[0]);
        } else {
            dwr.util.setValue('turns', sel);
        }
    }
    if (data.turn != null) {
        if (data.resetChat) {
            var table = dwr.util.byId('curturntable');
            while (table.rows.length > 0) table.deleteRow(0);
        }
        renderChat('curturn', 'curturntable', data.turn);
        var turncontent = data.turn.join('<br />');
        var val = dwr.util.getValue('turns');
        var val2 = dwr.util.byId('turns').options;
        if (data.turns != null) {
            dwr.util.setValue('history', turncontent, {escapeHtml: false});
        } else if (dwr.util.getValue('turns') == dwr.util.byId('turns').options[0].value) {
            dwr.util.setValue('history', dwr.util.getValue('history', {escapeHtml: false}) + "<br />" + turncontent, {escapeHtml: false});
        }
    }
    if (data.phases != null) {
        var phasev = dwr.util.getValue('phase');
        dwr.util.byId('phasecommand').style.display = '';
        dwr.util.byId('endcommand').style.display = '';
        dwr.util.removeAllOptions('phase');
        dwr.util.addOptions('phase', data.phases);
        if (data.turnChanged) {
            phasev = 'Untap';
        }
        dwr.util.setValue('phase', phasev);
    } else {
        dwr.util.byId('phasecommand').style.display = 'none';
        dwr.util.byId('endcommand').style.display = 'none';
    }
    if (data.collapsed != null) {
        for (var c in data.collapsed) {
            doToggle(data.collapsed[c]);
        }
    }
    if (data.stamp != null) {
        dwr.util.setValue('gamestamp', data.stamp);
    }
}

function details(thistag) {
    DS.doToggle(game, thistag, {callback: playerMap});
    doToggle(thistag);
}

function showStatus(data) {
    dwr.util.setValue('status', data, {escapeHtml: false});
}

function getCard(card) // Open card text in separate window (always on top)
{
    var divid = "card" + card;
    if (dwr.util.byId(divid) == null) {
        DS.getCardText('showCard', card, {callback: playerMap});
    } else {
        dwr.util.setValue("cards", card);
        selectCard();
    }
}


function showCard(data) {
    var old = dwr.util.getValue('extra', {escapeHtml: false});
    var text = data.text.join("<br />");
    dwr.util.setValue('extra', old + "<div id=card" + data.id + ">" + text + "</div>", {escapeHtml: false});
    dwr.util.addOptions("cards", [data], "id", "name");
    dwr.util.setValue("cards", data.id);
    selectCard();
}

function selectCard() {
    if (dwr.util.getValue("cards") == "NOCARD") {
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
    var text = data.join('<br/>');
    dwr.util.setValue('history', text, {escapeHtml: false});
}

function callbackShowDecks(data) {
    // Deck List
    dwr.util.removeAllRows('decks');
    for (var dIdx = 0; dIdx < data.decks.length; dIdx++) {
        var dRow = addGameRow('decks', data.decks[dIdx].name);
        if (dRow.cells.length == 0) {
            dRow.insertCell(0).innerHTML = '<a onclick="loadDeck(' + "'" + data.decks[dIdx].name + "');" + '">' + data.decks[dIdx].name + '</a>';
            dRow.insertCell(1);
            dRow.insertCell(2);
        }
        dRow.cells[1].innerHTML = 'L' + data.decks[dIdx].lib + ' C' + data.decks[dIdx].crypt + ' G ' + data.decks[dIdx].groups;
        dRow.cells[2].innerHTML = "<a onclick='doDelete(\"" + data.decks[dIdx].name + "\");'>&#x2717;</a>";
        dRow.cells[2].className = 'delete';
    }
    // Register Decks for Games
    for (var gIdx = 0; gIdx < data.games.length; gIdx++) {
        var gRow = addGameRow('opengames', data.games[gIdx].game);
        if (gRow.cells.length == 0) {
            gRow.insertCell(0).innerHTML = data.games[gIdx].game;
            gRow.insertCell(1);
            gRow.insertCell(2);
        }
        gRow.cells[1].innerHTML = data.games[gIdx].name;
        gRow.cells[2].innerHTML = 'L' + data.games[gIdx].lib + ' C' + data.games[gIdx].crypt + ' G ' + data.games[gIdx].groups;
    }
    dwr.util.removeAllOptions('reggames');
    dwr.util.removeAllOptions('regdecks');
    dwr.util.addOptions('reggames', data.games, 'game', 'game');
    dwr.util.addOptions('regdecks', data.decks, 'name', 'name');
}

function callbackShowCards(data) {
    var len = dwr.util.byId('showcards').rows.length;
    for (i = 0; i < len; i++) {
        dwr.util.byId('showcards').deleteRow(0);
    }
    for (var i = 0; i < data.length; i++) {
        dwr.util.byId('showcards').insertRow(0).insertCell(0).innerHTML = '<a onclick="getCardDeck(null,' + "'" + data[i].id + "'" + ');">' + data[i].name + '</a>';
    }
}

function callbackUpdateDeck(data) {
    dwr.util.setValue('deckname', data);
}

// Callback for MainCreator
function callbackMain(data) {
    if (data.loggedIn) {
        toggleVisible('player', 'register');
        toggleVisible('globalchat', 'register');
        dwr.util.setValue('chatstamp', data.stamp);
        renderChat('gchatwin', 'gchattable', data.chat);
        renderOnline('whoson', data.who);
        renderOnline('adson', data.admins);
        renderMyGames(data.myGames);
        renderActiveGames(data.games);
        if (data.refresh > 0) {
            refresher = setTimeout("DS.doPoll({callback: playerMap, errorHandler: errorhandler})", data.refresh);
        }
    }
}

// Callback for AdminCreator
function callbackAdmin(data) {
    for (var game in data.games) {
        if (data.games.hasOwnProperty(game)) {
            var row = addGameRow('gameadmintable', "cg" + game);
            if (row.cells.length == 0) {
                row.insertCell(0);
                row.cells[0].innerHTML = game;
                row.insertCell(1);
                row.cells[1].innerHTML = '<select id="cgs' + game + '"></select><button onclick="invitePlayer(' + "'" + game + "'" + ');">Invite</button>';
                dwr.util.addOptions('cgs' + game, data.players);
                row.insertCell(2);
                row.cells[2].innerHTML = '<button onclick="startGame(' + "'" + game + "'" + ');">Start game</button>';
            }
            for (var idx = row.cells.length; --idx > 2;) {
                row.deleteCell(idx);
            }
            var regs = data.games[game].registrations;
            for (var player in regs) {
                if (regs.hasOwnProperty(player)) {
                    var csize = regs[player].cryptSize;
                    var lsize = regs[player].libSize;
                    var grps = regs[player].groups;
                    row.insertCell(3).innerHTML = player + '(C' + csize + ',L' + lsize + ',G ' + grps + ')';
                }
            }
        }
    }
    dwr.util.removeAllOptions('endgameselector');
    dwr.util.addOptions('endgameselector', data.runningGames);
}