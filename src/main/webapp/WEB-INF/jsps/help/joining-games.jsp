<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="container-fluid tab-pane fade" role="tabpanel" aria-labelledby="help2" tabindex="0" id="panel2">
    <div class="row">
        <div class="col-3 col-xxl-2">
            <div class="h-100 flex-column align-items-stretch">
                <nav class="nav nav-pills flex-column" id="join-help-nav">
                    <a class="nav-link active" href="#lobby">The Lobby</a>
                    <a class="nav-link" href="#privateGames">Private Games</a>
                    <a class="nav-link" href="#publicGames">Public Games</a>
                    <a class="nav-link" href="#deckRegistration">Registering a Deck</a>
                </nav>
            </div>
        </div>
        <div class="col-9 col-xxl-10">
            <div data-bs-spy="scroll" data-bs-target="#join-help-nav" data-bs-smooth-scroll="true" tabindex="0" class="scrollable mhd-80">
                <div id="lobby">
                    <h4 class="mt-2">The Lobby</h4>
                    The lobby has 3 main sections.
                    <ul>
                        <li>Private game management</li>
                        <li>View / Join public games</li>
                        <li>Register decks for joined games</li>
                    </ul>
                    Creating a game is optional, there are a few automatically generated public games that are free to
                    join, with the option to create more if you wish.<br/>
                    There are two steps to joining a game
                    <ol>
                        <li>Be invited to a private game / Join a public game</li>
                        <li>Register a deck with a invited / joined game</li>
                    </ol>
                    Games are created using the <strong>Create Game</strong> section. If you create a game note that the
                    name must be unique across all games, and special characters in the name are not allowed.<br/>
                    Tick the <strong>Public Game</strong> option to create a public game that anyone can join.
                </div>
                <div id="privateGames">
                    <h4 class="mt-2">Private Games</h4>
                    If you create a private game you are required to send out invitations to other players, and choose
                    when to start the game.<br/>
                    The only requirement to starting a game is that at least 1 player is registered. You can use this
                    feature to create a private game where you can test the flow of your deck, or experiment with
                    commands before joining a real game with other players.<br/>
                    The <strong>My Games</strong> section will have options to invite players by name, and view the
                    registered players
                    <div class="card shadow w-50 my-2">
                        <div class="card-header bg-body-secondary">
                            <h5>My Games</h5>
                        </div>
                        <ul class="list-group list-group-flush" id="currentGames">
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center"><h6 class="d-inline">Test
                                    Game</h6><span class="d-flex justify-content-between align-items-center gap-1"><button
                                        class="btn btn-outline-secondary btn-sm" disabled>Start</button><button
                                        class="btn btn-outline-secondary btn-sm" disabled>Close</button></span></div>
                                <table class="table table-bordered table-sm table-hover mt-2">
                                    <tbody>
                                    <tr>
                                        <td class="w-25">Caine</td>
                                        <td class="w-75">Crypt: 12 Library: 70 Groups: 3/4</td>
                                    </tr>
                                    <tr>
                                        <td class="w-25">Lilith</td>
                                        <td class="w-75"></td>
                                    </tr>
                                    <tr>
                                        <td class="w-25">Saulot</td>
                                        <td class="w-75"></td>
                                    </tr>
                                    </tbody>
                                </table>
                            </li>
                        </ul>
                        <div class="card-footer">
                            <label for="testPlayerList" class="form-label">Players</label>
                            <input class="form-control ui-autocomplete-input" id="testPlayerList"
                                   placeholder="Start typing a player name" autocomplete="off" disabled>
                            <label class="form-label mt-2" for="helpGame">Games</label>
                            <select class="form-select" id="helpGame" disabled>
                                <option value="Test Game">Test Game</option>
                            </select>
                            <button class="btn btn-outline-secondary btn-sm mt-2" disabled>Invite</button>
                        </div>
                    </div>
                </div>
                <div id="publicGames">
                    <h4 class="mt-2">Public Games</h4>
                    <p>
                        Public games are either created automatically every few minutes by the system, or manually by a
                        player and set to public
                    </p>
                    <p>
                        Public games expire after 5 days, the expiry time alongside the game name. When a player registers a deck to a public game, this expiry time is refreshed. This is in place to make sure that game spots are continually being utilized fully<br/>
                    </p>
                    <p>
                        When a public game reaches 5 registered players it will start automatically.<br/>
                    </p>
                    <p>
                        If a public game would expire, and there are 4 registered players, it will start as a 4 player
                        game
                        There is currently no way to start a public 3 player or less game, use the private games feature for
                        this.
                    </p>
                    You can leave a public game before it starts by clicking the <strong>Leave</strong> button.
                    <div class="card shadow my-2 w-50">
                        <div class="card-header bg-body-secondary">
                            <h5>Public Games</h5>
                        </div>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="d-inline">Brave eating Interest</h6>
                                    <span class="d-flex justify-content-between align-items-center gap-1">
                                        <span>Closes in 16 hours</span>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Join</button>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Leave</button>
                                    </span>
                                </div>
                                <table class="table table-bordered table-sm table-hover mt-2">
                                    <tbody>
                                    <tr>
                                        <td class="w-50">Cairne</td>
                                        <td class="w-50 text-center">Registered</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="d-inline">Freezing cantering August</h6>
                                    <span class="d-flex justify-content-between align-items-center gap-1">
                                        <span>Closes in 16 hours</span>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Join</button>
                                    </span>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="d-inline">Bored declassifying Department</h6>
                                    <span class="d-flex justify-content-between align-items-center gap-1">
                                        <span>Closes in 19 hours</span>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Join</button>
                                    </span>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="d-inline">Rich bushelling Wrecker</h6>
                                    <span class="d-flex justify-content-between align-items-center gap-1">
                                        <span>Closes in 4 days</span>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Join</button>
                                    </span>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="d-inline">Test Public</h6>
                                    <span class="d-flex justify-content-between align-items-center gap-1">
                                        <span>Closes in 5 days</span>
                                        <button class="btn btn-outline-secondary btn-sm" disabled>Join</button>
                                    </span>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
                <div id="deckRegistration">
                    <h4 class="mt-2">Registering a Deck</h4>
                    <p>
                        You register your deck for both private and public games in the <strong>Register Deck</strong> section
                    </p>
                    All your valid decks that are in the <span class="badge text-bg-secondary">MODERN</span> format will be available.
                    Choose the deck and the game, and click <strong>Register</strong>.<br/>
                    You will be able to see the games and their registration status in the panel.
                    <div class="card shadow w-50 my-2">
                        <div class="card-header bg-body-secondary">
                            <h5>Register Deck</h5>
                        </div>
                        <ul class="list-group list-group-flush">
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span>Rich bushelling Wrecker</span>
                                    <span></span>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span>Test Game</span>
                                    <span>Crypt: 12 Library: 70 Groups: 3/4</span>
                                </div>
                            </li>
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span>Brave eating Interest</span>
                                    <span>Crypt: 12 Library: 90 Groups: 4/5</span>
                                </div>
                            </li>
                        </ul>
                        <div class="card-footer">
                            <label for="testDeckList" class="form-label">Decks</label>
                            <select class="form-select" id="testDeckList" disabled><option value="Assamite Bleed Vote">Assamite Bleed Vote</option></select>
                            <label for="testGameList" class="form-label mt-2">Invited Games</label>
                            <select class="form-select" id="testGameList" disabled><option value="Rich bushelling Wrecker">Rich bushelling Wrecker</option></select>
                            <button class="btn btn-outline-secondary btn-sm mt-2" disabled>Register</button>
                            <div id="registerResult" class="badge text-bg-light">Successfully registered Assamite Bleed Vote in game Rich bushelling Wrecker</div>
                        </div>
                    </div>
                    You can change your deck registration at any time up until the game starts.
                </div>
            </div>
        </div>
    </div>
</div>