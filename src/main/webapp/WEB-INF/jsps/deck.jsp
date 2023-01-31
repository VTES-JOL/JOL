<div class='row'>
    <div class='col-sm-4'>
        <div class='box'>
            <h4 class="header">Your decks:</h4>
            <table id="decks" class="clean-table light">
            </table>
        </div>
    </div>
    <div class="col-sm-4">
        <div class="box">
            <h4 class="header">
                Deck Editor
                <button class="btn btn-info btn-sm float-right" onclick="newDeck()">New Deck</button>
            </h4>
            <div>
                <label for="deckName">Deck Name:</label><input type="text" id="deckName" size="35"/>
                <button class="btn btn-info btn-sm float-right left-margin" onclick="saveDeck()">Save</button>
                <button class="btn btn-info btn-sm float-right" onclick="parseDeck()">Check</button>
            </div>
            <div class="light">
                <textarea id="deckText"></textarea>
            </div>
        </div>
        <div class="box">
            <h4 class="header">Deck Errors</h4>
            <div id="deckErrors"></div>
        </div>
    </div>
    <div class="col-sm-4">
        <div class="box">
            <h4 class="header">Deck Preview <span id="deckSummary" class="float-right"></span></h4>
            <div class="light">
                <div id="deckPreview"></div>
            </div>
        </div>
    </div>
</div>
