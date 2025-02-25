<div class="modal" id="quickCommandModal" tabindex="-1" role="dialog" aria-labelledby="quickCommandModalLabel" aria-hidden="true">
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
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('unlock')">Unlock
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge')">Edge</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('edge burn')">Burn
                    edge
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw')">Draw</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('draw crypt')">Draw
                    crypt
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('discard random')">
                    Discard random
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle')">Shuffle
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendCommand('shuffle crypt')">
                    Shuffle crypt
                </button>

                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +1')">Pool +1
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +2')">+2</button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendCommand('pool +3')">+3</button>

                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -1')">Pool -1
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -2')">-2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendCommand('pool -3')">-3</button>
            </div>
        </div>
    </div>
</div>