<div class="card shadow">
    <div class="card-body position-relative">
        <div id="globalChatOutput" class="scrollable mhd-70"></div>
        <div id="newMessages"
             class="text-center p-2 text-bg-success rounded hover-success position-absolute d-none justify-content-between align-items-center"
             onclick="scrollGlobalChat();">
            <i class="bi bi-chevron-double-down"></i>
            <span>New Messages</span>
            <i class="bi bi-chevron-double-down"></i>
        </div>
        <form id="globalChatForm" action="javascript:doGlobalChat();" autocomplete='off'>
            <label for="globalChat" class="d-none">Global Chat</label>
            <input class="form-control rounded-pill border border-secondary-subtle mt-2" type="text" id="globalChat"
                   placeholder="Chat with players..."/>
        </form>
    </div>
</div>
