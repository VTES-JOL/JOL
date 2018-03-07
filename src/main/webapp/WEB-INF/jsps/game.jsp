<div id="game-info" class="container reactive-height">
    <div id="gameDetails" class="col-6 container">
        <div id="playerHand" class="player-only col-6 grey border-right">
            <div class="game-header">
                <h5>Hand</h5>
            </div>
            <div id="hand" class="scrollable half-height-content"></div>
        </div>
        <div id="playerCommands" class="player-only col-6 grey">
            <div class="game-header">
                <h5>Commands</h5>
            </div>
            <form onsubmit="return doSubmit()" autocomplete="off">
                <table class="full-width padded">
                    <tr id="phaseCommand">
                        <td>
                            <label for="phase">Phase:</label>
                        </td>
                        <td>
                            <select id="phase" name="phase"></select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="command">Command:</label>
                        </td>
                        <td>
                            <input name="command" type="text" id="command" class="full-width"
                                   maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="chat">Chat:</label>
                        </td>
                        <td>
                            <input name="chat" type="text" id="chat" class="full-width" maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="ping">Ping:</label>
                        </td>
                        <td>
                            <select id="ping" name="ping"></select>
                        </td>
                    </tr>
                    <tr id="endCommand">
                        <td>
                            <label for="endturn">End turn?</label>
                        </td>
                        <td>
                            <select id="endTurn" name="endturn">
                                <option value="No">No</option>
                                <option value="Yes">Yes</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input name="Submit" type="submit" value="Submit"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <span id="status"></span>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
        <div id="gameChatContainer" class="col-8">
            <div class="game-header">
                <h5>Game Chat</h5>
                <span class="" id="gameLabel"></span>
            </div>
            <div id="gameChat" class="scrollable side-padded half-height-content light"></div>
        </div>
    </div>
    <div id="other" class="col-4 grey border-left">
        <div class="game-header">
            <h5>Game Information</h5>
            <select id="otherSelect" onchange="updateOther()">
                <option value="notes">Notes</option>
                <option value="history">History</option>
                <option value="deck" class="player-only">Deck</option>
            </select>
        </div>
        <div id="history" style="display:none;" class="reactive-height-content">
            <h5 class="notes-header">History
                <select id="turns" onchange="getHistory()"></select>
            </h5>
            <div id="historyOutput" class="scrollable side-padded reactive-height-content-header"></div>
        </div>
        <div id="notes" class="container reactive-height-content">
            <div class="col-6 fill-width">
                <h5 class="notes-header">Global Notes</h5>
                <textarea id="globalNotes" class="game-notes border-right reactive-height-content-header"></textarea>
            </div>
            <div class="col-6 fill-width player-only">
                <h5 class="notes-header">Private Notes</h5>
                <textarea id="privateNotes" class="game-notes reactive-height-content-header"></textarea>
            </div>
        </div>
        <div id="gameDeck" class="reactive-height-content">
            <h5 class="notes-header">Registered Deck</h5>
            <div id="gameDeckOutput" class="scrollable side-padded reactive-height-content-header"></div>
        </div>
    </div>
</div>
<table class="game-table light">
    <tr>
        <td id="state" colspan="4" class="no-padding"></td>
    </tr>
</table>