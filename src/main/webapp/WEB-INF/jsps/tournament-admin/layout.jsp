<div class="row mt-1 g-2">
    <div class="col-lg-4">
        <jsp:include page="tournament-list.jsp"/>
    </div>
    <div class="col-lg-8">
        <div class="d-none" id="tourEditCol">
            <jsp:include page="tournament-admin.jsp"/>
        </div>
        <div class="d-none" id="tourTablesCol">
            <jsp:include page="tournament-manager.jsp"/>
        </div>
    </div>
</div>
