/****************************************************************************
 * The portion of the webserver that will listen in on a local port.
 *  It will relay messages retrieved by the web server to the daemon process
 ****************************************************************************/
var net = require('net'),
    EventEmitter = require("events").EventEmitter,
    util = require('util');

function DaemonListener() {
    EventEmitter.call(this);
}

util.inherits(DaemonListener, EventEmitter);

// Thin wrapper around 'net' object
DaemonListener.prototype = {
    listening: false,
    client: null,
    listen: function(port) {
        var that = this;
        this.client = net.connect({port: port}, function() {
            console.log('Client connected');
            that.listening = true;
        });

        // Todo: Wrap all this events to higher level methods
        this.client.on('data', function(data) {
            that.emit('data', data);
        });
        this.client.on('end', function(data) {
            that.emit('end', data);
        });
        this.client.on('error', function(data) {
            that.emit('error', data);
        });
    },
    write: function(dataString){
        if(client) {
            client.write(dataString);
        }
    }
};

module.exports = DaemonListener;
