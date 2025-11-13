<div class="container-fluid tab-pane fade bg-secondary-subtle" role="tabpanel" aria-labelledby="help3" tabindex="0"
     id="panel3">
    <h4>Playing cards & Influencing</h4>
    The <span class="badge bg-secondary fs-6 px-2 py-1">play</span> command is used to play cards from your hand, and
    the <span class="badge bg-secondary fs-6 px-2 py-1">influence</span> command is used to play vampires from your
    uncontrolled region.
    Both of these commands add additional context to the game chat, and setup the initial state of the card, like
    capacity, disciplines, etc.<br/>
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">When clicking a card name in the hand, or other playable regions a list of modes
                are available to choose from.
            </li>
            <li class="list-group-item">Cards that have multiple modes will have multiple options.</li>
            <li class="list-group-item">Clicking on a mode that uses disciplines will print those disciplines in chat.
            </li>
            <li class="list-group-item">Cards that require a target card will prompt for this after a mode has been
                selected.
            </li>
            <li class="list-group-item">Sensible defaults for target regions have been chosen, though not every card has
                been implemented correctly this way.
            </li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Drawing cards</h4>
    The best command to use for drawing cards is the <span class="badge bg-secondary fs-6 px-2 py-1">draw</span>
    command. While you can achieve the same result with move, draw provides more context in the game chat.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">Shortcuts to draw a single card from the library, or crypt are available in the
                Quick Commands.
            </li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Discarding cards</h4>
    There is a specific <span class="badge bg-secondary fs-6 px-2 py-1">discard</span> command to allow you to discard a
    card from your hand only, with the ability to do this randomly, and the option to replace automatically or not.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">There is a shortcut to discard a random card in the Quick Commands.</li>
            <li class="list-group-item">Clicking on a card in hand will display a shortcut to both discard, and discard
                and redraw.
            </li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Burning cards</h4>
    There is a <span class="badge bg-secondary fs-6 px-2 py-1">burn</span> command that will move the card from the
    ready/torpor region into it's owner's ash-heap.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">Clicking a card in play will have a shortcut to burn this card.</li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Moving cards</h4>
    For all other cases, the <span class="badge bg-secondary fs-6 px-2 py-1">move</span> command will allow you to move
    cards in play from one position ( PLAYER / REGION / CARD ), to another position.
    This command is the most commonly used, and will cover most use-cases inside a game of VTES.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">There is currently no way to move cards via clicking, yet.</li>
            <li class="list-group-item">Valid target regions for moving include "ready", "uncontrolled", "hand",
                "library", "crypt", "ashheap", "torpor", and "rfg". Use "rfg" when a card is removed from the game.
            </li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Shuffling</h4>
    The <span class="badge bg-secondary fs-6 px-2 py-1">shuffle</span> command will shuffle regions, or randomize a
    selection of cards in a region.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <uL class="list-group list-group-flush">
            <li class="list-group-item">There is a shortcut to shuffle library, or crypt in the Quick Commands.</li>
        </uL>
    </div>
    <hr class="my-4"/>
    <h4>Command Reference</h4>

    <%-- Play --%>
    <div id="playCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Play</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">play [SOURCE] [@ discipline/s] [DESTINATION] [draw]</h6>
                <p class="card-text">
                    Use this to play cards from your hand, or another region such as research<br/>
                    This command can have optional information added such as the disciplines to play with, or whether
                    you want to
                    draw to replace.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>SOURCE</th>
                        <td>The default player is always yourself. The default region is hand.</td>
                    </tr>
                    <tr>
                        <th>@ discipline/s</th>
                        <td>Use this keyword combined with 1 or more discipline codes to provide context to other
                            players about the mode you wish to play this card as
                        </td>
                    </tr>
                    <tr>
                        <th>DESTINATION</th>
                        <td>Choose a player/region only to add card to region, or player/region/card to add card to an
                            existing card in region. The default region is the ash-heap.
                        </td>
                    </tr>
                    <tr>
                        <th>draw</th>
                        <td>When added, card is automatically drawn to replace.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>play 2 draw</th>
                        <td>move hand #2 to your ash-heap and redraw. ( Combat cards )</td>
                    </tr>
                    <tr>
                        <th>play 3</th>
                        <td>move hand #3 to your ash-heap without redrawing ( Bum's Rush, Wake with Evening's Freshness
                            )
                        </td>
                    </tr>
                    <tr>
                        <th>play 2 ready draw</th>
                        <td>move hand #2 to your ready region, and replace ( Allies )</td>
                    </tr>
                    <tr>
                        <th>play 1 ready 2 1</th>
                        <td>move hand #1 to card #2 in your ready region ( Retainers, Equipment )</td>
                    </tr>
                    <tr>
                        <th>play 1 Lilith uncontrolled 1</th>
                        <td>move hand #1 to card #1 in Lilith's uncontrolled region ( Brainwash )</td>
                    </tr>
                    <tr>
                        <th>play 1 Lilith ready 2</th>
                        <td>move hand #1 to card # in Lilith's ready region ( Fame )</td>
                    </tr>
                    <tr>
                        <th>play 1 @ ani</th>
                        <td>play hand #1 to your ash-heap, and print <span class="icon ani"></span> in the chat log.
                        </td>
                    </tr>
                    <tr>
                        <th>play 1 @ ani,FOR</th>
                        <td>play hand #1 to your ash-heap, and print <span class="icon ani"></span> <span
                                class="icon FOR"></span> in the chat log.
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Influence --%>
    <div id="influenceCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Influence</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">influence [SOURCE]</h6>
                <p class="card-text">
                    Use this to play a vampire from your uncontrolled region<br/>
                    This command will adjust capacity, and disciplines as required.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>SOURCE</th>
                        <td>The player is always yourself. The region is always uncontrolled.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>influence 2</th>
                        <td>play uncontrolled #2 to your ready region.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Draw --%>
    <div id="drawCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Draw</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">draw [crypt] [amount]</h6>
                <p class="card-text">
                    Use this to draw cards from either the library, or crypt. They will go into the hand, or
                    uncontrolled regions respectfully.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>crypt</th>
                        <td>This modifies the draw command to draw from crypt into uncontrolled region.</td>
                    </tr>
                    <tr>
                        <th>amount</th>
                        <td>The number of cards to draw.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>draw</th>
                        <td>draw 1 card from your library into your hand.</td>
                    </tr>
                    <tr>
                        <th>draw crypt</th>
                        <td>draw 1 card from your crypt into your uncontrolled region.</td>
                    </tr>
                    <tr>
                        <th>draw 3</th>
                        <td>Draw 3 cards into your hand. Useful for Heart of Nizchetus.</td>
                    </tr>
                    <tr>
                        <th>draw crypt 4</th>
                        <td>Draw 4 cards from your crypt into your uncontrolled region.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Discard --%>
    <div id="discardCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Discard</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">discard [CARD | random] [draw]</h6>
                <p class="card-text">
                    Moves a card from your hand into the ashheap. Note that this will not draw a replacement card by
                    default.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>CARD</th>
                        <td>The position of the card in hand</td>
                    </tr>
                    <tr>
                        <th>random</th>
                        <td>Choose a random card in hand</td>
                    </tr>
                    <tr>
                        <th>draw</th>
                        <td>Replace the card immediately.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>discard 3</th>
                        <td>discards card #3 from your hand without replacing.</td>
                    </tr>
                    <tr>
                        <th>discard 5 draw</th>
                        <td>discards card #5 from your hand and replaces it with a new card from your library.</td>
                    </tr>
                    <tr>
                        <th>discard random</th>
                        <td>discard random card from hand without replacing.</td>
                    </tr>
                    <tr>
                        <th>discard random draw</th>
                        <td>discards a random card from hand and replaces it with a new card from your library.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Burn --%>
    <div id="burnCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Burn</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">burn [SOURCE]</h6>
                <p class="card-text">
                    Burns a card, moving it to its owner's ash heap. Cards on the burned card are moved to their owner's
                    ash heaps.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>SOURCE</th>
                        <td>The default player is yourself. The default region is the ready region.</td>
                    </tr>
                    <tr>
                        <th>top</th>
                        <td>Optionally use top instead of specifying position 1 when determining source.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>burn ready 2</th>
                        <td>Burns card #2 in your ready region. Useful for master cards that come into play.</td>
                    </tr>
                    <tr>
                        <th>burn Lilith ready 2</th>
                        <td>Burns card #2 in Lilith's ready region. Useful for destroying a rival's location or
                            equipment.
                        </td>
                    </tr>
                    <tr>
                        <th>burn Lilith library top</th>
                        <td>Burns the top card from Lilith's library. Useful for cards like The Slaughterhouse.</td>
                    </tr>
                    <tr>
                        <th>burn crypt top</th>
                        <td>Burns the top card of your crypt.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Move --%>
    <div id="moveCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Move</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">move [SOURCE] [DESTINATION] [top]</h6>
                <p class="card-text">
                    Use this to move any card around.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>SOURCE</th>
                        <td>The default player is yourself. The default region is the ready region.</td>
                    </tr>
                    <tr>
                        <th>DESTINATION</th>
                        <td>The default player is yourself. The default region is the ready region.</td>
                    </tr>
                    <tr>
                        <th>top</th>
                        <td>You can optionally use the keyword top to move the card to the top of the target
                            region/card.
                        </td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>move 1</th>
                        <td>Moves ready #1 to the last spot in your ready region. Useful for re-ordering your regions.
                        </td>
                    </tr>
                    <tr>
                        <th>move 2 1 ready 1</th>
                        <td>Moves card #1 on ready #2 to ready #1. Use this to move equipment / retainers from one
                            minion to another.
                        </td>
                    </tr>
                    <tr>
                        <th>move torpor 1 ready</th>
                        <td>Moves torpor #1 to the ready region. Use this for rescuing from torpor.</td>
                    </tr>
                    <tr>
                        <th>move Caine torpor 1 Lilith ready</th>
                        <td>Moves torpor #1 from player Caine to player Lilith's ready region. Use this for
                            Graverobbing.
                        </td>
                    </tr>
                    <tr>
                        <th>move hand 1 research</th>
                        <td>Moves hand #1 to the research area. Useful for cards that need to be put face-down like
                            Mokole Blood.
                        </td>
                    </tr>
                    <tr>
                        <th>move ashheap 1 hand</th>
                        <td>Moves ash-heap #1 to your hand. Useful for resolving Ashur Tablets.</td>
                    </tr>
                    <tr>
                        <th>move ashheap 1 library</th>
                        <td>Moves ash-heap #1 to the bottom of your library. Useful for recursion cards like Ashur
                            Tablets.
                        </td>
                    </tr>
                    <tr>
                        <th>move ashheap 1 library top</th>
                        <td>Moves ash-heap #1 to the top of your library. Useful for cards that re-sort your library.
                        </td>
                    </tr>
                    <tr>
                        <th>move library 1 hand</th>
                        <td>Moves the top card from your library to your hand. Useful for Heart of Nizchetus.</td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

    <%-- Shuffle --%>
    <div id="shuffleCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Shuffle</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">shuffle [SOURCE] [amount]</h6>
                <p class="card-text">
                    Shuffle a particular stack of cards.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>SOURCE</th>
                        <td>The default player is yourself. The default region is the library.</td>
                    </tr>
                    <tr>
                        <th>amount</th>
                        <td>The amount of cards to shuffle.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tr>
                        <th>shuffle</th>
                        <td>Shuffle your library.</td>
                    </tr>
                    <tr>
                        <th>shuffle Lilith library</th>
                        <td>Shuffle Lilith's library.</td>
                    </tr>
                    <tr>
                        <th>shuffle crypt</th>
                        <td>Shuffle your crypt.</td>
                    </tr>
                    <tr>
                        <th>shuffle library 5</th>
                        <td>Shuffle the top 5 cards of your library, while preserving the order of the remaining
                            cards.
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>