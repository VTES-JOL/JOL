<table class="game-table">
    <tr>
        <td width="30%" id="hand"></td>
        <td id="dsForm">
            <h5>Commands</h5>
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
        <td colspan="2" id="gameNotes">
            <div id="globalPad">
                <h5>Global notes and pending actions:</h5>
                <textarea name="global" id="global"></textarea>
            </div>
            <div id="playerPad">
                <h5>Private notepad:</h5>
                <textarea name="notes" id="notes"></textarea>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <small id="gamename"></small>
            <small id="gamestamp" class="float-right"></small>
        </td>
        <td>
            <small class="float-right">Current Turn: <span id="turnlabel"></span></small>
        </td>
        <td colspan="2">
            <select title="cards" id="cards" name="cards" onchange="selectCard()">
                <option value="NOCARD">Show history</option>
            </select>
            <span class="float-right">Turn selector:<select title="turns" id="turns" name="turns" onchange="getHistory()"></select></span>
        </td>
    </tr>
    <tr>
        <td colspan="2" width="60%">
            <div class="history" id="curturn">
                <table id="curturntable"></table>
            </div>
        </td>
        <td colspan="2" width="40%">
            <div class="history" id="extra">
                <input type="hidden" id="extraSelect" value="history"/>
                <div id="history"></div>
            </div>
        </td>
    </tr>
    <tr class="no-padding">
        <td colspan="4" id="state" class="no-padding">
        </td>
    </tr>
</table>