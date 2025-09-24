<div class="row mt-2">
    <div class="col-lg-4 col-md-6">
        <div class="card shadow">
            <div class="card-header bg-body-secondary">
                <h5>Cardinal Benediction 2025</h5>
            </div>
            <div class="card-body">
                <h6>Rules</h6>
                <p><strong>Rounds Start:</strong>1st April 2025 - 00:00 UTC</p>
                <p><strong>Rounds End:</strong> 30th June 2025 - 23:59 UTC</p>
                <p><strong>Finals Start:</strong>10th July 2025 - 00:00 UTC</p>
                <p><strong>Finals End:</strong> 10th October 2025 - 23:59 UTC</p>
                <p><strong>Format:</strong>Single deck online standard constructed 3 rounds + final, each round to be
                    played concurrently</p>
                <p><strong>Requirements:</strong> You will need a valid VEKN ID linked against your JOL Profile</p>
                <p><strong>Special Rules:</strong>
                <ul>
                    <li>Always indicated in Global Notes of your game if IRL events are preventing you for playing.</li>
                    <li>players are expected to connect TWICE a day during week days (Mondays to Fridays) to ensure
                        interaction with the tables are not delayed
                    </li>
                    <li>players can interact and play during weekend</li>
                </ul>
                <p><strong>Additional Rules - Monday -> Friday</strong></p>
                <ul>
                    <li>
                        if a Methuselah hasn't put anything in global notes about being away, then 24 hours without
                        responding is considered a non-response and the game can continue. They will then have another -
                        24 hours additional time where they need to respond.
                        However, if 72 hours pass after that first time they need to respond and we haven't heard
                        anything from them, we consider them to have left the game. Play can continue without having to
                        wait each time for them to respond.
                    </li>
                    <li>
                        The player can come back to the game, the 24h will still apply, but this time after 48 hours
                        without responding, then the game will continue like the player left.
                    </li>
                    <li>
                        For any question or query about applying this, players can call a judge who is not playing at
                        the table to make a decision.
                    </li>
                    <li>
                        Ensure you are clear on declarations of actions, blocks and/or terms of political actions
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <div class="col-lg-4 col-md-6">
        <div id="registrationPanel">
            <div class="card shadow">
                <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
                    <h5 class="d-inline">Register Tournament Deck</h5>
                    <span class="badge badge-sm badge-light" id="validId" style="display: none;">VEKN Registered: &check;</span>
                </div>
                <div class="card-body" id="tournamentRegistration">
                    <span><strong>Note:</strong> Only valid decks in Modern format are displayed here.</span><br/>
                    <label for="tournamentRound1" class="form-label">Deck</label>
                    <select id="tournamentRound1" class="form-select"></select>
                    <%--          <label for="tournamentRound2" class="form-label">Round 2</label>--%>
                    <%--          <select id="tournamentRound2" class="form-select"></select>--%>
                    <%--          <label for="tournamentRound3" class="form-label">Round 3</label>--%>
                    <%--          <select id="tournamentRound3" class="form-select"></select>--%>
                    <button id="tournamentRegisterButton" onclick="registerforTournament()"
                            class="btn btn-outline-secondary btn-sm mt-2">
                        Register
                    </button>
                    <br/>
                    <span id="tournamentRegistrationResult"></span>
                </div>
            </div>
        </div>
        <div id="registrationMessage" class="card shadow">
            <div class="card-header bg-body-secondary">
                <h5>Registration</h5>
            </div>
            <div class="card-body">
                To register for this tournament you will need a VEKN ID added to your <a href="#"
                                                                                         onclick="doNav('profile')">Profile</a>page
                first.
            </div>
        </div>
    </div>
    <div class="col-lg-4 col-md-6 mt-2 mt-lg-0">
        <div class="card">
            <div class="card-header bg-body-secondary">
                <h5>Current Registrations</h5>
            </div>
            <table class="card-body table table-sm table-bordered table-hover mb-0">
                <thead>
                <tr>
                    <th>Player Name</th>
                </tr>
                </thead>
                <tbody id="playerRegistrations">

                </tbody>
            </table>
        </div>
    </div>
</div>
