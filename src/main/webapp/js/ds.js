"use strict";

// ---------------------------------------------------------------------------
// REST API client — replaces DWR-generated DS.js
// ---------------------------------------------------------------------------
const _ctx = '/jol/api';

function _enc(s) { return encodeURIComponent(s); }

function apiCall(method, path, body, opts) {
    const init = { method, headers: {'Content-Type': 'application/json'} };
    if (body !== null && body !== undefined) init.body = JSON.stringify(body);
    return fetch(_ctx + path, init)
        .then(r => {
            if (!r.ok) return r.text().then(msg => { throw new Error(`${r.status}: ${msg || r.statusText}`); });
            if (r.status === 204) return {};
            return r.json();
        })
        .then(data => opts?.callback && opts.callback(data))
        .catch(err => opts?.errorHandler && opts.errorHandler(String(err)));
}

function apiGet(path, opts) { return apiCall('GET', path, null, opts); }
function apiPost(path, body, opts) { return apiCall('POST', path, body, opts); }
function apiPut(path, body, opts) { return apiCall('PUT', path, body, opts); }
function apiDel(path, opts) { return apiCall('DELETE', path, null, opts); }

function apiGetText(path, opts) {
    return fetch(_ctx + path)
        .then(r => { if (!r.ok) throw new Error(r.statusText); return r.text(); })
        .then(data => opts?.callback && opts.callback(data))
        .catch(err => opts?.errorHandler && opts.errorHandler(String(err)));
}

const DS = {
    // Navigation / polling
    init:                    (target, opts) => apiPost('/navigate', {target, init: true}, opts),
    navigate:                (target, opts) => apiPost('/navigate', {target}, opts),
    doPoll:                  (opts) => apiGet('/poll', opts),

    // Global chat
    chat:                    (text, opts) => apiPost('/chat', {text}, opts),

    // Lobby
    createGame:              (name, publicFlag, format, opts) => apiPost('/lobby/games', {name, publicFlag, format}, opts),
    startGame:               (game, opts) => apiPost(`/lobby/games/${_enc(game)}/start`, {}, opts),
    invitePlayer:            (game, player, opts) => apiPost(`/lobby/games/${_enc(game)}/invite`, {player}, opts),
    unInvitePlayer:          (game, player, opts) => apiDel(`/lobby/games/${_enc(game)}/invite/${_enc(player)}`, opts),
    registerDeck:            (gameName, deckName, opts) => apiPost(`/lobby/games/${_enc(gameName)}/deck`, {deckName}, opts),

    // Game actions
    submitForm:              (game, phase, command, chat, ping, opts) => apiPost(`/game/${_enc(game)}/submit`, {phase, command, chat, ping}, opts),
    endPlayerTurn:           (game, opts) => apiPost(`/game/${_enc(game)}/end-turn`, {}, opts),
    endTurn:                 (game, opts) => apiPost(`/game/${_enc(game)}/force-end-turn`, {}, opts),
    endGame:                 (name, opts) => apiDel(`/game/${_enc(name)}`, opts),
    gameChat:                (game, chat, opts) => apiPost(`/game/${_enc(game)}/chat`, {chat}, opts),
    doToggle:                (game, id, opts) => apiPost(`/game/${_enc(game)}/toggle/${_enc(id)}`, {}, opts),
    updateGlobalNotes:       (game, notes, opts) => apiPut(`/game/${_enc(game)}/notes/global`, {notes}, opts),
    updatePrivateNotes:      (game, notes, opts) => apiPut(`/game/${_enc(game)}/notes/private`, {notes}, opts),
    getState:                (game, forceLoad, opts) => apiPost(`/game/${_enc(game)}/state`, {forceLoad}, opts),
    rollbackGame:            (game, turn, opts) => apiPost(`/game/${_enc(game)}/rollback`, {turn}, opts),
    replacePlayer:           (game, existingPlayer, newPlayer, opts) => apiPut(`/game/${_enc(game)}/replace-player`, {existingPlayer, newPlayer}, opts),
    getGameDeck:             (game, opts) => apiGet(`/game/${_enc(game)}/deck`, opts),
    getGamePlayers:          (game, opts) => apiGet(`/game/${_enc(game)}/players`, opts),
    getGameTurns:            (game, opts) => apiGet(`/game/${_enc(game)}/turns`, opts),
    getHistory:              (game, turn, opts) => apiGet(`/game/${_enc(game)}/history?turn=${_enc(turn || '')}`, opts),

    // Decks
    filterDecks:             (filter, opts) => apiGet(`/decks?filter=${_enc(filter || '')}`, opts),
    saveDeck:                (deckName, contents, comment, opts) => apiPost('/decks', {deckName, contents, comment}, opts),
    deleteDeck:              (deckName, opts) => apiDel(`/decks/${_enc(deckName)}`, opts),
    loadDeck:                (deckName, opts) => apiPost('/decks/load', {deckName}, opts),
    newDeck:                 (opts) => apiPost('/decks/new', {}, opts),
    validate:                (contents, format, opts) => apiPost('/decks/validate', {contents, format}, opts),
    parseDeck:               (deckName, contents, opts) => apiPost('/decks/validate', {contents, format: ''}, opts),

    // User profile
    updateProfile:           (email, discordID, veknID, country, opts) => apiPut('/user/profile', {email, discordID, veknID, country}, opts),
    changePassword:          (newPassword, opts) => apiPut('/user/password', {newPassword}, opts),
    setUserPreferences:      (imageTooltips, opts) => apiPut('/user/preferences', {imageTooltips}, opts),
    setEdgeColor:            (color, opts) => apiPut('/user/edge-color', {color}, opts),

    // Admin
    setRole:                 (player, role, value, opts) => apiPut(`/admin/player/${_enc(player)}/role`, {role, value}, opts),
    deletePlayer:            (playerName, opts) => apiDel(`/admin/player/${_enc(playerName)}`, opts),
    setMessage:              (message, opts) => apiPost('/admin/message', {message}, opts),
    getVekn:                 (playerName, opts) => apiGet(`/admin/player/${_enc(playerName)}/vekn`, opts),
    exportPastGamesAsCsv:    (opts) => apiGetText('/admin/export/games.csv', opts),

    // Tournament
    createTournament:        (tourName, regStart, regEnd, playStart, playEnd, tourFormat, gameFormat, rules, specRulesCon, specRules, numberOfRounds, reqId, originalName, opts) =>
                                 apiPost('/tournament', {tourName, regStart, regEnd, playStart, playEnd, tourFormat, gameFormat, rules, specRulesCon, specRules, numberOfRounds, reqId, originalName}, opts),
    loadTournamentDetails:   (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/details`, opts),
    getRoundsForTournament:  (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/rounds`, opts),
    getRoundsForTournamentCsv: (tourName, opts) => apiGetText(`/tournament/${_enc(tourName)}/rounds/csv`, opts),
    getTournamentPlayers:    (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/players`, opts),
    createTournamentTables:  (tourName, opts) => apiPost(`/tournament/${_enc(tourName)}/tables`, {}, opts),
    saveTables:              (tourName, rounds, opts) => apiPut(`/tournament/${_enc(tourName)}/rounds`, rounds, opts),
    importTables:            (tourName, csvData, opts) => apiPost(`/tournament/${_enc(tourName)}/rounds/import`, {csvData}, opts),
    createFinalTable:        (tourName, opts) => apiPost(`/tournament/${_enc(tourName)}/final`, {}, opts),
    setFinalSeeding:         (tourName, seeding, opts) => apiPut(`/tournament/${_enc(tourName)}/seeding`, seeding, opts),
    loadFinalSeeding:        (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/seeding`, opts),
    closeTournament:         (tourName, opts) => apiPost(`/tournament/${_enc(tourName)}/close`, {}, opts),
    joinTournament:          (game, opts) => apiPost(`/tournament/${_enc(game)}/join`, {}, opts),
    leaveTournament:         (game, opts) => apiPost(`/tournament/${_enc(game)}/leave`, {}, opts),
    registerTournamentDeck:  (tournament, deckName, opts) => apiPost(`/tournament/${_enc(tournament)}/deck`, {deckName}, opts),
    resetTables:             (tourName, opts) => apiDel(`/tournament/${_enc(tourName)}/rounds`, opts),
    getFinalPlayers:         (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/final-players`, opts),
    saveFinal:               (tourName, players, opts) => apiPut(`/tournament/${_enc(tourName)}/final-players`, players, opts),
    getRegDelta:             (tourName, round, opts) => apiGet(`/tournament/${_enc(tourName)}/round-delta?round=${round}`, opts),
    loadCrypt:               (tourName, player, opts) => apiGet(`/tournament/${_enc(tourName)}/crypt?player=${_enc(player)}`, opts),
    cryptCount:              (tourName, player, opts) => apiGet(`/tournament/${_enc(tourName)}/crypt-count?player=${_enc(player)}`, opts),
    tournamentAlreadyActive: (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/status`, opts),
    gameAlreadyStarted:      (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/game-started`, opts),
    getTournamentRounds:     (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/rounds-count`, opts),
    publishTournament:       (tourName, opts) => apiPost(`/tournament/${_enc(tourName)}/publish`, {}, opts),
    getRoundSummary:         (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/round-summary`, opts),
    closeTableGame:          (tourName, round, table, opts) => apiPost(`/tournament/${_enc(tourName)}/round/${round}/table/${table}/close`, {}, opts),
    getStandings:            (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/standings`, opts),
    getAllRegisteredPlayers:  (tourName, opts) => apiGet(`/tournament/${_enc(tourName)}/registered`, opts),
};
// ---------------------------------------------------------------------------

let version = null;
let refresher = null;
let game = null;
let ws = null;
let wsConnected = false;
let wsHeartbeat = null;
let wsReconnect = null;
let player = null;
let currentPage = 'main';
let USER_TIMEZONE = moment.tz.guess();
let gameChatLastDay = null;
let globalChatLastPlayer = null;
let globalChatLastDay = null;
let TITLE = 'V:TES Online';
let DESKTOP_VIEWPORT_CONTENT = 'width=1024';
let profile = {
    email: "",
    discordID: "",
    updating: false,
    imageTooltipPreference: true,
    edgeColor: "#FFFFFF"
};
let subscribed =  localStorage.getItem("notifications-subscribed") === "true";

let pointerCanHover = window.matchMedia("(hover: hover)").matches;
let scrollChat = false;
let lastReceivedGlobalNotes = null;
let lastReceivedPrivateNotes = null;
const regionNames = new Intl.DisplayNames(['en'], { type: 'region' });

function errorhandler(errorString) {
    if (errorString && errorString.startsWith('401')) {
        location.href = '/jol/login';
        return;
    }
    $("#connectionMessage").removeClass("d-none");
    refresher = setTimeout(function() {
        DS.navigate(null, {callback: processData, errorHandler: errorhandler});
    }, 5000);
}

$(document).ready(function () {
    moment.tz.load({
        zones: [],
        links: [],
        version: '2024b'
    });
    const parts = window.location.pathname.replace(/^\/jol\//, '').split('/');
    let initialTarget = parts[0] || 'main';
    if (initialTarget === 'game' && parts[1]) initialTarget = 'g' + decodeURIComponent(parts[1]);
    DS.init(initialTarget, {callback: init, errorHandler: errorhandler});
    window.addEventListener('popstate', function(e) {
        const t = e.state && e.state.target ? e.state.target : 'main';
        DS.navigate(t, {callback: processData, errorHandler: errorhandler});
    });
});

function init(data) {
    if (localStorage.getItem("jol-theme") === "dark") {
        $("#wrapper").attr("data-bs-theme", "dark");
    }
    processData(data);
    $("h4.collapse").click(function () {
        $(this).next().slideToggle();
    });
    initWebSocket();
}

function initWebSocket() {
    if (wsReconnect) {
        clearTimeout(wsReconnect);
        wsReconnect = null;
    }
    if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
        return;
    }

    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const url = `${proto}//${location.host}/jol/ws/updates`;
    console.log('[WS] Connecting to', url);
    ws = new WebSocket(url);

    ws.onopen = () => {
        wsConnected = true;
        $("#wsStatus").addClass("d-none");
        if (refresher) clearTimeout(refresher);
        console.log('[WS] Connected — push notifications active, polling suspended');
        // Re-join the game room if we're already on a game page (e.g. after reconnect)
        if (currentPage === 'game' && game) wsJoinGame(game);
        if (wsHeartbeat) clearInterval(wsHeartbeat);
        wsHeartbeat = setInterval(() => {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({type: 'ping'}));
            }
        }, 30000);
    };

    ws.onmessage = (evt) => {
        const msg = JSON.parse(evt.data);
        if (msg.type === 'pong') {
            if (msg.version) checkVersion(msg.version);
            return;
        }
        console.log('[WS] Notification received:', msg, '(currentPage=' + currentPage + ', game=' + game + ')');
        if (refresher) clearTimeout(refresher);
        if (msg.type === 'game') {
            if (currentPage === 'game' && game === msg.id) {
                console.log('[WS] Triggering game refresh');
                refreshState(false);
            } else if (currentPage === 'main') {
                console.log('[WS] Game changed, refreshing main page');
                DS.doPoll({callback: processData, errorHandler: errorhandler});
            }
        } else if (msg.type === 'main' && currentPage === 'main') {
            console.log('[WS] Main state changed, refreshing');
            DS.doPoll({callback: processData, errorHandler: errorhandler});
        }
    };

    ws.onclose = (evt) => {
        wsConnected = false;
        if (wsHeartbeat) {
            clearInterval(wsHeartbeat);
            wsHeartbeat = null;
        }
        if (evt.code === 1008 || evt.reason === 'Unauthorized') {
            location.href = '/jol/login';
            return;
        }
        $("#wsStatus").removeClass("d-none");
        console.log('[WS] Connection closed (code=' + evt.code + '), falling back to polling, reconnecting in 5s');
        if (currentPage === 'main') {
            DS.doPoll({callback: processData, errorHandler: errorhandler});
        } else if (currentPage === 'game' && game) {
            refreshState(false);
        }
        wsReconnect = setTimeout(initWebSocket, 5000);
    };

    ws.onerror = (evt) => {
        console.error('[WS] Connection error', evt);
        ws.close();
    };
}

