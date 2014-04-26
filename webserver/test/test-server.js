/************************************
 * Test socket server.
 * Placedholder for the Java daemon process
 ************************************/

var net = require('net');

var server = net.createServer(function(socket) {
    console.log('Client connection received');
});

var port = process.argv[2] ? process.argv[2] : '8081';

server.listen(port, function() {
    var address = server.address();
    console.log("Opened a server on %j", address);
});

server.on('connection', function(socket) {
    socket.on('data', function(data) {
        console.log('Server received: ', data.toString());
        socket.write('Hello from the server\n');
    });
    socket.on('end', function() {
        console.log('Client is disconnecting');
    });
    socket.on('error', function(error) {
        console.log(error);
    });
});

server.on('error', function(error) {
    console.log(error);
});
