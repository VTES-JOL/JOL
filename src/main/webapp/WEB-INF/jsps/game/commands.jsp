<div class="card shadow commands" id="commandCard">
    <div class="card-header bg-body-secondary">Commands</div>
    <div class="card-body p-2">
        <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm">
            <div class="align-items-center">
                <label for="phase">Phase</label>
                <select id="phase" class="form-select form-select-sm mb-2"></select>
                <label for="command">Command</label>
                <div class="input-group input-group-sm mb-2">
                    <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                            data-bs-target="#quickCommandModal" tabindex="-1">...
                    </button>
                    <input type="text" class="form-control form-control-sm" id="command">
                </div>
                <label for="chat">Chat</label>
                <div class="input-group input-group-sm mb-2">
                    <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                            data-bs-target="#quickChatModal" tabindex="-1">...
                    </button>
                    <input type="text" class="form-control form-control-sm" id="chat">
                </div>
                <label for="ping">Ping</label>
                <select id="ping" class="form-select form-select-sm mb-2"></select>
                <label for="endTurn">End</label>
                <select id="endTurn" class="form-select form-select-sm mb-2">
                    <option value="No">No</option>
                    <option value="Yes">Yes</option>
                </select>
                <button class="btn btn-secondary btn-sm" id="gameSubmit">Submit</button>
            </div>
        </form>
    </div>
</div>