function setPreferences(value) {
    profile.imageTooltipPreference = value;
}

function setEdgeColorPref(value) {
    profile.edgeColor = value;
}

const processDataHandlers = {
    checkVersion, navigate, loadGame,
    callbackMain, callbackLobby, callbackShowDecks, callbackAdmin,
    callbackProfile, callbackTournament, callbackTournamentAdmin,
    callbackAllGames, showStatus, setPreferences, setEdgeColorPref,
};

function processData(a) {
    $("#connectionMessage").addClass("d-none");
    for (const key in a) {
        const fn = processDataHandlers[key];
        if (fn) {
            fn(a[key]);
        } else {
            console.warn('[processData] unhandled key:', key);
        }
    }
}

function checkVersion(newVersion) {
    if (version === null) {
        version = newVersion;
    } else if (version !== newVersion) {
        alert("JOL version has changed. The application will reload.");
        location.reload();
    }
}

function callbackAllGames(data) {
    renderActiveGames(data.games);
    renderPastGames(data.history);
}

$(document).on('shown.bs.tab', '[data-bs-target="#pastGamesPane"]', function () {
    $('#exportCsvBtn').removeClass('d-none');
});
$(document).on('hidden.bs.tab', '[data-bs-target="#pastGamesPane"]', function () {
    $('#exportCsvBtn').addClass('d-none');
});

function createButton(config, fn, ...args) {
    let button = $("<button/>");
    if (config.text) {
        button.text(config.text);
    } else if (config.html) {
        button.html(config.html);
    }
    button.addClass(config.class);
    button.on('click', function () {
        if (!config.confirm || confirm(config.confirm)) {
            fn(...args, {callback: processData, errorHandler: errorhandler});
        }
    });
    return button;
}

function callbackAdmin(data) {
    let userRoles = $("#userRoles")
    userRoles.empty();
    $.each(data.userRoles, function (index, value) {
        let playerRow = $("<tr/>");
        let nameCell = $("<td/>").text(value.name);
        let onlineCell = $("<td/>").text(moment(value.lastOnline).tz("UTC").format("D-MMM-YY HH:mm z"));
        let removeJudgeButton = value.roles.includes("JUDGE") ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setRole, value.name, "JUDGE", false) : "";
        let judgeCell = $("<td/>").addClass("text-center").append(removeJudgeButton);
        let removeSuperButton = value.roles.includes("SUPER_USER") ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setRole, value.name, "SUPER_USER", false) : "";
        let superCell = $("<td/>").addClass("text-center").append(removeSuperButton);
        let removePlaytestButton = value.roles.includes("SUPER_USER") ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setRole, value.name, "PLAYTESTER", false) : "";
        let playtestCell = $("<td/>").addClass("text-center").append(removePlaytestButton);
        let removeAdminButton = value.roles.includes("PLAYTESTER") ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setRole, value.name, "ADMIN", false) : "";
        let adminCell = $("<td/>").addClass("text-center").append(removeAdminButton);
        let removeTournamentButton = value.roles.includes("TOURNAMENT_ADMIN") ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setRole, value.name, "TOURNAMENT_ADMIN", false) : "";
        let tournamentCell = $("<td/>").addClass("text-center").append(removeTournamentButton);
        playerRow.append(nameCell, onlineCell, judgeCell, superCell, playtestCell, adminCell, tournamentCell);
        userRoles.append(playerRow);
    })
    let adminReplacementList = $("#adminReplacementList");
    let adminPlayerList = $("#adminPlayerList");
    adminReplacementList.empty();
    adminPlayerList.empty();
    $.each(data.substitutes, function (index, value) {
        let replacementOption = $("<option/>", {value: value, text: value});
        adminReplacementList.append(replacementOption);
        let playerOption = $("<option/>", {value: value, text: value});
        adminPlayerList.append(playerOption);
    })
    let deletePlayerList = $("#deletePlayerList");
    deletePlayerList.empty();
    $.each(data.players, function (index, value) {
        let playerRow = $("<tr/>");
        let nameCell = $("<td/>").text(value.name);
        let onlineCell = $("<td/>").text(moment(value.lastOnline).tz("UTC").format("D-MMM-YY HH:mm z"));
        let legacyDeckCell = $("<td/>").text(value.legacyDeckCount);
        let modernDeckCell = $("<td/>").text(value.modernDeckCount);
        let deletePlayerButton = value.activeGamesCount === 0 ? createButton({
            text: "Remove",
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this player?"
        }, DS.deletePlayer, value.name) : "";
        let deleteCell = $("<td/>").append(deletePlayerButton);
        playerRow.append(nameCell, onlineCell, legacyDeckCell, modernDeckCell, deleteCell);
        deletePlayerList.append(playerRow);
    })

    let adminGameList = $("#adminGameList");
    let rollbackGamList = $("#rollbackGamesList");
    let endTurnList = $("#endTurnList");
    adminGameList.empty();
    rollbackGamList.empty();
    $.each(data.games, function (id, name) {
        adminGameList.append($("<option/>", {value: id, text: name}));
        endTurnList.append($("<option/>", {value: id, text: name}));
        rollbackGamList.append($("<option/>", {value: id, text: name}));
    })
    adminChangeGame();
    rollbackChangeGame();

    let idleGameList = $("#idleGameList");
    idleGameList.empty();
    $.each(data.idleGames, function (index, gameEntry) {
        let playerCount = Object.keys(gameEntry.idlePlayers).length;
        let firstPlayerRow = true;
        $.each(gameEntry.idlePlayers, function (key, value) {
            let row = $("<tr/>");
            if (firstPlayerRow) {
                let nameCell = $("<td/>").attr('rowspan', playerCount).text(gameEntry.gameName).on('click', function () {
                    doNav('g' + gameEntry.gameId);
                });
                let timestampCell = $("<td/>").attr('rowspan', playerCount).text(moment(gameEntry.gameTimestamp).tz("UTC").format("D-MMM-YY HH:mm z"));
                row.append(nameCell, timestampCell);
            }
            let playerCell = $("<td/>").text(key);
            let playerTimeCell = $("<td/>").text(moment(value).tz("UTC").format("D-MMM-YY HH:mm z"));
            row.append(playerCell, playerTimeCell);
            if (firstPlayerRow) {
                let endGameCell = $("<td/>").attr('rowspan', playerCount);
                let endGameButton = createButton({
                    text: "Close",
                    class: "btn btn-outline-secondary btn-sm",
                    confirm: "Are you sure you want to end this game?"
                }, DS.endGame, gameEntry.gameId);
                endGameCell.append(endGameButton);
                row.append(endGameCell);
                firstPlayerRow = false;
            }
            idleGameList.append(row);
        })
    })
}

function adminChangeGame() {
    let currentGame = $("#adminGameList").val();
    DS.getGamePlayers(currentGame, {callback: setPlayers, errorHandler: errorhandler});
}

function rollbackChangeGame() {
    let currentGame = $("#rollbackGamesList").val();
    DS.getGameTurns(currentGame, {callback: setRollbackTurns, errorHandler: errorhandler});
}

function rollbackGame() {
    let currentGame = $("#rollbackGamesList").val();
    let currentTurn = $("#rollbackTurnsList").val();
    if (confirm("Are you sure you want to rollback to turn " + currentTurn + " for " + currentGame)) {
        DS.rollbackGame(currentGame, currentTurn, {callback: processData, errorHandler: errorhandler});
    }
}

function setPlayers(data) {
    let adminReplacePlayerList = $("#adminReplacePlayerList");
    adminReplacePlayerList.empty();
    $.each(data, function (index, value) {
        let playerOption = $("<option/>", {value: value, text: value});
        adminReplacePlayerList.append(playerOption);
    })
}

function setRollbackTurns(data) {
    let rollbackTurnsList = $("#rollbackTurnsList");
    rollbackTurnsList.empty();
    $.each(data, function (index, value) {
        let rollbackTurnOption = $("<option/>", {value: value, text: value});
        rollbackTurnsList.append(rollbackTurnOption);
    })
}

function replacePlayer() {
    let currentGame = $("#adminGameList").val();
    let existingPlayer = $("#adminReplacePlayerList").val();
    let newPlayer = $("#adminReplacementList").val();
    DS.replacePlayer(currentGame, existingPlayer, newPlayer, {callback: processData, errorHandler: errorhandler});
}

function adminEndTurn() {
    let currentGame = $("#endTurnList").val();
    if (confirm("Are you sure you want to end turn for " + currentGame)) {
        DS.endTurn(currentGame, {callback: processData, errorHandler: errorhandler});
    }
}

function addRole() {
    let player = $("#adminPlayerList").val();
    let role = $("#adminRoleList").val();
    DS.setRole(player, role, true, {callback:processData});
}

// Lobby state
let _lobbyGames = [];
let _lobbyCurrentGame = null;
let _lobbyPlayers = [];
let _lobbyDecks = [];

function callbackLobby(data) {
    _lobbyGames = data.games || [];
    _lobbyPlayers = data.players || [];
    _lobbyDecks = data.decks || [];

    // Populate format dropdown in create panel
    let createFormat = $("#lobbyGameFormat");
    createFormat.empty();
    $.each(data.gameFormats, function (index, value) {
        createFormat.append($("<option/>", {value: value, text: value}));
    });

    // Player autocomplete for both create and detail invite inputs
    $("#lobbyInviteInput, #lobbyDetailInviteInput").autocomplete({
        source: _lobbyPlayers,
        change: function (event, ui) {
            if (ui.item === null) $(this).val("");
        }
    });

    // Render unified game list
    let list = $("#lobbyGameList").empty();
    $.each(_lobbyGames, function (index, game) {
        let visClass = game.visibility === "PUBLIC" ? "bg-success-subtle text-success-emphasis border border-success-subtle"
                                                    : "bg-secondary-subtle text-secondary-emphasis border border-secondary-subtle";
        let relLabel = {OWNER: "Owner", REGISTERED: "Registered", INVITED: "Invited", OPEN: "Open"}[game.playerRelationship] || "";
        let relClass = {
            OWNER: "text-primary", REGISTERED: "text-success", INVITED: "text-warning-emphasis", OPEN: "text-muted"
        }[game.playerRelationship] || "text-muted";

        let registeredCount = game.registrations.filter(r => r.registered).length;
        let totalCount = game.registrations.length;
        let playerCount = totalCount > 0 ? `${registeredCount}/${totalCount} <i class="bi bi-person"></i>` : "";
        let countdown = game.visibility === "PUBLIC"
            ? `<small class="text-muted">closes ${moment().to(moment(game.created).add(5, 'days'))}</small>`
            : "";

        let item = $(`
            <a href="#" class="list-group-item list-group-item-action px-3 py-2">
                <div class="d-flex justify-content-between align-items-start">
                    <span class="fw-semibold text-break me-2">${game.name}</span>
                    <span class="badge ${visClass} text-nowrap">${game.visibility}</span>
                </div>
                <div class="d-flex justify-content-between align-items-center mt-1">
                    <span class="badge bg-secondary">${game.format}</span>
                    <small class="${relClass}">${relLabel}</small>
                </div>
                ${countdown || playerCount ? `<div class="d-flex justify-content-between align-items-center mt-1">
                    <span class="small text-muted">${playerCount}</span>
                    ${countdown}
                </div>` : ""}
            </a>
        `).on('click', function (e) {
            e.preventDefault();
            selectLobbyGame(game);
        });
        list.append(item);
    });

    // If a game was selected before refresh, re-select it by name to update detail panel
    if (_lobbyCurrentGame) {
        let refreshed = _lobbyGames.find(g => g.name === _lobbyCurrentGame.name);
        if (refreshed) {
            selectLobbyGame(refreshed);
        }
    }

    if (data.message) {
        let msg = $("#lobbyDetailMsg");
        msg.text(data.message).removeClass("d-none");
        setTimeout(() => msg.addClass("d-none"), 4000);
    }
}

function newLobbyGame() {
    _lobbyCurrentGame = null;
    $("#lobbyGameName").val("");
    $("#lobbyPendingInvites").empty();
    $("#lobbyCreateError").text("");
    toggleLobbyInviteSection();
    $("#lobbyCreateCol").removeClass("d-none");
    $("#lobbyDetailCol").addClass("d-none");
    $("#lobbyEmptyCol").addClass("d-none");
}

function toggleLobbyInviteSection() {
    let isPrivate = $("#lobbyPublicFlag").val() === "PRIVATE";
    $("#lobbyInviteSection").toggleClass("d-none", !isPrivate);
}

function addLobbyPendingInvite() {
    let name = $("#lobbyInviteInput").val().trim();
    if (!name) return;
    let existing = $("#lobbyPendingInvites li").map(function() { return $(this).data("player"); }).get();
    if (existing.includes(name)) return;
    let item = $(`<li class="list-group-item d-flex justify-content-between align-items-center py-1 px-2">
        <span>${name}</span>
        <button type="button" class="btn-close btn-sm" aria-label="Remove"></button>
    </li>`).data("player", name);
    item.find(".btn-close").on("click", function() { item.remove(); });
    $("#lobbyPendingInvites").append(item);
    $("#lobbyInviteInput").val("").autocomplete("close");
}

function doCreateLobbyGame() {
    let gameName = $("#lobbyGameName").val().trim();
    let publicFlag = $("#lobbyPublicFlag").val();
    let format = $("#lobbyGameFormat").val();
    if (!gameName) { $("#lobbyCreateError").text("Name is required."); return; }
    if (gameName.indexOf("'") > -1 || gameName.indexOf('"') > -1) {
        $("#lobbyCreateError").text("Name cannot contain ' or \" characters.");
        return;
    }
    let pendingInvites = $("#lobbyPendingInvites li").map(function() { return $(this).data("player"); }).get();
    DS.createGame(gameName, publicFlag, format, {
        callback: function(data) {
            processData(data);
            let newGame = (_lobbyGames || []).find(g => g.name === gameName);
            if (newGame) {
                selectLobbyGame(newGame);
                // Fire invites sequentially for private games
                if (publicFlag === "PRIVATE") {
                    pendingInvites.forEach(function(p) {
                        DS.invitePlayer(gameName, p, {callback: processData, errorHandler: errorhandler});
                    });
                }
            }
        },
        errorHandler: errorhandler
    });
    $("#lobbyGameName").val("");
    $("#lobbyPendingInvites").empty();
}

