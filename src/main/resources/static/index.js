function show($el) {
  $el.removeAttr("hidden")
}

function hide($el) {
  $el.attr("hidden", true)
}

$(document).ready(() => {
  $("#shorturl-putnew").submit((event) => {
    // Do not attempt to submit
    event.preventDefault()
    
    // Generate URL with API
    fetch("/api/", {
      method: "POST",
      headers: {
        "Content-Type": "text/plain"
      },
      body: $("#url").val()
    }).then((response) => response.json()).then((data) => {
      let $result = $("#result")
      let $error = $("#error")

      if(data.error) {
        $error.text(data.message);
        show($error);
        hide($result);
      } else {
        $result.text(data.short);
        $result.attr("href", data.short);
        hide($error);
        show($result);
      }
    });
  });
});
