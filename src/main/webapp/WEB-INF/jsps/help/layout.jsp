<div class="container-fluid pt-2">
  <h2>V:TES Online - Help</h2>
  <div class="mt-2">
    <nav class="nav nav-pills" id="helpTab" role="tablist">
      <a class="nav-link active" data-bs-toggle="tab" href="#deck" type="button" role="tab" aria-selected="true" id="help1" data-bs-target="#panel1" aria-controls="panel1">Creating a deck</a>
      <a class="nav-link" data-bs-toggle="tab" href="#join" type="button" role="tab" aria-selected="false" id="help2" data-bs-target="#panel2" aria-controls="panel2">Joining Games</a>
      <a class="nav-link" data-bs-toggle="tab" href="#gameInfo" type="button" role="tab" aria-selected="false" id="help6" data-bs-target="#panel6" aria-controls="panel6">Game Information</a>
      <a class="nav-link" data-bs-toggle="tab" href="#cards" type="button" role="tab" aria-selected="false" id="help3" data-bs-target="#panel3" aria-controls="panel3">Moving cards</a>
      <a class="nav-link" data-bs-toggle="tab" href="#counters" type="button" role="tab" aria-selected="false" id="help4" data-bs-target="#panel4" aria-controls="panel4">Managing counters</a>
      <a class="nav-link" data-bs-toggle="tab" href="#cardInfo" type="button" role="tab" aria-selected="false" id="help5" data-bs-target="#panel5" aria-controls="panel5">Card Information</a>
      <a class="nav-link" data-bs-toggle="tab" href="#tips" type="button" role="tab" aria-selected="false" id="help7" data-bs-target="#panel7" aria-controls="panel7">Tips</a>
    </nav>
    <div class="tab-content p-2 text-bg-light bg-secondary-subtle" id="helpContent">
      <jsp:include page="creating-deck.jsp"/>
      <jsp:include page="joining-games.jsp"/>
      <jsp:include page="card-information.jsp"/>
      <jsp:include page="moving-cards.jsp"/>
      <jsp:include page="managing-counters.jsp"/>
      <jsp:include page="game-infomation.jsp"/>
      <jsp:include page="tips.jsp"/>
    </div>
  </div>
</div>

<script>
  $(document).ready(function() {
    tippy("a.card-name", {
      placement: 'auto',
      allowHTML: true,
      appendTo: () => document.body,
      popperOptions: {
        strategy: 'fixed',
        modifiers: [
          {
            name: 'flip',
            options: {
              fallbackPlacements: ['bottom', 'right'],
            },
          },
          {
            name: 'preventOverflow',
            options: {
              altAxis: true,
              tether: false,
            },
          },
        ],
      },
      onTrigger: function (instance, event) {
        event.stopPropagation();
      },
      theme: "light",
      touch: "hold",
      onShow: function (instance) {
        tippy.hideAll({exclude: instance});
        instance.setContent("Loading...");
        let ref = $(instance.reference);
        let cardId = ref.data('card-id');
        let content = '<img width="350" height="500" src="https://static.tornsignpost.org/images/' + cardId + '" alt="Loading..."/>';
        instance.setContent(content);
      }
    });
    let targetAnchor = $(window.location.hash);
    let tabId = targetAnchor.closest('.tab-pane').attr('id');
    $("#helpTab").find('a[data-bs-target="#' + tabId + '"]').click();
    let hash = window.location.hash;

    $(".nav-pills").find("a").each(function(key, value) {
      if (hash === $(value).attr('href')) {
        $(value).tab('show');
      }

      $(value).click(function() {
        location.hash = $(this).attr('href');
      });
    });
  })
</script>
