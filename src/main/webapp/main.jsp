<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <title>V:TES Online</title>

    <!-- Required by Bootstrap -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <link rel="stylesheet" type="text/css" href="css/styles.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/light.css"/>
    <link rel="shortcut icon" href="images/favicon.ico"/>
    <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
</head>
<body>
<div id="loadMessage" class="col text-center">
    <h1>Loading...</h1>
</div>
<div id="loaded" style="display :none;">
    <jsp:include page="/WEB-INF/jsps/topbar.jsp"/>

    <div id="content" class="container-fluid">
        <div id="main">
            <jsp:include page="/WEB-INF/jsps/main.jsp"/>
        </div>

        <div id="game" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/game.jsp"/>
        </div>

        <div id="active" style="display:none;">
            <jsp:include page="/WEB-INF/jsps/active.jsp"/>
        </div>

        <div id="deck" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/deck.jsp"/>
        </div>

        <div id="admin" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/admin.jsp"/>
        </div>

        <div id="tournament" style="display: none">
            <jsp:include page="/WEB-INF/jsps/tournament.jsp"/>
        </div>

        <div id="help" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/commands.jsp"/>
        </div>

        <div id="profile" style="display:none">
            <jsp:include page="/WEB-INF/jsps/profile.jsp"/>
        </div>

        <div id="super" style="display:none">
            <jsp:include page="/WEB-INF/jsps/super.jsp"/>
        </div>
        <!-- Footer -->
        <div class="row mt-2">
            <div class="col">
                <span id="chatstamp" class="label label-light label-basic navbar-text"></span>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="quickCommandModal" tabindex="-1" role="dialog" aria-labelledby="quickCommandModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickCommandModalLabel">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Command</span>
                </h5>
            </div>
            <div class="modal-body">
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn
                    edge
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw
                    crypt
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">
                    Discard random
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">
                    Shuffle crypt
                </button>

                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +1')">Pool +1
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +2')">+2</button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +3')">+3</button>

                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -1')">Pool -1
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -2')">-2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -3')">-3</button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="quickChatModal" tabindex="-1" role="dialog" aria-labelledby="quickChatModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickChatModalLabel">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Chat</span>
                </h5>
            </div>
            <div class="modal-body">
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Bleed')">Bleed</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Hunt')">Hunt</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Block?')">Block?</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No block')">No block
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Yes')">Yes</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No')">No</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Wait')">Wait</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('1')">1</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('2')">2</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('3')">3</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('4')">4</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre-range')">No
                    pre-range
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No
                    pre, no maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Long')">Long</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No grapple')">No grapple
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Wave')">Wave</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No additional strikes')">No
                    additional strikes
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No press')">No press
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Combat ends')">Combat ends
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendChat('No sudden/wash')">No
                    sudden/wash
                </button>
            </div>
        </div>
    </div>
</div>

<div class="modal" id="playCardModal" tabindex="-1" role="dialog" aria-labelledby="playCardModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content loading" style="height:30vh;text-align:center">
            <h2 style="position:relative;top:43%">Loading...</h2>
        </div>
        <div class="modal-content loaded" style="text-align:center">
            <div class="modal-header">
                <h5 class="modal-title">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span class="card-type action"></span>
                    <span class="card-name" id="playCardModalLabel">Song of Serenity</span>
                </h5>
            </div>
            <div class="modal-body">
                <div class="requirements">
                    <span class="card-clan"></span>
                    <span class="card-cost">Costs 1 blood.</span>
                </div>
                <p class="mb-2">
                    <img class="burn-option" src="images/burn-option.png"></img>
                    <span class="preamble">Only usable before range is chosen.</span>
                </p>
                <div class="card-modes"></div>
                <div class="templates d-none">
                    <button type="button" class="card-mode btn btn-block btn-outline-dark mb-2" aria-pressed="false"
                            data-toggle="button">
                        <span class="discipline">a</span>
                        <p class="mode-text">The opposing minion gets -1 strength this round. A vampire may play only
                            one Song of Serenity each combat.</p>
                    </button>
                </div>
                <div class="extended-play-panel" style="display:none">
                    <hr/>
                    <button id="playCardModalPlayButton" type="button"
                            class="btn btn-block btn-primary mb-2" style="white-space:normal"
                            onclick="playCard(event);">Play
                    </button>
                </div>
                <div class="d-flex justify-content-center">
                    <button type="button" class="btn btn-outline-dark round-button mx-1"
                            title="Previous card"
                            onclick="previousCard();">&lt;
                    </button>
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard and replace"
                            onclick="discard();">
                        <span>&#10607;</span>
                    </button>
                    <button type="button" class="btn btn-outline-danger round-button mx-1"
                            title="Discard"
                            onclick="discard(false);">
                        <span>&#10585;</span>
                    </button>
                    <button type="button" class="btn btn-outline-dark round-button mx-1"
                            title="Next card"
                            onclick="nextCard();">&gt;
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="targetPicker" class="fixed-top" style="display:none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">
                    <button type="button" class="close" onclick="closeTargetPicker()">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span class="card-type action"></span>
                    <span class="card-name">Song of Serenity</span>
                </h5>
            </div>
            <div class="modal-body">Pick target.</div>
        </div>
    </div>