function selectLobbyGame(game) {
    _lobbyCurrentGame = game;

    // Header
    $("#lobbyDetailName").text(game.name);
    $("#lobbyDetailFormatBadge").text(game.format);
    let visEl = $("#lobbyDetailVisibilityBadge");
    if (game.visibility === "PUBLIC") {
        visEl.text("Public").removeClass("bg-secondary").addClass("bg-success");
    } else {
        visEl.text("Private").removeClass("bg-success").addClass("bg-secondary");
    }

    // Action buttons — all hidden first
    $("#lobbyDetailStartBtn, #lobbyDetailCloseBtn, #lobbyDetailJoinBtn, #lobbyDetailLeaveBtn").addClass("d-none");
    if (game.playerRelationship === "OWNER") {
        if (game.gameStatus === "Inviting") $("#lobbyDetailStartBtn").removeClass("d-none");
        $("#lobbyDetailCloseBtn").removeClass("d-none");
    } else if (game.playerRelationship === "OPEN") {
        $("#lobbyDetailJoinBtn").removeClass("d-none");
    } else if (game.playerRelationship === "REGISTERED" || game.playerRelationship === "INVITED") {
        $("#lobbyDetailLeaveBtn").removeClass("d-none");
    }

    // Player table
    let tbody = $("#lobbyDetailPlayerBody").empty();
    $.each(game.registrations, function (i, reg) {
        let statusIcon = reg.registered
            ? `<i class="bi bi-check-circle text-success"></i>`
            : `<i class="bi bi-hourglass text-muted"></i>`;
        let removeBtn = "";
        if (game.playerRelationship === "OWNER") {
            removeBtn = `<button class="btn btn-sm btn-outline-danger py-0 px-1 remove-invite-btn" data-player="${reg.player}"><i class="bi bi-x"></i></button>`;
        }
        let row = $(`<tr>
            <td>${reg.player}</td>
            <td class="text-center">${statusIcon}</td>
            <td class="text-end">${removeBtn}</td>
        </tr>`);
        row.find(".remove-invite-btn").on("click", function() {
            let p = $(this).data("player");
            DS.unInvitePlayer(_lobbyCurrentGame.name, p, {callback: processData, errorHandler: errorhandler});
        });
        tbody.append(row);
    });

    // Invite section (owner only)
    let inviteSection = $("#lobbyDetailInviteSection");
    if (game.playerRelationship === "OWNER") {
        inviteSection.removeClass("d-none");
        $("#lobbyDetailInviteInput").val("").autocomplete({
            source: _lobbyPlayers,
            change: function(e, ui) { if (!ui.item) $(this).val(""); }
        });
    } else {
        inviteSection.addClass("d-none");
    }

    // Deck registration section: show when the current player is in the registrations list
    // (covers INVITED/REGISTERED relationships, and also owners who invited themselves)
    let playerInRegistrations = game.registrations.some(r => r.player === player);
    let deckSection = $("#lobbyDetailDeckSection");
    if (playerInRegistrations) {
        deckSection.removeClass("d-none");
        let myReg = game.registrations.find(r => r.player === player);
        let registeredName = myReg && myReg.deckName ? myReg.deckName : null;
        $("#lobbyRegisteredDeckName").text(registeredName || "");

        // Populate deck dropdown filtered by game format
        let dropdown = $("#lobbyDeckDropdown");
        // Remove previous deck items (keep search input)
        dropdown.find("li:not(:first-child)").remove();
        $.each(_lobbyDecks, function(i, deck) {
            if (deck.gameFormats && deck.gameFormats.includes(game.format)) {
                let li = $(`<li><a class="dropdown-item" href="#">${deck.name}</a></li>`);
                li.find("a").on("click", function(e) {
                    e.preventDefault();
                    DS.registerDeck(_lobbyCurrentGame.name, deck.name, {callback: processData, errorHandler: errorhandler});
                });
                dropdown.append(li);
            }
        });

        // Show deck preview if registered
        if (registeredName) {
            DS.loadDeck(registeredName, {
                callback: function(deckData) {
                    if (deckData && deckData.selectedDeck) {
                        renderDeck(deckData.selectedDeck.deck, "#lobbyDeckPreview");
                        addCardTooltips("#lobbyDeckPreview");
                        $("#lobbyDeckPreviewSection").removeClass("d-none");
                    }
                },
                errorHandler: function() {}
            });
        } else {
            $("#lobbyDeckPreviewSection").addClass("d-none");
            $("#lobbyDeckPreview").empty();
        }
    } else {
        deckSection.addClass("d-none");
        $("#lobbyDeckPreviewSection").addClass("d-none");
        $("#lobbyDeckPreview").empty();
    }

    $("#lobbyDetailMsg").addClass("d-none");
    $("#lobbyDetailCol").removeClass("d-none");
    $("#lobbyCreateCol").addClass("d-none");
    $("#lobbyEmptyCol").addClass("d-none");
}

function exitLobbyDetail() {
    _lobbyCurrentGame = null;
    $("#lobbyDetailCol").addClass("d-none");
    $("#lobbyCreateCol").addClass("d-none");
    $("#lobbyEmptyCol").removeClass("d-none");
}

function filterLobbyDeckList() {
    let filter = $("#lobbyDeckSearch").val().toUpperCase();
    $("#lobbyDeckDropdown li:not(:first-child) a").each(function() {
        let txt = $(this).text().toUpperCase();
        $(this).closest("li").toggle(txt.includes(filter));
    });
}

function startLobbyGame() {
    if (!_lobbyCurrentGame || !confirm("Start Game?")) return;
    DS.startGame(_lobbyCurrentGame.name, {callback: processData, errorHandler: errorhandler});
}

function closeLobbyGame() {
    if (!_lobbyCurrentGame || !confirm("Close Game?")) return;
    DS.endGame(_lobbyCurrentGame.gameId, {callback: processData, errorHandler: errorhandler});
}

function joinLobbyGame() {
    if (!_lobbyCurrentGame) return;
    DS.invitePlayer(_lobbyCurrentGame.name, player, {callback: processData, errorHandler: errorhandler});
}

function leaveLobbyGame() {
    if (!_lobbyCurrentGame || !confirm("Leave Game?")) return;
    DS.unInvitePlayer(_lobbyCurrentGame.name, player, {callback: processData, errorHandler: errorhandler});
}

function inviteLobbyPlayer() {
    if (!_lobbyCurrentGame) return;
    let p = $("#lobbyDetailInviteInput").val().trim();
    if (!p) return;
    DS.invitePlayer(_lobbyCurrentGame.name, p, {callback: processData, errorHandler: errorhandler});
    $("#lobbyDetailInviteInput").val("");
}

function createTournament() {
    //read out Tournament Details
    let tourName = $("#tourName");
    let regStart = $("#regStart");
    let regEnd = $("#regEnd");
    let playStart = $("#playStart");
    let playEnd = $("#playEnd");
    let tourFormat = $("#tourFormat");
    let gameFormat = $("#gameFormat");
    let rules = new Array();
    $("#rulesDiv div label").each(function( index, rule ) {
        rules.push(rule.textContent);
    });
    let specRulesCon = $("#specRulesCon");
    let specRules = new Array();
    $("#specRulesDiv div label").each(function( index, specRule ) {
        specRules.push(specRule.textContent);
    });
    let numOfRounds = $("#numOfRounds");
    let reqId = $("#reqId");
    //create tournament
    let name = tourName.val();
    let originalName = $("#originalTourName").val();
    DS.createTournament(name, regStart.val(), regEnd.val(), playStart.val(), playEnd.val(), tourFormat.val(), gameFormat.val(), rules, specRulesCon.val(), specRules, numOfRounds.val(), reqId.val(), originalName,
        {callback: function(success) { callbackCreateTournament(success, name, originalName); }, errorHandler: errorhandler});
}

function callbackCreateTournament(success, name, originalName) {
    let msg = $("#tourMsg");
    if(success) {
        let regEnd = $("#regEnd").val();
        let playStart = $("#playStart").val();
        let playEnd = $("#playEnd").val();
        let isRename = originalName && originalName !== name;
        if (isRename) {
            // Update the existing list item instead of adding a duplicate
            let item = $('#tournamentAdminList .tournament-admin-entry[data-name="' + originalName + '"]');
            item.attr('data-name', name).find('span:first').text(name);
            let opt = $('#nameOfTournament option[value="' + originalName + '"]');
            opt.val(name).text(name);
        } else {
            addTournamentToAdminList(name, "EDIT", regEnd, playStart, playEnd);
        }
        $("#originalTourName").val(name);
        msg.text("Tournament saved").addClass("text-success").removeClass("text-warning");
        loadTournamentDetails(name);
    } else {
        msg.text("Tournament creation failed").addClass("text-warning").removeClass("text-success");
    }
}

function addTournamentToAdminList(name, status, regEnd, playStart, playEnd) {
    let badge;
    if (status === "ACTIVE") badge = $('<span class="badge text-bg-success">Active</span>');
    else if (status === "STARTING") badge = $('<span class="badge text-bg-secondary">Starting</span>');
    else badge = $('<span class="badge text-bg-warning">Draft</span>');
    let item = $('<li class="list-group-item d-flex justify-content-between align-items-center tournament-admin-entry" style="cursor:pointer">')
        .attr('data-name', name)
        .attr('data-status', status)
        .attr('data-reg-end', regEnd || '')
        .attr('data-play-start', playStart || '')
        .attr('data-play-end', playEnd || '')
        .on('click', function() { tournamentAdminClick(this); })
        .append($('<span>').text(name))
        .append(badge);
    $('#tournamentAdminList').prepend(item);
    // Keep hidden select in sync so existing callbacks continue to work
    if ($('#nameOfTournament option[value="' + name + '"]').length === 0) {
        $('#nameOfTournament').append($('<option>').val(name).text(name));
    }
}

function resetForm() {
    let tourName = $("#tourName");
    let regStart = $("#regStart");
    let regEnd = $("#regEnd");
    let playStart = $("#playStart");
    let playEnd = $("#playEnd");
    let rulesDiv = $("#rulesDiv");
    let specRulesCon = $("#specRulesCon");
    let specRulesDiv = $("#specRulesDiv");
    let numOfRounds = $("#numOfRounds");
    //clear Form
    tourName.val("");
    regStart.val("");
    regEnd.val("");
    playStart.val("");
    playEnd.val("");
    specRulesCon.val("");
    numOfRounds.val("");
    rulesDiv.empty();
    specRulesDiv.empty();
}

function loadTournamentDetails(name) {
    DS.loadTournamentDetails(name, {callback: callbackLoadTournamentDetails, errorHandler: errorhandler});
}

function closeTournament() {
    let nameOfTournament = $("#nameOfTournament").val();
    DS.closeTournament(nameOfTournament);
}

function loadTournament(name) {
    let sel = $("#nameOfTournament");
    if (sel.find(`option[value="${name}"]`).length === 0) {
        sel.append($("<option>").val(name).text(name));
    }
    sel.val(name);
    $("#tourTablesTitle").text(name);
    DS.tournamentAlreadyActive(name, {callback: callbackStatusTournament, errorHandler: errorhandler});
    resetTournamentManager();
    enterTourTablesMode();
}

function callbackStatusTournament(isActive) {
    let nameOfTournament = $("#nameOfTournament option:selected").text();
    if(isActive) {
        let nameOfTournament = $("#nameOfTournament option:selected").text();
        DS.gameAlreadyStarted(nameOfTournament, {callback: callbackSaveButton, errorHandler: errorhandler});
        DS.getFinalPlayers(nameOfTournament, {callback: loadFinalTable, errorHandler: errorhandler})
        showTablesReadOnly(nameOfTournament);
    } else {
        DS.getTournamentRounds(nameOfTournament, {
            callback: function(rounds) {
                callbackTournamentRounds(rounds);
                DS.getAllRegisteredPlayers(nameOfTournament, {callback: callbackTableManager, errorHandler: errorhandler});
            },
            errorHandler: errorhandler
        });
        $("#saveFinal").addClass("d-none");
        $("#setFinalSeating").addClass("d-none");
        $("#saveTables").removeClass("d-none");
    }
}

function saveFinal() {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    let players = new Array();
    $.each($("#finalTable").find("li"), function(index, player) {
        players.push(player.firstChild.firstChild.textContent);
    })
    DS.saveFinal(tournamentSelected, players, {callback: processData, errorHandler: errorhandler});
    //reset
    $("#tourFinal").addClass("d-none");
}

function startSeeding() {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    if(!$("#finalSeeding-"+tournamentSelected.replace(/\s+/g, '')).is(":visible")){
       DS.loadFinalSeeding(tournamentSelected, { callback: callbackFinalSeeding, errorHandler: errorhandler});
    }
}

function startFinal() {
    if (confirm("Are you sure you want to START the FINAL?")) {
        let tournamentSelected = $("#nameOfTournament option:selected").text();
        DS.createFinalTable(tournamentSelected, {callback: processData, errorHandler: errorhandler});
        //reset
        $("#saveFinal").addClass("d-none");
    }
}

function callbackLoadTournamentDetails(data) {
    resetForm();
    let tourName = $("#tourName");
    let regStart = $("#regStart");
    let regEnd = $("#regEnd");
    let playStart = $("#playStart");
    let playEnd = $("#playEnd");
    let tourFormat = $("#tourFormat");
    let gameFormat = $("#gameFormat");
    let specRulesCon = $("#specRulesCon");
    let numOfRounds = $("#numOfRounds");
    let reqId = $("#reqId");
    let rulesDiv = $("#rulesDiv");
    let specRulesDiv = $("#specRulesDiv");

    tourName.val(data.name);
    $("#originalTourName").val(data.name);
    regStart.val(data.regStart);
    regEnd.val(data.regEnd);
    playStart.val(data.playStart);
    playEnd.val(data.playEnd);
    tourFormat.val(data.tourFormat);
    gameFormat.val(data.gameFormat);
    specRulesCon.val(data.specRulesCon);
    numOfRounds.val(data.numRounds);
    reqId.val(data.reqId)

    $.each(data.rules, function(index, rule) {
        addRule(rule, rulesDiv);
    });
    $.each(data.specRules, function(index, rule) {
        addRule(rule, specRulesDiv);
    });
    if (data.status === "EDIT") {
        $("#publishBtnContainer").show();
    } else {
        $("#publishBtnContainer").hide();
    }
    enterTourEditMode();
}

