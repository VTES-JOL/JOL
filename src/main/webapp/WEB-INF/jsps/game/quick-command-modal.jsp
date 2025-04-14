<div class="modal modal-lg" id="quickCommandModal" tabindex="-1" role="dialog" aria-labelledby="quickCommandModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickCommandModalLabel">
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Command</span>
                </h5>
            </div>
            <div class="modal-body">
                <div>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock</button>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn edge</button>
                    <button type="button" class="btn btn-warning m-1" title="Gain 1 VP and 6 pool." onclick="sendCommand('vp +1; pool +6')">Ousted prey!</button>
                </div>
                <h6 class="m-1 d-inline btn pe-none bg-secondary-subtle">Library/Hand</h6>
                <div class="d-inline">
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">Discard random</button>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle</button>
                </div>
                <h6 class="m-1 d-inline btn pe-none bg-secondary-subtle">Crypt</h6>
                <div class="d-inline">
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw crypt</button>
                    <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">Shuffle crypt</button>
                </div>
                <hr/>
                <div>
                    <div class="d-lg-inline d-block">
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -6')">-6</button>
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -5')">-5</button>
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -4')">-4</button>
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -3')">-3</button>
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -2')">-2</button>
                        <button type="button" class="btn btn-outline-danger bg-danger-subtle m-1" onclick="sendCommand('pool -1')">-1</button>
                    </div>
                    <h6 class="d-lg-inline btn pe-none bg-secondary-subtle m-2">Pool</h6>
                    <div class="d-lg-inline d-block">
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +1')">+1</button>
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +2')">+2</button>
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +3')">+3</button>
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +4')">+4</button>
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +5')">+5</button>
                        <button type="button" class="btn btn-outline-success bg-success-subtle m-1" onclick="sendCommand('pool +6')">+6</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>