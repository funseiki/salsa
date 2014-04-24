/****************************************************************************
 * The portion of the webserver that will listen for socket.io request
 *  Messages from the client will be relayed to the daemon listener
 ****************************************************************************/
var socketio = require("socket.io");

exports.listen = function(server) {
    var io = socketio.listen(server);
    io.sockets.on('connection', function(socket) {
        socket.emit('news', {hello: 'world'});
        socket.on('pokemon', function(data) {
            console.log(data);
        });
        socket.emit('attributes', ['attr1', 'attr2', 'att3']);
    });

    return server;
};
