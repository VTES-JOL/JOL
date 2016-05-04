<%@page contentType="text/html" %>
<table border=1 width=100%>
    <tr>
        <td width=25% align=top>
            Your decks:
            <div class="gamediv">
                <table class="gametable" id="decks" border="1" cellspacing="1" cellpadding="1" width="100%">
                </table>
            </div>
            Open games:
            <table id="opengames" border="1" cellspacing="1" cellpadding="1" width="100%">
            </table>
            Register for game:
            <select id="reggames">
            </select>
            <select id="regdecks">
            </select>
            <button onclick="doregister();">Register</button>
        </td>
        <td width=40%>
            <table width=100%>
                <tr>
                    <td align=left>Name: <input readonly=readonly id=deckname type=text size=20 maxlength=30/></td>
                    <td align=right>
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
            <textarea rows=25 cols=60 id="decktext" readonly=readonly></textarea>
            <table width="100%">
                <tr>
                    <td align=left>Search for cards:</td>
                    <td align=right>
                        Shuffle:<input type="checkbox" name="shuffle" value="yes"/>
                        <button id="adjust" onclick="doadjust();">Parse deck</button>
                    </td>
                </tr>
            </table>
            <form action="javascript:dosearch();">
                Type: <select id="cardtype">
                <option value="All">All</option>
            </select><br/>
                Query: <input type=text id=cardquery/><br/>
                <button onclick="dosearch();">Search cards</button>
                <br/>
            </form>
            <div class="cardsdiv">
                <table class="gametable" id="showcards" cellspacing=0 cellpadding=0 border=0></table>
            </div>
        </td>
        <td align=top>
            Deck errors: <br/>
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
            Card Texts:
            <select id="deckcards" onchange="selectCardDeck()"></select>
            <input type=hidden id="cardSelect" value="history"/>
            <div class="history" id="cardtext"></div>
        </td>
    </tr>
</table>