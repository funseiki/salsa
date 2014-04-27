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
