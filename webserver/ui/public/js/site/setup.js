function setupSocket(url, callbacks) {
    var socket = io.connect(url);
    $.each(callbacks, function(index, callback) {
        // Free sockets!
        callback(socket);
    });
}

$(document).ready(function() {
    $.get('/serverinfo', function(data, status, xhr) {
        var url = data.ip + ":" + data.port;
        setupSocket(url, [socketHandler.setupListeners.bind(socketHandler)]);
    });
});

$('#collapseOne').collapse('hide');

$('#myTab a').click(function (e) {
  e.preventDefault()
  $(this).tab('show')
});

$('.submit').click(function(){
    guiBuilder.addTab("Test");
});

$(".nav-tabs").on('click','button', function(){
    var tab = $(this);
    guiBuilder.deleteTab(tab);
});