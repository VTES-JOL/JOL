<div class="row">
    <div class="col-sm-6">
        <h4 class="header">Rules</h4>
        <div class="light padded">
            <h4>SchreckNET 2024/2025</h4>
            <p><strong>Rounds Start:</strong> 7th November 2024 - 0:00 UTC</p>
            <p><strong>Rounds End:</strong> 21st February 2025 - 23:59 UTC</p>
            <p><strong>Format:</strong> 3R + Final, Multi-deck, each round to be played concurrently</p>
            <p><strong>Requirements:</strong> You will need a valid VEKN ID linked against your JOL Profile</p>
            <p><strong>Special Rules:</strong>
            <ul>
                <li>
                    If a Methuselah hasn't put anything in global notes about being away, then 24 hours without
                    responding is considered a non-response and the game can continue.
                    They will then have another 24 hours additional time when they need to respond.
                    However, if 72 hours pass after that first time they need to respond, and we haven't heard anything
                    from them, we consider them to have left the game.
                </li>
                <li>
                    Play can continue without having to wait each time for them to respond.<br/>
                    The player can come back to the game, the 24h will still apply, but this time after 48 hours without
                    responding, hen the game will continue like the player left.
                </li>
                <li>
                    For any question or query about applying this, players can call a judge who is not playing at the
                    table to make a decision.
                </li>
                <li>
                    Ensure you are clear on declarations of actions, blocks and/or terms of political actions
                </li>
            </ul>
        </div>
    </div>
    <div class="col-sm-6">
        <div id="registrationPanel" class="row">
            <div class="col-6">
                <h4 class="header">
                    Register Tournament Deck
                    <span class="label float-right label-small label-warning" id="validId" style="display: none;">VEKN Registered: &check;</span>
                </h4>
                <div class="light padded" id="tournamentRegistration">
                    <span><strong>Note:</strong> Only valid decks in Modern format are displayed here.</span><br/>
                    <label for="tournamentRound1">Round 1</label>
                    <select id="tournamentRound1"></select>
                    <br/>
                    <label for="tournamentRound2">Round 2</label>
                    <select id="tournamentRound2"></select>
                    <br/>
                    <label for="tournamentRound3">Round 3</label>
                    <select id="tournamentRound3"></select>
                    <br/>
                    <button id="tournamentRegisterButton" onclick="registerforTournament()" class="btn btn-primary">Register</button>
                    <br/>
                    <span id="tournamentRegistrationResult"></span>
                </div>
            </div>
            <div class="col-6">
                <h4 class="header">Current Registrations</h4>
                <table class="clean-table light">
                    <thead>
                    <tr><th>Player Name</th></tr>
                    </thead>
                    <tbody id="playerRegistrations"></tbody>
                </table>
            </div>
        </div>
        <div id="registrationMessage">
            <h4 class="header">Registration</h4>
            <div class="light padded">
                To register for this tournament you will need a VEKN ID added to your <a href="#" onclick="doNav('profile')">Profile</a> page first.
            </div>
        </div>
    </div>
</div>
