<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
        <title>V:TES Online Command Set</title>
    </head>
    <body>
        <ul class="help">
            <li><a href="#help_blood">Blood</a>
            <li><a href="#help_capacity">Capacity</a>
            <li><a href="#help_discard">Discard</a>
            <li><a href="#help_draw">Draw</a>
            <li><a href="#help_edge">Edge</a>
            <li><a href="#help_label">Label</a>
            <li><a href="#help_move">Move</a>
            <li><a href="#help_order">Order</a>
            <li><a href="#help_play">Play</a>
            <li><a href="#help_random">Random</a>
            <li><a href="#help_show">Show</a>
            <li><a href="#help_shuffle">Shuffle</a>
            <li><a href="#help_transfer">Transfer</a>
            <li><a href="#help_lock">Lock</a>
            <li><a href="#help_unlock">Unlock</a>
        </ul>
        <div id="help-body">
            <div id="help_blood">
                <h2>Blood</h2>
                <p>
                    Manipulates counters on various cards. Generally used for moving blood on or off of vampires, or
                    manipulating pool
                    totals. Can be used for cards such as Dreams of the Sphinx to indicate the number of counters on the
                    card. The
                    default PLAYER is yourself. There is no default region.
                </p>
                <h4>Syntax</h4>
                <p>
                    <code>blood [PLAYER] [REGION CARD] [+|-]AMOUNT</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command"> blood +2</td>
                        <td> adds 2 to your pool</td>
                    </tr>
                    <tr>
                        <td class="help-command"> blood -2</td>
                        <td> deducts 2 from your pool</td>
                    </tr>
                    <tr>
                        <td class="help-command"> blood ready 1 +3</td>
                        <td> adds 3 blood to your card #1 in your ready region (Dreams of the Sphinx)</td>
                    </tr>
                    <tr>
                        <td class="help-command"> blood ready 1 1 +2</td>
                        <td> adds 2 blood to the first card attached to your card #1 in your ready region (adding life
                            to a Raven Spy)
                        </td>
                    </tr>
                    <tr>
                        <td class="help-command"> blood Adam ashheap 2 +7</td>
                        <td> adds 7 blood to Adam's card #2 in his ashheap region</td>
                    </tr>
                </table>
            </div>
            <div id="help_capacity">
                <h2>Capacity</h2>
                <p>
                    Changes the capacity of a vampire or other minion with blood.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>capacity [PLAYER] [REGION] CARD [+|-]amount</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">capacity 2 +1</td>
                        <td>Adds 1 capacity to ready minion #1, for example if a master skill card is added.</td>
                    </tr>
                    <tr>
                        <td class="help-command">capacity Jeff torpor 1 -1</td>
                        <td>Reduces the capacity of Jeff's first torporized minion by one.</td>
                    </tr>
                </table>
            </div>
            <div id="help_discard">
                <h2>Discard</h2>
                <p>
                    Moves a card from your hand into the ashheap. Note that this will not draw a replacement card by
                    default.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>discard CARD [draw]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">discard 3</td>
                        <td>discards card #3 from your hand without replacing</td>
                    </tr>
                    <tr>
                        <td class="help-command">discard 5 draw</td>
                        <td>discards card #5 from your hand and replaces it with a new card from your library</td>
                    </tr>
                </table>
            </div>
            <div id="help_draw">
                <h2>Draw</h2>
                <p>
                    Used to draw cards from your library or from your crypt. By default, the command will draw one card from your
                    library.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>draw [vamp] [NUM]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">draw</td>
                        <td>draw 1 card from your library</td>
                    </tr>
                    <tr>
                        <td class="help-command">draw vamp</td>
                        <td>draw 1 card from your crypt</td>
                    </tr>
                    <tr>
                        <td class="help-command">draw 5</td>
                        <td>draws 5 cards from your library</td>
                    </tr>
                    <tr>
                        <td class="help-command">draw vamp 4</td>
                        <td>draws 4 vamps from your crypt</td>
                    </tr>
                </table>
            </div>
            <div id="help_edge">
                <h2>Edge</h2>
                <p>
                    Assign or burn the edge.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>edge [burn]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">edge</td>
                        <td>gives you the Edge</td>
                    </tr>
                    <tr>
                        <td class="help-command">edge burn</td>
                        <td>burns the Edge, making it uncontrolled (eg votes)</td>
                    </tr>
                </table>
            </div>
            <div id="help_label">
                <h2>Label</h2>
                <p>
                    Used to tag a card with some extra text.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>label [PLAYER] [REGION] CARD [text here]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">label 5 corruption counters - 1</td>
                        <td>Put a note attached to card #5 in your ready region describing corruption counters.</td>
                    </tr>
                    <tr>
                        <td class="help-command">label 5</td>
                        <td>Remove the note attached to card #5</td>
                    </tr>
                    <tr>
                        <td class="help-command">label Jeff inactive 1 Is Tremere</td>
                        <td>Label Jeff's first inactive with a label "Is Tremere", perhaps because it was a target of Arcane Library</td>
                    </tr>
                </table>
            </div>
            <div id="help_move">
                <h2>Move</h2>
                <p>
                    Moves cards from one region to another. Note that cards can be placed on top of other cards. The default
                    SRCPLAYER is yourself. The default SRCREGION is your ready region. The default DESTREGION is your opponent's
                    ready region. DESTPLAYER and DESTCARD do not have defaults obviously.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>move [SRCPLAYER] [SRCREGION] CARD [DESTPLAYER] [DESTREGION] [DESTCARD]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">move 1</td>
                        <td>moves card #1 in your ready region to the last spot in the ready region (assigns card #1 a different #)</td>
                    </tr>
                    <tr>
                        <td class="help-command">move ready 2 1 ashheap</td>
                        <td>moves card #1 which is attached to card #2 in the ready region from card #2 to your ashheap</td>
                    </tr>
                    <tr>
                        <td class="help-command">move re 2 1 ash</td>
                        <td>same as above but with shortforms for regions</td>
                    </tr>
                    <tr>
                        <td class="help-command">move Scott torpor 1 Ghost ready</td>
                        <td>moves vampire #1 from Scott's torpor region to Ghost's ready region (Graverobbing at DOM)</td>
                    </tr>
                    <tr>
                        <td class="help-command"> move hand 1 ready 2</td>
                        <td>moves card #1 in your hand to card #2 in your ready region</td>
                    </tr>
                    <tr>
                        <td class="help-command">move ashheap 1 hand</td>
                        <td>moves card #1 in your ashheap into your hand (useful for mistakes)</td>
                    </tr>
                </table>
            </div>
            <div id="help_order">
                <h2>Order</h2>
                <p>
                    Re-seat the players around the table. Useful with the restructuring cards. Also used with Reversal of Fortune,
                    since the server always advances player turns in order.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>order index1 index2 index3 index4 index5</code>
                </p>
                <h4>Examples</h4>
                <p>Assume the current table seating is "Al Bo Cy Di Ed".</p>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">order 5 4 3 2 1</td>
                        <td>Reverses play order, now "Ed Di Cy Bo Al".</td>
                    </tr>
                    <tr>
                        <td class="help-command">order 1 3 2 4 5</td>
                        <td>Switch seats. New order is "Al Cy Bo Di Ed".</td>
                    </tr>
                </table>
            </div>
            <div id="help_play">
                <h2>Play</h2>
                <p>
                    Manipulates cards in various ways. You can use this command to play vampires from your uncontrolled region to
                    your ready region. You may also use this command to play cards from your hand. The default PLAYER is yourself.
                    The default REGION is your ashheap if you are playing a library card or your ready region if you are playing a
                    vampire. By default you will not draw to replace a card played.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>play [vamp] CARD [PLAYER] [REGION] [CARD] [draw]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">play vamp 1</td>
                        <td>moves your vamp #1 from inactive to ready (example: revealing a vampire)</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 2 draw</td>
                        <td>moves your hand card #2 to your ashheap and redrawing (example: standard play-and-draw)</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 3</td>
                        <td>moves your hand card #3 to your ashheap without redrawing (example: Bum's Rush, Events)</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 1 ready 2 draw</td>
                        <td>moves your hand card #1 to be attached to your ready region card #2 and then draws (example: Owl
                            Companion, Perfectionist)</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 1 ready 2 1</td>
                        <td>moves your hand card #1 to be attached to the first card already attached to ready region card #2</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 1 Jeff</td>
                        <td>moves your hand card #1 to Jeff's ashheap</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 1 Jeff ready</td>
                        <td>moves your hand card #1 to Jeff's ready region</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 7 Jeff ready 2</td>
                        <td>moves your hand card #7 to Jeff's ready region card #2 (example: Charnas the Imp, Fame)</td>
                    </tr>
                    <tr>
                        <td class="help-command">play 1 torpor</td>
                        <td>moves your hand card #1 to your torpor region</td>
                    </tr>
                </table>
            </div>
            <div id="help_random">
                <h2>Random</h2>
                <p>
                    Roll a NUMBER-sided die.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>random [NUMBER]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">random 6</td>
                        <td>roll a 6 sided dice</td>
                    </tr>
                </table>
            </div>
            <div id="help_show">
                <h2>Show</h2>
                <p>
                    Reveal hidden cards to somebody. Used for searching libraries/crypts, or for peeking at the top of
                    libraries/crypts. Often combined with SHUFFLE. The text of the shown cards show up in the private
                    scratchpad of the intended methuselah.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>show [REGION] amount [[PLAYER]|all]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command"> show 1</td>
                        <td>peek at the top card of your library</td>
                    </tr>
                    <tr>
                        <td class="help-command">show 1 Jeff</td>
                        <td>show Jeff the top card of your library</td>
                    </tr>
                    <tr>
                        <td class="help-command">show crypt 12</td>
                        <td>look at your whole crypt (assuming crypt is <= 12 cards)</td>
                    </tr>
                    <tr>
                        <td class="help-command">show hand 1 Jeff</td>
                        <td>show Jeff the first card in your hand. shuffle your hand first if you want to show a random card</td>
                    </tr>
                    <tr>
                        <td class="help-command">show library 1 all</td>
                        <td>show everyone in the game the top card of your library.</td>
                    </tr>
                </table>
            </div>
            <div id="help_shuffle">
                <h2>Shuffle</h2>
                <p>
                    Shuffles a particular stack of cards.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>shuffle [PLAYER] [REGION] [num]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">shuffle</td>
                        <td>shuffles your library and crypt</td>
                    </tr>
                    <tr>
                        <td class="help-command">shuffle Paolo library</td>
                        <td>shuffle Paolo's library</td>
                    </tr>
                    <tr>
                        <td class="help-command">shuffle crypt</td>
                        <td>shuffles your crypt, but not your library</td>
                    </tr>
                    <tr>
                        <td class="help-command">shuffle library 5</td>
                        <td>shuffles the top 5 cards of your library</td>
                    </tr>
                </table>
            </div>
            <div id="help_transfer">
                <h2>Transfer</h2>
                <p>
                    Transfers your pool to vampires in your uncontrolled/inactive region. The default PLAYER is yourself. To reveal
                    a vampire, you must use the PLAY command.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>transfer [REGION] VAMP [+|-]AMOUNT</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">transfer 1 +4</td>
                        <td>move 4 pool onto uncontrolled vampire #1</td>
                    </tr>
                    <tr>
                        <td class="help-command">transfer re 3 -1</td>
                        <td>remove 1 blood from vampire #3 in ready region, add 1 to pool.</td>
                    </tr>
                </table>
            </div>
            <div id="help_lock">
                <h2>Lock</h2>
                <p>
                    Locks a particular card. You may target any card on the table. Note that locking other people's cards should
                    never be necessary. The default PLAYER is yourself. The default REGION is your ready region.
                </p>
                <h4>Syntax</h4>
                <p>
                    <code>lock [PLAYER] [REGION] CARD</code>
                </p>
                <h5>Deprecated: VEKN has changed the keyword tap to lock.  This command will be removed in a future release.</h5>
                <p>
                    <code>tap [PLAYER] [REGION] CARD</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">lock ready 1</td>
                        <td>lock card #1 in your ready region</td>
                    </tr>
                    <tr>
                        <td class="help-command">lock George ready 3</td>
                        <td>lock card #3 in George's ready region (Jar the Soul, Misdirection)</td>
                    </tr>
                </table>
            </div>
            <div id="help_unlock">
                <h2>Unlock</h2>
                <p>
                    Unlocks a particular card. You may target any card on the table. Note that unlocking other people's cards should
                    never be necessary. The default PLAYER is yourself. The default REGION includes all of your cards. In other
                    words, you can easily start your turn by using a UNTAP command to unlock all of your cards.
                </p>
                <h4>Syntax</h4>
                <p>
                <code>unlock [PLAYER] [REGION] [CARD]</code>
                </p>
                <h5>Deprecated: VEKN has changed the keyword untap to unlock.  This command will be removed in a future release.</h5>
                <p>
                    <code>untap [PLAYER] [REGION] [CARD]</code>
                </p>
                <h4>Examples</h4>
                <table class="help-example">
                    <tr>
                        <th class="help-command">Command</th>
                        <th>Result</th>
                    </tr>
                    <tr>
                        <td class="help-command">unlock</td>
                        <td>unlock all of your cards (start of turn)</td>
                    </tr>
                    <tr>
                        <td class="help-command">unlock ready 1</td>
                        <td>unlock card #1 in your ready region (Speak with Spirits, 2nd Tradition)</td>
                    </tr>
                    <tr>
                        <td class="help-command">unlock George ready 3</td>
                        <td>unlock card #3 in George's ready region (Babble, Angela Preston)</td>
                    </tr>
                </table>
            </div>
        </div>
    </body>
</html>
