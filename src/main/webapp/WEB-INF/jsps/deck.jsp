<div class='row'>
    <div class='col-sm-4'>
        <div id='playerDecks' class='box'>
            <h4 class="header">Your decks:</h4>
            <table id="decks" class="clean-table light">
            </table>
        </div>
    </div>
    <div class='col-sm-4 p-sm-0'>
        <div id='deckEditor' class='box'>
            <h4 class="header">
                Deck Editor
                <span class="float-right">
                    <span id="noedit">
                        <button class="btn btn-secondary btn-sm" onclick="doEdit();">Edit</button>
                        <button class="btn btn-secondary btn-sm" onclick="doNewDeck();">New</button>
                    </span>
                    <span id="deckEdit" style="display :none;">
                        <button class="btn btn-primary btn-sm" onclick="doSave();">Save</button>
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
                    <button id="adjust" class="btn btn-info btn-sm" onclick="doAdjust();">Parse</button>
                </div>
            </div>
            <div id="deckerrors"></div>
            <div id="deckcontents" class="full-width"></div>
        </div>
    </div>
    <div class='col-sm-4'>
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
                            <button class="btn btn-info btn-sm">Search</button>
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
