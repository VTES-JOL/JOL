<div class="row g-2 flex-fill align-items-stretch" style="min-height: 0">
    <div class="col-lg-4 d-flex flex-column">
        <div class="card shadow flex-fill d-flex flex-column">
            <div class="card-header bg-body-secondary">
                <span class="fw-semibold">Tournaments</span>
            </div>
            <ul class="list-group list-group-flush flex-fill overflow-auto" style="min-height: 0"
                id="playerTournamentList"></ul>
        </div>
    </div>
    <div class="col-lg-8 d-flex flex-column">
        <%-- Open tournament detail: join/leave + deck selection --%>
        <div id="openTourDetail" class="d-none d-flex flex-column flex-fill">
            <div class="card shadow flex-fill d-flex flex-column">
                <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
                    <span class="fw-semibold" id="openTourName"></span>
                    <span id="openTourJoinBtn"></span>
                </div>
                <div class="card-body flex-fill overflow-auto" style="min-height: 0">
                    <div id="openTourRules" class="mb-3"></div>
                    <div id="openTourDeckSection" class="d-none">
                        <h6 class="fw-semibold mb-2">Deck Selection</h6>
                        <div class="d-flex align-items-center gap-2 mb-3">
                            <span id="openTourDeckLabel" class="text-muted small"></span>
                            <div class="dropdown" id="openTourDeckDropdown"></div>
                        </div>
                        <div id="openTourDeckPreview"></div>
                    </div>
                </div>
            </div>
        </div>
        <%-- Finals invite detail: seeding panel --%>
        <div id="finalsTourDetail" class="d-none d-flex flex-column flex-fill">
            <div class="card shadow flex-fill d-flex flex-column">
                <div class="card-header bg-body-secondary">
                    <span class="fw-semibold" id="finalsTourName"></span>
                </div>
                <div class="card-body flex-fill overflow-auto" style="min-height: 0">
                    <p class="text-muted small mb-2">You have been selected for the final table. Seeding order below —
                        seat selection will open in turn.</p>
                    <ol id="finalsSeedingList" class="list-group list-group-numbered"></ol>
                </div>
            </div>
        </div>
    </div>
</div>
