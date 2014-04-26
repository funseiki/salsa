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
    state: null,
    result: null,
    client: null,
    clearResult: function() {
        this.result = "";
    },
    updateResult: function(newLine) {
        result += newLine;
    },
    parseResponse: function(response) {
        switch(response) {
            case "START_SNAPSHOT":
            case "START_RESULT":
                // Clear out the result if we're just starting to read our stream
                this.clearResult();
                this.state=response;
                break;
            case "END_SNAPSHOT":
                this.emit('snapshot', this.result);
                this.state = response;
                break;
            case "END_RESULT":
                this.emit('result', this.result);
                this.state="READY";
                break;
            case "PROCESSING_QUERY":
                break;
            default: // Data
                updateResult(response);
                break;
        }
    },
    listen: function(port) {
        var that = this;
        this.client = net.connect({port: port}, function() {
            console.log('Client connected');
            that.state = "READY";
        });
        this.client.on('data', function(data) {
            // Parse the response we've received to see if we should emit anything
            that.parseResponse(data);
        });
        this.client.on('end', function(data) {
            that.emit('end', data);
        });
        this.client.on('error', function(data) {
            that.emit('error', data);
        });
    },
    write: function(dataString) {
        if(client && this.state == "READY") {
            // Should only allow writes if we're there are no jobs being sent already
            client.write(dataString);
        }
    }
};

module.exports = DaemonListener;
