var express = require('express')
    , http = require('http')
    , path = require('path')
    , client = require('./listeners').client
    , config = require('./config');

var app = express();

// Configuration stuff
app.set('port', config.server.port);
app.use(express.static(path.join(__dirname, "ui/public")));

// Endpoint for grabbing information about this server (like its url... which, in retrospect the client should know.. but.. #localhost_problems)
app.get('/serverinfo', function(req, res) {
    res.json({
        ip: config.server.ip,
        port: config.server.port
    });
});

// Send index.html on '/' requests
app.get("/", function(req, res) {
    res.sendfile(path.join(__dirname, "ui/public/index.html"));
});

var server = http.createServer(app);

// Start the client
client.listen(server, config.daemon.port);
server.listen(app.get('port'), function() {
    console.log("Express server listening on", app.get('port'));
});
