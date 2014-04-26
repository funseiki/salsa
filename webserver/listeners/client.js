/****************************************************************************
 * The portion of the webserver that will listen for socket.io request
 *  Messages from the client will be relayed to the daemon listener
 ****************************************************************************/
var socketio = require("socket.io"),
    daemon = new require("./daemon")();

exports.listen = function(server, daemonPort) {
    var io = socketio.listen(server);

    if(daemonPort) {
        daemon.listen(daemonPort);

        // The daemon API
        daemon.on('data', function(data) {
            console.log("Response received: " + data.toString());
            daemon.end();
        });

        daemon.on('end', function() {
            console.log('daemon disconnected');
        });

        daemon.on('error', function(err) {
            console.error(err);
        });

        daemon.on("data", function(data) {
            console.log(data);
        });
    }

    io.sockets.on('connection', function(socket) {
        // The client API
        socket.emit('news', {hello: 'world'});

        socket.on('query', function(query) {
            console.log(data);
            // TODO: First make sure we're not already sending a query
            daemon.write(query);
        });
        socket.emit('attributes', ['attr1', 'attr2', 'att3']);
    });

    return server;
};
