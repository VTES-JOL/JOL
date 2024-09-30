"use strict";
var refresher = null;
var game = null;
var player = null;
var currentPage = 'main';
var currentOption = "notes";
var USER_TIMEZONE = moment.tz.guess();
var gameChatLastDay = null;
var globalChatLastPlayer = null;
var globalChatLastDay = null;
var TITLE = 'V:TES Online';
var DESKTOP_VIEWPORT_CONTENT = 'width=1024';
var profile = {
    email: "",
    discordID: "",
    updating: false
};
var pointerCanHover = window.matchMedia("(hover: hover)").matches;

function errorhandler(errorString, exception) {
    if (exception.name === "dwr.engine.incompleteReply" || exception.name === 'dwr.engine.textHtmlReply') {
        document.location = "/jol/main.jsp";
    }
}

$(document).ready(function () {
    moment.tz.load({
        zones: [],
        links: [],
        version: '2018c'
    });
    DS.init({callback: init});
});

function init(data) {
    $("#loadMessage").hide();
    $("#loaded").show();
    processData(data);
    $("h4.collapse").click(function () {
        $(this).next().slideToggle();
    })
}

function processData(data) {
    for (var item in data) {
        eval(item + '(data[item]);');
    }
}

function callbackAllGames(data) {
    renderActiveGames(data.games);
    renderPastGames(data.history);
}

function callbackAdmin(data) {
    var currentGames = $("#currentGames");
    var publicGames = $("#publicGames");
    var myGameList = $("#myGameList");
    var playerList = $("#playerList");
    var invitedGames = $("#invitedGames");
    var invitedGamesList = $("#invitedGamesList");
    var mydeckList = $("#mydeckList");
    if (data.message) {
        $("#registerResult").text(data.message).addClass("label label-light");
    } else {
        $("#registerResult").empty().removeClass("label label-light");
    }

    playerList.autocomplete({
        source: data.players,
        change: function (event, ui) {
            if (ui.item === null) {
                $(this).val((ui.item ? ui.item.id : ""));
            }
        }
    });

    currentGames.empty();
    myGameList.empty();
    var myGamesOption = '';
    $.each(data.myGames, function (index, game) {
        myGamesOption += '<option value="' + game.name + '">' + game.name + '</option>';
        var headerRow = $("<tr/>");
        var gameHeader = $("<th/>").text(game.name);
        var startHeader = $("<th/>");
        if (game.gameStatus === 'Inviting') {
            var startButton = $("<button/>").addClass("btn btn-primary").text("Start").click(function () {
                if (confirm("Start game?")) {
                    DS.startGame(game.name, {callback: processData, errorHandler: errorhandler});
                }
            });
            startHeader.append(startButton);
        }
        var endButton = $("<button/>").addClass("btn btn-primary").text("Close").click(function () {
            if (confirm("End game?")) {
                DS.endGame(game.name, {callback: processData, errorHandler: errorhandler});
            }
        });
        startHeader.append(endButton);
        headerRow.append(gameHeader);
        headerRow.append(startHeader);
        currentGames.append(headerRow);
        $.each(game.registrations, function (i, registration) {
            var registrationRow = $("<tr/>");
            var player = $("<td/>").text(registration.player);
            registrationRow.append(player);
            var summary = $("<td/>").text(registration.deckSummary);
            if (registration.registered && !registration.valid) {
                summary.append($('<span/>').addClass("label label-warning left-margin").text('Invalid'));
            }
            registrationRow.append(summary);
            currentGames.append(registrationRow);
        });

        $.each(game.players, function (i, playerStatus) {
            var playerRow = $("<tr/>");
            var playerName = $("<td/>").text(playerStatus.playerName);
            var pool = $("<td/>").text(playerStatus.pool + " pool");
            playerRow.append(playerName);
            playerRow.append(pool);
            currentGames.append(playerRow);
        })
    });
    myGameList.append(myGamesOption);

    publicGames.empty();
    $.each(data.publicGames, function (index, game) {
        var headerRow = $("<tr/>");
        var gameHeader = $("<th/>").text(game.name);
        var joinHeader = $("<th/>");
        var joinButton = $("<button/>").addClass('btn btn-primary').text("Join").click(function () {
            if (confirm("Join game?")) {
                DS.invitePlayer(game.name, player, {callback: processData, errorHandler: errorhandler});
            }
        });
        joinHeader.append(joinButton);
        headerRow.append(gameHeader);
        headerRow.append(joinHeader);
        publicGames.append(headerRow);
        $.each(game.registrations, function (i, registration) {
            var registrationRow = $("<tr/>");
            var playerCell = $("<td/>").text(registration.player);
            if (registration.player === player) {
                joinButton.hide();
                var leaveButton = $("<button/>").addClass('btn btn-primary').text("Leave").click(function() {
                    if (confirm("Leave game?")) {
                        DS.unInvitePlayer(game.name, player, {callback: processData, errorHandler: errorhandler});
                    }
                })
                joinHeader.append(leaveButton);
            }
            registrationRow.append(playerCell);
            var summary = $("<td/>").text(registration.deckSummary);
            if (!registration.valid && registration.registered) {
                summary.append($('<label/>').addClass("label-invalid").text('Invalid'));
            }
            registrationRow.append(summary);
            publicGames.append(registrationRow);
        });
    })

    invitedGames.empty();
    invitedGamesList.empty();
    var invitedGamesOption = '';
    $.each(data.invitedGames, function (index, game) {
        var gameRow = $("<tr/>");
        var gameName = $("<td/>").text(game.gameName);
        var deckSummary = $("<td/>").text(game.deckSummary);
        if (game.registered && !game.valid) {
            var errorMessage = $("<span/>").addClass("label label-warning left-margin").text("Invalid");
            deckSummary.append(errorMessage);
        }
        gameRow.append(gameName);
        gameRow.append(deckSummary);
        invitedGames.append(gameRow);
        invitedGamesOption += '<option value="' + game.gameName + '">' + game.gameName + '</option>';
    });
    invitedGamesList.append(invitedGamesOption);

    mydeckList.empty();
    var deckListOption = '';
    $.each(data.decks, function (index, deck) {
        deckListOption += '<option value="' + deck.name + '">' + deck.name + '</option>';
    })
    mydeckList.append(deckListOption);

}

