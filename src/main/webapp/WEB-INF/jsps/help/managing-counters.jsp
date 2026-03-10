<div class="container-fluid tab-pane fade bg-secondary-subtle" role="tabpanel" aria-labelledby="help4" tabindex="0"
     id="panel4">
    <h4>Managing Pool</h4>
    The <span class="badge bg-secondary fs-6 px-2 py-1">pool</span> command is used to manipulate player pool
    values.<br/>
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <ul class="list-group list-group-flush">
            <li class="list-group-item">There are a few preset options for common pool costs / gains available in the
                Quick Commands
            </li>
            <li class="list-group-item">Amounts for pool and blood must always be prefixed with a <code>+</code> or
                <code>-</code>.
            </li>
        </ul>
    </div>
    <hr class="my-4"/>
    <h4>Managing Blood / Counters</h4>
    Currently, there is only one counter type in JOL. This represents the concept of blood / life / counters. <br/>
    <ul>
        <li>If the card is a vampire, then the counter color will be red.</li>
        <li>If the card is imbued, ally, or retainer, then the color will be green.</li>
        <li>All other cards will show as a generic grey counter.</li>
    </ul>
    The <span class="badge bg-secondary fs-6 px-2 py-1">blood</span> command is used to manipulate counters on cards.
    <br/>
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <ul class="list-group list-group-flush">
            <li class="list-group-item">Clicking on a card on the screen will give options to modify the blood / counter
                values.
            </li>
        </ul>
    </div>
    <hr class="my-4"/>
    <h4>Transfers</h4>
    The <span class="badge bg-secondary fs-6 px-2 py-1">transfer</span> command is a shortcut for both manipulating pool
    and blood, to assist during the influence phase.<br/>
    There is no concept of maximum transfers enforced so the player will have to manage the number of transfers
    available.
    Usage of this command will allow you to transfer pool to blood when using positive values, or transfer blood to pool
    if negative values are used.
    <div class="card mt-2">
        <div class="card-header">
            <h5 class="mt-2 fs-6">Tips</h5>
        </div>
        <ul class="list-group list-group-flush">
            <li class="list-group-item">There are shortcut arrows to transfer to and from pool to the card when you
                click on the card in play.
            </li>
        </ul>
    </div>
    <hr class="my-4"/>
    <h4>Command Reference</h4>

    <%-- Pool --%>
    <div id="poolCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Pool</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">pool [PLAYER] [AMOUNT]</h6>
                <p class="card-text">
                    Use this to add or remove pool from a player.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>PLAYER</th>
                        <td>The default player is always yourself.</td>
                    </tr>
                    <tr>
                        <th>AMOUNT</th>
                        <td>The amount of pool to add (+) or remove (-).</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>pool +2</th>
                        <td>Add 2 pool to your own pool.</td>
                    </tr>
                    <tr>
                        <th>pool -1</th>
                        <td>Remove 1 pool from your own pool.</td>
                    </tr>
                    <tr>
                        <th>pool Lilith -2</th>
                        <td>Remove 2 pool from Lilith's pool.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Blood --%>
    <div id="bloodCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Blood / Counters</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">blood [PLAYER] [REGION] [CARD] [AMOUNT]</h6>
                <p class="card-text">
                    Use this to add or remove counters from a card.
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
                    <tr>
                        <th>AMOUNT</th>
                        <td>The amount of blood/counters to add (+) or remove (-).</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>blood 1 +2</th>
                        <td>Add 2 blood to your card #1 in the ready region.</td>
                    </tr>
                    <tr>
                        <th>blood 2 -1</th>
                        <td>Remove 1 blood from your card #2 in the ready region.</td>
                    </tr>
                    <tr>
                        <th>blood Lilith ready 1 -2</th>
                        <td>Remove 2 blood from Lilith's card #1 in her ready region.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <%-- Transfer --%>
    <div id="transferCommand" class="mb-4">
        <div class="card">
            <div class="card-header">
                <h5 class="mt-2">Transfer</h5>
            </div>
            <div class="card-body">
                <h6 class="card-title fs-5">transfer [CARD] [AMOUNT]</h6>
                <p class="card-text">
                    Transfer pool to or from a card in your uncontrolled region.
                </p>
                <table class="table table-bordered">
                    <caption class="caption-top">Command Options</caption>
                    <tbody>
                    <tr>
                        <th>CARD</th>
                        <td>The position of the card in your uncontrolled region.</td>
                    </tr>
                    <tr>
                        <th>AMOUNT</th>
                        <td>Positive values (+) transfer pool to blood. Negative values (-) transfer blood to pool.</td>
                    </tr>
                    </tbody>
                </table>
                <table class="table table-bordered">
                    <caption class="caption-top">Examples</caption>
                    <tbody>
                    <tr>
                        <th>transfer 1 +1</th>
                        <td>Transfer 1 pool to card #1 in your uncontrolled region.</td>
                    </tr>
                    <tr>
                        <th>transfer 2 -2</th>
                        <td>Transfer 2 blood from card #2 in your uncontrolled region back to your pool.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>