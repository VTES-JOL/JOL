<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card shadow flex-fill d-flex flex-column">
    <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span class="fw-semibold">Tournament</span>
        <span class="d-flex gap-1 align-items-center">
            <div id="tourMsg" class="badge text-bg-light me-1"></div>
            <button class="btn btn-sm btn-outline-secondary" onclick="createTournament()">Save <i class="bi-floppy"></i></button>
            <span id="publishBtnContainer" style="display:none">
                <button class="btn btn-sm btn-success" onclick="publishTournament()">Publish <i class="bi-send"></i></button>
            </span>
            <button class="btn btn-sm btn-outline-secondary" onclick="exitTourMode()">Cancel</button>
        </span>
    </div>
    <div class="card-body p-2 flex-fill overflow-auto px-3 min-h-0">
        <input type="hidden" id="originalTourName"/>
        <div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="tourName">Name</label>
                <div class="col-9"><input id="tourName" class="form-control form-control-sm"/></div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="regStart">Reg Start</label>
                <div class="col-9"><input type="date" id="regStart" name="regStart" class="form-control form-control-sm"></div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="regEnd">Reg End</label>
                <div class="col-9"><input type="date" id="regEnd" name="regEnd" class="form-control form-control-sm"></div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="playStart">Play Start</label>
                <div class="col-9"><input type="date" id="playStart" name="playStart" class="form-control form-control-sm"></div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="playEnd">Play End</label>
                <div class="col-9"><input type="date" id="playEnd" name="playEnd" class="form-control form-control-sm"></div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="numOfRounds">Rounds</label>
                <div class="col-9">
                    <select name="numOfRounds" id="numOfRounds" class="form-select form-select-sm">
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                    </select>
                </div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="reqId">VEKN ID</label>
                <div class="col-9">
                    <select name="reqId" id="reqId" class="form-select form-select-sm">
                        <option value="true">Required</option>
                        <option value="false">Not Required</option>
                    </select>
                </div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="tourFormat">Format</label>
                <div class="col-9">
                    <select name="tourFormat" id="tourFormat" class="form-select form-select-sm">
                        <option value="SINGLE_DECK">Single Deck</option>
                        <option value="MULTI_DECK">Multi-Deck</option>
                    </select>
                </div>
            </div>
            <div class="row mb-2">
                <label class="col-form-label col-3" for="gameFormat">Game</label>
                <div class="col-9">
                    <select name="gameFormat" id="gameFormat" class="form-select form-select-sm">
                        <option value="STANDARD">Standard</option>
                        <option value="V5">V5</option>
                        <option value="DUEL">Duel</option>
                        <option value="PLAYTEST">Playtest</option>
                    </select>
                </div>
            </div>
            <label for="ruleText" class="form-label small text-muted mb-1">Tournament Rules</label>
            <div class="input-group input-group-sm mb-1">
                <input id="ruleText" class="form-control form-control-sm" placeholder="Add a rule..."/>
                <button onclick="addTournamentRule()" class="btn btn-outline-secondary btn-sm">Add</button>
            </div>
            <div id="rulesDiv" class="mb-2"></div>
            <label for="specRulesCon" class="form-label small text-muted mb-1">Special Rules Condition</label>
            <textarea id="specRulesCon" name="specRulesCon" rows="3" class="form-control form-control-sm mb-2">The following JOL rules will be enforced for the duration of the rounds with the exception of the period between &lt;Date&gt; and &lt;Date&gt;.</textarea>
            <div class="input-group input-group-sm mb-1">
                <input id="specRuleText" class="form-control form-control-sm" placeholder="Add a special rule..."/>
                <button onclick="addSpecTournamentRule()" class="btn btn-outline-secondary btn-sm">Add</button>
            </div>
            <div id="specRulesDiv" class="mb-2"></div>
        </div>
    </div>
</div>