function callbackTournamentRounds(data) {
    let tourRounds = $("#tourRounds");
    tourRounds.empty();
    $.each(data, function (index, round) {
        let div = $("<div/>").attr("id","tourAllTables-"+round);
        let label = $("<span/>").addClass("h4").text("Round "+round)
            .append($("<i/>").addClass("bi bi-sort-numeric-down").on("click", () => sortPlayerVekn(round)))
            .append($("<i/>").addClass("bi bi-sort-alpha-down").on("click", () => sortPlayerNames(round)));
        let createTableButton = $("<button/>").attr("id", "createTable-"+round)
            .on('click', function () { createTable(round) })
            .addClass("btn btn-outline-secondary text-dark bg-info btn-sm mt-2 w-100")
            .text("Create Table");
        let divForPlayers = $("<div/>").attr("id","tourPlayer-"+round);
        let divForTables = $("<ol/>").addClass("card-body p-1 grid")
            .attr("id","tableTour-"+round)
            .css({"--bs-columns": "4", "--bs-gap": "0.5rem"})
            .css("list-style-position","inside")
        divForTables.sortable({
            handle: ".bi-grip-vertical",
            dropOnEmpty: true});
        div.append(label, createTableButton, divForPlayers, divForTables);
        tourRounds.append(div);
    });
}

function callbackTableManager(data) {
    $("#tourRounds div").each(function (index) {
        let roundNumber = parseInt(index)+1;
        let players = $("#"+"tourPlayer-"+roundNumber)
            .addClass("card-body p-1 grid sortable")
            .css({"--bs-columns": "4", "--bs-gap": "0.5rem"});
        players.empty();
        players.addClass("sortable"+roundNumber);
        players.sortable({
            connectWith: ".sortable"+roundNumber,
            handle: ".bi-grip-vertical",
            dropOnEmpty: true});
        $.each(data, function(index, reg) {
            let playerSpan = $("<span/>").attr("data-player", reg.player).text(reg.player);
            let veknSpan = $("<span/>").addClass("fw-bold").text(reg.vekn);
            let playerDiv = $("<div/>").addClass("d-flex flex-column").append(playerSpan, veknSpan);
            let listItem = $("<li/>")
                .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center")
                .attr("data-vekn", reg.vekn)
                .attr("data-player", reg.player)
                .append(playerDiv)
                .append("<i class='bi bi-grip-vertical'></i>");
            listItem.disableSelection();
            players.append(listItem);
        });
    })
}

function callbackTournamentAdmin(data) {
    let list = $('#tournamentAdminList');
    list.empty();
    let sel = $('#nameOfTournament');
    let current = sel.val();
    sel.empty();
    (data.tournaments || []).forEach(function(t) {
        let badge;
        if (t.status === 'ACTIVE') badge = $('<span class="badge text-bg-success">Active</span>');
        else if (t.status === 'STARTING') badge = $('<span class="badge text-bg-secondary">Starting</span>');
        else badge = $('<span class="badge text-bg-warning">Draft</span>');
        let item = $('<li class="list-group-item d-flex justify-content-between align-items-center tournament-admin-entry" style="cursor:pointer">')
            .attr('data-name', t.name)
            .attr('data-status', t.status)
            .attr('data-reg-end', t.registrationEndTime || '')
            .attr('data-play-start', t.startTime || '')
            .attr('data-play-end', t.endTime || '')
            .on('click', function() { tournamentAdminClick(this); })
            .append($('<span>').text(t.name))
            .append(badge);
        list.append(item);
        sel.append($('<option>').val(t.name).text(t.name));
    });
    if (current && sel.find(`option[value="${CSS.escape(current)}"]`).length > 0) {
        sel.val(current);
    }
}

function enterTourEditMode() {
    $("#tourEditCol").removeClass("d-none");
    $("#tourTablesCol").addClass("d-none");
}

function enterTourTablesMode() {
    $("#tourTablesCol").removeClass("d-none");
    $("#tourEditCol").addClass("d-none");
}

function exitTourMode() {
    $("#tourEditCol").addClass("d-none");
    $("#tourTablesCol").addClass("d-none");
}

function newTournament() {
    resetForm();
    $("#originalTourName").val("");
    $("#tourMsg").text("").removeClass("text-success text-warning");
    $("#publishBtnContainer").hide();
    enterTourEditMode();
}

function tournamentAdminClick(el) {
    let name = $(el).data("name");
    let status = $(el).data("status");
    let regEnd = new Date($(el).data("reg-end"));
    let playEnd = new Date($(el).data("play-end"));
    let now = new Date();

    if (status === "EDIT") {
        loadTournamentDetails(name);
    } else if (status === "STARTING") {
        if (now > regEnd) {
            loadTournament(name);
        } else {
            loadTournamentDetails(name);
        }
    } else if (status === "ACTIVE") {
        if (now <= playEnd) {
            loadTournamentReadOnly(name);
        } else {
            loadFinalsAdmin(name);
        }
    }
}

function publishTournament() {
    let name = $("#tourName").val();
    if (!name) return;
    if (!confirm("Publish \"" + name + "\"? Players will be able to see and register for this tournament.")) return;
    DS.publishTournament(name, {
        callback: function(success) {
            if (success) {
                $("#tourMsg").text("Tournament published").addClass("text-success").removeClass("text-warning");
                $("#publishBtnContainer").hide();
                let item = $('#tournamentAdminList .tournament-admin-entry[data-name="' + name + '"]');
                item.attr('data-status', 'STARTING');
                item.find('.badge').removeClass('text-bg-warning').addClass('text-bg-secondary').text('Starting');
            } else {
                $("#tourMsg").text("Publish failed").addClass("text-warning").removeClass("text-success");
            }
        },
        errorHandler: errorhandler
    });
}

function loadTournamentReadOnly(name) {
    let sel = $("#nameOfTournament");
    if (sel.find(`option[value="${name}"]`).length === 0) {
        sel.append($("<option>").val(name).text(name));
    }
    sel.val(name);
    $("#tourTablesTitle").text(name);
    resetTournamentManager();
    enterTourTablesMode();
    DS.getRoundSummary(name, {callback: callbackRoundSummaryReadOnly, errorHandler: errorhandler});
}

function callbackRoundSummaryReadOnly(data) {
    let tourRounds = $("#tourRounds");
    tourRounds.empty();
    let tourName = $("#nameOfTournament").val();
    $.each(data, function(round, tables) {
        let label = $("<span/>").addClass("h4").text("Round " + round);
        let tableList = $("<div/>").addClass("row g-2 mt-1");
        $.each(tables, function(table, players) {
            let tableLabel = $("<div/>").addClass("fw-semibold mb-1").text("Table " + table);
            let playerTable = $("<table/>").addClass("table table-sm table-bordered mb-0");
            let thead = $("<thead/>").append(
                $("<tr/>").append(
                    $("<th/>").text("Player"),
                    $("<th/>").text("Pool"),
                    $("<th/>").text("VP"),
                    $("<th/>").text("GW")
                )
            );
            let tbody = $("<tbody/>");
            let allDone = players.every(p => p.pool <= 0);
            $.each(players, function(i, p) {
                let gwBadge = p.gw ? $("<span/>").addClass("badge text-bg-success").text("GW") : "";
                tbody.append($("<tr/>").append(
                    $("<td/>").text(p.name),
                    $("<td/>").text(p.pool),
                    $("<td/>").text(p.vp),
                    $("<td/>").append(gwBadge)
                ));
            });
            playerTable.append(thead, tbody);
            let cardBody = $("<div/>").addClass("card p-2").append(tableLabel, playerTable);
            if (allDone) {
                let closeBtn = $("<button/>")
                    .addClass("btn btn-sm btn-outline-danger mt-2 w-100")
                    .text("Close Table")
                    .on("click", (function(r, t) {
                        return function() {
                            if (!confirm("Close table and record VP/GW results?")) return;
                            DS.closeTableGame(tourName, r, t, {
                                callback: function(ok) {
                                    if (ok) DS.getRoundSummary(tourName, {callback: callbackRoundSummaryReadOnly, errorHandler: errorhandler});
                                    else alert("Could not close table — game may already be closed.");
                                },
                                errorHandler: errorhandler
                            });
                        };
                    })(round, table));
                cardBody.append(closeBtn);
            }
            let col = $("<div/>").addClass("col-lg-3 col-md-4 col-6").append(cardBody);
            tableList.append(col);
        });
        tourRounds.append($("<div/>").addClass("mb-3").append(label, tableList));
    });
}

function loadFinalsAdmin(name) {
    let sel = $("#nameOfTournament");
    if (sel.find(`option[value="${name}"]`).length === 0) {
        sel.append($("<option>").val(name).text(name));
    }
    sel.val(name);
    $("#tourTablesTitle").text(name);
    resetTournamentManager();
    enterTourTablesMode();
    DS.loadFinalSeeding(name, {
        callback: function(seeding) {
            if (seeding && seeding.length > 0) {
                DS.getFinalPlayers(name, {callback: loadFinalTable, errorHandler: errorhandler});
                DS.gameAlreadyStarted(name, {callback: callbackSaveButton, errorHandler: errorhandler});
                $("#saveFinal").removeClass("d-none");
            } else {
                showFinalistSelection(name);
            }
        },
        errorHandler: errorhandler
    });
}

function showFinalistSelection(tourName) {
    DS.getStandings(tourName, {
        callback: function(standings) {
            let tourRounds = $("#tourRounds");
            tourRounds.empty();
            let heading = $("<p/>").addClass("mb-2").text("Select 5 players for the finals:");
            let playerList = $("<div/>").attr("id", "finalistCheckboxes").addClass("mb-3");
            $.each(standings, function(i, s) {
                let id = "finalist-" + i;
                let labelText = s.rank + ". " + s.player
                    + (s.vekn ? " (" + s.vekn + ")" : "")
                    + " — " + s.gw + " GW, " + s.vp + " VP";
                let row = $("<div/>").addClass("form-check")
                    .append($("<input/>").addClass("form-check-input finalist-check").attr({type:"checkbox", id, value: s.player}))
                    .append($("<label/>").addClass("form-check-label").attr("for", id).text(labelText));
                playerList.append(row);
            });
            let saveBtn = $("<button/>").addClass("btn btn-success btn-sm").text("Save Finals Selection")
                .on("click", function() {
                    let selected = [];
                    $(".finalist-check:checked").each(function() { selected.push($(this).val()); });
                    if (selected.length !== 5) {
                        alert("Please select exactly 5 players for the finals.");
                        return;
                    }
                    DS.saveFinal(tourName, selected, {
                        callback: function() { loadFinalsAdmin(tourName); },
                        errorHandler: errorhandler
                    });
                });
            tourRounds.append(heading, playerList, saveBtn);
        },
        errorHandler: errorhandler
    });
}

function createTable(round) {
    let table = $("#"+"tableTour-"+round);
    let divRound = $("<li/>")
        .addClass("card-body border border-success p-1")
        .append("<i class='bi bi-grip-vertical'></i>");
    let label = $("<span/>").addClass("h5").text("Table").append($("<br/>"));
    let list = $("<ul/>").addClass("border list-group table-size-max table-size-min sortable"+round)
        .attr("round", round)
        .css("min-height","38px");
    list.sortable({
        connectWith: ".sortable"+round,
        handle: ".bi-grip-vertical",
        dropOnEmpty: true});
    let removeTable = removeTableButton(round, divRound, list)
    divRound.append(label, removeTable, list);
    table.append(divRound);
}

function addTournamentRule() {
    var ruleText = $("#ruleText");
    addRule(ruleText.val(), $("#rulesDiv"))
    ruleText.val("");
}

function addSpecTournamentRule() {
    let specRuleText = $("#specRuleText");
    addRule(specRuleText.val(), $("#specRulesDiv"))
    specRuleText.val("");
}

function addRule(rulesInput, rulesCon) {
    let ruleDiv =  $("<div/>");
    ruleDiv.addClass("border rounded m-1");
    let ruleLabel =  $("<label/>");
    ruleLabel.addClass("form-label m-1").text(rulesInput);
    let removeButton = $("<button/>");
    removeButton.text("Remove Rule");
    removeButton.addClass("btn btn-outline-secondary btn-sm mt-2 form-control m-1")
    removeButton.on('click', function () {
        ruleDiv.remove();
        removeButton.remove();
    });
    ruleDiv.append(ruleLabel).append(removeButton)
    rulesCon.append(ruleDiv);
}

function saveTables() {
    if (confirm("Are you sure you want to SAVE the Tournament Tables?")) {
        let tournamentSelected = $("#nameOfTournament option:selected").text();
        DS.resetTables(tournamentSelected);
        let rounds = {};
        $("#tourRounds ul[round]").each(function(index, ul) {
            let players = [];
            let round = $(ul).attr("round");
            let tableNumber = $(ul).closest("li").index() + 1;
            $(ul).find("li [data-player]").each(function(i, el) {
                let name = $(el).attr("data-player");
                if (name) players.push(name);
            });
            if (round) {
                if (!rounds[round]) rounds[round] = {};
                rounds[round][tableNumber] = players;
            }
        });
        DS.saveTables(tournamentSelected, rounds);
        resetTournamentManager();
    }

function importTables() {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    let csvData = $("#importTablesCsv").val().trim();
    let errorDiv = $("#importTablesError");

    if (!csvData) {
        errorDiv.text("Please paste CSV data before importing.").removeClass("d-none");
        return;
    }
    errorDiv.addClass("d-none");

    DS.importTables(tournamentSelected, csvData, {
        callback: function() {
            bootstrap.Modal.getInstance(document.getElementById("importTablesModal")).hide();
            $("#importTablesCsv").val("");
            let msg = $("#importTablesMsg");
            msg.text("Tables imported successfully.").removeClass("d-none alert-danger").addClass("alert alert-success");
            setTimeout(function() { msg.addClass("d-none"); }, 4000);
            resetTournamentManager();
        },
        errorHandler: function(msg) {
            errorDiv.text("Import failed: " + msg).removeClass("d-none");
        }
    });
}

}

