<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="container-fluid tab-pane fade" role="tabpanel" aria-labelledby="help6" tabindex="0" id="panel6">
    <div id="commandIntro" class="mb-2">
        <h4>Command Basics</h4>
        At the core of the functionality of JOL are commands, every action that modifies the current state of the game
        can be issued by typed commands.<br/>
        While some elements are also supported via the click-to-play interface, understanding the command system will
        help for more complicated card interactions, or to achieve effects that aren't currently found in
        click-to-play<br/>
    </div>
    <hr class="my-4"/>
    <div id="cardTargets" class="mb-2">
        <h4>Targeting cards in JOL</h4>
        JOL uses a system of [PLAYER] [REGION] [INDEX] to work out how to pick a card as either a source, or a
        destination<br/>
        The INDEX represents the location inside the region, starting at 1. When a card is placed on another card, this
        INDEX value can also represent the parent card, and the position inside the card list.<br/><br/>

        <h5>Targeting Example</h5>
        <div class="card w-50 w-md-33 w-lg-25 w-xl-20 mb-2">
            <div class="card-header bg-secondary-subtle">
                <h6 class="d-flex justify-content-between align-items-center mb-0 lh-base">
                <span class="fw-bold">
                    <span>Caine</span>
                </span>
                    <span class="d-inline align-items-center">
                    <span class="badge rounded-pill text-bg-danger">5</span>
                </span>
                </h6>
            </div>
            <div class="card-body px-0 py-0">
                <div class="p-2 bg-success-subtle " type="button" onclick="details('4-READY');"
                     data-bs-toggle="collapse" data-bs-target="#4-READY" aria-expanded="true" aria-controls="4-READY">
                    <span class="fw-bold">Ready</span>
                    <span>( 8 )</span>
                </div>
                <ol class="region list-group list-group-flush list-group-numbered">
                    <li data-card-id="201526"
                        class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1 shadow">
                        <div class="mx-1 me-auto w-100">
                            <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
                                <span>
                                    <a data-card-id="201526" class="card-name text-wrap">Leumeah</a>
                                    <span class="badge rounded-pill text-bg-warning ">3</span>
                                </span>
                                <span class="d-flex gap-1 align-items-center">
                                    <span class="badge text-bg-dark p-1 px-2" style="font-size: 0.6rem;">LOCKED</span>
                                    <span class="badge rounded-pill shadow text-bg-danger">6 / 6</span>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center w-100 pb-1">
                                <span>
                                    <span class="icon pot"></span>
                                    <span class="icon for"></span>
                                    <span class="icon cel"></span>
                                    <span class="icon PRE"></span>
                                </span>
                                <span class="d-flex align-items-center">
                                    <span class="badge bg-light text-black shadow border border-secondary-subtle"></span>
                                    <span>
                                        <span class="clan brujah" title="brujah"></span>
                                    </span>
                                </span>
                            </div>
                            <ol class="list-group list-group-numbered ms-n3">
                            </ol>
                        </div>
                    </li>
                    <li data-card-id="201521"
                        class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1 shadow">
                        <div class="mx-1 me-auto w-100">
                            <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
                                <span>
                                    <a data-card-id="201521" class="card-name text-wrap">Casey Snyder</a>
                                    <span class="badge rounded-pill text-bg-warning ">2</span>
                                </span>
                                <span class="d-flex gap-1 align-items-center">
                                    <span class="badge rounded-pill shadow text-bg-danger">6 / 6</span>
                                </span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center w-100 pb-1">
                                <span>
                                    <span class="icon for"></span>
                                    <span class="icon cel"></span>
                                    <span class="icon ani"></span>
                                    <span class="icon PRO"></span>
                                </span>
                                <span class="d-flex align-items-center">
                                    <span class="badge bg-light text-black shadow border border-secondary-subtle"></span>
                                    <span>
                                        <span class="clan gangrel" title="gangrel"></span>
                                    </span>
                                </span>
                            </div>
                            <ol class="list-group list-group-numbered ms-n3">
                                <li data-card-id="101550"
                                    class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1">
                                    <div class="mx-1 me-auto w-100">
                                        <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
                                            <span>
                                                <a data-card-id="101550" class="card-name text-wrap">Raven Spy</a>
                                            </span>
                                            <span class="d-flex gap-1 align-items-center">
                                                <span class="badge rounded-pill shadow text-bg-success">1</span>
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between align-items-center w-100 pb-1">
                                            <span class="d-flex align-items-center">
                                                <span class="badge bg-light text-black shadow border border-secondary-subtle"></span>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li data-card-id="100568"
                                    class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1">
                                    <div class="mx-1 me-auto w-100">
                                        <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
                                            <span><a data-card-id="100568"
                                                     class="card-name text-wrap">Dog Pack</a></span>
                                            <span class="d-flex gap-1 align-items-center">
                                                <span class="badge rounded-pill shadow text-bg-success">1</span>
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between align-items-center w-100 pb-1">
                                            <span class="d-flex align-items-center">
                                                <span class="badge bg-light text-black shadow border border-secondary-subtle"></span>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                                <li data-card-id="101816"
                                    class="list-group-item d-flex justify-content-between align-items-baseline px-2 pt-2 pb-1   ">
                                    <div class="mx-1 me-auto w-100">
                                        <div class="d-flex justify-content-between align-items-baseline w-100 pb-1">
                                            <span>
                                                <a data-card-id="101816" class="card-name text-wrap">Sniper Rifle</a>
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between align-items-center w-100 pb-1">
                                            <span class="d-flex align-items-center">
                                                <span class="badge bg-light text-black shadow border border-secondary-subtle"></span>
                                            </span>
                                        </div>
                                    </div>
                                </li>
                            </ol>
                        </div>
                    </li>
                </ol>
            </div>
        </div>
        <table class="table">
            <tr class="table-secondary">
                <th>Target</th>
                <th>Card chosen</th>
            </tr>
            <tr>
                <th><b>caine ready 1</b></th>
                <td><a class="card-name" data-card-id="201526">Leumeah</a></td>
            </tr>
            <tr>
                <th><b>caine ready 2 2</b></th>
                <td><a data-card-id="100568" class="card-name text-wrap">Dog Pack</a></td>
            </tr>
        </table>
        Most commands that affect cards will use this format, certain commands will have a default value for either the
        PLAYER or REGION.<br/>
        See the individual command notes for how these defaults work.
    </div>
    <hr class="my-4"/>
    <div id="gameManagement" class="mb-2">
        <h4>Game Management</h4>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">vp [PLAYER] [AMOUNT / withdraw]</h5>
            </div>
        <div class="card-body">
            <p class="card-text">
                Modify victory points or withdraw from the game.
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
                        <td>[PLAYER]</td>
                        <td>The player whose VP should be modified. Defaults to yourself.</td>
                    </tr>
                    <tr>
                        <td>[AMOUNT]</td>
                        <td>Add or remove victory points (e.g. +1, -1).</td>
                    </tr>
                    <tr>
                        <td>withdraw</td>
                        <td>Withdraw from the game and gain 0.5 VP.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <table class="table table-bordered mt-3">
                <caption class="caption-top">Examples</caption>
                <tbody>
                <tr>
                    <th>vp +1</th>
                    <td>Gain 1 victory point.</td>
                </tr>
                <tr>
                    <th>vp Player2 -1</th>
                    <td>Player2 loses 1 victory point.</td>
                </tr>
                <tr>
                    <th>vp withdraw</th>
                    <td>Withdraw from the game.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">timeout</h5>
            </div>
        <div class="card-body">
            <p class="card-text">
                Request a game timeout. If all remaining players request a timeout, the game ends and surviving players are awarded 0.5 VP.
            </p>
            <table class="table table-bordered">
                <caption class="caption-top">Examples</caption>
                <tbody>
                <tr>
                    <th>timeout</th>
                    <td>Request to time out the game.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">order [NUMBER] [NUMBER] ...</h5>
            </div>
        <div class="card-body">
            <p class="card-text">
                Change the seating order of the players. The numbers represent the player's current position (starting from 1).
            </p>
            <table class="table table-bordered">
                <caption class="caption-top">Examples</caption>
                <tbody>
                <tr>
                    <th>order 5 1 4 2 3</th>
                    <td>Sets the new player order using the specified original positions.</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">edge [PLAYER / burn]</h5>
            </div>
        <div class="card-body">
            <p class="card-text">
                Give the edge to a player, or burn it.
            </p>
            <table class="table table-bordered">
                <caption class="caption-top">Examples</caption>
                <tbody>
                <tr>
                    <th>edge</th>
                    <td>Give yourself the edge.</td>
                </tr>
                <tr>
                    <th>edge Player2</th>
                    <td>Give Player2 the edge.</td>
                </tr>
                <tr>
                    <th>edge burn</th>
                    <td>Burn the edge (no one has it).</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    </div>
    <hr class="my-4"/>
    <div id="hiddenInformation" class="mb-2">
        <h4>Hidden Information</h4>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">choose [CHOICE]</h5>
            </div>
            <div class="card-body">
                Make a hidden choice (e.g. for a vote or effect).
            </div>
            <div class="card-footer">
                <table class="table table-borderless mb-0">
                    <thead>
                    <tr>
                        <th scope="col" class="py-0">Example</th>
                        <th scope="col" class="py-0">Result</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="py-0"><code>choose 5</code></td>
                        <td class="py-0">Sets your hidden choice to "5".</td>
                    </tr>
                    <tr>
                        <td class="py-0"><code>choose Aye</code></td>
                        <td class="py-0">Sets your hidden choice to "Aye".</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">reveal</h5>
            </div>
            <div class="card-body">
                Reveal and clear all hidden choices made by players.
            </div>
            <div class="card-footer">
                <table class="table table-borderless mb-0">
                    <thead>
                    <tr>
                        <th scope="col" class="py-0">Example</th>
                        <th scope="col" class="py-0">Result</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="py-0"><code>reveal</code></td>
                        <td class="py-0">Announces all hidden choices and clears them.</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="card mb-2">
            <div class="card-header bg-secondary-subtle">
                <h5 class="fs-5 mt-2">show [PLAYER] [REGION] [AMOUNT] [all]</h5>
            </div>
            <div class="card-body">
                Reveal a specific number of cards (or all cards) from your region to another player (or everyone).
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
                            <td>[PLAYER]</td>
                            <td>The recipient player. Defaults to everyone if "all" is specified, or yourself if omitted.</td>
                        </tr>
                        <tr>
                            <td>[REGION]</td>
                            <td>The source region (e.g. library, hand). Defaults to library.</td>
                        </tr>
                        <tr>
                            <td>[AMOUNT]</td>
                            <td>The number of cards to show. Defaults to 100.</td>
                        </tr>
                        <tr>
                            <td>all</td>
                            <td>Show cards to ALL players.</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="card-footer">
                <table class="table table-borderless mb-0">
                    <thead>
                    <tr>
                        <th scope="col" class="py-0">Example</th>
                        <th scope="col" class="py-0">Result</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="py-0"><code>show library all</code></td>
                        <td class="py-0">Show up to 100 cards from your library to everyone.</td>
                    </tr>
                    <tr>
                        <td class="py-0"><code>show hand Player2 all</code></td>
                        <td class="py-0">Show all cards from your hand to Player2.</td>
                    </tr>
                    <tr>
                        <td class="py-0"><code>show hand</code></td>
                        <td class="py-0">Shows your hand cards to yourself (useful for clearing public notes).</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>