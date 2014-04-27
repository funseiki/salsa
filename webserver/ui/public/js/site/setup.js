var socketHandler = {
    state: 'NONE',
    setupListeners: function(socket) {
        this.socket = socket;
        var that = this;
        socket.emit('query', {type: 'ATTRIBUTE_LIST'});
        socket.on('error', function() {
            console.log("Error with the socket");
        });

        socket.on('client_error', function(message) {
            console.log(message);
        });

        socket.on('result', function(results) {
            console.log(results);
            switch(that.state) {
                case 'SUM':
                case 'AVERAGE':
                    visualizer.update(results.data);
                    visualizer.done();
                    break;
                case 'ATTRIBUTE_LIST':
                    guiBuilder.buildAttributeList(results.data);
                    break;
                case 'TUPLES':
                    guiBuilder.buildTupleList(results.data);
                    break;
                default:
                    break;
            }
        });

        socket.on('snapshot', function(results) {
            visualizer.update(results.data);
        });
    },
    uiListeners: function() {
        // Method for setting up listeners to the UI
    }
};

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
