<div class="modal" id="quickChatModal" tabindex="-1" role="dialog" aria-labelledby="quickChatModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="quickChatModalLabel">
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    <span>Quick Chat</span>
                </h5>
            </div>
            <div class="modal-body">
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Bleed')">Bleed</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Hunt')">Hunt</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Block?')">Block?</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No block')">No block
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Blocked')">Blocked
                </button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Yes')">Yes</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('No')">No</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('Wait')">Wait</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('1')">1</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('2')">2</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('3')">3</button>
                <button type="button" class="btn btn-outline-secondary m-1" onclick="sendChat('4')">4</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre-range')">No
                    pre-range
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No maneuver')">No maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No pre, no maneuver')">No
                    pre, no maneuver
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Long')">Long</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No grapple')">No grapple
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 1')">H1</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 2')">H2</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Hands for 3')">H3</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Wave')">Wave</button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No additional strikes')">No
                    additional strikes
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('No press')">No press
                </button>
                <button type="button" class="btn btn-outline-danger m-1" onclick="sendChat('Combat ends')">Combat ends
                </button>
                <button type="button" class="btn btn-outline-success m-1" onclick="sendChat('No sudden/wash')">No
                    sudden/wash
                </button>
            </div>
        </div>
    </div>
</div>
