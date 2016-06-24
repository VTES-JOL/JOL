<%@page contentType="text/html" %>
<table border="1" width="100%">
    <tr>
        <td width="25%" align="top">
            <label for="decks">Your decks:</label>
            <div class="gamediv">
                <table class="gametable" id="decks" border="1" cellspacing="1" cellpadding="1" width="100%">
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
            <button onclick="doregister();">Register</button>
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
                            <button onclick="doedit();">Edit</button>
                            <button onclick="donewdeck();">New</button>
                        </div>
                        <div id="deckedit" style="display :none;">
                            <button onclick="dosave();">Save</button>
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
                        <button id="adjust" onclick="doadjust();">Parse deck</button>
                    </td>
                </tr>
            </table>
            <form action="javascript:dosearch();">
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
                            <button onclick="dosearch();">Search cards</button>
                        </td>
                    </tr>
                </table>
            </form>
            <div class="cardsdiv">
                <table class="gametable" id="showcards" cellspacing="0" cellpadding="0" border="0"></table>
            </div>
        </td>
        <td align="top">
            <p>Deck errors:</p>
            <div id="deckerrors" class="errdiv">
            </div>
            <hr/>
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