<div class="card shadow">
    <div class="card-header bg-body-secondary">
        <h5>Global Chat:</h5>
    </div>
    <div class="card-body">
        <div id="globalChatOutput" class="scrollable"></div>
        <form id="globalChatForm" action="javascript:doGlobalChat();" autocomplete='off'>
            <label for="globalChat" class="d-none">Global Chat</label>
            <input class="form-control border border-secondary-subtle" type="text" maxlength="200" id="globalChat" placeholder="Chat with players"/>
        </form>
    </div>
</div>