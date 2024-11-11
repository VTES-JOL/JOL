<div class="mt-2">
  <nav class="nav nav-tabs" id="helpTab" role="tablist">
    <button class="nav-link active" data-bs-toggle="tab" type="button" role="tab" aria-selected="true" id="countersTab"
            data-bs-target="#countersPanel" aria-controls="countersPanel">Managing counters
    </button>
    <button class="nav-link" data-bs-toggle="tab" type="button" role="tab" aria-selected="false" id="moveTab"
            data-bs-target="#movePanel" aria-controls="movePanel">Moving cards
    </button>
    <button class="nav-link" data-bs-toggle="tab" type="button" role="tab" aria-selected="false" id="cardTab"
            data-bs-target="#cardPanel" aria-controls="cardPanel">Managing Card Information
    </button>
    <button class="nav-link" data-bs-toggle="tab" type="button" role="tab" aria-selected="false" id="gameTab"
            data-bs-target="#gamePanel" aria-controls="gamePanel">Managing Game Information
    </button>
  </nav>
  <div class="tab-content p-2 text-bg-light shadow" id="helpContent">
    <div class="tab-pane fade show active" role="tabpanel" aria-labelledby="countersTab" tabindex="0" id="countersPanel">
      <h5>Blood</h5>
      <p>
        Manipulates counters on various cards. Generally used for moving blood on or off of vampires, or
        manipulating pool
        totals. Can be used for cards such as Dreams of the Sphinx to indicate the number of counters on the
        card. The default PLAYER is yourself. The default REGION is ready region
      </p>
      <h5>Syntax</h5>
      <p>
        <code>blood [PLAYER] [REGION CARD] [+|-]AMOUNT</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>blood ready 1 +3</td>
          <td> adds 3 blood to your card #1 in your ready region (Dreams of the Sphinx)</td>
        </tr>
        <tr>
          <td> blood ready 1 1 +2</td>
          <td> adds 2 blood to the first card attached to your card #1 in your ready region (adding life
            to a Raven Spy)
          </td>
        </tr>
        <tr>
          <td> blood Adam ashheap 2 +7</td>
          <td> adds 7 blood to Adam's card #2 in his ashheap region</td>
        </tr>
      </table>
      <hr/>
      <h5>Pool</h5>
      <p>
        Manipulates player pool values.
        The default PLAYER is yourself.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>pool [PLAYER] [+|-]AMOUNT</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td> pool +2</td>
          <td> adds 2 to your pool</td>
        </tr>
        <tr>
          <td> pool -2</td>
          <td> deducts 2 from your pool</td>
        </tr>
        <tr>
          <td class='help-command'>pool Adam +2</td>
          <td> adds 2 to Adam's pool</td>
        </tr>
      </table>
      <h5>Transfer</h5>
      <p>
        Transfers your pool to vampires in your uncontrolled/inactive region. The default PLAYER is
        yourself. To reveal
        a vampire, you must use the PLAY command.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>transfer [REGION] VAMP [+|-]AMOUNT</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>transfer 1 +4</td>
          <td>move 4 pool onto uncontrolled vampire #1</td>
        </tr>
        <tr>
          <td>transfer re 3 -1</td>
          <td>remove 1 blood from vampire #3 in ready region, add 1 to pool.</td>
        </tr>
      </table>
    </div>
    <div class="tab-pane fade" role="tabpanel" aria-labelledby="moveTab" tabindex="0" id="movePanel">
      <h5>Play</h5>
      <p>
        Manipulates cards in various ways. You can use this command to play vampires from your uncontrolled
        region to your ready region. You may also use this command to play cards from your hand.
      </p>
      <h5>Syntax</h5>
      <div>
        <code>play [vamp] CARD [@ DISCIPLINES] [PLAYER] [REGION] [CARD] [draw]</code>
        <ul>
          <li>The default PLAYER is yourself.</li>
          <li>The default REGION is your ashheap if you are playing a library card or your ready region if you
            are playing a vampire.
          </li>
          <li>By default you will not draw to replace a card played.</li>
          <li>DISCIPLINES are printed in the log to let others know how you are using the card.</li>
        </ul>
      </div>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>play vamp 1</td>
          <td>moves your vamp #1 from inactive to ready (example: revealing a vampire)</td>
        </tr>
        <tr>
          <td>play 2 draw</td>
          <td>moves your hand card #2 to your ashheap and redrawing (example: standard play-and-draw)</td>
        </tr>
        <tr>
          <td>play 3</td>
          <td>moves your hand card #3 to your ashheap without redrawing (example: Bum's Rush, Events)</td>
        </tr>
        <tr>
          <td>play 1 ready 2 draw</td>
          <td>moves your hand card #1 to be attached to your ready region card #2 and then draws (example:
            Owl
            Companion, Perfectionist)
          </td>
        </tr>
        <tr>
          <td>play 1 ready 2 1</td>
          <td>moves your hand card #1 to be attached to the first card already attached to ready region
            card #2
          </td>
        </tr>
        <tr>
          <td>play 1 Jeff</td>
          <td>moves your hand card #1 to Jeff's ashheap</td>
        </tr>
        <tr>
          <td>play 1 Jeff ready</td>
          <td>moves your hand card #1 to Jeff's ready region</td>
        </tr>
        <tr>
          <td>play 7 Jeff ready 2</td>
          <td>moves your hand card #7 to Jeff's ready region card #2 (example: Charnas the Imp, Fame)</td>
        </tr>
        <tr>
          <td>play 1 torpor</td>
          <td>moves your hand card #1 to your torpor region</td>
        </tr>
        <tr>
          <td>play 1 @ ani</td>
          <td>play card #1 at basic animalism</td>
        </tr>
        <tr>
          <td>play 1 @ ani,FOR</td>
          <td>play card #1 at basic animalism and superior fortitude</td>
        </tr>
      </table>
      <h5>Move</h5>
      <p>
        Moves cards from one region to another. Note that cards can be placed on top of other cards. The
        default
        SRCPLAYER is yourself. The default SRCREGION is your ready region. The default DESTREGION is your
        opponent's
        ready region. DESTPLAYER and DESTCARD do not have defaults obviously.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>move [SRCPLAYER] [SRCREGION] CARD [DESTPLAYER] [DESTREGION] [DESTCARD]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>move 1</td>
          <td>moves card #1 in your ready region to the last spot in the ready region (assigns card #1 a
            different #)
          </td>
        </tr>
        <tr>
          <td>move ready 2 1 ashheap</td>
          <td>moves card #1 which is attached to card #2 in the ready region from card #2 to your
            ashheap
          </td>
        </tr>
        <tr>
          <td>move re 2 1 ash</td>
          <td>same as above but with shortforms for regions</td>
        </tr>
        <tr>
          <td>move Scott torpor 1 Ghost ready</td>
          <td>moves vampire #1 from Scott's torpor region to Ghost's ready region (Graverobbing at DOM)
          </td>
        </tr>
        <tr>
          <td> move hand 1 ready 2</td>
          <td>moves card #1 in your hand to card #2 in your ready region</td>
        </tr>
        <tr>
          <td>move ashheap 1 hand</td>
          <td>moves card #1 in your ashheap into your hand (useful for mistakes)</td>
        </tr>
        <tr>
          <td>move ashheap 1 library</td>
          <td>moves card #1 in your ashheap onto the bottom of your library</td>
        </tr>
        <tr>
          <td>move ashheap 1 library top</td>
          <td>moves card #1 in your ashheap onto the top of your library</td>
        </tr>
      </table>
      <h5>Draw</h5>
      <p>
        Used to draw cards from your library or from your crypt. By default, the command will draw one card
        from your
        library.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>draw [crypt] [NUM]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>draw</td>
          <td>draw 1 card from your library</td>
        </tr>
        <tr>
          <td>draw crypt</td>
          <td>draw 1 card from your crypt</td>
        </tr>
        <tr>
          <td>draw 5</td>
          <td>draws 5 cards from your library</td>
        </tr>
        <tr>
          <td>draw crypt 4</td>
          <td>draws 4 vamps from your crypt</td>
        </tr>
      </table>
      <h5>Discard</h5>
      <p>
        Moves a card from your hand into the ashheap. Note that this will not draw a replacement card by
        default.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>discard CARD|random [draw]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>discard 3</td>
          <td>discards card #3 from your hand without replacing</td>
        </tr>
        <tr>
          <td>discard 5 draw</td>
          <td>discards card #5 from your hand and replaces it with a new card from your library</td>
        </tr>
        <tr>
          <td class='help-command'>discard random</td>
          <td>discard random card from hand without replacing</td>
        </tr>
        <tr>
          <td class='help-command'>discard random draw</td>
          <td>discards a random card from hand and replaces it with a new card from your library</td>
        </tr>
      </table>
      <h5>Burn</h5>
      <p>
        Burns a card, moving it to its owner's ash heap. Cards on the burned card are moved to their owner's ash
        heaps.
        The default SRCPLAYER is yourself. The default SRCREGION is your ready region.

        This command does not work in games created before January 2021.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>burn [SRCPLAYER] [SRCREGION] CARD|top </code>
      </p>
      <h5>Examples</h5>
      <table class="table table-bordered table-sm table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>burn re 1</td>
          <td>burns card #1 in your ready region</td>
        </tr>
        <tr>
          <td>burn Jane re 2</td>
          <td>burns card #2 in Jane's ready region</td>
        </tr>
        <tr>
          <td>burn Jane lib top</td>
          <td>burn the top card of Jane's library</td>
        </tr>
        <tr>
          <td>burn crypt top</td>
          <td>burn the top card of your crypt</td>
        </tr>
      </table>
      <h5>Shuffle</h5>
      <p>
        Shuffles a particular stack of cards.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>shuffle [PLAYER] [REGION] [num]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>shuffle</td>
          <td>shuffles your library and crypt</td>
        </tr>
        <tr>
          <td>shuffle Paolo library</td>
          <td>shuffle Paolo's library</td>
        </tr>
        <tr>
          <td>shuffle crypt</td>
          <td>shuffles your crypt, but not your library</td>
        </tr>
        <tr>
          <td>shuffle library 5</td>
          <td>shuffles the top 5 cards of your library</td>
        </tr>
      </table>
    </div>
    <div class="tab-pane fade" role="tabpanel" aria-labelledby="cardTab" tabindex="0" id="cardPanel">
      <h5>Lock</h5>
      <p>
        Locks a particular card. You may target any card on the table. Note that locking other people's
        cards should
        never be necessary. The default PLAYER is yourself. The default REGION is your ready region.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>lock [PLAYER] [REGION] CARD</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>lock ready 1</td>
          <td>lock card #1 in your ready region</td>
        </tr>
        <tr>
          <td>lock George ready 3</td>
          <td>lock card #3 in George's ready region (Jar the Soul, Misdirection)</td>
        </tr>
      </table>
      <h5>Unlock</h5>
      <p>
        Unlocks a particular card. You may target any card on the table. Note that unlocking other people's
        cards should
        never be necessary. The default PLAYER is yourself. The default REGION includes all of your cards.
        In other
        words, you can easily start your turn by using a UNTAP command to unlock all of your cards.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>unlock [PLAYER] [REGION] [CARD]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>unlock</td>
          <td>unlock all of your cards (start of turn)</td>
        </tr>
        <tr>
          <td>unlock ready 1</td>
          <td>unlock card #1 in your ready region (Speak with Spirits, 2nd Tradition)</td>
        </tr>
        <tr>
          <td>unlock George ready 3</td>
          <td>unlock card #3 in George's ready region (Babble, Angela Preston)</td>
        </tr>
      </table>
      <h5>Label</h5>
      <p>
        Used to tag a card with some extra text.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>label [PLAYER] [REGION] CARD [text here]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>label 5 corruption counters - 1</td>
          <td>Put a note attached to card #5 in your ready region describing corruption counters.</td>
        </tr>
        <tr>
          <td>label 5</td>
          <td>Remove the note attached to card #5</td>
        </tr>
        <tr>
          <td>label Jeff inactive 1 Is Tremere</td>
          <td>Label Jeff's first inactive with a label "Is Tremere", perhaps because it was a target of
            Arcane Library
          </td>
        </tr>
      </table>
      <h5>Capacity</h5>
      <p>
        Changes the capacity of a vampire or other minion with blood.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>capacity [PLAYER] [REGION] CARD [+|-]amount</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>capacity 2 +1</td>
          <td>Adds 1 capacity to ready minion #1, for example if a master skill card is added.</td>
        </tr>
        <tr>
          <td>capacity Jeff torpor 1 -1</td>
          <td>Reduces the capacity of Jeff's first torporized minion by one.</td>
        </tr>
      </table>
      <h5>Disciplines</h5>
      <p>
        The default PLAYER is yourself. The default region is the ready region.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>disc [PLAYER] [REGION CARD] [+|-][DISCIPLINE CODE]</code>
        <code>disc [PLAYER] [REGION CARD] reset</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td> disc ready 1 +pot</td>
          <td>Adds <span class="discipline pot"></span> to a vampire or adds <span
                  class="discipline POT"></span> to a vampire with <span class="discipline pot"></span></td>
        </tr>
        <tr>
          <td> disc ready 1 +pot +DOM</td>
          <td>Adds <span class="discipline pot"></span> and <span class="discipline DOM"></span> to a vampire
          </td>
        </tr>
        <tr>
          <td> disc ready 1 reset</td>
          <td>Resets disciplines back to card default</td>
        </tr>
      </table>
      <h5>Votes</h5>
      <p>
        The default PLAYER is yourself. The default region is the ready region.
        Note: This is a label only, no mechanics use votes currently.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>votes [PLAYER] [REGION CARD] [+|-]AMOUNT</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td> votes ready 1 +3</td>
          <td>labels ready region card #1 with 3 votes</td>
        </tr>
        <tr>
          <td> votes ready 1 1 0</td>
          <td>removes vote label from ready region card #1.1
          </td>
        </tr>
        <tr>
          <td> votes Adam 2 +4</td>
          <td> adds 4 votes to Adam's ready region card #2</td>
        </tr>
      </table>
      <h5>Contest</h5>
      <p>
        The default PLAYER is yourself. The default region is the ready region.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>contest [PLAYER] [CARD]</code>
        <code>contest [PLAYER] [CARD] clear</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td> contest ready 1</td>
          <td>Marks card 1 in your ready region as contested</td>
        </tr>
        <tr>
          <td> contest ShanDow ready 2</td>
          <td>Marks card 2 in ShanDow's ready region as contested.</td>
        </tr>
        <tr>
          <td> contest ready 1 clear</td>
          <td>Clears the contested flag on card 1 in your ready region.</td>
        </tr>
      </table>
    </div>
    <div class="tab-pane fade" role="tabpanel" aria-labelledby="gameTab" tabindex="0" id="gamePanel">
      <h5>Edge</h5>
      <p>
        Assign or burn the edge.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>edge [PLAYER] [burn]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>edge</td>
          <td>gives you the Edge</td>
        </tr>
        <tr>
          <td>edge Adam</td>
          <td>gives the edge to Adam</td>
        </tr>
        <tr>
          <td>edge burn</td>
          <td>burns the Edge, making it uncontrolled (eg votes)</td>
        </tr>
      </table>
      <h5>Choose</h5>
      <p>
        Choose a secret for yourself. Reveal all players' secrets simultaneously with the reveal command. Useful for Malkavian Prank.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>choose SECRET</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>choose 2</td>
          <td>Your choice (2) is saved</td>
        </tr>
      </table>
      <h5>Reveal</h5>
      <p>
        Reveal all secrets previously chosen with the choose command. Useful for Malkavian Prank.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>reveal</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>reveal</td>
          <td>All secret choices are revealed</td>
        </tr>
      </table>
      <h5>Random</h5>
      <p>
        Roll a NUMBER-sided die.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>random [NUMBER]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>random 6</td>
          <td>roll a 6 sided die</td>
        </tr>
      </table>
      <h5>Show</h5>
      <p>
        Reveal hidden cards to somebody. Used for searching libraries/crypts, or for peeking at the top of
        libraries/crypts. Often combined with SHUFFLE. The text of the shown cards show up in the private
        scratchpad of the intended methuselah.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>show [REGION] amount [[PLAYER]|all]</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td> show 1</td>
          <td>peek at the top card of your library</td>
        </tr>
        <tr>
          <td>show 1 Jeff</td>
          <td>show Jeff the top card of your library</td>
        </tr>
        <tr>
          <td>show crypt 12</td>
          <td>look at your whole crypt (assuming crypt is <= 12 cards)</td>
        </tr>
        <tr>
          <td>show hand 1 Jeff</td>
          <td>show Jeff the first card in your hand. shuffle your hand first if you want to show a random
            card
          </td>
        </tr>
        <tr>
          <td>show library 1 all</td>
          <td>show everyone in the game the top card of your library.</td>
        </tr>
      </table>
      <h5>Flip</h5>
      <p>
        Flip a coin. Heads or tails.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>flip</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>flip</td>
          <td>Flips a coin. Results appear in game chat.</td>
        </tr>
      </table>
      <h5>Victory Points</h5>
      <p>
        There is no default player.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>vp PLAYER [+|-]AMOUNT</code><br/>
        <code>vp PLAYER withdraw</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>vp ShanDow 1</td>
          <td>Gives ShanDow 1 VP</td>
        </tr>
        <tr>
          <td>vp ShanDow -1</td>
          <td>Removes 1 VP from ShanDow</td>
        </tr>
        <tr>
          <td>vp ShanDow withdraw</td>
          <td>Gives player 0.5 VP, and sets their pool to 0</td>
        </tr>
      </table>
      <h5>Call Game</h5>
      <p>
        Requests a game hit the virtual time limit. Needs two players to complete.<br/>
        Sets all remaining players pool to 0, and awards 0.5VP to each surviving player.
      </p>
      <h5>Syntax</h5>
      <p>
        <code>timeout</code>
      </p>
      <h5>Examples</h5>
      <table class="table table-sm table-bordered table-hover">
        <tr>
          <th>Command</th>
          <th>Result</th>
        </tr>
        <tr>
          <td>timeout</td>
          <td>First request will simply log a request.<br/>Another timeout request by another player will
            complete the timeout
          </td>
        </tr>
        <tr>
          <td>timeout cancel</td>
          <td>If a player adds the cancel command it will cancel a pending timeout request.</td>
        </tr>
      </table>
    </div>
  </div>
</div>