function resetTournamentManager() {
    //reset
    let tourRounds = $("#tourRounds");
    tourRounds.empty();
    $("#saveTables").addClass("d-none");
    $("#saveFinal").addClass("d-none");
    $("#finalStartedMsg").addClass("d-none");
    $("#tourFinal").addClass("d-none");
}

function downloadCurrentTables() {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    DS.getRoundsForTournamentCsv(tournamentSelected, {callback: (data) => createCsvDownloadLink(data, 'rounds.csv'), errorHandler: errorhandler});
}
function showCurrentTables() {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    DS.getRoundsForTournament(tournamentSelected, {callback: callbackShowTables, errorHandler: errorhandler});
}

function callbackShowTables(data) {
    $.each(data, function (indexRound, round) {
        let players = $("#"+"tourPlayer-"+indexRound);
        players.empty();
        let tables = $("#"+"tableTour-"+indexRound);
        tables.empty();
        $.each(round, function (indexTable, table) {
            let divRound = $("<li/>").addClass("card-body border border-success p-1")
                .append("<i class='bi bi-grip-vertical'></i>");
            let label = $("<span/>").text("Table").addClass("h5").append($("<br/>"));
            let list = $("<ul/>").addClass("border list-group sortable"+indexRound)
                .attr("round", indexRound)
                .css("min-height","38px");
            list.sortable({
                connectWith: ".sortable"+indexRound,
                handle: ".bi-grip-vertical",
                dropOnEmpty: true});
            let removeTable = removeTableButton(indexRound, divRound, list)
            $.each(table, function(index, player) {
                let listItem = $("<li/>")
                    .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center");
                DS.getVekn(player.name, {callback: function setVeknId(veknId) {
                        let nameSpan = $("<span/>").attr("data-player", player.name).text(player.name);
                        let veknSpan = $("<span/>").addClass("fw-bold").text(veknId);
                        let playerDiv = $("<div/>").addClass("d-flex flex-column").append(nameSpan, veknSpan);
                        listItem.append(playerDiv).append("<i class='bi bi-grip-vertical'></i>");
                    }, errorHandler: errorhandler});
                listItem.disableSelection();
                list.append(listItem);
            });
            divRound.append(label, removeTable, list);
            tables.append(divRound);
            let tournamentSelected = $("#nameOfTournament option:selected").text();
            DS.getRegDelta(tournamentSelected, indexRound, {callback: callbackShowPlayers, errorHandler: errorhandler});
        })
    })
}

function loadFinalTable(data) {
    //show final tabel
    $("#tourFinal").removeClass("d-none");
    let final = $("#finalTable");
    final.empty();
    final.sortable({
                connectWith: ".sortableFinal",
                handle: ".bi-grip-vertical",
                dropOnEmpty: true});
    $.each(data, function(index, player) {
        let playerSpan = $("<span/>").attr("data-player", player.player).text(player.player);
        let veknSpan = $("<span/>").addClass("fw-bold").text(player.vekn);
        let playerDiv = $("<div/>").addClass("d-flex flex-column").append(playerSpan, veknSpan);
        let listItem = $("<li/>")
            .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center")
            .append(playerDiv)
            .append("<i class='bi bi-grip-vertical'></i>");
        listItem.disableSelection();
        final.append(listItem);
    });
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    DS.getFinalDelta(tournamentSelected, {callback: callbackFinal, errorHandler: errorhandler});
}

function showTablesReadOnly(tourName) {
    DS.getRoundsForTournament(tourName, {callback: callbackShowTablesReadOnly, errorHandler: errorhandler});
}

function callbackShowTablesReadOnly(data) {
    let tourRounds = $("#tourRounds");
    tourRounds.empty();
    $.each(data, function (indexRound, round) {
        let label = $("<span/>").addClass("h4").text("Round " + indexRound);
        let tableList = $("<ol/>").addClass("card-body p-1 grid")
            .css({"--bs-columns": "4", "--bs-gap": "0.5rem"})
            .css("list-style-position", "inside");
        $.each(round, function (indexTable, table) {
            let tableLabel = $("<span/>").addClass("h5").text("Table " + indexTable).append($("<br/>"));
            let playerList = $("<ul/>").addClass("border list-group").css("min-height", "38px");
            $.each(table, function (index, playerEntry) {
                let listItem = $("<li/>").addClass("border rounded p-2 border-secondary");
                DS.getVekn(playerEntry.name, {callback: function(veknId) {
                    let nameSpan = $("<span/>").text(playerEntry.name);
                    let veknSpan = $("<span/>").addClass("fw-bold ms-2").text(veknId);
                    listItem.append(nameSpan, veknSpan);
                }, errorHandler: errorhandler});
                playerList.append(listItem);
            });
            tableList.append($("<li/>").addClass("card-body border border-success p-1").append(tableLabel, playerList));
        });
        tourRounds.append($("<div/>").append(label, tableList));
    });
}

function removeTableButton(indexRound, divRound, list) {
    return $("<button/>")
        .text("Remove Table")
        .addClass("btn btn-outline-secondary text-dark bg-warning btn-sm mt-1 mb-1")
        .on('click', function () {
            list.each(function(index, ul) {
                $("#"+"tourPlayer-"+indexRound).append($(ul).find("li"));
            })
            divRound.remove()
        });
}

function callbackShowPlayers(data) {
    $.each(data, function(index, round) {
        let players = $("#"+"tourPlayer-"+index)
            .addClass("card-body p-1 grid sortable")
            .css({"--bs-columns": "4", "--bs-gap": "0.5rem"});
        players.empty();
        players.addClass("sortable"+index);
        players.sortable({
            connectWith: ".sortable"+index,
            handle: ".bi-grip-vertical",
            dropOnEmpty: true});
        $.each(round, function(index, player) {
            let playerSpan = $("<span/>").attr("data-player", player.player).text(player.player);
            let veknSpan = $("<span/>").addClass("fw-bold").text(player.vekn);
            let playerDiv = $("<div/>").addClass("d-flex flex-column").append(playerSpan, veknSpan);
            let listItem = $("<li/>")
                .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center")
                .append(playerDiv)
                .append("<i class='bi bi-grip-vertical'></i>");
            listItem.disableSelection();
            players.append(listItem);
        })
    });
}

function createCsvDownloadLink(data, filename = 'export.csv') {
    const blob = new Blob([data], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
}

function callbackFinal(data) {
    let players = $("#finalPlayers").css({"--bs-columns": "4", "--bs-gap": "0.5rem"});
    players.empty();
    players.sortable({
        connectWith: ".sortableFinal",
        handle: ".bi-grip-vertical",
        dropOnEmpty: true});
    $.each(data, function(index, reg) {
        let playerSpan = $("<span/>").attr("data-player", reg.player).text(reg.player);
        let veknSpan = $("<span/>").addClass("fw-bold").text(reg.vekn);
        let playerDiv = $("<div/>").addClass("d-flex flex-column").append(playerSpan, veknSpan);
        let listItem = $("<li/>")
            .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center")
            .append(playerDiv)
            .append("<i class='bi bi-grip-vertical'></i>");
        listItem.disableSelection();
        players.append(listItem);
    });

    $("#tourFinal").removeClass("d-none");
}

function callbackSaveButton(isStarted) {
    if(isStarted) {
        $("#finalStartedMsg").removeClass("d-none");
    } else {
        $("#saveFinal").removeClass("d-none");
    }
}

function filterChooseDeck() {
    var input, filter, a, i;
    input = document.getElementById('searchDeckInput');
    filter = input.value.toUpperCase();
    var div = document.getElementById("chooseDeckDropdown");
    a = div.getElementsByTagName('a');
    for (i = 0; i < a.length; i++) {
        var txtValue = a[i].textContent || a[i].innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            a[i].style.display = '';
        } else {
            a[i].style.display = 'none';
        }
    }
}

function createTournamentTables() {
    if (confirm("Are you sure you want to create the Tournament Tables?")) {
        let tournamentSelected = $("#nameOfTournament option:selected").text();
        DS.createTournamentTables(tournamentSelected, {
            callback: function() {
                let item = $('#tournamentAdminList .tournament-admin-entry[data-name="' + tournamentSelected + '"]');
                item.attr('data-status', 'ACTIVE');
                item.find('.badge').removeClass('text-bg-secondary').addClass('text-bg-success').text('Active');
                resetTournamentManager();
            },
            errorHandler: errorhandler
        });
    }
}

function callbackFinalSeeding(data) {
    let tournamentSelected = $("#nameOfTournament option:selected").text();
    let activTournaments = $("#finalSeeding");
    let card = $("<div/>").addClass("card shadow mb-2").attr("id", "finalSeeding-"+tournamentSelected.replace(/\s+/g, ''));
    let header = $("<div/>").addClass("card-header bg-body-secondary")
        .append("<h5/>").text("Final Table Seeding - " + tournamentSelected);
    let finalSeeding = $("<div/>").addClass("card-body");
    let finalSeedingTable = $("<ol/>").attr("id", "finalSeedingTable-"+tournamentSelected.replace(/\s+/g, '')).addClass('border list-group');
    finalSeeding.append(finalSeedingTable);
    $.each(data, function(index, player) {
        DS.loadCrypt(tournamentSelected, player, {callback: function callbackCrypt(crypt) {
            DS.cryptCount(tournamentSelected, player, {callback: function callbackCryptCount(count) {
                let rank = index+1;
                let playerSpan = $("<span/>").addClass("fw-bold").text(player +" Rank: "+ rank);
                let countSpan = $("<span/>").text(" [" + count + "]");
                let listItem = $("<li/>")
                    .attr("data-rank", rank)
                    .attr("data-player", player)
                    .append(playerSpan)
                    .append(countSpan)
                    .addClass("border rounded p-2 border-secondary d-flex justify-content-between align-items-center");
                let ul = $("<ul/>").addClass("d-flex w-100 text-center justify-content-between");
                $.each(crypt, function(cardIndex, card) {
                    const cardLink = $("<a/>").text(card.name).attr("data-card-id", card.id).addClass("card-name");
                    let li = $("<li/>")
                        .addClass("list-group-item align-items-center p-2 shadow text-center w-100")
                        .append(cardLink);
                    ul.append(li);
                });
                listItem.append(ul);
                finalSeedingTable.append(listItem);
                addCardTooltips(listItem);
            }, errorHandler: errorhandler});
        }, errorHandler: errorhandler});
    });
    card.append(header).append(finalSeedingTable);
    activTournaments.append(card);
}

let _tournamentData = null;

function callbackTournament(data) {
    _tournamentData = data;
    let list = $("#playerTournamentList");
    list.empty();
    $("#openTourDetail").addClass("d-none");
    $("#finalsTourDetail").addClass("d-none");

    $.each(data.tournaments, function(index, tournament) {
        let registrationEnds = moment(tournament.registrationEndTime).tz("UTC");
        let badge = tournament.registered
            ? $("<span class='badge text-bg-success ms-2'>Registered</span>")
            : $("<span class='badge text-bg-secondary ms-2'>Open</span>");
        let item = $("<li/>")
            .addClass("list-group-item list-group-item-action d-flex justify-content-between align-items-center")
            .css("cursor", "pointer")
            .attr("data-type", "open")
            .attr("data-name", tournament.name)
            .append(
                $("<span/>").addClass("d-flex align-items-center")
                    .append($("<span/>").addClass("badge bg-secondary me-2").text(tournament.deckFormat))
                    .append($("<span/>").text(tournament.name))
                    .append(badge)
            )
            .append($("<small/>").addClass("text-muted").text("Closes " + moment().to(registrationEnds)))
            .on("click", function() { showOpenTournamentDetail(tournament, data); });
        list.append(item);
    });

    $.each(data.finalsInvites, function(index, tournament) {
        let item = $("<li/>")
            .addClass("list-group-item list-group-item-action d-flex justify-content-between align-items-center")
            .css("cursor", "pointer")
            .attr("data-type", "finals")
            .attr("data-name", tournament.name)
            .append(
                $("<span/>").addClass("d-flex align-items-center")
                    .append($("<span/>").addClass("badge text-bg-danger me-2").text("Finals"))
                    .append($("<span/>").text(tournament.name))
            )
            .on("click", function() { showFinalsTournamentDetail(tournament); });
        list.append(item);
    });

    if (list.children().length === 0) {
        list.append($("<li/>").addClass("list-group-item text-muted small").text("No tournaments available."));
    }
}

