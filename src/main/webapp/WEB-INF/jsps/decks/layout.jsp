<div class='row mt-2 g-2'>
    <div class='col-md-4 col-lg-3'>
        <jsp:include page="deck-list.jsp"/>
    </div>
    <div class="col-md-8 col-lg-9">
        <div class="row g-2">
            <div class="col-12 d-none" id="deckEditorCol">
                <jsp:include page="deck-editor.jsp"/>
            </div>
            <div class="col-12" id="deckPreviewCol">
                <jsp:include page="deck-preview.jsp"/>
            </div>
        </div>
    </div>
</div>
