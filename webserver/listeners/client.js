/****************************************************************************
 * The portion of the webserver that will listen for socket.io request
 *  Messages from the client will be relayed to the daemon listener
 ****************************************************************************/
var socketio = require("socket.io"),
    DaemonListener = require("./daemon");
var daemon = new DaemonListener();

exports.listen = function(server, daemonPort) {
    var io = socketio.listen(server);

    if(daemonPort) {
        daemon.listen(daemonPort);

        // The daemon API
        daemon.on('snapshot', function(data) {
	    console.log("GOT SNAPSHOT: ", data.toString());
            // Send the snapshot result to all connected clients
            io.sockets.emit('snapshot', {'data': data});
        });

        daemon.on('result', function(data) {
	    console.log("GOT RESULT: ", data.toString());
            // Send the snapshot result to all connected clients
            io.sockets.emit('result', {'data': data});
        });

        daemon.on('end', function() {
            console.log('daemon disconnected');
        });

        daemon.on('error', function(err) {
            console.error(err);
            io.sockets.emit('error');
        });

        // Notify clients that the data is still being processed
        daemon.on('processing', function() {
            io.sockets.emit('client_error', {message: 'Query still processing'});
        });
    }

    io.sockets.on('connection', function(socket) {
        socket.on('query', function(queryParams) {
	    console.log(queryParams);
            var query = null;
            switch(queryParams.type) {
                case 'SUM':
                case 'AVERAGE':
                    var groupBy = -1;
                    if(queryParams.groupBy) {
                        groupBy = queryParams.groupBy;
                    }
                    query = queryParams.type + " " + queryParams.column + " " + groupBy;
                    break;
                case 'ATTRIBUTE_LIST':
                    query = 'ATTRIBUTE_LIST';
                    break;
                case 'TUPLES':
                    query = 'TUPLES';
                    break;
            }
            if(!query) {
                // The query was not formatted correctly
                socket.emit('client_error', {message: "Query " + queryParams +" not recognized"});
            }
            else if(!daemon.write(query)) {
                // Notify the socket that the query was not accepted
                socket.emit('client_error', {message: 'Query still processing'});
            }
        });
    });

    return server;
};