function showOpenTournamentDetail(tournament, data) {
    $("#finalsTourDetail").addClass("d-none");
    $("#playerTournamentList .list-group-item").removeClass("active");
    $(`#playerTournamentList [data-name="${tournament.name}"][data-type="open"]`).addClass("active");

    $("#openTourName").text(tournament.name);

    // Join / Leave button
    let joinBtn = $("#openTourJoinBtn").empty();
    if (data.veknLinked) {
        if (tournament.registered) {
            joinBtn.append(createButton({class: "btn btn-outline-secondary btn-sm", text: "Leave", confirm: "Leave Tournament?"}, DS.leaveTournament, tournament.name));
        } else {
            joinBtn.append(createButton({class: "btn btn-outline-secondary btn-sm", text: "Join"}, DS.joinTournament, tournament.name));
        }
    } else {
        joinBtn.append("<span class='badge bg-warning-subtle text-black'>Requires VEKN #</span>");
    }

    // Rules
    let rulesDiv = $("#openTourRules").empty();
    let registrationEnds = moment(tournament.registrationEndTime).tz("UTC");
    rulesDiv.append($("<p/>").addClass("text-muted small mb-1").text("Registration closes " + moment().to(registrationEnds)));
    if (tournament.rules && tournament.rules.length) {
        let ul = $("<ul/>");
        $.each(tournament.rules, function(i, r) { ul.append($("<li/>").text(r)); });
        rulesDiv.append($("<strong/>").text("Rules"), ul);
    }
    if (tournament.conditions) {
        let specUl = $("<ul/>");
        $.each(tournament.specialRules || [], function(i, r) { specUl.append($("<li/>").text(r)); });
        rulesDiv.append($("<strong/>").text("Special Rules: "), $("<span/>").text(tournament.conditions), specUl);
    }

    // Deck selection (only if registered)
    let deckSection = $("#openTourDeckSection");
    if (tournament.registered) {
        deckSection.removeClass("d-none");
        let reg = (data.registeredGames || []).find(g => g.name === tournament.name);
        let format = reg ? reg.format : tournament.deckFormat;
        $("#openTourDeckLabel").text(reg && reg.deck ? "Current deck: " + reg.deck.name : "No deck selected");

        let dropdownDiv = $("#openTourDeckDropdown").empty();
        let toggleBtn = $("<button/>")
            .addClass("btn btn-outline-secondary btn-sm dropdown-toggle")
            .attr({type: "button", "data-bs-toggle": "dropdown", "aria-expanded": "false", "data-bs-auto-close": "outside"})
            .text("Choose Deck");
        let dropdownMenu = $("<ul/>").addClass("dropdown-menu dropdown-menu-end").attr("data-name", tournament.name);
        $.each(data.decks || [], function(i, deck) {
            if (deck.gameFormats && deck.gameFormats.includes(format)) {
                dropdownMenu.append(
                    $("<li/>").append(
                        $("<a/>").addClass("dropdown-item").text(deck.name).on("click", function() {
                            DS.registerTournamentDeck(tournament.name, deck.name, {callback: processData, errorHandler: errorhandler});
                        })
                    )
                );
            }
        });
        dropdownDiv.append(toggleBtn, dropdownMenu);

        let deckPreview = $("#openTourDeckPreview").empty();
        if (reg && reg.deck) {
            renderDeck(reg.deck, "#openTourDeckPreview");
            addCardTooltips("#openTourDeckPreview");
        }
    } else {
        deckSection.addClass("d-none");
    }

    $("#openTourDetail").removeClass("d-none");
}

function showFinalsTournamentDetail(tournament) {
    $("#openTourDetail").addClass("d-none");
    $("#playerTournamentList .list-group-item").removeClass("active");
    $(`#playerTournamentList [data-name="${tournament.name}"][data-type="finals"]`).addClass("active");

    $("#finalsTourName").text(tournament.name + " — Finals");
    let seedingList = $("#finalsSeedingList").empty();
    $.each(tournament.finalsSeeding || [], function(i, player) {
        seedingList.append(
            $("<li/>").addClass("list-group-item d-flex justify-content-between align-items-center")
                .append($("<span/>").text(player))
        );
    });

    $("#finalsTourDetail").removeClass("d-none");
}

function registerForTournament(deckRow, deck) {
    let game = $(deckRow).closest('[data-name]').data('name');
    DS.registerTournamentDeck(game, deck, {callback: processData, errorHandler: errorhandler});
}

function setImageTooltip() {
    profile.imageTooltipPreference = $("#imageTooltips").is(':checked');
    DS.setUserPreferences(profile.imageTooltipPreference, {callback: processData, errorHandler: errorhandler});
}

function setEdgeColor() {
    profile.edgeColor = $("#edgecolorpicker").val();
    DS.setEdgeColor(profile.edgeColor, {callback: processData, errorHandler: errorhandler});
}

function callbackProfile(data) {
    if (profile.email !== data.email)
        $('#profileEmail').val(data.email);
    if (profile.discordID !== data.discordID)
        $('#discordID').val(data.discordID);
    if (profile.veknId !== data.veknID)
        $("#veknID").val(data.veknID);
    if (profile.country !== data.country) {
        $("#profileCountry").val(data.country);
    }
    if (profile.updating) {
        let result = $('#profileUpdateResult');
        result.text('Done!');
        result.stop(true);
        result.css('opacity', 1);
        result.css('color', 'green');
        result.fadeTo(2000, 0);
    }
    $("#playerPreferences .form-check-input").prop("checked", false);
    if (data.imageTooltipPreference) {
        $("#imageTooltips").prop("checked", true);
    }

    $("#edgecolorpicker").val(data.edgeColor);

    if (subscribed) {
        $("#enableNotifications").prop("checked", true);
    }

    profile = data;
    profile.updating = false;

    $('#profilePasswordError').val('');
    $('#profileNewPassword').val('');
    $('#profileConfirmPassword').val('');
    updateNavUserDisplay();
}

function enterEditMode() {
    $("#deckEditorCol").removeClass("d-none");
    $("#deckPreviewCol").addClass("d-none");
}

function exitEditMode() {
    $("#deckEditorCol").addClass("d-none");
    $("#deckPreviewCol").removeClass("d-none");
}

function filterDeckList() {
    let text = $("#deckTextFilter").val().toLowerCase();
    $("#decks .list-group-item").each(function () {
        let name = ($(this).data("name") || "").toLowerCase();
        let comment = ($(this).data("comment") || "").toLowerCase();
        $(this).toggle(!text || name.includes(text) || comment.includes(text));
    });
}

function callbackShowDecks(data) {
    let filter = $("#deckFilter");
    let validatorFormat = $("#validatorFormat");
    filter.empty();
    validatorFormat.empty();
    let currentFilter = data.deckFilter;
    filter.append(new Option("All", ""));
    $.each(data.tags, function (index, value) {
        let selected = currentFilter.includes(value);
        filter.append(new Option(value, value, selected, selected));
        validatorFormat.append(new Option(value, value, selected, selected));
    })
    selectDeckFilter();
    const deckText = $("#deckText");
    const deckErrors = $("#deckErrors");
    const deckPreview = $("#deckPreview");
    const deckSummary = $("#deckSummary");
    const deckName = $("#deckName");
    const deckValidation = $("#deckValidation");
    const deckValidationBadge = $("#deckValidationBadge");
    const deckValidationErrors = $("#deckValidationErrors");
    if (data.selectedDeck) {
        deckText.val(data.contents);
        deckSummary.empty();
        deckErrors.empty();
        deckValidationBadge.empty();
        deckValidationErrors.empty();
        deckSummary.text(data.selectedDeck['stats']['summary']);
        deckName.val(data.selectedDeck['deck']['name']);
        $("#deckPreviewTitle").text(data.selectedDeck['deck']['name'] || "Preview");
        const errors = data.selectedDeck.errors;
        if (errors && errors.length > 0) {
            deckValidation.removeClass("d-none");
            deckValidationBadge.append(
                $("<span/>").addClass("badge bg-warning-subtle text-warning-emphasis border border-warning-subtle")
                    .html('<i class="bi bi-exclamation-triangle me-1"></i>Invalid')
            );
            $.each(errors, function (i, error) {
                deckValidationErrors.append($("<div/>").text(error));
                deckErrors.append($("<div/>").text(error));
            });
        } else {
            deckValidation.removeClass("d-none");
            deckValidationBadge.append(
                $("<span/>").addClass("badge bg-success-subtle text-success-emphasis border border-success-subtle")
                    .html('<i class="bi bi-check-circle me-1"></i>Valid')
            );
        }
        renderDeck(data.selectedDeck.deck, "#deckPreview");
        addCardTooltips("#deckPreview");
    } else {
        deckText.val("");
        deckErrors.text("");
        deckPreview.empty();
        deckName.val("");
        $("#deckPreviewTitle").text("Preview");
        deckValidation.addClass("d-none");
    }
}

function selectDeckFilter() {
    let filter = $("#deckFilter").val();
    DS.filterDecks(filter, {callback: callbackFilterDecks, errorHandler: errorhandler});
}

function callbackFilterDecks(decks) {
    let deckList = $("#decks");
    deckList.empty();
    $.each(decks, function (index, deck) {
        const deckRow = $("<div/>").addClass("list-group-item list-group-item-action py-1 px-2")
            .data("name", deck.name).data("comment", deck.comments || "");
        const deckName = $("<span/>").addClass("deck-name-link").text(deck.name).click(function () {
            exitEditMode();
            DS.loadDeck(deck.name, {callback: processData, errorHandler: errorhandler});
        });
        const deleteButton = $("<button/>").addClass("btn btn-sm btn-outline-secondary p-1").css("font-size", "0.6rem").html("<i class='bi-trash'></i>").click(function (event) {
            if (confirm("Delete deck?")) {
                DS.deleteDeck(deck.name, {callback: processData, errorHandler: errorhandler});
            }
            event.stopPropagation();
        });
        deckRow.append(
            $("<div/>").addClass("d-flex justify-content-between align-items-center")
                .append(deckName)
                .append(deleteButton)
        );
        if (deck.comments) {
            deckRow.append(
                $("<div/>").addClass("text-muted small text-truncate").text(deck.comments)
            );
        }
        deckList.append(deckRow);
    });
    filterDeckList();
}

function callbackShowGameDeck(data) {
    renderDeck(data, "#gameDeck");
    addCardTooltips("#gameDeck");
}

function callbackMain(data) {
    if (data.loggedIn) {
        if (data.who && player) {
            const me = data.who.find(p => p.name === player);
            if (me && me.country && !profile.country) {
                profile.country = me.country;
                updateNavUserDisplay();
            }
        }
        renderOnline('onlinePlayers', data.who);
        renderGlobalChat(data.chat);
        renderMyGames("myGames", data.games);
        renderMyGames("oustedGames", data.ousted);
        if (refresher) clearTimeout(refresher);
        if (!wsConnected) {
            refresher = setTimeout(() => DS.doPoll({callback: processData, errorHandler: errorhandler}), 5000);
        }
    } else {
        document.location = "/jol/";
    }
}

function renderDeck(data, div) {
    let render = $(div);
    render.empty();
    if (data.crypt) {
        if (div === "#gameDeck") {
            render.append($("<div/>").addClass("fw-semibold small text-muted mt-1 mb-1").text("Deck: " + data.name));
        }
        render.append($("<div/>").addClass("fw-semibold small text-muted mt-1").text("Crypt (" + data.crypt['count'] + ")"));
        const crypt = $("<ul/>").addClass("deck-list");
        let cards = data.crypt.cards;
        cards.sort((a,b) => a.name.localeCompare(b.name));
        $.each(cards, function (index, card) {
            const cardRow = $("<li/>");
            const cardLink = $("<a/>").text(card.name).attr("data-card-id", card.id).addClass("card-name");
            if (card.comments === "playtest") {
                cardLink.attr("data-secured", "true");
            }
            cardRow.append(card['count'] + " x ").append(cardLink);
            crypt.append(cardRow);
        })
        render.append(crypt);
    }
    if (data.library) {
        render.append($("<div/>").addClass("fw-semibold small text-muted mt-2").text("Library (" + data.library['count'] + ")"));
        $.each(data.library.cards, function (index, libraryCards) {
            render.append($("<div/>").addClass("fw-semibold small text-muted mt-1").text(libraryCards.type + " (" + libraryCards['count'] + ")"));
            const section = $("<ul/>").addClass("deck-list");
            let cards = libraryCards.cards;
            cards.sort((a,b) => a.name.localeCompare(b.name));
            $.each(cards, function (index, card) {
                const cardRow = $("<li/>");
                const cardLink = $("<a/>").text(card.name).attr("data-card-id", card.id).addClass("card-name");
                if (card.comments === "playtest") {
                    cardLink.attr("data-secured", "true");
                }
                cardRow.append(card['count'] + " x ").append(cardLink);
                section.append(cardRow);
            })
            render.append(section);
        })
    }
    if (div === "#gameDeck") {
        if (data.comments !== "") {
            let comments = $("<div/>")
                .addClass("border m-1 p-2 small text-muted")
                .append($("<span/>").text(data.comments));
            render.append(comments);
        }
    }
    if (div === "#deckPreview") {
        $("#deckComment").val(data.comments);
    }
}

function parseDeck() {
    const contents = $("#deckText").val();
    const deckName = $("#deckName").val();
    DS.parseDeck(deckName, contents, {callback: processData, errorHandler: errorhandler});
}

function newDeck() {
    $("#deckName").val("");
    $("#deckComment").val("");
    DS.newDeck({callback: function(data) { processData(data); enterEditMode(); }, errorHandler: errorhandler});
}

function saveDeck() {
    const deckName = $("#deckName").val();
    const contents = $("#deckText").val();
    const comment = $("#deckComment").val();
    DS.saveDeck(deckName, contents, comment, {
        callback: function(data) { exitEditMode(); processData(data); },
        errorHandler: errorhandler
    });
}

function validate() {
    const contents = $("#deckText").val();
    const validator = $("#validatorFormat").val();
    DS.validate(contents, validator, {callback: processData, errorHandler: errorhandler})
}

function toggleVisible(s, h) {
    $("#" + h).hide();
    $("#" + s).show();
}

function doGlobalChat() {
    let chatInput = $("#globalChat");
    let chatLine = chatInput.val();
    chatInput.val('');
    if (chatLine === "") {
        return;
    }
    DS.chat(chatLine, {callback: processData, errorHandler: errorhandler});
}

function doNav(target) {
    const urlPath = target.startsWith('g') ? 'game/' + encodeURIComponent(target.substring(1)) : target;
    history.pushState({target}, '', '/jol/' + urlPath);
    $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
    if (refresher) clearTimeout(refresher);
    scrollChat = true;
    $('#targetPicker').hide();
    DS.navigate(target, {callback: processData, errorHandler: errorhandler});
    return false;
}

function updateNavUserDisplay() {
    $('#navUserName').text(player || '');
    const country = profile && profile.country;
    const flagEl = $('#navUserFlag');
    flagEl.empty();
    if (country) {
        flagEl.html(`<span class="fi fi-${country.toLowerCase()} fis rounded-1"></span>`);
    } else {
        flagEl.html('<i class="bi bi-person-circle text-secondary"></i>');
    }
}

function renderButton(data) {
    let buttonsDiv = $("#buttons");
    $.each(data, function (i, value) {
        let key = value.split(":")[0];
        let label = value.split(":")[1];
        const keyPath = key.startsWith('g') ? 'game/' + encodeURIComponent(key.substring(1)) : key;
        let button = $("<a/>").addClass("nav-item nav-link").attr("href", "/jol/" + keyPath).text(label).click(key, function (e) {
            e.preventDefault();
            doNav(key);
        });
        if (game === label || currentPage.toLowerCase() === key.toLowerCase()) {
            button.addClass("active");
        }
        buttonsDiv.append(button);
    });
}

