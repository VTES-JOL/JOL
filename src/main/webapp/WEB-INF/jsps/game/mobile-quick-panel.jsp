<div class="game-quick-panel" id="gameMobileQuickPanel">

    <div class="game-quick-panel-section">
        <div class="game-quick-panel-label">Quick Chat</div>
        <div class="p-2">
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Block?')">Block?</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No block')">No block</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Yes')">Yes</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No')">No</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Wait')">Wait</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('1')">1</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('2')">2</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('3')">3</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('4')">4</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('5')">5</button>
            <br/>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre-range')">No pre-range</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No pre, no maneuver</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Long')">Long</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Close')">Close</button>
            <br/>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No grapple')">No grapple</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Wave')">Wave</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No additional strikes')">No additional strikes</button>
            <br/>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No press')">No press</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Press to continue')">Press to continue</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Press to end')">Press to end</button>
            <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Combat ends')">Combat ends</button>
            <br/>
            <button type="button" class="btn btn-outline-success m-1" onclick="sendChat('No sudden/wash')">No sudden/wash</button>
            <button type="button" class="btn btn-outline-success m-1" onclick="sendChat('No votes / No Delaying Tactics')">No votes / No DT</button>
        </div>
    </div>

    <div class="game-quick-panel-section player-only">
        <div class="game-quick-panel-label">Commands</div>
        <div class="p-2">
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn edge</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('open')">Toggle Open Hand</button>
            <button type="button" class="btn btn-success m-1" title="Gain 1 VP and 6 pool."
                    onclick="sendCommand('vp +1; pool +6')">Ousted prey!</button>
            <br/>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">Discard random</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw crypt</button>
            <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">Shuffle crypt</button>
            <hr class="my-1"/>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -6')">-6</button>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -5')">-5</button>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -4')">-4</button>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -3')">-3</button>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -2')">-2</button>
            <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -1')">-1</button>
            <span class="btn pe-none bg-secondary-subtle m-1">Pool</span>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +1')">+1</button>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +2')">+2</button>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +3')">+3</button>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +4')">+4</button>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +5')">+5</button>
            <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +6')">+6</button>
        </div>
    </div>

</div>