</div>

<div class="modal" id="cardModal" tabindex="-1" role="dialog" aria-labelledby="cardModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content loading" style="height:30vh;text-align:center">
            <h2 style="position:relative;top:43%">Loading...</h2>
        </div>
        <div class="modal-content loaded" style="text-align:center">
            <div class="modal-header">
                <h5 class="modal-title">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span class="card-clan"></span>
                    <span class="card-name" id="cardModalLabel">Maris Streck</span>
                </h5>
            </div>
            <div class="modal-body">
                <div class="d-flex align-items-center">
                    <span class="discipline">a</span>
                    <span class="votes" title="Votes">3</span>
                </div>
                <p class="mb-0">
                    <span class="card-label label label-light"></span>
                    <span class="card-text"></span>
                </p>
            </div>
            <div class="modal-footer d-flex flex-wrap justify-content-center">
                <div class="transfers-and-counters">
                    <div class="transfers">
                        <div class="transfer-btn transfer-btn-left" title="Transfer one pool to this card"
                             onclick="transferToCard();">&#9668;
                        </div>
                        <div class="transfer-btn transfer-btn-right" title="Transfer one blood to your pool"
                             onclick="transferToPool();">&#9658;
                        </div>
                        <div class="card-modal-pool">99 pool</div>
                    </div>
                    <div class="counters blood"
                         title="Counters; click right side to increase, left to decrease"
                         onclick="countersClicked(event)">99/99
                    </div>
                </div>
                <button type="button" class="btn btn-outline-dark m-1" title="Play"
                        data-region="inactive-region" data-top-level-only
                        onclick="playVamp();">
                    <span>Play</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Bleed"
                        data-region="ready-region" data-lock-state="unlocked"
                        data-top-level-only
                        onclick="bleed();">
                    <span>Bleed</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Contest"
                        data-region="ready-region torpor" data-contested="false"
                        onclick="contest(true);">
                    <span>Contest</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Clear Contest"
                        data-region="ready-region torpor" data-contested="true"
                        onclick="contest(false);">
                    <span>Clear Contest</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Hunt"
                        data-region="ready-region" data-lock-state="unlocked"
                        data-top-level-only
                        onclick="hunt();">
                    <span>Hunt</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Torpor"
                        data-region="ready-region"
                        data-top-level-only
                        onclick="torpor();">
                    <span>Send to Torpor</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Go Anarch"
                        data-region="ready-region" data-lock-state="unlocked"
                        data-top-level-only
                        onclick="goAnarch();">
                    <span>Go Anarch</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Leave Torpor"
                        data-region="torpor" data-lock-state="unlocked"
                        data-top-level-only
                        onclick="leaveTorpor();">
                    <span>Leave Torpor</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Lock"
                        data-lock-state="unlocked"
                        onclick="lock();">
                    <span>&cudarrr;</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Unlock"
                        data-lock-state="locked"
                        onclick="unlock();">
                    <div style="transform: rotate(-90deg);">&#10548;</div>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Block"
                        data-region="ready-region" data-top-level-only
                        onclick="block();">
                    <span>Block</span>
                </button>
                <button type="button" class="btn btn-outline-dark m-1" title="Burn"
                        onclick="burn();">
                    <span>Burn</span>
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap -->
<!-- jQuery first, then Popper.js, then Bootstrap JS -->
<script type="text/javascript" src="js/jquery-3.4.1.js"></script>
<script src="https://unpkg.com/popper.js@1.15.0/dist/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
        integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
        crossorigin="anonymous"></script>

<script type='text/javascript' src="js/ga.js"></script>
<script type='text/javascript' src='js/moment-with-locales.min.js'></script>
<script type='text/javascript' src='js/moment-timezone-with-data.min.js'></script>
<script type="text/javascript" src="js/jquery-ui.js"></script>
<script type="text/javascript" src="js/jquery-throttle.js"></script>
<script type='text/javascript' src='dwr/engine.js'></script>
<script type='text/javascript' src='dwr/interface/DS.js'></script>
<script type='text/javascript' src='dwr/util.js'></script>
<script type='text/javascript' src="js/tippy.all.min.js"></script>
<script type='text/javascript' src='js/ds.js?version=<%= System.getenv("JOL_VERSION") %>&i=1'></script>
<script type="text/javascript" src="js/card-modal.js?version=<%= System.getenv("JOL_VERSION") %>"></script>
<script src='https://www.google.com/recaptcha/api.js'></script>
</body>
</html>
