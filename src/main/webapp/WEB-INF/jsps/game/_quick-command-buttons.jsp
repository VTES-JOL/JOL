<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn edge</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('open')">Toggle Open Hand</button>
<button type="button" class="btn btn-success m-1" title="Gain 1 VP and 6 pool."
        onclick="sendCommand('vp +1; pool +6')">Ousted prey!</button>
<br/>
<span class="m-1 d-inline btn pe-none bg-secondary-subtle">Library/Hand</span>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">Discard random</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle</button>
<span class="m-1 d-inline btn pe-none bg-secondary-subtle">Crypt</span>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw crypt</button>
<button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">Shuffle crypt</button>
<hr/>
<div class="d-lg-inline d-block">
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -6')">-6</button>
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -5')">-5</button>
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -4')">-4</button>
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -3')">-3</button>
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -2')">-2</button>
    <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -1')">-1</button>
</div>
<span class="d-lg-inline btn pe-none bg-secondary-subtle m-2">Pool</span>
<div class="d-lg-inline d-block">
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +1')">+1</button>
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +2')">+2</button>
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +3')">+3</button>
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +4')">+4</button>
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +5')">+5</button>
    <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +6')">+6</button>
</div>
