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
var DaemonProto = {
    state: null,
    result: null,
    client: null,
    clearResult: function() {
        this.result = "";
    },
    updateResult: function(newLine) {
        this.result += newLine;
        // Add a line break to each new line (we need some kind of separator)
        this.result += "\n";
    },
    parseLine: function(line) {
        switch(line) {
            case "START_SNAPSHOT":
            case "START_RESULT":
                // Clear out the result if we're just starting to read our stream
                this.clearResult();
                this.state=line;
                break;
            case "END_SNAPSHOT":
                this.emit('snapshot', this.result);
                this.state = line;
                break;
            case "END_RESULT":
                this.emit('result', this.result);
                this.state="READY";
                break;
            case "PROCESSING_QUERY":
                break;
            default: // Data
                this.updateResult(line);
                break;
        }
    },
    parseResponse: function(response) {
        var responseString = response.toString();
        console.log("Received response", responseString);
        var lines = responseString.split("\n");
        for(var index in lines) {
            this.parseLine(lines[index]);
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
	console.log("The DAEMON is about to WRITE: ", dataString);
        if(this.client && this.state == "READY") {
            // Should only allow writes if we're there are no jobs being sent already
            this.client.write(dataString + "\n");
	    console.log("SHOULD HAVE WRITTEN");
            return true;
        }
        else {
	    console.log("DID NOT WRITE");
            // Notify the client that we cannot perform a query yet
            return false;
        }
    }
};

// Prototype should not overwrite EventEmitter attributes
for(var attribute in DaemonProto) {
    DaemonListener.prototype[attribute] = DaemonProto[attribute];
}

module.exports = DaemonListener;
