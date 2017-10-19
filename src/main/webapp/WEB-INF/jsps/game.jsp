<table class="game-table light">
    <tr>
        <td id="hand" class="game-cell command-cell"></td>
        <td id="dsForm" class="game-cell command-cell">
            <h5 class="game-header">Commands</h5>
            <form onsubmit="return doSubmit();" autocomplete='off'>
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
        </td>
        <td id="gameNotes" class="game-cell command-cell-double" colspan="2">
            <div id="globalPad" class="half-height">
                <h5 class="game-header">Global notes and pending actions:</h5>
                <textarea name="global" id="global"></textarea>
            </div>
            <div id="playerPad" class="half-height">
                <h5 class="game-header">Private notepad:</h5>
                <textarea name="notes" id="notes"></textarea>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="2" class="game-cell">
            <h5 class="game-header">
                <span id="gamename" class="label-basic"></span>
                <span class="float-right label-basic">Current Turn: <span id="turnlabel"></span></span>
            </h5>
            <div id="curturn" class="history">
                <table id="curturntable"></table>
            </div>
        </td>
        <td colspan="2" class="game-cell">
            <h5 class="game-header">
                <select title="cards" id="cards" name="cards" onchange="selectCard()">
                    <option value="NOCARD">Show history</option>
                </select>
                <span class="float-right">Turn selector:<select title="turns" id="turns" name="turns" onchange="getHistory()"></select></span>
            </h5>
            <div id="extra" class="history grey">
                <input type="hidden" id="extraSelect" value="history"/>
                <div id="history"></div>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="4" id="state" class="no-padding">
        </td>
    </tr>
</table>