function callbackSuper(data) {

}

function callbackProfile(data) {
    if (profile.email !== data.email)
        $('#profileEmail').val(data.email);
    if (profile.discordID !== data.discordID)
        $('#discordID').val(data.discordID);
    if (profile.updating) {
        var result = $('#profileUpdateResult');
        result.text('Done!');
        result.stop(true);
        result.css('opacity', 1);
        result.css('color', 'green');
        result.fadeTo(2000, 0);
    }

    profile = data;
    profile.updating = false;

    $('#profilePasswordError').val('');
    $('#profileNewPassword').val('');
    $('#profileConfirmPassword').val('');
}

function callbackShowDecks(data) {
    var decks = $("#decks");
    decks.empty();
    $.each(data.decks, function (index, deck) {
        const deckRow = $("<tr/>");
        const deckName = $("<td/>").text(deck.name);
        const formatLabel = $("<span/>").text(deck.deckFormat).addClass("label float-right label-small");
        deckName.click(function () {
            DS.loadDeck(deck.name, {callback: processData, errorHandler: errorhandler});
        })
        const deleteButton = $("<button/>").addClass("btn btn-warning btn-sm float-right left-margin").text("✗").click(function(event) {
            if (confirm("Delete deck?")) {
                DS.deleteDeck(deck.name, {callback: processData, errorHandler: errorhandler});
            }
            event.stopPropagation();
        });
        deckName.append(deleteButton);
        deckName.append(formatLabel);
        deckRow.append(deckName);
        decks.append(deckRow);
    });
    const deckText = $("#deckText");
    const deckErrors = $("#deckErrors");
    const deckPreview = $("#deckPreview");
    const deckSummary = $("#deckSummary");
    const deckName = $("#deckName");
    if (data.selectedDeck) {
        deckText.val(data.selectedDeck.contents);
        deckSummary.text(data.selectedDeck.details['stats']['summary']);
        deckName.val(data.selectedDeck.details.deck['name']);
        var validSpan = $("<span/>").addClass("label label-small left-margin");
        if (data.selectedDeck.details['stats']['valid']) {
            validSpan.text("VALID").addClass("label-success")
        } else {
            validSpan.text("INVALID").addClass("label-warning");
        }
        deckSummary.append(validSpan);
        deckErrors.html(data.selectedDeck.details.deck.comments.replace(/\n/g, "<br/>"));
        renderDeck(data.selectedDeck.details.deck, "#deckPreview");
        addCardTooltips("#deckPreview");
    } else {
        deckText.val("");
        deckErrors.text("");
        deckPreview.empty();
        deckName.val("");
    }
}

