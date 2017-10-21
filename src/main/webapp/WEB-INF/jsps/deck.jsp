<div class='container'>
    <div class='col col-3'>
        <div id='gameRegistration' class='box'>
            <h4 class="header">Game Registration:</h4>
            <table class="light clean-table" id="opengames"></table>
            <div class="footer">
                <select title="reggames" id="reggames"></select>
                <select title="Choose deck" id="regdecks"></select>
                <button onclick="doRegister();">Register</button>
            </div>
        </div>
        <div id='playerDecks' class='box'>
            <h4 class="header">Your decks:</h4>
            <table id="decks" class="clean-table light">
            </table>
        </div>
    </div>
    <div class='col col-4'>
        <div id='deckEditor' class='box'>
            <h4 class="header">
                Deck Editor
                <span class="float-right">
                    <span id="noedit">
                        <button onclick="doEdit();">Edit</button>
                        <button onclick="doNewDeck();">New</button>
                    </span>
                    <span id="deckEdit" style="display :none;">
                        <button onclick="doSave();">Save</button>
                    </span>
                </span>
            </h4>
            <div class="light">
                <textarea id="deckText" readonly="readonly"></textarea>
            </div>
            <div class="footer">
                <label for="deckName">Name:</label>
                <input readonly="readonly" id="deckName" type="text" size="20" maxlength="40"/>
                <div class="float-right">
                    <label for="shuffle">Shuffle:</label>
                    <input type="checkbox" id="shuffle" name="shuffle" value="yes"/>
                    <button id="adjust" onclick="doAdjust();">Parse deck</button>
                </div>
            </div>
            <div id="deckerrors"></div>
            <div id="deckcontents" class="full-width"></div>
        </div>
    </div>
    <div class='col col-3'>
        <div id='editorCardDetails' class='box'>
            <h4 class="header">
                Card Text:
                <select id="deckcards" onchange="selectCardDeck()" class="float-right"></select>
            </h4>
            <div class="light">
                <input type="hidden" id="cardSelect" value="history"/>
                <div class="history padded" id="cardtext"></div>
            </div>
        </div>
        <div id='cardSearch' class='box'>
            <h4 class="header">Card Search</h4>
            <form action="javascript:doSearch();" class="light padded">
                <table class="clean-no-border">
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
                            <button>Search cards</button>
                        </td>
                    </tr>
                </table>
                <div class="cardsdiv history">
                    <table id="showcards"></table>
                </div>
            </form>
        </div>
    </div>
</div>