var refresher = null;

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
    if (data.player == null) {
        toggleVisible('logininputs', 'loggedin');
        dwr.util.setValue('login', 'Log in');
    } else {
        dwr.util.setValue('loggedin', data.player + ' is logged in');
        toggleVisible('loggedin', 'logininputs');
        dwr.util.setValue('login', 'Log out');
    }
    var game = data.game;
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

function findGameRow(tid, label) {
    var table = dwr.util.byId(tid);
    for (var idx = 0; idx < table.rows.length; idx++) {
        var row = table.rows[idx];
        if (row.label == label) {
            return idx;
        }
    }
    return -1;
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
        var row = addGameRow('activegames', games[index]);
        if (row.cells.length == 0) {
            row.insertCell(0);
            row.insertCell(1);
        }
        if (row.cells.length == 2) {
            row.cells[1].colspan = '1';
            row.insertCell(2);
            row.insertCell(3);
        }
        row.cells[0].innerHTML = makeGameLink(games[index].game);
        row.cells[1].innerHTML = games[index].access;
        row.cells[2].innerHTML = games[index].turn;
        row.cells[3].innerHTML = '&nbsp ' + games[index].available.join(',');
    }
}

function renderNews(news) {
    var newsItems = '';
    for (var index = 0; index < news.length; index++) {
        newsItems += '<p><a href="' + news[index].url + '">' + news[index].text + '</a></p>';
    }
    dwr.util.byId('news').innerHTML = newsItems;
}

function loadMain(data) {
    if (data.loggedIn) {
        toggleVisible('player', 'register');
        toggleVisible('globalchat', 'register');
        dwr.util.setValue('chatstamp', data.stamp);
        renderChat('gchatwin', 'gchattable', data.chat);
        renderOnline('whoson', data.who);
        renderOnline('adson', data.admins);
        renderMyGames(data.myGames);
        renderActiveGames(data.games);
        renderNews(data.news);
        if (data.refresh > 0) {
            refresher = setTimeout("DS.doPoll(playerMap)", data.refresh);
        }
    }
}

function loadDeck(deck) {
    dwr.util.setValue('deckname', deck);
    DS.getDeck(deck, {callback: playerMap});
}

function showDeck(data) {
    if (data.text != null) dwr.util.setValue('decktext', data.text);
    dwr.util.byId('deckcontents').innerHTML = data.format;
    if (data.errors != null) dwr.util.setValue('deckerrors', data.errors.join('<br />'));
}

