"use strict";
let version = null;
let refresher = null;
let game = null;
let player = null;
let currentPage = 'main';
let currentOption = "notes";
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
    imageTooltipPreference: true
};
let pointerCanHover = window.matchMedia("(hover: hover)").matches;
let scrollChat = false;

function errorhandler(errorString, exception) {
    $("#connectionMessage").removeClass("d-none");
    refresher = setTimeout("DS.init({callback: processData, errorHandler: errorhandler})", 5000);
}

$(document).ready(function () {
    moment.tz.load({
        zones: [],
        links: [],
        version: '2024b'
    });
    DS.init({callback: init, errorHandler: errorhandler});
});

function init(data) {
    processData(data);
    $("h4.collapse").click(function () {
        $(this).next().slideToggle();
    })
}

function setPreferences(value) {
    profile.imageTooltipPreference = value;
}

function processData(a) {
    $("#connectionMessage").addClass("d-none");
    for (let b in a) {
        eval(b + '(a[b]);');
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

function createButton(config, fn, ...args) {
    let button = $("<button/>");
    if (config.text) {
        button.text(config.text);
    } else if (config.html) {
        button.html(config.html);
    }
    button.addClass(config.class);
    button.on('click', function () {
        if (confirm(config.confirm)) {
            fn(...args, {callback: processData, errorHandler: errorhandler});
        }
    });
    return button;
}

function callbackSelectGame(data) {

}

function callbackAdmin(data) {
    let userRoles = $("#userRoles")
    userRoles.empty();
    $.each(data.userRoles, function (index, value) {
        let playerRow = $("<tr/>");
        let nameCell = $("<td/>").text(value.name);
        let onlineCell = $("<td/>").text(moment(value.lastOnline).tz("UTC").format("D-MMM-YY HH:mm z"));
        let removeJudgeButton = value.judge ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setJudge, value.name, false) : "";
        let judgeCell = $("<td/>").addClass("text-center").append(removeJudgeButton);
        let removeSuperButton = value.superUser ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setSuperUser, value.name, false) : "";
        let superCell = $("<td/>").addClass("text-center").append(removeSuperButton);
        let removeAdminButton = value.admin ? createButton({
            html: '<i class="bi bi-x"></i>',
            class: "btn btn-outline-secondary btn-sm",
            confirm: "Are you sure you want to remove this role?"
        }, DS.setAdmin, value.name, false) : "";
        let adminCell = $("<td/>").addClass("text-center").append(removeAdminButton);
        playerRow.append(nameCell, onlineCell, judgeCell, superCell, adminCell);
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
    let endTurnList = $("#endTurnList");
    adminGameList.empty();
    $.each(data.games, function (index, value) {
        adminGameList.append($("<option/>", {value: value, text: value}));
        endTurnList.append($("<option/>", {value: value, text: value}));
    })
    adminChangeGame();

    let idleGameList = $("#idleGameList");
    idleGameList.empty();
    $.each(data.idleGames, function (index, game) {
        let playerCount = Object.keys(game.idlePlayers).length;
        let firstPlayerRow = true;
        $.each(game.idlePlayers, function (key, value) {
            let row = $("<tr/>");
            if (firstPlayerRow) {
                let nameCell = $("<td/>").attr('rowspan', playerCount).text(game.gameName).on('click', function () {
                    doNav('g' + game.gameName);
                });
                let timestampCell = $("<td/>").attr('rowspan', playerCount).text(moment(game.gameTimestamp).tz("UTC").format("D-MMM-YY HH:mm z"));
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
                }, DS.endGame, game.gameName);
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

function setPlayers(data) {
    let adminReplacePlayerList = $("#adminReplacePlayerList");
    adminReplacePlayerList.empty();
    $.each(data, function (index, value) {
        let playerOption = $("<option/>", {value: value, text: value});
        adminReplacePlayerList.append(playerOption);
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
    let functionName = "DS.set" + role;
    eval(functionName + "('" + player + "', true, {callback:processData});");
}

function callbackLobby(data) {
    let currentGames = $("#currentGames");
    let publicGames = $("#publicGames");
    let myGameList = $("#myGameList");
    let playerList = $("#playerList");
    let invitedGames = $("#invitedGames");
    let invitedGamesList = $("#invitedGamesList");
    let myDeckList = $("#myDeckList");

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
    $.each(data.myGames, function (index, game) {
        myGameList.append(new Option(game.name, game.name));
        let gameItem = $("<li/>").addClass("list-group-item");
        let gameHeader = $("<div/>").addClass("d-flex justify-content-between align-items-center");
        let gameName = $("<h6/>").addClass("d-inline").text(game.name);
        let startButton = game.gameStatus === 'Inviting' ? createButton({text: "Start", class: "btn btn-outline-secondary btn-sm", confirm: "Start Game?"}, DS.startGame, game.name) : "";
        let endButton = createButton({text: "Close", class: "btn btn-outline-secondary btn-sm", confirm: "End Game?"}, DS.endGame, game.name);
        let buttonWrapper = $("<span/>").addClass("d-flex justify-content-between align-items-center gap-1");
        let playerTable = $("<table/>").addClass("table table-bordered table-sm table-hover mt-2");
        let tableBody = $("<tbody/>");
        buttonWrapper.append(startButton, endButton);
        playerTable.append(tableBody);
        gameHeader.append(gameName, buttonWrapper);
        gameItem.append(gameHeader, playerTable);
        currentGames.append(gameItem);
        $.each(game.registrations, function (i, registration) {
            let registrationRow = $("<tr/>");
            let player = $("<td/>").addClass("w-25").text(registration.player);
            registrationRow.append(player);
            let summary = $("<td/>").addClass("w-75").text(registration.deckSummary);
            if (registration.registered && !registration.valid) {
                summary.append($('<span/>').addClass("badge text-bg-warning").text('Invalid'));
            }
            registrationRow.append(summary);
            playerTable.append(registrationRow);
        });

        $.each(game.players, function (i, playerStatus) {
            let playerRow = $("<tr/>");
            let playerName = $("<td/>").text(playerStatus.playerName);
            let pool = $("<td/>").text(playerStatus.pool + " pool");
            playerRow.append(playerName);
            playerRow.append(pool);
            playerTable.append(playerRow);
        })
    });

    publicGames.empty();
    $.each(data.publicGames, function (index, game) {
        let created = moment(game.created).tz("UTC");
        let expiry = created.add(5, 'days');
        let gameItem = $("<li/>").addClass("list-group-item");
        let gameHeader = $("<div/>").addClass("d-flex justify-content-between align-items-center");
        let gameName = $("<h6/>").addClass("d-inline").text(game.name);
        let expiryText = $("<span/>").text("Closes " + moment().to(expiry));
        let buttonWrapper = $("<span/>").addClass("d-flex justify-content-between align-items-center gap-1");
        let joinButton = createButton({
            class: "btn btn-outline-secondary btn-sm",
            text: "Join",
            confirm: "Join Game?"
        }, DS.invitePlayer, game.name, player);

        buttonWrapper.append(expiryText, joinButton);
        gameHeader.append(gameName, buttonWrapper);
        gameItem.append(gameHeader);
        publicGames.append(gameItem);
        if (game.registrations.length > 0)
        {
            let playerTable = $("<table/>").addClass("table table-bordered table-sm table-hover mt-2");
            let tableBody = $("<tbody/>");
            playerTable.append(tableBody);
            gameItem.append(playerTable);
            $.each(game.registrations, function (i, registration) {
                let registrationRow = $("<tr/>");
                let playerCell = $("<td/>").addClass("w-50").text(registration.player);
                if (registration.player === player) {
                    let leaveButton = registration.player === player ? createButton({
                        class: "btn btn-outline-secondary btn-sm",
                        text: "Leave",
                        confirm: "Leave Game?"
                    }, DS.unInvitePlayer, game.name, player) : "";
                    buttonWrapper.append(leaveButton);
                }
                registrationRow.append(playerCell);
                let summary = $("<td/>").addClass("w-50 text-center")
                if (registration.registered) {
                    summary.text("Registered");
                }
                if (!registration.valid && registration.registered) {
                    summary.append($('<span/>').addClass("badge text-bg-warning pl-1").text('Invalid'));
                }
                registrationRow.append(summary);
                tableBody.append(registrationRow);
            });        }
    })

    invitedGames.empty();
    invitedGamesList.empty();
    $.each(data.invitedGames, function (index, game) {
        let gameItem = $("<li/>").addClass("list-group-item");
        let gameHeader = $("<div/>").addClass("d-flex justify-content-between align-items-center");
        let gameName = $("<span/>").text(game.gameName);
        let deckSummary = $("<span/>").text(game.deckSummary);
        if (game.registered && !game.valid) {
            let errorMessage = $("<span/>").addClass("label label-warning left-margin").text("Invalid");
            deckSummary.append(errorMessage);
        }
        gameHeader.append(gameName, deckSummary);
        gameItem.append(gameHeader);
        invitedGames.append(gameItem);
        invitedGamesList.append(new Option(game.gameName, game.gameName));
    });

    myDeckList.empty();
    $.each(data.decks, function (index, deck) {
        myDeckList.append(new Option(deck.name, deck.name));
    })

    // Registration Result
    let registerResult = $("#registerResult");
    registerResult.empty();
    if (data.message) {
        registerResult.text(data.message).addClass("badge text-bg-light");
    }

}

function callbackTournament(data) {
    if (data.idValid) {
        $("#validId").show();
        $("#registrationPanel").show();
        $("#registrationMessage").hide();
        let tournamentRound1 = $("#tournamentRound1");
        tournamentRound1.empty();
        // let tournamentRound2 = $("#tournamentRound2");
        // tournamentRound2.empty();
        // let tournamentRound3 = $("#tournamentRound3");
        // tournamentRound3.empty();
        $.each(data.decks, function (index, deck) {
            tournamentRound1.append($("<option/>", {value: deck, label: deck}));
            // tournamentRound2.append($("<option/>", {value: deck, label: deck}));
            // tournamentRound3.append($("<option/>", {value: deck, label: deck}));
        })
        if (data.registeredDecks.length > 0) {
            tournamentRound1.val(data.registeredDecks[0]);
            // tournamentRound2.val(data.registeredDecks[1]);
            // tournamentRound3.val(data.registeredDecks[2]);
            $("#tournamentRegisterButton").text("Update Registration")
        }
        let playerRegistrations = $("#playerRegistrations");
        playerRegistrations.empty();
        $.each(data.registrations, function (index, player) {
            playerRegistrations.append($("<tr/>").append($("<td/>").text(player)));
        })
        if (data.message) {
            $("#tournamentRegistrationResult").text(data.message).addClass("label label-light");
        } else {
            $("#tournamentRegistrationResult").empty().removeClass("label label-light");
        }
    } else {
        $("#validID").hide();
        $("#registrationPanel").hide();
        $("#registrationMessage").show();
    }
}

function registerforTournament() {
    let tournamentRound1 = $("#tournamentRound1").val();
    // let tournamentRound2 = $("#tournamentRound2").val();
    // let tournamentRound3 = $("#tournamentRound3").val();
    DS.registerTournamentDeck(tournamentRound1, null, null, {
        callback: processData,
        errorHandler: errorhandler
    });
}

function setImageTooltip(value) {
    profile.imageTooltipPreference = value;
    DS.setUserPreferences(profile.imageTooltipPreference, {callback: processData, errorHandler: errorhandler});
}

function callbackProfile(data) {
    if (profile.email !== data.email)
        $('#profileEmail').val(data.email);
    if (profile.discordID !== data.discordID)
        $('#discordID').val(data.discordID);
    if (profile.veknId !== data.veknID)
        $("#veknID").val(data.veknID);
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
        $("#imageTooltipPreference").prop("checked",true)
    } else {
        $("#textTooltipPreference").prop("checked",true)
    }

    profile = data;
    profile.updating = false;

    $('#profilePasswordError').val('');
    $('#profileNewPassword').val('');
    $('#profileConfirmPassword').val('');
}

function callbackShowDecks(data) {
    let decks = $("#decks");
    decks.empty();
    $.each(data.decks, function (index, deck) {
        const deckRow = $("<tr/>");
        const deckCell = $("<td/>").addClass("d-flex justify-content-between align-items-center");
        const deckName = $("<span/>").text(deck.name).click(function () {
            DS.loadDeck(deck.name, {callback: processData, errorHandler: errorhandler});
        });
        const formatLabel = $("<span/>").text(deck.deckFormat).addClass("badge");
        let formatStyle = deck.deckFormat === "MODERN" ? "text-bg-secondary" : "text-bg-warning";
        formatLabel.addClass(formatStyle)
        const deleteButton = $("<button/>").addClass("btn btn-sm btn-outline-secondary border p-1").css("font-size", "0.6rem").html("<i class='bi-trash'></i>").click(function (event) {
            if (confirm("Delete deck?")) {
                DS.deleteDeck(deck.name, {callback: processData, errorHandler: errorhandler});
            }
            event.stopPropagation();
        });
        let wrapper = $("<span/>").addClass("d-flex gap-1 align-items-center").append(formatLabel, deleteButton);
        deckCell.append(deckName, wrapper);
        deckRow.append(deckCell);
        decks.append(deckRow);
    });
    const deckText = $("#deckText");
    const deckErrors = $("#deckErrors");
    const deckPreview = $("#deckPreview");
    const deckSummary = $("#deckSummary");
    const deckName = $("#deckName");
    if (data.selectedDeck) {
        deckText.val(data.selectedDeck.contents);
        deckSummary.empty();
        deckSummary.append($("<span/>").text(data.selectedDeck.details['stats']['summary']));
        deckName.val(data.selectedDeck.details.deck['name']);
        let validSpan = $("<span/>").addClass("badge badge-sm");
        if (data.selectedDeck.details['stats']['valid']) {
            validSpan.text("VALID").addClass("text-bg-success")
        } else {
            validSpan.text("INVALID").addClass("text-bg-warning");
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
    renderDeck(data, "#gameDeck");
    addCardTooltips("#gameDeck");
}

function callbackMain(data) {
    if (data.loggedIn) {
        renderOnline('onlinePlayers', data.who);
        renderGlobalChat(data.chat);
        renderMyGames("#myGames", data.games);
        renderMyGames("#oustedGames", data.ousted);
        $("#globalMessage").val(data.message);
        if (refresher) clearTimeout(refresher);
        refresher = setTimeout("DS.doPoll({callback: processData, errorHandler: errorhandler})", 5000);
    } else {
        document.location = "/jol/";
    }
}

function renderDeck(data, div) {
    let render = $(div);
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
    let chatInput = $("#globalChat");
    let chatLine = chatInput.val();
    chatInput.val('');
    if (chatLine === "") {
        return;
    }
    DS.chat(chatLine, {callback: processData, errorHandler: errorhandler});
}

function doNav(target) {
    $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
    if (refresher) clearTimeout(refresher);
    scrollChat = true;
    $('#targetPicker').hide();
    DS.navigate(target, {callback: processData, errorHandler: errorhandler});
    return false;
}

function renderButton(data) {
    let buttonsDiv = $("#buttons");
    $.each(data, function (i, value) {
        let key = value.split(":")[0];
        let label = value.split(":")[1];
        let button = $("<a/>").addClass("nav-item nav-link").text(label).click(key, function () {
            DS.navigate(key, {callback: processData, errorHandler: errorhandler});
            if (refresher) clearTimeout(refresher);
            $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
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
        let button = $("<a/>").addClass("dropdown-item").text(value).click(key, function () {
            DS.navigate(key, {callback: processData, errorHandler: errorhandler});
            $('#navbarNavAltMarkup').collapse('hide'); //Collapse the navbar
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
        const dateAndTime = line.split(' ', 2);
        const date = dateAndTime[0];
        const time = dateAndTime[1];
        //Strip off date and time; reattached later
        line = line.slice(date.length + time.length + 2);
        let timestamp;
        if (date === gameChatLastDay)
            timestamp = time;
        else {
            gameChatLastDay = date;
            timestamp = date + ' ' + time;
        }
        let timeSpan = $("<span/>").text(timestamp).addClass('chat-timestamp');
        let playerLabel = '';
        if (line[0] === '[') {
            let player = line.split(']', 1)[0].slice(1); //Strip [
            playerLabel = $('<b/>').text(player)[0].outerHTML;
            line = line.slice(player.length + 3); //3 for [] and space
        }
        let lineElement = $('<p/>').addClass('chat').append(timeSpan, ' ', playerLabel, ' ', line);
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
            let dayBreak = $('<div style="height: .9rem; margin-bottom: .6rem; margin-top: -.3rem; border-bottom: 1px solid #dcc; text-align: center">'
                + '<span style="font-size: .8rem; background-color: #fff; padding: 0 .5rem; color: #b99; font-weight: bold">'
                + day
                + '</span>'
                + '</div>');
            container.append(dayBreak);
        }

        let timestamp = moment(chat.timestamp).tz("UTC").format("HH:mm");
        let userTimestamp = moment(chat.timestamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
        let chatLine = $("<p/>").addClass("chat");
        let timeOutput = $("<span/>").text(timestamp).attr("title", userTimestamp).addClass('chat-timestamp');
        let playerLabel = globalChatLastPlayer === chat.player && globalChatLastDay === day ? "" : "<b>" + chat.player + "</b> ";
        let message = $("<span/>").html(" " + playerLabel + chat.message);

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

function toggleDetailedMode(elem) {
    let checked = elem.checked;
    localStorage.setItem("jol-details", checked);
    let playersDiv = $(".players");
    if (checked) {
        playersDiv.removeClass("d-none");
    } else {
        playersDiv.addClass("d-none");
    }
}

function renderMyGames(id, games) {
    let checked = localStorage.getItem("jol-details") || "true";
    let ownGames = $(id);
    ownGames.empty();
    $.each(games, function (index, game) {
        let gameRow = $("<li/>").addClass("list-group-item p-0 border").on('click', function() {doNav("g" + game.name);});
        let header = $("<div/>").addClass("d-flex p-2 justify-content-between w-100 border-bottom bg-body-tertiary");
        let title = $("<span/>").addClass("fw-bold").text(game.name);
        let turn = $("<small/>").text(game.turn).addClass("d-inline-block d-md-none d-xl-inline-block");
        header.append(title, turn);
        let players = $("<div/>").addClass("players pb-2");
        let toggle = $("#myGamesDetailedMode");
        if (checked === "true") {
            toggle.prop("checked",true);
            players.removeClass("d-none");
        } else {
            toggle.prop("checked",false);
            players.addClass("d-none");
        }
        let predator = renderPlayer(game.players, game.predator);
        let activePlayer = renderPlayer(game.players, game.activePlayer);
        let prey = renderPlayer(game.players, game.prey);
        activePlayer.addClass("fw-semibold");
        let self = game.players[player];
        if (self.pinged) {
            title.prepend($("<i/>").addClass('me-2 text-danger bi-exclamation-triangle'));
        } else if (!self.current) {
            title.prepend($("<i/>").addClass('me-2 bi-bell'));
        }
        players.append(predator, activePlayer, prey);
        gameRow.append(header, players);
        ownGames.append(gameRow);
    });
}

function renderPlayer(players, target) {
    let span = $("<span/>").addClass("my-2 px-2 border-end border-start w-100 text-center");
    if (target !== "") {
        span.text(players[target].playerName);
        if (players[target].pinged) {
            span.append("<i class='bi-exclamation-triangle ms-1'></i>");
        }
    }
    return span;
}

function renderGameLink(game) {
    return $("<a/>").text(game.gameName).on('click', function () {
        doNav("g" + game.gameName);
    });
}

function renderOnline(div, who) {
    let container = $("#" + div);
    container.empty();
    if (who === null) {
        return;
    }
    $.each(who, function (index, player) {
        let playerSpan = $("<span/>").text(player.name).addClass("badge mb-1 fs-6");
        if (player.superUser) {
            playerSpan.addClass("text-bg-secondary");
        } else if (player.admin) {
            playerSpan.addClass("text-bg-warning");
        } else {
            playerSpan.addClass("text-bg-light border border-secondary-subtle")
        }
        if (player.judge) {
            playerSpan.addClass("border border-2 border-dark border-dotted");
        }
        container.append(playerSpan);
        container.append(" ");
    });
}

function renderActiveGames(games) {
    let activeGames = $("#activeGames tbody");
    activeGames.empty();
    $.each(games, function (index, game) {
        let gameRow = $("<tr/>");
        let gameLink = $("<td/>").html(renderGameLink(game));
        let turn = $("<td/>").text(game.turn);
        let timestamp = $("<td/>").text(moment(game.timestamp).tz("UTC").format("D-MMM HH:mm z"));
        gameRow.append(gameLink, turn, timestamp);
        activeGames.append(gameRow);
    });
}

function renderPastGames(history) {
    let pastGames = $("#pastGames tbody");
    pastGames.empty();
    $.each(history, function (index, game) {
        let startTime = moment(game.started, moment.ISO_8601)
        startTime = startTime.isValid ? startTime.tz("UTC").format("D-MMM-YYYY HH:mm z") : game.started
        let endTime = moment(game.ended, moment.ISO_8601).tz("UTC").format("D-MMM-YYYY HH:mm z");
        let firstPlayerRow = true;
        $.each(game.results, function (i, value) {
            let playerRow = $("<tr/>");
            if (firstPlayerRow) {
                let gameName = $("<td/>").attr('rowspan', 5).text(game.name);
                let gameStarted = $("<td/>").attr('rowspan', 5).text(startTime);
                let gameFinished = $("<td/>").attr('rowspan', 5).text(endTime);
                playerRow.append(gameName, gameStarted, gameFinished);
                playerRow.addClass("border-3 border-top border-bottom-0 border-start-0 border-end-0")
                firstPlayerRow = false;
            } else {
                playerRow.addClass("border-top")
            }
            let playerName = $("<td/>").text(value.playerName);
            let deckName = $("<td/>").text(value.deckName);
            let score = $("<td/>").text((value.victoryPoints !== "0" ? value.victoryPoints + " VP" : "") + (value.gameWin ? ", 1 GW" : ""));
            playerRow.append(playerName, deckName, score);
            pastGames.append(playerRow);
        })
    })
}

function navigate(data) {
    if (data.target !== currentPage) {
        $("#" + currentPage).hide();
        $("#" + data.target).show();
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
        renderButton(data.buttons);
        renderGameButtons(data.gameButtons);
        $('#logout').show();
        $("#gameRow").show();
        player = data.player;
    }
    $("#message").html(data.message)
    let timestamp = moment(data.stamp).tz("UTC").format("D-MMM HH:mm z");
    let userTimestamp = moment(data.stamp).tz(USER_TIMEZONE).format("D-MMM HH:mm z");
    $('#timeStamp').text(timestamp).attr("title", userTimestamp);
    renderDesktopViewButton();
}

function registerDeck() {
    let regGame = $("#invitedGamesList").val();
    let regDeck = $("#myDeckList").val();
    DS.registerDeck(regGame, regDeck, {callback: processData, errorHandler: errorhandler});
}

function doCreateGame() {
    let newGameDiv = $("#newGameName");
    let isPublic = $("#publicFlag");
    let gameName = newGameDiv.val();
    if (gameName.indexOf("\'") > -1 || gameName.indexOf("\"") > -1) {
        alert("Game name can not contain \' or \" characters in it");
        return;
    }
    DS.createGame(gameName, isPublic.prop('checked'), {callback: processData, errorHandler: errorhandler});
    newGameDiv.val('');
    isPublic.prop('checked', false);
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
    DS.submitForm(game, phase, command, chat, ping, endTurn, {callback: processData, errorHandler: errorhandler});
    return false;
}

function sendChat(message) {
    DS.submitForm(game, null, '', message, null, 'No', {callback: processData, errorHandler: errorhandler});
    $('#quickChatModal').modal('hide');
    return false;
}

function sendCommand(command, message = '') {
    DS.submitForm(game, null, command, message, null, 'No', {callback: processData, errorHandler: errorhandler});
    $('#quickCommandModal').modal('hide');
    return false;
}

function sendGlobalNotes() {
    DS.updateGlobalNotes(game, $("#globalNotes").val());
    return false;
}

function sendPrivateNotes() {
    DS.updatePrivateNotes(game, $("#privateNotes").val());
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
        phaseSelect.removeAttr('disabled');
        endTurn.removeAttr('disabled');

        if (phaseSelect.children('option').length !== data.phases.length) {
            $.each(data.phases, function (index, value) {
                phase.append(new Option(value, value));
            });
        }
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
        currentOption = "notes";
        gameChatLastDay = null;
        // initial state for cards
        $(".panel-default").removeClass("d-none");
        $(".panel-secondary").addClass("d-none");

        // initial state for controls
        playerControls.addClass("d-none");
        chatControls.attr('disabled', true);
        globalNotes.attr('disabled', true);
        controlGrid.addClass("spectator");
    }
    let fetchFullLog = false;
    if (data.logLength !== null) {
        let myLogLength = gameChatOutput.children().length + (data.turn.length);
        fetchFullLog = myLogLength < data.logLength;
    }

    // enable chat controls if judge or player
    if (data.player || data.judge) {
        globalNotes.removeAttr('disabled');
        chatControls.removeAttr('disabled');
    }

    // If playing enable player controls
    if (data.player) {
        playerControls.removeClass("d-none");
        controlGrid.removeClass("spectator");
    }

    // if not the current player disable phase select and end turn
    if (player !== data.currentPlayer) {
        phaseSelect.attr('disabled', true);
        endTurn.attr('disabled', true);
    }

    //If we're missing any messages from the log, skip adding this batch and
    //get a full refresh from server to prevent new messages appearing in the
    //past, where they are likely to be missed.
    if (data.turn.length > 0 && !fetchFullLog) {
        renderGameChat(data.turn);
        addCardTooltips("#gameChatOutput");
    }

    // Global Notes
    if (data.globalNotes) {
        globalNotes.val(data.globalNotes);
    }

    //Only clobber your private notes with the server's if something has changed,
    //like another player has shown you some cards.
    if (data.privateNotes) {
        privateNotes.val(data.privateNotes);
    }

    if (data.turns.length > 0) {
        let turnSelect = $("#historySelect");
        turnSelect.empty();
        data.turns.shift();
        $.each(data.turns, function (index, turn) {
            turnSelect.append($(new Option(turn, turn)));
        });
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

    $.each(data.pinged, function(index, pinged) {
        $(`.player[data-player='${pinged}']`).find(".pinged").removeClass("d-none");
    });

    // Render hand
    if (data.hand !== null) {
        $("#hand").html(data.hand);
        addCardTooltips("#hand");
    }

    // Setup polling
    if (refresher) clearTimeout(refresher);
    if (data.refresh > 0 || fetchFullLog) {

        //If we're missing anything from the log, fetch the whole thing from
        //server immediately
        let timeout = data.refresh;
        if (fetchFullLog) {
            timeout = 0;
        }
        refresher = setTimeout("refreshState(" + fetchFullLog + ")", timeout);
    }

}

function addCardTooltips(parent) {
    let linkSelector = `${parent} a.card-name`;
    //On devices without pointer hover capabilities, like phones, do not bind
    //tippy tooltips to cards that already have a click handler that shows the card.
    //This fixes the bug where cards in hand required a double-tap to show the modal.
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
            theme: "light",
            touch: "hold",
            onShow: function (instance) {
                tippy.hideAll({exclude: instance});
                instance.setContent("Loading...");
                let ref = $(instance.reference);
                let cardId = ref.data('card-id');
                if (cardId == null) { //Backwards compatibility in main chat
                    cardId = instance.reference.title;
                    ref.data('card-id', cardId);
                    instance.reference.removeAttribute('title');
                }
                if (profile.imageTooltipPreference) {
                    let content = `<img width="350" height="500" src="https://static.tornsignpost.org/images/${cardId}" alt="Loading..."/>`;
                    instance.setContent(content);
                } else {
                    $.get({
                        dataType: "html",
                        url: "https://static.tornsignpost.org/html/" + cardId, success: function (data) {
                            let content = `<div class="p-2">${data}</div>`;
                            instance.setContent(content);
                        }
                    });
                }
            }
        });
}

function regionCommands(event, tag) {
    let regionModal = $("#regionModal");
    regionModal.find(".loaded").hide();
    regionModal.find(".loading").show();
    regionModal.modal('show');
}

function details(event, tag) {
    console.log(event);
    event.preventDefault();
    event.stopPropagation();
    $(`[aria-controls='${tag}'] i`).toggleClass("d-none");
    tippy.hideAll({duration: 0});
    if (refresher) clearTimeout(refresher);
    DS.doToggle(game, tag, {callback: processData, errorHandler: errorhandler});
}

function showStatus(data) {
    if (data !== "") {
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
        let turnContent = $("<p/>").addClass("chat").html(content);
        historyDiv.append(turnContent);
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
    DS.updateProfile(email, discordID, veknID, {callback: processData, errorHandler: updateProfileErrorHandler});
}

function updatePassword() {
    let profileNewPassword = dwr.util.getValue("profileNewPassword");
    let profileConfirmPassword = dwr.util.getValue("profileConfirmPassword");
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
    let viewport = $('meta[name=viewport]').get(0);
    let text = (
        viewport.content === DESKTOP_VIEWPORT_CONTENT
            ? 'Mobile' : 'Desktop') + ' View';
    let button = $('<a/>')
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
    let $link = $('#toggleMobileViewLink').eq(0);
    let viewport = $('meta[name=viewport]').get(0);
    console.log('before: ' + viewport.content)
    if (viewport.content === DESKTOP_VIEWPORT_CONTENT) {
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
