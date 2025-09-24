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
</div>