<div class="row g-2 flex-fill align-items-stretch" style="min-height: 0">
    <div class="col-lg-4 d-flex flex-column">
        <jsp:include page="tournament-list.jsp"/>
    </div>
    <div class="col-lg-8 d-flex flex-column">
        <div class="d-none d-flex flex-column flex-fill" id="tourEditCol">
            <jsp:include page="tournament-admin.jsp"/>
        </div>
        <div class="d-none d-flex flex-column flex-fill" id="tourTablesCol">
            <jsp:include page="tournament-manager.jsp"/>
        </div>
    </div>
</div>
