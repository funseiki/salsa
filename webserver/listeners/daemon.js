/****************************************************************************
 * The portion of the webserver that will listen in on a local port.
 *  It will relay messages retrieved by the web server to the daemon process
 ****************************************************************************/
var net = require('net');

exports.listen = function(port){
    var client = net.connect({port: port}, function() {
        console.log('Client connected');
        //client.write('sum 2 2');
    });

    client.on('data', function(data) {
        console.log("Response received: " + data.toString());
        client.end();
    });

    client.on('end', function() {
        console.log('client disconnected');
    });

    client.on('error', function(err) {
        console.error(err);
    });
};
