<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="container-fluid tab-pane fade show active" role="tabpanel" aria-labelledby="help1" tabindex="0" id="panel1">
    <div class="row">
        <div class="col-3 col-xxl-2">
            <div class="h-100 flex-column align-items-stretch">
                <nav class="nav nav-pills flex-column" id="deck-help-nav">
                    <a class="nav-link active" href="#cardNames">Card Names</a>
                    <a class="nav-link" href="#deckFormat">Deck Format</a>
                    <a class="nav-link" href="#deckEditor">Using the Deck Editor</a>
                </nav>
            </div>
        </div>
        <div class="col-9 col-xxl-10">
            <div data-bs-spy="scroll" data-bs-target="#deck-help-nav" data-bs-smooth-scroll="true" tabindex="0" class="scrollable h-50">
                <div id="cardNames">
                    <h4 class="mt-2">Card Names</h4>
                    Because JOL uses text based deck lists there are some rules to make sure the decks can be parsed into valid cards.  Each card has one or more unique ways to distinguish it from other cards in the collection.
                    <table class="table mt-2">
                        <thead>
                        <tr class="table-secondary">
                            <th>Rule</th>
                            <th class="w-50">Description</th>
                            <th>Example</th>
                            <th>Valid Names</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <th>Official Card Name</th>
                            <td>Card names are taken from the <a href="https://www.vekn.net/images/stories/downloads/cardlist.txt" target="_blank">VEKN Official Card Text</a></td>
                            <td class="fs-6"><a class="card-name" data-card-id="201257">Sébastien Goulet</a></td>
                            <td class="fs-6"><a class="card-name" data-card-id="201257">Sébastien Goulet</a></td>
                        </tr>
                        <tr>
                            <th>Aliases</th>
                            <td>Aliases are also created for each <strong>AKA</strong> entry in the card list.</td>
                            <td class="fs-6"><a class="card-name" data-card-id="201257">Sébastian Goulet</a></td>
                            <td class="fs-6">
                                <a class="card-name" data-card-id="201257">Sébastian Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a>
                            </td>
                        </tr>
                        <tr>
                            <th>Commas</th>
                            <td>Cards listed in the official list that end with <strong>, The</strong> also have an option to include <strong>The</strong> at the start of the name as well.</td>
                            <td class="fs-6"><a class="card-name" data-card-id="200101">Ankou, The</a></td>
                            <td class="fs-6">
                                <a class="card-name" data-card-id="200101">The Ankou</a><br/>
                                <a class="card-name" data-card-id="200101">Ankou, The</a>
                            </td>
                        </tr>
                        <tr>
                            <th>Unicode Characters</th>
                            <td>To simplify typing card names, any official name, or alias that contains Unicode characters will have an alias created with the characters replaced</td>
                            <td class="fs-6"><a class="card-name" data-card-id="201257">Sebastian Goulet</a></td>
                            <td class="fs-6">
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a>
                            </td>
                        </tr>
                        <tr>
                            <th>Advanced</th>
                            <td>To distinguish advanced vampires from their base the suffix <strong>ADV</strong> is added in parentheses to the end of the name</td>
                            <td class="fs-6"><a class="card-name" data-card-id="201363">Theo Bell <i class="icon adv"></i></a></td>
                            <td class="fs-6"><a class="card-name" data-card-id="201363">Theo Bell (ADV)</a></td>
                        </tr>
                        <tr>
                            <th>Grouping</th>
                            <td>
                                Vampires with the same name, but in different groups can be distinguished by adding the group number in parentheses to the end of the name.  If the vampire is also Advanced the ADV suffix comes last.<br/>
                                It's always valid to include the group number if you wish.
                            </td>
                            <td class="fs-6">
                                <a class="card-name" data-card-id="201362">Theo Bell</a><br/>
                                <a class="card-name" data-card-id="201613">Theo Bell</a>
                            </td>
                            <td class="fs-6">
                                <a class="card-name" data-card-id="201362">Theo Bell</a><br/>
                                <a class="card-name" data-card-id="201613">Theo Bell (G6)</a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    Multiple rules can apply to a single card
                    <table class="table mt-2">
                        <tr class="table-secondary">
                            <th>Card Name</th>
                            <th>Valid JOL Card Names</th>
                        </tr>
                        <tr>
                            <td><a class="card-name" data-card-id="200101">Ankou, The</a></td>
                            <td><a class="card-name" data-card-id="200101">Ankou, The (G5)</a><br/>
                                <a class="card-name" data-card-id="200101">The Ankou</a><br/>
                                <a class="card-name" data-card-id="200101">The Ankou (G5)</a><br/>
                                <a class="card-name" data-card-id="200101">Ankou, The</a></td>
                        </tr>
                        <tr>
                            <td><a class="card-name" data-card-id="201257">Sébastien Goulet</a></td>
                            <td>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastian Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sebastien Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sebastian Goulet</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastien Goulet (G3)</a><br/>
                                <a class="card-name" data-card-id="201257">Sébastian Goulet (G3)</a><br/>
                                <a class="card-name" data-card-id="201257">Sebastien Goulet (G3)</a><br/>
                                <a class="card-name" data-card-id="201257">Sebastian Goulet (G3)</a>
                            </td>
                        </tr>
                        <tr>
                            <td><a class="card-name" data-card-id="201363">Theo Bell <i class="icon adv"></i></a></td>
                            <td><a class="card-name" data-card-id="201363">Theo Bell (ADV)</a><br/>
                                <a class="card-name" data-card-id="201363">Theo Bell (G2 ADV)</a></td>
                        </tr>
                    </table>
                </div>
                <div id="deckFormat">
                    <h4 class="mt-2">Deck Format</h4>
                    JOL currently uses a text based format as the data entry mode.<br/>
                    These are the 4 patterns that JOL looks for in order to create a valid deck.<br/>
                    Anything else that doesn't match these patterns will be discarded.
                    <table class="table mt-2">
                        <tr class="table-secondary">
                            <th>Line Format</th>
                            <th>Description</th>
                        </tr>
                        <tr>
                            <th>
                                4 x Theo Bell
                            </th>
                            <td>Number of cards followed by the character <strong>x</strong> followed by the name of the card.</td>
                        </tr>
                        <tr>
                            <th>4 Theo Bell</th>
                            <td>
                                Number of cards followed by name of the card.
                            </td>
                        </tr>
                        <tr>
                            <th>Theo Bell (ADV)</th>
                            <td>1 copy only of the card</td>
                        </tr>
                        <tr>
                            <th></th>
                            <td>Blank line - will be skipped</td>
                        </tr>
                    </table>
                    Card counts will be combined at the end of the deck parsing process, when saving the deck will be simplified.
                    <table class="table mt-2">
                        <tr class="table-secondary">
                            <th>Example Deck</th>
                            <th>Result</th>
                        </tr>
                        <tr>
                            <td>
                                Theo Bell<br/>
                                Theo Bell<br/>
                                <br/>
                                2 x Theo Bell<br/>
                                2 Theo Bell (ADV)<br/>
                                1 x Theo Bell (G2 ADV)
                            </td>
                            <td>
                                <a class="card-name" data-card-id="201362">4 x Theo Bell</a><br/>
                                <a class="card-name" data-card-id="201363">3 x Theo Bell (ADV)</a>
                            </td>
                        </tr>
                    </table>
                </div>
                <div id="deckEditor">
                    <h4 class="mt-2">Using the Deck Editor</h4>
                    The deck editor function is very basic, there is no search option.  There are dedicated sites that are better suited to searching and building your deck.  See the links below.
                    <ul>
                        <li><a href="https://vdb.im/">VDB</a></li>
                        <li><a href="https://amaranth.vtes.co.nz/">Amaranth</a></li>
                        <li><a href="https://vtesdecks.com/">VTES Decks</a></li>
                    </ul>
                    However, the deck editor will assist you in making sure the cards you have entered are in a valid format, and will highlight errors where it can't find the card specified.<br/>
                    A preview screen is located in the middle that will show the current deck contents, and the grouping / card count at the top of the screen
                    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center w-25 my-2 ms-4">
                        <h5 class="d-inline">Preview</h5>
                        <span id="deckSummary" class="d-flex justify-content-between align-items-center gap-1"><span>Crypt: 13 Library: 67 Groups: 6</span><span class="badge badge-sm text-bg-success">VALID</span></span>
                    </div>
                    Decks that have been created previously and have not been saved in the modern JOL format will have a label beside their name of <span class="badge text-bg-warning">LEGACY</span><br/>
                    These are decks that have not been automatically parsed and contain errors, you will have to manually inspect the deck, confirm the contents, and save the deck to convert it into the <span class="badge text-bg-secondary">MODERN</span> format required to play.
                </div>
            </div>
        </div>
    </div>
</div>