function renderGameButtons(data) {
    let buttonsDiv = $("#gameButtons");
    let newActivity = false;
    $.each(data, function (key, value) {
        let li = $("<li/>");
        const keyPath = key.startsWith('g') ? 'game/' + encodeURIComponent(key.substring(1)) : key;
        let button = $("<a/>").addClass("dropdown-item").attr("href", "/jol/" + keyPath).text(value).click(key, function (e) {
            e.preventDefault();
            doNav(key);
        });
        if (game === value || currentPage.toLowerCase() === key.toLowerCase()) {
            button.addClass("active");
        }
        li.append(button);
        buttonsDiv.append(li);
        $('#gameButtonsNav').show();
        if (value.indexOf('*') > -1) newActivity = true;
    });
    $('#myGamesLink').text('My Games' + (newActivity ? ' *' : ''));
}

function isScrolledToBottom(container) {
    let scrollTop = container.scrollTop();
    let maxScrollTop = container.prop("scrollHeight") - container.prop("clientHeight");
    return Math.abs(maxScrollTop - scrollTop) < 20;
}

function scrollBottom(container) {
    container.scrollTop(container.prop("scrollHeight") - container.prop("clientHeight"));
}

function renderGameChat(data) {
    if (data === null) {
        return;
    }
    let container = $("#gameChatOutput");
    // Only scroll to bottom if scrollbar is at bottom (has not been scrolled up)
    let scrollToBottom = isScrolledToBottom(container);
    $.each(data, function (index, line) {
        const parts = line.split('||', 3);
        const dateAndTime = parts[0].split(' ', 2);
        const date = dateAndTime[0];
        const time = dateAndTime[1];
        const playerSource = parts[1];
        const message = parts[2]
            .replaceAll("&#64;"+player, "<span style='background-color: #D4D7F9; color:black'>@"+player+"</span>")
            .replaceAll("&#64;All", "<span style='background-color: #D4D7F9; color:black'>@All</span>");
        let timestamp;
        if (date === gameChatLastDay)
            timestamp = time;
        else {
            gameChatLastDay = date;
            timestamp = date + ' ' + time;
        }
        let timeSpan = $("<span/>").text(timestamp).addClass('chat-timestamp');
        let playerLabel = playerSource === "null" ? '' : $("<b/>").text(playerSource);
        let lineElement = $('<p/>').addClass('chat').append(timeSpan, ' ', playerLabel, ' ', message);
        container.append(lineElement);
    });
    if (scrollToBottom)
        scrollBottom(container);
}

function scrollGlobalChat() {
    scrollBottom($("#globalChatOutput"));
    $("#newMessages").removeClass("d-flex").addClass("d-none");
    scrollChat = true;
}

