<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<table width="100%" border="2">
    <tr>
        <td valign="top" width="30%" id="hand"></td>
        <td id="dsForm">
            <form onsubmit="return doSubmit();">
                <table>
                    <tr id="phasecommand">
                        <td>
                            <label for="phase">Phase:</label>
                        </td>
                        <td>
                            <select class="btn-vtes-default" id="phase" name="phase"></select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="command">Command:</label>
                        </td>
                        <td>
                            <input name="command" type="text" id="command" size="25" maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="chat">Chat:</label>
                        </td>
                        <td>
                            <input name="chat" type="text" id="chat" size="25" maxlength="100"/>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="ping">Ping:</label>
                        </td>
                        <td>
                            <select class="btn-vtes-default" id="ping" name="ping"></select>
                        </td>
                    </tr>
                    <tr id="endcommand">
                        <td>
                            <label for="endturn">End turn?</label>
                        </td>
                        <td>
                            <select class="btn-vtes-default" id="endturn" name="endturn">
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
        <td colspan="2" valign=top>
            <div id="globalPad">
                <label for="global">Global notes and pending actions:</label><br/>
                <textarea rows="4" cols="50" name="global" id="global"></textarea>
            </div>
            <div id="playerPad">
                <label for="notes">Private notepad:</label><br/>
                <textarea rows="4" cols="50" name="notes" id="notes"></textarea>
            </div>
        </td>
    </tr>
    <tr>
        <td align="left">
            <table>
                <tr>
                    <td align="left">
                        <div id="gamename"></div>
                    </td>
                    <td align="right">
                        <div id="gamestamp"></div>
                </tr>
            </table>
        </td>
        <td align="center">
            <span>Current Turn: <span id="turnlabel"></span></span>
        </td>
        <td align="left">
            <select class="btn-vtes-default" title="cards" id="cards" name="cards" onchange="selectCard()">
                <option value="NOCARD">Show history</option>
            </select>
        </td>
        <td align="right">
            <span>Turn selector:<select class="btn-vtes-default" title="turns" id="turns" name="turns" onchange="getHistory()"></select></span>
        </td>
    </tr>
    <tr>
        <td colspan="2" width="60%">
            <div class="history" id="curturn">
                <table class="chattable" cellspacing="0" cellpadding="0" border="0" id="curturntable"></table>
            </div>
        </td>
        <td colspan="2" width="40%">
            <div class="history" id="extra">
                <input type="hidden" id="extraSelect" value="history"/>
                <div id="history"></div>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="4">
            <span id="state"></span>
        </td>
    </tr>
</table>