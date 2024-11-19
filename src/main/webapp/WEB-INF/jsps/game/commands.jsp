<div class="card shadow mt-2 mt-md-0">
  <div class="card-header bg-body-secondary">Commands</div>
  <div class="card-body">
    <form onsubmit="return doSubmit()" autocomplete="off" id="gameForm">
      <div class="row gy-1 align-items-center">
        <div class="col-md-2">
          <label for="phase">Phase</label>
        </div>
        <div class="col-md-10">
          <select id="phase" class="form-select form-select-sm"></select>
        </div>
        <div class="col-md-2">
          <label for="command">Command</label>
        </div>
        <div class="col-md-10">
          <div class="input-group input-group-sm">
            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                    data-bs-target="#quickCommandModal" tabindex="-1">...
            </button>
            <input type="text" class="form-control form-control-sm" id="command">
          </div>
        </div>
        <div class="col-md-2">
          <label for="chat">Chat</label>
        </div>
        <div class="col-md-10">
          <div class="input-group input-group-sm">
            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal"
                    data-bs-target="#quickChatModal" tabindex="-1">...
            </button>
            <input type="text" class="form-control form-control-sm" id="chat">
          </div>
        </div>
        <div class="col-md-2 col-1">
          <label for="ping">Ping</label>
        </div>
        <div class="col-md-4 col-5">
          <select id="ping" class="form-select form-select-sm"></select>
        </div>
        <div class="col-md-1 col-1">
          <label for="endTurn">End</label>
        </div>
        <div class="col-md-3 col-5">
          <select id="endTurn" class="form-select form-select-sm">
            <option value="No">No</option>
            <option value="Yes">Yes</option>
          </select>
        </div>
        <div class="col-md-2">
          <button class="btn btn-secondary btn-sm" id="gameSubmit">Submit</button>
        </div>
      </div>
    </form>
  </div>
</div>