function renderGlobalChat(data) {
    if (!data || data.length === 0) {
        return;
    }
    let container = $("#globalChatOutput");
    // Only scroll to bottom if scrollbar is at bottom (has not been scrolled up)

    if (container.children().length === 0) {
        scrollChat = false;
    }

    let isAtBottom = isScrolledToBottom(container);

    let onlySelfChat = true;

    $.each(data, function (index, chat) {
        let day = moment(chat.timestamp).tz("UTC").format("D MMMM");
        if (globalChatLastDay !== day) {
            let dayBreak = $('<div class="chat-day-break"><span class="chat-day-label">' + day + '</span></div>');
            container.append(dayBreak);
        }

        let timestamp = moment(chat.timestamp).tz("UTC").format("HH:mm");
        let userTimestamp = moment(chat.timestamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
        let chatLine = $("<p/>").addClass("chat");
        let timeOutput = $("<span/>").text(timestamp).attr("title", userTimestamp).addClass('chat-timestamp');
        let playerLabel = globalChatLastPlayer === chat.player && globalChatLastDay === day ? "" : "<b>" + chat.player + "</b> ";
        //replace player name with colored player name
        let msg = chat.message
            .replaceAll("&#64;"+player, "<span class='chat-mention'>@"+player+"</span>")
            .replaceAll("&#64;All", "<span class='chat-mention'>@All</span>");
        let message = $("<span/>").html(" " + playerLabel + msg);

        if (chat.player !== player) {
            onlySelfChat = false;
        }

        chatLine.append(timeOutput).append(message);
        container.append(chatLine);
        globalChatLastPlayer = chat.player;
        globalChatLastDay = day;
    });
    addCardTooltips("#globalChatOutput");

    if (!isAtBottom && !onlySelfChat) {
        $("#newMessages").addClass("d-flex").removeClass("d-none");
    }

    if (isAtBottom || scrollChat) {
        scrollBottom(container);
        scrollChat = false;
    }
}

function renderMyGames(id, games) {
    let ownGames = $("#" + id);
    let countBadge = $("#" + id + "-count");
    ownGames.empty();
    countBadge.text(games.length);

    $.each(games, function (index, gameEntry) {
        let self = gameEntry.players[player];
        let pinged = self && self.pinged;
        let needsAttention = self && !self.current;

        let gameRow = $("<li/>")
            .addClass("list-group-item game-entry px-2 py-2")
            .on('click', function () { doNav("g" + gameEntry.gameId); });

        if (pinged) {
            gameRow.addClass("border-start border-3 border-danger");
        } else if (needsAttention) {
            gameRow.addClass("border-start border-3 border-primary-subtle");
        }

        let nameRow = $("<div/>").addClass("d-flex align-items-center justify-content-between");
        let title = $("<span/>").addClass("fw-bold text-break").text(gameEntry.name);
        if (pinged) {
            title.prepend($("<i/>").addClass("me-1 text-danger bi-exclamation-triangle"));
        } else if (needsAttention) {
            title.prepend($("<i/>").addClass("me-1 bi-bell"));
        }
        nameRow.append(title);
        if (gameEntry.turn) {
            nameRow.append($("<small/>").addClass("text-muted ms-2 text-nowrap").text(gameEntry.turn));
        }
        gameRow.append(nameRow);

        if (id === "myGames" && gameEntry.predator) {
            let pred = gameEntry.players[gameEntry.predator];
            let active = gameEntry.players[gameEntry.activePlayer];
            let prey = gameEntry.players[gameEntry.prey];
            let predName = pred ? pred.playerName : gameEntry.predator;
            let activeName = active ? active.playerName : gameEntry.activePlayer;
            let preyName = prey ? prey.playerName : gameEntry.prey;

            let seatRow = $("<div/>").addClass("d-flex align-items-center gap-1 text-muted small mt-1");
            seatRow.append($("<i/>").addClass("bi bi-arrow-left-short"));
            seatRow.append($("<span/>").text(predName));
            if (pred && pred.pinged) seatRow.append($("<i/>").addClass("bi-exclamation-triangle text-danger"));
            seatRow.append($("<span/>").addClass("mx-1 text-body-tertiary").text("·"));
            seatRow.append($("<strong/>").text(activeName));
            seatRow.append($("<span/>").addClass("mx-1 text-body-tertiary").text("·"));
            seatRow.append($("<span/>").text(preyName));
            if (prey && prey.pinged) seatRow.append($("<i/>").addClass("bi-exclamation-triangle text-danger"));
            seatRow.append($("<i/>").addClass("bi bi-arrow-right-short"));
            gameRow.append(seatRow);
        }

        ownGames.append(gameRow);
    });
}

function renderPlayer(players, target) {
    let pinged = players[target] && players[target]["pinged"] ? "<i class='bi-exclamation-triangle ms-1'></i>" : "";
    let playerName = players[target] ? players[target]["playerName"] : "";
    let template = `
        <span class='my-2 px-2 border-end border-start w-100 text-center'>
            ${playerName}
            ${pinged}
        </span>
    `
    return $(template);
}

function renderGameLink(gameEntry) {
    return $("<a/>").text(gameEntry.gameName).on('click', function () {
        doNav("g" + gameEntry.gameId);
    });
}

function renderOnline(div, who) {
    let container = $("#" + div);
    tippy.hideAll({duration: 0});
    container.empty();
    if (who === null) {
        return;
    }
    $("#online-users-header").text("Online (" + who.length + ")");
    $.each(who, function (index, playerEntry) {
        let lastOnline = moment(playerEntry.lastOnline).tz("UTC");
        let sinceLastOnline = moment.duration(moment().diff(lastOnline)).asMinutes();
        let flag = playerEntry.country
            ? `<span data-tippy-content="${regionNames.of(playerEntry.country)}" class="fi fi-${playerEntry.country.toLowerCase()} fis"></span>`
            : "";
        let admin = playerEntry.roles.includes('ADMIN') ? '<i data-tippy-content="Administrator" class="bi bi-star-fill text-warning"></i>' : "";
        let judge = playerEntry.roles.includes('JUDGE') ? '<i data-tippy-content="Judge" class="bi bi-person-raised-hand text-success"></i>' : "";
        let offline = sinceLastOnline > 60
            ? `<i data-tippy-content="Last Online: ${lastOnline.format('D-MMM HH:mm z')}" class="bi bi-clock-history text-muted"></i>`
            : "";
        let playerDiv = `<div class="online-player-row">
            ${flag}
            <span class="flex-grow-1 text-truncate">${playerEntry.name}</span>
            ${admin}${judge}${offline}
        </div>`;
        container.append(playerDiv);
    });

    if (who.length > 8) {
        let collapseEl = document.getElementById("onlinePlayersList");
        if (collapseEl && bootstrap.Collapse) {
            bootstrap.Collapse.getOrCreateInstance(collapseEl, { toggle: false }).hide();
        }
    }

    tippy('[data-tippy-content]', { theme: 'light'});
}

function renderActiveGames(games) {
    let activeGames = $("#activeGames tbody");
    activeGames.empty();
    $.each(games, function (index, gameEntry) {
        let gameRow = $("<tr/>");
        let gameLink = $("<td/>").html(renderGameLink(gameEntry));
        let turn = $("<td/>").text(gameEntry.turn);
        let timestamp = $("<td/>").text(moment(gameEntry.timestamp).tz("UTC").format("D-MMM HH:mm z"));
        gameRow.append(gameLink, turn, timestamp);
        activeGames.append(gameRow);
    });
}

function renderPastGames(history) {
    let pastGames = $("#pastGames tbody");
    pastGames.empty();
    $.each(history, function (index, gameEntry) {
        let startTime = moment(gameEntry.started, moment.ISO_8601)
        startTime = startTime.isValid ? startTime.tz("UTC").format("D-MMM-YYYY HH:mm z") : gameEntry.started
        let endTime = moment(gameEntry.ended, moment.ISO_8601).tz("UTC").format("D-MMM-YYYY HH:mm z");
        let firstPlayerRow = true;
        $.each(gameEntry.results, function (i, value) {
            let playerRow = $("<tr/>");
            if (firstPlayerRow) {
                let gameName = $("<td/>").attr('rowspan', gameEntry.results.length).text(gameEntry.name);
                let gameStarted = $("<td/>").attr('rowspan', gameEntry.results.length).text(startTime);
                let gameFinished = $("<td/>").attr('rowspan', gameEntry.results.length).text(endTime);
                playerRow.append(gameName, gameStarted, gameFinished);
                playerRow.addClass("border-3 border-top border-bottom-0 border-start-0 border-end-0")
                firstPlayerRow = false;
            } else {
                playerRow.addClass("border-top")
            }
            let playerName = $("<td/>").text(value.playerName);
            let nameString = value.deckName.length > 50 ? (value.deckName.substring(0, 50) + "...") : value.deckName;
            let deckName = $("<td/>").text(nameString);
            let score = $("<td/>").text((value.victoryPoints !== "0" ? value.victoryPoints + " VP" : "") + (value.gameWin ? ", 1 GW" : ""));
            playerRow.append(playerName, deckName, score);
            pastGames.append(playerRow);
        })
    })
}

function wsJoinGame(gameId) {
    if (ws && ws.readyState === WebSocket.OPEN && gameId) {
        ws.send(JSON.stringify({type: 'join', game: gameId}));
    }
}

function wsLeaveGame(gameId) {
    if (ws && ws.readyState === WebSocket.OPEN && gameId) {
        ws.send(JSON.stringify({type: 'leave', game: gameId}));
    }
}

function navigate(data) {
    if (data.target !== currentPage) {
        // Leave current game room before switching pages
        if (currentPage === 'game' && game) wsLeaveGame(game);
        $("#" + currentPage).hide();
        $("#" + data.target).show();
        currentPage = data.target;
    }
    const prevGame = game;
    game = data.game;
    if (currentPage === 'game') {
        // Leave the old game room if switching games on the same page
        if (prevGame && prevGame !== game) wsLeaveGame(prevGame);
        // Join the new game room so the server sends targeted notifications
        if (game && game !== prevGame) wsJoinGame(game);
    }
    $("#buttons").empty();
    $('#gameButtons').empty();
    // Always hide the My Games item to start.
    // Will be shown if necessary.
    $('#gameButtonsNav').hide();
    $('#titleLink').text(TITLE + (data.chats ? ' *' : ''));
    if (data.player === null) {
        $('#userMenu').addClass('d-none');
        $("#gameRow").hide();
        player = null;
    } else {
        renderButton(data.buttons);
        renderGameButtons(data.gameButtons);
        $('#userMenu').removeClass('d-none');
        $("#gameRow").show();
        player = data.player;
        updateNavUserDisplay();
    }
    $("#message").html(data.message)
    let timestamp = moment(data.stamp).tz("UTC").format("D-MMM HH:mm z");
    let userTimestamp = moment(data.stamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
    $('#timeStamp').text(timestamp).attr("title", userTimestamp);
    renderDesktopViewButton();
}

function registerDeck(deckRow, deck) {
    let game = $(deckRow).closest('[data-name]').data('name');
    DS.registerDeck(game, deck, {callback: processData, errorHandler: errorhandler});
}

function doCreateGame() {
    let newGameDiv = $("#newGameName");
    let publicFlag = $("#publicFlag").val();
    let gameName = newGameDiv.val();
    let format = $("#gameFormat").val();
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(gameName, publicFlag, format, {callback: processData, errorHandler: errorhandler});
    newGameDiv.val('');
}

function updateMessage() {
    let globalMessage = $("#globalMessage");
    DS.setMessage(globalMessage.val(), {callback: processData, errorHandler: errorhandler});
}

function invitePlayer() {
    let game = $("#myGameList").val();
    let player = $("#playerList").val();
    DS.invitePlayer(game, player, {callback: processData, errorHandler: errorhandler});
}

function refreshState(force) {
    DS.getState(game, force, {callback: processData, errorHandler: errorhandler});
}

function doToggle(tag) {
    if (document.getElementById(tag)) {
        new bootstrap.Collapse('#' + tag);
    }
}

function doShowDeck() {
    if ($("#gameDeck").html() === "")
        DS.getGameDeck(game, {callback: callbackShowGameDeck, errorHandler: errorhandler});
}

function doEndTurn() {
    if (confirm("Are you sure you want to end your turn?")) {
        DS.endPlayerTurn(game, {callback: processData, errorHandler: errorhandler});
    }
    return false;
}

function doSubmit(event) {
    const phaseSelect = $("#phase");
    const commandInput = $("#command");
    const chatInput = $("#chat");
    const pingSelect = $("#ping");

    let phase = phaseSelect.val() || null;
    let ping = pingSelect.val() || null;
    const command = commandInput.val();
    const chat = chatInput.val();
    if (!command && !chat && !phase) return false;
    commandInput.val("");
    chatInput.val("");
    pingSelect.val("");
    const submitBtn = $("#gameSubmit").prop("disabled", true);
    DS.submitForm(game, phase, command, chat, ping, {
        callback: (data) => { submitBtn.prop("disabled", false); processData(data); },
        errorHandler: (err, ex) => { submitBtn.prop("disabled", false); errorhandler(err, ex); }
    });
    return false;
}

function sendChat(message) {
    DS.submitForm(game, null, '', message, null, {callback: processData, errorHandler: errorhandler});
    $('#quickChatModal').modal('hide');
    return false;
}

function sendCommand(command, message = '') {
    DS.submitForm(game, null, command, message, null, {callback: processData, errorHandler: errorhandler});
    $('#quickCommandModal').modal('hide');
    return false;
}

function sendGlobalNotes() {
    DS.updateGlobalNotes(game, $("#globalNotes").val(), {errorHandler: errorhandler});
    return false;
}

function sendPrivateNotes() {
    DS.updatePrivateNotes(game, $("#privateNotes").val(), {errorHandler: errorhandler});
    return false;
}

function toggleChat() {
    $("#gameChatCard").toggleClass("d-none");
    $("#historyCard").toggleClass("d-none");
    if ($("#gameHistory").children().length === 0) {
        getHistory();
    }
}

function toggleNotes() {
    $("#notesCard").toggleClass("d-none");
    $("#gameDeckCard").toggleClass("d-none");
    if ($("#gameDeck").children().length === 0) {
        doShowDeck();
    }
}

function loadGame(data) {
    // //Reset on game change
    const gameTitle = $("#gameTitle");
    if (gameTitle.text() !== data.name) {
        $("#ping").empty();
        gameChatLastDay = null;
    }
    gameTitle.text(data.name);
    $("#gameLabel").text(data.label);

    // Phases
    let phaseSelect = $("#phase");
    let endTurn = $("#endTurn");
    if (data.phases.length > 0) {
        phaseSelect.empty();
        phaseSelect.prop('disabled', false);
        endTurn.prop('disabled', false);
        data.phases.forEach(p => phaseSelect.append(new Option(p, p)));
        if (data.phase) {
            phaseSelect.val(data.phase);
        }
    }

    let chat = $("#chat");
    let command = $("#command");
    let gameChatOutput = $("#gameChatOutput");
    let gameHistory = $("#gameHistory");
    let gameDeck = $("#gameDeck");
    let privateNotes = $("#privateNotes");
    let playerControls = $(".player-only");
    let globalNotes = $("#globalNotes");
    let controlGrid = $(".control-grid");
    let chatControls = $(".can-chat");

    // Chat Log
    if (data.resetChat) {
        gameChatOutput.empty();
        gameHistory.empty();
        gameDeck.empty();
        globalNotes.val("");
        privateNotes.val("");
        chat.empty();
        command.empty();
        lastReceivedGlobalNotes = null;
        lastReceivedPrivateNotes = null;
        gameChatLastDay = null;
        // initial state for cards
        $(".panel-default").removeClass("d-none");
        $(".panel-secondary").addClass("d-none");

        // initial state for controls
        playerControls.addClass("d-none").prop('disabled', true);
        chatControls.prop('disabled', true);
        globalNotes.prop('disabled', true);
        controlGrid.addClass("spectator");
    }
    const fetchFullLog = false;

    // enable chat controls if judge or player
    if (data.player || data.judge) {
        globalNotes.prop('disabled', false);
        chatControls.prop('disabled', false);
    }

    // If playing enable player controls
    if (data.player) {
        playerControls.removeClass("d-none").prop('disabled', false);
        controlGrid.removeClass("spectator");
    }

    // if not the current player disable phase select and end turn
    if (player !== data.currentPlayer) {
        phaseSelect.prop('disabled', true);
        endTurn.prop('disabled', true);
    }

    //If we're missing any messages from the log, skip adding this batch and
    //get a full refresh from server to prevent new messages appearing in the
    //past, where they are likely to be missed.
    if (data.turn.length > 0 && !fetchFullLog) {
        renderGameChat(data.turn);
        addCardTooltips("#gameChatOutput");
    }

    // Global Notes — only update from server if the value has changed since we last received it,
    // and the user doesn't currently have the field focused (typing in progress).
    if (data.globalNotes && data.globalNotes !== lastReceivedGlobalNotes
            && document.activeElement !== globalNotes[0]) {
        lastReceivedGlobalNotes = data.globalNotes;
        globalNotes.val(data.globalNotes);
    }

    // Same guards for private notes.
    if (data.privateNotes && data.privateNotes !== lastReceivedPrivateNotes
            && document.activeElement !== privateNotes[0]) {
        lastReceivedPrivateNotes = data.privateNotes;
        privateNotes.val(data.privateNotes);
    }

    if (data.turns.length > 0) {
        const turnSelect = $("#historySelect");
        turnSelect.empty();
        data.turns.slice(1).forEach(turn => turnSelect.append(new Option(turn, turn)));
    }

    // Render state
    if (data.state !== null) {
        $("#state").html(data.state);
        addCardTooltips("#state");
    }

    // Pings
    if (data.ping !== null) {
        let pingSelect = $("#ping");

        //+1 for the empty option
        if (pingSelect.children('option').length !== data.ping.length + 1) {
            pingSelect.empty();
            pingSelect.append(new Option("", ""));
            $.each(data.ping, function (index, value) {
                let option = new Option(value, value);
                pingSelect.append(option);
            });
        }
    }

    $.each(data.pinged, function (index, pinged) {
        $(`.player[data-player='${pinged}']`).find(".pinged").removeClass("d-none");
    });

    // Render hand
    if (data.hand !== null) {
        let hand = $("#hand");
        hand.html(data.hand);
        $("#handHeader").text("Hand ("+hand.find("li").length+")");
        addCardTooltips("#hand");
    }

    // Setup polling — immediate if log is incomplete, fallback timer when WebSocket is not connected
    if (refresher) clearTimeout(refresher);
    if (fetchFullLog) {
        refreshState(true);
    } else if (!wsConnected && data.refresh > 0) {
        refresher = setTimeout(() => refreshState(false), data.refresh);
    }

}

function addCardTooltips(parent) {
    const linkSelector = `${parent} a.card-name`;
    // On touch-only devices, skip tippy on elements with a click handler (e.g. cards in hand)
    // so that tapping to show the card modal doesn't require a double-tap.
    if (!pointerCanHover && $(linkSelector).closest('[onclick]').length) return;
    tippy(linkSelector, {
        placement: 'auto',
        allowHTML: true,
        appendTo: () => document.body,
        popperOptions: {
            strategy: 'fixed',
            modifiers: [
                {
                    name: 'flip',
                    options: {
                        fallbackPlacements: ['bottom', 'right'],
                    },
                },
                {
                    name: 'preventOverflow',
                    options: {
                        altAxis: true,
                        tether: false,
                    },
                },
            ],
        },
        onTrigger: function (instance, event) {
            event.stopPropagation();
        },
        theme: "cards",
        touch: "hold",
        onShow: function (instance) {
            tippy.hideAll({exclude: instance});
            instance.setContent("Loading...");
            let ref = $(instance.reference);
            let cardId = ref.data('card-id');
            let secured = ref.data('secured') || false ? "secured/" : "";
            if (cardId == null) { //Backwards compatibility in main chat
                cardId = instance.reference.title;
                ref.data('card-id', cardId);
                instance.reference.removeAttribute('title');
            }
            if (profile.imageTooltipPreference) {
                let content = `<img width="350" height="500" src="${BASE_URL}/${secured}images/${cardId}" alt="Loading..."/>`;
                instance.setContent(content);
            } else {
                $.get({
                    dataType: "html",
                    url: `${BASE_URL}/${secured}html/${cardId}`, success: function (data) {
                        let content = `<div class="p-2">${data}</div>`;
                        instance.setContent(content);
                    }
                });
            }
        }
    });
}

function togglePanel(event, tag) {
    event.preventDefault();
    event.stopPropagation();
    $(`[aria-controls='${tag}'] i`).toggleClass("d-none");
    tippy.hideAll({duration: 0});
    if (refresher) clearTimeout(refresher);
    DS.doToggle(game, tag, {callback: processData, errorHandler: errorhandler});
}

function showStatus(data) {
    if (data) {
        $("#gameStatusMessage").html(data);
        bootstrap.Toast.getOrCreateInstance($("#liveToast")).show();
    }
}

function getHistory() {
    let turns = $('#historySelect').val();
    DS.getHistory(game, turns, {callback: loadHistory, errorHandler: errorhandler});
}

function loadHistory(data) {
    let historyDiv = $("#gameHistory");
    historyDiv.empty();
    $.each(data, function (index, content) {
        const dateAndTime = content.timestamp;
        const playerSource = content.source;
        const message = content.message
            .replaceAll("&#64;"+player, "<span style='background-color: #D4D7F9; color:black'>@"+player+"</span>")
            .replaceAll("&#64;All", "<span style='background-color: #D4D7F9; color:black'>@All</span>");
        let timeSpan = $("<span/>").text(dateAndTime).addClass('chat-timestamp');
        let playerLabel = playerSource === "null" ? '' : $("<b/>").text(playerSource);
        let lineElement = $('<p/>').addClass('chat').append(timeSpan, ' ', playerLabel, ' ', message);
        historyDiv.append(lineElement);
    });
    addCardTooltips("#gameHistory");
}

function updateProfileErrorHandler() {
    let result = $('#profileUpdateResult');
    result.text('An error occurred');
    result.stop(true);
    result.css('opacity', 1);
    result.css('color', 'red');
}

function updateProfile() {
    profile.updating = true;
    let email = $('#profileEmail').val();
    let discordID = $('#discordID').val();
    let veknID = $("#veknID").val();
    let country = $("#profileCountry").val();
    DS.updateProfile(email, discordID, veknID, country, {callback: processData, errorHandler: updateProfileErrorHandler});
}

function updatePassword() {
    let profileNewPassword = $('#profileNewPassword').val();
    let profileConfirmPassword = $('#profileConfirmPassword').val();
    if (!profileNewPassword && !profileConfirmPassword) {
        $('#profilePasswordError').text("Enter a new password.");
    } else if (profileNewPassword !== profileConfirmPassword) {
        $('#profilePasswordError').text("Password confirmation does not match.");
    } else {
        DS.changePassword(profileNewPassword, {callback: processData, errorHandler: errorhandler});
        $('#profilePasswordError').text("Password updated.");
    }
}

function renderDesktopViewButton() {
    let viewport = $('meta[name=viewport]').get(0);
    let isDesktop = viewport.content === DESKTOP_VIEWPORT_CONTENT;
    $('#desktopViewLabel').text(isDesktop ? 'Mobile View' : 'Desktop View');
    $('#desktopViewIcon').attr('class', isDesktop ? 'bi bi-phone me-2' : 'bi bi-display me-2');
}

function toggleMobileView(event) {
    if (event) event.preventDefault();
    let viewport = $('meta[name=viewport]').get(0);
    if (viewport.content === DESKTOP_VIEWPORT_CONTENT) {
        viewport.content = 'width=device-width, initial-scale=1, shrink-to-fit=no';
        $('#desktopViewLabel').text('Desktop View');
        $('#desktopViewIcon').attr('class', 'bi bi-display me-2');
    } else {
        viewport.content = DESKTOP_VIEWPORT_CONTENT;
        $('#desktopViewLabel').text('Mobile View');
        $('#desktopViewIcon').attr('class', 'bi bi-phone me-2');
    }
    pointerCanHover = window.matchMedia("(hover: hover)").matches;
    $('body').scrollTop(0);
}

function exportCsv() {
    DS.exportPastGamesAsCsv({callback: (data) => createCsvDownloadLink(data, 'past-games.csv'), errorHandler: errorhandler});
}

function toggleMode() {
    const wrapper = $("#wrapper");
    const isDark = wrapper.attr("data-bs-theme") !== "dark";
    if (isDark) {
        wrapper.attr("data-bs-theme", "dark");
    } else {
        wrapper.removeAttr("data-bs-theme");
    }
    localStorage.setItem("jol-theme", isDark ? "dark" : "");
}

function shuffleSeeding() {
    const ul = document.getElementById("finalTable");
    const count = ul.children.length;
    const start = Math.floor(Math.random() * count);

    for (let i = 0; i < start; i++) {
        ul.appendChild(ul.firstElementChild);
    }
}

function sortPlayerVekn(round) {
    const ul = document.getElementById("tourPlayer-"+round);

    const items = [...ul.querySelectorAll('li')];

    items.sort((a, b) => {
        return Number(a.dataset.vekn) - Number(b.dataset.vekn);
    });

    ul.append(...items);
}

function sortPlayerNames(round) {
    const ul = document.getElementById("tourPlayer-"+round);

    [...ul.children]
        .sort((a, b) =>
            a.dataset.player.localeCompare(
                b.dataset.player,
                undefined,
                { sensitivity: 'base' }
            )
        )
        .forEach(li => ul.appendChild(li));
}
