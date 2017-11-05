<table class="game-table light">
    <tr>
        <td class="game-cell command-cell">
            <div id="hand"></div>
        </td>
        <td class="game-cell command-cell">
            <div class="game-header">
                <h5>Commands</h5>
                <button id="gameDeck" onclick="doShowDeck();">Deck</button>
            </div>
            <form onsubmit="return doSubmit();" autocomplete='off' class="padded" id="dsForm">
                <table>
                    <tr id="phasecommand">
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
                            <input name="command" type="text" id="command" maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="chat">Chat:</label>
                        </td>
                        <td>
                            <input name="chat" type="text" id="chat" maxlength="100"/>
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
                    <tr id="endcommand">
                        <td>
                            <label for="endturn">End turn?</label>
                        </td>
                        <td>
                            <select id="endturn" name="endturn">
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
            <form onsubmit="return doGameChat();" autocomplete="off" class="padded" id="judgeForm">
                <table>
                    <tr>
                        <td>
                            <label for="judgeChat">Judge Chat:</label>
                        </td>
                        <td>
                            <input id="judgeChat" name="judgeChat" maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input name="Submit" type="submit" value="Submit"/>
                        </td>
                    </tr>
                </table>
            </form>
        </td>
        <td id="gameNotes" class="game-cell command-cell-double" colspan="2">
            <div id="globalPad" class="half-height">
                <div class="game-header">
                    <h5>Global notes and pending actions:</h5>
                </div>
                <textarea name="global" id="global"></textarea>
            </div>
            <div id="playerPad" class="half-height">
                <div class="game-header">
                    <h5>Private notes:</h5>
                </div>
                <textarea name="notes" id="notes"></textarea>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="game-cell">
            <div class="game-header">
                <h5 id="gamename"></h5>
                <span>Current Turn: <span id="turnlabel"></span></span>
            </div>
            <div id="curturn" class="history">
                <div id="gameChat"></div>
            </div>
        </td>
        <td colspan="2" class="game-cell">
            <div class="game-header">
                <h5>&nbsp;</h5>
                <span>
                <select title="cards" id="cards" name="cards" onchange="selectCard()">
                    <option value="history">History</option>
                </select>
                <select title="turns" id="turns" name="turns" onchange="getHistory()"></select>
                </span>
            </div>
            <div id="extra" class="history grey">
                <input type="hidden" id="extraSelect" value="history"/>
                <div id="history"></div>
                <div id="gameDeckContents" class="display: none;"></div>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="4" id="state" class="no-padding">
        </td>
    </tr>
</table>