function callbackShowGameDeck(data) {
    renderDeck(data, "#gameDeckOutput");
    addCardTooltips("#gameDeckOutput");
}

function callbackMain(data) {
    var timestamp = moment(data.stamp).tz("UTC").format("D-MMM HH:mm z");
    var userTimestamp = moment(data.stamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
    $('#chatstamp').text(timestamp).attr("title", userTimestamp);
    if (data.loggedIn) {
        renderOnline('whoson', data.who);
        renderGlobalChat(data.chat);
        renderMyGames(data.games);
        if (refresher) clearTimeout(refresher);
        refresher = setTimeout("DS.doPoll({callback: processData, errorHandler: errorhandler})", 5000);
    } else {
        document.location = "/jol/";
    }
}

function renderDeck(data, div) {
    var render = $(div);
    render.empty();
    if (data.crypt) {
        render.append($("<h5/>").text("Crypt: (" + data.crypt['count'] + ")"));
        const crypt = $("<ul/>").addClass("deck-list");
        $.each(data.crypt.cards, function (index, card) {
            const cardRow = $("<li/>");
            const cardLink = $("<a/>").text(card.name).attr("data-card-id", card.id).addClass("card-name");
            cardRow.append(card['count'] + " x ").append(cardLink);
            crypt.append(cardRow);
        })
        render.append(crypt);
    }
    if (data.library) {
        render.append($("<h5/>").text("Library: (" + data.library['count'] + ")"));
        $.each(data.library.cards, function (index, libraryCards) {
            render.append($("<h5/>").text(libraryCards.type + ": (" + libraryCards['count'] + ")"));
            const section = $("<ul/>").addClass("deck-list");
            $.each(libraryCards.cards, function (index, card) {
                const cardRow = $("<li/>");
                const cardLink = $("<a/>").text(card.name).attr("data-card-id", card.id).addClass("card-name");
                cardRow.append(card['count'] + " x ").append(cardLink);
                section.append(cardRow);
            })
            render.append(section);
        })
    }
}

function parseDeck() {
    const contents = $("#deckText").val();
    const deckName = $("#deckName").val();
    DS.parseDeck(deckName, contents, {callback: processData, errorHandler: errorhandler});
}

function newDeck() {
    $("#deckName").val("");
    DS.newDeck({callback: processData, errorHandler: errorhandler});
}

function saveDeck() {
    const deckName = $("#deckName").val();
    const contents = $("#deckText").val();
    if (confirm("Saving a deck will remove all lines with errors.  Are you sure?")) {
        DS.saveDeck(deckName, contents, {callback: processData, errorHandler: errorhandler});
    }
}

function toggleVisible(s, h) {
    $("#" + h).hide();
    $("#" + s).show();
}

function doGlobalChat() {
    var chatInput = $("#gchat");
    var chatLine = chatInput.val();
    chatInput.val('');
    if (chatLine === "") {
        return;
    }
    DS.chat(chatLine, {callback: processData, errorHandler: errorhandler});
}

function doNav(target) {
    $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
    DS.navigate(target, {callback: processData, errorHandler: errorhandler});
    return false;
}

function renderButton(data) {
    var buttonsDiv = $("#buttons");
    $.each(data, function (key, value) {
        var button = $("<a/>").addClass("nav-item nav-link").text(value).click(key, function () {
            DS.navigate(key, {callback: processData, errorHandler: errorhandler});
            $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
        });
        if (game === value || currentPage.toLowerCase() === key.toLowerCase()) {
            button.addClass("active");
        }
        buttonsDiv.append(button);
    });
}

function renderGameButtons(data) {
    var buttonsDiv = $("#gameButtons");
    var newActivity = false;
    $.each(data, function (key, value) {
        var button = $("<a/>").addClass("dropdown-item").text(value).click(key, function () {
            DS.navigate(key, {callback: processData, errorHandler: errorhandler});
            $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
        });
        if (game === value || currentPage.toLowerCase() === key.toLowerCase()) {
            button.addClass("active");
        }
        buttonsDiv.append(button);
        $('#gameButtonsNav').show();
        if (value.indexOf('*') > -1) newActivity = true;
    });
    $('#myGamesLink').text('My Games' + (newActivity ? ' *' : ''));
}

function isScrolledToBottom(container) {
    var scrollTop = container.scrollTop();
    var maxScrollTop = container.prop("scrollHeight") - container.prop("clientHeight");
    return Math.abs(maxScrollTop - scrollTop) < 20;
}

function scrollBottom(container) {
    container.scrollTop(container.prop("scrollHeight") - container.prop("clientHeight"));
}

function renderGameChat(data) {
    if (data === null) {
        return;
    }
    var container = $("#gameChatOutput");
    // Only scroll to bottom if scrollbar is at bottom (has not been scrolled up)
    var scrollToBottom = isScrolledToBottom(container);
    $.each(data, function (index, line) {
        var dateAndTime = line.split(' ', 2);
        var date = dateAndTime[0];
        var time = dateAndTime[1];
        //Strip off date and time; reattached later
        line = line.slice(date.length + time.length + 2);
        var timestamp = null;
        if (date == gameChatLastDay)
            timestamp = time;
        else {
            gameChatLastDay = date;
            timestamp = date + ' ' + time;
        }
        var timeSpan = $("<span/>").text(timestamp).addClass('chat-timestamp');
        var playerLabel = '';
        if (line[0] == '[') {
            var player = line.split(']', 1)[0].slice(1); //Strip [
            playerLabel = $('<b/>').text(player)[0].outerHTML;
            line = line.slice(player.length + 3); //3 for [] and space
        }
        //var lineElement = $('<p/>').addClass('chat').html(timeSpan[0].outerHTML + ' ' + line);
        var lineElement = $('<p/>').addClass('chat').append(timeSpan, ' ', playerLabel, ' ', line);
        container.append(lineElement);
    });
    if (scrollToBottom)
        scrollBottom(container);
}

function renderGlobalChat(data) {
    if (!data) {
        return;
    }
    var container = $("#globalChatOutput");
    var contentHt0 = container.prop("scrollHeight");
    // Only scroll to bottom if scrollbar is at bottom (has not been scrolled up)
    var scrollToBottom = isScrolledToBottom(container);

    $.each(data, function (index, chat) {
        var day = moment(chat.timestamp).tz("UTC").format("D MMMM");
        if (globalChatLastDay != day) {
            var dayBreak = $('<div style="height: .9rem; margin-bottom: .6rem; margin-top: -.3rem; border-bottom: 1px solid #dcc; text-align: center">'
                + '<span style="font-size: .8rem; background-color: #fff; padding: 0 .5rem; color: #b99; font-weight: bold">'
                + day
                + '</span>'
                + '</div>');
            container.append(dayBreak);
        }

        var timestamp = moment(chat.timestamp).tz("UTC").format("HH:mm");
        var userTimestamp = moment(chat.timestamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
        var chatLine = $("<p/>").addClass("chat");
        var timeOutput = $("<span/>").text(timestamp).attr("title", userTimestamp).addClass('chat-timestamp');
        var playerLabel = globalChatLastPlayer === chat.player && globalChatLastDay === day ? "" : "<b>" + chat.player + "</b> ";
        var message = $("<span/>").html(" " + playerLabel + chat.message);

        chatLine.append(timeOutput).append(message);
        container.append(chatLine);
        globalChatLastPlayer = chat.player;
        globalChatLastDay = day;
    });
    addCardTooltips("#globalChatOutput");

    if (scrollToBottom) {
        $('#newChatAlert').hide();
        scrollBottom(container);
    } else if (container.prop("scrollHeight") != contentHt0) {
        $('#newChatAlert').show();
    }
}

function renderMyGames(games) {
    var ownGames = $("#ownGames");
    ownGames.empty();
    $.each(games, function (index, game) {
        var gameRow = $("<tr/>");
        var gameLink = $("<td/>");
        var status = $("<td/>");
        gameLink.html(renderGameLink(game));
        if (game.pinged) {
            status.text("!");
        } else if (!game.current) {
            status.text("*");
        }
        if (game.turn) {
            gameRow.addClass("active");
        }
        if (game.flagged) {
            gameRow.addClass("flagged");
        }
        if (game.ousted) {
            gameRow.addClass("ousted");
        }
        gameRow.append(gameLink);
        gameRow.append(status);
        ownGames.append(gameRow);
    });
}

function renderGameLink(game, small) {
    return '<a onclick="doNav(' + "'g" + game.gameName + "');" + '">'
        + game.gameName
        + "</a>";
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

function renderActiveGames(games) {
    var activeGames = $("#activeGames tbody");
    activeGames.empty();
    $.each(games, function (index, game) {
        var gameRow = $("<tr/>");
        var gameLink = $("<td/>").html(renderGameLink(game));
        var turn = $("<td/>").text(game.turn);
        var owner = $("<td/>").text(game.owner);
        gameRow.append(gameLink);
        gameRow.append(turn);
        gameRow.append(owner);
        activeGames.append(gameRow);
    });
}

function renderPastGames(history) {
    var pastGames = $("#pastGames tbody");
    pastGames.empty();
    $.each(history, function(index, game) {
        let gameRow = $("<tr/>").addClass("border-bottom");
        let gameName = $("<td/>").text(game.name);
        let startTime = moment(game.started, moment.ISO_8601)
        if (startTime.isValid()) {
            startTime = startTime.tz("UTC").format("D-MMM-YYYY HH:mm z");
        } else {
            startTime = game.started;
        }
        let started = $("<td/>").text(startTime);
        let ended = $("<td/>").text(moment(game.ended, moment.ISO_8601).tz("UTC").format("D-MMM-YYYY HH:mm z"));
        let results = $("<td/>").addClass("no-border");
        let resultsTable = $("<table/>").addClass("clean-no-border light");
        console.log(game.results);
        $.each(game.results, function(i, value) {
            let playerRow = $("<tr/>");
            let playerName = $("<td/>").text(value.playerName);
            let deckName = $("<td/>").text(value.deckName);
            let score = $("<td/>").text((value.victoryPoints !== "0" ? value.victoryPoints + " VP" : "") + (value.gameWin ? ", 1 GW" : ""))
            playerRow.append(playerName, deckName, score);
            resultsTable.append(playerRow);
        })
        results.append(resultsTable);
        gameRow.append(gameName, started, ended, results);
        pastGames.append(gameRow)
    })
}

function navigate(data) {
    if (data.target !== currentPage) {
        $("#" + currentPage).hide();
        $("#" + data.target).show();
        ga('send', 'pageview', data.target);
        currentPage = data.target;
    }
    game = data.game;
    $("#buttons").empty();
    $('#gameButtons').empty();
    // Always hide the My Games item to start.
    // Will be shown if necessary.
    $('#gameButtonsNav').hide();
    $('#titleLink').text(TITLE + (data.chats ? ' *' : ''));
    if (data.player === null) {
        $('#logout').hide();
        $("#gameRow").hide();
        player = null;
    } else {
        renderButton({active: "Watch", deck: "Decks", profile: "Profile"});
        renderButton({admin: "Game Admin", tournament: "Tournaments"});
        renderGameButtons(data.gameButtons);
        $('#logout').show();
        $("#gameRow").show();
        player = data.player;
    }
    renderButton({help: "Help"});
    renderDesktopViewButton();
}

function registerDeck() {
    var regGame = $("#invitedGamesList").val();
    var regDeck = $("#mydeckList").val();
    DS.registerDeck(regGame, regDeck, {callback: processData, errorHandler: errorhandler});
}

function doCreateGame() {
    var newGameDiv = $("#newGameName");
    var isPublic = $("#publicFlag");
    var gameName = newGameDiv.val();
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(gameName, isPublic.prop('checked'), {callback: processData, errorHandler: errorhandler});
    newGameDiv.val('');
    isPublic.prop('checked', false);
}

function invitePlayer() {
    var game = $("#myGameList").val();
    var player = $("#playerList").val();
    DS.invitePlayer(game, player, {callback: processData, errorHandler: errorhandler});
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
    } else {
        region.hide();
        regionToggle.text("+");
    }
}

function doShowDeck() {
    if ($("#gameDeckOutput").html() === "")
        DS.getGameDeck(game, {callback: callbackShowGameDeck, errorHandler: errorhandler});
}

function doSubmit() {
    const phaseSelect = $("#phase");
    const commandInput = $("#command");
    const chatInput = $("#chat");
    const pingSelect = $("#ping");
    const endTurnSelect = $("#endTurn");

    let phase = phaseSelect.val();
    let ping = pingSelect.val();
    const command = commandInput.val();
    const chat = chatInput.val();
    const endTurn = endTurnSelect.val();
    phase = phase === "" ? null : phase;
    ping = ping === "" ? null : ping;
    commandInput.val("");
    chatInput.val("");
    pingSelect.val("");
    endTurnSelect.val("No");
    if (endTurn === "Yes") {
        phaseSelect.val("Unlock");
    }
    DS.submitForm(game, phase, command, chat, ping, endTurn, null, null, {
        callback: processData,
        errorHandler: errorhandler
    });
    return false;
}

function sendChat(message) {
    DS.submitForm(
        game, null, '', message, null, 'No', null, null, {
            callback: processData,
            errorHandler: errorhandler
        });
    $('#quickChatModal').modal('hide');
    return false;
}

function sendCommand(command, message = '') {
    DS.submitForm(
        game, null, command, message, null, 'No', null, null, {
            callback: processData,
            errorHandler: errorhandler
        });
    $('#quickCommandModal').modal('hide');
    return false;
}

function sendGlobalNotes() {
    DS.submitForm(
        game, null, '', '', null, 'No', $("#globalNotes").val(), null, {
            callback: processData,
            errorHandler: errorhandler
        });
    return false;
}

function sendPrivateNotes() {
    DS.submitForm(
        game, null, '', '', null, 'No', null, $("#privateNotes").val(), {
            callback: processData,
            errorHandler: errorhandler
        });
    return false;
}

function setOther(newOption) {
    currentOption = newOption;
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

function otherClicked(event) {
    setOther($(event.target).data('target'));
    event.preventDefault();
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
    if ($("#historyOutput").html() === '')
        getHistory();
}

var lastPrivateNotesFromServer;

function loadGame(data) {
    //Reset on game change
    const gameTitle = $("#gameTitle");
    if (gameTitle.text() !== data.name) {
        $("#ping").empty();
        gameChatLastDay = null;
    }

    gameTitle.text(data.name);
    if (!data.player) {
        $(".player-only").hide();
    } else {
        $(".player-only").show();
    }
    const gameLog = $("#gameChatOutput");
    const privateNotes = $("#privateNotes");
    if (data.resetChat) {
        gameLog.empty();
        $("#historyOutput").empty();
        $("#gameDeckOutput").empty();
        $("#globalNotes").empty();
        privateNotes.empty();
        $("#chat").empty();
        $("#command").empty();
        currentOption = "notes";
        gameChatLastDay = null;
        lastPrivateNotesFromServer = null;
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
    setOther(currentOption);
    if (data.hand !== null) {
        $("#hand").html(data.hand);
    }
    if (data.state !== null) {
        $("#state").html(data.state);
    }
    if (data.global !== null) {
        $("#globalNotes").val(data.global);
    }
    //Only clobber your private notes with the server's if something has changed,
    //like another player has shown you some cards.
    if (data.text !== null && data.text != lastPrivateNotesFromServer) {
        privateNotes.val(data.text);
        lastPrivateNotesFromServer = data.text;
    }
    if (data.label !== null) {
        $("#gameLabel").text(data.label);
    }
    var fetchFullLog = false;
    if (data.logLength !== null) {
        var myLogLength = gameLog.children().length
            + (data.turn == null ? 0 : data.turn.length);
        fetchFullLog = myLogLength < data.logLength;
    }
    //If we're missing any messages from the log, skip adding this batch and
    //get a full refresh from server to prevent new messages appearing in the
    //past, where they are likely to be missed.
    if (data.turn !== null && !fetchFullLog) {
        renderGameChat(data.turn);
    }

    if (data.ping !== null) {
        var pingSelect = $("#ping");

        //+1 for the empty option
        if (pingSelect.children('option').length !== data.ping.length + 1) {
            pingSelect.empty();
            pingSelect.append(new Option("", ""));
            $.each(data.ping, function (index, value) {
                var option = new Option(value, value);
                pingSelect.append(option);
            });
        }

        $.each(data.ping, function (index, value) {
            var option = pingSelect.children('option[value="' + value + '"]:first');
            var pinged = $.inArray(value, data.pinged) !== -1;
            option.removeClass('pinged');
            if (pinged) option.addClass('pinged');
        });
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
        $("#phaseCommand").show();
        $("#endCommand").show();

        var phaseSelect = $("#phase");
        if (phaseSelect.children('option').length !== data.phases.length) {
            phaseSelect.empty();
            $.each(data.phases, function (index, value) {
                phase.append(new Option(value, value));
            });
        }
        if (data.turnChanged) {
            phaseSelect.val("Unlock");
        }
    } else {
        $("#phaseCommand").hide();
        $("#endCommand").hide();
    }
    if (data.collapsed !== null) {
        for (var c in data.collapsed) {
            doToggle(data.collapsed[c]);
        }
    }
    addCardTooltips("#game");

    if (data.refresh > 0 || fetchFullLog) {
        if (refresher) clearTimeout(refresher);

        //If we're missing anything from the log, fetch the whole thing from
        //server immediately
        var timeout = data.refresh;
        if (fetchFullLog) {
            console.log('Client log missing lines (has '
                + gameLog.children().length + '/' + data.logLength
                + '); requesting full refresh');
            timeout = 0;
        }
        refresher = setTimeout("refreshState(" + fetchFullLog + ")", timeout);
    }
}

function addCardTooltips(parent) {
    var linkSelector = `${parent} a.card-name`;
    //On devices without pointer hover capabilities, like phones, do not bind
    //tippy tooltips to cards that already have a click handler that shows the card.
    //This fixes the bug where cards in hand required a double-tap to show the modal.
    if (!pointerCanHover) {
        linkSelector += `[onclick^="pickTarget"], ${parent} a.card-name:not([onclick])`;
    }
    tippy(linkSelector, {
        animateFill: false,
        hideOnClick: false,
        flipOnUpdate: true,
        placement: 'auto',
        boundary: 'viewport',
        interactive: true,
        theme: 'light',
        onShow: function (instance) {
            //HACK To workaround the "sticky" / duplicate popups
            tippy.hideAll({duration: 0});

            instance.setContent("Loading...");
            var ref = $(instance.reference);
            var cardId = ref.data('card-id');
            if (cardId == null) { //Backwards compatibility in main chat
                cardId = instance.reference.title;
                ref.data('card-id', cardId);
                instance.reference.removeAttribute('title');
            }
            $.get({
                dataType: "html",
                url: "rest/api/cards/" + cardId, success: function (data) {
                    instance.setContent(data);
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
    addCardTooltips("#historyOutput");
}

function updateProfileErrorHandler(errorString, exception) {
    var result = $('#profileUpdateResult');
    result.text('An error occurred');
    result.stop(true);
    result.css('opacity', 1);
    result.css('color', 'red');
}

function updateProfile() {
    profile.updating = true;
    var email = $('#profileEmail').val();
    var discordID = $('#discordID').val();
    DS.updateProfile(email, discordID, {callback: processData, errorHandler: updateProfileErrorHandler});
}

function updatePassword() {
    var profileNewPassword = dwr.util.getValue("profileNewPassword");
    var profileConfirmPassword = dwr.util.getValue("profileConfirmPassword");
    if (!profileNewPassword && !profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Enter a new password.");
    } else if (profileNewPassword !== profileConfirmPassword) {
        dwr.util.setValue("profilePasswordError", "Password confirmation does not match.");
    } else {
        DS.changePassword(profileNewPassword, {callback: processData, errorHandler: errorhandler});
        dwr.util.setValue("profilePasswordError", "Password updated");
    }
}

function renderDesktopViewButton() {
    var viewport = $('meta[name=viewport]').get(0);
    var text = (
        viewport.content == DESKTOP_VIEWPORT_CONTENT
            ? 'Mobile' : 'Desktop') + ' View';
    var button = $('<a/>')
        .attr('id', 'toggleMobileViewLink')
        .addClass('nav-item nav-link')
        .text(text)
        .click(function () {
            toggleMobileView();
            $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
        });
    $('#buttons').append(button);
}

function toggleMobileView(event) {
    if (event) event.preventDefault();
    var $link = $('#toggleMobileViewLink').eq(0);
    var viewport = $('meta[name=viewport]').get(0);
    console.log('before: ' + viewport.content)
    if (viewport.content == DESKTOP_VIEWPORT_CONTENT) {
        viewport.content = 'width=device-width, initial-scale=1, shrink-to-fit=no';
        $link.text('Desktop View');
    } else {
        viewport.content = DESKTOP_VIEWPORT_CONTENT;
        $link.text('Mobile View');
    }
    console.log('after: ' + viewport.content)
    pointerCanHover = window.matchMedia("(hover: hover)").matches;
    $('body').scrollTop(0);
}
