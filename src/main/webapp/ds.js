function getCard(game, card) // Open card text in separate window (always on top)
{
    var divid = "card" + card;
    if ($(divid) == null) {
        DS.getCardText(pmap, 'showCard', game, card);
    } else {
        DWRUtil.setValue("cards", card);
        selectCard();
    }
}
function showCard(data) {
    var old = DWRUtil.getValue('extra');
    var text = data.text.join("<br />");
    DWRUtil.setValue('extra', old + "<div id=card" + data.id + ">" + text + "</div>");
    DWRUtil.addOptions("cards", [data], "id", "name");
    DWRUtil.setValue("cards", data.id);
    selectCard();
}
function selectCard() {
    if (DWRUtil.getValue("cards") == "NOCARD") {
        selectHistory();
    } else {
        var divid = "card" + DWRUtil.getValue("cards");
        var selected = DWRUtil.getValue("extraSelect");
        DWRUtil.setValue("extraSelect", divid);
        shide(divid, selected);
    }
}
function pmap(data) {
    for (var item in data) {
        eval(item + '(data[item]);');
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

function dochat(did, id, data) {
    var curScroll = $(did).scrollTop;
    $(did).scrollTop = 1000000;
    if ($(did).scrollTop == curScroll) {
        curScroll = 1000000;
    }
    var table = $(id);
    for (var idx in data) {
        table.insertRow(table.rows.length).insertCell(0).innerHTML = data[idx];
    }
    $(did).scrollTop = curScroll;
}

var refresher = null;
function loadGame(data) {
    if (!data.player) {
        $('hand').style.display = 'none';
        $('playerPad').style.display = 'none';
        if (!data.admin) {
            $('dsForm').style.display = 'none';
        }
    } else {
        $('hand').style.display = '';
        $('playerPad').style.display = '';
    }
    if (data.hand != null)
        DWRUtil.setValue('hand', data.hand);
    if (data.state != null)
        DWRUtil.setValue('state', data.state);
    if (data.global != null)
        DWRUtil.setValue('global', data.global);
    if (data.text != null)
        DWRUtil.setValue('notes', data.text);
    if (data.label != null) {
        DWRUtil.setValue('turnlabel', data.label);
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
        var sel = DWRUtil.getValue('ping');
        DWRUtil.removeAllOptions('ping');
        DWRUtil.addOptions('ping', {"NNNPPPP": "No ping"});
        DWRUtil.addOptions('ping', pingarr, 'value', 'key');
        DWRUtil.setValue('ping', sel);
    }
    if (data.turns != null) {
        var sel = DWRUtil.getValue('turns');
        var num = $('turns').options.length;
        DWRUtil.removeAllOptions('turns');
        DWRUtil.addOptions('turns', data.turns);
        if (num != data.turns.length && (data.turns.length == 1 || sel == data.turns[1])) {
            DWRUtil.setValue('turns', data.turns[0]);
        } else {
            DWRUtil.setValue('turns', sel);
        }
    }
    if (data.turn != null) {
        if (data.resetChat) {
            var table = $('curturntable');
            while (table.rows.length > 0) table.deleteRow(0);
        }
        dochat('curturn', 'curturntable', data.turn);
        var turncontent = data.turn.join('<br />');
        var val = DWRUtil.getValue('turns');
        var val2 = $('turns').options;
        if (data.turns != null) {
            DWRUtil.setValue('history', turncontent);
        } else if (DWRUtil.getValue('turns') == $('turns').options[0].value) {
            DWRUtil.setValue('history', DWRUtil.getValue('history') + "<br />" + turncontent);
        }
    }
    if (data.phases != null) {
        var phasev = DWRUtil.getValue('phase');
        $('phasecommand').style.display = '';
        $('endcommand').style.display = '';
        DWRUtil.removeAllOptions('phase');
        DWRUtil.addOptions('phase', data.phases);
        if (data.turnChanged) {
            phasev = 'Untap';
        }
        DWRUtil.setValue('phase', phasev);
    } else {
        $('phasecommand').style.display = 'none';
        $('endcommand').style.display = 'none';
    }
    if (data.collapsed != null) {
        for (var c in data.collapsed) {
            doToggle(data.collapsed[c]);
        }
    }
    if (data.stamp != null) {
        DWRUtil.setValue('gamestamp', data.stamp);
    }
}
function showStatus(data) {
    if (data != null) {
        DWRUtil.setValue('status', data);
    }
}

function dosubmit() {
    if ($('phase') != null) {
        phase = DWRUtil.getValue('phase');
    }
    var command = DWRUtil.getValue('command');
    DWRUtil.setValue('command', "");
    var chat = DWRUtil.getValue('chat');
    DWRUtil.setValue('chat', "");
    var ping = null;
    if ($('ping').selectedIndex > 0) {
        ping = DWRUtil.getValue('ping');
    }
    DWRUtil.setValue('ping', 'NNNPPPP');
    var endTurn = DWRUtil.getValue('endturn');
    if (endTurn == "Yes") DWRUtil.setValue('phase', "Untap");
    $('endturn').selectedIndex = 0;
    // TODO should only submit global and text if they've changed
    var global = DWRUtil.getValue('global');
    var text = DWRUtil.getValue('notes');
    DS.submitForm(pmap, game, phase, command, chat, ping, endTurn, global, text);
    return false;
}

function dsdebug(data) {
    //DWRUtil.setValue('dsdebug',DWRUtil.getValue('dsdebug') + "DEBUG " + data);
}

function ds2debug(data) {
    DWRUtil.setValue('dsdebug', DWRUtil.getValue('dsdebug') + "DEBUG " + data);
}

function loadHistory(data) {
    var text = data.join('<br/>');
    DWRUtil.setValue('history', text);
}

function retrieveHistory() {
    DS.getHistory(loadHistory, game, DWRUtil.getValue('turns'));
}

function selectHistory() {
    var id = DWRUtil.getValue("extraSelect");
    $('cards').selectedIndex = 0;
    DWRUtil.setValue("extraSelect", "history");
    shide('history', id);
}

function getHistory() {
    selectHistory();
    retrieveHistory();
}

function shide(s, h) {
    $(h).style.display = 'none';
    $(s).style.display = '';
}
function init() {
//	DWRUtil.useLoadingMessage();
    DS.getTypes(loadTypes);
    DS.init(pmap);
}
function loadTypes(data) {
    DWRUtil.addOptions('cardtype', data);
}
function dsnav(target) {
    DS.navigate(pmap, target);
}
var game = null;
function navigate(data) {
    shide('loaded', 'loadmsg');
    var selected = DWRUtil.getValue("contentselect");
    DWRUtil.setValue("contentselect", data.target);
    shide(data.target, selected);
    DWRUtil.setValue('buttons', '');
    dobuttons(data.playerButtons);
    dobuttons(data.adminButtons);
    dobuttons(data.gameButtons);
    DWRUtil.setValue('buttons', DWRUtil.getValue('buttons') + '<button onclick=dsnav("help");>Help</button>');
    if (data.player == null) {
        shide('logininputs', 'loggedin');
        DWRUtil.setValue('login', 'Log in');
    } else {
        DWRUtil.setValue('username', data.player);
        shide('loggedin', 'logininputs');
        DWRUtil.setValue('login', 'Log out');
    }
    game = data.game;
    if (game == null) {
        DWRUtil.setValue('gamename', '');
    } else if (DWRUtil.getValue('gamename') != game) {
        DWRUtil.setValue('gamename', game);
    }
}
function dobuttons(data) {
    var buttons = '';
    for (var item in data) {
        buttons += '<button onclick="dsnav(' + "'" + item + "'" + ');">' + data[item] + "</button>";
    }
    DWRUtil.setValue('buttons', DWRUtil.getValue('buttons') + buttons);
}
function addgamerow(tid, label) {
    var table = $(tid);
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
    var table = $(tid);
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
function loadmain(data) {
    if (data.loggedIn) {
        shide('player', 'register');
        $('globalchat').style.display = '';
        if (data.chat != null) {
            dochat('gchatwin', 'gchattable', data.chat);
        }
        if (data.who != null) {
            DWRUtil.setValue('whoson', data.who.join(', '));
        }
        if (data.admins != null) {
            DWRUtil.setValue('adson', data.admins.join(', '));
        }
        if (data.stamp != null) {
            DWRUtil.setValue('chatstamp', data.stamp);
        }
        if (data.myGames != null) {
            for (var i in data.myGames) {
                var row = addgamerow('owngames', data.myGames[i].game);
                if (row.cells.length == 0) {
                    row.insertCell(0);
                    row.insertCell(1);
                }
                if (data.myGames[i].started) {
                    row.cells[0].innerHTML = mkgamelink(data.myGames[i].game);
                    if (!data.myGames[i].current) {
                        row.cells[1].innerHTML = '*';
                    } else {
                        row.cells[1].innerHTML = '&nbsp';
                    }
                } else {
                    row.cells[0].innerHTML = data.myGames[i].game;
                    row.cells[1].innerHTML = 'C' + data.myGames[i].cryptSize + ' L' + data.myGames[i].libSize;
                }
            }
        }
    } else {
        shide('register', 'player');
        $('globalchat').style.display = 'none';
    }
    for (var i in data.remgames) {
        var row = findgamerow('activegames', data.remgames[i]);
        if (row >= 0) $('activegames').deleteRow(row);
        row = findgamerow('owngames', data.remgames[i]);
        if (row >= 0) $('owngames').deleteRow(row);
    }
    for (var i in data.games) {
        var row = addgamerow('activegames', data.games[i].game);
        if (row.cells.length == 0) {
            row.insertCell(0);
            row.insertCell(1);
        }
        if (data.games[i].turn == null) {
            row.cells[0].innerHTML = data.games[i].game;
            row.cells[1].innerHTML = 'forming';
            row.cells[1].colspan = 3;
        } else {
            if (row.cells.length == 2) {
                row.cells[1].colspan = '1';
                row.insertCell(2);
                row.insertCell(3);
            }
            row.cells[0].innerHTML = mkgamelink(data.games[i].game);
            row.cells[1].innerHTML = data.games[i].access;
            row.cells[2].innerHTML = data.games[i].turn;
            row.cells[3].innerHTML = '&nbsp ' + data.games[i].available.join(',');
            //row.cells[3].class = 'gameplayers';
        }
    }
    var news = '';
    for (var i in data.news) {
        news += '<p><a href="' + data.news[i].url + '">' + data.news[i].text + "</a></p>";
    }
    $('news').innerHTML = news;
    if (data.refresh > 0) {
        if (refresher != null) clearTimeout(refresher);
        refresher = setTimeout("DS.doPoll(pmap)", data.refresh);
    }
}
function globchat() {
    DS.chat(pmap, DWRUtil.getValue('gchat'));
    DWRUtil.setValue('gchat', '');
}
function doedit() {
    shide('deckedit', 'noedit');
    $('deckname').readOnly = null;
    $('decktext').readOnly = null;
}
function donewdeck() {
    doedit();
    var name = findname();
    DWRUtil.setValue('deckname', name);
    var newdeck = new Object();
    newdeck.text = '';
    newdeck.format = '';
    newdeck.errors = '';
    showDeck(newdeck);
}
function findname() {
    var idx = 1;
    var rows = $('decks').rows
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
    DS.refreshDeck(pmap, DWRUtil.getValue('deckname'), DWRUtil.getValue('decktext'), DWRUtil.getValue('shuffle'));
}
function dosave() {
    shide('noedit', 'deckedit');
    $('deckname').readOnly = 'readonly';
    $('decktext').readOnly = 'readonly';
    DS.submitDeck(pmap, DWRUtil.getValue('deckname'), DWRUtil.getValue('decktext'));
}
function repldeckname(data) {
    DWRUtil.setValue('deckname', data);
}
function showDeck(data) {
    if (data.text != null) DWRUtil.setValue('decktext', data.text);
    $('deckcontents').innerHTML = data.format;
    if (data.errors != null) DWRUtil.setValue('deckerrors', data.errors.join('<br />'));
}
function loaddeck(deck) {
    DWRUtil.setValue('deckname', deck);
    DS.getDeck(pmap, deck);
}
function showDecks(data) {
    for (var i in data.decks) {
        var row = addgamerow('decks', data.decks[i].name);
        if (row.cells.length == 0) {
            row.insertCell(0).innerHTML = '<a onclick="loaddeck(' + "'" + data.decks[i].name + "');" + '">' + data.decks[i].name + '</a>';
            row.insertCell(1);
        }
        row.cells[1].innerHTML = 'L' + data.decks[i].lib + ' C' + data.decks[i].crypt + ' G ' + data.decks[i].groups;
    }
    for (var i in data.games) {
        var row = addgamerow('opengames', data.games[i].game);
        if (row.cells.length == 0) {
            row.insertCell(0).innerHTML = data.games[i].game;
            row.insertCell(1);
            row.insertCell(2);
        }
        row.cells[1].innerHTML = data.games[i].name;
        row.cells[2].innerHTML = 'L' + data.games[i].lib + ' C' + data.games[i].crypt + ' G ' + data.games[i].groups;
    }
    DWRUtil.removeAllOptions('reggames');
    DWRUtil.removeAllOptions('regdecks');
    DWRUtil.addOptions('reggames', data.games, 'game', 'game');
    DWRUtil.addOptions('regdecks', data.decks, 'name', 'name');
}
function doregister() {
    DS.registerDeck(pmap, DWRUtil.getValue('reggames'), DWRUtil.getValue('regdecks'));
}
function showCards(data) {
    var len = $('showcards').rows.length;
    for (i = 0; i < len; i++) {
        $('showcards').deleteRow(0);
    }
    for (var i in data) {
        $('showcards').insertRow(0).insertCell(0).innerHTML = '<a onclick="javascript:getCardDeck(null,' + "'" + data[i].id + "'" + ');">' + data[i].name + '</a>';
    }
}
// cut pasted and altered from top
function getCardDeck(game, card) // Open card text in separate window (always on top)
{
    var divid = "dcard" + card;
    if ($(divid) == null) {
        DS.getCardText(pmap, 'showCardDeck', game, card);
    } else {
        DWRUtil.setValue("deckcards", card);
        selectCardDeck();
    }
}
function showCardDeck(data) {
    var old = DWRUtil.getValue('cardtext');
    var text = data.text.join("<br />");
    DWRUtil.setValue('cardtext', old + "<div id=dcard" + data.id + ">" + text + "</div>");
    DWRUtil.addOptions("deckcards", [data], "id", "name");
    DWRUtil.setValue("deckcards", data.id);
    selectCardDeck();
}
function selectCardDeck() {
    var divid = "dcard" + DWRUtil.getValue("deckcards");
    var selected = DWRUtil.getValue("cardSelect");
    DWRUtil.setValue("cardSelect", divid);
    shide(divid, selected);
}
function dosearch() {
    DS.cardSearch(pmap, DWRUtil.getValue("cardtype"), DWRUtil.getValue("cardquery"));
}
function creategame() {
    DS.createGame(pmap, DWRUtil.getValue("creategame"));
    DWRUtil.setValue("creategame", '');
}
function inviteplayer(game) {
    DS.invitePlayer(pmap, game, DWRUtil.getValue('cgs' + game));
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
            DWRUtil.addOptions('cgs' + game, data.players);
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
    DWRUtil.removeAllOptions('endgameselector');
    DWRUtil.addOptions('endgameselector', data.runningGames);
}
function loadsuper(data) {

}
function beuser() {
    DS.dosu(getsuper, DWRUtil.getValue("userspec"));
}
function unend() {
    DS.unend(getsuper, DWRUtil.getValue("gamespec"));
}
function closegames() {
    DS.endGames(pmap, DWRUtil.getValue("endgameselector"));
}
function getinfo() {
    DS.inspect(getsuper, DWRUtil.getValue("icmd"));
}
function dosuper() {
    DS.doCommand(getsuper, DWRUtil.getValue("scmd"));
}
function getsuper(data) {
    DWRUtil.setValue('sres', data);
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
    DWRUtil.setValue('bugdetail', data.details);
    DWRUtil.removeAllRows('commenttable');
    for (var comment in data.comments) {
        var row = $('commenttable').insertRow();
        row.insertCell(0);
        row.cells[0].innerHTML = commment;
    }
}