function showDecks(data) {
    // Deck List
    for (var dIdx = 0; dIdx < data.decks.length; dIdx++) {
        var row = addGameRow('decks', data.decks[dIdx].name);
        if (row.cells.length == 0) {
            row.insertCell(0).innerHTML = '<a onclick="loadDeck(' + "'" + data.decks[dIdx].name + "');" + '">' + data.decks[dIdx].name + '</a>';
            row.insertCell(1);
        }
        row.cells[1].innerHTML = 'L' + data.decks[dIdx].lib + ' C' + data.decks[dIdx].crypt + ' G ' + data.decks[dIdx].groups;
    }
    // Register Decks for Games
    for (var gIdx = 0; gIdx < data.games.length; gIdx++) {
        var row = addGameRow('opengames', data.games[gIdx].game);
        if (row.cells.length == 0) {
            row.insertCell(0).innerHTML = data.games[gIdx].game;
            row.insertCell(1);
            row.insertCell(2);
        }
        row.cells[1].innerHTML = data.games[gIdx].name;
        row.cells[2].innerHTML = 'L' + data.games[gIdx].lib + ' C' + data.games[gIdx].crypt + ' G ' + data.games[gIdx].groups;
    }
    dwr.util.removeAllOptions('reggames');
    dwr.util.removeAllOptions('regdecks');
    dwr.util.addOptions('reggames', data.games, 'game', 'game');
    dwr.util.addOptions('regdecks', data.decks, 'name', 'name');
}
/*

 function getCard(game, card) // Open card text in separate window (always on top)
 {
 var divid = "card" + card;
 if (dwr.util.byId(divid) == null) {
 DS.getCardText(pmap, 'showCard', game, card);
 } else {
 dwr.util.setValue("cards", card);
 selectCard();
 }
 }
 function showCard(data) {
 var old = dwr.util.getValue('extra');
 var text = data.text.join("<br />");
 dwr.util.setValue('extra', old + "<div id=card" + data.id + ">" + text + "</div>");
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
 shide(divid, selected);
 }
 }
 function details(thistag) {
 dsdebug("details " + thistag);
 DS.doToggle(pmap, game, thistag);
 doToggle(thistag);
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

 function dsload(force) {
 DS.getState(pmap, game, force);
 }



 var refresher = null;
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
 dwr.util.setValue('hand', data.hand);
 if (data.state != null)
 dwr.util.setValue('state', data.state);
 if (data.global != null)
 dwr.util.setValue('global', data.global);
 if (data.text != null)
 dwr.util.setValue('notes', data.text);
 if (data.label != null) {
 dwr.util.setValue('turnlabel', data.label);
 //		    dochat('curturn',data.label);
 }
 if (data.refresh > 0) {
 if (refresher != null) clearTimeout(refresher);
 refresher = setTimeout("dsload(false)", data.refresh);
 }
 if (data.pingkeys != null) {
 var pingarr = new Array();
 for (var i in data.pingkeys) {
 pingarr[i] = new Object();
 pingarr[i].key = data.pingkeys[i];
 pingarr[i].value = data.pingvalues[i];
 }
 var sel = dwr.util.getValue('ping');
 dwr.util.removeAllOptions('ping');
 dwr.util.addOptions('ping', {"NNNPPPP": "No ping"});
 dwr.util.addOptions('ping', pingarr, 'value', 'key');
 dwr.util.setValue('ping', sel);
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
 dochat('curturn', 'curturntable', data.turn);
 var turncontent = data.turn.join('<br />');
 var val = dwr.util.getValue('turns');
 var val2 = dwr.util.byId('turns').options;
 if (data.turns != null) {
 dwr.util.setValue('history', turncontent);
 } else if (dwr.util.getValue('turns') == dwr.util.byId('turns').options[0].value) {
 dwr.util.setValue('history', dwr.util.getValue('history') + "<br />" + turncontent);
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
 function showStatus(data) {
 if (data != null) {
 dwr.util.setValue('status', data);
 }
 }

 function dosubmit() {
 if (dwr.util.byId('phase') != null) {
 phase = dwr.util.getValue('phase');
 }
 var command = dwr.util.getValue('command');
 dwr.util.setValue('command', "");
 var chat = dwr.util.getValue('chat');
 dwr.util.setValue('chat', "");
 var ping = null;
 if (dwr.util.byId('ping').selectedIndex > 0) {
 ping = dwr.util.getValue('ping');
 }
 dwr.util.setValue('ping', 'NNNPPPP');
 var endTurn = dwr.util.getValue('endturn');
 if (endTurn == "Yes") dwr.util.setValue('phase', "Untap");
 dwr.util.byId('endturn').selectedIndex = 0;
 // TODO should only submit global and text if they've changed
 var global = dwr.util.getValue('global');
 var text = dwr.util.getValue('notes');
 DS.submitForm(pmap, game, phase, command, chat, ping, endTurn, global, text);
 return false;
 }

 function dsdebug(data) {
 //dwr.util.setValue('dsdebug',dwr.util.getValue('dsdebug') + "DEBUG " + data);
 }

 function ds2debug(data) {
 dwr.util.setValue('dsdebug', dwr.util.getValue('dsdebug') + "DEBUG " + data);
 }

 function loadHistory(data) {
 var text = data.join('<br/>');
 dwr.util.setValue('history', text);
 }

 function retrieveHistory() {
 DS.getHistory(loadHistory, game, dwr.util.getValue('turns'));
 }

 function selectHistory() {
 var id = dwr.util.getValue("extraSelect");
 dwr.util.byId('cards').selectedIndex = 0;
 dwr.util.setValue("extraSelect", "history");
 shide('history', id);
 }

 function getHistory() {
 selectHistory();
 retrieveHistory();
 }


 var game = null;
 function addgamerow(tid, label) {
 var table = dwr.util.byId(tid);
 for (var idx in table.rows) {
 var row = table.rows[idx];
 if (row.label == label) {
 return row;
 }
 }
 var row = table.insertRow(0);
 row.label = label;
 return row;
 }
 function findgamerow(tid, label) {
 var table = dwr.util.byId(tid);
 for (var idx in table.rows) {
 var row = table.rows[idx];
 if (row.label == label) {
 return idx;
 }
 }
 return -1;
 }
 function mkgamelink(game) {
 return '<a class=gamelink onclick="dsnav(' + "'g" + game + "');" + '">' + game + "</a>";
 }
 function globchat() {
 DS.chat(pmap, dwr.util.getValue('gchat'));
 dwr.util.setValue('gchat', '');
 }
 function doedit() {
 shide('deckedit', 'noedit');
 dwr.util.byId('deckname').readOnly = null;
 dwr.util.byId('decktext').readOnly = null;
 }
 function donewdeck() {
 doedit();
 var name = findname();
 dwr.util.setValue('deckname', name);
 var newdeck = new Object();
 newdeck.text = '';
 newdeck.format = '';
 newdeck.errors = '';
 showDeck(newdeck);
 }
 function findname() {
 var idx = 1;
 var rows = dwr.util.byId('decks').rows
 var found = false;
 var name = '';
 while (found == false) {
 var name = 'newdeck' + idx;
 found = true;
 for (var idx in rows) {
 if (rows[idx].label == name) {
 found = false;
 break;
 }
 idx = idx + 1;
 }
 }
 return name;
 }
 function doadjust() {
 DS.refreshDeck(pmap, dwr.util.getValue('deckname'), dwr.util.getValue('decktext'), dwr.util.getValue('shuffle'));
 }
 function dosave() {
 shide('noedit', 'deckedit');
 dwr.util.byId('deckname').readOnly = 'readonly';
 dwr.util.byId('decktext').readOnly = 'readonly';
 DS.submitDeck(pmap, dwr.util.getValue('deckname'), dwr.util.getValue('decktext'));
 }
 function repldeckname(data) {
 dwr.util.setValue('deckname', data);
 }
 function showDeck(data) {
 if (data.text != null) dwr.util.setValue('decktext', data.text);
 dwr.util.byId('deckcontents').innerHTML = data.format;
 if (data.errors != null) dwr.util.setValue('deckerrors', data.errors.join('<br />'));
 }


 function doregister() {
 DS.registerDeck(pmap, dwr.util.getValue('reggames'), dwr.util.getValue('regdecks'));
 }
 function showCards(data) {
 var len = dwr.util.byId('showcards').rows.length;
 for (i = 0; i < len; i++) {
 dwr.util.byId('showcards').deleteRow(0);
 }
 for (var i in data) {
 dwr.util.byId('showcards').insertRow(0).insertCell(0).innerHTML = '<a onclick="javascript:getCardDeck(null,' + "'" + data[i].id + "'" + ');">' + data[i].name + '</a>';
 }
 }
 // cut pasted and altered from top
 function getCardDeck(game, card) // Open card text in separate window (always on top)
 {
 var divid = "dcard" + card;
 if (dwr.util.byId(divid) == null) {
 DS.getCardText(pmap, 'showCardDeck', game, card);
 } else {
 dwr.util.setValue("deckcards", card);
 selectCardDeck();
 }
 }
 function showCardDeck(data) {
 var old = dwr.util.getValue('cardtext');
 var text = data.text.join("<br />");
 dwr.util.setValue('cardtext', old + "<div id=dcard" + data.id + ">" + text + "</div>");
 dwr.util.addOptions("deckcards", [data], "id", "name");
 dwr.util.setValue("deckcards", data.id);
 selectCardDeck();
 }
 function selectCardDeck() {
 var divid = "dcard" + dwr.util.getValue("deckcards");
 var selected = dwr.util.getValue("cardSelect");
 dwr.util.setValue("cardSelect", divid);
 shide(divid, selected);
 }
 function dosearch() {
 DS.cardSearch(pmap, dwr.util.getValue("cardtype"), dwr.util.getValue("cardquery"));
 }
 function creategame() {
 DS.createGame(pmap, dwr.util.getValue("creategame"));
 dwr.util.setValue("creategame", '');
 }
 function inviteplayer(game) {
 DS.invitePlayer(pmap, game, dwr.util.getValue('cgs' + game));
 }
 function startgame(game) {
 if (confirm("Start game?")) {
 DS.startGame(pmap, game);
 }
 }
 function removedeck(deck) {
 if (confirm("Remove deck?")) {
 DS.removeDeck(deck);
 }
 }
 function doadmin(data) {
 for (var game in data.games) {
 var row = addgamerow('gameadmintable', "cg" + game);
 if (row.cells.length == 0) {
 row.insertCell(0);
 row.cells[0].innerHTML = game;
 row.insertCell(1);
 row.cells[1].innerHTML = '<select id="cgs' + game + '"></select><button onclick="inviteplayer(' + "'" + game + "'" + ');">Invite</button>';
 dwr.util.addOptions('cgs' + game, data.players);
 row.insertCell(2);
 row.cells[2].innerHTML = '<button onclick="startgame(' + "'" + game + "'" + ');">Start game</button>';
 }
 for (var idx = row.cells.length; --idx > 2;) {
 row.deleteCell(idx);
 }
 var regs = data.games[game].registrations;
 for (var player in regs) {
 var csize = regs[player].cryptSize;
 var lsize = regs[player].libSize;
 var grps = regs[player].groups;
 row.insertCell(3).innerHTML = player + '(C' + csize + ',L' + lsize + ',G ' + grps + ')';
 }
 }
 dwr.util.removeAllOptions('endgameselector');
 dwr.util.addOptions('endgameselector', data.runningGames);
 }
 function loadsuper(data) {

 }
 function beuser() {
 DS.dosu(getsuper, dwr.util.getValue("userspec"));
 }
 function unend() {
 DS.unend(getsuper, dwr.util.getValue("gamespec"));
 }
 function closegames() {
 DS.endGames(pmap, dwr.util.getValue("endgameselector"));
 }
 function getinfo() {
 DS.inspect(getsuper, dwr.util.getValue("icmd"));
 }
 function dosuper() {
 DS.doCommand(getsuper, dwr.util.getValue("scmd"));
 }
 function getsuper(data) {
 dwr.util.setValue('sres', data);
 }
 function loadbugs(data) {
 dsdebug("loading bugs");
 for (var bug in data) {
 dsdebug("adding " + bug);
 var row = addgamerow('bugtable', 'bugentry' + bug.index);
 if (row.cells.length == 0) {
 row.insertCell(0);
 row.insertCell(1);
 row.insertCell(2);
 row.cells[2].innerHTML = bug.filer;
 //       row.cells[1].innerHTML = '<button onclick="getbug(' + "'" + bug.index + "'" + ');">' + bug.summary = '</button>';
 row.cells[0].innerHTML = bug.status;
 }
 }
 dsdebug("done loading");
 }
 function getbug(index) {
 DS.getBugDetail(loadbug, index);
 }
 function loadbug(data) {
 dwr.util.setValue('bugdetail', data.details);
 dwr.util.removeAllRows('commenttable');
 for (var comment in data.comments) {
 var row = dwr.util.byId('commenttable').insertRow();
 row.insertCell(0);
 row.cells[0].innerHTML = commment;
 }
 }
 */
