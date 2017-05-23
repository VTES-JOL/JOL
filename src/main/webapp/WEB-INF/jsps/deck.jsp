<%@page contentType="text/html" %>
<table border="1" width="100%">
    <tr>
        <td width="25%" align="top">
            <label for="decks">Your decks:</label>
            <div class="gamediv">
                <table id="mydecks" class="gametable" border="1" cellspacing="1" cellpadding="1" width="100%">
                    <thead>
                        <colgroup>
                            <col width="50%"/>
                            <col width="40%"/>
                            <col width="10%"/>
                        </colgroup>
                    </thead>
                    <tbody id="decks">

                    </tbody>
                </table>
            </div>
            <span>Open games:</span>
            <table id="opengames" border="1" cellspacing="1" cellpadding="1" width="100%">
            </table>
            <label for="reggames">Register for game:</label>
            <select title="reggames" id="reggames">
            </select>
            <select title="Choose deck" id="regdecks">
            </select>
            <button onclick="doRegister();" class="btn-vtes-default">Register</button>
        </td>
        <td width="40%">
            <table width="100%">
                <tr>
                    <td align="left">
                        <label for="deckname">Name:</label>
                        <input readonly="readonly" id="deckname" type="text" size="40" maxlength="40"/>
                    </td>
                    <td align="right">
                        <div id="noedit">
                            <button class="btn-vtes-default" onclick="doEdit();">Edit</button>
                            <button class="btn-vtes-default" onclick="doNewDeck();">New</button>
                        </div>
                        <div id="deckedit" style="display :none;">
                            <button onclick="doSave();">Save</button>
                        </div>
                    </td>
                </tr>
            </table>
            <textarea rows="25" cols="60" id="decktext" readonly="readonly" style="width:100%;"></textarea>
            <table width="100%">
                <tr>
                    <td align=left>Search for cards:</td>
                    <td align=right>
                        <label for="shuffle">Shuffle:</label>
                        <input type="checkbox" id="shuffle" name="shuffle" value="yes"/>
                        <button class="btn-vtes-default" id="adjust" onclick="doAdjust();">Parse deck</button>
                    </td>
                </tr>
            </table>
            <form action="javascript:doSearch();">
                <table>
                    <tr>
                        <td>
                            <label for="cardtype">Type:</label>
                        </td>
                        <td>
                            <select id="cardtype">
                                <option value="All">All</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="cardquery">Query:</label>
                        </td>
                        <td>
                            <input type="text" id="cardquery"/><br/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <button class="btn-vtes-default">Search cards</button>
                        </td>
                    </tr>
                </table>
            </form>
            <div class="cardsdiv">
                <table class="gametable" id="showcards" cellspacing="0" cellpadding="0" border="0"></table>
            </div>
        </td>
        <td align="top">
            <div id="deckerrors" class="errdiv">
            </div>
            <div id="deckcontentdiv">
                <table>
                    <tr>
                        <td id="deckcontents"></td>
                    </tr>
                </table>
            </div>
            <hr/>
            <p>Card Texts:</p>
            <select id="deckcards" onchange="selectCardDeck()"></select>
            <input type="hidden" id="cardSelect" value="history"/>
            <div class="history" id="cardtext"></div>
        </td>
    </tr>
</table>