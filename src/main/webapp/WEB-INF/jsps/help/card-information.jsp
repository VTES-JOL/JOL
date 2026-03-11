<div class="container-fluid tab-pane fade" role="tabpanel" aria-labelledby="help5" tabindex="0" id="panel5">
    <h4>Locking & Unlocking</h4>
    Cards that are used to perform actions are typically <span class="badge bg-secondary fs-6 px-2 py-1">lock</span>-ed
    after the action is successful. At the start of your turn, you would typically
    <span class="badge bg-secondary fs-6 px-2 py-1">unlock</span> all your cards.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <ul class="list-group list-group-flush">
            <li class="list-group-item">Clicking on a card in play will give you the option to lock or unlock it.</li>
            <li class="list-group-item">There is a "Unlock All" shortcut in the Quick Commands to unlock all your cards
                at once.
            </li>
        </ul>
    </div>
    <hr class="my-4"/>
    <h4>Adding information to the card</h4>
    You can add custom labels, change the clan, or adjust the capacity of cards in play to reflect game state changes
    (e.g., from library cards or special abilities).
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <ul class="list-group list-group-flush">
            <li class="list-group-item">Labels are useful for tracking temporary effects or noting specific card states.
            </li>
            <li class="list-group-item">When changing disciplines, use <code>+</code> to add and <code>-</code> to remove.
            </li>
        </ul>
    </div>
    <hr class="my-4"/>
    <h4>Contesting</h4>
    If two or more players have the same unique card in play, they must be contested.
    The <span class="badge bg-secondary fs-6 px-2 py-1">contest</span> command is used to mark a card as contested.
    <hr class="my-4"/>

    <h4>Command Reference</h4>

    <%-- Votes --%>
    <div id="votesCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="mt-2 fs-5">votes [PLAYER] [REGION] [INDEX] [AMOUNT]</h5>
            </div>
            <div class="card-body">
                <p class="card-text">
                    Set or modify the number of votes on a minion.
                </p>
                <div class="card mt-2">
                    <div class="card-header">
                        <h6 class="mt-2 fs-6">Command Options</h6>
                    </div>
                    <table class="table mb-0">
                        <thead>
                        <tr>
                            <th scope="col">Option</th>
                            <th scope="col">Description</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>[AMOUNT]</td>
                            <td>Add or remove votes (e.g. +1, -1), set to a specific number (e.g. 3), or set to "priscus" (or "P"). Use 0 to remove all votes.</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <table class="table table-bordered mt-3">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>votes 1 +1</th>
                        <td>Adds 1 vote to your minion #1 in the ready region.</td>
                    </tr>
                    <tr>
                        <th>votes 2 P</th>
                        <td>Sets the votes on your minion #2 to Priscus.</td>
                    </tr>
                    <tr>
                        <th>votes Caine ready 1 3</th>
                        <td>Sets the votes on Caine's minion #1 to 3.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Sect --%>
    <div id="sectCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="mt-2 fs-5">sect [PLAYER] [REGION] [INDEX] [SECT]</h5>
            </div>
            <div class="card-body">
                <p class="card-text">
                    Change the sect of a card.
                </p>
                <div class="card mt-2">
                    <div class="card-header">
                        <h6 class="mt-2 fs-6">Command Options</h6>
                    </div>
                    <table class="table mb-0">
                        <thead>
                        <tr>
                            <th scope="col">Option</th>
                            <th scope="col">Description</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>[SECT]</td>
                            <td>The new sect for the card (e.g. camarilla, anarch, sabbat, independent). Leave empty to clear.</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <table class="table table-bordered mt-3">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>sect 1 camarilla</th>
                        <td>Changes the sect of your card #1 to Camarilla.</td>
                    </tr>
                    <tr>
                        <th>sect 2</th>
                        <td>Clears the sect from your card #2.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Path --%>
    <div id="pathCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">path [PLAYER] [REGION] [INDEX] [PATH]</h5>
            </div>
            <div class="card-body">
                <p class="card-text">
                    Change the path of a card.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>path 1 caine</th>
                        <td>Changes the path of your card #1 to Path of Caine.</td>
                    </tr>
                    <tr>
                        <th>path 2</th>
                        <td>Clears the path from your card #2.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Flip --%>
    <div id="flipCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">flip</h5>
            </div>
            <div class="card-body">
                <p class="card-text">
                    Perform a coin flip.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>flip</th>
                        <td>The game announces either "Heads" or "Tails".</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Lock --%>
    <div id="lockCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="mt-2 fs-5">lock [REGION] [INDEX]</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">lock [PLAYER] [REGION] [CARD]</h6>
                <p class="card-text">
                    Locks a card.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>PLAYER</th>
                        <td>The default player is always yourself.</td>
                    </tr>
                    <tr>
                        <th>REGION</th>
                        <td>The default region is the ready region.</td>
                    </tr>
                    <tr>
                        <th>CARD</th>
                        <td>The position of the card in the region.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>lock 1</th>
                        <td>Locks your card #1 in the ready region.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Unlock --%>
    <div id="unlockCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="mt-2 fs-5">unlock [REGION] [INDEX]</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">unlock [PLAYER] [REGION] [CARD]</h6>
                <p class="card-text">
                    Unlocks a card, or all your cards if no card is specified.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>PLAYER</th>
                        <td>The default player is always yourself.</td>
                    </tr>
                    <tr>
                        <th>REGION</th>
                        <td>The default region is the ready region.</td>
                    </tr>
                    <tr>
                        <th>CARD</th>
                        <td>The position of the card in the region. If omitted, unlocks ALL cards for the player.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>unlock</th>
                        <td>Unlocks all your cards.</td>
                    </tr>
                    <tr>
                        <th>unlock 2</th>
                        <td>Unlocks your card #2 in the ready region.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Label --%>
    <div id="labelCommand" class="mb-4">
        <div class="card">
            <div class="card-header bg-secondary-subtle">
                <h5 class="mt-2 fs-5">label [REGION] [INDEX] [TEXT]</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">label [PLAYER] [REGION] [CARD] [TEXT]</h6>
                <p class="card-text">
                    Adds a text label to a card.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>label 1 +1 strength</th>
                        <td>Adds the label "+1 strength" to your card #1.</td>
                    </tr>
                    <tr>
                        <th>label 2</th>
                        <td>Clears the label from your card #2.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Clan --%>
    <div id="clanCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Clan</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">clan [PLAYER] [REGION] [CARD] [CLAN]</h6>
                <p class="card-text">
                    Changes the clan of a card.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>clan 1 Tremere</th>
                        <td>Changes the clan of your card #1 to Tremere.</td>
                    </tr>
                    <tr>
                        <th>clan 1</th>
                        <td>Clears the clan from your card #1.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Capacity --%>
    <div id="capacityCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Capacity</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">capacity [PLAYER] [REGION] [CARD] [AMOUNT]</h6>
                <p class="card-text">
                    Adjusts the capacity of a vampire. Requires a <code>+</code> or <code>-</code> prefix.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>capacity 1 +1</th>
                        <td>Increases capacity of your card #1 by 1.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Disciplines --%>
    <div id="disciplinesCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Disciplines</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">disc [PLAYER] [REGION] [CARD] [+|-DISC] [reset]</h6>
                <p class="card-text">
                    Adds or removes disciplines from a card, or resets them to the card's printed values.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>disc 1 +aus</th>
                        <td>Adds basic Auspex to your card #1.</td>
                    </tr>
                    <tr>
                        <th>disc 1 +AUS -dom</th>
                        <td>Adds superior Auspex and removes Dominate from your card #1.</td>
                    </tr>
                    <tr>
                        <th>disc 1 reset</th>
                        <td>Resets disciplines on your card #1 to printed values.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Contest --%>
    <div id="contestCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Contest</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">contest [PLAYER] [REGION] [CARD] [clear]</h6>
                <p class="card-text">
                    Marks a card as contested, or clears the contested state.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>contest 1</th>
                        <td>Mark your card #1 as contested.</td>
                    </tr>
                    <tr>
                        <th>contest 1 clear</th>
                        <td>Clear contested state from your card #1.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>