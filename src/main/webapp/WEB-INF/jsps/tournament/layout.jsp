<div class="row mt-1 g-2">
    <div class="col-lg-4">
        <div class="card shadow">
            <div class="card-header bg-body-secondary">
                <span class="fw-semibold">Tournaments</span>
            </div>
            <ul class="list-group list-group-flush scrollable mhd-70" id="playerTournamentList"></ul>
        </div>
    </div>
    <div class="col-lg-8">
        <%-- Open tournament detail: join/leave + deck selection --%>
        <div id="openTourDetail" class="d-none">
            <div class="card shadow">
                <div class="card-header bg-body-secondary d-flex justify-content-between align-items-center">
                    <span class="fw-semibold" id="openTourName"></span>
                    <span id="openTourJoinBtn"></span>
                </div>
                <div class="card-body scrollable mhd-70">
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
        <div id="finalsTourDetail" class="d-none">
            <div class="card shadow">
                <div class="card-header bg-body-secondary">
                    <span class="fw-semibold" id="finalsTourName"></span>
                </div>
                <div class="card-body scrollable mhd-70">
                    <p class="text-muted small mb-2">You have been selected for the final table. Seeding order below — seat selection will open in turn.</p>
                    <ol id="finalsSeedingList" class="list-group list-group-numbered"></ol>
                </div>
            </div>
        </div>
    </div>
</div>
